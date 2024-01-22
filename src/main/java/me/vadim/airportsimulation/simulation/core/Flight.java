package me.vadim.airportsimulation.simulation.core;

import me.vadim.airportsimulation.simulation.core.enums.FlightType;

import java.time.Duration;
import java.time.LocalDateTime;

public class Flight {
    private final int number;
    private final Airplane airplane;
    private final FlightType type;
    private final LocalDateTime dateTime;
    private final Duration timeOffset;
    private Duration liveDuration;
    private Duration waitingDuration;
    public Flight(int number, Airplane airplane, FlightType type, LocalDateTime dateTime, Duration timeOffset) {
        this.number = number;
        this.airplane = airplane;
        this.type = type;
        this.dateTime = dateTime;
        this.timeOffset = timeOffset;
        waitingDuration = Duration.ZERO;
        liveDuration = Duration.ZERO;
    }
    public void load() {
        getAirplane().setCurrentFlight(this);
    }
    public long live(long secondsStep) {
        liveDuration = liveDuration.plusSeconds(secondsStep);
        if(liveDuration.compareTo(airplane.getType().getDuration()) > 0) {
            long remainder = liveDuration.minus(airplane.getType().getDuration()).toSeconds();
            liveDuration = airplane.getType().getDuration();
            return remainder;
        }
        return 0;
    }
    public void waiting(long secondsStep) {
        waitingDuration = waitingDuration.plusSeconds(secondsStep);
    }
    public int getNumber() {
        return number;
    }
    public Airplane getAirplane() {
        return airplane;
    }
    public FlightType getType() {
        return type;
    }
    public LocalDateTime getDateTime() {
        return dateTime;
    }
    public Duration getTimeOffset() {
        return timeOffset;
    }
    public Duration getWaitingDuration() {
        return waitingDuration;
    }
    public LocalDateTime getFinalTime() {
        return dateTime.plus(timeOffset);
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Рейс №%d".formatted(getNumber()));
        sb.append("\nСамолёт: %s".formatted(airplane.getType()));
        if(airplane.getAirline() != null) {
            sb.append("\nАвиакомпания: %s".formatted(airplane.getAirline().getName()));
        }
        sb.append("\nТип: %s".formatted(type.getName()));
        sb.append("\nОжидаемое время: %s".formatted(getDateTime().toString()));
        return sb.toString();
    }

    public boolean isDone() {
        return liveDuration.compareTo(airplane.getType().getDuration()) == 0;
    }
}
