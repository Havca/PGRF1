package model;

import java.util.ArrayList;
import java.util.List;

public class Pattern {
  private List<List<Integer>> pattern = new ArrayList<>();
  private final int width;
  private final int height;

  public Pattern(List<List<Integer>> pattern)
  {
    this.pattern = pattern;
    width = pattern.get(0).size();
    height = pattern.size();
  }

  public int getColor(int x, int y)
  {
    return pattern.get(y % height).get(x % width);
  }
}
