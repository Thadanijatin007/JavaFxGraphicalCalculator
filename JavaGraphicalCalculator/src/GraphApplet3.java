import edu.awt.*;
import edu.data.*;
import edu.draw.*;

import javax.swing.*;
import java.applet.Applet;
import java.awt.*;

public class GraphApplet3 extends Applet {
    private DisplayCanvas canvas;

    public GraphApplet3() {
    }

    public static void main(String[] a) {
        JFrame f = new JFrame();
        Applet app = new GraphApplet3();
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
        Value yValue = new ValueMath(func, xSlider);
        DrawGeometric vLine = new DrawGeometric(0, xSlider, new Constant(0.0D), xSlider, yValue);
        DrawGeometric hLine = new DrawGeometric(0, new Constant(0.0D), yValue, xSlider, yValue);
        DrawGeometric point = new DrawGeometric(10, xSlider, yValue, 3, 3);
        vLine.setColor(Color.lightGray);
        hLine.setColor(Color.lightGray);
        point.setColor(Color.magenta);
        point.setFillColor(Color.magenta);
        DrawString info = new DrawString("x = #\nf(x) = #", 0, new Value[]{xSlider, yValue});
        info.setFont(new Font("SansSerif", 1, 12));
        info.setColor(new Color(0, 100, 0));
        info.setOffset(10);
        ComputeButton graphIt = new ComputeButton("Graph It!");
        this.setLayout(new BorderLayout(3, 3));
        this.setBackground(Color.lightGray);
        Panel top = new Panel();
        top.setLayout(new BorderLayout(3, 3));
        Panel bottom = new Panel();
        bottom.setLayout(new BorderLayout(3, 3));
        this.add(this.canvas, "Center");
        this.add(limits, "East");
        this.add(bottom, "South");
        this.add(top, "North");
        top.add(input, "Center");
        top.add(new Label(" f(x) = "), "West");
        top.add(graphIt, "East");
        bottom.add(xSlider, "Center");
        bottom.add(xInput, "East");
        bottom.add(new Label("  x = "), "West");
        this.canvas.add(new Axes());
        this.canvas.add(hLine);
        this.canvas.add(vLine);
        this.canvas.add(point);
        this.canvas.add(graph);
        this.canvas.add(info);
        this.canvas.add(new DrawBorder(Color.darkGray, 2));
        Controller cc = new Controller();
        xInput.setOnUserAction(cc);
        xSlider.setOnUserAction(cc);
        coords.setOnChange(cc);
        cc.add(new Tie(xSlider, xInput));
        cc.add(hLine);
        cc.add(vLine);
        cc.add(point);
        cc.add(info);
        cc.add(xInput);
        cc.add(xSlider);
        Controller gc = new Controller();
        input.setOnUserAction(gc);
        graphIt.setOnUserAction(gc);
        gc.add(input);
        gc.add(graph);
        gc.add(cc);
        gc.setErrorReporter(this.canvas);
        limits.setErrorReporter(this.canvas);
    }
}
