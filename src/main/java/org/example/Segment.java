package org.example;

import java.awt.*;

abstract class Segment {
    public abstract void draw(Graphics g);
    public abstract Point getStartPoint();
    public abstract Point getEndPoint();
    public abstract double getLength();
    public abstract Point getPointAt(double distance);
}
