package me.vadim.airportsimulation.simulation.core.enums;

import java.time.Duration;

public enum AirplaneType {
    PASSENGER("Пассажирский", Duration.ofMinutes(30)), CARGO("Грузовой", Duration.ofHours(1)), BUSINESS_JET("Бизнес джет", Duration.ofMinutes(5));
    private final Duration duration;
    private final String name;
    AirplaneType(String name, Duration duration) {
        this.name = name;
        this.duration = duration;
    }
    public Duration getDuration() {
        return duration;
    }
    public String getName() {
        return name;
    }
}
