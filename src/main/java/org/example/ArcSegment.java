package org.example;

import java.awt.*;

class ArcSegment extends Segment {
    private Point startPoint, endPoint; // Punkty początkowy i końcowy łuku
    private Point center;              // Środek elipsy
    private int a, b;                  // Półosie elipsy (a - pozioma, b - pionowa)
    private double startAngle, endAngle;

    public ArcSegment(Point startPoint, Point endPoint, double startAngle, double endAngle) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.startAngle = startAngle;
        this.endAngle = endAngle;

        calculateCenterAndAxes();
    }

    // Obliczanie środka i półosi elipsy
    private void calculateCenterAndAxes() {
        int centerX = (startPoint.x + endPoint.x) / 2;
        int centerY = (startPoint.y + endPoint.y) / 2;
        center = new Point(centerX, centerY);

        // Półosie elipsy
        a = Math.abs(startPoint.x - endPoint.x) / 2; // Pozioma półoś
        b = Math.abs(startPoint.y - endPoint.y) / 2; // Pionowa półoś
    }

    @Override
    public void draw(Graphics g) {
//        g.setColor(Color.BLUE);

        // Normalizacja kątów
        int normalizedStartAngle = (int) startAngle;
        int normalizedArcAngle = (int) (endAngle - startAngle);

        // Poprawienie przypadku ujemnych kątów
        if (normalizedArcAngle < 0) {
            normalizedStartAngle += normalizedArcAngle; // Przesunięcie startu
            normalizedArcAngle = -normalizedArcAngle;  // Odwrócenie łuku
        }

        // Rysowanie łuku
        g.drawArc(center.x - a, center.y - b, 2 * a, 2 * b,
                normalizedStartAngle, normalizedArcAngle);
    }

    @Override
    public double getLength() {
        return Math.toRadians(Math.abs(endAngle - startAngle)) * Math.sqrt((a * a + b * b) / 2.0);
    }

    @Override
    public Point getPointAt(double distance) {
        double totalAngle = Math.toRadians(Math.abs(endAngle - startAngle));
        double ratio = distance / (totalAngle * Math.sqrt((a * a + b * b) / 2.0));
        double angle;

        // Kąt na podstawie normalizacji
        if (startAngle <= endAngle) {
            angle = Math.toRadians(startAngle) + ratio * totalAngle;
        } else {
            angle = Math.toRadians(startAngle) - ratio * totalAngle;
        }

        int x = (int) (center.x + a * Math.cos(angle));
        int y = (int) (center.y - b * Math.sin(angle));
        return new Point(x, y);
    }
}
