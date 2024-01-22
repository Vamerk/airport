package me.vadim.airportsimulation.simulation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import me.vadim.airportsimulation.simulation.core.Airline;
import me.vadim.airportsimulation.simulation.core.Airplane;
import me.vadim.airportsimulation.simulation.core.Airport;
import me.vadim.airportsimulation.simulation.core.Flight;
import me.vadim.airportsimulation.simulation.core.enums.AirplaneType;
import me.vadim.airportsimulation.simulation.core.enums.FlightType;
import me.vadim.airportsimulation.simulation.core.enums.RunwayType;
import me.vadim.airportsimulation.simulation.core.helper.Consts;
import me.vadim.airportsimulation.simulation.core.helper.ValueRange;
import me.vadim.airportsimulation.simulation.core.helper.adapters.ColorTypeAdapter;
import me.vadim.airportsimulation.simulation.core.helper.adapters.DurationTypeAdapter;
import me.vadim.airportsimulation.simulation.core.helper.adapters.LocalDateTimeTypeAdapter;
import me.vadim.airportsimulation.simulation.core.helper.adapters.ValueRangeTypeAdapter;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

public class Controller {
    private final static Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
            .registerTypeAdapter(Color.class, new ColorTypeAdapter())
            .registerTypeAdapter(ValueRange.class, new ValueRangeTypeAdapter())
            .create();
    private transient Random random;
    private final int seed;
    private final Duration durationSimulation;
    private long secondsStep;
    private final ValueRange<Long> offsetsRange;
    private final Airport airport;
    private boolean isPause;
    private boolean isGenerateRandomFlights;
    private transient Canvas canvas;
    private transient Timeline simulationTimeline;
    private double speed;
    public Controller(int seed, Duration durationSimulation, LocalDateTime startDate, int maxRunwaysCount, ValueRange<Long> offsetsRange, Canvas canvas) {
        this.seed = seed;
        random = new Random(seed);
        this.durationSimulation = durationSimulation;
        this.offsetsRange = offsetsRange;
        this.secondsStep = 5*60;
        airport = new Airport(maxRunwaysCount, startDate);
        isPause = false;
        airport.setDrawer(5, 5, canvas.getWidth() - 10, canvas.getHeight() - 10);
        registerRandomAirlines(random, airport);
        registerRandomRunways(random, airport);
        isGenerateRandomFlights = true;
        this.canvas = canvas;
        canvas.setOnMouseMoved(airport::mouseMovedEvent);
    }
    public void load(Canvas canvas) {
        this.canvas = canvas;
        setupTimeline();
        random = new Random(seed);
        airport.load();
        canvas.setOnMouseMoved(airport::mouseMovedEvent);
        airport.setSpeed(speed);
    }
    public void setupTimeline() {
        simulationTimeline = new Timeline(
                new KeyFrame(javafx.util.Duration.ONE, actionEvent -> airport.draw(canvas.getGraphicsContext2D())),
                new KeyFrame(javafx.util.Duration.ONE, actionEvent -> airport.drawTooltip(canvas.getGraphicsContext2D())),
                new KeyFrame(javafx.util.Duration.ONE, actionEvent -> clearBorder()),
                new KeyFrame(javafx.util.Duration.millis(20)),
                new KeyFrame(javafx.util.Duration.ZERO, actionEvent -> simulate())
        );
        simulationTimeline.setCycleCount(Animation.INDEFINITE);
    }
    public void setSpeed(double value) {
        speed = value;
        airport.setSpeed(value);
    }
    public void pause() {
        isPause = true;
    }
    public void resume() {
        isPause = false;
    }
    public boolean isPause() {
        return isPause;
    }
    public void setSecondsStep(long secondsStep) {
        this.secondsStep = secondsStep;
    }
    private void clearBorder() {
        var g = canvas.getGraphicsContext2D();
        g.clearRect(0, 0, canvas.getWidth(), 5);
        g.clearRect(0, 0, 5, canvas.getHeight());
        g.clearRect(canvas.getWidth()-5, 0, 5, canvas.getHeight());
        g.clearRect(0, canvas.getHeight()-5, canvas.getWidth(), 5);
    }
    public boolean isDone() {
        return durationSimulation.compareTo(airport.getLiveDuration()) <= 0;
    }
    private void simulate() {
        if(isPause)
            return;
        if(!isDone() && airport.isFixit()) {
            if(isGenerateRandomFlights)
                generateFlights();
            airport.live(secondsStep);
        }
    }
    public void start() {
        if(simulationTimeline == null)
            setupTimeline();
        simulationTimeline.play();
    }
    public void stop() {
        simulationTimeline.stop();
    }
    private void generateFlights() {
        LinkedList<Airplane> airplanes = new LinkedList<>();
        FlightType[] types = FlightType.values();
        while(random.nextFloat() >= 0.5f) {
            if(airplanes.isEmpty()) {
                airport.getAirlines().forEach(airline -> airplanes.addAll(airline.getAirplanes()));
            }
            if(airplanes.isEmpty())
                return;
            Airplane notBusyPlane = null;
            while(notBusyPlane == null || notBusyPlane.isFly()) {
                notBusyPlane = airplanes.get(random.nextInt(0, airplanes.size()));
            }
            FlightType rndType = types[random.nextInt(0, types.length)];
            Duration offset = Duration.ofSeconds((rndType == FlightType.LANDING && random.nextBoolean() ? -1 : 1) * offsetsRange.getRandomValueFromRange(random));
            airport.registerFlight(notBusyPlane, rndType, airport.getCurrentDateTime().plusSeconds(random.nextLong(offsetsRange.getMin(), 3600)), offset);
        }
    }
    public void registerRandomRunways(Random random, Airport airport) {
        int c = random.nextInt(2, airport.getMaxRunwaysCount());
        for(int i = 0; i < c; i++) {
            airport.registerRunway(RunwayType.BOTH);
        }
    }
    public void registerRandomAirlines(Random random, Airport airport) {
        int c = random.nextInt(5, Consts.airlines.length);
        HashSet<String> names = new HashSet<>();
        AirplaneType[] types = AirplaneType.values();
        for(int i = 0; i < c; i++) {
            String name = Consts.airlines[random.nextInt(0, Consts.airlines.length)];
            while(!names.add(name))
                name = Consts.airlines[random.nextInt(0, Consts.airlines.length)];
            Airline airline = airport.registerAirline(name);
            int c1 = random.nextInt(10, 50);
            for(int j = 0; j < c1; j++) {
                airline.registerAirplane(Consts.airplanes[random.nextInt(0, Consts.airplanes.length)], types[random.nextInt(0, types.length)]);
            }
        }
    }
    public String getReport() {
        return airport.toString();
    }
    public static String serialize(Controller simulationController) {
        return gson.toJson(simulationController);
    }
    public static Controller deserialize(String json) {
        return gson.fromJson(json, Controller.class);
    }

    public int getSeed() {
        return seed;
    }

    public Duration getDuration() {
        return durationSimulation;
    }

    public LocalDateTime getStartTime() {
        return airport.getStartDate();
    }

    public int getMaxRunwaysCount() {
        return airport.getMaxRunwaysCount();
    }

    public ValueRange<Long> getVR() {
        return offsetsRange;
    }

    public long getStep() {
        return secondsStep;
    }

    public double getSpeed() {
        return airport.getSpeed();
    }
}
