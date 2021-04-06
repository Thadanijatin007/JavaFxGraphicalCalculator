import edu.awt.Controller;
import edu.awt.Tie;
import edu.awt.Tieable;
import edu.awt.VariableInput;
import edu.data.*;
import edu.draw.*;
import edu.functions.WrapperFunction;

import javax.swing.*;
import java.applet.Applet;
import java.awt.*;
import java.util.Hashtable;
import java.util.StringTokenizer;

public class SecantTangent extends GenericGraphApplet {
    VariableInput x1Input = new VariableInput();
    VariableInput x2Input = new VariableInput();
    private Function func;

    public SecantTangent() {
    }

    public static void main(String[] a) {
        JFrame f = new JFrame();
        Applet app = new GenericGraphApplet();
        app.init();
        f.getContentPane().add(app);
        f.pack();
        f.setSize(new Dimension(500, 500));
        f.setVisible(true);
    }

    protected void setUpParameterDefaults() {
        this.parameterDefaults = new Hashtable();
        String varName = this.getParameter("Variable", "x");
        this.parameterDefaults.put("Function", " e ^ " + varName);
    }

    protected void setUpCanvas() {
        super.setUpCanvas();
        if (this.functionInput != null) {
            this.func = this.functionInput.getFunction(this.xVar);
        } else {
            String def = this.getParameter("Function");
            Function f = new SimpleFunction(this.parser.parse(def), this.xVar);
            this.func = new WrapperFunction(f);
        }

        Graph1D graph = new Graph1D(this.func);
        Color color = this.getColorParam("GraphColor", Color.black);
        graph.setColor(color);
        Color tangentColor = this.getColorParam("TangentColor", Color.red);
        Color secantColor = this.getColorParam("SecantColor", new Color(0, 200, 0));
        DraggablePoint drag1 = new DraggablePoint();
        DraggablePoint drag2 = new DraggablePoint();
        drag1.clampY(this.func);
        drag2.clampY(this.func);
        drag1.setColor(tangentColor);
        drag1.setGhostColor(this.lighten(tangentColor));
        drag2.setColor(secantColor);
        drag2.setGhostColor(this.lighten(secantColor));
        DrawGeometric secant = new DrawGeometric(1, drag1.getXVar(), drag1.getYVar(), drag2.getXVar(), drag2.getYVar());
        secant.setColor(secantColor);
        TangentLine tangent = new TangentLine(drag1.getXVar(), this.func);
        tangent.setColor(tangentColor);
        this.canvas.add(drag1);
        this.canvas.add(drag2);
        this.canvas.add(tangent);
        this.canvas.add(secant);
        this.canvas.add(graph);
        Value tangentSlope = new ValueMath(this.func.derivative(1), drag1.getXVar());
        Value secantSlope = new ValueMath(new ValueMath(drag2.getYVar(), drag1.getYVar(), '-'), new ValueMath(drag2.getXVar(), drag1.getXVar(), '-'), '/');
        DrawString info;
        if ("no".equalsIgnoreCase(this.getParameter("ShowTangentSlope", "yes"))) {
            info = new DrawString("Secant Slope = #", 0, new Value[]{secantSlope});
        } else {
            info = new DrawString("Secant Slope = #\nTangent Slope = #", 0, new Value[]{secantSlope, tangentSlope});
        }

        info.setFont(new Font("Monospaced", 0, 10));
        info.setNumSize(7);
        info.setColor(this.getColorParam("SlopeTextColor", Color.black));
        info.setBackgroundColor(this.getColorParam("SlopeTextBackground", Color.white));
        info.setFrameWidth(1);
        this.canvas.add(info);
        Panel xIn = new Panel();
        xIn.setBackground(this.getColorParam("PanelColor", Color.lightGray));
        xIn.setLayout(new GridLayout(1, 4, 3, 3));
        xIn.add(new Label("Tangent at " + this.xVar.getName() + " = ", 2));
        xIn.add(this.x1Input);
        xIn.add(new Label("Secant to  " + this.xVar.getName() + " = ", 2));
        xIn.add(this.x2Input);
        if (this.inputPanel == null) {
            this.mainPanel.add(xIn, "South");
        } else {
            this.inputPanel.add(xIn, "South");
        }

        Controller dragControl = new Controller();
        this.mainController.remove(this.canvas);
        this.mainController.add(graph);
        this.mainController.add(dragControl);
        dragControl.add(this.x1Input);
        dragControl.add(this.x2Input);
        dragControl.add(drag1);
        dragControl.add(drag2);
        dragControl.add(tangent);
        dragControl.add(secant);
        dragControl.add(info);
        drag1.setOnUserAction(dragControl);
        drag2.setOnUserAction(dragControl);
        this.x1Input.setOnTextChange(dragControl);
        this.x2Input.setOnTextChange(dragControl);
        dragControl.add(new Tie((Tieable) drag1.getXVar(), this.x1Input));
        dragControl.add(new Tie((Tieable) drag2.getXVar(), this.x2Input));
        double[] d1 = this.getNumericParam("X1");
        double x1 = d1 != null && d1.length == 1 ? d1[0] : 0.0D;
        this.x1Input.setVal(x1);
        drag1.setLocation(x1, 0.0D);
        double[] d2 = this.getNumericParam("X2");
        double x2 = d2 != null && d2.length == 1 ? d2[0] : 1.0D;
        this.x2Input.setVal(x2);
        drag2.setLocation(x2, 0.0D);
    }

    private Color lighten(Color c) {
        int r = c.getRed();
        int g = c.getGreen();
        int b = c.getBlue();
        int nr;
        int ng;
        int nb;
        if (r > 200 && g > 200 && b > 200) {
            nb = b / 2;
            ng = g / 2;
            nr = r / 2;
        } else {
            nb = 255 - (255 - b) / 3;
            ng = 255 - (255 - g) / 3;
            nr = 255 - (255 - r) / 3;
        }

        return new Color(nr, ng, nb);
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
                    } catch (NumberFormatException var11) {
                    }
                }

                Double d;
                if (toks.countTokens() > 0) {
                    try {
                        d = Double.valueOf(toks.nextToken());
                        this.x1Input.setVal(d);
                    } catch (NumberFormatException var10) {
                    }
                }

                if (toks.countTokens() > 0) {
                    try {
                        d = Double.valueOf(toks.nextToken());
                        this.x2Input.setVal(d);
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
