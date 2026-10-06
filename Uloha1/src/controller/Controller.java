package controller;

import view.Canvas;

import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

import graphics.rasterizer.LineRasterizer;
import graphics.rasterizer.TrivialLineRasterizer;
import model.Line;
import model.Point;
import model.Polygon;

import java.awt.Color;
/**
 * Handles user input and controls the application flow related to the {@link Canvas}.
 * The controller coordinates input events, canvas operations, and rendering updates.
 *
 * @author PGRF FIM UHK
 * @version 2026
 */
public class Controller {

    private final Canvas canvas;
    private final LineRasterizer raster;
    private Point startPointDrag = null;
    private Point currentPointDrag = null;

    private Point startPoint = null;
    private Point lastPoint = null;
    private Point currentPoint = null;
    private Point trackerPoint = null;

    private final int LINE_COLOR = Color.WHITE.getRGB();
    private final int PREVIEW_COLOR = Color.RED.getRGB();
    private final List<Line> lines = new ArrayList<Line>();
    private final List<Polygon> polygons = new ArrayList<Polygon>();
    private Polygon currentPolygon = new Polygon();

    private boolean editMode = false;
    private int indexOfEditedPoint = -1;
    private Polygon selectedPolygon = null;


    /* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- Constructors -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= */

    public Controller(Canvas canvas) {
        this.canvas = canvas;
        this.raster = new TrivialLineRasterizer(canvas.getRaster());
    }

    /* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= Main functions -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- */
    
    public void init() {
        canvas.clear();

        //---------------------------Clear---------------------------
        canvas.addKeyListener(new KeyAdapter() {
            @Override 
            public void keyPressed(KeyEvent e)
            {
                if(e.getKeyCode() == KeyEvent.VK_C) {
                    lines.clear();
                    canvas.clear();
                    canvas.repaint();
                    startPointDrag = null;
                    currentPointDrag = null;
                    startPoint = null;
                    currentPoint = null;
                    lastPoint = null;
                    polygons.clear();
                    currentPolygon = new Polygon();
                }

                if(e.getKeyCode() == KeyEvent.VK_E) {
                    editMode = !editMode;
                }

                if(e.getKeyCode() == KeyEvent.VK_T) {
                    Polygon p = new Polygon();
                    p.addPoint(new Point(50, 50));
                    p.addPoint(new Point(50, 100));
                    p.addPoint(new Point(100, 100));
                    p.addPoint(new Point(100, 50));
                    polygons.add(p);
                    render();
                }
                    
            }
        });

        canvas.addMouseListener(new MouseAdapter(){
            @Override 
            public void mousePressed(MouseEvent e)
            {
                if(editMode)
                {
                    var overlappingPoint = checkPointsOverlapping(getPoint(e));
                    if(overlappingPoint == null) return;
                    selectedPolygon = overlappingPoint.first();
                    Point selectedPoint = overlappingPoint.second();
                    var selectedPolygonsPoints = selectedPolygon.getPoints();
                    indexOfEditedPoint = selectedPolygonsPoints.indexOf(selectedPoint);
                    //Point previousPoint = selectedPolygonsPoints.get(indexOfEditedPoint - 1 < 0 ? selectedPolygonsPoints.size() - 1 : indexOfEditedPoint - 1);
                    //Point nextPoint = selectedPolygonsPoints.get((indexOfEditedPoint + 1) % selectedPolygonsPoints.size());

  
                }
                else
                {
                    startPointDrag = new Point(e.getX(), e.getY());
                    currentPointDrag = new Point(e.getX(), e.getY());
                }
                
            }

            @Override 
            public void mouseReleased(MouseEvent e)
            {
                if(editMode)
                {
                    selectedPolygon = null;
                    indexOfEditedPoint = -1;
                }
                else if(startPointDrag == null || currentPointDrag == null)
                {
                    return;
                }
                else
                {
                    lines.add(new Line(startPointDrag, getPoint(e), LINE_COLOR));
                    currentPointDrag = null;
                    startPointDrag = null;
                    render();
                }
                
            }

            //---------------------------Polygon point detection---------------------------
            @Override
            public void mouseClicked(MouseEvent e)
            {
      
                if(!editMode)
                {    
                    if(startPoint == null)
                    {
                        
                        startPoint = getPoint(e);
                        currentPoint = getPoint(e);
                        lastPoint = getPoint(e);
                        currentPolygon.addPoint(startPoint);
                    }
                    else
                    {
                        lastPoint = currentPoint;
                        currentPoint = getPoint(e);
                        
                        
                        if(pointsOverlapping(startPoint, currentPoint))
                        {
                            currentPoint = null;
                            startPoint = null;
                            lastPoint = null;
                            polygons.add(currentPolygon);
                            System.out.println("Polygon added: " + currentPolygon);
                            currentPolygon = new Polygon();
                        }
                        else
                        {
                            currentPolygon.addPoint(currentPoint);
                        }
                        

                        render();
                    }
                }
                
            }

            
        }); 

        canvas.addMouseMotionListener(new MouseAdapter(){
            @Override 
            public void mouseDragged(MouseEvent e)
            {
                if(editMode)
                {
                    currentPointDrag = getPoint(e);
                    if(selectedPolygon != null && indexOfEditedPoint != -1)
                    {
                        selectedPolygon.getPoints().set(indexOfEditedPoint, currentPointDrag);
                        render();
                    }

                }
                else
                {
                    canvas.clear();
                    render();
                    currentPointDrag = getPoint(e);
                    raster.rasterize(new Line(startPointDrag, currentPointDrag, PREVIEW_COLOR));
                    canvas.repaint();
                }
                
                
            }

            @Override 
            public void mouseMoved(MouseEvent e)
            {
                canvas.clear();
                render();
                trackerPoint = getPoint(e);
                raster.rasterize(new Line(currentPoint, trackerPoint, PREVIEW_COLOR));
                raster.rasterize(new Line(trackerPoint, startPoint, PREVIEW_COLOR));
                canvas.repaint();


            }
        });

        
        canvas.repaint();
    }

    ///-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= Helper functions -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
    private Point getPoint(MouseEvent e) {
        return new Point(e.getX(), e.getY());
    }

    //----------------------------Rendering----------------------------
    private void render() {
        canvas.clear();
        for(Line line : lines) {
            raster.rasterize(line);
        }


        for(Polygon p : polygons)
        {
            ArrayList<Point> points = p.getPoints();
            int size = points.size();
            for(int i = 0; i < size; i++)
            {
                raster.rasterize(new Line(points.get(i), points.get((i+1) % size), LINE_COLOR));
            }
        }

        if(currentPolygon.getPoints().size() > 0)
        {
            ArrayList<Point> points = currentPolygon.getPoints();
            int size = points.size();
            for(int i = 0; i < size - 1; i++)
            {
                raster.rasterize(new Line(points.get(i), points.get((i+1) % size) , LINE_COLOR));
            }
        }
        canvas.repaint();
    }

    //----------------------------Point overlapping detection----------------------------
    private boolean pointsOverlapping(Point p1, Point p2)
    {
        return Math.abs(p1.getX() - p2.getX()) < 10 && Math.abs(p1.getY() - p2.getY()) < 10;
    }
    
    //----------------------------Point overlapping detection for polygons----------------------------
    private Pair<Polygon, Point> checkPointsOverlapping(Point p)
    {
        for(Polygon pol : polygons)
        {
            for(Point polP : pol.getPoints())
            {
                if(pointsOverlapping(polP, p))
                {
                    return new Pair<Polygon, Point>(pol, polP);
                }
            }
        }
        
        return null;
    }

    public record Pair<F, S>(F first, S second) {} //std::pair copy
    

}