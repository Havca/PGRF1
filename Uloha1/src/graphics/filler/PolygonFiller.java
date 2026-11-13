package graphics.filler;

import model.Pattern;
import model.Polygon;

public interface PolygonFiller {

    void fill(Polygon polygon, int fillColor);

    void fill(Polygon polygon, Pattern pattern);

}
