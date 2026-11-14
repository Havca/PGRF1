package graphics.filler;
import model.Pattern;

public interface SeedFiller {

    boolean fill(int x1, int y1, int targetColor, int fillColor, int borderColor);
    boolean fill(int x1, int y1, int targetColor, int borderColor, Pattern p);


}
