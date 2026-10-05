package graphics;

import java.awt.Graphics;

public interface Raster {
  void present(Graphics graphics);

  void clear(int color);

  void setPixel(int x, int y, int color);

  int getHeight();

  int getWidth();
  
}
