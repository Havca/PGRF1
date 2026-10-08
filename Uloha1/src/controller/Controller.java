package controller;

//Object import
import view.Canvas;
import graphics.rasterizer.LineRasterizer;
import graphics.rasterizer.BresenhamsAlgorithm;
//import graphics.rasterizer.TrivialLineRasterizer;
import model.Line;
import model.Point;
import model.Polygon;

//Library import
import java.awt.event.*;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.SwingUtilities;


/**
 * Handles user input and controls the application flow related to the {@link Canvas}.
 * The controller coordinates input events, canvas operations, and rendering updates.
 *
 * @author Ondrej Havelka
 * @version 2026
 */
public class Controller {
    //App parts variables
    private final Canvas canvas;
    private final LineRasterizer raster;

    //Line drawing variables
    private Point startPointDrag = null;
    private Point currentPointDrag = null;
    private boolean lineDrag = false;

    //Polygon drawing variables
    private Point startPoint = null;
    private Point currentPoint = null;
    private Point trackerPoint = null;
    private Polygon currentPolygon = new Polygon();

    //Colors
    private final int LINE_COLOR = Color.WHITE.getRGB();
    private final int PREVIEW_COLOR = Color.RED.getRGB();

    //Data structure for polygons
    private final List<Polygon> polygons = new ArrayList<Polygon>();

    //Edit mode variables
    private boolean editMode = false;
    private int indexOfEditedPoint = -1;
    private Polygon selectedPolygon = null;


    /* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- Constructors -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= */

    public Controller(Canvas canvas) {
        this.canvas = canvas;
        //this.raster = new TrivialLineRasterizer(canvas.getRaster());  ORIGINAL
        this.raster = new BresenhamsAlgorithm(canvas.getRaster());

    }

    /* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= Main functions -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- */
    
    public void init() {
        canvas.clear();

        canvas.addKeyListener(new KeyAdapter() {
            @Override 
            public void keyPressed(KeyEvent e)
            {
                //---------------------------Clear---------------------------
                if(e.getKeyCode() == KeyEvent.VK_C) {
                    polygons.clear();
                    canvas.clear();
                    canvas.repaint();
                    startPointDrag = null;
                    currentPointDrag = null;
                    startPoint = null;
                    currentPoint = null;
                    currentPolygon = new Polygon();
                }

                //---------------------------Edit Mode---------------------------
                if(e.getKeyCode() == KeyEvent.VK_E) {
                    editMode = !editMode;
                    trackerPoint = null;
                    if (editMode) { //AI
                        var editBorder = BorderFactory.createTitledBorder(
                            BorderFactory.createLineBorder(Color.ORANGE, 2),
                            "EDIT MODE");
                        editBorder.setTitleColor(Color.ORANGE);
                        canvas.setBorder(editBorder);
                    } else {
                        canvas.setBorder(null);
                    }
                    render();
                }

                ////---------------------------Remove last object---------------------------
                if(e.getKeyCode() == KeyEvent.VK_Z) {
                    if(polygons.isEmpty()) return;
                    polygons.remove(polygons.size() - 1);
                    render();
                }
            }
        });

        //-------------------------------------------------Mouse listeners----------------------------------------------
        canvas.addMouseListener(new MouseAdapter(){
            //Used for line drawing or vertex moving
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
                }
                else
                {
                    startPointDrag = new Point(e.getX(), e.getY());
                    currentPointDrag = new Point(e.getX(), e.getY());
                }
            }

            //Used for line drawing or vertex moving
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
                else if(lineDrag)
                {
                    Polygon p = new Polygon();
                    p.addPoint(startPointDrag);
                    p.addPoint(getPoint(e));
                    polygons.add(p);
                    currentPointDrag = null;
                    startPointDrag = null;
                    lineDrag = false;
                    render();
                }
                
            }

            //---------------------------Polygon point detection---------------------------
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if(editMode)
                {
                    //Point deletion
                    if(SwingUtilities.isRightMouseButton(e))
                    {
                        var overlappingPoint = checkPointsOverlapping(getPoint(e));
                        if(overlappingPoint == null) return;
                        selectedPolygon = overlappingPoint.first();
                        Point selectedPoint = overlappingPoint.second();
                        var selectedPolygonsPoints = selectedPolygon.getPoints();
                        indexOfEditedPoint = selectedPolygonsPoints.indexOf(selectedPoint);
                        if(selectedPolygon != null && indexOfEditedPoint != -1)
                        {
                            selectedPolygon.getPoints().remove(indexOfEditedPoint);
                            
                        }
                    }
                    render();
                }
                else
                {    
                    
                    if(startPoint == null) //Adding points to polygon
                    {
                        startPoint = getPoint(e);
                        currentPoint = getPoint(e);
                        currentPolygon.addPoint(startPoint);
                    }
                    else
                    {
                        currentPoint = getPoint(e);
                        if(pointsOverlapping(startPoint, currentPoint)) //Close polygon
                        {
                            currentPoint = null;
                            startPoint = null;
                            polygons.add(currentPolygon);
                            currentPolygon = new Polygon();
                        }
                        else //Continue adding points
                        {
                            currentPolygon.addPoint(currentPoint);
                        }
                        render();
                    }
                }
            }
        }); 

        //-------------------------------------------------Mouse motion listeners----------------------------------------------
        canvas.addMouseMotionListener(new MouseAdapter(){
            @Override 
            public void mouseDragged(MouseEvent e)
            {
                if(editMode) //Edit position of a point in a polygon
                {
                    currentPointDrag = getPoint(e);
                    if(selectedPolygon != null && indexOfEditedPoint != -1)
                    {
                        selectedPolygon.getPoints().set(indexOfEditedPoint, currentPointDrag);
                        render();
                    }
                }
                else //Draw line
                {
                    canvas.clear();
                    render();
                    lineDrag = true;
                    currentPointDrag = getPoint(e);
                    raster.rasterize(new Line(startPointDrag, currentPointDrag, PREVIEW_COLOR));
                    canvas.repaint();
                }
            }

            @Override 
            public void mouseMoved(MouseEvent e)
            {
                //Draw preview lines
                render();
                if(editMode) return;
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

        //Draw all closed polygons
        for(Polygon p : polygons)
        {
            ArrayList<Point> points = p.getPoints();
            int size = points.size();
            for(int i = 0; i < size; i++)
            {
                raster.rasterize(new Line(points.get(i), points.get((i+1) % size), LINE_COLOR));
            }
        }

        //Draw current open polygon
        if(currentPolygon.getPoints().size() > 1)
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
    //Margin 10 units for click
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