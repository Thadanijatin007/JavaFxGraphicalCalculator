import edu.awt.Controller;
import edu.awt.ExpressionInput;
import edu.awt.JCMPanel;
import edu.data.Function;
import edu.data.Parser;
import edu.data.Variable;
import edu.draw.*;

import javax.swing.*;
import java.applet.Applet;
import java.awt.*;
import java.sql.*;

public class JavaGraphicalCalculator extends Applet {

    //  Database credentials
    static final String USER = "jatin";
    static final String PASS = "g9xdbL5x8bGepSrJ";
    static final String DB_NAME = "JavaGraphicalCalculator";
    static final String DB_URL = "jdbc:sqlserver://JATIN-THADANI-PC\\MSSQLSERVER;database=" + DB_NAME + ";";

    Connection db_conn = null;

    public JavaGraphicalCalculator() {
        this.connectDatabase();
    }

    public static void main(String[] a) {

        JFrame frame = new JFrame("Java Graphical Calculator");
        Applet app = new JavaGraphicalCalculator();
        app.init();
        frame.getContentPane().add(app);
        frame.pack();
        frame.setSize(new Dimension(600, 600));
        frame.setVisible(true);
    }

    public void init() {

        Parser parser = new Parser();
        Variable x = new Variable("x");
        parser.add(x);

        CoordinateRect cr = this.getCoordinate();
        DisplayCanvas canvas = new DisplayCanvas(cr);
        canvas.setUseOffscreenCanvas(false);
        canvas.setHandleMouseZooms(false);
        LimitControlPanel limits = new LimitControlPanel();
        limits.addCoords(canvas);

        String function = this.getDatabaseConfiguration("function");
        ExpressionInput input = new ExpressionInput(function, parser);
        Function func = input.getFunction(x);
        Graph1D graph = new Graph1D(func);
        JCMPanel jcmPanel = new JCMPanel();
        jcmPanel.add(canvas, "Center");
        jcmPanel.add(input, "South");
        jcmPanel.add(limits, "East");
        jcmPanel.setInsetGap(5);

        this.setLayout(new BorderLayout());
        this.add(jcmPanel, "Center");
        this.setBackground(Color.lightGray);

        canvas.add(new Axes());
        canvas.add(graph);

        Controller controller = jcmPanel.getController();
        controller.setErrorReporter(canvas);
        limits.setErrorReporter(canvas);
        jcmPanel.gatherInputs();
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

    public CoordinateRect getCoordinate() {
        double xmin = Double.valueOf(getDatabaseConfiguration("xmin"));
        double xmax = Double.valueOf(getDatabaseConfiguration("xmax"));
        double ymin = Double.valueOf(getDatabaseConfiguration("ymin"));
        double ymax = Double.valueOf(getDatabaseConfiguration("ymax"));

        CoordinateRect dbcr = new CoordinateRect(xmin, xmax, ymin, ymax);
        return dbcr;
    }


} // End of class
