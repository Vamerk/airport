package me.vadim.airportsimulation.drawer;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import me.vadim.airportsimulation.MainApplication;
import me.vadim.airportsimulation.simulation.core.Airplane;


public class AirplaneDrawer extends Drawer<Airplane> {
    private static Image image = new Image(MainApplication.class.getResourceAsStream("img/airplane.png"));

    public AirplaneDrawer(Airplane source, double x, double y, double width, double height) {
        super(source, x, y, width, height, Color.AQUA);
    }

    @Override
    public void draw(GraphicsContext g) {
       // super.draw(g);
        g.drawImage(image, rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
        if(!moves.isEmpty()) {
            var pair = moves.getFirst();
            boolean isDelete = true;
            if(Math.abs(rectangle.getX() - pair.getFirst()) > 1) {
                rectangle.moveByX(getSpeed() * (pair.getFirst() - rectangle.getX()));
                isDelete = false;
            }
            else
                rectangle.setX(pair.getFirst());
            if(Math.abs(rectangle.getY() - pair.getSecond()) > 1) {
                rectangle.moveByY(getSpeed() * (pair.getSecond() - rectangle.getY()));
                isDelete = false;
            }
            else
                rectangle.setY(pair.getSecond());
            if(isDelete)
                moves.removeFirst();
        }
    }
}
