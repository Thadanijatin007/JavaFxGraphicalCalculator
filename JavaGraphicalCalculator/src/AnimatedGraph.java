import edu.awt.Animator;
import edu.awt.JCMPanel;
import edu.awt.VariableInput;
import edu.data.*;
import edu.draw.CoordinateRect;
import edu.draw.DrawString;
import edu.draw.Graph1D;

import javax.swing.*;
import java.applet.Applet;
import java.awt.*;
import java.util.Hashtable;
import java.util.StringTokenizer;

public class AnimatedGraph extends GenericGraphApplet {
    private Function func;
    private Graph1D graph;
    private Animator animator;
    private Variable kVar;
    private VariableInput kMin;
    private VariableInput kMax;
    private VariableInput kIntervals;

    public AnimatedGraph() {
    }

    public static void main(String[] a) {
        JFrame f = new JFrame();
        Applet app = new AnimatedGraph();
        app.init();
        f.getContentPane().add(app);
        f.pack();
        f.setSize(new Dimension(500, 500));
        f.setVisible(true);
    }

    protected void setUpParser() {
        int options = 133;
        if (!"no".equalsIgnoreCase(this.getParameter("UseNextAndPrev", "yes"))) {
            options |= 48;
        }

        this.animator = new Animator(options);
        this.kVar = this.animator.getValueAsVariable(this.getParameter("Parameter", "k"));
        this.parser.add(this.kVar);
        super.setUpParser();
        this.parameterDefaults = new Hashtable();
        String defaultFunction = this.xVar.getName() + " / (" + this.kVar.getName() + " - " + this.xVar.getName() + "^2)";
        this.parameterDefaults.put("Function", defaultFunction);
        if (!"no".equalsIgnoreCase(this.getParameter("UseAnimatorInputs"))) {
            this.parameterDefaults.put("TwoLimitsColumns", "yes");
        }

    }

    protected void setUpBottomPanel() {
        super.setUpBottomPanel();
        if (this.inputPanel != null) {
            this.inputPanel.add(this.animator, "South");
        } else {
            this.mainPanel.add(this.animator, "South");
        }

    }

    protected void setUpCanvas() {
        super.setUpCanvas();
        if (this.functionInput != null) {
            this.func = this.functionInput.getFunction(this.xVar);
        } else {
            String def = this.getParameter("Function");
            this.func = new SimpleFunction(this.parser.parse(def), this.xVar);
        }

        this.graph = new Graph1D(this.func);
        this.graph.setColor(this.getColorParam("GraphColor", Color.magenta));
        this.canvas.add(this.graph);
        if (!"no".equalsIgnoreCase(this.getParameter("UseAnimatorInputs"))) {
            this.kMin = new VariableInput(this.kVar.getName() + "Start", this.getParameter("ParameterMin", "-2"));
            this.kMax = new VariableInput(this.kVar.getName() + "End", this.getParameter("ParameterMax", "2"));
            this.kIntervals = new VariableInput("Intervals", this.getParameter("Intervals", "25"));
            this.kIntervals.setInputStyle(2);
            this.kIntervals.setMin(1.0D);
            this.kIntervals.setMax(1000.0D);
            this.kMin.setOnUserAction(this.mainController);
            this.kMax.setOnUserAction(this.mainController);
            this.kIntervals.setOnUserAction(this.mainController);
            this.animator.setMin(this.kMin);
            this.animator.setMax(this.kMax);
            this.animator.setIntervals(this.kIntervals);
            if (this.limitsPanel != null) {
                this.mainController.add(this.kMin);
                this.mainController.add(this.kMax);
                this.mainController.add(this.kIntervals);
            } else {
                JCMPanel ap = new JCMPanel(9, 0);
                ap.setBackground(this.getColorParam("PanelBackground", Color.lightGray));
                ap.add(new Label(this.kMin.getName()));
                ap.add(this.kMin);
                ap.add(new Label());
                ap.add(new Label(this.kMax.getName()));
                ap.add(this.kMax);
                ap.add(new Label());
                ap.add(new Label(this.kIntervals.getName()));
                ap.add(this.kIntervals);
                ap.add(new Label());
                this.mainPanel.add(ap, "East");
            }
        } else {
            try {
                this.animator.setMin(Double.valueOf(this.getParameter("ParameterMin", "-2")));
                this.animator.setMax(Double.valueOf(this.getParameter("ParameterMax", "2")));
                this.animator.setIntervals((int) Math.round(Double.valueOf(this.getParameter("Intervals", "25"))));
            } catch (NumberFormatException var3) {
            }
        }

        this.animator.setOnChange(this.mainController);
        if (!"no".equalsIgnoreCase(this.getParameter("ShowParameter", "yes"))) {
            DrawString param = new DrawString(this.kVar.getName() + " = #", 8, new Value[]{this.kVar});
            param.setBackgroundColor(this.canvas.getBackground());
            Color c = this.getColorParam("ParameterColor", Color.black);
            param.setColor(c);
            this.canvas.add(param);
        }

    }

    protected void setUpLimitsPanel() {
        super.setUpLimitsPanel();
        if (this.limitsPanel != null && this.kMin != null) {
            this.limitsPanel.addComponentPair(this.kMin, this.kMax);
            this.limitsPanel.addComponent(this.kIntervals);
        }

    }

    protected void doLoadExample(String example) {
        this.animator.stop();
        int pos = example.indexOf(";");
        boolean startAnimation = false;
        double[] limits = new double[]{-5.0D, 5.0D, -5.0D, 5.0D};
        if (pos > 0) {
            String nums = example.substring(pos + 1);
            example = example.substring(0, pos);
            StringTokenizer toks = new StringTokenizer(nums, " ,");

            int cntr;
            if (toks.countTokens() >= 4) {
                for (cntr = 0; cntr < 4; ++cntr) {
                    try {
                        Double d = Double.valueOf(toks.nextToken());
                        limits[cntr] = d;
                    } catch (NumberFormatException var18) {
                    }
                }
            }

            double d = 0;
            if (toks.hasMoreTokens()) {
                try {
                    d = Double.valueOf(toks.nextToken());
                    if (this.kMin == null) {
                        this.animator.setMin(d);
                    } else {
                        this.kMin.setVal(d);
                    }
                } catch (NumberFormatException var17) {
                }
            }

            if (toks.hasMoreTokens()) {
                try {
                    d = Double.valueOf(toks.nextToken());
                    if (this.kMax == null) {
                        this.animator.setMax(d);
                    } else {
                        this.kMax.setVal(d);
                    }
                } catch (NumberFormatException var16) {
                }
            }

            if (toks.hasMoreTokens()) {
                try {
                    cntr = (int) Math.round(Double.valueOf(toks.nextToken()));
                    if (this.kIntervals == null) {
                        this.animator.setIntervals(cntr);
                    } else {
                        this.kIntervals.setVal(d);
                    }
                } catch (NumberFormatException var15) {
                }
            }

            if (toks.hasMoreTokens()) {
                try {
                    cntr = (int) Math.round(Double.valueOf(toks.nextToken()));
                    this.animator.setLoopStyle(cntr);
                } catch (NumberFormatException var14) {
                }
            }

            if (toks.hasMoreTokens()) {
                try {
                    d = (int) Math.round(Double.valueOf(toks.nextToken()));
                    startAnimation = d == 1;
                } catch (NumberFormatException var13) {
                }
            }
        }

        if (this.functionInput != null) {
            this.functionInput.setText(example);
        } else {
            try {
                this.func = new SimpleFunction(this.parser.parse(example), this.xVar);
                this.graph.setFunction(this.func);
            } catch (ParseError var12) {
            }
        }

        CoordinateRect coords = this.canvas.getCoordinateRect(0);
        coords.setLimits(limits);
        coords.setRestoreBuffer();
        this.mainController.compute();
        if (startAnimation) {
            try {
                synchronized (this) {
                    this.wait(250L);
                }
            } catch (InterruptedException var11) {
            }

            this.animator.start();
        }

    }

    public void stop() {
        this.animator.stop();
        super.stop();
    }
}
