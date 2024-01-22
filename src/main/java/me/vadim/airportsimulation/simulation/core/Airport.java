package me.vadim.airportsimulation.simulation.core;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import me.vadim.airportsimulation.drawer.AirportDrawer;
import me.vadim.airportsimulation.drawer.Drawer;
import me.vadim.airportsimulation.simulation.core.enums.FlightType;
import me.vadim.airportsimulation.simulation.core.enums.RunwayType;
import me.vadim.airportsimulation.simulation.core.helper.DurationUtils;
import me.vadim.airportsimulation.simulation.core.helper.Pair;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.LinkedList;

public class Airport {
    private AirportDrawer drawer;
    private final LinkedList<Pair<Double, Double>> runwaysCoords;
    private final HashSet<Airline> airlines;
    private final HashSet<Runway> runways;
    private final LinkedList<Flight> flights;
    private final int maxCountRunways;
    private final LocalDateTime startDateTime;
    private Duration durationOfLive;
    private int number = 1;
    public Airport(int maxCountRunways, LocalDateTime startDateTime) {
        this.airlines = new HashSet<>();
        this.runways = new HashSet<>();
        this.flights = new LinkedList<>();
        this.maxCountRunways = maxCountRunways;
        this.startDateTime = startDateTime;
        durationOfLive = Duration.ZERO;
        runwaysCoords = new LinkedList<>();
    }
    public void load() {
        runways.forEach(Runway::load);
        if(isDrawerSetup())
            drawer.setSource(this);
    }
    private boolean isDrawerSetup() {
        return drawer != null;
    }
    public boolean isFixit() {
        boolean res = true;
        for(var runway : runways) {
            res = res && runway.isFixit();
        }
        return (!isDrawerSetup() || drawer.isFixit()) && res;
    }
    private void setupRunwaysCords() {
        if(isDrawerSetup()) {
            runwaysCoords.clear();
            double width = drawer.getWidth()/(maxCountRunways+2);
            double height = drawer.getHeight()/2;
            double xOffset = (width * 2)/maxCountRunways;
            double yOffset = 5;
            double x = xOffset;
            for(int i = 0; i < maxCountRunways; i++) {
                runwaysCoords.add(new Pair<>(drawer.getX() + x, drawer.getY() + yOffset));
                x += width + xOffset;
            }
        }
    }
    private void balanceRunwayCords() {
        int i = 0;
        if(runwaysCoords.isEmpty())
            setupRunwaysCords();
        double width = drawer.getWidth()/(maxCountRunways+2);
        double height = drawer.getHeight()/2;
        for(var runway : runways) {
            if(!runway.isDrawerSetup()) {
                runway.setDrawer(drawer.getX(), drawer.getY(), width, height);
                runway.setSpeed(drawer.getSpeed());
            }
            var p = runwaysCoords.get(i++);
            runway.move(p.getFirst(), p.getSecond());
        }
    }
    public void setDrawer(double x, double y, double width, double height) {
        drawer = new AirportDrawer(this, x, y, width, height);
        setupRunwaysCords();
    }
    public void draw(GraphicsContext g) {
        if(isDrawerSetup()){
            drawer.draw(g);
        }
        runways.forEach(runway -> runway.draw(g));
        String info = "Длительность: %s".formatted(DurationUtils.toStringFormat("DD:HH:mm:ss", getLiveDuration()));
        double y = drawer.getY() + drawer.getHeight() - Drawer.computeTextHeight(g.getFont(), info) - 10;
        Drawer.drawTextOnBackground(g, drawer.getX() + drawer.getWidth() - Drawer.computeTextWidth(g.getFont(), info) + 70, y, Color.BLACK, Color.WHITE, info );
        info = "Дата: %s".formatted(getCurrentDateTime().format(DateTimeFormatter.ofPattern("HH:mm:ss\ndd.MM.yyyy")));
        y -= Drawer.computeTextHeight(g.getFont(), info) - 10;
        Drawer.drawTextOnBackground(g, drawer.getX() + drawer.getWidth() - Drawer.computeTextWidth(g.getFont(), info), y, Color.BLACK, Color.WHITE, info );
    }
    public void drawTooltip(GraphicsContext g) {
        if(isDrawerSetup()){
            drawer.drawTooltip(g);
        }
        runways.forEach(runway -> runway.drawTooltip(g));
    }
    public boolean mouseMovedEvent(MouseEvent event) {
        drawer.drawTooltip = false;
        for(var runway : runways) {
            if(runway.mouseMovedEvent(event)) {
                return true;
            }
        }
        if(!isDrawerSetup())
            return false;
        return drawer.mouseMovedEvent(event);
    }
    public void checkTime() {
        LinkedList<Flight> toRem = new LinkedList<>();
        for(var flight : flights) {
            if(!flight.getDateTime().isAfter(getCurrentDateTime())) {
                var runway = getFreeOrMinRunway(flight.getType());
                runway.addFlight(flight);
                toRem.add(flight);
            }
        }
        toRem.forEach(flights::remove);
    }
    public void live(long secondsStep) {
        checkTime();
        runways.forEach(runway -> runway.live(secondsStep));
        durationOfLive = durationOfLive.plusSeconds(secondsStep);
    }
    public Runway getFreeOrMinRunway(FlightType type) {
        Runway res = getFreeRunway(type);
        return res == null ? getMinRunway(type) : res;
    }
    public Runway getFreeRunway(FlightType type) {
        RunwayType needRunwayType = type == FlightType.LANDING ? RunwayType.LANDING : RunwayType.TAKEOFF;
        for(var runway : runways) {
            if(runway.isFree() && (runway.getType() == needRunwayType || runway.getType() == RunwayType.BOTH)) {
                return runway;
            }
        }
        return null;
    }
    public Runway getMinRunway(FlightType type) {
        RunwayType needRunwayType = type == FlightType.LANDING ? RunwayType.LANDING : RunwayType.TAKEOFF;
        Duration minDur = null;
        Runway minRunway = null;
        for(var runway : runways) {
            if(runway.getType() == needRunwayType || runway.getType() == RunwayType.BOTH) {
                if(minRunway == null || minDur.compareTo(Duration.between(startDateTime.plus(durationOfLive), runway.getFreeTime())) > 0) {
                    minRunway = runway;
                    minDur = Duration.between(startDateTime.plus(durationOfLive), runway.getFreeTime());
                }
            }
        }
        return minRunway;
    }
    public Airline registerAirline(String name) {
        Airline airline = new Airline(name);
        if(airlines.add(airline))
            return airline;
        return null;
    }
    public boolean registerRunway(RunwayType type) {
        if(maxCountRunways <= runways.size())
            return false;
        boolean res = runways.add(new Runway(type));
        if(res)
            balanceRunwayCords();
        return res;
    }
    public Flight registerFlight(Airplane airplane, FlightType type, LocalDateTime dateTime, Duration timeOffset) {
        Flight flight = new Flight(number++, airplane, type, dateTime, timeOffset);
        airplane.setCurrentFlight(flight);
        if(flights.add(flight))
            return flight;
        return null;
    }
    public HashSet<Airline> getAirlines() {
        return airlines;
    }
    public HashSet<Runway> getRunways() {
        return runways;
    }
    public LinkedList<Flight> getFlights() {
        return flights;
    }
    public LocalDateTime getCurrentDateTime() {
        return startDateTime.plus(durationOfLive);
    }
    public Duration getLiveDuration() {
        return durationOfLive;
    }
    public int getTotalNumberOfCompletedFlights() {
        int number = 0;
        for(var runway : runways)
            number += runway.getTotalNumberOfCompletedFlights();
        return number;
    }
    public Duration getMinDelayTakeoff() {
        Duration minDur = null;
        Duration temp = null;
        for(var runway : runways) {
            temp = runway.getMinDelayTakeoff();
            if(temp == null)
                continue;
            if (minDur == null || minDur.compareTo(temp) > 0)
                minDur = temp;
        }
        return minDur;
    }
    public Duration getMaxDelayTakeoff() {
        Duration maxDur = null;
        Duration temp = null;
        for(var runway : runways) {
            temp = runway.getMaxDelayTakeoff();
            if(temp == null)
                continue;
            if (maxDur == null || maxDur.compareTo(temp) < 0)
                maxDur = temp;
        }
        return maxDur;
    }
    public Duration getAverageDelayTakeoff() {
        Duration avg = Duration.ZERO;
        for(var runway : runways) {
            var temp = runway.getAverageDelayTakeoff();
            if(temp != null)
                avg = avg.plus(temp);
        }
        if(runways.isEmpty())
            return avg;
        return avg.dividedBy(runways.size());
    }
    public int getMaxQueueLength() {
        int max = 0;
        for(var runway : runways) {
            max = Math.max(max, runway.getMaxQueueLength());
        }
        return max;
    }
    public int getMinQueueLength() {
        int min = 0;
        for(var runway : runways) {
            min = Math.min(min, runway.getMinQueueLength());
        }
        return min;
    }
    public int getAverageQueueLength() {
        int avg = 0;
        for(var runway : runways) {
            avg += runway.getAverageQueueLength();
        }
        if(runways.isEmpty())
            return 0;
        return avg/runways.size();
    }
    public Duration getAverageEmployment() {
        Duration avg = Duration.ZERO;
        for(var runway : runways) {
            avg = avg.plus(runway.getAverageEmployment());
        }
        if(runways.isEmpty())
            return avg;
        return avg.dividedBy(runways.size());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Аэропорт");
        sb.append("\nКол-во полос: %d".formatted(getRunways().size()));
        sb.append("\nОбщее количество обслуженных заявок: %d".formatted(getTotalNumberOfCompletedFlights()));
        sb.append("\nСредняя время занятости: %s".formatted(DurationUtils.toString(getAverageEmployment())));
        Duration maxDelayTakeoff = getMaxDelayTakeoff();
        if(maxDelayTakeoff != null)
            sb.append("\nМаксимальная задержка вылета: %s".formatted(DurationUtils.toString(maxDelayTakeoff)));
        Duration minDelayTakeoff = getMinDelayTakeoff();
        if(minDelayTakeoff != null)
            sb.append("\nМинимальная задержка вылета: %s".formatted(DurationUtils.toString(minDelayTakeoff)));
        sb.append("\nСредняя задержка вылета: %s".formatted(DurationUtils.toString(getAverageDelayTakeoff())));
        sb.append("\nМинимальная длина очереди: %d".formatted(getMinQueueLength()));
        sb.append("\nМаксимальная длина очереди: %d".formatted(getMaxQueueLength()));
        sb.append("\nСредняя длина очереди: %d".formatted(getAverageQueueLength()));
        return sb.toString();
    }

    public int getMaxRunwaysCount() {
        return maxCountRunways;
    }

    public void setSpeed(double value) {
        if(isDrawerSetup())
            drawer.setSpeed(value);
        runways.forEach(runway -> runway.setSpeed(value));
    }

    public LocalDateTime getStartDate() {
        return startDateTime;
    }

    public double getSpeed() {
        if(isDrawerSetup())
            return drawer.getSpeed();
        return 0;
    }
}
