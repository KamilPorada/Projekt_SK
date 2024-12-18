package org.example;

import java.awt.*;

class ArcSegment extends Segment {
    private Point center;
    private int radius;
    private double startAngle, endAngle;

    public ArcSegment(Point center, int radius, double startAngle, double endAngle) {
        this.center = center;
        this.radius = radius;
        this.startAngle = startAngle;
        this.endAngle = endAngle;
    }

    @Override
    public void draw(Graphics g) {
        g.drawArc(center.x - radius, center.y - radius, 2 * radius, 2 * radius,
                (int) startAngle, (int) (endAngle - startAngle));
    }

    @Override
    public double getLength() {
        return Math.toRadians(endAngle - startAngle) * radius;
    }

    @Override
    public Point getPointAt(double distance) {
        double angle = Math.toRadians(startAngle) + distance / radius;
        int x = (int) (center.x + radius * Math.cos(angle));
        int y = (int) (center.y - radius * Math.sin(angle));      
        return new Point(x, y);
    }
}
