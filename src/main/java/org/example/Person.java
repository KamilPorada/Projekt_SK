package org.example;

import java.awt.*;
import java.util.Random;

class Person {
    private int x, y;
    private Color color;
    private final int RADIUS = 7;
    private int speed;
    private int direction;
    private int id;

    public Person(int x, int y, int gender, int direction, int id) {
        this.x = x;
        this.y = y;

        if (gender == 0) {
            this.color = Color.PINK;
        } else if (gender == 1) {
            this.color = Color.BLUE;
        } else {
            throw new IllegalArgumentException("Płeć musi być 0 (kobieta) lub 1 (mężczyzna)");
        }

        Random random = new Random();
        this.speed = random.nextInt(3) + 2;
        this.direction = direction;
        this.id = id;
    }

    public void draw(Graphics g) {
        g.setColor(color);
        g.fillOval(x - RADIUS, y - RADIUS, 2 * RADIUS, 2 * RADIUS);

        if (color == Color.PINK) {
            g.setColor(Color.BLACK);
        } else if (color == Color.BLUE) {
            g.setColor(Color.WHITE);
        }

        g.setFont(new Font("Arial", Font.BOLD, 10));
        FontMetrics metrics = g.getFontMetrics();
        String idString = String.valueOf(id);
        int textWidth = metrics.stringWidth(idString);
        int textHeight = metrics.getAscent() - metrics.getDescent();
        g.drawString(idString, x - textWidth / 2, y + textHeight / 2);
    }

    public void move() {
        if (direction == 0) {
            this.y -= speed;
        } else if (direction == 1) {
            this.y += speed;
        }
    }

    public boolean intersects(Person other) {
        int dx = this.x - other.x;
        int dy = this.y - other.y;
        int distanceSquared = dx * dx + dy * dy;
        return distanceSquared < (2 * RADIUS) * (2 * RADIUS);
    }
}

