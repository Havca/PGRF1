package model;


public class Line {
  private final Point point1;
  private final Point point2;
  private final int color;

  public Line(Point point1, Point point2, int color) {
    this.point1 = point1;
    this.point2 = point2;
    this.color = color;
  }

  public Point getPoint1() {
    return point1;
  }

  public Point getPoint2() {
    return point2;
  }

  public int getColor() {
    return color;
  }

  @Override 
  public String toString() {
    return "Line{" + "point1=" + point1 + ", point2=" + point2 + ", color=" + Integer.toHexString(color) + '}';
  }
}
