import edu.awt.*;
import edu.data.*;
import edu.draw.CoordinateRect;
import edu.draw.Crosshair;
import edu.draw.ParametricCurve;
import edu.functions.WrapperFunction;

import javax.swing.*;
import java.applet.Applet;
import java.awt.*;
import java.util.Hashtable;
import java.util.StringTokenizer;

public class Parametric extends GenericGraphApplet {
    private Function xFunc;
    private Function yFunc;
    private ParametricCurve graph;
    private Animator tracer;
    private Crosshair crosshair;
    private VariableInput tMin;
    private VariableInput tMax;
    private VariableInput tIntervals;
    private ExpressionInput functionInput2;

    public Parametric() {
    }

    public static void main(String[] a) {
        JFrame f = new JFrame();
        Applet app = new Parametric();
        app.init();
        f.getContentPane().add(app);
        f.pack();
        f.setSize(new Dimension(500, 500));
        f.setVisible(true);
    }

    protected void setUpParameterDefaults() {
        this.parameterDefaults = new Hashtable();
        this.parameterDefaults.put("TwoLimitsColumns", "yes");
        this.parameterDefaults.put("Variable", "t");
        this.parameterDefaults.put("XName", "x");
        this.parameterDefaults.put("FunctionLabel", "  " + this.getParameter("XName") + "(" + this.getParameter("Variable") + ") = ");
        this.parameterDefaults.put("FunctionLabel2", "  " + this.getParameter("YName", "y") + "(" + this.getParameter("Variable") + ") = ");
    }

    protected void setUpCanvas() {
        super.setUpCanvas();
        if (this.functionInput != null) {
            this.xFunc = this.functionInput.getFunction(this.xVar);
            this.yFunc = this.functionInput2.getFunction(this.xVar);
        } else {
            String xFuncDef = " cos(" + this.xVar.getName() + ") + cos(3*" + this.xVar.getName() + ")";
            String yFuncDef = " sin(4*" + this.xVar.getName() + ") - sin(2*" + this.xVar.getName() + ")";
            xFuncDef = this.getParameter("Function", xFuncDef);
            yFuncDef = this.getParameter("Function2", yFuncDef);
            Function f = new SimpleFunction(this.parser.parse(xFuncDef), this.xVar);
            this.xFunc = new WrapperFunction(f);
            f = new SimpleFunction(this.parser.parse(yFuncDef), this.xVar);
            this.yFunc = new WrapperFunction(f);
        }

        this.graph = new ParametricCurve(this.xFunc, this.yFunc);
        Color color = this.getColorParam("CurveColor");
        if (color != null) {
            this.graph.setColor(color);
        }

        if ("no".equalsIgnoreCase(this.getParameter("UseParamInputs", "yes"))) {
            this.tMin = new VariableInput(this.xVar.getName() + "Start", this.getParameter("ParameterMin", "-2"));
            this.tMax = new VariableInput(this.xVar.getName() + "End", this.getParameter("ParameterMax", "2"));
            this.tIntervals = new VariableInput("Intervals", this.getParameter("Intervals", "200"));
            this.tIntervals.setInputStyle(2);
            this.tIntervals.setMin(1.0D);
            this.tIntervals.setMax(5000.0D);
            this.tMin.setOnUserAction(this.mainController);
            this.tMax.setOnUserAction(this.mainController);
            this.tIntervals.setOnUserAction(this.mainController);
            this.graph.setTMin(this.tMin);
            this.graph.setTMax(this.tMax);
            this.graph.setIntervals(this.tIntervals);
            if (this.limitsPanel != null) {
                this.mainController.add(this.tMin);
                this.mainController.add(this.tMax);
                this.mainController.add(this.tIntervals);
            } else {
                JCMPanel ap = new JCMPanel(9, 0);
                ap.setBackground(this.getColorParam("PanelBackground", Color.lightGray));
                ap.add(new Label(this.tMin.getName()));
                ap.add(this.tMin);
                ap.add(new Label());
                ap.add(new Label(this.tMax.getName()));
                ap.add(this.tMax);
                ap.add(new Label());
                ap.add(new Label(this.tIntervals.getName()));
                ap.add(this.tIntervals);
                ap.add(new Label());
                this.mainPanel.add(ap, "East");
            }
        } else {
            try {
                this.graph.setTMin(new Constant(Double.valueOf(this.getParameter("ParameterMin", "-2"))));
                this.graph.setTMax(new Constant(Double.valueOf(this.getParameter("ParameterMax", "2"))));
                this.graph.setIntervals(new Constant(Double.valueOf(this.getParameter("Intervals", "25"))));
            } catch (NumberFormatException var6) {
            }
        }

        if (!"no".equalsIgnoreCase(this.getParameter("UseTracer", "yes"))) {
            this.tracer = new Animator();
            this.tracer.setMin(this.graph.getTMin());
            this.tracer.setMax(this.graph.getTMax());
            this.tracer.setUndefinedWhenNotRunning(true);
            this.tracer.setStartButtonName("Trace Curve!");
            double[] d = this.getNumericParam("TracerIntervals");
            int ints;
            if (d != null && d.length == 1) {
                ints = (int) Math.round(d[0]);
            } else {
                ints = 100;
            }

            if (ints <= 0) {
                this.tracer.setIntervals(this.graph.getIntervals());
            } else {
                this.tracer.setIntervals(ints);
            }

            Variable v = this.tracer.getValueAsVariable();
            this.crosshair = new Crosshair(new ValueMath(this.xFunc, v), new ValueMath(this.yFunc, v));
            this.crosshair.setLineWidth(3);
            this.crosshair.setColor(this.getColorParam("CrosshairColor", Color.gray));
            this.canvas.add(this.crosshair);
            if (this.inputPanel != null) {
                this.inputPanel.add(this.tracer, "West");
            } else if (this.limitsPanel == null) {
                Panel p = new Panel();
                p.add(this.tracer);
                this.mainPanel.add(p, "South");
            }
        }

        this.canvas.add(this.graph);
    }

    protected void setUpLimitsPanel() {
        super.setUpLimitsPanel();
        if (this.limitsPanel != null && this.tMin != null) {
            this.limitsPanel.addComponentPair(this.tMin, this.tMax);
            this.limitsPanel.addComponent(this.tIntervals);
        }

        if (this.inputPanel == null && this.tracer != null && this.limitsPanel != null) {
            this.limitsPanel.addComponent(this.tracer);
        }

    }

    protected void setUpBottomPanel() {
        if (!"no".equalsIgnoreCase(this.getParameter("UseFunctionInput", "yes"))) {
            this.inputPanel = new JCMPanel();
            this.inputPanel.setBackground(this.getColorParam("PanelBackground", Color.lightGray));
            Panel in = new JCMPanel(2, 1);
            this.inputPanel.add(in, "Center");
            String varName;
            if (!"no".equalsIgnoreCase(this.getParameter("UseComputeButton", "yes"))) {
                varName = this.getParameter("ComputeButtonName", "New Functions");
                this.computeButton = new Button(varName);
                this.inputPanel.add(this.computeButton, "East");
                this.computeButton.addActionListener(this);
            }

            varName = this.getParameter("Variable");
            String def = this.getParameter("Function");
            if (def == null) {
                def = "cos(" + varName + ") + cos(3*" + varName + ")";
            }

            this.functionInput = new ExpressionInput(def, this.parser);
            String label = this.getParameter("FunctionLabel");
            JCMPanel p;
            if ("none".equalsIgnoreCase(label)) {
                in.add(this.functionInput);
            } else {
                p = new JCMPanel();
                p.add(this.functionInput, "Center");
                p.add(new Label(label), "West");
                in.add(p);
            }

            def = this.getParameter("Function2");
            if (def == null) {
                def = "sin(4*" + varName + ") - sin(2*" + varName + ")";
            }

            this.functionInput2 = new ExpressionInput(def, this.parser);
            label = this.getParameter("FunctionLabel2");
            if ("none".equalsIgnoreCase(label)) {
                in.add(this.functionInput2);
            } else {
                p = new JCMPanel();
                p.add(this.functionInput2, "Center");
                p.add(new Label(label), "West");
                in.add(p);
            }

            this.mainPanel.add(this.inputPanel, "South");
            this.functionInput.setOnUserAction(this.mainController);
            this.functionInput2.setOnUserAction(this.mainController);
        }

    }

    protected void setUpMainPanel() {
        super.setUpMainPanel();
        if (this.tracer != null) {
            Controller traceController = new Controller();
            traceController.add(this.tracer);
            traceController.add(this.crosshair);
            this.tracer.setOnChange(traceController);
        }
    }

    protected void doLoadExample(String example) {
        if (this.tracer != null) {
            this.tracer.stop();
        }

        int pos = example.indexOf(";");
        if (pos != -1) {
            String example2 = example.substring(pos + 1);
            example = example.substring(0, pos);
            pos = example2.indexOf(";");
            double[] limits = new double[]{-5.0D, 5.0D, -5.0D, 5.0D};
            if (pos > 0) {
                String nums = example2.substring(pos + 1);
                example2 = example2.substring(0, pos);
                StringTokenizer toks = new StringTokenizer(nums, " ,");
                int cntr;
                if (toks.countTokens() >= 4) {
                    for (cntr = 0; cntr < 4; ++cntr) {
                        try {
                            Double d = Double.valueOf(toks.nextToken());
                            limits[cntr] = d;
                        } catch (NumberFormatException var13) {
                        }
                    }
                }

                double d = 0;
                if (toks.hasMoreTokens()) {
                    try {
                        d = Double.valueOf(toks.nextToken());
                        if (this.tMin == null) {
                            this.graph.setTMin(new Constant(d));
                            if (this.tracer != null) {
                                this.tracer.setMin(d);
                            }
                        } else {
                            this.tMin.setVal(d);
                        }
                    } catch (NumberFormatException var12) {
                    }
                }

                if (toks.hasMoreTokens()) {
                    try {
                        d = Double.valueOf(toks.nextToken());
                        if (this.tMax == null) {
                            this.graph.setTMax(new Constant(d));
                            if (this.tracer != null) {
                                this.tracer.setMax(d);
                            }
                        } else {
                            this.tMax.setVal(d);
                        }
                    } catch (NumberFormatException var11) {
                    }
                }

                if (toks.hasMoreTokens()) {
                    try {
                        cntr = (int) Math.round(Double.valueOf(toks.nextToken()));
                        if (this.tIntervals == null) {
                            if (this.tracer != null && this.tracer.getIntervals() == this.graph.getIntervals()) {
                                this.tracer.setIntervals(cntr);
                            }

                            this.graph.setIntervals(new Constant(d));
                        } else {
                            this.tIntervals.setVal(d);
                        }
                    } catch (NumberFormatException var10) {
                    }
                }
            }

            if (this.functionInput != null) {
                this.functionInput.setText(example);
                this.functionInput2.setText(example2);
            } else {
                try {
                    Function f = new SimpleFunction(this.parser.parse(example), this.xVar);
                    ((WrapperFunction) this.xFunc).setFunction(f);
                    Function g = new SimpleFunction(this.parser.parse(example2), this.xVar);
                    ((WrapperFunction) this.yFunc).setFunction(g);
                } catch (ParseError var9) {
                }
            }

            CoordinateRect coords = this.canvas.getCoordinateRect(0);
            coords.setLimits(limits);
            coords.setRestoreBuffer();
            this.mainController.compute();
        }
    }

    public void stop() {
        if (this.tracer != null) {
            this.tracer.stop();
        }

        super.stop();
    }
}
