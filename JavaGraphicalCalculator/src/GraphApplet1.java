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

import javax.swing.*;
import java.applet.Applet;
import java.awt.*;

public class GraphApplet1 extends Applet {
    public GraphApplet1() {
    }

    public static void main(String[] a) {
        JFrame f = new JFrame();
        Applet app = new GraphApplet1();
        app.init();
        f.getContentPane().add(app);
        f.pack();
        f.setSize(new Dimension(500, 500));
        f.setVisible(true);
    }

    public void init() {
        Parser parser = new Parser();
        Variable x = new Variable("x");
        parser.add(x);
        DisplayCanvas canvas = new DisplayCanvas();
        canvas.setUseOffscreenCanvas(false);
        canvas.setHandleMouseZooms(true);
        LimitControlPanel limits = new LimitControlPanel();
        limits.addCoords(canvas);
        ExpressionInput input = new ExpressionInput("sin(x)+2*cos(3*x)", parser);
        Function func = input.getFunction(x);
        Graph1D graph = new Graph1D(func);
        JCMPanel main = new JCMPanel();
        main.add(canvas, "Center");
        main.add(input, "South");
        main.add(limits, "East");
        main.setInsetGap(3);
        this.setLayout(new BorderLayout());
        this.add(main, "Center");
        this.setBackground(Color.lightGray);
        canvas.add(new Axes());
        canvas.add(graph);
        Controller controller = main.getController();
        controller.setErrorReporter(canvas);
        limits.setErrorReporter(canvas);
        main.gatherInputs();
    }
}
