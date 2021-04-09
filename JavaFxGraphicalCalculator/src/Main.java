import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class Main extends Application {

    final int WINDOW_SIZE = 10;
    //creating the line chart with two axis created above
    final LineChart<Number, Number> lineChart = null;

    String FUNCTION = "f(x^3)-sin(x)";
    Integer XMIN = -10;
    Integer XMAX = 10;
    Integer YMIN = -10;
    Integer YMAX = 10;

    //defining the axes
    NumberAxis xAxis = null;
    NumberAxis yAxis = null;

    //defining a series to display data
    XYChart.Series<Number, Number> series = null;
    Scene scene = null;
    private ScheduledExecutorService scheduledExecutorService;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        Parent root = FXMLLoader.load(getClass().getResource("JavaFxGraphicalCalculator.fxml"));
        primaryStage.setTitle("JavaFx Graphical Calculator");

        setAxis();
        setupLineChart();
        setupScene();
        evealuateFunction();
        setData();

        // set scene to the stage
        primaryStage.setScene(scene);
        // show the stage
        primaryStage.show();


    }

    public void setAxis() {
        xAxis = new NumberAxis();
        yAxis = new NumberAxis();

        xAxis.setLabel("X");
        xAxis.setAnimated(true);
        yAxis.setLabel("Y");
        yAxis.setAnimated(true);
    }

    public void setupLineChart() {
        LineChart<Number, Number> lineChart = new LineChart<Number, Number>(xAxis, yAxis);
        series = new XYChart.Series<Number, Number>();
        series.setName("Function : " + FUNCTION);

        // add series to chart
        lineChart.getData().add(series);

        lineChart.setTitle("Realtime JavaFX Charts");
        lineChart.setAnimated(true); // disable animations
    }

    public void setupScene() {
        // setup scene
        scene = new Scene(lineChart, 800, 600);
    }

    public void evealuateFunction() {
        //        Expression expression = new net.objecthunter.exp4j.ExpressionBuilder("sin(x)*sin(x)+cos(x)*cos(x)")
        //                .variables("x")
        //                .build()
        //                .setVariable("x", 0.5);
        //
        //        double result = expression.evaluate();
    }

    public void setData() {
        // put dummy data onto graph per second
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            // get a random integer between 0-10
            Integer random = ThreadLocalRandom.current().nextInt(10);

            // Update the chart
            Platform.runLater(() -> {
                // get current time
                Date now = new Date();
                // put random number with current time
                series.getData().add(new XYChart.Data<>(10, random));

                if (series.getData().size() > WINDOW_SIZE)
                    series.getData().remove(0);
            });
        }, 0, 1, TimeUnit.SECONDS);
    }


}
