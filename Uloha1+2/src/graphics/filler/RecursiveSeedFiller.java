package graphics.filler;

import graphics.Raster;
import java.util.LinkedList;
import java.util.Queue;
import model.Pattern;
import model.Point;

public class RecursiveSeedFiller implements SeedFiller {

    private final Raster raster;
    private final Queue<Point> q = new LinkedList<>();

    public RecursiveSeedFiller(Raster raster) {
        this.raster = raster;
    }

    @Override
    public boolean fill(int x1, int y1, int targetColor, int fillColor, int borderColor) {
        if (targetColor == fillColor || 
            raster.getPixel(x1, y1) == null || 
            raster.getPixel(x1, y1) != targetColor) 
        {
            return false;
        }

        q.clear();
        q.add(new Point(x1, y1));

        while(!q.isEmpty())
        {
            Point p = q.remove();
            int x = p.getX();
            int y = p.getY();
            
            if(checkPoint(raster.getPixel(x - 1, y), targetColor, borderColor, fillColor))
            {
                raster.setPixel(x - 1, y, fillColor);
                q.add(new Point(x - 1, y));
            }
            if(checkPoint(raster.getPixel(x + 1, y), targetColor, borderColor, fillColor))
            {
                raster.setPixel(x + 1, y, fillColor);
                q.add(new Point(x + 1, y));
            }
            if(checkPoint(raster.getPixel(x, y - 1), targetColor, borderColor, fillColor))
            {
                raster.setPixel(x, y - 1, fillColor);
                q.add(new Point(x, y - 1));
            }

            if(checkPoint(raster.getPixel(x, y + 1), targetColor, borderColor, fillColor))
            {
                raster.setPixel(x, y + 1, fillColor);
                q.add(new Point(x, y + 1));
            }
        }

        return true;
    }


    @Override 
    public boolean fill(int x1, int y1, int targetColor, int borderColor, Pattern pattern) {
        if (raster.getPixel(x1, y1) == null || 
            raster.getPixel(x1, y1) != targetColor) {
            return false;
        }

        q.clear();
        q.add(new Point(x1, y1));

        while(!q.isEmpty())
        {
            Point p = q.remove();
            int x = p.getX();
            int y = p.getY();

            if(checkPoint(raster.getPixel(x - 1, y), targetColor, borderColor, -1))
            {
                raster.setPixel(x - 1, y, pattern.getColor(x - 1, y));
                q.add(new Point(x - 1, y));
            }
            if(checkPoint(raster.getPixel(x + 1, y), targetColor, borderColor, -1))
            {
                raster.setPixel(x + 1, y, pattern.getColor(x + 1, y));
                q.add(new Point(x + 1, y));
            }
            if(checkPoint(raster.getPixel(x, y - 1), targetColor, borderColor, -1))
            {
                raster.setPixel(x, y - 1, pattern.getColor(x, y - 1));
                q.add(new Point(x, y - 1));
            }
            if(checkPoint(raster.getPixel(x, y + 1), targetColor, borderColor, -1))
            {
                raster.setPixel(x, y + 1, pattern.getColor(x, y + 1));
                q.add(new Point(x, y + 1));
            }
        }
        
        return true;
    }

    private boolean checkPoint(Integer color, int targetColor, int borderColor, int fillColor)
    {
        return color != null && color == targetColor && color != borderColor && color != fillColor;
    }
}


