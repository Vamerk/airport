package me.vadim.airportsimulation.drawer;

import javafx.scene.paint.Color;
import me.vadim.airportsimulation.simulation.core.Airport;

public class AirportDrawer extends Drawer<Airport> {
    public AirportDrawer(Airport source, double x, double y, double width, double height) {
        super(source, x, y, width, height, Color.WHITE);
    }
}
