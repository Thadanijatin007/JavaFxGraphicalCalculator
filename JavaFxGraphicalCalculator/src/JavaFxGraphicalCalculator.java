import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.sql.*;

public class JavaFxGraphicalCalculator extends Application {
    //  Database credentials
    static final String USER = "jatin";
    static final String PASS = "g9xdbL5x8bGepSrJ";
    static final String DB_NAME = "JavaGraphicalCalculator";
    static final String DB_URL = "jdbc:sqlserver://JATIN-THADANI-PC\\MSSQLSERVER;database=" + DB_NAME + ";";
    public GridPane gridPane;
    TextField txtFunction = null;
    Button btnGenerateGraph = null;
    private Double XMIN = -10.0;
    private Double XMAX = 10.0;
    private Connection db_conn = null;
    private String FUNCTION = "";
    private Stage primaryStage;
    private XYChart.Series<Number, Number> series = null;
    private Scene scene = null;
    private LineChart<Number, Number> lineChart = null;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        try {
            gridPane = FXMLLoader.load(getClass().getResource("JavaFxGraphicalCalculator.fxml"));
            primaryStage.setTitle("JavaFx Graphical Calculator");

            this.connectDatabase();
            this.getDefaultValues();
            this.getControls();
            this.addListeners();
            this.setLineChart();
            this.setSeries();
            this.setScene();
            this.setData();

            this.primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void connectDatabase() {
        try {
            DriverManager.registerDriver(new com.microsoft.sqlserver.jdbc.SQLServerDriver());
            db_conn = DriverManager.getConnection(DB_URL, USER, PASS);

            if (db_conn != null) {
                System.out.println("Connected");
            } else {
                System.out.println("Error in sql connection");
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
            System.out.println("Error in sql connection : " + exception.getMessage());
        }
    }

    public String getDatabaseConfiguration(String key) {

        Statement stmt = null;
        String sql = "SELECT ConfigurationValue FROM Configuration WHERE ConfigurationKey = '" + key + "'";
        ResultSet rs;
        String value = "";

        try {
            stmt = db_conn.createStatement();
            rs = stmt.executeQuery(sql);
            rs.next();
            value = rs.getString("ConfigurationValue");
            System.out.println("Key : " + key + "\t\tValue : " + value);

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return value;
    }

    public void setDatabaseConfiguration(String key, String value) {

        String updateQuery = "UPDATE Configuration SET ConfigurationValue = '" + value + "' WHERE ConfigurationKey = '" + key + "' ;";
        PreparedStatement prepsInsertProduct = null;
        ResultSet resultSet = null;

        try {

            prepsInsertProduct = db_conn.prepareStatement(updateQuery, Statement.RETURN_GENERATED_KEYS);
            prepsInsertProduct.execute();
            resultSet = prepsInsertProduct.getGeneratedKeys();

            while (resultSet.next()) {
                System.out.println("Generated: " + resultSet.getString(1));
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void getDefaultValues() {
        FUNCTION = this.getDatabaseConfiguration("function");
        XMIN = Double.valueOf(this.getDatabaseConfiguration("XMIN"));
        XMAX = Double.valueOf(this.getDatabaseConfiguration("XMAX"));
    }

    public void getControls() {
        btnGenerateGraph = (Button) gridPane.getChildren().get(2);
        txtFunction = (TextField) gridPane.getChildren().get(1);
        txtFunction.setText(FUNCTION);
    }

    public void addListeners() {
        btnGenerateGraph.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                series.getData().clear();
                FUNCTION = txtFunction.getText();
                series.setName("Function : " + FUNCTION);
                setData();
                setDatabaseConfiguration("function",FUNCTION);
            }
        });

    }

    public void setLineChart() {
        try {
            lineChart = (LineChart) gridPane.getChildren().get(0);

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
        scene = new Scene(gridPane, 800, 600);
        primaryStage.setScene(scene);
    }

    private void setData() {
        for (Double cntr = XMIN; cntr <= XMAX; cntr++) {
            Double value = this.evealuateFunction(cntr);
            series.getData().add(new XYChart.Data<>(cntr, value));
        }
    }

    public Double evealuateFunction(Double value) {
        double result = 0.0;
        try {
            Expression expression = new ExpressionBuilder(FUNCTION)
                    .variables("x")
                    .build()
                    .setVariable("x", value);

            result = expression.evaluate();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return result;
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
