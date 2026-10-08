package graphics.rasterizer;

import graphics.Raster;
import model.Line;

public class BresenhamsAlgorithm implements LineRasterizer {

  private final Raster raster;

  public BresenhamsAlgorithm(Raster raster)
  {
    this.raster = raster;
  }
  public void rasterize(Line line) {
    
    if(line != null && line.getPoint1() != null && line.getPoint2() != null)
    {
      rasterize(
      line.getPoint1().getX(), 
      line.getPoint1().getY(), 
      line.getPoint2().getX(), 
      line.getPoint2().getY(), 
      line.getColor()
    );
    } 
  }

  private void rasterize(int x1, int y1, int x2, int y2, int color)
  {
    if(x1 == x2 && y1 == y2) {
      raster.setPixel(x1, y1, color);
      return;
    }

    int deltaX = x2 - x1;
    int deltaY = y2 - y1;
    if(Math.abs(deltaX) > Math.abs(deltaY))
    {
      plotX(x1, y1, x2, y2, color);
    }
    else
    {
      plotY(x1, y1, x2, y2, color);
    }
  }

  private void plotX(int x1, int y1, int x2, int y2, int color)
  {
    if(x1 > x2)
    {
      int tempX = x1;
      int tempY = y1;
      x1 = x2;
      y1 = y2;
      x2 = tempX;
      y2 = tempY;
    }

    int dx = x2 - x1;
    int dy = Math.abs((y2 - y1));
    int increment = (y2 >= y1) ? 1 : -1;

    int y = y1;
    int d = 2 * dy - dx;
    for(int x = x1; x <= x2; x++)
    {
      raster.setPixel(x,y, color);

      if(d > 0)
      {
        y += increment;
        d += 2 * (dy - dx);
      }
      else
      {
        d += 2* dy;
      }
    }
  }

  private void plotY(int x1, int y1, int x2, int y2, int color)
  {
    if(y1 > y2)
    {
      int tempX = x1;
      int tempY = y1;
      x1 = x2;
      y1 = y2;
      x2 = tempX;
      y2 = tempY;
    }

    int dy = y2 - y1;
    int dx = Math.abs(x2 - x1);
    int increment = (x2 >= x1) ? 1 : -1;

    int x = x1;
    int d = 2 * dx - dy;

    for(int y = y1; y <= y2; y++)
    {
      raster.setPixel(x, y, color);

      if(d > 0)
      {
        x += increment;
        d += 2 * (dx - dy);
      }
      else
      {
        d += 2 * dx;
      }
    }
  }
}
