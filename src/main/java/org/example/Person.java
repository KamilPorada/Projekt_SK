package org.example;

import java.awt.*;
import java.util.Random;

class Person {
    private int x, y;         // Współrzędne kropki
    private Color color;      // Kolor kropki (różowy lub niebieski)
    private final int RADIUS = 7; // Promień kropki (średnica 10)
    private int speed;        // Prędkość poruszania się osoby (od 1 do 3)
    private int direction;    // Kierunek ruchu (0 - północ, 1 - południe)
    private int id;           // Numer ID osoby

    // Konstruktor klasy Person
    public Person(int x, int y, int gender, int direction, int id) {
        this.x = x;
        this.y = y;

        // Ustawienie koloru w zależności od płci
        if (gender == 0) {
            this.color = Color.PINK;  // Kobieta - różowa kropka
        } else if (gender == 1) {
            this.color = Color.BLUE;  // Mężczyzna - niebieska kropka
        } else {
            throw new IllegalArgumentException("Płeć musi być 0 (kobieta) lub 1 (mężczyzna)");
        }

        // Losowanie prędkości od 1 do 3
        Random random = new Random();
        this.speed = random.nextInt(3) + 1;

        // Kierunek ruchu (0 - północ, 1 - południe)
        this.direction = direction;

        // Ustawienie numeru ID osoby
        this.id = id;
    }

    // Metoda rysująca kropkę na ekranie
    public void draw(Graphics g) {
        // Rysowanie kropki
        g.setColor(color);
        g.fillOval(x - RADIUS, y - RADIUS, 2 * RADIUS, 2 * RADIUS);

        // Wybór koloru tekstu w zależności od koloru kropki
        if (color == Color.PINK) {
            g.setColor(Color.BLACK); // Czarny tekst na różowej kropce
        } else if (color == Color.BLUE) {
            g.setColor(Color.WHITE); // Biały tekst na niebieskiej kropce
        }

        // Rysowanie numeru ID
        g.setFont(new Font("Arial", Font.BOLD, 10)); // Czcionka
        FontMetrics metrics = g.getFontMetrics();
        String idString = String.valueOf(id);
        int textWidth = metrics.stringWidth(idString);
        int textHeight = metrics.getAscent() - metrics.getDescent();
        g.drawString(idString, x - textWidth / 2, y + textHeight / 2);
    }

    // Metoda przesuwająca kropkę o jej prędkość w odpowiednim kierunku
    public void move() {
        if (direction == 0) {
            this.y -= speed;  // Jeśli kierunek północny, zmniejszamy współrzędną Y
        } else if (direction == 1) {
            this.y += speed;  // Jeśli kierunek południowy, zwiększamy współrzędną Y
        }
    }

    // Metoda sprawdzająca, czy ta osoba koliduje z inną
    public boolean intersects(Person other) {
        int dx = this.x - other.x;
        int dy = this.y - other.y;
        int distanceSquared = dx * dx + dy * dy;
        return distanceSquared < (2 * RADIUS) * (2 * RADIUS);
    }

    // Getter dla współrzędnych X i Y
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    // Getter dla prędkości
    public int getSpeed() {
        return speed;
    }

    // Getter dla kierunku
    public int getDirection() {
        return direction;
    }
}
