package controller;

//Object import
import graphics.clipping.PolygonClipper;
import graphics.clipping.SutherlandHodgmanClipper;
import graphics.filler.ScanlineFiller;
import graphics.rasterizer.LineRasterizer;
import graphics.rasterizer.XiaolinWuLineRasterizer;
import java.awt.Color;
import java.awt.event.*;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.SwingUtilities;
import model.Line;
import model.Point;
import model.Polygon;
import view.Canvas;


/**
 * Handles user input and controls the application flow related to the {@link Canvas}.
 * The controller coordinates input events, canvas operations, and rendering updates.
 *
 * @author Ondrej Havelka
 * @version 2026
 */
public class Controller {
    //Colors
    private final int LINE_COLOR = Color.WHITE.getRGB();
    private final int PREVIEW_COLOR = Color.RED.getRGB();
    private static final int CLIPPING_TRIANGLE_COLOR = new Color(0xE6B85C).getRGB();
    private static final int FILL_COLOR = new Color(0x58BFA9).getRGB();

    private static final List<Point> CLIPPING_POLYGON_VERTICES = List.of(
        new Point(250, 100),
        new Point(480, 470),
        new Point(230, 600),
        new Point(180, 600),
        new Point(40, 470)
    );

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
    private final Polygon clippingPolygon = createPolygon(CLIPPING_POLYGON_VERTICES, CLIPPING_TRIANGLE_COLOR);
    private boolean polygonClosed = false;

    private final PolygonClipper clipper = new SutherlandHodgmanClipper();

    //private Point seedFillPoint;
    //private boolean seedFillMode;
    //private final SeedFiller seedFiller;
    private final ScanlineFiller scanlineFiller;

    //Edit mode variables
    private boolean editMode = false;
    private int indexOfEditedPoint = -1;
    private Polygon selectedPolygon = null;

    //Align mode variables
    private boolean alignMode = false;


    /* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- Constructors -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= */

    public Controller(Canvas canvas) {
        this.canvas = canvas;
        //this.raster = new TrivialLineRasterizer(canvas.getRaster());  ORIGINAL
        this.raster = new XiaolinWuLineRasterizer(canvas.getRaster());
        //this.seedFiller = new RecursiveSeedFiller(canvas.getRaster());
        this.scanlineFiller = new ScanlineFiller(canvas.getRaster());

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
                    canvas.clear();
                    canvas.repaint();
                    startPointDrag = null;
                    currentPointDrag = null;
                    startPoint = null;
                    currentPoint = null;
                    currentPolygon = new Polygon();
                    render();
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


                //--------------------Straight, Vertical and Diagonal line-----------------
                if(e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    alignMode = !alignMode;
                    alignCurrentPoint();
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
                    Point selectedPoint = overlappingPoint;
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
                if(pointOutOfBounds(getPoint(e)))
                {
                    currentPointDrag = null;
                    return;
                }

                if(editMode)
                {
                    selectedPolygon = null;
                    indexOfEditedPoint = -1;
                }
                //else if(startPointDrag == null || currentPointDrag == null)
                //{
                //    return;
                //}
                else if(lineDrag)
                {
                    Polygon p = new Polygon();
                    p.addPoint(startPointDrag);
                    if(alignMode)
                    {
                        p.addPoint(currentPointDrag);
                    }
                    else
                    {
                        p.addPoint(getPoint(e));
                    }
                    
                    //polygons.add(p);
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
                        Point selectedPoint = overlappingPoint;
                        var selectedPolygonsPoints = selectedPolygon.getPoints();
                        indexOfEditedPoint = selectedPolygonsPoints.indexOf(selectedPoint);
                        if(indexOfEditedPoint != -1)
                        {
                            selectedPolygon.getPoints().remove(indexOfEditedPoint);
                            
                        }
                    }
                    render();
                }
                else
                {    
                    if(polygonClosed) return;
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
                            polygonClosed = true;
                            currentPolygon.connectLastPointToFirst();
                            //currentPolygon.addLine(new Line(currentPolygon.getPoints().get(currentPolygon.getPoints().size() - 1), currentPolygon.getPoints().get(0), LINE_COLOR));
                        }
                        else //Continue adding points
                        {
                            currentPolygon.addPoint(currentPoint);
                            //currentPolygon.addLine(new Line(currentPolygon.getPoints().get(currentPolygon.getPoints().size() - 2), currentPoint, LINE_COLOR));
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
                if(pointOutOfBounds(getPoint(e)))
                {
                    currentPointDrag = null;
                    return;
                }

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
                    if (alignMode) {
                        alignCurrentPoint();
                    }
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
                if(startPoint != null && trackerPoint != null && trackerPoint != null)
                {
                    raster.rasterize(new Line(currentPoint, trackerPoint, PREVIEW_COLOR));
                    raster.rasterize(new Line(trackerPoint, startPoint, PREVIEW_COLOR));
                }
                
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

        for(Line l : clippingPolygon.getLines())
        {
            raster.rasterize(l);
        }
        //Draw all closed polygons
        for(Line l : currentPolygon.getLines())
        {
            raster.rasterize(l);
        }

        Polygon subject = withPreviewLine();
        
        Polygon clipped;
        try {
            clipped = clipper.clip(subject, clippingPolygon);
        } catch (UnsupportedOperationException unfinishedClipper) {
            clipped = null;
        }

        if(clipped != null) {
            scanlineFiller.fill(clipped, FILL_COLOR);
        }



            /*Polygon p = currentPolygon;
            System.out.println("Points: " + p.getPoints());
            System.out.println("Lines: " + p.getLines());
            System.out.println("Color: " + Integer.toHexString(p.getColor()));*/
        canvas.repaint();
    }

    //----------------------------Point overlapping detection----------------------------
    //Margin 10 units for click
    private boolean pointsOverlapping(Point p1, Point p2)
    {
        return Math.abs(p1.getX() - p2.getX()) < 10 && Math.abs(p1.getY() - p2.getY()) < 10;
    }
    
    //----------------------------Point overlapping detection for polygons----------------------------
    private Point checkPointsOverlapping(Point p)
    {
        
            for(Point polP : currentPolygon.getPoints())
            {
                if(pointsOverlapping(polP, p))
                {
                    return polP;
                }
            }
        
        return null;
    }

    //----------------------------Aligns currentPoint to one of 3 axes-------------------------------
    private void alignCurrentPoint()
    {
        if (startPointDrag == null || currentPointDrag == null) {
            return;
        }

        int dx = currentPointDrag.getX() - startPointDrag.getX();
        int dy = currentPointDrag.getY() - startPointDrag.getY();

        if (dx == 0 && dy == 0) {
            return;
        }

        double length = Math.hypot(dx, dy); //sqrt(dx^2 + dy^2)
        double angle = Math.atan2(dy, dx); //used atan2 to avoid undefined tan in pi/2
        double snappedAngle = Math.round(angle / (Math.PI / 4.0)) * (Math.PI / 4.0); //pi/4 = 45 deg

        int alignedX = startPointDrag.getX() + (int) Math.round(length * Math.cos(snappedAngle));
        int alignedY = startPointDrag.getY() + (int) Math.round(length * Math.sin(snappedAngle));
        if(alignedX < 0 || alignedX >= canvas.getWidth() || alignedY < 0 || alignedY >= canvas.getHeight())
        {
            currentPointDrag = null;
        }
        else
        {
            currentPointDrag = new Point(alignedX, alignedY);
        }
    }

    private boolean pointOutOfBounds(Point p)
    {
        return p.getX() < 0 || p.getX() >= canvas.getWidth() || p.getY() < 0 || p.getY() >= canvas.getHeight();
    }

    private Polygon createPolygon(List<? extends Point> vertices, int color) {
        Polygon result = new Polygon();
        result.setColor(color);
        for(Point v : vertices)
        {
            result.addPoint(v);
        }
        result.connectLastPointToFirst();
        return result;
    }

    private Polygon withPreviewLine() {
        
        Polygon result = new Polygon();
        for (Point p : currentPolygon.getPoints()) {
            result.addPoint(p);
        }
        if(startPoint != null)
        {
            result.addPoint(trackerPoint);
        }
        
        result.connectLastPointToFirst();
        return result;
    }

    public record Pair<F, S>(F first, S second) {} //std::pair copy
    

}