package org.example;

import org.example.Segment;

import java.awt.*;
import java.util.List;
import java.util.Random;

public class Car {
    private final List<Segment> path;
    private double currentPosition;
    private int carNumber;
    private int vehicleType;
    private double speed;
    private static final Random random = new Random();



    public Car(List<Segment> path, int carNumber, int vehicleType) {
        this.path = path;
        this.currentPosition = 0;
        this.carNumber = carNumber;
        this.vehicleType = vehicleType;
        this.speed = generateSpeed(vehicleType);

    }

    public int getType() {
        return this.vehicleType;
    }

    private double generateSpeed(int vehicleType) {
        double mean;  // Średnia prędkość
        double stdDev; // Odchylenie standardowe

        switch (vehicleType) {
            case 0: // Samochód osobowy
                mean = 50.0;
                stdDev = 10.0;
                break;
            case 1: // Autobus
                mean = 40.0;
                stdDev = 8.0;
                break;
            case 2: // Ciężarówka
                mean = 35.0;
                stdDev = 7.0;
                break;
            case 3: // Motocykl
                mean = 60.0;
                stdDev = 15.0;
                break;
            case 4: // SUV
                mean = 45.0;
                stdDev = 10.0;
                break;
            default: // Domyślnie
                mean = 50.0;
                stdDev = 10.0;
        }

        // Generowanie prędkości na podstawie rozkładu normalnego
        double generatedSpeed;
        do {
            generatedSpeed = mean + stdDev * random.nextGaussian();
        } while (generatedSpeed < 0); // Zapewniamy, że prędkość nie będzie ujemna

        return generatedSpeed;
    }

    public void move(List<Car> otherCars, int[] trafficLightStates) {


        // Sprawdź, czy przed samochodem znajduje się inny samochód na tym samym pasie
        Car carInFront = findCarInFront(otherCars);
        if (carInFront != null) {
            double distanceToCarInFront = carInFront.currentPosition - this.currentPosition;

            // Zwiększamy odległość do 30 jednostek
            double safeDistance = 50.0;

            if (distanceToCarInFront < safeDistance) {
                // Jeśli odległość do pojazdu z przodu jest mniejsza niż bezpieczna, zmniejsz prędkość
                double speedReductionFactor = (safeDistance - distanceToCarInFront) / safeDistance;
                speed = generateSpeed(vehicleType) * speedReductionFactor;  // Zmniejsz prędkość w zależności od odległości
            } else {
                // Jeśli nie ma przeszkód, poruszaj się z normalną prędkością
                speed = generateSpeed(vehicleType);
            }
        }

        // Poruszaj się zgodnie z aktualną prędkością
        currentPosition += speed * 0.1;
    }


    private Car findCarInFront(List<Car> otherCars) {
        Car carInFront = null;
        double minDistance = Double.MAX_VALUE;

        for (Car otherCar : otherCars) {
            if (otherCar == this) continue; // Ignoruj samego siebie

            if (otherCar.currentPosition > this.currentPosition) {
                double distance = otherCar.currentPosition - this.currentPosition;
                if (distance < minDistance) {
                    minDistance = distance;
                    carInFront = otherCar;
                }
            }
        }

        return carInFront; // Może zwrócić null, jeśli nie ma samochodu z przodu
    }





    public boolean hasFinished() {
        return currentPosition > getTotalPathLength();
    }

    public void draw(Graphics g) {
        Point position = getCarPosition(); // Pobieramy pozycję pojazd
        int vehicleSize; // Stała wielkość dla pojazdów (np. 40x40 dla kwadratów, 40 średnica dla okręgów)
        int wheelSize;

        // Tworzymy generator losowy

        switch (vehicleType) {
            case 0: // Samochód osobowy
                // Rysowanie centralnego kształtu - okrąg
                vehicleSize = 20;
                wheelSize = 10;
                g.setColor(Color.RED);
                g.fillOval(position.x - vehicleSize / 2, position.y - vehicleSize / 2, vehicleSize, vehicleSize); // Centralny okrąg (nadwozie)

                // Rysowanie kół (cztery koła w rogach okręgu)
                g.setColor(Color.BLACK);
                g.fillOval(position.x - vehicleSize / 2 - wheelSize / 2, position.y - vehicleSize / 2 - wheelSize / 2, wheelSize, wheelSize);  // Przednie lewe koło
                g.fillOval(position.x + vehicleSize / 2 - wheelSize / 2, position.y - vehicleSize / 2 - wheelSize / 2, wheelSize, wheelSize);  // Przednie prawe koło
                g.fillOval(position.x - vehicleSize / 2 - wheelSize / 2, position.y + vehicleSize / 2 - wheelSize / 2, wheelSize, wheelSize);  // Tylne lewe koło
                g.fillOval(position.x + vehicleSize / 2 - wheelSize / 2, position.y + vehicleSize / 2 - wheelSize / 2, wheelSize, wheelSize);  // Tylne prawe koło

                break;

            case 1: // Autobus
                // Rysowanie centralnego kształtu - kwadrat
                vehicleSize = 25;
                wheelSize = 10;
                g.setColor(Color.BLUE);
                g.fillRect(position.x - vehicleSize / 2, position.y - vehicleSize / 2, vehicleSize, vehicleSize); // Centralny kwadrat (nadwozie)

                // Rysowanie kół (cztery koła w rogach kwadratu)
                g.setColor(Color.BLACK);
                g.fillOval(position.x - vehicleSize / 2 - wheelSize / 2, position.y - vehicleSize / 2 - wheelSize / 2, wheelSize, wheelSize);  // Przednie lewe koło
                g.fillOval(position.x + vehicleSize / 2 - wheelSize / 2, position.y - vehicleSize / 2 - wheelSize / 2, wheelSize, wheelSize);  // Przednie prawe koło
                g.fillOval(position.x - vehicleSize / 2 - wheelSize / 2, position.y + vehicleSize / 2 - wheelSize / 2, wheelSize, wheelSize);  // Tylne lewe koło
                g.fillOval(position.x + vehicleSize / 2 - wheelSize / 2, position.y + vehicleSize / 2 - wheelSize / 2, wheelSize, wheelSize);  // Tylne prawe koło
                break;

            case 2: // Ciężarówka
                // Rysowanie centralnego kształtu - kwadrat
                vehicleSize = 30;
                wheelSize = 10;
                g.setColor(Color.GREEN);
                g.fillRect(position.x - vehicleSize / 2, position.y - vehicleSize / 2, vehicleSize, vehicleSize); // Centralny kwadrat (nadwozie)

                // Rysowanie kół (cztery koła w rogach kwadratu)
                g.setColor(Color.BLACK);
                g.fillOval(position.x - vehicleSize / 2 - wheelSize / 2, position.y - vehicleSize / 2 - wheelSize / 2, wheelSize, wheelSize);  // Przednie lewe koło
                g.fillOval(position.x + vehicleSize / 2 - wheelSize / 2, position.y - vehicleSize / 2 - wheelSize / 2, wheelSize, wheelSize);  // Przednie prawe koło
                g.fillOval(position.x - vehicleSize / 2 - wheelSize / 2, position.y + vehicleSize / 2 - wheelSize / 2, wheelSize, wheelSize);  // Tylne lewe koło
                g.fillOval(position.x + vehicleSize / 2 - wheelSize / 2, position.y + vehicleSize / 2 - wheelSize / 2, wheelSize, wheelSize);  // Tylne prawe koło
                break;

            case 3: // Motocykl
                // Rysowanie centralnego kształtu - okrąg
                vehicleSize = 15;
                wheelSize = 5;
                g.setColor(Color.ORANGE);
                g.fillOval(position.x - vehicleSize / 2, position.y - vehicleSize / 2, vehicleSize, vehicleSize); // Centralny okrąg (silnik)

                // Rysowanie kół (cztery koła w rogach okręgu)
                g.setColor(Color.BLACK);
                g.fillOval(position.x - vehicleSize / 2 - wheelSize / 2, position.y - vehicleSize / 2 - wheelSize / 2, wheelSize, wheelSize);  // Przednie lewe koło
                g.fillOval(position.x + vehicleSize / 2 - wheelSize / 2, position.y - vehicleSize / 2 - wheelSize / 2, wheelSize, wheelSize);  // Przednie prawe koło
                g.fillOval(position.x - vehicleSize / 2 - wheelSize / 2, position.y + vehicleSize / 2 - wheelSize / 2, wheelSize, wheelSize);  // Tylne lewe koło
                g.fillOval(position.x + vehicleSize / 2 - wheelSize / 2, position.y + vehicleSize / 2 - wheelSize / 2, wheelSize, wheelSize);  // Tylne prawe koło

                break;

            case 4: // SUV
                vehicleSize = 18;
                wheelSize = 7;
                // Rysowanie centralnego kształtu - kwadrat
                g.setColor(Color.DARK_GRAY);
                g.fillRect(position.x - vehicleSize / 2, position.y - vehicleSize / 2, vehicleSize, vehicleSize); // Centralny kwadrat (nadwozie)

                // Rysowanie kół (cztery koła w rogach kwadratu)
                g.setColor(Color.BLACK);
                g.fillOval(position.x - vehicleSize / 2 - wheelSize / 2, position.y - vehicleSize / 2 - wheelSize / 2, wheelSize, wheelSize);  // Przednie lewe koło
                g.fillOval(position.x + vehicleSize / 2 - wheelSize / 2, position.y - vehicleSize / 2 - wheelSize / 2, wheelSize, wheelSize);  // Przednie prawe koło
                g.fillOval(position.x - vehicleSize / 2 - wheelSize / 2, position.y + vehicleSize / 2 - wheelSize / 2, wheelSize, wheelSize);  // Tylne lewe koło
                g.fillOval(position.x + vehicleSize / 2 - wheelSize / 2, position.y + vehicleSize / 2 - wheelSize / 2, wheelSize, wheelSize);  // Tylne prawe koło
                break;

            default:
                g.setColor(Color.GRAY);
                g.fillOval(position.x - 5, position.y - 5, 10, 10); // Domyślny pojazd - kropka
                break;
        }

        // Ustalanie koloru numeru ID
        if (vehicleType == 0 || vehicleType==1) {
            g.setColor(Color.WHITE); // Biały tekst dla autobusu
        } else {
            g.setColor(Color.BLACK); // Czarny tekst dla pozostałych pojazdów
        }

        // Rysowanie numeru ID
        g.setFont(new Font("Arial", Font.BOLD, 10)); // Czcionka
        FontMetrics metrics = g.getFontMetrics();
        String idString = String.valueOf(carNumber);
        int textWidth = metrics.stringWidth(idString);
        int textHeight = metrics.getAscent() - metrics.getDescent();
        g.drawString(idString, position.x - textWidth / 2, position.y + textHeight / 2);       }


    private Point getCarPosition() {
        if (path.isEmpty()) {
            // Jeśli lista path jest pusta, zwróć domyślny punkt, np. nowy Punkt(0, 0)
            return new Point(0, 0);
        }

        double distanceTraveled = currentPosition;
        for (Segment segment : path) {
            if (distanceTraveled <= segment.getLength()) {
                return segment.getPointAt(distanceTraveled);
            }
            distanceTraveled -= segment.getLength();
        }

        // Jeśli cała trasa została pokonana, zwróć punkt na końcu ostatniego segmentu
        return path.get(path.size() - 1).getPointAt(0);
    }


    private double getTotalPathLength() {
        return path.stream().mapToDouble(Segment::getLength).sum();
    }
}

