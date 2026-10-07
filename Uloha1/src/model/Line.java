package model;


public class Line {
  private Point point1;
  private Point point2;
  private int color;

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
}
