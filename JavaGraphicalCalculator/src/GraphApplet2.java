import edu.awt.*;
import edu.data.*;
import edu.draw.*;

import javax.swing.*;
import java.applet.Applet;
import java.awt.*;

public class GraphApplet2 extends Applet {
    private DisplayCanvas canvas;

    public GraphApplet2() {
    }

    public static void main(String[] a) {
        JFrame f = new JFrame();
        Applet app = new GraphApplet2();
        app.init();
        f.getContentPane().add(app);
        f.pack();
        f.setSize(new Dimension(500, 500));
        f.setVisible(true);
    }

    public void stop() {
        this.canvas.releaseResources();
    }

    public void init() {
        Parser parser = new Parser();
        Variable x = new Variable("x");
        parser.add(x);
        this.canvas = new DisplayCanvas();
        this.canvas.setHandleMouseZooms(true);
        this.canvas.add(new Panner());
        CoordinateRect coords = this.canvas.getCoordinateRect();
        LimitControlPanel limits = new LimitControlPanel(33, false);
        limits.addCoords(this.canvas);
        ExpressionInput input = new ExpressionInput("sin(x)+2*cos(3*x)", parser);
        Function func = input.getFunction(x);
        Graph1D graph = new Graph1D(func);
        VariableInput xInput = new VariableInput();
        VariableSlider xSlider = new VariableSlider(coords.getValueObject(0), coords.getValueObject(1));
        DrawString info = new DrawString("x = #\nf(x) = #", 0, new Value[]{xSlider, new ValueMath(func, xSlider)});
        info.setFont(new Font("SansSerif", 1, 12));
        info.setColor(new Color(0, 100, 0));
        info.setOffset(10);
        ComputeButton graphIt = new ComputeButton("Graph It!");
        JCMPanel main = new JCMPanel();
        JCMPanel top = new JCMPanel();
        JCMPanel bottom = new JCMPanel();
        main.add(this.canvas, "Center");
        main.add(limits, "East");
        main.add(bottom, "South");
        main.add(top, "North");
        main.setInsetGap(3);
        top.add(input, "Center");
        top.add(new Label(" f(x) = "), "West");
        top.add(graphIt, "East");
        bottom.add(xSlider, "Center");
        bottom.add(xInput, "East");
        bottom.add(new Label("  x = "), "West");
        this.setLayout(new BorderLayout());
        this.add(main, "Center");
        this.setBackground(Color.lightGray);
        this.canvas.add(new Axes());
        this.canvas.add(graph);
        this.canvas.add(new Crosshair(xSlider, func));
        this.canvas.add(info);
        this.canvas.add(new DrawBorder(Color.darkGray, 2));
        main.gatherInputs();
        Controller controller = main.getController();
        graphIt.setOnUserAction(controller);
        coords.setOnChange(controller);
        controller.add(new Tie(xSlider, xInput));
    }
}
