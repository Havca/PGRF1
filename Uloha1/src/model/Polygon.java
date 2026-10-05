package model;

import java.util.ArrayList;

public class Polygon {
  private final ArrayList<Point> points = new ArrayList<Point>();

  public ArrayList<Point> getPoints() {
    return points;
  }

  public void addPoint(Point point) {
    points.add(point);
  }

  public void clearPoints() {
    points.clear();
  }

}
