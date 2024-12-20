package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TrafficSimulation extends JPanel {

    private final int WIDTH = 1440;
    private final int HEIGHT = 780;
    private final Color GRAY = new Color(169, 169, 169);
    private final Color WHITE = Color.WHITE;
    private final Color GREEN = new Color(34, 139, 34);
    private final List<Car> cars = new ArrayList<>();
    private final Random random = new Random();
    private final int[] trafficLightStates = new int[6]; // 0 - red, 1 - yellow, 2 - green
    private int currentPhase = 0;
    private long phaseStartTime;

    public TrafficSimulation() {
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(GREEN);
        phaseStartTime = System.currentTimeMillis();

        // Timer do aktualizacji samochodów
        Timer updateTimer = new Timer(50, e -> {
            updateTrafficLights();
            for (Car car : cars) {
                car.move(trafficLightStates); // Przesuń samochód tylko jeśli światło pozwala
                if (car.hasFinished()) {
                    cars.remove(car);
                    break; // Unikamy ConcurrentModificationException
                }
            }
            repaint();
        });

        // Timer do dodawania nowych samochodów
        Timer spawnTimer = new Timer(1000 + random.nextInt(2000), e -> {
            cars.add(new Car(chooseRandomPath())); // Dodaj nowy samochód z losową trasą
        });

        updateTimer.start();
        spawnTimer.start();
    }

    private void updateTrafficLights() {
        long elapsed = (System.currentTimeMillis() - phaseStartTime) / 1000;
        int greenDuration = 10; // 10 sekund zielone
        int yellowDuration = 2; // 2 sekundy żółte
        int totalPhaseDuration = greenDuration + yellowDuration * 2; // Całkowity czas fazy

        // Oblicz czas w bieżącej fazie
        long phaseTime = elapsed % totalPhaseDuration;

        if (phaseTime < greenDuration) {
            // Zielone światła dla aktywnej fazy
            switch (currentPhase) {
                case 0 -> setTrafficLights(new int[]{2, 2, 0, 0, 0, 0}); // Północ/Południe zielone
                case 1 -> setTrafficLights(new int[]{0, 0, 2, 0, 2, 0}); // Wschód/Zachód prawe pasy zielone
                case 2 -> setTrafficLights(new int[]{0, 0, 0, 2, 0, 2}); // Wschód/Zachód lewe pasy zielone
            }
        } else if (phaseTime < greenDuration + yellowDuration) {
            // Żółte światła dla aktywnej fazy
            switch (currentPhase) {
                case 0 -> setTrafficLights(new int[]{1, 1, 0, 0, 0, 0}); // Północ/Południe żółte
                case 1 -> setTrafficLights(new int[]{0, 0, 1, 0, 1, 0}); // Wschód/Zachód prawe pasy żółte
                case 2 -> setTrafficLights(new int[]{0, 0, 0, 1, 0, 1}); // Wschód/Zachód lewe pasy żółte
            }
        } else {
            // Czerwone światła dla wszystkich + przygotowanie na żółte w następnej fazie
            // Żółte w fazie przygotowującej się do przejścia na zielone
            int nextPhase = (currentPhase + 1) % 3;
            switch (nextPhase) {
                case 0 -> setTrafficLights(new int[]{1, 1, 0, 0, 0, 0}); // Północ/Południe przygotowanie
                case 1 -> setTrafficLights(new int[]{0, 0, 1, 0, 1, 0}); // Wschód/Zachód prawe pasy przygotowanie
                case 2 -> setTrafficLights(new int[]{0, 0, 0, 1, 0, 1}); // Wschód/Zachód lewe pasy przygotowanie
            }
        }

        // Przełącz fazę po pełnym cyklu
        if (elapsed >= totalPhaseDuration) {
            phaseStartTime = System.currentTimeMillis();
            currentPhase = (currentPhase + 1) % 3; // Przełącz fazę (0 -> 1 -> 2 -> 0)
        }
    }





    private void setTrafficLights(int[] states) {
        System.arraycopy(states, 0, trafficLightStates, 0, states.length);
    }

    // Funkcja losująca jedną z 12 tras
    private List<Segment> chooseRandomPath() {
        List<Segment> path = new ArrayList<>();
        int route = random.nextInt(12) + 1; // Losuj liczbę od 1 do 12

        switch (route) {
            case 1 -> { // Trasa 1 - północ - wschód
                path.add(new LineSegment(new Point(WIDTH / 2 - 40, 0), new Point(WIDTH / 2 - 40, HEIGHT / 2)));
                path.add(new ArcSegment(new Point(WIDTH / 2 - 40, HEIGHT / 2 - 70), new Point(WIDTH / 2 + 45, HEIGHT / 2 + 70), 180, 270));
                path.add(new LineSegment(new Point(WIDTH / 2, HEIGHT / 2 + 70), new Point(WIDTH, HEIGHT / 2 + 70)));
            }
            case 2 -> path.add(new LineSegment(new Point(WIDTH / 2 - 40, 0), new Point(WIDTH / 2 - 40, HEIGHT))); // Trasa 2 - północ - południe
            case 3 -> { // Trasa 3 - północ - zachód
                path.add(new LineSegment(new Point(WIDTH / 2 - 40, 0), new Point(WIDTH / 2 - 40, HEIGHT / 2 - 100)));
                path.add(new ArcSegment(new Point(WIDTH / 2 - 40, HEIGHT / 2 - 130), new Point(WIDTH / 2 - 120, HEIGHT / 2 - 68), 0, -90));
                path.add(new LineSegment(new Point(WIDTH / 2 - 80, HEIGHT / 2 - 68), new Point(0, HEIGHT / 2 - 68)));
            }
            case 4 -> { // Trasa 4 - wschód - północ
                path.add(new LineSegment(new Point(WIDTH, HEIGHT / 2 - 68), new Point(WIDTH / 2 + 60, HEIGHT / 2 - 68)));
                path.add(new ArcSegment(new Point(WIDTH / 2 + 80, HEIGHT / 2 - 68), new Point(WIDTH / 2 + 40, HEIGHT / 2 - 100), -90, -180));
                path.add(new LineSegment(new Point(WIDTH / 2 + 40, HEIGHT / 2 - 85), new Point(WIDTH / 2 + 40, 0)));
            }
            case 5 -> path.add(new LineSegment(new Point(WIDTH, HEIGHT / 2 - 68), new Point(0, HEIGHT / 2 - 68))); // Trasa 5 - wschód - zachód
            case 6 -> { // Trasa 6 - wschód - południe
                path.add(new LineSegment(new Point(WIDTH, HEIGHT / 2), new Point(WIDTH / 2, HEIGHT / 2)));
                path.add(new ArcSegment(new Point(WIDTH / 2 + 40, HEIGHT / 2), new Point(WIDTH / 2 - 40, HEIGHT / 2 + 200), 90, 180));
                path.add(new LineSegment(new Point(WIDTH / 2 - 40, HEIGHT / 2 + 100), new Point(WIDTH / 2 - 40, HEIGHT)));
            }
            case 7 -> { // Trasa 7 - południe - wschód
                path.add(new LineSegment(new Point(WIDTH / 2 + 40, HEIGHT), new Point(WIDTH / 2 + 40, HEIGHT / 2 + 100)));
                path.add(new ArcSegment(new Point(WIDTH / 2 + 40, HEIGHT / 2 + 130), new Point(WIDTH / 2 + 120, HEIGHT / 2 + 68), 180, 90));
                path.add(new LineSegment(new Point(WIDTH / 2 + 80, HEIGHT / 2 + 68), new Point(WIDTH, HEIGHT / 2 + 68)));
            }
            case 8 -> path.add(new LineSegment(new Point(WIDTH / 2 + 40, HEIGHT), new Point(WIDTH / 2 + 40, 0))); // Trasa 8 - południe - północ
            case 9 -> { // Trasa 9 - południe - zachód
                path.add(new LineSegment(new Point(WIDTH / 2 + 40, HEIGHT), new Point(WIDTH / 2 + 40, HEIGHT / 2 - 34)));
                path.add(new ArcSegment(new Point(WIDTH / 2 + 40, HEIGHT / 2), new Point(WIDTH / 2 - 200, HEIGHT / 2 - 68), 0, 90));
                path.add(new LineSegment(new Point(WIDTH / 2 - 80, HEIGHT / 2 - 68), new Point(0, HEIGHT / 2 - 68)));
            }
            case 10 -> { // Trasa 10 - zachód - południe
                path.add(new LineSegment(new Point(0, HEIGHT / 2 + 68), new Point(WIDTH / 2 - 80, HEIGHT / 2 + 68)));
                path.add(new ArcSegment(new Point(WIDTH / 2 - 120, HEIGHT / 2 + 68), new Point(WIDTH / 2 - 40, HEIGHT / 2 + 130), 90, 0));
                path.add(new LineSegment(new Point(WIDTH / 2 - 40, HEIGHT / 2 + 100), new Point(WIDTH / 2 - 40, HEIGHT)));
            }
            case 11 -> path.add(new LineSegment(new Point(0, HEIGHT / 2 + 68), new Point(WIDTH, HEIGHT / 2 + 68))); // Trasa 11 - zachód - wschód
            case 12 -> { // Trasa 12 - zachód - północ
                path.add(new LineSegment(new Point(0, HEIGHT / 2), new Point(WIDTH / 2, HEIGHT / 2)));
                path.add(new ArcSegment(new Point(WIDTH / 2 - 40, HEIGHT / 2), new Point(WIDTH / 2 + 40, HEIGHT / 2 - 200), 270, 360));
                path.add(new LineSegment(new Point(WIDTH / 2 + 40, HEIGHT / 2 - 100), new Point(WIDTH / 2 + 40, 0)));
            }
        }

        return path;
    }

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
        }}

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawRoad(g, WIDTH / 2, 0, HEIGHT / 2 - 100, 80, 2, false);
        drawRoad(g, WIDTH / 2, HEIGHT / 2 + 100, HEIGHT / 2 - 100, 80, 2, false);
        drawRoad(g, 0, HEIGHT / 2, WIDTH / 2 - 80, 67, 3, true);
        drawRoad(g, WIDTH / 2 + 80, HEIGHT / 2, WIDTH / 2 - 80, 67, 3, true);
        g.setColor(GRAY);
        g.fillRect(WIDTH / 2 - 80, HEIGHT / 2 - 100, 160, 201);

        // Rysowanie świateł
        drawTrafficLight(g, WIDTH / 2 - 80, HEIGHT / 2 - 130, false, trafficLightStates[0]); // Północ
        drawTrafficLight(g, WIDTH / 2 + 80, HEIGHT / 2 + 130, false, trafficLightStates[1]); // Południe
        drawTrafficLight(g, WIDTH / 2 + 110, HEIGHT / 2 - 100, true, trafficLightStates[2]); // Wschód - prawo/prosto
        drawTrafficLight(g, WIDTH / 2 + 110, HEIGHT / 2 - 33, true, trafficLightStates[3]); // Wschód - lewo
        drawTrafficLight(g, WIDTH / 2 - 110, HEIGHT / 2 + 102, true, trafficLightStates[4]); // Zachód - prawo/prosto
        drawTrafficLight(g, WIDTH / 2 - 110, HEIGHT / 2 + 34, true, trafficLightStates[5]); // Zachód - lewo

        for (Car car : cars) {
            car.draw(g);
        }
    }

    private void drawTrafficLight(Graphics g, int centerX, int centerY, boolean horizontal, int state) {
        int rectWidth = 24;
        int rectHeight = 45;
        int radius = 6;

        if (horizontal) {
            g.setColor(Color.BLACK);
            g.fillRect(centerX - rectHeight / 2, centerY - rectWidth / 2, rectHeight, rectWidth - 1);
            int redX = centerX - rectHeight / 2 + 2;
            int yellowX = redX + radius * 2 + 2;
            int greenX = yellowX + radius * 2 + 2;
            int lightY = centerY - radius;

            g.setColor(state == 2 ? Color.GREEN : (state == 1 ? Color.YELLOW : Color.RED));
            g.fillOval(state == 2 ? greenX : state == 1 ? yellowX : redX, lightY, radius * 2, radius * 2);
        } else {
            g.setColor(Color.BLACK);
            g.fillRect(centerX - rectWidth / 2, centerY - rectHeight / 2, rectWidth, rectHeight - 1);
            int lightX = centerX - radius;
            int redY = centerY - rectHeight / 2 + 2;
            int yellowY = redY + radius * 2 + 2;
            int greenY = yellowY + radius * 2 + 2;

            g.setColor(state == 2 ? Color.GREEN : (state == 1 ? Color.YELLOW : Color.RED));
            g.fillOval(lightX, state == 2 ? greenY : state == 1 ? yellowY : redY, radius * 2, radius * 2);
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

    private static class Car {
        private final List<Segment> path;
        private double currentPosition;

        public Car(List<Segment> path) {
            this.path = path;
            this.currentPosition = 0;
        }

        public void move(int[] trafficLightStates) {
            // Logika sprawdzająca światła i decydująca o ruchu
            if (canMove(trafficLightStates)) {
                currentPosition += 5;
            }
        }

        private boolean canMove(int[] trafficLightStates) {
            // Implementacja sprawdzania, czy samochód może się ruszać na podstawie świateł
            return true; // Placeholder
        }

        public boolean hasFinished() {
            return currentPosition > getTotalPathLength();
        }

        public void draw(Graphics g) {
            Point position = getCarPosition();
            g.setColor(Color.RED);
            g.fillOval(position.x - 5, position.y - 5, 10, 10);
        }

        private Point getCarPosition() {
            double distanceTraveled = currentPosition;
            for (Segment segment : path) {
                if (distanceTraveled <= segment.getLength()) {
                    return segment.getPointAt(distanceTraveled);
                }
                distanceTraveled -= segment.getLength();
            }
            return path.get(path.size() - 1).getPointAt(0);
        }

        private double getTotalPathLength() {
            return path.stream().mapToDouble(Segment::getLength).sum();
        }
    }
}
