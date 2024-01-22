package me.vadim.airportsimulation;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.util.converter.DateTimeStringConverter;
import me.vadim.airportsimulation.simulation.Controller;
import me.vadim.airportsimulation.simulation.core.helper.DurationUtils;
import me.vadim.airportsimulation.simulation.core.helper.ValueRange;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML
    private Button pauseButton;
    @FXML
    private Button startButton;
    @FXML
    private Button stopButton;
    @FXML
    private Button saveButton;
    @FXML
    private Button loadButton;
    @FXML
    private Canvas simulationCanvas;
    @FXML
    private TextField seedField;
    @FXML
    private TextField durationField;
    @FXML
    private TextField startTimeField;
    @FXML
    private TextField maxRunwaysCountField;
    @FXML
    private TextField fromField;
    @FXML
    private TextField toField;
    @FXML
    private Label speedSliderInfo;
    @FXML
    private Label stepSliderInfo;
    @FXML
    private Slider speedSlider;
    @FXML
    private Slider stepSlider;
    Controller simCont;
    boolean isPause;
    private void makeTextFieldsForOnlyNumber(TextField ... fields) {
        for(var field : fields) {
            field.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*")) {
                    field.setText(newValue.replaceAll("[^\\d]", ""));
                }
            });
        }
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        speedSlider.valueProperty().addListener((observableValue, number, t1) -> {
            speedSliderInfo.setText("%.2f".formatted(t1.doubleValue()));
            if(simCont != null)
                simCont.setSpeed(t1.doubleValue());
        });
        stepSlider.valueProperty().addListener((observableValue, number, t1) -> {
            stepSliderInfo.setText(DurationUtils.toString(Duration.ofSeconds(t1.longValue())));
            if(simCont != null)
                simCont.setSecondsStep(t1.longValue());
        });
        speedSliderInfo.setText("%.2f".formatted(speedSlider.getValue()));
        stepSliderInfo.setText(DurationUtils.toString(Duration.ofSeconds(stepSlider.valueProperty().longValue())));
        makeTextFieldsForOnlyNumber(seedField, durationField, maxRunwaysCountField, fromField, toField);
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        try {
            startTimeField.setTextFormatter(new TextFormatter<>(new DateTimeStringConverter(format), format.parse("00:00:00")));
        } catch (ParseException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK);
            alert.setTitle("Ошибка");
            alert.setHeaderText("Произошла ошибка");
            alert.show();
        }
    }
    private void disableControls(Control ... controls) {
        for(var control : controls)
            control.setDisable(true);
    }
    private void enableControls(Control ... controls) {
        for(var control : controls)
            control.setDisable(false);
    }
    private boolean isTextFieldsEmpty(TextField ... textFields) {
        for (var text : textFields){
            if(text.getText().isEmpty() || text.getText().isBlank())
                return true;
        }
        return false;
    }

    @FXML
    protected void onStartButtonClick() {
        if(isTextFieldsEmpty(seedField, durationField, startTimeField, maxRunwaysCountField, fromField, toField)) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "", ButtonType.OK);
            alert.setTitle("Внимание");
            alert.setHeaderText("Заполните поля");
            alert.show();
            return;
        }
        int seed = Integer.parseInt(seedField.getText());
        Duration durSimulation = Duration.ofDays(Long.parseLong(durationField.getText()));
        LocalDateTime localDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.parse(startTimeField.getText()));
        int maxC = Integer.parseInt(maxRunwaysCountField.getText());
        long min = Long.parseLong(fromField.getText());
        long max = Long.parseLong(fromField.getText());
        ValueRange<Long> vr = new ValueRange<>(Math.min(min, max), Math.max(min, max));
        simCont = new Controller(seed, durSimulation, localDateTime, maxC, vr, simulationCanvas);
        simCont.setSpeed(speedSlider.getValue());
        simCont.setSecondsStep(stepSlider.valueProperty().longValue());
        simCont.start();
        isPause = false;
        disableControls(seedField, durationField, startTimeField, maxRunwaysCountField, fromField, toField);
        startButton.setVisible(false);
        stopButton.setVisible(true);
        pauseButton.setVisible(true);
        saveButton.setVisible(true);
        loadButton.setVisible(false);

    }
    @FXML
    protected void onPauseButtonClick() {
        isPause = !isPause;
        if(isPause) {
            pauseButton.setText("Возобновить");
            simCont.pause();
        }
        else {
            pauseButton.setText("Пауза");
            simCont.resume();
        }
    }
    @FXML
    protected void onStopButtonClick() {
        startButton.setVisible(true);
        stopButton.setVisible(false);
        pauseButton.setVisible(false);
        saveButton.setVisible(false);
        loadButton.setVisible(true);
        simCont.stop();
        enableControls(seedField, durationField, startTimeField, maxRunwaysCountField, fromField, toField);
    }
    @FXML
    protected void onSaveButtonClick() {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Json files (*.json)", "*.json");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showSaveDialog(simulationCanvas.getScene().getWindow());
        if(file != null) {
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(file.getPath()));
                writer.write(Controller.serialize(simCont));
                writer.close();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setHeaderText("Что-то пошло не так!");
                alert.setTitle("Ошибка");
            }
        }
    }
    @FXML
    protected void onLoadButtonClick() {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Json files (*.json)", "*.json");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showOpenDialog(simulationCanvas.getScene().getWindow());
        if(file != null) {
            try {
                String json = new String(Files.readAllBytes(Paths.get(file.getPath())), StandardCharsets.UTF_8);
                simCont = Controller.deserialize(json);
                simCont.load(simulationCanvas);
                if(simCont.isPause())
                    onPauseButtonClick();
                seedField.setText("%d".formatted(simCont.getSeed()));
                durationField.setText("%d".formatted(simCont.getDuration().toDays()));
                startTimeField.setText("%s".formatted(simCont.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm:ss"))));
                maxRunwaysCountField.setText("%d".formatted(simCont.getMaxRunwaysCount()));
                fromField.setText("%d".formatted(simCont.getVR().getMin()));
                toField.setText("%d".formatted(simCont.getVR().getMax()));
                stepSlider.setValue(simCont.getStep());
                speedSlider.setValue(simCont.getSpeed());
                simCont.start();
                isPause = false;
                disableControls(seedField, durationField, startTimeField, maxRunwaysCountField, fromField, toField);
                startButton.setVisible(false);
                stopButton.setVisible(true);
                pauseButton.setVisible(true);
                saveButton.setVisible(true);
                loadButton.setVisible(false);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setHeaderText("Что-то пошло не так!");
                alert.setTitle("Ошибка");
            }
        }
    }

}