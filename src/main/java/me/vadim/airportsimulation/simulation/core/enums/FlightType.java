package me.vadim.airportsimulation.simulation.core.enums;

public enum FlightType {
    LANDING("Посадка"), TAKEOFF("Взлёт");
    private final String name;
    FlightType(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
}
