package graphics.rasterizer;

import model.Line;
import graphics.Raster;

public class TrivialLineRasterizer implements LineRasterizer {
  private final Raster raster;

  public TrivialLineRasterizer(Raster raster) {
    this.raster = raster;
  }



  @Override 
  public void rasterize(Line line) {
    rasterize(
      line.getPoint1().getX(), 
      line.getPoint1().getY(), 
      line.getPoint2().getX(), 
      line.getPoint2().getY(), 
      line.getColor()
    );

  }

  private void rasterize(int x1, int y1, int x2, int y2, int color) {

    if(x1 < 0 || x1 >= raster.getWidth() || y1 < 0 || y1 >= raster.getHeight()) {
      throw new IllegalArgumentException("Pixel coordinates out of bounds: (" + x1 + ", " + y1 + ")");
    }

    if(x2 < 0 || x2 >= raster.getWidth() || y2 < 0 || y2 >= raster.getHeight()) {
      throw new IllegalArgumentException("Pixel coordinates out of bounds: (" + x2 + ", " + y2 + ")");
    }

    if(x1 == x2 && y1 == y2) {
      raster.setPixel(x1, y1, color);
      return;
    }

    int deltaX = x2 - x1;
    int deltaY = y2 - y1;

    if(deltaX == 0) //vertikalni cara
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

      for(int y = y1; y <= y2; y++)
      {
        raster.setPixel(x1, y, color);
      }
    }

    int dominantniOsa = Math.abs(deltaX) >= Math.abs(deltaY) ? 0 : 1; //pro osu x je hodnota 0, pro y 1

    if(dominantniOsa == 0 && x1 > x2)
    {
      int tempX = x1;
      int tempY = y1;
      x1 = x2;
      y1 = y2;
      x2 = tempX;
      y2 = tempY;
    }

    if(dominantniOsa == 1 && y1 > y2)
    {
      int tempX = x1;
      int tempY = y1;
      x1 = x2;
      y1 = y2;
      x2 = tempX;
      y2 = tempY;
    }

    deltaX = x2 - x1;
    deltaY = y2 - y1;

    float k = deltaY / (float)deltaX;
    float q = y1 - k * x1;

    if(dominantniOsa == 0)
    {
      for(int x = x1; x <= x2; x++)
      {
        int y = Math.round(k * x  + q);
        raster.setPixel(x, y, color);
      }
    }
    else
    {
      for(int y = y1; y <= y2; y++)
      {
        int x = Math.round((y - q) / k);
        raster.setPixel(x, y, color);
      }
    }




      

    /*if(x1 > x2)
    {
      int tempX = x1;
      int tempY = y1;
      x1 = x2;
      y1 = y2;
      x2 = tempX;
      y2 = tempY;
    }

    
    
    

    // (x2 - x1) / (y2 - y1)  > 

    if(x1 == x2)
    {
      for(int y = Math.min(y1, y2); y < Math.max(y1, y2); y++)
      {
        raster.setPixel(x1, y, color);
      }
    }
    else if(Math.tan((x2-x1) / (float)(y2-y1)) > 1) //podle y
    {
      float k = (y2 - y1) / (float)(x2 - x1);

      float q = y1 - k * x1;

      for(int y = y1; y <= y2; y++)
      {
        float x = (y - q) / k;
        raster.setPixel(Math.round(x), y, color);
      }
    }
    else //podle x
    {
      float k = (y2 - y1) / (float)(x2 - x1);

      float q = y1 - k * x1;

      for(int x = x1; x <= x2; x++)
      {
        float y = k * x + q;
        raster.setPixel(x, Math.round(y), color);
      }
    }*/


    
    //TODO: x2 == x1 (verical line) 
    //TODO: x1 vlevo a x2 vpravo (x1 < x2), naopak nefunguje
    //TODO:     pokud je y2 - y1 > x2 - x1, tak to nefunguje (strma cara)
    
  }
}
