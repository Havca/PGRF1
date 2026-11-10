package model;

import java.util.ArrayList;



public class Polygon {
  private final ArrayList<Point> points = new ArrayList<>();
  private final ArrayList<Line> lines = new ArrayList<>();
  private  int color = 0xFFFFFFFF; // Default color is white


  public ArrayList<Point> getPoints() {
    return points;
  }

  public ArrayList<Line> getLines() {
    return lines;
  }

  public void addPoint(Point point) {
    points.add(point);
    if(points.size() > 1)
    {
      lines.add(new Line(points.get(points.size() - 2), point, color));
    }
  }

  public boolean deletePoint(Point point)
  {
    int index = points.indexOf(point);
    if(index == -1) return false;
    points.remove(index);
    recreateLinesFromPoints();
    return true;
  }

  public boolean changePoint(int index, Point newPoint)
  {
    if(index == -1) return false;
    points.set(index, newPoint);
    recreateLinesFromPoints();
    return true;
  }

  public void clearPoints() {
    points.clear();
    lines.clear();
  }

  public void addLine(Line line) {
    lines.add(line);
  }

  public void connectLastPointToFirst() {
    if(points.size() > 1)
    {
      lines.add(new Line(points.get(points.size() - 1), points.get(0), color));
    }
  }

  public void setColor(int color) {
    this.color = color;
  }

  public int getColor() {
    return color;
  }

  public Point getLast()
  {
    return points.getLast();
  }

  private void recreateLinesFromPoints()
  {
    lines.clear();
    for(int i = 0; i < points.size(); i++)
    {
      lines.add(new Line(points.get(i), points.get((i+1) % points.size()), color));
    }
  }

  @Override 
  public String toString() {
    return "Polygon{" + "points=" + points + '}';
  }

}
