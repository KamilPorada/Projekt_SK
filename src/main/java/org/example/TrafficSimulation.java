package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class TrafficSimulation extends JPanel {

    private final int WIDTH = 1440;
    private final int HEIGHT = 780;
    private final Color GRAY = new Color(169, 169, 169);
    private final Color WHITE = Color.WHITE;
    private final Color GREEN = new Color(34, 139, 34); // Grass color

    public TrafficSimulation() {
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(GREEN);

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
