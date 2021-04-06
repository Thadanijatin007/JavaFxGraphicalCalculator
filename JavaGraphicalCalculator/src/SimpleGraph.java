import edu.awt.*;
import edu.data.*;
import edu.draw.CoordinateRect;
import edu.draw.DrawGeometric;
import edu.draw.Graph1D;
import edu.functions.WrapperFunction;

import javax.swing.*;
import java.applet.Applet;
import java.awt.*;
import java.util.StringTokenizer;

public class SimpleGraph extends GenericGraphApplet {
    private VariableInput xInput;
    private Function func;
    private Graph1D graph;
    private DrawGeometric point;
    private DrawGeometric vLine;
    private DrawGeometric hLine;

    public SimpleGraph() {
    }

    public static void main(String[] a) {
        JFrame f = new JFrame();
        Applet app = new SimpleGraph();
        app.init();
        f.getContentPane().add(app);
        f.pack();
        f.setSize(new Dimension(500, 500));
        f.setVisible(true);
    }

    protected void setUpCanvas() {
        super.setUpCanvas();
        if (this.functionInput != null) {
            this.func = this.functionInput.getFunction(this.xVar);
        } else {
            String def = this.getParameter("Function", " abs(" + this.xVar.getName() + ") ^ " + this.xVar.getName());
            Function f = new SimpleFunction(this.parser.parse(def), this.xVar);
            this.func = new WrapperFunction(f);
        }

        this.graph = new Graph1D(this.func);
        Color color = this.getColorParam("GraphColor");
        if (color != null) {
            this.graph.setColor(color);
        }

        if (!"no".equalsIgnoreCase(this.getParameter("ShowPoint", "yes"))) {
            this.vLine = new DrawGeometric();
            this.hLine = new DrawGeometric();
            this.point = new DrawGeometric();
            this.canvas.add(this.vLine);
            this.canvas.add(this.hLine);
            this.canvas.add(this.point);
        }

        this.canvas.add(this.graph);
    }

    protected void setUpMainPanel() {
        super.setUpMainPanel();
        if (!"no".equalsIgnoreCase(this.getParameter("ShowPoint", "yes"))) {
            this.xInput = new VariableInput();
            this.xInput.setInputStyle(1);
            CoordinateRect coords = this.canvas.getCoordinateRect();
            VariableSlider xSlider = new VariableSlider(coords.getValueObject(0), coords.getValueObject(1));
            Value yValue = new ValueMath(this.func, xSlider);
            DisplayLabel yDisplay = new DisplayLabel(" y = #", yValue);
            JCMPanel panel = new JCMPanel(1, 3);
            panel.setBackground(this.getColorParam("PanelBackground", Color.lightGray));
            JCMPanel subpanel = new JCMPanel();
            String varName = this.getParameter("Variable", "x");
            subpanel.add(new Label(" " + varName + " = ", 1), "West");
            subpanel.add(this.xInput, "Center");
            panel.add(xSlider);
            panel.add(subpanel);
            panel.add(yDisplay);
            if (this.inputPanel == null) {
                this.mainPanel.add(panel, "South");
            } else {
                this.inputPanel.setBackground(this.getColorParam("PanelBackground", Color.lightGray));
                this.inputPanel.add(panel, "South");
            }

            this.hLine.setPoints(new Constant(0.0D), yValue, xSlider, yValue);
            this.hLine.setPoints(new Constant(0.0D), yValue, xSlider, yValue);
            this.point.setShape(11);
            this.point.setPoints(xSlider, yValue, 5, 5);
            this.point.setLineWidth(3);
            this.vLine.setPoints(xSlider, new Constant(0.0D), xSlider, yValue);
            Color c = this.getColorParam("LineColor", Color.lightGray);
            this.vLine.setColor(c);
            this.hLine.setColor(c);
            c = this.getColorParam("DotColor", Color.gray);
            this.point.setColor(c);
            Controller cc = new Controller();
            this.xInput.setOnTextChange(cc);
            xSlider.setOnUserAction(cc);
            coords.setOnChange(cc);
            cc.add(this.xInput);
            cc.add(xSlider);
            cc.add(new Tie(xSlider, this.xInput));
            cc.add(this.hLine);
            cc.add(this.vLine);
            cc.add(this.point);
            cc.add(yDisplay);
            this.mainController.add(cc);
            this.mainController.remove(this.canvas);
            this.mainController.add(this.graph);
        }
    }

    protected void doLoadExample(String example) {
        int pos = example.indexOf(";");
        double[] limits = new double[]{-5.0D, 5.0D, -5.0D, 5.0D};
        if (pos > 0) {
            String limitsText = example.substring(pos + 1);
            example = example.substring(0, pos);
            StringTokenizer toks = new StringTokenizer(limitsText, " ,");
            if (toks.countTokens() >= 4) {
                for (int i = 0; i < 4; ++i) {
                    try {
                        Double d = Double.valueOf(toks.nextToken());
                        limits[i] = d;
                    } catch (NumberFormatException var10) {
                    }
                }

                if (toks.countTokens() > 0 && this.xInput != null) {
                    try {
                        Double d = Double.valueOf(toks.nextToken());
                        this.xInput.setVal(d);
                    } catch (NumberFormatException var9) {
                    }
                }
            }
        }

        if (this.functionInput != null) {
            this.functionInput.setText(example);
        } else {
            try {
                Function f = new SimpleFunction(this.parser.parse(example), this.xVar);
                ((WrapperFunction) this.func).setFunction(f);
            } catch (ParseError var8) {
            }
        }

        CoordinateRect coords = this.canvas.getCoordinateRect(0);
        coords.setLimits(limits);
        coords.setRestoreBuffer();
        this.mainController.compute();
    }
}
