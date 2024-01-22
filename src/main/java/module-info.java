module me.vadim.airportsimulation {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.google.gson;
    opens me.vadim.airportsimulation to javafx.fxml;
    opens me.vadim.airportsimulation.simulation to com.google.gson;
    opens me.vadim.airportsimulation.simulation.core to com.google.gson;
    opens me.vadim.airportsimulation.simulation.core.enums to com.google.gson;
    opens me.vadim.airportsimulation.simulation.core.helper to com.google.gson;
    opens me.vadim.airportsimulation.drawer to com.google.gson;
    exports me.vadim.airportsimulation;
}