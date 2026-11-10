package controller;

//Object import
import graphics.clipping.PolygonClipper;
import graphics.clipping.SutherlandHodgmanClipper;
import graphics.filler.RecursiveSeedFiller;
import graphics.filler.ScanlineFiller;
import graphics.filler.SeedFiller;
import graphics.rasterizer.LineRasterizer;
import graphics.rasterizer.XiaolinWuLineRasterizer;
import java.awt.Color;
import java.awt.event.*;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import model.Line;
import model.Point;
import model.Polygon;
import view.Canvas;
import view.gui.Toolbar;



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
    private final Toolbar toolbar;

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
    private final ScanlineFiller scanlineFiller;

    //Edit mode variables
    private boolean editMode = false;
    //private Point selectedPoint = null;
    private int indexOfEditedPoint = -1;
    //private Polygon selectedPolygon = null;

    //Align mode variables
    private boolean alignMode = false;

    //SeedFill
    private final SeedFiller seedFiller;
    private Point seedFillPoint;
    private boolean seedFillMode;


    /* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- Constructors -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= */

    public Controller(Canvas canvas, Toolbar toolbar) {
        this.canvas = canvas;
        this.toolbar = toolbar;
        //this.raster = new TrivialLineRasterizer(canvas.getRaster());  ORIGINAL
        this.raster = new XiaolinWuLineRasterizer(canvas.getRaster());
        //this.seedFiller = new RecursiveSeedFiller(canvas.getRaster());
        this.scanlineFiller = new ScanlineFiller(canvas.getRaster());
        this.seedFiller = new RecursiveSeedFiller(canvas.getRaster());

    }

    /* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= Main functions -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- */
    
    public void init() {
        canvas.clear();
        toolbar.addClearListener(_ -> clearDrawing());
        toolbar.addSeedFillListener(_ -> toggleSeedFillMode());

        //----------------------------Clear-------------------------------
        canvas.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0), "clearDrawing");
        canvas.getActionMap().put("clearDrawing", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearDrawing();
            }
        });

        //----------------------------Align mode-------------------------------
        canvas.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
          .put(KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT, InputEvent.SHIFT_DOWN_MASK), "toggleAlignMode");
        canvas.getActionMap().put("toggleAlignMode", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                alignMode = !alignMode;
                System.out.println("toggle");
                alignCurrentPoint();
                render();
            }
        });

        //----------------------------Edit Mode-------------------------------
        canvas.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_E, 0), "toggleEditMode");
        canvas.getActionMap().put("toggleEditMode", new AbstractAction() {
           @Override
           public void actionPerformed(ActionEvent e) {
            System.out.println("editmode");
                editMode = !editMode;
                trackerPoint = null;
                if (editMode) { //AI
                    var editBorder = BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(Color.ORANGE, 2), "EDIT MODE");
                    editBorder.setTitleColor(Color.ORANGE);
                    canvas.setBorder(editBorder);
                } else {
                        canvas.setBorder(null);
                }
                render();
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
                    indexOfEditedPoint = currentPolygon.getPoints().indexOf(overlappingPoint);
                    System.out.println("SelectedPoint: " + overlappingPoint);
                }
                else
                {
                    if(seedFillMode)
                    {
                        fillAt(e);
                    }
                    else
                    {
                        startPointDrag = new Point(e.getX(), e.getY());
                        currentPointDrag = new Point(e.getX(), e.getY());
                    }
                    
                    
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
                    indexOfEditedPoint = -1;
                }
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
                        Point overlappingPoint = checkPointsOverlapping(getPoint(e));
                        currentPolygon.deletePoint(overlappingPoint);
                    }
                    render();
                }
                else if(!seedFillMode)
                {    
                    if(polygonClosed) return;
                    if(startPoint == null) //Adding points to polygon
                    {
                        startPoint = getPoint(e);
                        currentPoint = getPoint(e);
                        currentPolygon.addPoint(startPoint);
                        currentPolygon.setColor(LINE_COLOR);
                    }
                    else
                    {
                        currentPoint = alignMode ? alignPoint(currentPolygon.getLast(), getPoint(e)) : getPoint(e);
                        
                        if(pointsOverlapping(startPoint, currentPoint)) //Close polygon
                        {
                            currentPoint = null;
                            startPoint = null;
                            polygonClosed = true;
                            currentPolygon.connectLastPointToFirst();
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
                if(pointOutOfBounds(getPoint(e)))
                {
                    currentPointDrag = null;
                    return;
                }

                if(editMode) //Edit position of a point in a polygon
                {
                    currentPointDrag = getPoint(e);
                    if(currentPolygon != null && indexOfEditedPoint != -1)
                    {
                        currentPolygon.changePoint(indexOfEditedPoint, currentPointDrag);
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
                if(alignMode)
                {
                    trackerPoint = alignPoint(currentPoint, getPoint(e));
                }
                else
                {
                    trackerPoint = getPoint(e);
                }
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
        
        if(seedFillPoint != null)
        {
            seedFiller.fill(seedFillPoint.getX(), seedFillPoint.getY(), canvas.getBackgroundColor(), FILL_COLOR);
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
            System.out.println("null");
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

    private Point alignPoint(Point prevPoint, Point currentPoint)
{
    if (prevPoint == null || currentPoint == null) {
        return null;
    }

    int dx = currentPoint.getX() - prevPoint.getX();
    int dy = currentPoint.getY() - prevPoint.getY();

    if (dx == 0 && dy == 0) {
        return currentPoint;
    }

    double length = Math.hypot(dx, dy);
    double angle = Math.atan2(dy, dx);
    double snappedAngle = Math.round(angle / (Math.PI / 4.0)) * (Math.PI / 4.0);

    int alignedX = prevPoint.getX() + (int) Math.round(length * Math.cos(snappedAngle));
    int alignedY = prevPoint.getY() + (int) Math.round(length * Math.sin(snappedAngle));

    if (alignedX < 0 || alignedX >= canvas.getWidth()
            || alignedY < 0 || alignedY >= canvas.getHeight()) {
        return null;
    }

    return new Point(alignedX, alignedY);
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

    private void clearDrawing() {
        currentPolygon.clearPoints();
        canvas.clear();
        canvas.repaint();
        startPointDrag = null;
        startPoint = null;
        currentPointDrag = null;
        currentPolygon = new Polygon();
        currentPoint = null;
        seedFillPoint = null;
        trackerPoint = null;
        seedFillMode = false;
        toolbar.setSeedFillMode(false);
        polygonClosed = false;
        render();
    }

    private void toggleSeedFillMode() {
        seedFillMode = !seedFillMode;
        toolbar.setSeedFillMode(seedFillMode);
        render();
    }

    private void fillAt(MouseEvent event) {
        Point seed = getPoint(event);
        Integer seedColor = canvas.getRaster().getPixel(seed.getX(), seed.getY());
        if (seedFillPoint != null && seedColor != null && seedColor == FILL_COLOR) {
            seedFillPoint = null;
            render();
            return;
        }

        boolean filled = seedFiller.fill(seed.getX(), seed.getY(), canvas.getBackgroundColor(), FILL_COLOR);
        if (filled) {
            seedFillPoint = seed;
            render();
        }
    }

    public record Pair<F, S>(F first, S second) {} //std::pair copy
    

}