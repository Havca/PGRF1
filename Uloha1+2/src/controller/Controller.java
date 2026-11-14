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
import model.Pattern;
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
    private static final int LIGHT_BLUE = new Color(0x00A8E8).getRGB();
    private static final int DARK_BLUE  = new Color(0x1C3A5A).getRGB();
    private static final int WHITE = Color.WHITE.getRGB();
    private static final int RED = Color.RED.getRGB();
    private static final int YELLOW = Color.YELLOW.getRGB();
    private static final int BLACK = Color.BLACK.getRGB();

    private static final List<Point> CLIPPING_POLYGON_VERTICES = List.of(
        new Point(250, 100),
        new Point(480, 470),
        new Point(230, 600),
        new Point(180, 600),
        new Point(40, 470)
    );

    

    private static final List<List<Integer>> PATTERN_CREEPER_LARGE = List.of(
        // Row 1 (Scaled to 2 rows)
        List.of(LIGHT_BLUE, LIGHT_BLUE, LIGHT_BLUE, LIGHT_BLUE, LIGHT_BLUE, LIGHT_BLUE, LIGHT_BLUE, LIGHT_BLUE, LIGHT_BLUE, LIGHT_BLUE),
        List.of(LIGHT_BLUE, LIGHT_BLUE, LIGHT_BLUE, LIGHT_BLUE, LIGHT_BLUE, LIGHT_BLUE, LIGHT_BLUE, LIGHT_BLUE, LIGHT_BLUE, LIGHT_BLUE),
        
        // Row 2 (Scaled to 2 rows)
        List.of(LIGHT_BLUE, LIGHT_BLUE, LIGHT_BLUE, LIGHT_BLUE, DARK_BLUE,  DARK_BLUE,  LIGHT_BLUE, LIGHT_BLUE, LIGHT_BLUE, LIGHT_BLUE),
        List.of(LIGHT_BLUE, LIGHT_BLUE, LIGHT_BLUE, LIGHT_BLUE, DARK_BLUE,  DARK_BLUE,  LIGHT_BLUE, LIGHT_BLUE, LIGHT_BLUE, LIGHT_BLUE),
        
        // Row 3 (Scaled to 2 rows)
        List.of(LIGHT_BLUE, LIGHT_BLUE, DARK_BLUE,  DARK_BLUE,  DARK_BLUE,  DARK_BLUE,  DARK_BLUE,  DARK_BLUE,  LIGHT_BLUE, LIGHT_BLUE),
        List.of(LIGHT_BLUE, LIGHT_BLUE, DARK_BLUE,  DARK_BLUE,  DARK_BLUE,  DARK_BLUE,  DARK_BLUE,  DARK_BLUE,  LIGHT_BLUE, LIGHT_BLUE),
        
        // Row 4 (Scaled to 2 rows)
        List.of(LIGHT_BLUE, LIGHT_BLUE, DARK_BLUE,  DARK_BLUE,  LIGHT_BLUE, LIGHT_BLUE, DARK_BLUE,  DARK_BLUE,  LIGHT_BLUE, LIGHT_BLUE),
        List.of(LIGHT_BLUE, LIGHT_BLUE, DARK_BLUE,  DARK_BLUE,  LIGHT_BLUE, LIGHT_BLUE, DARK_BLUE,  DARK_BLUE,  LIGHT_BLUE, LIGHT_BLUE),
        
        // Row 5 (Scaled to 2 rows)
        List.of(LIGHT_BLUE, LIGHT_BLUE, LIGHT_BLUE, LIGHT_BLUE, LIGHT_BLUE, LIGHT_BLUE, LIGHT_BLUE, LIGHT_BLUE, LIGHT_BLUE, LIGHT_BLUE),
        List.of(LIGHT_BLUE, LIGHT_BLUE, LIGHT_BLUE, LIGHT_BLUE, LIGHT_BLUE, LIGHT_BLUE, LIGHT_BLUE, LIGHT_BLUE, LIGHT_BLUE, LIGHT_BLUE)
    );  

    private static final Pattern pRocketShip = new Pattern(PATTERN_CREEPER_LARGE);

    private static final List<List<Integer>> PATTERN_HEART_LARGE = List.of(
    List.of(WHITE, WHITE, RED,   RED,   WHITE, WHITE, RED,   RED,   WHITE, WHITE),
    List.of(WHITE, WHITE, RED,   RED,   WHITE, WHITE, RED,   RED,   WHITE, WHITE),
    
    List.of(RED,   RED,   RED,   RED,   RED,   RED,   RED,   RED,   RED,   RED),
    List.of(RED,   RED,   RED,   RED,   RED,   RED,   RED,   RED,   RED,   RED),
    
    List.of(RED,   RED,   RED,   RED,   RED,   RED,   RED,   RED,   RED,   RED),
    List.of(RED,   RED,   RED,   RED,   RED,   RED,   RED,   RED,   RED,   RED),
    
    List.of(WHITE, WHITE, RED,   RED,   RED,   RED,   RED,   RED,   WHITE, WHITE),
    List.of(WHITE, WHITE, RED,   RED,   RED,   RED,   RED,   RED,   WHITE, WHITE),
    
    List.of(WHITE, WHITE, WHITE, WHITE, RED,   RED,   WHITE, WHITE, WHITE, WHITE),
    List.of(WHITE, WHITE, WHITE, WHITE, RED,   RED,   WHITE, WHITE, WHITE, WHITE)
);
private static final Pattern pHeartLarge = new Pattern(PATTERN_HEART_LARGE);

private static final List<List<Integer>> PATTERN_SMILEY_LARGE = List.of(
    List.of(YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW),
    List.of(YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW),

    List.of(YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW),
    List.of(YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW),
    
    List.of(YELLOW, YELLOW, YELLOW, YELLOW, BLACK,  BLACK,  YELLOW, YELLOW, BLACK,  BLACK,  YELLOW, YELLOW, YELLOW, YELLOW),
    List.of(YELLOW, YELLOW, YELLOW, YELLOW, BLACK,  BLACK,  YELLOW, YELLOW, BLACK,  BLACK,  YELLOW, YELLOW, YELLOW, YELLOW),
    
    List.of(YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW),
    List.of(YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW),
    
    List.of(YELLOW, YELLOW, BLACK,  BLACK,  YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, BLACK,  BLACK, YELLOW, YELLOW),
    List.of(YELLOW, YELLOW, BLACK,  BLACK,  YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, BLACK,  BLACK, YELLOW, YELLOW),
    
    List.of(YELLOW, YELLOW, YELLOW, YELLOW, BLACK,  BLACK,  BLACK,  BLACK,  BLACK,  BLACK,  YELLOW, YELLOW, YELLOW, YELLOW),
    List.of(YELLOW, YELLOW, YELLOW, YELLOW, BLACK,  BLACK,  BLACK,  BLACK,  BLACK,  BLACK,  YELLOW, YELLOW, YELLOW, YELLOW),

    List.of(YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW),
    List.of(YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW)
);
private static final Pattern pSmileyLarge = new Pattern(PATTERN_SMILEY_LARGE);
    

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
    private Polygon selectedPolygon = null;
    private int indexOfEditedPoint = -1;

    //Align mode variables
    private boolean alignMode = false;

    //SeedFill
    private final SeedFiller seedFiller;
    private Point seedFillPoint;
    private boolean seedFillMode;
    private boolean patternMode = false;
    private Pattern selectedPattern = pSmileyLarge;
    private int fillColor = FILL_COLOR;


    /* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- Constructors -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= */

    public Controller(Canvas canvas, Toolbar toolbar) {
        this.canvas = canvas;
        this.toolbar = toolbar;
        this.raster = new XiaolinWuLineRasterizer(canvas.getRaster());
        this.scanlineFiller = new ScanlineFiller(canvas.getRaster());
        this.seedFiller = new RecursiveSeedFiller(canvas.getRaster());
    }

    /* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= Main functions -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- */
    
    public void init() {
        canvas.clear();
        toolbar.addClearListener(_ -> clearDrawing());
        toolbar.addSeedFillListener(_ -> toggleSeedFillMode());
        toolbar.addPatternModeListener(_ -> togglePatternMode());
        toolbar.addFillColorListener(_ -> {
            fillColor = switch (toolbar.getSelectedFillColor()) {
                case "Red" -> RED;
                case "Yellow" -> YELLOW;
                case "Black" -> BLACK;
                case "White" -> WHITE;
                default -> FILL_COLOR;
            };
            render();
        });
        toolbar.addPatternSelectionListener(_ -> {
            selectedPattern = switch (toolbar.getSelectedPattern()) {
                case "Creeper" -> pRocketShip;
                case "Heart" -> pHeartLarge;
                default -> pSmileyLarge;
            };
            render();
        });

        

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
                if(editMode) //Select vertex to be moved
                {
                    var overlappingPoint = checkPointsOverlapping(getPoint(e));
                    if(overlappingPoint == null) return;
                    if(currentPolygon.getPoints().contains(overlappingPoint))
                    {
                        indexOfEditedPoint = currentPolygon.getPoints().indexOf(overlappingPoint);
                        selectedPolygon = currentPolygon;
                    }
                    else
                    {
                        indexOfEditedPoint= clippingPolygon.getPoints().indexOf(overlappingPoint);
                        selectedPolygon = clippingPolygon;
                    }

                }
                else //start drawing line OR seed fill
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
                    selectedPolygon = null;
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
                        if(selectedPolygon == currentPolygon)
                        {
                            currentPolygon.changePoint(indexOfEditedPoint, currentPointDrag);
                        }
                        else
                        {
                            clippingPolygon.changePoint(indexOfEditedPoint, currentPointDrag);
                        }
                        
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
            if(patternMode)
            {
                scanlineFiller.fill(clipped, selectedPattern);
            }
            else
            {
                scanlineFiller.fill(clipped, fillColor);
            }

            
        }

        if(seedFillPoint != null)
        {
            if(patternMode)
            {   
                seedFiller.fill(seedFillPoint.getX(), seedFillPoint.getY(), canvas.getBackgroundColor(), LINE_COLOR, selectedPattern);
            }
            else
            {
                seedFiller.fill(seedFillPoint.getX(), seedFillPoint.getY(), canvas.getBackgroundColor(), fillColor, LINE_COLOR);
            }
        }

        for(Line l : clippingPolygon.getLines())
        {
            raster.rasterize(l);
        }

        for(Line l : currentPolygon.getLines())
        {
            raster.rasterize(l);
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

            for(Point polP : clippingPolygon.getPoints())
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

    private void togglePatternMode() {
        patternMode = !patternMode;
        toolbar.setPatternMode(patternMode);
        render();
    }

    private void fillAt(MouseEvent event) {
        Point seed = getPoint(event);
        Integer seedColor = canvas.getRaster().getPixel(seed.getX(), seed.getY());
        if (seedFillPoint != null && seedColor != null && seedColor == fillColor) {
            seedFillPoint = null;
            render();
            return;
        }

        boolean filled;
        if(patternMode)
        {
            filled = seedFiller.fill(seed.getX(), seed.getY(), canvas.getBackgroundColor(), LINE_COLOR, selectedPattern);
        }
        else
        {   
            filled = seedFiller.fill(seed.getX(), seed.getY(), canvas.getBackgroundColor(), fillColor, LINE_COLOR);
        }
        
        if (filled) {
            seedFillPoint = seed;
            render();
        }
    }

    public record Pair<F, S>(F first, S second) {} //std::pair copy
    

}