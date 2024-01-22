package me.vadim.airportsimulation.simulation.core;

import me.vadim.airportsimulation.simulation.core.enums.AirplaneType;

import java.util.HashSet;
import java.util.LinkedList;

public class Airline {
    private final String name;
    private final LinkedList<Airplane> airplanes;
    public Airline(String name) {
        this(name, new HashSet<>());
    }
    public Airline(String name, HashSet<Airplane> airplanes) {
        this.name = name;
        this.airplanes = new LinkedList<>();
        airplanes.forEach(airplane -> airplane.setAirline(this));
    }
    public boolean registerAirplane(String model, AirplaneType type) {
        return airplanes.add(new Airplane(model, type));
    }
    public LinkedList<Airplane> getAirplanes() {
        return airplanes;
    }

    public String getName() {
        return name;
    }
}
