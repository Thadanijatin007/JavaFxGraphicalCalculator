//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import edu.awt.Computable;
import edu.awt.Controller;
import edu.awt.Tie;
import edu.awt.VariableInput;
import edu.awt.VariableSlider;
import edu.data.Expression;
import edu.data.Function;
import edu.data.ParseError;
import edu.data.SimpleFunction;
import edu.data.Value;
import edu.data.ValueMath;
import edu.draw.CoordinateRect;
import edu.draw.Crosshair;
import edu.draw.DrawBorder;
import edu.draw.DrawString;
import edu.draw.Graph1D;
import edu.draw.Grid;
import edu.draw.Panner;
import edu.draw.TangentLine;
import edu.functions.WrapperFunction;
import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.StringTokenizer;
import javax.swing.JFrame;

public class Derivatives extends GenericGraphApplet {
    private String functionName;
    private Function func;
    private Function deriv;
    private Expression derivExpression;
    private Function deriv2;
    private Controller subController = new Controller();
    private VariableInput xInput;

    public Derivatives() {
    }

    protected void setUpParameterDefaults() {
        this.parameterDefaults = new Hashtable();
        this.parameterDefaults.put("Function", " tan(" + this.getParameter("Variable", "x") + ")");
    }

    protected void setUpMainPanel() {
        super.setUpMainPanel();
        if (this.limitsPanel != null) {
            this.limitsPanel.addCoords(this.canvas.getCoordinateRect(1));
            if (this.deriv2 != null) {
                this.limitsPanel.addCoords(this.canvas.getCoordinateRect(2));
            }
        } else {
            Tie coordTie = new Tie(this.canvas.getCoordinateRect(0), this.canvas.getCoordinateRect(1));
            if (this.deriv2 != null) {
                coordTie.add(this.canvas.getCoordinateRect(2));
            }

            this.canvas.getCoordinateRect(0).setSyncWith(coordTie);
            this.canvas.getCoordinateRect(1).setSyncWith(coordTie);
            if (this.deriv2 != null) {
                this.canvas.getCoordinateRect(2).setSyncWith(coordTie);
            }
        }

        Value xMin = this.canvas.getCoordinateRect().getValueObject(0);
        Value xMax = this.canvas.getCoordinateRect().getValueObject(1);
        this.canvas.getCoordinateRect().setOnChange(this.subController);
        VariableSlider xSlider = new VariableSlider(xMin, xMax);
        xSlider.setOnUserAction(this.subController);
        this.xInput.setOnTextChange(this.subController);
        this.subController.add(xSlider);
        this.subController.add(this.xInput);
        this.subController.add(new Tie(xSlider, this.xInput));
        Panel p = new Panel();
        p.setLayout(new BorderLayout(5, 5));
        p.add(this.xInput.withLabel(), "West");
        p.add(xSlider, "Center");
        if (this.limitsPanel == null && !"no".equalsIgnoreCase(this.getParameter("UseRestoreButton", "no"))) {
            Button res = new Button("Restore Limits");
            p.add(res, "East");
            res.setBackground(Color.lightGray);
            res.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    Derivatives.this.canvas.getCoordinateRect(0).restore();
                    Derivatives.this.canvas.getCoordinateRect(1).restore();
                    if (Derivatives.this.deriv2 != null) {
                        Derivatives.this.canvas.getCoordinateRect(2).restore();
                    }

                }
            });
        }

        if ("yes".equalsIgnoreCase(this.getParameter("ShowFormula", "yes"))) {
            Panel s = new Panel();
            s.setLayout(new GridLayout(2, 1, 3, 3));
            s.add(p);
            Derivatives.ExprLbl lbl = new Derivatives.ExprLbl(" " + this.functionName + "'(" + this.xVar.getName() + ") = ");
            this.mainController.add(lbl);
            s.add(lbl);
            p = s;
        }

        if (this.inputPanel == null) {
            p.setBackground(this.getColorParam("PanelBackground", Color.lightGray));
            this.mainPanel.add(p, "South");
        } else {
            this.inputPanel.add(p, "South");
        }

    }

    protected void setUpCanvas() {
        boolean showSecond = !"no".equalsIgnoreCase(this.getParameter("SecondDerivative", "no"));
        this.xInput = new VariableInput(this.xVar.getName(), this.getParameter("X", "1"));
        if (this.functionInput != null) {
            this.func = this.functionInput.getFunction(this.xVar);
            this.derivExpression = this.functionInput.getExpression().derivative(this.xVar);
        } else {
            String def = this.getParameter("Function");
            Expression exp = this.parser.parse(def);
            Function f = new SimpleFunction(exp, this.xVar);
            this.derivExpression = exp.derivative(this.xVar);
            this.func = new WrapperFunction(f);
        }

        Graph1D graph = new Graph1D(this.func);
        Color color = this.getColorParam("GraphColor", Color.black);
        graph.setColor(color);
        this.deriv = this.func.derivative(1);
        Graph1D derivGraph = new Graph1D(this.deriv);
        derivGraph.setColor(color);
        Graph1D deriv2Graph = null;
        if (showSecond) {
            this.deriv2 = this.deriv.derivative(1);
            deriv2Graph = new Graph1D(this.deriv2);
            deriv2Graph.setColor(color);
        }

        if (showSecond) {
            this.canvas.addNewCoordinateRect(0.0D, 0.3333333333333333D, 0.0D, 1.0D);
            this.canvas.addNewCoordinateRect(0.3333333333333333D, 0.6666666666666666D, 0.0D, 1.0D);
            this.canvas.addNewCoordinateRect(0.6666666666666666D, 1.0D, 0.0D, 1.0D);
        } else {
            this.canvas.addNewCoordinateRect(0.0D, 0.5D, 0.0D, 1.0D);
            this.canvas.addNewCoordinateRect(0.5D, 1.0D, 0.0D, 1.0D);
        }

        color = this.getColorParam("CanvasColor");
        if (color != null) {
            this.canvas.setBackground(color);
        }

        if (!"no".equalsIgnoreCase(this.getParameter("UsePanner", "no"))) {
            this.canvas.add(new Panner(), 0);
            this.canvas.add(new Panner(), 1);
            if (showSecond) {
                this.canvas.add(new Panner(), 2);
            }
        }

        if (!"no".equalsIgnoreCase(this.getParameter("UseGrid", "no"))) {
            Grid g = new Grid();
            color = this.getColorParam("GridColor");
            if (color != null) {
                g.setColor(color);
            }

            this.canvas.add(g, 0);
            g = new Grid();
            color = this.getColorParam("GridColor");
            if (color != null) {
                g.setColor(color);
            }

            this.canvas.add(g, 1);
            if (showSecond) {
                g = new Grid();
                color = this.getColorParam("GridColor");
                if (color != null) {
                    g.setColor(color);
                }

                this.canvas.add(g, 2);
            }
        }

        this.canvas.add(this.makeAxes(), 0);
        this.canvas.add(this.makeAxes(), 1);
        if (showSecond) {
            this.canvas.add(this.makeAxes(), 2);
        }

        if (!"no".equalsIgnoreCase(this.getParameter("UseMouseZoom", "no"))) {
            this.canvas.setHandleMouseZooms(true);
        }

        if ("yes".equalsIgnoreCase(this.getParameter("UseOffscreenCanvas", "yes"))) {
            this.canvas.setUseOffscreenCanvas(true);
        }

        this.mainController.setErrorReporter(this.canvas);
        this.mainPanel.add(this.canvas, "Center");
        this.canvas.add(graph, 0);
        this.canvas.add(derivGraph, 1);
        if (showSecond) {
            this.canvas.add(deriv2Graph, 2);
        }

        Color tangentColor = this.getColorParam("TangentColor", Color.red);
        Color tangentColor2 = this.getColorParam("TangentColor2", new Color(0, 180, 0));
        this.mainController.remove(this.canvas);
        this.mainController.add(graph);
        this.mainController.add(derivGraph);
        if (showSecond) {
            this.mainController.add(deriv2Graph);
        }

        this.subController = new Controller();
        this.mainController.add(this.subController);
        TangentLine tan = new TangentLine(this.xInput, this.func);
        Crosshair cross = new Crosshair(this.xInput, this.deriv);
        tan.setColor(tangentColor);
        cross.setColor(tangentColor);
        this.canvas.add(tan, 0);
        this.canvas.add(cross, 1);
        this.subController.add(tan);
        this.subController.add(cross);
        if (showSecond) {
            tan = new TangentLine(this.xInput, this.deriv);
            cross = new Crosshair(this.xInput, this.deriv2);
            tan.setColor(tangentColor2);
            cross.setColor(tangentColor2);
            this.canvas.add(tan, 1);
            this.canvas.add(cross, 2);
            this.subController.add(tan);
            this.subController.add(cross);
        }

        this.functionName = this.getParameter("FunctionName", "f");
        String yName = this.getParameter("YName", "y");
        Color textColor = this.getColorParam("TextColor", Color.black);
        Color bgColor = this.getColorParam("TextBackground", Color.white);
        DrawString str;
        if ("yes".equalsIgnoreCase(this.getParameter("ShowGraphLabels", "yes"))) {
            str = new DrawString(yName + " = " + this.functionName + "(" + this.xVar.getName() + ")");
            str.setColor(textColor);
            str.setBackgroundColor(bgColor);
            str.setFrameWidth(1);
            this.canvas.add(str, 0);
            str = new DrawString(yName + " = " + this.functionName + " ' (" + this.xVar.getName() + ")");
            str.setColor(textColor);
            str.setBackgroundColor(bgColor);
            str.setFrameWidth(1);
            this.canvas.add(str, 1);
            if (showSecond) {
                str = new DrawString(yName + " = " + this.functionName + " ' ' (" + this.xVar.getName() + ")");
                str.setColor(textColor);
                str.setBackgroundColor(bgColor);
                str.setFrameWidth(1);
                this.canvas.add(str, 2);
            }
        }

        if ("yes".equalsIgnoreCase(this.getParameter("ShowValues", "yes"))) {
            str = new DrawString(this.functionName + "(#) = #", 8, new Value[]{this.xInput, new ValueMath(this.func, this.xInput)});
            str.setColor(textColor);
            str.setBackgroundColor(bgColor);
            str.setFrameWidth(1);
            str.setNumSize(7);
            this.canvas.add(str, 0);
            this.subController.add(str);
            str = new DrawString(this.functionName + " ' (#) = #", 8, new Value[]{this.xInput, new ValueMath(this.deriv, this.xInput)});
            str.setColor(textColor);
            str.setBackgroundColor(bgColor);
            str.setFrameWidth(1);
            str.setNumSize(7);
            this.canvas.add(str, 1);
            this.subController.add(str);
            if (showSecond) {
                str = new DrawString(this.functionName + " ' ' (#) = #", 8, new Value[]{this.xInput, new ValueMath(this.deriv2, this.xInput)});
                str.setColor(textColor);
                str.setBackgroundColor(bgColor);
                str.setFrameWidth(1);
                str.setNumSize(7);
                this.canvas.add(str, 2);
                this.subController.add(str);
            }
        }

    }

    protected void addCanvasBorder() {
        double[] bw = this.getNumericParam("BorderWidth");
        int borderWidth;
        if (bw != null && bw.length != 0 && !(bw[0] > 25.0D)) {
            borderWidth = (int)Math.round(bw[0]);
        } else {
            borderWidth = 2;
        }

        if (borderWidth > 0) {
            this.canvas.add(new DrawBorder(this.getColorParam("BorderColor", Color.black), borderWidth), 0);
            this.canvas.add(new DrawBorder(this.getColorParam("BorderColor", Color.black), borderWidth), 1);
            if (this.deriv2 != null) {
                this.canvas.add(new DrawBorder(this.getColorParam("BorderColor", Color.black), borderWidth), 2);
            }
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
                for(int i = 0; i < 4; ++i) {
                    try {
                        Double d = Double.valueOf(toks.nextToken());
                        limits[i] = d;
                    } catch (NumberFormatException var10) {
                    }
                }

                if (toks.countTokens() > 0) {
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
                Expression exp = this.parser.parse(example);
                this.derivExpression = exp.derivative(this.xVar);
                Function f = new SimpleFunction(exp, this.xVar);
                ((WrapperFunction)this.func).setFunction(f);
            } catch (ParseError var8) {
            }
        }

        CoordinateRect coords = this.canvas.getCoordinateRect(0);
        coords.setLimits(limits);
        coords.setRestoreBuffer();
        this.canvas.getCoordinateRect(1).setRestoreBuffer();
        if (this.deriv2 != null) {
            this.canvas.getCoordinateRect(0).setRestoreBuffer();
        }

        this.mainController.compute();
    }

    public static void main(String[] a) {
        JFrame f = new JFrame();
        Applet app = new Derivatives();
        app.init();
        f.getContentPane().add(app);
        f.pack();
        f.setSize(new Dimension(500, 500));
        f.setVisible(true);
    }

    private class ExprLbl extends Label implements Computable {
        String label;

        ExprLbl(String label) {
            this.label = label;
            this.compute();
        }

        public void compute() {
            this.setText(this.label + Derivatives.this.derivExpression.toString());
        }
    }
}
