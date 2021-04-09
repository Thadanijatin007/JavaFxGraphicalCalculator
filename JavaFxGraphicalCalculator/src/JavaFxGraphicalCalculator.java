import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.concurrent.ScheduledExecutorService;

public class JavaFxGraphicalCalculator extends Application {
    private final int WINDOW_SIZE = 100;
    public GridPane root;
    private final String FUNCTION = "(x^3)-sin(x)";
    private final Integer XMIN = -10;
    private final Integer XMAX = 10;
    private final Integer YMIN = -10;
    private final Integer YMAX = 10;

    private Stage primaryStage;
    private NumberAxis xAxis = null;
    private NumberAxis yAxis = null;
    private XYChart.Series<Number, Number> series = null;
    private Scene scene = null;
    private LineChart<Number, Number> lineChart = null;
    private ScheduledExecutorService scheduledExecutorService;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        try {
            root  = FXMLLoader.load(getClass().getResource("JavaFxGraphicalCalculator.fxml"));
            primaryStage.setTitle("JavaFx Graphical Calculator");

            this.setAxis();
            this.setLineChart();
            this.setSeries();
            this.setScene();
            this.setData();

            scene = new Scene(root, 800, 600);
            primaryStage.setScene(scene);

            this.primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setAxis() {
        xAxis = new NumberAxis();
        yAxis = new NumberAxis();

        xAxis.setLabel("X");
        xAxis.setAnimated(true);
        yAxis.setLabel("Y");
        yAxis.setAnimated(true);
    }

    public void setLineChart() {
        try {
            lineChart = new LineChart<Number, Number>(xAxis, yAxis);
            lineChart.setTitle("JavaFx Graphical Calculator");
            lineChart.setAnimated(true);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void setSeries() {
        // Creating series
        series = new XYChart.Series<Number, Number>();
        series.setName("Function : " + FUNCTION);
        // add series to chart
        lineChart.getData().add(series);
    }

    private void setScene() {

        //scene = new Scene(lineChart, 800, 600);
        //primaryStage.setScene(scene);
    }

    private void setData() {
        this.evealuateFunction(0.5);
    }

    public void evealuateFunction(Double value) {
        double result;
        try {
            Expression expression = new ExpressionBuilder(FUNCTION)
                    .variables("x")
                    .build()
                    .setVariable("x", value);

            result = expression.evaluate();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void stop() {
        try {
            super.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //scheduledExecutorService.shutdownNow();
    }

}
