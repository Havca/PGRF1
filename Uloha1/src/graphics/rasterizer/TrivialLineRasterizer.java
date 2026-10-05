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

    
    //TODO: x2 == x1 (verical line) 
    //TODO: x1 vlevo a x2 vpravo (x1 < x2), naopak nefunguje
    //TODO:     pokud je y2 - y1 > x2 - x1, tak to nefunguje (strma cara)
    float k = (y2 - y1) / (float)(x2 - x1);

    float q = y1 - k * x1;

    for(int x = x1; x <= x2; x++)
    {
      float y = k * x + q;
      raster.setPixel(x, Math.round(y), color);
    }
  }
}
