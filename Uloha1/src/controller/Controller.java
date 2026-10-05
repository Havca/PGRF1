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
    private final Polygon p = new Polygon();
    private boolean editMode = false;


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
                }

                if(e.getKeyCode() == KeyEvent.VK_E) {
                    editMode = !editMode;
                }
            }
        });

        

        canvas.addMouseListener(new MouseAdapter(){
            @Override 
            public void mousePressed(MouseEvent e)
            {
                startPointDrag = new Point(e.getX(), e.getY());
                currentPointDrag = new Point(e.getX(), e.getY());
            }

            @Override 
            public void mouseReleased(MouseEvent e)
            {
                lines.add(new Line(startPointDrag, getPoint(e), LINE_COLOR));
                currentPointDrag = null;
                startPointDrag = null;
                render();
            }

            //---------------------------Polygon point detection---------------------------
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if(editMode)
                {
                    Point overlappingPoint = checkPointsOverlapping(getPoint(e));
                    
                }
                else
                {
                    if(startPoint == null)
                    {
                        
                        startPoint = getPoint(e);
                        currentPoint = getPoint(e);
                        lastPoint = getPoint(e);
                        p.addPoint(startPoint);
                    }
                    else
                    {
                        lastPoint = currentPoint;
                        currentPoint = getPoint(e);
                        p.addPoint(currentPoint);
                        
                        if(pointsOverlapping(startPoint, currentPoint))
                        {
                            lines.add(new Line(lastPoint, startPoint, LINE_COLOR));
                            currentPoint = null;
                            startPoint = null;
                            lastPoint = null;
                            polygons.add(p);
                            p.clearPoints();
                        }
                        else
                        {
                            lines.add(new Line(lastPoint, currentPoint, LINE_COLOR));
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
                canvas.clear();
                render();
                currentPointDrag = getPoint(e);
                raster.rasterize(new Line(startPointDrag, currentPointDrag, PREVIEW_COLOR));
                canvas.repaint();
                
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

    private Point getPoint(MouseEvent e) {
        return new Point(e.getX(), e.getY());
    }

    private void render() {
        canvas.clear();
        for(Line line : lines) {
            raster.rasterize(line);
        }
        canvas.repaint();
    }

    private boolean pointsOverlapping(Point p1, Point p2)
    {
        return Math.abs(p1.getX() - p2.getX()) < 10 && Math.abs(p1.getY() - p2.getY()) < 10;
    }

    private Point checkPointsOverlapping(Point p)
    {
        for(Line line : lines)
        {
            if(pointsOverlapping(p, line.getPoint1()))
            {
                return line.getPoint1();
            }

            if(pointsOverlapping(p, line.getPoint2()))
            {
                return line.getPoint2();
            }
        }
        return null;
    }
    

}