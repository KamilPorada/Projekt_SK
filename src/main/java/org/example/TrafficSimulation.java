package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class TrafficSimulation extends JPanel {

    private static final int WIDTH = 1440;
    private static final int HEIGHT = 780;
    private final int NORTH_STOP_LINE = 140;
    private final int SOUTH_STOP_LINE = 640;
    private final int WEST_STOP_LINE = 490;
    private final int EAST_STOP_LINE = 950;

    private final Color GRAY = new Color(169, 169, 169);
    private final Color WHITE = Color.WHITE;
    private final Color GREEN = new Color(34, 139, 34);
    private final List<Car> cars = new ArrayList<>();
    private int carCounter;        // Licznik samochodów

    private final Random random = new Random();
    private final int[] trafficLightStates = new int[6]; // 0 - red, 1 - yellow, 2 - green
    private long startSimulationTime, starttrafficLightTime;
    private long simulationTime = 0, trafficLightTime=0;
    private int actualTour=1;

    public TrafficSimulation() {
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(GREEN);
        startSimulationTime = System.currentTimeMillis();
        starttrafficLightTime = System.currentTimeMillis();
        carCounter = 1;  // Liczba samochodów zaczyna się od 1

        // Timer do aktualizacji samochodów
        Timer updateTimer = new Timer(50, e -> {
            long currentTime = System.currentTimeMillis();

            simulationTime = (currentTime - startSimulationTime) / 1000;
            trafficLightTime = (currentTime - starttrafficLightTime) / 1000;
            if(trafficLightTime>45)
                starttrafficLightTime = System.currentTimeMillis();


            for (Car car : cars) {
                car.move(trafficLightStates); // Przesuń samochód tylko jeśli światło pozwala
                if (car.hasFinished()) {
                    cars.remove(car);
                    break; // Unikamy ConcurrentModificationException
                }
            }
            updateTrafficLights();
            repaint();
        });

        // Timer do dodawania nowych samochodów
        Timer spawnTimer = new Timer(random.nextInt(2000), e -> {
            // Tworzymy nowy samochód i nadajemy mu numer na podstawie carCounter
            Car newCar = new Car(chooseRandomPath(), actualTour, carCounter);
            cars.add(newCar);
            carCounter++;  // Zwiększamy licznik samochodów
        });

        updateTimer.start();
        spawnTimer.start();
    }

    private void updateTrafficLights() {

        if(trafficLightTime>=0 && trafficLightTime<=2){
            setTrafficLights(new int[]{1, 1, 0, 0, 0, 0});
            actualTour=1;
        }
        else if(trafficLightTime>2 && trafficLightTime<=12){
            setTrafficLights(new int[]{2, 2, 0, 0, 0, 0});
            actualTour=1;
        }
        else if(trafficLightTime>12 && trafficLightTime<=14){
            setTrafficLights(new int[]{1, 1, 0, 0, 0, 0});
            actualTour=1;
        }
        else if(trafficLightTime>15 && trafficLightTime<=17){
            setTrafficLights(new int[]{0, 0, 1, 0, 1, 0});
            actualTour=2;
        }
        else if(trafficLightTime>17 && trafficLightTime<=27){
            setTrafficLights(new int[]{0, 0, 2, 0, 2, 0});
            actualTour=2;
        }
        else if(trafficLightTime>27 && trafficLightTime<=29){
            setTrafficLights(new int[]{0, 0, 1, 0, 1, 0});
            actualTour=2;
        }
        else if(trafficLightTime>30 && trafficLightTime<=32)
        {
            setTrafficLights(new int[]{0, 0, 0, 1, 0, 1});
            actualTour=3;
        }
        else if(trafficLightTime>32 && trafficLightTime<=42){
            setTrafficLights(new int[]{0, 0, 0, 2, 0, 2});
            actualTour=3;
        }
        else if(trafficLightTime>42 && trafficLightTime<=44){
            setTrafficLights(new int[]{0, 0, 0, 1, 0, 1});
            actualTour=3;
        }

    }

    private void setTrafficLights(int[] states) {
        System.arraycopy(states, 0, trafficLightStates, 0, states.length);
    }

    // Funkcja losująca jedną z 12 tras
    private List<Segment> chooseRandomPath() {
        List<Segment> path = new ArrayList<>();

        // Sprawdzamy stan świateł i losujemy trasę tylko wtedy, gdy światło na odpowiednich kierunkach jest zielone
        if (actualTour == 1) { // Północ-Południe
            if (trafficLightStates[0] == 2 && trafficLightStates[1] == 2) {
                int route = random.nextInt(2) + 1; // Losuj między trasami 1 i 2
                if (route == 1) {
                    path.add(new LineSegment(new Point(WIDTH / 2 - 40, 0), new Point(WIDTH / 2 - 40, HEIGHT / 2)));
                    path.add(new ArcSegment(new Point(WIDTH / 2 - 40, HEIGHT / 2 - 70), new Point(WIDTH / 2 + 45, HEIGHT / 2 + 70), 180, 270));
                    path.add(new LineSegment(new Point(WIDTH / 2, HEIGHT / 2 + 70), new Point(WIDTH, HEIGHT / 2 + 70)));
                } else {
                    path.add(new LineSegment(new Point(WIDTH / 2 - 40, 0), new Point(WIDTH / 2 - 40, HEIGHT))); // Trasa 2 - północ - południe
                }
            }
        } else if (actualTour == 2) { // Wschód-Zachód
            if (trafficLightStates[2] == 2 && trafficLightStates[4] == 2) {
                int route = random.nextInt(2) + 5; // Losuj między trasami 5 i 6
                if (route == 5) {
                    path.add(new LineSegment(new Point(WIDTH, HEIGHT / 2 - 68), new Point(0, HEIGHT / 2 - 68))); // Trasa 5 - wschód - zachód
                } else {
                    path.add(new LineSegment(new Point(WIDTH, HEIGHT / 2), new Point(WIDTH / 2, HEIGHT / 2)));
                    path.add(new ArcSegment(new Point(WIDTH / 2 + 40, HEIGHT / 2), new Point(WIDTH / 2 - 40, HEIGHT / 2 + 200), 90, 180));
                    path.add(new LineSegment(new Point(WIDTH / 2 - 40, HEIGHT / 2 + 100), new Point(WIDTH / 2 - 40, HEIGHT))); // Trasa 6 - wschód - południe
                }
            }
        } else if (actualTour == 3) { // Zachód-Południe
            if (trafficLightStates[3] == 2 && trafficLightStates[5] == 2) {
                int route = random.nextInt(2) + 9; // Losuj między trasami 9 i 10
                if (route == 9) {
                    path.add(new LineSegment(new Point(WIDTH / 2 + 40, HEIGHT), new Point(WIDTH / 2 + 40, HEIGHT / 2 - 34)));
                    path.add(new ArcSegment(new Point(WIDTH / 2 + 40, HEIGHT / 2), new Point(WIDTH / 2 - 200, HEIGHT / 2 - 68), 0, 90));
                    path.add(new LineSegment(new Point(WIDTH / 2 - 80, HEIGHT / 2 - 68), new Point(0, HEIGHT / 2 - 68))); // Trasa 9 - południe - zachód
                } else {
                    path.add(new LineSegment(new Point(0, HEIGHT / 2 + 68), new Point(WIDTH / 2 - 80, HEIGHT / 2 + 68)));
                    path.add(new ArcSegment(new Point(WIDTH / 2 - 120, HEIGHT / 2 + 68), new Point(WIDTH / 2 - 40, HEIGHT / 2 + 130), 90, 0));
                    path.add(new LineSegment(new Point(WIDTH / 2 - 40, HEIGHT / 2 + 100), new Point(WIDTH / 2 - 40, HEIGHT))); // Trasa 10 - zachód - południe
                }
            }
        }

        return path;
    }


    private void drawRoad(Graphics g, int startX, int startY, int length, int laneWidth, int laneCount, boolean isHorizontal, boolean isDirectional) {
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

            // Rysowanie linii przerywanej prostopadłej do kierunku drogi
            if(isDirectional){
                int stopLineX = startX + length - 150; // 150 jednostek przed końcem drogi
                for (int y = startY+70; y <= startY + roadWidth-5; y += 20) {
                    g.drawLine(stopLineX, y, stopLineX, y + 10); // Segment przerywanej linii prostopadłej
                }
            }
            else{
                int stopLineX = startX + 150; // 150 jednostek przed końcem drogi
                for (int y = startY; y <= startY + roadWidth-70; y += 20) {
                    g.drawLine(stopLineX, y, stopLineX, y + 10); // Segment przerywanej linii prostopadłej
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

            // Rysowanie linii przerywanej prostopadłej do kierunku drogi
            if(isDirectional){
                int stopLineY = startY + 150; // 150 jednostek przed końcem drogi
                for (int x = startX+85; x <= startX + roadWidth-5; x += 20) {
                    g.drawLine(x, stopLineY, x + 10, stopLineY); // Segment przerywanej linii prostopadłej
                }
            }
            else{
                int stopLineY = startY + length - 150; // 150 jednostek przed końcem drogi
                for (int x = startX+5; x <= startX + roadWidth-85; x += 20) {
                    g.drawLine(x, stopLineY, x + 10, stopLineY); // Segment przerywanej linii prostopadłej
                }
            }
        }
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawRoad(g, WIDTH / 2, 0, HEIGHT / 2 - 100, 80, 2, false,false); //północ
        drawRoad(g, WIDTH / 2, HEIGHT / 2 + 100, HEIGHT / 2 - 100, 80, 2, false,true); //południe
        drawRoad(g, 0, HEIGHT / 2, WIDTH / 2 - 80, 67, 3, true,true); // zachód
        drawRoad(g, WIDTH / 2 + 80, HEIGHT / 2, WIDTH / 2 - 80, 67, 3, true,false);  //wschód
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
        private int actualTour;  // Dodaj zmienną do klasy Car
        private int carNumber;  // Numer samochodu


        public Car(List<Segment> path, int actualTour, int carNumber) {
            this.path = path;
            this.currentPosition = 0;
            this.actualTour = actualTour;  // Ustaw wartość actualTour
            this.carNumber = carNumber;
        }

        public void move(int[] trafficLightStates) {
            // Logika sprawdzająca światła i decydująca o ruchu
            if (canMove(trafficLightStates)) {
                currentPosition += 5;
            }
        }

        private boolean canMove(int[] trafficLightStates) {
//            // Przykład dla tury 1 - północ i południe
//            if (actualTour == 1) {
//                // Sprawdź, czy światło na północy jest zielone (trafficLightStates[0] == 2) i na południu (trafficLightStates[1] == 2)
//                if (trafficLightStates[0] == 2 && trafficLightStates[1] == 2) {
//                    return true; // Możesz się poruszać
//                }
//            }
//            // Przykład dla tury 2 - wschód i zachód
//            else if (actualTour == 2) {
//                // Sprawdź, czy światła na wschodzie i zachodzie są zielone
//                if (trafficLightStates[2] == 2 && trafficLightStates[4] == 2) {
//                    return true; // Możesz się poruszać
//                }
//            }
//            // Przykład dla tury 3 - wschód i zachód
//            else if (actualTour == 3) {
//                // Sprawdź, czy światła na wschodzie i zachodzie są zielone
//                if (trafficLightStates[3] == 2 && trafficLightStates[5] == 2) {
//                    return true; // Możesz się poruszać
//                }
//            }
            return true; // Jeżeli nie pasuje żadna tura lub światło jest czerwone
        }



        public boolean hasFinished() {
            return currentPosition > getTotalPathLength();
        }

        public void draw(Graphics g) {
            Point position = getCarPosition(); // Pobieramy pozycję samochodu

            // Rysowanie samochodu jako kropki
            g.setColor(Color.RED);
            g.fillOval(position.x - 5, position.y - 5, 10, 10); // Rysowanie kropki w odpowiedniej pozycji

            // Rysowanie numeru samochodu obok kropki
            g.setColor(Color.BLACK); // Kolor numeru
            g.drawString(String.valueOf(carNumber), position.x + 10, position.y); // Rysowanie numeru samochodu
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
