import edu.awt.JCMPanel;
import edu.awt.VariableInput;
import edu.data.*;
import edu.draw.CoordinateRect;
import edu.draw.DrawString;
import edu.draw.Graph1D;
import edu.draw.RiemannSumRects;
import edu.functions.WrapperFunction;

import javax.swing.*;
import java.applet.Applet;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.Hashtable;
import java.util.StringTokenizer;

public class RiemannSums extends GenericGraphApplet {
    private Variable intervals;
    private VariableInput intCtInput;
    private Choice methodChoice;
    private Function func;
    private Graph1D graph;
    private RiemannSumRects sums;

    public RiemannSums() {
    }

    public static void main(String[] a) {
        JFrame f = new JFrame();
        Applet app = new RiemannSums();
        app.init();
        f.getContentPane().add(app);
        f.pack();
        f.setSize(new Dimension(500, 500));
        f.setVisible(true);
    }

    public void itemStateChanged(ItemEvent evt) {
        if (evt.getSource() == this.methodChoice) {
            this.sums.setMethod(this.methodChoice.getSelectedIndex());
            this.mainController.compute();
        } else {
            super.itemStateChanged(evt);
        }

    }

    protected void setUpParameterDefaults() {
        this.parameterDefaults = new Hashtable();
        String func = " 3 / (1 + " + this.getParameter("Variable", "x") + "^2)";
        this.parameterDefaults.put("Function", func);
        this.parameterDefaults.put("ComputeButtonName", "Compute!");
    }

    protected void setUpCanvas() {
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

        double[] intCtD = this.getNumericParam("IntervalCount");
        if (intCtD != null && intCtD.length >= 1) {
            if (Double.isNaN(intCtD[0]) || intCtD[0] < 1.0D || intCtD[0] > 5000.0D) {
                intCtD[0] = 5.0D;
            }
        } else {
            intCtD = new double[]{5.0D};
        }

        int intCt = (int) (intCtD[0] + 0.5D);
        if ("yes".equalsIgnoreCase(this.getParameter("UseIntervalInput", "yes"))) {
            this.intCtInput = new VariableInput(null, "" + intCt);
            this.intCtInput.setInputStyle(2);
            this.intCtInput.setMin(1.0D);
            this.intCtInput.setMax(5000.0D);
            this.intervals = this.intCtInput.getVariable();
        } else {
            this.intervals = new Variable(null, intCt);
        }

        int method = 0;
        String methodStr = this.getParameter("Method");
        if (methodStr != null && methodStr.trim().length() > 0) {
            switch (methodStr.trim().charAt(0)) {
                case 'C':
                case 'c':
                    method = 3;
                case 'D':
                case 'E':
                case 'F':
                case 'G':
                case 'H':
                case 'J':
                case 'K':
                case 'N':
                case 'O':
                case 'P':
                case 'Q':
                case 'S':
                case 'U':
                case 'V':
                case 'W':
                case 'X':
                case 'Y':
                case 'Z':
                case '[':
                case '\\':
                case ']':
                case '^':
                case '_':
                case '`':
                case 'a':
                case 'b':
                case 'd':
                case 'e':
                case 'f':
                case 'g':
                case 'h':
                case 'j':
                case 'k':
                case 'n':
                case 'o':
                case 'p':
                case 'q':
                case 's':
                default:
                    break;
                case 'I':
                case 'i':
                    method = 4;
                    break;
                case 'L':
                case 'l':
                    method = 0;
                    break;
                case 'M':
                case 'm':
                    method = 2;
                    break;
                case 'R':
                case 'r':
                    method = 1;
                    break;
                case 'T':
                case 't':
                    method = 5;
            }
        }

        if ("yes".equalsIgnoreCase(this.getParameter("UseMethodInput", "yes"))) {
            this.methodChoice = new Choice();
            this.methodChoice.add("Left Endpoint");
            this.methodChoice.add("Right Endpoint");
            this.methodChoice.add("Midpoint");
            this.methodChoice.add("~Circumscribed");
            this.methodChoice.add("~Inscribed");
            this.methodChoice.add("Trapezoid");
            this.methodChoice.select(method);
            this.methodChoice.addItemListener(this);
        }

        this.sums = new RiemannSumRects(this.func, this.intervals);
        this.sums.setMethod(method);
        this.canvas.add(this.sums);
        Color c = this.getColorParam("RectColor");
        if (c != null) {
            this.sums.setColor(c);
        }

        c = this.getColorParam("OutlineColor");
        if (c != null) {
            this.sums.setOutlineColor(c);
        }

        super.setUpCanvas();
        this.canvas.getCoordinateRect().setGap(10);
        this.canvas.add(this.graph);
        DrawString ds = new DrawString("sum = #", 0, new Value[]{this.sums.getValueObject(-1)});
        ds.setBackgroundColor(this.getColorParam("TextBackground", Color.white));
        ds.setColor(this.getColorParam("TextColor", Color.black));
        ds.setFrameWidth(1);
        this.canvas.add(ds);
        this.mainController.add(ds);
        this.mainController.add(this.sums);
        if (this.intCtInput != null) {
            this.intCtInput.setOnUserAction(this.mainController);
        }

        this.canvas.getCoordinateRect().setOnChange(this.mainController);
    }

    protected void setUpMainPanel() {
        super.setUpMainPanel();
        if (this.methodChoice != null || this.intCtInput != null) {
            JCMPanel panel = new JCMPanel();
            panel.setLayout(new FlowLayout());
            panel.setBackground(this.getColorParam("PanelBackground", Color.lightGray));
            if (this.intCtInput != null) {
                panel.add(new Label("Intervals:"));
                panel.add(this.intCtInput);
            }

            if (this.methodChoice != null) {
                panel.add(new Label("Method:"));
                panel.add(this.methodChoice);
            }

            if (this.inputPanel == null) {
                this.mainPanel.add(panel, "South");
            } else {
                this.inputPanel.setBackground(this.getColorParam("PanelBackground", Color.lightGray));
                this.inputPanel.add(panel, "South");
            }

        }
    }

    protected void doLoadExample(String example) {
        int pos = example.indexOf(";");
        double[] limits = new double[]{-5.0D, 5.0D, -5.0D, 5.0D};
        if (pos > 0) {
            String limitsText = example.substring(pos + 1);
            example = example.substring(0, pos);
            pos = limitsText.indexOf(";");
            if (pos > 0) {
                String methodStr = limitsText.substring(pos + 1).trim();
                limitsText = limitsText.substring(0, pos);
                if (methodStr.length() > 0) {
                    byte method;
                    switch (methodStr.charAt(0)) {
                        case 'C':
                        case 'c':
                            method = 3;
                            break;
                        case 'D':
                        case 'E':
                        case 'F':
                        case 'G':
                        case 'H':
                        case 'J':
                        case 'K':
                        case 'N':
                        case 'O':
                        case 'P':
                        case 'Q':
                        case 'S':
                        case 'U':
                        case 'V':
                        case 'W':
                        case 'X':
                        case 'Y':
                        case 'Z':
                        case '[':
                        case '\\':
                        case ']':
                        case '^':
                        case '_':
                        case '`':
                        case 'a':
                        case 'b':
                        case 'd':
                        case 'e':
                        case 'f':
                        case 'g':
                        case 'h':
                        case 'j':
                        case 'k':
                        case 'n':
                        case 'o':
                        case 'p':
                        case 'q':
                        case 's':
                        default:
                            method = -1;
                            break;
                        case 'I':
                        case 'i':
                            method = 4;
                            break;
                        case 'L':
                        case 'l':
                            method = 0;
                            break;
                        case 'M':
                        case 'm':
                            method = 2;
                            break;
                        case 'R':
                        case 'r':
                            method = 1;
                            break;
                        case 'T':
                        case 't':
                            method = 5;
                    }

                    if (method >= 0) {
                        this.sums.setMethod(method);
                        if (this.methodChoice != null) {
                            this.methodChoice.select(method);
                        }
                    }
                }
            }

            StringTokenizer toks = new StringTokenizer(limitsText, " ,");
            if (toks.countTokens() >= 4) {
                for (int i = 0; i < 4; ++i) {
                    try {
                        Double d = Double.valueOf(toks.nextToken());
                        limits[i] = d;
                    } catch (NumberFormatException var11) {
                    }
                }

                if (toks.countTokens() > 0) {
                    try {
                        Double d = Double.valueOf(toks.nextToken());
                        double intCtD = d;
                        if (intCtD < 1.0D) {
                            intCtD = 1.0D;
                        } else if (intCtD > 5000.0D) {
                            intCtD = 5000.0D;
                        }

                        this.intervals.setVal((int) (intCtD + 0.5D));
                    } catch (NumberFormatException var10) {
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
            } catch (ParseError var9) {
            }
        }

        CoordinateRect coords = this.canvas.getCoordinateRect(0);
        coords.setLimits(limits);
        coords.setRestoreBuffer();
        this.mainController.compute();
    }
}
