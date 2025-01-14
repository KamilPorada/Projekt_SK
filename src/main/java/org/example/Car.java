package org.example;

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

    private double generateSpeed(int vehicleType) {
        double mean;
        double stdDev;

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
            case 4: // Quad
                mean = 45.0;
                stdDev = 10.0;
                break;
            default: // Domyślnie
                mean = 50.0;
                stdDev = 10.0;
        }

        double generatedSpeed;
        do {
            generatedSpeed = mean + stdDev * random.nextGaussian();
        } while (generatedSpeed < 0);

        return generatedSpeed;
    }

    public void move(List<Car> otherCars) {
        Car carInFront = findCarInFront(otherCars);
        if (carInFront != null) {
            double distanceToCarInFront = carInFront.currentPosition - this.currentPosition;
            double safeDistance = 50.0;

            if (distanceToCarInFront < safeDistance) {
                double speedReductionFactor = (safeDistance - distanceToCarInFront) / safeDistance;
                speed = generateSpeed(vehicleType) * speedReductionFactor;  // Zmniejsz prędkość w zależności od odległości
            } else {
                speed = generateSpeed(vehicleType);
            }
        }
        currentPosition += speed * 0.1;
    }

    private Car findCarInFront(List<Car> otherCars) {
        Car carInFront = null;
        double minDistance = Double.MAX_VALUE;

        for (Car otherCar : otherCars) {
            if (otherCar == this) continue;
            if (otherCar.currentPosition > this.currentPosition) {
                double distance = otherCar.currentPosition - this.currentPosition;
                if (distance < minDistance) {
                    minDistance = distance;
                    carInFront = otherCar;
                }
            }
        }
        return carInFront;
    }

    public boolean hasFinished() {
        return currentPosition > getTotalPathLength();
    }

    public void draw(Graphics g) {
        Point position = getCarPosition();
        int vehicleSize;
        int wheelSize;

        switch (vehicleType) {
            case 0: // Samochód osobowy
                vehicleSize = 20;
                wheelSize = 10;
                g.setColor(Color.RED);
                g.fillOval(position.x - vehicleSize / 2, position.y - vehicleSize / 2, vehicleSize, vehicleSize); // Centralny okrąg (nadwozie)

                g.setColor(Color.BLACK);
                g.fillOval(position.x - vehicleSize / 2 - wheelSize / 2, position.y - vehicleSize / 2 - wheelSize / 2, wheelSize, wheelSize);  // Przednie lewe koło
                g.fillOval(position.x + vehicleSize / 2 - wheelSize / 2, position.y - vehicleSize / 2 - wheelSize / 2, wheelSize, wheelSize);  // Przednie prawe koło
                g.fillOval(position.x - vehicleSize / 2 - wheelSize / 2, position.y + vehicleSize / 2 - wheelSize / 2, wheelSize, wheelSize);  // Tylne lewe koło
                g.fillOval(position.x + vehicleSize / 2 - wheelSize / 2, position.y + vehicleSize / 2 - wheelSize / 2, wheelSize, wheelSize);  // Tylne prawe koło

                break;

            case 1: // Autobus
                vehicleSize = 25;
                wheelSize = 10;
                g.setColor(Color.BLUE);
                g.fillRect(position.x - vehicleSize / 2, position.y - vehicleSize / 2, vehicleSize, vehicleSize); // Centralny kwadrat (nadwozie)

                g.setColor(Color.BLACK);
                g.fillOval(position.x - vehicleSize / 2 - wheelSize / 2, position.y - vehicleSize / 2 - wheelSize / 2, wheelSize, wheelSize);  // Przednie lewe koło
                g.fillOval(position.x + vehicleSize / 2 - wheelSize / 2, position.y - vehicleSize / 2 - wheelSize / 2, wheelSize, wheelSize);  // Przednie prawe koło
                g.fillOval(position.x - vehicleSize / 2 - wheelSize / 2, position.y + vehicleSize / 2 - wheelSize / 2, wheelSize, wheelSize);  // Tylne lewe koło
                g.fillOval(position.x + vehicleSize / 2 - wheelSize / 2, position.y + vehicleSize / 2 - wheelSize / 2, wheelSize, wheelSize);  // Tylne prawe koło
                break;

            case 2: // Ciężarówka
                vehicleSize = 30;
                wheelSize = 10;
                g.setColor(Color.GREEN);
                g.fillRect(position.x - vehicleSize / 2, position.y - vehicleSize / 2, vehicleSize, vehicleSize); // Centralny kwadrat (nadwozie)

                g.setColor(Color.BLACK);
                g.fillOval(position.x - vehicleSize / 2 - wheelSize / 2, position.y - vehicleSize / 2 - wheelSize / 2, wheelSize, wheelSize);  // Przednie lewe koło
                g.fillOval(position.x + vehicleSize / 2 - wheelSize / 2, position.y - vehicleSize / 2 - wheelSize / 2, wheelSize, wheelSize);  // Przednie prawe koło
                g.fillOval(position.x - vehicleSize / 2 - wheelSize / 2, position.y + vehicleSize / 2 - wheelSize / 2, wheelSize, wheelSize);  // Tylne lewe koło
                g.fillOval(position.x + vehicleSize / 2 - wheelSize / 2, position.y + vehicleSize / 2 - wheelSize / 2, wheelSize, wheelSize);  // Tylne prawe koło
                break;

            case 3: // Motocykl
                vehicleSize = 15;
                wheelSize = 5;
                g.setColor(Color.ORANGE);
                g.fillOval(position.x - vehicleSize / 2, position.y - vehicleSize / 2, vehicleSize, vehicleSize); // Centralny okrąg (silnik)

                g.setColor(Color.BLACK);
                g.fillOval(position.x - vehicleSize / 2 - wheelSize / 2, position.y - vehicleSize / 2 - wheelSize / 2, wheelSize, wheelSize);  // Przednie lewe koło
                g.fillOval(position.x + vehicleSize / 2 - wheelSize / 2, position.y - vehicleSize / 2 - wheelSize / 2, wheelSize, wheelSize);  // Przednie prawe koło
                g.fillOval(position.x - vehicleSize / 2 - wheelSize / 2, position.y + vehicleSize / 2 - wheelSize / 2, wheelSize, wheelSize);  // Tylne lewe koło
                g.fillOval(position.x + vehicleSize / 2 - wheelSize / 2, position.y + vehicleSize / 2 - wheelSize / 2, wheelSize, wheelSize);  // Tylne prawe koło

                break;

            case 4: // Quad
                vehicleSize = 18;
                wheelSize = 7;
                g.setColor(Color.DARK_GRAY);
                g.fillRect(position.x - vehicleSize / 2, position.y - vehicleSize / 2, vehicleSize, vehicleSize); // Centralny kwadrat (nadwozie)

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

        if (vehicleType == 0 || vehicleType==1 || vehicleType == 4) {
            g.setColor(Color.WHITE);
        } else {
            g.setColor(Color.BLACK);
        }

        g.setFont(new Font("Arial", Font.BOLD, 10));
        FontMetrics metrics = g.getFontMetrics();
        String idString = String.valueOf(carNumber);
        int textWidth = metrics.stringWidth(idString);
        int textHeight = metrics.getAscent() - metrics.getDescent();
        g.drawString(idString, position.x - textWidth / 2, position.y + textHeight / 2);       }


    private Point getCarPosition() {
        if (path.isEmpty()) {
            return new Point(0, 0);
        }

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

    public double getSpeed(){
        return speed;
    }
}

//    Pozwól że teraz opisze ci cała symulacje i ty na to napiszesz okej ze zrozumialem a potem w kolejnych promptach bede ci wyznaczal jakie masz napisac podrozi=działy. Wykonana symulacja to symulacja ruchu drogowego jest ona napisana w czystej javie w ide inteliij idea. Jak sama nazwa wskazuje symuluje ona ruch drogowy. Symulowana sytuacja to skrzyżowanie które przecina dwie trasy: Przysucha - Białobrzegi oraz Tomaszów Mazowiecki - Radom. Znajduja sie tam 6 świateł dla samochodów. Trasa z Tomaszowa do Radomia ma po jednym świetle. Jest to tradycyjna droga zawierająca pas prawy z którego samochody mogą jechać w prawo prosto w lewo oraz pas lewy czyli odjazdowy analogicznie Trasa z Radomia do oTomaszowa. Trasy Przysucha - Białobrzegi mają po dwa światła co za tym idzie po dwa prawe pasy tj. prawy pas służy do sk®etu prawo i prosto a lewy do skretu w lewo oraz 3 pas typowo lewy no to pas odjazdowy. Dodatkowo symulacja przewiduje dwa prześcia dla piszych na trasie biegącej z PRzysuchy w stronr Białobrzegów. Symulacja zakłada istninie 5 rodzajów samochodów: osobowych, ciężarowych(tir), autobusów, motocyklów oraz Ōuadów. Czerwony kropka oznacza osobowy niebieska autobus zielony tir zolty motocylk ciemno szary quad. KAzdy samochod na srodku kropki z kolkami (wizualiacja pojazdu) ma napisany swoj numer w kolejnosci generowania. Analogicznie piesi sa to mniejsze niebieskie(mezczyzni) oraz rozowe (kobiety) kropki oni tez maja swoje numery w kolejnosci generowania. Symulacja rozpoczyna sie po uruchomieniu aplikacji otwiera sie okno z animacja symulacji. Trwa ona 135 sekund czyli 3 pełne zmiany wsyztkich świateł. Zapomniałem wcześniej dodac ze piesi tez maja swoje swiatla czyli w sumie jest jeszcze 4 swiatla dla piszych po dwa dla kazdego przejscia. Po zakonczeniu symulacji wszytskie wyniki sa zapisywane do pliku csv. tzn. Liczba oraz typ sdamochodow na konkretnej trasie, srednia predkosc kazdego typu samochodu, liczba awarii swiatel na danym pasie oraz liczba oraz plec pieszych na danym przejsciu. Za kazdym razem przy zmianie swiatel jest szanasa 5% na wystapienie awarii danego swiatla na danym pasie wowczas pas jest wylaczony z ruchu i samochody nie jezdza tamtędy. Losowanie pasa jest poprzez ustalone wczesniej wagi im wieksza waga dal danego pasa tym wieksza szansa na wylosowanie danego pasa podczas danej tury swiatel. Czas pomiedzy generwoaniem kolejnych pojazdow okreslany jest za pomoca rozkladu wykladniczego, natomiast losowanie typu pojazdu okresla sie za pomoca prawdopodobienstw ustalonych w kodzie. Liczba pieszych na danym przejsciu losowana jest za pomoca rozkladu poissona natomiast plec jest zalezna od przejscia oraz prawdopodobienstwa wystapienia danej plci na danym przejsciu. Predkosc samochodow jest generwoana za pomoca rozkaldu normalnego opartego na sredniej i odchyleniu standardowym. Po zakonczeniu symulacji mozna uruchomic druga aplikacje rowniez w javie ktora dokonuje analizy wynikow symulacji. Mozna analizowac kontrtna symulacje oraz zbiorczo wsyztkie symulacje wuybierajac odpowiedni radobutton. Analiza przedstawia tabelę liczby pojazdów w zaleśności od trasy i typu samochodu analogiczny wykres łupkowy przedstawiający tę zależność, tabele ze śrenimi preskociami samochodow w zaleznosci od jego typu, liczbe awarii swiatel w zaleznosci od trasy, rozklad plci piszych jako diagram kolowy oraz wykres slu[okwy przedstaiajacy strukture plci w zaleznosci od przejscia. Aplikacje sa w javie, ui w swing a wizualizacja danych jest dostepna przy uzyiu jfree chart