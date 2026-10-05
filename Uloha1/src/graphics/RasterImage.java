package graphics;

import java.awt.*;
import java.awt.image.BufferedImage;

public class RasterImage implements Raster {
  private final BufferedImage image;


  public RasterImage(int width, int height) {
    this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
  }

  @Override
  public int getHeight() {
    return image.getHeight();
  }

  @Override 
  public int getWidth() {
    return image.getWidth();
  } 

  @Override 
  public void present(Graphics graphics) {
    graphics.drawImage(image, 0, 0, null);
  }

  @Override 
  public void clear(int color) {
    Graphics graphics = image.getGraphics();

    graphics.setColor(new Color(color));
    graphics.fillRect(0, 0, image.getWidth(), image.getHeight());

    graphics.dispose();
  }

  @Override 
  public void setPixel(int x, int y, int color) {
    if(x < 0 || x >= image.getWidth() || y < 0 || y >= image.getHeight()) {
      throw new IllegalArgumentException("Pixel coordinates out of bounds: (" + x + ", " + y + ")");
    }
    image.setRGB(x, y, color);
  }
  
}
