package org.example;

import java.awt.*;

class LineSegment extends Segment {
    private Point start, end;

    public LineSegment(Point start, Point end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public void draw(Graphics g) {
//        g.setColor(Color.BLUE);
        g.drawLine(start.x, start.y, end.x, end.y);
    }

    @Override
    public double getLength() {
        return start.distance(end);
    }

    @Override
    public Point getStartPoint() {
        return start;
    }

    @Override
    public Point getEndPoint() {
        return end;
    }
    @Override
    public Point getPointAt(double distance) {
        double dx = end.x - start.x;
        double dy = end.y - start.y;
        double length = getLength();
        double ratio = distance / length;
        int x = (int) (start.x + ratio * dx);
        int y = (int) (start.y + ratio * dy);
        return new Point(x, y);
    }
}
