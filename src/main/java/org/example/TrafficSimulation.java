package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class TrafficSimulation extends JPanel {

    private static final int WIDTH = 1440;
    private static final int HEIGHT = 780;
    private final Color GRAY = new Color(169, 169, 169);
    private final Color WHITE = Color.WHITE;
    private final Color GREEN = new Color(34, 139, 34);
    private final List<Car> cars = new ArrayList<>();
    private ArrayList<Person> people = new ArrayList<>(); // Lista osób (kropek)

    private int carCounter, personNumber;

    private final Random random = new Random();
    private final int[] trafficLightStates = new int[6]; // 0 - red, 1 - yellow, 2 - green, 3 - failure
    private int pedestrianLightStates = 2; // 0 - red, 1 - yellow, 2 - green,

    private long startSimulationTime, starttrafficLightTime;
    private long simulationTime = 0, trafficLightTime = 0;
    private int actualTour = 1;
    private int[] carCounts = new int[5]; // 0: car, 1: SUV, 2: bus, 3: truck, 4: motorcycle
    private int[] carsInEachRoute = new int[12];
    int[] trafficLightsFailure = new int[6]; // 0: działa, 1: awaria

    // Dostępne pasy dla różnych wartości actualTour
    int[][] lanesForTour = {
            {0, 1, 2, 3, 4, 5}, // actualTour = 1
            {6, 7, 8, 9},       // actualTour = 2
            {10, 11}            // actualTour = 3
    };
    private Timer spawnTimer;






    public TrafficSimulation() {
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(GREEN);
        startSimulationTime = System.currentTimeMillis();
        starttrafficLightTime = System.currentTimeMillis();
        carCounter = 1;
        personNumber=1;

        Timer updateTimer = new Timer(30, e -> {
            long currentTime = System.currentTimeMillis();

            simulationTime = (currentTime - startSimulationTime) / 1000;
            trafficLightTime = (currentTime - starttrafficLightTime) / 1000;
            if (trafficLightTime > 45){
                starttrafficLightTime = System.currentTimeMillis();
                generateTrafficLightFailure();
                generateRandomPeople();
            }

            for (Car car : cars) {
                car.move(cars, trafficLightStates);
                if (car.hasFinished()) {
                    cars.remove(car);
                    carCounts[car.getType()]++;
                    break;
                }
            }
            updateTrafficLights();
            updatePedestrianLights();
            repaint();
        });

        spawnTimer = new Timer(30, e -> {
            int[] weights = getLaneWeights(actualTour);
            int totalWeight = 0;
            for (int weight : weights) {
                totalWeight += weight;
            }
            int chosenLane = chooseLane(random, lanesForTour[actualTour - 1], weights, totalWeight);
            double lambda = getLambdaForLane(chosenLane);
            double timeBetweenCars = generateExponentialTime(random, lambda);

            System.out.println("Wygenerowano samochód na pasie: " + chosenLane +
                    " | Czas do następnego: " + timeBetweenCars + " sekund");

            // Ustawiamy czas do następnego spawnowania samochodu na podstawie obliczonego czasu
            spawnTimer.setDelay((int) (timeBetweenCars * 1000)); // Ustawiamy opóźnienie w milisekundach

            // Tworzymy nowy pojazd
            if (Arrays.stream(trafficLightStates).anyMatch(state -> state == 2)) {
                Car newCar = new Car(chooseRandomPath(chosenLane), carCounter, getRandomCarType());
                System.out.println("Samochód:"+getRandomCarType());
                cars.add(newCar);
                carCounter++;
            }
        });

        updateTimer.start();
        spawnTimer.start();
        generateRandomPeople();

    }



    private void moveAllPeople() {
        for (Person person : people) {
            person.move();
        }
    }

    private void generateRandomPeople() {
        Random random = new Random();
        int numberOfPeople = 10+random.nextInt(20); // Liczba osób od 0 do 50

        // Definicja prostokątów
        int[][] rects = {
                {WIDTH - 160, HEIGHT / 2 - 150, 100, 50},  // Prostokąt 1
                {WIDTH - 160, HEIGHT / 2 + 102, 100, 50},  // Prostokąt 2
                {60, HEIGHT / 2 - 150, 100, 50},           // Prostokąt 3
                {60, HEIGHT / 2 + 102, 100, 50}            // Prostokąt 4
        };

        // Losowanie osób w obrębie prostokątów
        for (int i = 0; i < numberOfPeople; i++) {
            // Wybór prostokąta losowo
            int rectIndex = random.nextInt(rects.length);
            int rectX = rects[rectIndex][0];
            int rectY = rects[rectIndex][1];
            int rectWidth = rects[rectIndex][2];
            int rectHeight = rects[rectIndex][3];

            // Losowanie pozycji w obrębie wybranego prostokąta
            int x = rectX + random.nextInt(rectWidth);
            int y = rectY + random.nextInt(rectHeight);

            // Losowanie płci (0 - kobieta, 1 - mężczyzna)
            int gender = random.nextInt(2); // 0 lub 1

            // Określenie kierunku w zależności od prostokąta
            int direction = (rectIndex == 1 || rectIndex == 3) ? 0 : 1; // 0 - północ (prostokąty 2 i 4), 1 - południe (prostokąty 1 i 3)

            // Tworzenie nowej osoby z określonym kierunkiem
            Person person = new Person(x, y, gender, direction, personNumber);
            personNumber++;

            // Sprawdzanie, czy kropka koliduje z istniejącymi
            boolean collides;
            do {
                collides = false;
                for (Person existingPerson : people) {
                    if (person.intersects(existingPerson)) {
                        collides = true; // Jeśli koliduje, szukamy nowej pozycji
                        break;
                    }
                }
                if (collides) {
                    // Generowanie nowej pozycji, jeśli kropka koliduje
                    x = rectX + random.nextInt(rectWidth);
                    y = rectY + random.nextInt(rectHeight);
                    person = new Person(x, y, gender, direction, personNumber); // Tworzymy nową osobę
                    personNumber++;
                }
            } while (collides); // Dopóki koliduje, próbujemy ponownie

            // Dodanie osoby do listy
            people.add(person);
        }
    }



    private int getRandomCarType() {
        double[] probabilities = {0.4, 0.3, 0.25, 0.2, 0.15}; // Prawdopodobieństwa
        double rand = random.nextDouble(); // Losowa liczba od 0 do 1
        double cumulativeProbability = 0.0;
        for (int i = 0; i < probabilities.length; i++) {
            cumulativeProbability += probabilities[i];
            if (rand < cumulativeProbability) {
                return i;
            }
        }
        return 0;
    }


    private static int[] getLaneWeights(int actualTour) {
        return switch (actualTour) {
            case 1 -> new int[]{20, 15, 5, 10, 10, 5}; // Wagi dla pasów 0-5
            case 2 -> new int[]{50, 20, 10, 20};       // Wagi dla pasów 6-9
            case 3 -> new int[]{70, 30};              // Wagi dla pasów 10-11
            default -> throw new IllegalArgumentException("Nieprawidłowa wartość actualTour");
        };
    }

    // Metoda do losowania pasa na podstawie wag
    private static int chooseLane(Random random, int[] lanes, int[] weights, int totalWeight) {
        int randomValue = random.nextInt(totalWeight);
        int cumulativeWeight = 0;

        for (int i = 0; i < lanes.length; i++) {
            cumulativeWeight += weights[i];
            if (randomValue < cumulativeWeight) {
                return lanes[i];
            }
        }
        return -1; // Nie powinno się zdarzyć
    }

    // Generowanie czasu między samochodami (rozklad wykładniczy)
    private static double generateExponentialTime(Random random, double lambda) {
        double u = random.nextDouble();
        return -Math.log(1 - u) / lambda;
    }

    // Pobieranie wartości lambda dla konkretnego pasa
    private static double getLambdaForLane(int lane) {

        if(lane == 0 || lane==6 || lane==10) return 3;
        if(lane == 1 || lane == 4) return 1;
        if(lane==7 || lane==9) return 0.8;
        if(lane == 2 || lane==3 || lane==5 || lane==8) return 0.5;
        if(lane == 11) return 0.2;
        return 0.1;
    }

    private void updatePedestrianLights() {
        if (trafficLightTime >= 0 && trafficLightTime <= 8) {
            moveAllPeople();
            pedestrianLightStates=2;
        } else if(trafficLightTime > 8 && trafficLightTime <= 10){
            moveAllPeople();
            pedestrianLightStates=1;
        }else if(trafficLightTime > 10 && trafficLightTime <= 25){
            moveAllPeople();
            pedestrianLightStates=0;
        }
        else if(trafficLightTime==35){
            pedestrianLightStates=0;
        }
    }

    private void updateTrafficLights() {
        int[] baseTrafficLights;

        if (trafficLightTime >= 0 && trafficLightTime <= 2) {
            baseTrafficLights = new int[]{1, 1, 0, 0, 0, 0};
            actualTour = 1;
        } else if (trafficLightTime > 2 && trafficLightTime <= 12) {
            baseTrafficLights = new int[]{2, 2, 0, 0, 0, 0};
            actualTour = 1;
        } else if (trafficLightTime > 12 && trafficLightTime <= 14) {
            baseTrafficLights = new int[]{1, 1, 0, 0, 0, 0};
            actualTour = 1;
        } else if (trafficLightTime > 15 && trafficLightTime <= 17) {
            baseTrafficLights = new int[]{0, 0, 1, 0, 1, 0};
            actualTour = 2;
        } else if (trafficLightTime > 17 && trafficLightTime <= 27) {
            baseTrafficLights = new int[]{0, 0, 2, 0, 2, 0};
            actualTour = 2;
        } else if (trafficLightTime > 27 && trafficLightTime <= 29) {
            baseTrafficLights = new int[]{0, 0, 1, 0, 1, 0};
            actualTour = 2;
        } else if (trafficLightTime > 30 && trafficLightTime <= 32) {
            baseTrafficLights = new int[]{0, 0, 0, 1, 0, 1};
            actualTour = 3;
        } else if (trafficLightTime > 32 && trafficLightTime <= 42) {
            baseTrafficLights = new int[]{0, 0, 0, 2, 0, 2};
            actualTour = 3;
        } else if (trafficLightTime > 42 && trafficLightTime <= 44) {
            baseTrafficLights = new int[]{0, 0, 0, 1, 0, 1};
            actualTour = 3;
        } else {
            baseTrafficLights = new int[]{0, 0, 0, 0, 0, 0}; // Domyślne, wszystkie światła wyłączone
            actualTour = 1;
        }

        // Uwzględnienie awarii: każde uszkodzone światło ustawia się na żółte (1)
        for (int i = 0; i < baseTrafficLights.length; i++) {
            if (trafficLightsFailure[i] == 1) {
                baseTrafficLights[i] = 3; // Nadpisz stan na żółty migający
            }
        }

        setTrafficLights(baseTrafficLights);
    }


    private void generateTrafficLightFailure() {
        double failureProbability = 0.05; // 5% szans na awarię
        Random random = new Random();

        for (int i = 0; i < trafficLightsFailure.length; i++) {
            // Generuj zdarzenie z rozkładu Bernoulliego
            if (random.nextDouble() < failureProbability) {
                trafficLightsFailure[i] = 1; // Światło uległo awarii
                System.out.println("Światło nr " + (i + 1) + " uległo awarii!");
            }
        }
    }

    private void setTrafficLights(int[] states) {
        System.arraycopy(states, 0, trafficLightStates, 0, states.length);
    }

    private List<Segment> chooseRandomPath(int chosenLane) {
        List<Segment> path = new ArrayList<>();


        if (actualTour == 1) {
            if (trafficLightStates[0] == 2 || trafficLightStates[1] == 2) {
                switch (chosenLane) {
                    case 0:
                        // średnie (częściej wybierane) północ-wschód
                        if(trafficLightsFailure[0]==0){
                            path.add(new LineSegment(new Point(WIDTH / 2 - 40, 0), new Point(WIDTH / 2 - 40, HEIGHT / 2)));
                            path.add(new ArcSegment(new Point(WIDTH / 2 - 40, HEIGHT / 2 - 70), new Point(WIDTH / 2 + 45, HEIGHT / 2 + 70), 180, 270));
                            path.add(new LineSegment(new Point(WIDTH / 2, HEIGHT / 2 + 70), new Point(WIDTH, HEIGHT / 2 + 70)));
                            carsInEachRoute[0]+=1;
                        }
                        break;
                    case 1:
                        // duże (często wybierane) północ-południe
                        if(trafficLightsFailure[0]==0) {
                            path.add(new LineSegment(new Point(WIDTH / 2 - 40, 0), new Point(WIDTH / 2 - 40, HEIGHT)));
                            carsInEachRoute[1] += 1;
                        }
                        break;
                    case 2:
                        // małe (rzadziej wybierane) północ-zachód
                        if(trafficLightsFailure[0]==0) {
                            path.add(new LineSegment(new Point(WIDTH / 2 - 40, 0), new Point(WIDTH / 2 - 40, HEIGHT / 2 - 100)));
                            path.add(new ArcSegment(new Point(WIDTH / 2 - 40, HEIGHT / 2 - 130), new Point(WIDTH / 2 - 120, HEIGHT / 2 - 68), 0, -90));
                            path.add(new LineSegment(new Point(WIDTH / 2 - 80, HEIGHT / 2 - 68), new Point(0, HEIGHT / 2 - 68)));
                            carsInEachRoute[2] += 1;
                        }
                        break;
                    case 3:
                        // średnie (częściej wybierane) południe-wschód
                        if(trafficLightsFailure[1]==0) {
                            path.add(new LineSegment(new Point(WIDTH / 2 + 40, HEIGHT), new Point(WIDTH / 2 + 40, HEIGHT / 2 + 100)));
                            path.add(new ArcSegment(new Point(WIDTH / 2 + 40, HEIGHT / 2 + 130), new Point(WIDTH / 2 + 120, HEIGHT / 2 + 68), 180, 90));
                            path.add(new LineSegment(new Point(WIDTH / 2 + 80, HEIGHT / 2 + 68), new Point(WIDTH, HEIGHT / 2 + 68)));
                            carsInEachRoute[3] += 1;
                        }
                        break;
                    case 4:
                        // duże (częściej wybierane) południe-północ
                        if(trafficLightsFailure[1]==0) {
                            path.add(new LineSegment(new Point(WIDTH / 2 + 40, HEIGHT), new Point(WIDTH / 2 + 40, 0)));
                            carsInEachRoute[4] += 1;
                        }
                        break;
                    case 5:
                        // małe (rzadziej wybierane) południe-zachód
                        if(trafficLightsFailure[1]==0) {
                            path.add(new LineSegment(new Point(WIDTH / 2 + 40, HEIGHT), new Point(WIDTH / 2 + 40, HEIGHT / 2 - 34)));
                            path.add(new ArcSegment(new Point(WIDTH / 2 + 40, HEIGHT / 2), new Point(WIDTH / 2 - 200, HEIGHT / 2 - 68), 0, 90));
                            path.add(new LineSegment(new Point(WIDTH / 2 - 80, HEIGHT / 2 - 68), new Point(0, HEIGHT / 2 - 68)));
                            carsInEachRoute[5] += 1;
                        }
                        break;
                }
            }
        } else if (actualTour == 2) {
            if (trafficLightStates[2] == 2 || trafficLightStates[4] == 2) {
                switch (chosenLane) {
                    case 6:
                        // duże (częściej wybierane) wschód-północ
                        if(trafficLightsFailure[2]==0) {
                            path.add(new LineSegment(new Point(WIDTH, HEIGHT / 2 - 68), new Point(WIDTH / 2 + 60, HEIGHT / 2 - 68)));
                            path.add(new ArcSegment(new Point(WIDTH / 2 + 80, HEIGHT / 2 - 68), new Point(WIDTH / 2 + 40, HEIGHT / 2 - 100), -90, -180));
                            path.add(new LineSegment(new Point(WIDTH / 2 + 40, HEIGHT / 2 - 85), new Point(WIDTH / 2 + 40, 0)));
                            carsInEachRoute[6] += 1;
                        }
                        break;
                    case 7:
                        // średnie wschód-zachód
                        if(trafficLightsFailure[2]==0) {
                            path.add(new LineSegment(new Point(WIDTH, HEIGHT / 2 - 68), new Point(0, HEIGHT / 2 - 68)));
                            carsInEachRoute[7] += 1;
                        }
                        break;
                    case 8:
                        // duże (częściej wybierane) zachód-południe
                        if(trafficLightsFailure[4]==0) {
                            path.add(new LineSegment(new Point(0, HEIGHT / 2 + 68), new Point(WIDTH / 2 - 80, HEIGHT / 2 + 68)));
                            path.add(new ArcSegment(new Point(WIDTH / 2 - 120, HEIGHT / 2 + 68), new Point(WIDTH / 2 - 40, HEIGHT / 2 + 130), 90, 0));
                            path.add(new LineSegment(new Point(WIDTH / 2 - 40, HEIGHT / 2 + 100), new Point(WIDTH / 2 - 40, HEIGHT)));
                            carsInEachRoute[8] += 1;
                        }
                        break;
                    case 9:
                        // średnie zachód-wschód
                        if(trafficLightsFailure[4]==0) {
                            path.add(new LineSegment(new Point(0, HEIGHT / 2 + 68), new Point(WIDTH, HEIGHT / 2 + 68)));
                            carsInEachRoute[9] += 1;
                        }
                        break;
                }
            }
        } else if (actualTour == 3) {
            if (trafficLightStates[3] == 2 || trafficLightStates[5] == 2) {
                switch (chosenLane) {
                    case 10:
                        // małe (rzadziej wybierane) wschód-południe
                        if(trafficLightsFailure[3]==0) {
                            path.add(new LineSegment(new Point(WIDTH, HEIGHT / 2), new Point(WIDTH / 2, HEIGHT / 2)));
                            path.add(new ArcSegment(new Point(WIDTH / 2 + 40, HEIGHT / 2), new Point(WIDTH / 2 - 40, HEIGHT / 2 + 200), 90, 180));
                            path.add(new LineSegment(new Point(WIDTH / 2 - 40, HEIGHT / 2 + 100), new Point(WIDTH / 2 - 40, HEIGHT)));
                            carsInEachRoute[10] += 1;
                        }
                        break;
                    case 11:
                        // małe (rzadziej wybierane) zachód-północ
                        if(trafficLightsFailure[5]==0) {
                            path.add(new LineSegment(new Point(0, HEIGHT / 2), new Point(WIDTH / 2, HEIGHT / 2)));
                            path.add(new ArcSegment(new Point(WIDTH / 2 - 40, HEIGHT / 2), new Point(WIDTH / 2 + 40, HEIGHT / 2 - 200), 270, 360));
                            path.add(new LineSegment(new Point(WIDTH / 2 + 40, HEIGHT / 2 - 100), new Point(WIDTH / 2 + 40, 0)));
                            carsInEachRoute[11] += 1;
                        }
                        break;
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
            if (isDirectional) {
                int stopLineX = startX + length - 620; // 150 jednostek przed końcem drogi
                for (int y = startY + 70; y <= startY + roadWidth - 5; y += 20) {
                    g.drawLine(stopLineX, y, stopLineX, y + 10); // Segment przerywanej linii prostopadłej
                }
            } else {
                int stopLineX = startX + 620; // 150 jednostek przed końcem drogi
                for (int y = startY; y <= startY + roadWidth - 70; y += 20) {
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
            if (isDirectional) {
                int stopLineY = startY + 270; // 150 jednostek przed końcem drogi
                for (int x = startX + 85; x <= startX + roadWidth - 5; x += 20) {
                    g.drawLine(x, stopLineY, x + 10, stopLineY); // Segment przerywanej linii prostopadłej
                }
            } else {
                int stopLineY = startY + length - 270; // 150 jednostek przed końcem drogi
                for (int x = startX + 5; x <= startX + roadWidth - 85; x += 20) {
                    g.drawLine(x, stopLineY, x + 10, stopLineY); // Segment przerywanej linii prostopadłej
                }
            }
        }
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawRoad(g, WIDTH / 2, 0, HEIGHT / 2 - 100, 80, 2, false, false); //północ
        drawRoad(g, WIDTH / 2, HEIGHT / 2 + 100, HEIGHT / 2 - 100, 80, 2, false, true); //południe
        drawRoad(g, 0, HEIGHT / 2, WIDTH / 2 - 80, 67, 3, true, true); // zachód
        drawRoad(g, WIDTH / 2 + 80, HEIGHT / 2, WIDTH / 2 - 80, 67, 3, true, false);  //wschód
        g.setColor(GRAY);
        g.fillRect(WIDTH / 2 - 80, HEIGHT / 2 - 100, 160, 201);

        // Rysowanie świateł
        long currentTime = System.currentTimeMillis();
        drawTrafficLight(g, WIDTH / 2 - 80, HEIGHT / 2 - 250, false, trafficLightStates[0], currentTime); // Północ
        drawTrafficLight(g, WIDTH / 2 + 80, HEIGHT / 2 + 250, false, trafficLightStates[1], currentTime); // Południe
        drawTrafficLight(g, WIDTH / 2 + 500, HEIGHT / 2 - 100, true, trafficLightStates[2], currentTime); // Wschód - prawo/prosto
        drawTrafficLight(g, WIDTH / 2 + 500, HEIGHT / 2 - 33, true, trafficLightStates[3], currentTime); // Wschód - lewo
        drawTrafficLight(g, WIDTH / 2 - 500, HEIGHT / 2 + 102, true, trafficLightStates[4], currentTime); // Zachód - prawo/prosto
        drawTrafficLight(g, WIDTH / 2 - 500, HEIGHT / 2 + 34, true, trafficLightStates[5], currentTime); // Zachód - lewo

        drawPedestrianLight(g,24,HEIGHT/2+120,pedestrianLightStates ,currentTime);
        drawPedestrianLight(g,195,HEIGHT/2-118,pedestrianLightStates ,currentTime);
        drawPedestrianLight(g,WIDTH-24,HEIGHT/2-118,pedestrianLightStates ,currentTime);
        drawPedestrianLight(g,WIDTH-195,HEIGHT/2+120,pedestrianLightStates ,currentTime);

        //Strzałka na północej drodze
        rotateArrow(g, WIDTH/2-40, HEIGHT/2-145, 40, 90);
        rotateArrow(g, WIDTH/2-40, HEIGHT/2-130, 20, 0);
        rotateArrow(g, WIDTH/2-40, HEIGHT/2-130, 20, 180);




        for (Car car : cars) {
            car.draw(g);
        }

        int[][] rects = {
                {WIDTH - 160, HEIGHT / 2 - 151, 100, 50},
                {WIDTH - 160, HEIGHT / 2 + 102, 100, 50},
                {60, HEIGHT / 2 - 151, 100, 50},
                {60, HEIGHT / 2 + 102, 100, 50}
        };



        Color graphiteColor = new Color(105, 105, 105);
        g.setColor(graphiteColor);
        for (int[] rect : rects) {
            g.fillRect(rect[0], rect[1], rect[2], rect[3]);
        }
        g.fillRect(WIDTH - 170,0,120,289);
        g.fillRect(WIDTH - 170,HEIGHT/2+102,120,289);
        g.fillRect(50,0,120,289);
        g.fillRect(50,HEIGHT/2+102,120,289);

        // Rysowanie osób (kropek)
        for (Person person : people) {
            person.draw(g);
        }




    }

    public void rotateArrow(Graphics g, int startX, int startY, int length, int rotate) {
        // Rzutowanie Graphics na Graphics2D
        Graphics2D g2d = (Graphics2D) g;

        // Ustawienie grubości linii
        g2d.setStroke(new BasicStroke(2)); // Grubość linii
        g2d.setColor(Color.WHITE);         // Kolor linii

        // Tworzymy macierz transformacji
        AffineTransform originalTransform = g2d.getTransform();

        // Obracamy rysowanie o zadany kąt
        g2d.rotate(Math.toRadians(rotate), startX, startY); // Obrót wokół punktu początkowego

        // Obliczanie końca strzałki
        int endX = startX + length;
        int endY = startY;

        // Rysowanie głównej linii strzałki
        g2d.drawLine(startX, startY, endX, endY);

        // Rysowanie grotu strzałki
        int arrowSize = 10; // Wielkość grotu
        int angle = 45; // Kąt grotu strzałki

        // Wyliczanie współrzędnych punktów grotu strzałki
        double rad = Math.toRadians(angle);

        // Lewa część grotu
        int leftX = (int) (endX - arrowSize * Math.cos(rad));
        int leftY = (int) (endY - arrowSize * Math.sin(rad));

        // Prawa część grotu
        int rightX = (int) (endX - arrowSize * Math.cos(-rad));
        int rightY = (int) (endY - arrowSize * Math.sin(-rad));

        // Rysowanie grotu
        g2d.drawLine(endX, endY, leftX, leftY);
        g2d.drawLine(endX, endY, rightX, rightY);

        // Przywracamy oryginalną transformację (w ten sposób zmiany nie będą dotyczyć innych rysunków)
        g2d.setTransform(originalTransform);
    }


    private void drawTrafficLight(Graphics g, int centerX, int centerY, boolean horizontal, int state, long currentTime) {
        int rectWidth = 24;
        int rectHeight = 45;
        int radius = 6;

        // Oblicz widoczność migającego żółtego światła
        boolean blinkOn = (currentTime / 500) % 2 == 0; // Co 500 ms zmienia stan (miganie)

        if (horizontal) {
            g.setColor(Color.BLACK);
            g.fillRect(centerX - rectHeight / 2, centerY - rectWidth / 2, rectHeight, rectWidth - 1);

            int redX = centerX - rectHeight / 2 + 2;
            int yellowX = redX + radius * 2 + 2;
            int greenX = yellowX + radius * 2 + 2;
            int lightY = centerY - radius;

            // Rysowanie stanu świateł
            if (state == 3) { // Awaria: tylko żółte miga
                if (blinkOn) {
                    g.setColor(Color.YELLOW);
                    g.fillOval(yellowX, lightY, radius * 2, radius * 2);
                }
            } else if (state == 2) { // Zielone
                g.setColor(Color.GREEN);
                g.fillOval(greenX, lightY, radius * 2, radius * 2);
            } else if (state == 1) { // Żółte
                g.setColor(Color.YELLOW);
                g.fillOval(yellowX, lightY, radius * 2, radius * 2);
            } else { // Czerwone
                g.setColor(Color.RED);
                g.fillOval(redX, lightY, radius * 2, radius * 2);
            }
        } else {
            g.setColor(Color.BLACK);
            g.fillRect(centerX - rectWidth / 2, centerY - rectHeight / 2, rectWidth, rectHeight - 1);

            int lightX = centerX - radius;
            int redY = centerY - rectHeight / 2 + 2;
            int yellowY = redY + radius * 2 + 2;
            int greenY = yellowY + radius * 2 + 2;

            // Rysowanie stanu świateł
            if (state == 3) { // Awaria: tylko żółte miga
                if (blinkOn) {
                    g.setColor(Color.YELLOW);
                    g.fillOval(lightX, yellowY, radius * 2, radius * 2);
                }
            } else if (state == 2) { // Zielone
                g.setColor(Color.GREEN);
                g.fillOval(lightX, greenY, radius * 2, radius * 2);
            } else if (state == 1) { // Żółte
                g.setColor(Color.YELLOW);
                g.fillOval(lightX, yellowY, radius * 2, radius * 2);
            } else { // Czerwone
                g.setColor(Color.RED);
                g.fillOval(lightX, redY, radius * 2, radius * 2);
            }
        }
    }

    private void drawPedestrianLight(Graphics g, int centerX, int centerY, int state, long currentTime) {
        int rectWidth = 20;
        int rectHeight = 34;
        int radius = 6;

        // Rysowanie prostokąta sygnalizatora
        g.setColor(Color.BLACK);
        g.fillRect(centerX - rectWidth / 2, centerY - rectHeight / 2, rectWidth, rectHeight);

        // Pozycje świateł
        int lightX = centerX - radius;
        int redY = centerY - rectHeight / 2 + 3;
        int greenY = centerY + rectHeight / 2 - radius * 2 - 3;

        // Oblicz widoczność migającego zielonego światła
        boolean blinkOn = (currentTime / 500) % 2 == 0; // Co 500 ms zmienia stan (miganie)

        // Rysowanie stanu świateł
        if (state == 2) { // Zielone
            g.setColor(Color.GREEN);
            g.fillOval(lightX, greenY, radius * 2, radius * 2);
        } else if (state == 1) { // Żółte (migające zielone)
            if (blinkOn) {
                g.setColor(Color.GREEN);
                g.fillOval(lightX, greenY, radius * 2, radius * 2);
            }
        } else { // Czerwone
            g.setColor(Color.RED);
            g.fillOval(lightX, redY, radius * 2, radius * 2);
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


}