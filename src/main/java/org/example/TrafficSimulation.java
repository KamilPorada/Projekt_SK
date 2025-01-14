package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class TrafficSimulation extends JPanel {

    private static final int WIDTH = 1440;
    private static final int HEIGHT = 780;
    private final Color GRAY = new Color(169, 169, 169);

    private final Color GREEN = new Color(34, 139, 34);
    private final List<Car> cars = new ArrayList<>();
    private ArrayList<Person> people = new ArrayList<>();

    private int carCounter, personNumber;

    private final Random random = new Random();
    private final int[] trafficLightStates = new int[6]; // 0 - red, 1 - yellow, 2 - green, 3 - failure
    private int pedestrianLightStates = 2; // 0 - red, 1 - yellow, 2 - green,
    int[][] carTypeCounts = new int[12][5];
    List<Double>[] carTypeSpeeds = new ArrayList[5];
    int[] trafficLightsFailure = new int[6]; // 0: działa, 1: awaria
    int[][] pedestrianCounts = new int[4][2];
    private long startSimulationTime, starttrafficLightTime;
    private long simulationTime = 0, trafficLightTime = 0;
    private int actualTour = 1;

    int[][] lanesForTour = {
            {0, 1, 2, 3, 4, 5},
            {6, 7, 8, 9},
            {10, 11}
    };
    private Timer spawnTimer;

    public TrafficSimulation() {
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(GREEN);
        startSimulationTime = System.currentTimeMillis();
        starttrafficLightTime = System.currentTimeMillis();
        carCounter = 1;
        personNumber=1;

        for (int i = 0; i < carTypeSpeeds.length; i++) {
            carTypeSpeeds[i] = new ArrayList<>();
        }

        Timer updateTimer = new Timer(30, e -> {
            long currentTime = System.currentTimeMillis();

            simulationTime = (currentTime - startSimulationTime) / 1000;
            trafficLightTime = (currentTime - starttrafficLightTime) / 1000;
            if (trafficLightTime > 45) {
                starttrafficLightTime = System.currentTimeMillis();
                generateTrafficLightFailure();
                generateRandomPeople(false);
            }

            if (simulationTime > 135) {
                JOptionPane.showMessageDialog(null, "Symulacja zakończona!", "Koniec symulacji", JOptionPane.INFORMATION_MESSAGE);
                try {
                    saveSimulationResultsToCSV("simulation_results.csv");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                System.exit(0);
            }

            for (Car car : cars) {
                car.move(cars);
                if (car.hasFinished()) {
                    cars.remove(car);
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
            spawnTimer.setDelay((int) (timeBetweenCars * 1000));

            if (Arrays.stream(trafficLightStates).anyMatch(state -> state == 2)) {
                int randomCarType = getRandomCarType();
                Car newCar = new Car(chooseRandomPath(chosenLane), carCounter, randomCarType);
                cars.add(newCar);
                carCounter++;
                carTypeCounts[chosenLane][randomCarType]++;
                carTypeSpeeds[randomCarType].add(newCar.getSpeed());
            }
        });

        updateTimer.start();
        spawnTimer.start();
        generateRandomPeople(true);
    }

    private void saveSimulationResultsToCSV(String filePath) throws IOException {
        String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write("Symulacja " + timestamp + "\n");
            writer.write("Car Type Counts (Lane x Type):\n");
            for (int lane = 0; lane < carTypeCounts.length; lane++) {
                for (int type = 0; type < carTypeCounts[lane].length; type++) {
                    writer.write(carTypeCounts[lane][type] + (type == carTypeCounts[lane].length - 1 ? "\n" : ","));
                }
            }

            writer.write("Car Type Speeds (Type x Speeds):\n");
            for (int type = 0; type < carTypeSpeeds.length; type++) {
                writer.write("Type " + type + ":");
                for (double speed : carTypeSpeeds[type]) {
                    writer.write(speed + ",");
                }
                writer.write("\n");
            }

            writer.write("Traffic Lights Failures:\n");
            for (int i = 0; i < trafficLightsFailure.length; i++) {
                writer.write("Light " + i + ": " + trafficLightsFailure[i] + "\n");
            }

            writer.write("Pedestrian Counts (Rectangle x Gender):\n");
            for (int rect = 0; rect < pedestrianCounts.length; rect++) {
                writer.write("Rectangle " + rect + ": Women=" + pedestrianCounts[rect][0] + ", Men=" + pedestrianCounts[rect][1] + "\n");
            }

            writer.write("\n=========================================\n\n");
        }
    }

    private void moveAllPeople() {
        for (Person person : people) {
            person.move();
        }
    }

    private int poisson(Random random, double lambda) {
        double l = Math.exp(-lambda);
        double p = 1.0;
        int k = 0;

        do {
            k++;
            p *= random.nextDouble();
        } while (p > l);

        return k - 1;
    }

    private void generateRandomPeople(boolean isInitial) {
        Random random = new Random();
        double lambda = 20.0;
        int numberOfPeople = poisson(random, lambda);

        int[][] initialRects = {
                {WIDTH - 160, HEIGHT / 2 - 150, 100, 50},
                {WIDTH - 160, HEIGHT / 2 + 102, 100, 50},
                {60, HEIGHT / 2 - 150, 100, 50},
                {60, HEIGHT / 2 + 102, 100, 50}
        };

        int[][] normalRects = {
                {WIDTH - 160, 0, 100, 50},
                {WIDTH - 160, HEIGHT, 100, 50},
                {60, 0, 100, 50},
                {60, HEIGHT, 100, 50}
        };

        int[][] rects = isInitial ? initialRects : normalRects;

        for (int i = 0; i < numberOfPeople; i++) {
            int rectIndex = random.nextInt(rects.length);
            int rectX = rects[rectIndex][0];
            int rectY = rects[rectIndex][1];
            int rectWidth = rects[rectIndex][2];
            int rectHeight = rects[rectIndex][3];

            int x = rectX + random.nextInt(rectWidth);
            int y = rectY + random.nextInt(rectHeight);

            int gender;
            if (rectIndex == 0 || rectIndex == 1) {
                gender = random.nextDouble() < 0.7 ? 0 : 1;
            } else {
                gender = random.nextDouble() < 0.7 ? 1 : 0;
            }

            int direction = (rectIndex == 1 || rectIndex == 3) ? 0 : 1;

            Person person = new Person(x, y, gender, direction, personNumber);
            personNumber++;
            pedestrianCounts[rectIndex][gender]++;

            boolean collides;
            do {
                collides = false;
                for (Person existingPerson : people) {
                    if (person.intersects(existingPerson)) {
                        collides = true;
                        break;
                    }
                }
                if (collides) {
                    x = rectX + random.nextInt(rectWidth);
                    y = rectY + random.nextInt(rectHeight);
                    person = new Person(x, y, gender, direction, personNumber);
                    pedestrianCounts[rectIndex][gender]++;
                    personNumber++;
                }
            } while (collides);
            people.add(person);
        }
    }

    private int getRandomCarType() {
        double[] probabilities = {0.4, 0.25, 0.15, 0.1, 0.1};
        double rand = random.nextDouble();
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
            case 1 -> new int[]{20, 15, 5, 10, 10, 5};
            case 2 -> new int[]{50, 20, 10, 20};
            case 3 -> new int[]{70, 30};
            default -> throw new IllegalArgumentException("Nieprawidłowa wartość actualTour");
        };
    }

    private static int chooseLane(Random random, int[] lanes, int[] weights, int totalWeight) {
        int randomValue = random.nextInt(totalWeight);
        int cumulativeWeight = 0;

        for (int i = 0; i < lanes.length; i++) {
            cumulativeWeight += weights[i];
            if (randomValue < cumulativeWeight) {
                return lanes[i];
            }
        }
        return -1;
    }

    private static double generateExponentialTime(Random random, double lambda) {
        double u = random.nextDouble();
        return -Math.log(1 - u) / lambda;
    }

    private static double getLambdaForLane(int lane) {

        if(lane == 0 || lane==6 || lane==10) return 3.5;
        if(lane == 1 || lane == 4) return 1.5;
        if(lane==7 || lane==9) return 1;
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
            baseTrafficLights = new int[]{0, 0, 0, 0, 0, 0};
            actualTour = 1;
        }

        for (int i = 0; i < baseTrafficLights.length; i++) {
            if (trafficLightsFailure[i] == 1) {
                baseTrafficLights[i] = 3;
            }
        }
        setTrafficLights(baseTrafficLights);
    }

    private void generateTrafficLightFailure() {
        double failureProbability = 0.05;
        Random random = new Random();

        for (int i = 0; i < trafficLightsFailure.length; i++) {
            if (random.nextDouble() < failureProbability) {
                trafficLightsFailure[i] = 1;
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
                        // Pas północ-wschód
                        if(trafficLightsFailure[0]==0){
                            path.add(new LineSegment(new Point(WIDTH / 2 - 40, 0), new Point(WIDTH / 2 - 40, HEIGHT / 2)));
                            path.add(new ArcSegment(new Point(WIDTH / 2 - 40, HEIGHT / 2 - 70), new Point(WIDTH / 2 + 45, HEIGHT / 2 + 70), 180, 270));
                            path.add(new LineSegment(new Point(WIDTH / 2, HEIGHT / 2 + 70), new Point(WIDTH, HEIGHT / 2 + 70)));
                        }
                        break;
                    case 1:
                        // Pas północ-południe
                        if(trafficLightsFailure[0]==0) {
                            path.add(new LineSegment(new Point(WIDTH / 2 - 40, 0), new Point(WIDTH / 2 - 40, HEIGHT)));
                        }
                        break;
                    case 2:
                        // Pas północ-zachód
                        if(trafficLightsFailure[0]==0) {
                            path.add(new LineSegment(new Point(WIDTH / 2 - 40, 0), new Point(WIDTH / 2 - 40, HEIGHT / 2 - 100)));
                            path.add(new ArcSegment(new Point(WIDTH / 2 - 40, HEIGHT / 2 - 130), new Point(WIDTH / 2 - 120, HEIGHT / 2 - 68), 0, -90));
                            path.add(new LineSegment(new Point(WIDTH / 2 - 80, HEIGHT / 2 - 68), new Point(0, HEIGHT / 2 - 68)));
                        }
                        break;
                    case 3:
                        // Pas południe-wschód
                        if(trafficLightsFailure[1]==0) {
                            path.add(new LineSegment(new Point(WIDTH / 2 + 40, HEIGHT), new Point(WIDTH / 2 + 40, HEIGHT / 2 + 100)));
                            path.add(new ArcSegment(new Point(WIDTH / 2 + 40, HEIGHT / 2 + 130), new Point(WIDTH / 2 + 120, HEIGHT / 2 + 68), 180, 90));
                            path.add(new LineSegment(new Point(WIDTH / 2 + 80, HEIGHT / 2 + 68), new Point(WIDTH, HEIGHT / 2 + 68)));
                        }
                        break;
                    case 4:
                        // Pas południe-północ
                        if(trafficLightsFailure[1]==0) {
                            path.add(new LineSegment(new Point(WIDTH / 2 + 40, HEIGHT), new Point(WIDTH / 2 + 40, 0)));
                        }
                        break;
                    case 5:
                        // Pas południe-zachód
                        if(trafficLightsFailure[1]==0) {
                            path.add(new LineSegment(new Point(WIDTH / 2 + 40, HEIGHT), new Point(WIDTH / 2 + 40, HEIGHT / 2 - 34)));
                            path.add(new ArcSegment(new Point(WIDTH / 2 + 40, HEIGHT / 2), new Point(WIDTH / 2 - 200, HEIGHT / 2 - 68), 0, 90));
                            path.add(new LineSegment(new Point(WIDTH / 2 - 80, HEIGHT / 2 - 68), new Point(0, HEIGHT / 2 - 68)));
                        }
                        break;
                }
            }
        } else if (actualTour == 2) {
            if (trafficLightStates[2] == 2 || trafficLightStates[4] == 2) {
                switch (chosenLane) {
                    case 6:
                        // Pas wschód-północ
                        if(trafficLightsFailure[2]==0) {
                            path.add(new LineSegment(new Point(WIDTH, HEIGHT / 2 - 68), new Point(WIDTH / 2 + 60, HEIGHT / 2 - 68)));
                            path.add(new ArcSegment(new Point(WIDTH / 2 + 80, HEIGHT / 2 - 68), new Point(WIDTH / 2 + 40, HEIGHT / 2 - 100), -90, -180));
                            path.add(new LineSegment(new Point(WIDTH / 2 + 40, HEIGHT / 2 - 85), new Point(WIDTH / 2 + 40, 0)));
                        }
                        break;
                    case 7:
                        // Pas wschód-zachód
                        if(trafficLightsFailure[2]==0) {
                            path.add(new LineSegment(new Point(WIDTH, HEIGHT / 2 - 68), new Point(0, HEIGHT / 2 - 68)));
                        }
                        break;
                    case 8:
                        // Pas zachód-południe
                        if(trafficLightsFailure[4]==0) {
                            path.add(new LineSegment(new Point(0, HEIGHT / 2 + 68), new Point(WIDTH / 2 - 80, HEIGHT / 2 + 68)));
                            path.add(new ArcSegment(new Point(WIDTH / 2 - 120, HEIGHT / 2 + 68), new Point(WIDTH / 2 - 40, HEIGHT / 2 + 130), 90, 0));
                            path.add(new LineSegment(new Point(WIDTH / 2 - 40, HEIGHT / 2 + 100), new Point(WIDTH / 2 - 40, HEIGHT)));
                        }
                        break;
                    case 9:
                        // Pas zachód-wschód
                        if(trafficLightsFailure[4]==0) {
                            path.add(new LineSegment(new Point(0, HEIGHT / 2 + 68), new Point(WIDTH, HEIGHT / 2 + 68)));
                        }
                        break;
                }
            }
        } else if (actualTour == 3) {
            if (trafficLightStates[3] == 2 || trafficLightStates[5] == 2) {
                switch (chosenLane) {
                    case 10:
                        // Pas wschód-południe
                        if(trafficLightsFailure[3]==0) {
                            path.add(new LineSegment(new Point(WIDTH, HEIGHT / 2), new Point(WIDTH / 2, HEIGHT / 2)));
                            path.add(new ArcSegment(new Point(WIDTH / 2 + 40, HEIGHT / 2), new Point(WIDTH / 2 - 40, HEIGHT / 2 + 200), 90, 180));
                            path.add(new LineSegment(new Point(WIDTH / 2 - 40, HEIGHT / 2 + 100), new Point(WIDTH / 2 - 40, HEIGHT)));
                        }
                        break;
                    case 11:
                        // Pas zachód-północ
                        if(trafficLightsFailure[5]==0) {
                            path.add(new LineSegment(new Point(0, HEIGHT / 2), new Point(WIDTH / 2, HEIGHT / 2)));
                            path.add(new ArcSegment(new Point(WIDTH / 2 - 40, HEIGHT / 2), new Point(WIDTH / 2 + 40, HEIGHT / 2 - 200), 270, 360));
                            path.add(new LineSegment(new Point(WIDTH / 2 + 40, HEIGHT / 2 - 100), new Point(WIDTH / 2 + 40, 0)));
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

            g.setColor(Color.WHITE);
            g.drawLine(startX, startY, startX + length, startY);
            g.drawLine(startX, startY + roadWidth, startX + length, startY + roadWidth);

            for (int i = 1; i < laneCount; i++) {
                int y = startY + i * laneWidth;
                for (int x = startX; x < startX + length; x += 20) {
                    g.drawLine(x, y, x + 10, y);
                }
            }

            if (isDirectional) {
                int stopLineX = startX + length - 620;
                for (int y = startY + 70; y <= startY + roadWidth - 5; y += 20) {
                    g.drawLine(stopLineX, y, stopLineX, y + 10);
                }
            } else {
                int stopLineX = startX + 620;
                for (int y = startY; y <= startY + roadWidth - 70; y += 20) {
                    g.drawLine(stopLineX, y, stopLineX, y + 10);
                }
            }

        } else {
            startX = startX - roadWidth / 2;
            g.setColor(GRAY);
            g.fillRect(startX, startY, roadWidth, length);

            g.setColor(Color.WHITE);
            g.drawLine(startX, startY, startX, startY + length);
            g.drawLine(startX + roadWidth, startY, startX + roadWidth, startY + length);

            for (int i = 1; i < laneCount; i++) {
                int x = startX + i * laneWidth;
                for (int y = startY; y < startY + length; y += 20) {
                    g.drawLine(x, y, x, y + 10);
                }
            }

            if (isDirectional) {
                int stopLineY = startY + 270;
                for (int x = startX + 85; x <= startX + roadWidth - 5; x += 20) {
                    g.drawLine(x, stopLineY, x + 10, stopLineY);
                }
            } else {
                int stopLineY = startY + length - 270;
                for (int x = startX + 5; x <= startX + roadWidth - 85; x += 20) {
                    g.drawLine(x, stopLineY, x + 10, stopLineY);
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

        //Strzałka prawo, prosto na wschodniej drodze
        rotateArrow(g, WIDTH/2+120, HEIGHT/2-66, 40, 180);
        rotateArrow(g, WIDTH/2+105, HEIGHT/2-66, 20, 270);

        //Strzałka lewo na wschdniej drodze
        rotateArrow(g, WIDTH/2+90, HEIGHT/2, 20, 90);
        g.drawLine(WIDTH/2+90, HEIGHT/2,WIDTH/2+120,HEIGHT/2);

        //Strzałka na południowej drodze
        rotateArrow(g, WIDTH/2+40, HEIGHT/2+145, 40, 270);
        rotateArrow(g, WIDTH/2+40, HEIGHT/2+130, 20, 0);
        rotateArrow(g, WIDTH/2+40, HEIGHT/2+130, 20, 180);

        //Strzałka prawo, prosto na zachodniej drodze
        rotateArrow(g, WIDTH/2-120, HEIGHT/2+66, 40, 0);
        rotateArrow(g, WIDTH/2-105, HEIGHT/2+66, 20, 90);

        //Strzałka lewo na zachodniej drodze
        rotateArrow(g, WIDTH/2-90, HEIGHT/2, 20, 270);
        g.drawLine(WIDTH/2-90, HEIGHT/2,WIDTH/2-120,HEIGHT/2);

        int zebraStartX1 = 60;
        int zebraEndX1 = 160;
        int zebraStartX2 = WIDTH - 160;
        int zebraEndX2 = WIDTH - 60;
        int zebraStartY = HEIGHT / 2 - 100;
        int zebraEndY = HEIGHT / 2 + 100;
        int stripeWidth = 20;
        int stripeGap = 10;

        g.setColor(Color.WHITE);

        for (int y = zebraStartY; y < zebraEndY; y += stripeWidth + stripeGap) {
            g.fillRect(zebraStartX1, y, zebraEndX1 - zebraStartX1, stripeWidth);
        }

        for (int y = zebraStartY; y < zebraEndY; y += stripeWidth + stripeGap) {
            g.fillRect(zebraStartX2, y, zebraEndX2 - zebraStartX2, stripeWidth);
        }

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

        for (Person person : people) {
            person.draw(g);
        }

        int minutes = (int) (simulationTime / 60);
        int seconds = (int) (simulationTime % 60);

        String formattedTime = String.format("%02d:%02d", minutes, seconds);

        g.setFont(new Font("Arial", Font.PLAIN, 30));
        g.setColor(Color.WHITE);

        int x = 3 * WIDTH/4-70;
        int y = 20;
        int width = 140;
        int height = 50;

        g.setColor(Color.BLACK);
        g.fillRoundRect(x, y, width, height, 15, 15);

        g.setColor(Color.GRAY);
        g.drawRoundRect(x, y, width, height, 15, 15);

        g.setColor(Color.GREEN);
        g.drawString(formattedTime, x + (width - g.getFontMetrics().stringWidth(formattedTime)) / 2, y + height / 2 + 10);

        drawSign(g,"TOMASZÓW MAZOWIECKI 58",480,50, 0);
        drawSign(g,"BIAŁOBRZEGI 31",220,510, 90);
        drawSign(g,"RADOM 32",820,680, 0);
        drawSign(g,"PRZYSUCHA 18",1120,190, 270);
    }

    public void drawSign(Graphics g, String placeName, int xOffset, int yOffset, int angle) {
        g.setFont(new Font("Arial", Font.BOLD, 8));
        g.setColor(Color.WHITE);

        FontMetrics metrics = g.getFontMetrics();
        int textWidth = metrics.stringWidth(placeName);
        int textHeight = 5 * metrics.getHeight() / 2;

        int width = textWidth + 30;
        int height = textHeight + 10;

        int x = getWidth() - xOffset - width;
        int y = yOffset;

        int poleX = x + (width / 2) - 1;
        int poleY = y + height;
        int poleHeight = 50;

        Graphics2D g2d = (Graphics2D) g;
        g2d.rotate(Math.toRadians(angle), x + (width / 2), y + (height / 2) + poleHeight / 2);

        g.setColor(new Color(0, 100, 0));
        g.fillRoundRect(x, y, width, height, 20, 20);

        g.setColor(Color.WHITE);
        g.drawRoundRect(x, y, width, height, 20, 20);

        g.setColor(Color.WHITE);
        g.drawString(placeName, x + (width - textWidth) / 2, y + (height + textHeight) / 2 - 10);

        g.setColor(new Color(50, 50, 50));
        g.fillRect(poleX, poleY, 2, poleHeight);

        g2d.rotate(-Math.toRadians(angle), x + (width / 2), y + (height / 2) + poleHeight / 2);
    }

    public void rotateArrow(Graphics g, int startX, int startY, int length, int rotate) {
        Graphics2D g2d = (Graphics2D) g;

        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(Color.WHITE);

        AffineTransform originalTransform = g2d.getTransform();

        g2d.rotate(Math.toRadians(rotate), startX, startY);

        int endX = startX + length;
        int endY = startY;

        g2d.drawLine(startX, startY, endX, endY);

        int arrowSize = 10;
        int angle = 45;

        double rad = Math.toRadians(angle);

        int leftX = (int) (endX - arrowSize * Math.cos(rad));
        int leftY = (int) (endY - arrowSize * Math.sin(rad));

        int rightX = (int) (endX - arrowSize * Math.cos(-rad));
        int rightY = (int) (endY - arrowSize * Math.sin(-rad));

        g2d.drawLine(endX, endY, leftX, leftY);
        g2d.drawLine(endX, endY, rightX, rightY);

        g2d.setTransform(originalTransform);
    }

    private void drawTrafficLight(Graphics g, int centerX, int centerY, boolean horizontal, int state, long currentTime) {
        int rectWidth = 24;
        int rectHeight = 45;
        int radius = 6;

        boolean blinkOn = (currentTime / 500) % 2 == 0;

        if (horizontal) {
            g.setColor(Color.BLACK);
            g.fillRect(centerX - rectHeight / 2, centerY - rectWidth / 2, rectHeight, rectWidth - 1);

            int redX = centerX - rectHeight / 2 + 2;
            int yellowX = redX + radius * 2 + 2;
            int greenX = yellowX + radius * 2 + 2;
            int lightY = centerY - radius;

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

        g.setColor(Color.BLACK);
        g.fillRect(centerX - rectWidth / 2, centerY - rectHeight / 2, rectWidth, rectHeight);

        int lightX = centerX - radius;
        int redY = centerY - rectHeight / 2 + 3;
        int greenY = centerY + rectHeight / 2 - radius * 2 - 3;

        boolean blinkOn = (currentTime / 500) % 2 == 0; // Co 500 ms zmienia stan (miganie)

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
        JFrame frame = new JFrame("Symulacja ruchu drogowego");
        TrafficSimulation simulation = new TrafficSimulation();
        frame.add(simulation);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}