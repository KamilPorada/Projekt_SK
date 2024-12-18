package org.example;

import java.awt.*;

abstract class Segment {
    public abstract void draw(Graphics g);
    public abstract double getLength();
    public abstract Point getPointAt(double distance);
}