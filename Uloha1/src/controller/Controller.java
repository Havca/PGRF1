package controller;

import view.Canvas;

import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

import graphics.rasterizer.LineRasterizer;
import graphics.rasterizer.TrivialLineRasterizer;
import model.Line;
import model.Point;

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
    private Point startPoint;
    private Point currentPoint;
    private final int LINE_COLOR = Color.WHITE.getRGB();
    private final int PREVIEW_COLOR = Color.RED.getRGB();
    private final List<Line> lines = new ArrayList<Line>();


    /* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- Constructors -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= */

    public Controller(Canvas canvas) {
        this.canvas = canvas;
        this.raster = new TrivialLineRasterizer(canvas.getRaster());
    }

    /* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= Main functions -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- */
    
    public void init() {
        canvas.clear();

        

        

        canvas.addMouseListener(new MouseAdapter(){
            @Override 
            public void mousePressed(MouseEvent e)
            {
                startPoint = new Point(e.getX(), e.getY());
                currentPoint = new Point(e.getX(), e.getY());
            }

            @Override 
            public void mouseReleased(MouseEvent e)
            {
                lines.add(new Line(startPoint, getPoint(e), LINE_COLOR));
                currentPoint = null;
                startPoint = null;
                render();
            }

            
        }); 

        canvas.addMouseMotionListener(new MouseAdapter(){
            @Override 
            public void mouseDragged(MouseEvent e)
            {
                canvas.clear();
                render();
                currentPoint = getPoint(e);
                raster.rasterize(new Line(startPoint, currentPoint, PREVIEW_COLOR));
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

}