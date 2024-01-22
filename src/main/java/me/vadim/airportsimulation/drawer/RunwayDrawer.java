package me.vadim.airportsimulation.drawer;

import javafx.scene.paint.Color;
import me.vadim.airportsimulation.simulation.core.Runway;

public class RunwayDrawer extends Drawer<Runway> {
    public RunwayDrawer(Runway source, double x, double y, double width, double height) {
        super(source, x, y, width, height, Color.GRAY);
    }
}
