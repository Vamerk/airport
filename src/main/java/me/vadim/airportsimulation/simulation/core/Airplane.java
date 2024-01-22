package me.vadim.airportsimulation.simulation.core;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import me.vadim.airportsimulation.drawer.AirplaneDrawer;
import me.vadim.airportsimulation.simulation.core.enums.AirplaneType;

public class Airplane {
    private AirplaneDrawer drawer;
    private Airline airline;
    private final String model;
    private final AirplaneType type;
    private transient Flight currentFlight;

    public Airplane(String model, AirplaneType type) {
        this.model = model;
        this.type = type;
    }
    public boolean isDrawerSetup() {
        return drawer != null;
    }
    public boolean isFixit() {
        return (!isDrawerSetup() || drawer.isFixit());
    }
    public void setDrawer(double x, double y, double width, double height) {
        drawer = new AirplaneDrawer(this, x, y, width, height);
    }
    public void draw(GraphicsContext g) {
        if(isDrawerSetup()){
            drawer.draw(g);
        }
    }
    public void drawTooltip(GraphicsContext g) {
        if(isDrawerSetup()){
            drawer.drawTooltip(g);
        }
    }
    public void move(double x, double y) {
        if(drawer != null) {
            drawer.move(x, y);
        }
    }
    public boolean mouseMovedEvent(MouseEvent event) {
        if(!isDrawerSetup())
            return false;
        return drawer.mouseMovedEvent(event);
    }

    public Airline getAirline() {
        return airline;
    }

    public void setAirline(Airline airline) {
        this.airline = airline;
    }

    public AirplaneType getType() {
        return type;
    }

    public Flight getCurrentFlight() {
        return currentFlight;
    }

    public void setCurrentFlight(Flight currentFlight) {
        this.currentFlight = currentFlight;
    }
    public boolean isFly() {
        return currentFlight != null;
    }
    public String getModel() {
        return model;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("%s Самолёт %s".formatted(getType().getName(), getModel()));
        if(getAirline() != null) {
            sb.append("\nАвиакомпания: %s".formatted(getAirline().getName()));
        }
        if(getCurrentFlight() != null) {
            sb.append("\nТекущий рейс: %s".formatted(getCurrentFlight().toString()));
        }
        return sb.toString();
    }

    public void setSpeed(double value) {
        if(isDrawerSetup())
            drawer.setSpeed(value);
    }

    public boolean isOnCords(double x, double y) {
        return (drawer.getLastMove() != null && (drawer.getLastMove().getFirst() == x && drawer.getLastMove().getSecond() == y)) || (drawer.getX() == x && drawer.getY() == y);
    }

    public void clearMoves() {
        if(isDrawerSetup())
            drawer.clearMoves();
    }
}
