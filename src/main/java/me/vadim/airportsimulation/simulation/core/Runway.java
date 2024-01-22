package me.vadim.airportsimulation.simulation.core;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import me.vadim.airportsimulation.drawer.AirportDrawer;
import me.vadim.airportsimulation.drawer.RunwayDrawer;
import me.vadim.airportsimulation.simulation.core.enums.FlightType;
import me.vadim.airportsimulation.simulation.core.enums.RunwayType;
import me.vadim.airportsimulation.simulation.core.helper.DurationUtils;
import me.vadim.airportsimulation.simulation.core.helper.Pair;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;

public class Runway {
    private RunwayDrawer drawer;
    private Flight currentFlight;
    private final ArrayList<Flight> readyFlights;
    private final ArrayList<Flight> flights;
    private RunwayType type;
    private int maxQueueLength;
    private int minQueueLength;
    public Runway(RunwayType type) {
        this.type = type;
        flights = new ArrayList<>();
        readyFlights = new ArrayList<>();
        minQueueLength = Integer.MAX_VALUE;
        maxQueueLength = Integer.MIN_VALUE;
    }
    public void load() {
        flights.forEach(Flight::load);
        readyFlights.forEach(Flight::load);
        if(currentFlight != null)
            currentFlight.load();
        if(isDrawerSetup())
            drawer.setSource(this);
    }
    boolean isDrawerSetup() {
        return drawer != null;
    }
    public boolean isFixit() {
        boolean res = true;
        if(currentFlight != null)
            res = currentFlight.getAirplane().isFixit();
        for(var flight : flights) {
            res = res && flight.getAirplane().isFixit();
        }
        return (!isDrawerSetup() || drawer.isFixit()) && res;
    }
    private void balanceFlightCords() {
        if(isDrawerSetup())
            balanceFlightCords(drawer.getX(), drawer.getY());
    }
    private void balanceFlightCords(double newX, double newY) {
        if(isDrawerSetup()) {
            int i = 0;
            double width = drawer.getWidth() / 2;
            double x = newX + drawer.getWidth() / 2 - width / 2;
            double startY = newY + drawer.getHeight() + 10;
            for(var flight : flights) {
                flight.getAirplane().clearMoves();
                flight.getAirplane().move(x, startY);
                startY += width + 10;
            }
        }
    }
    public void setDrawer(double x, double y, double width, double height) {
        drawer = new RunwayDrawer(this, x, y, width, height);
    }
    public void move(double x, double y) {
        if(drawer != null) {
            drawer.move(x, y);
        }
        //balanceFlightCords(x, y);
    }
    public void draw(GraphicsContext g) {
        if(isDrawerSetup()){
            drawer.draw(g);
        }
        if(currentFlight != null)
            currentFlight.getAirplane().draw(g);
        flights.forEach(flight -> flight.getAirplane().draw(g));
        readyFlights.forEach(flight -> flight.getAirplane().draw(g));
    }
    public void drawTooltip(GraphicsContext g) {
        if(isDrawerSetup()){
            drawer.drawTooltip(g);
        }
        if(currentFlight != null)
            currentFlight.getAirplane().drawTooltip(g);
        flights.forEach(flight -> flight.getAirplane().drawTooltip(g));
        readyFlights.forEach(flight -> flight.getAirplane().drawTooltip(g));
    }
    public boolean mouseMovedEvent(MouseEvent event) {
        drawer.drawTooltip = false;
        if(currentFlight != null && currentFlight.getAirplane().mouseMovedEvent(event)) {
            return true;
        }
        for(var flight : flights) {
            if(flight.getAirplane().mouseMovedEvent(event)) {
                return true;
            }
        }
        for(var flight : readyFlights) {
            if(flight.getAirplane().mouseMovedEvent(event)) {
                return true;
            }
        }
        if(!isDrawerSetup())
            return false;
        return drawer.mouseMovedEvent(event);
    }
    public void live(long secondsStep) {
        double width = drawer.getWidth() / 2;
        double x = drawer.getX() + drawer.getWidth() / 2 - width/2;
        if(currentFlight == null) {
            if(flights.isEmpty())
                return;
            currentFlight = flights.removeFirst();
            balanceFlightCords();
            if(!currentFlight.getAirplane().isDrawerSetup()) {
                currentFlight.getAirplane().setDrawer(x, drawer.getY() + drawer.getHeight() * 2, width, width);
                currentFlight.getAirplane().setSpeed(drawer.getSpeed());
            }
            double y = drawer.getY() + drawer.getHeight()/ 2 - width;
            currentFlight.getAirplane().clearMoves();
            currentFlight.getAirplane().move(x, y);
        }
        long remainder = currentFlight.live(secondsStep);
        flights.forEach(flight -> flight.waiting(secondsStep - remainder));
        if(currentFlight.isDone() && remainder != 0) {
            readyFlights.add(currentFlight);
            currentFlight = null;
            readyFlights.getLast().getAirplane().clearMoves();
            readyFlights.getLast().getAirplane().move(x, -width);
            live(remainder);
        }
    }
    public void addFlight(Flight flight) {
        double width = drawer.getWidth() / 2;
        double x = drawer.getX() + drawer.getWidth() / 2 - width/2;
        if(!flight.getAirplane().isDrawerSetup()) {
            flight.getAirplane().setDrawer(x, drawer.getY() + drawer.getHeight() * 2, width, width);
            flight.getAirplane().setSpeed(drawer.getSpeed());
        }
        flight.getAirplane().move(x, drawer.getY()+drawer.getHeight() + 10 * (flights.size()+1) + (width) * flights.size());
        flights.add(flight);
        flights.sort(Comparator.comparing(Flight::getDateTime));
        minQueueLength = Math.min(minQueueLength, flights.size());
        maxQueueLength = Math.max(maxQueueLength, flights.size());
    }
    public Flight getCurrentFlight() {
        return currentFlight;
    }
    public void setCurrentFlight(Flight currentFlight) {
        this.currentFlight = currentFlight;
    }
    public RunwayType getType() {
        return type;
    }
    public void setType(RunwayType type) {
        this.type = type;
    }
    public LocalDateTime getFreeTime() {
        LocalDateTime prevStartDateTime;
        LocalDateTime prevEndDateTime = currentFlight.getFinalTime();
        for(var flight : flights) {
            if(flight.getDateTime().isBefore(prevEndDateTime)) {
                prevStartDateTime = prevEndDateTime;
                prevEndDateTime = prevStartDateTime.plus(flight.getAirplane().getType().getDuration());
            } else {
                prevEndDateTime = flight.getFinalTime();
            }
        }
        return prevEndDateTime;
    }
    public boolean isFree() {
        return currentFlight == null;
    }
    public int getTotalNumberOfCompletedFlights() {
        return readyFlights.size();
    }
    public Duration getMinDelayTakeoff() {
        Duration min = null;
        for(var flight : readyFlights) {
            if(flight.getType() == FlightType.TAKEOFF) {
                if(min == null || min.compareTo(flight.getTimeOffset()) > 0)
                    min = flight.getTimeOffset();
            }
        }
        return min;
    }
    public Duration getMaxDelayTakeoff() {
        Duration max = null;
        for(var flight : readyFlights) {
            if(flight.getType() == FlightType.TAKEOFF) {
                if(max == null || max.compareTo(flight.getTimeOffset()) < 0)
                    max = flight.getTimeOffset();
            }
        }
        return max;
    }
    public Duration getAverageDelayTakeoff() {
        var min = getMinDelayTakeoff();
        var max = getMaxDelayTakeoff();
        if(min == null || max == null)
            return null;
        return min.plus(max).dividedBy(2);
    }
    public int getMaxQueueLength() {
        return maxQueueLength;
    }
    public int getMinQueueLength() {
        return minQueueLength;
    }
    public int getAverageQueueLength() {
        return (maxQueueLength + minQueueLength) / 2;
    }
    public Duration getAverageEmployment() {
        Duration avg = Duration.ZERO;
        for(var flight : readyFlights) {
            avg = avg.plus(flight.getAirplane().getType().getDuration());
        }
        if(readyFlights.isEmpty())
            return avg;
        return avg.dividedBy(readyFlights.size());
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Полоса");
        if(getType() != null)
            sb.append("\nТип: %s".formatted(getType().getName()));
        if(getCurrentFlight() != null)
            sb.append("\nНа ней: %s".formatted(getCurrentFlight().toString()));
        else
            sb.append("\nСвободна");
        sb.append("\nОбщее количество обслуженных заявок: %d".formatted(getTotalNumberOfCompletedFlights()));
        sb.append("\nСредняя время занятости: %s".formatted(DurationUtils.toString(getAverageEmployment())));
        if(getType() != RunwayType.LANDING) {
            Duration maxDelayTakeoff = getMaxDelayTakeoff();
            if(maxDelayTakeoff != null)
                sb.append("\nМаксимальная задержка вылета: %s".formatted(DurationUtils.toString(maxDelayTakeoff)));
            Duration minDelayTakeoff = getMinDelayTakeoff();
            if(minDelayTakeoff != null)
                sb.append("\nМинимальная задержка вылета: %s".formatted(DurationUtils.toString(minDelayTakeoff)));
        }
        sb.append("\nМинимальная длина очереди: %d".formatted(getMinQueueLength()));
        sb.append("\nМаксимальная длина очереди: %d".formatted(getMaxQueueLength()));
        return sb.toString();
    }

    public void setSpeed(double value) {
        if(isDrawerSetup())
            drawer.setSpeed(value);
        flights.forEach(flight -> flight.getAirplane().setSpeed(value));
        readyFlights.forEach(flight -> flight.getAirplane().setSpeed(value));
        if(currentFlight != null)
            currentFlight.getAirplane().setSpeed(value);
    }
}
