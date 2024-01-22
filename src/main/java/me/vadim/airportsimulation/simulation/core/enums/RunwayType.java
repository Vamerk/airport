package me.vadim.airportsimulation.simulation.core.enums;

public enum RunwayType {
    LANDING("Посадка"), TAKEOFF("Взлёт"), BOTH("Посадка и взлёт");
    private final String name;
    RunwayType(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
}
