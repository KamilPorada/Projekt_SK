package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Objects;

public class TrafficSimulation extends JPanel {

    private final int WIDTH = 1440;
    private final int HEIGHT = 780;
    private final Color GRAY = new Color(169, 169, 169);
    private final Color WHITE = Color.WHITE;
    private final Color GREEN = new Color(34, 139, 34);
    private java.util.List<Segment> path = new ArrayList<>();
    private double currentPosition = 0;
    private double totalPathLength;
    private Timer timer;

    public TrafficSimulation() {
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(GREEN);

//      Trasa 1 - pólnoc - wschód
//        path.add(new LineSegment(new Point(WIDTH/2-40, 0), new Point(WIDTH/2-40, HEIGHT/2)));
//        path.add(new ArcSegment(new Point(WIDTH/2-40, HEIGHT/2-70),new Point(WIDTH/2+45, HEIGHT/2+70),180,270));
//        path.add(new LineSegment(new Point(WIDTH/2, HEIGHT/2+70), new Point(WIDTH, HEIGHT/2+70)));
//        calculateTotalPathLength();


//        Trasa 2 - pólnoc - południe
//        path.add(new LineSegment(new Point(WIDTH/2-40, 0), new Point(WIDTH/2-40, HEIGHT)));
//        calculateTotalPathLength();

//        Trasa 3 - pólnoc - zachód
//        path.add(new LineSegment(new Point(WIDTH/2-40, 0), new Point(WIDTH/2-40, HEIGHT/2-100)));
//        path.add(new ArcSegment(new Point(WIDTH/2-40, HEIGHT/2-130),new Point(WIDTH/2-120, HEIGHT/2-68),0,-90));
//        path.add(new LineSegment(new Point(WIDTH/2-80, HEIGHT/2-68),new Point(0, HEIGHT/2-68)));
//        calculateTotalPathLength();

        //Trasa 4 - wschód - północ
//        path.add(new LineSegment(new Point(WIDTH, HEIGHT/2-68),new Point(WIDTH/2+60, HEIGHT/2-68)));
//        path.add(new ArcSegment(new Point(WIDTH/2+80, HEIGHT/2-68),new Point(WIDTH/2+40, HEIGHT/2-100),-90,-180));
//        path.add(new LineSegment(new Point(WIDTH/2+40, HEIGHT/2-85), new Point(WIDTH/2+40, 0)));
//        calculateTotalPathLength();

        //Trasa 5 - wschód - zachód
//        path.add(new LineSegment(new Point(WIDTH, HEIGHT/2-68),new Point(0, HEIGHT/2-68)));
//        calculateTotalPathLength();

        //Trasa 6 wchód - południe
//        path.add(new LineSegment(new Point(WIDTH, HEIGHT/2),new Point(WIDTH/2, HEIGHT/2)));
//        path.add(new ArcSegment(new Point(WIDTH/2+40, HEIGHT/2),new Point(WIDTH/2-40, HEIGHT/2+200),90,180));
//        path.add(new LineSegment(new Point(WIDTH/2-40, HEIGHT/2+100),new Point(WIDTH/2-40, HEIGHT)));
//        calculateTotalPathLength();

        //Trasa 7 południe - wschód
//        path.add(new LineSegment(new Point(WIDTH/2+40, HEIGHT),new Point(WIDTH/2+40, HEIGHT/2+100)));
//        path.add(new ArcSegment(new Point(WIDTH/2+40, HEIGHT/2+130),new Point(WIDTH/2+120, HEIGHT/2+68),180,90));
//        path.add(new LineSegment(new Point(WIDTH/2+80, HEIGHT/2+68),new Point(WIDTH, HEIGHT/2+68)));
//        calculateTotalPathLength();

        //Trasa 8 południe - północ
//        path.add(new LineSegment(new Point(WIDTH/2+40, HEIGHT),new Point(WIDTH/2+40, 0)));
//        calculateTotalPathLength();

        //Trasa 9 południe - zachód
//        path.add(new LineSegment(new Point(WIDTH/2+40, HEIGHT),new Point(WIDTH/2+40, HEIGHT/2-34)));
//        path.add(new ArcSegment(new Point(WIDTH/2+40, HEIGHT/2),new Point(WIDTH/2-200, HEIGHT/2-68),0,90));
//        path.add(new LineSegment(new Point(WIDTH/2-80, HEIGHT/2-68),new Point(0, HEIGHT/2-68)));
//        calculateTotalPathLength();

        //Trasa 10 zachód - południe
//        path.add(new LineSegment(new Point(0, HEIGHT/2+68),new Point(WIDTH/2-80, HEIGHT/2+68)));
//        path.add(new ArcSegment(new Point(WIDTH/2-120, HEIGHT/2+68),new Point(WIDTH/2-40, HEIGHT/2+130),90,0));
//        path.add(new LineSegment(new Point(WIDTH/2-40, HEIGHT/2+100),new Point(WIDTH/2-40, HEIGHT)));
//        calculateTotalPathLength();

        //Trasa 11 zachód - wschód
//        path.add(new LineSegment(new Point(0, HEIGHT/2+68),new Point(WIDTH, HEIGHT/2+68)));
//        calculateTotalPathLength();

        //Trasa 12 zachód - południe
//        path.add(new LineSegment(new Point(0, HEIGHT/2),new Point(WIDTH/2, HEIGHT/2)));
//        path.add(new ArcSegment(new Point(WIDTH/2-40, HEIGHT/2),new Point(WIDTH/2+40, HEIGHT/2-200),270,360));
//        path.add(new LineSegment(new Point(WIDTH/2+40, HEIGHT/2-100),new Point(WIDTH/2+40, 0)));
//        calculateTotalPathLength();


        timer = new Timer(50, e -> {
            currentPosition += 5; // Prędkość samochodu
            if (currentPosition > totalPathLength) { // Poprawione sprawdzenie końca trasy
                currentPosition = 0; // Zresetuj pozycję na początek
            }
            repaint();
        });
        timer.start();

    }

    private void calculateTotalPathLength() {
        totalPathLength = 0;
        for (Segment segment : path) {
            totalPathLength += segment.getLength();
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Traffic Simulation");
        TrafficSimulation simulation = new TrafficSimulation();
        frame.add(simulation);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Pas północ - skrzyżowanie
        drawRoad(g, WIDTH / 2, 0, HEIGHT / 2 - 100, 80, 2, false);
        // Pas skrzyżowanie - południe
        drawRoad(g, WIDTH / 2, HEIGHT / 2 + 100, HEIGHT / 2 - 100, 80, 2, false);
        // Pas zachód - skrzyżowanie
        drawRoad(g, 0, HEIGHT / 2, WIDTH / 2 - 80, 67, 3, true);
        // Pas skrzyżowanie wschód
        drawRoad(g, WIDTH / 2 + 80, HEIGHT / 2, WIDTH / 2 - 80, 67, 3, true);
        // Środek skrzyżowania
        g.setColor(GRAY);
        g.fillRect(WIDTH / 2 - 80, HEIGHT / 2 - 100, 160, 201);

        for (Segment segment : path) {
            segment.draw(g);
        }

        Point carPosition = getCarPosition();
        g.setColor(Color.RED);
        g.fillOval(carPosition.x - 5, carPosition.y - 5, 10, 10);

    }

    private Point getCarPosition() {
        double distanceTraveled = currentPosition;
        for (Segment segment : path) {
            if (distanceTraveled <= segment.getLength()) {
                return segment.getPointAt(distanceTraveled);
            }
            distanceTraveled -= segment.getLength();
        }
        return path.get(path.size() - 1).getPointAt(currentPosition); // Powinno być ostatnią pozycją na trasie
    }

    // Funkcja do rysowania drogi
    private void drawRoad(Graphics g, int startX, int startY, int length, int laneWidth, int laneCount, boolean isHorizontal) {
        int roadWidth = laneWidth * laneCount;

        if (isHorizontal) {
            startY = startY - roadWidth / 2;
            g.setColor(GRAY);
            g.fillRect(startX, startY, length, roadWidth);

            // Rysowanie białych linii granicznych
            g.setColor(WHITE);
            g.drawLine(startX, startY, startX + length, startY); // Górna granica
            g.drawLine(startX, startY + roadWidth, startX + length, startY + roadWidth); // Dolna granica

            // Rysowanie przerywanych linii między pasami
            for (int i = 1; i < laneCount; i++) {
                int y = startY + i * laneWidth;
                for (int x = startX; x < startX + length; x += 20) {
                    g.drawLine(x, y, x + 10, y); // Segment przerywanej linii
                }
            }
        } else {
            startX = startX - roadWidth / 2;
            g.setColor(GRAY);
            g.fillRect(startX, startY, roadWidth, length);

            // Rysowanie białych linii granicznych
            g.setColor(WHITE);
            g.drawLine(startX, startY, startX, startY + length); // Lewa granica
            g.drawLine(startX + roadWidth, startY, startX + roadWidth, startY + length); // Prawa granica

            // Rysowanie przerywanych linii między pasami
            for (int i = 1; i < laneCount; i++) {
                int x = startX + i * laneWidth;
                for (int y = startY; y < startY + length; y += 20) {
                    g.drawLine(x, y, x, y + 10); // Segment przerywanej linii
                }
            }
        }
    }
}
