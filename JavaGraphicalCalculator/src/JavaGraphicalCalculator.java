import edu.awt.Controller;
import edu.awt.ExpressionInput;
import edu.awt.JCMPanel;
import edu.data.Function;
import edu.data.Parser;
import edu.data.Variable;
import edu.draw.Axes;
import edu.draw.DisplayCanvas;
import edu.draw.Graph1D;
import edu.draw.LimitControlPanel;
import edu.draw.*;

import javax.swing.*;
import java.applet.Applet;
import java.awt.*;

public class JavaGraphicalCalculator extends Applet {

    public JavaGraphicalCalculator() {
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
        DisplayCanvas canvas = new DisplayCanvas();
        canvas.setUseOffscreenCanvas(false);
        canvas.setHandleMouseZooms(false);
        LimitControlPanel limits = new LimitControlPanel();
        limits.addCoords(canvas);

        ExpressionInput input = new ExpressionInput("sin(x)+2*cos(3*x)", parser);
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

    }

    public String getDatabaseConfiguration(String key) {
        return "0";
    }

    public void setDatabaseConfiguration(String key, String value) {
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
