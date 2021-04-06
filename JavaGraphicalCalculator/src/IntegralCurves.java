import edu.awt.*;
import edu.data.Function;
import edu.data.ParseError;
import edu.data.SimpleFunction;
import edu.data.Variable;
import edu.draw.CoordinateRect;
import edu.draw.DrawTemp;
import edu.draw.VectorField;
import edu.functions.WrapperFunction;

import javax.swing.*;
import java.applet.Applet;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

public class IntegralCurves extends GenericGraphApplet {
    private static final int RK4 = 0;
    private static final int RK2 = 1;
    private static final int EULER = 2;
    double dt = 0.1D;
    private Variable yVar;
    private Function xFunc;
    private Function yFunc;
    private ExpressionInput functionInput2;
    private VectorField field;
    private Animator animator;
    private final Vector curves = new Vector();
    private VariableInput deltaT;
    private VariableInput xStart;
    private VariableInput yStart;
    private Choice methodChoice;
    private Button startCurveButton;
    private Button clearButton;
    private Color curveColor;
    private final IntegralCurves.Draw curveDrawer = new IntegralCurves.Draw();
    private final double[] nextPoint = new double[2];
    private final double[] params = new double[2];

    public IntegralCurves() {
    }

    public static void main(String[] a) {
        JFrame f = new JFrame();
        Applet app = new IntegralCurves();
        app.init();
        f.getContentPane().add(app);
        f.pack();
        f.setSize(new Dimension(500, 500));
        f.setVisible(true);
    }

    protected void setUpParser() {
        this.yVar = new Variable(this.getParameter("Variable2", "y"));
        this.parser.add(this.yVar);
        super.setUpParser();
        this.parameterDefaults = new Hashtable();
        this.parameterDefaults.put("FunctionLabel", " f1(" + this.xVar.getName() + "," + this.yVar.getName() + ") = ");
        this.parameterDefaults.put("FunctionLabel2", " f2(" + this.xVar.getName() + "," + this.yVar.getName() + ") = ");
        this.parameterDefaults.put("Function", " " + this.yVar.getName() + " - 0.1*" + this.xVar.getName());
        this.parameterDefaults.put("Function2", " - " + this.xVar.getName() + " - 0.1*" + this.yVar.getName());
        this.defaultFrameSize = new int[]{580, 440};
    }

    protected void setUpCanvas() {
        super.setUpCanvas();
        String type;
        if (this.functionInput != null) {
            this.xFunc = this.functionInput.getFunction(new Variable[]{this.xVar, this.yVar});
            this.yFunc = this.functionInput2.getFunction(new Variable[]{this.xVar, this.yVar});
        } else {
            type = this.getParameter("Function");
            String yFuncDef = this.getParameter("Function2");
            Function f = new SimpleFunction(this.parser.parse(type), new Variable[]{this.xVar, this.yVar});
            this.xFunc = new WrapperFunction(f);
            f = new SimpleFunction(this.parser.parse(yFuncDef), new Variable[]{this.xVar, this.yVar});
            this.yFunc = new WrapperFunction(f);
        }

        type = (this.getParameter("VectorStyle", "") + "A").toUpperCase();
        int style = 0;
        switch (type.charAt(0)) {
            case 'A':
                style = 0;
                break;
            case 'L':
                style = 1;
                break;
            case 'S':
                style = 4;
        }

        this.field = new VectorField(this.xFunc, this.yFunc, style);
        Color color = this.getColorParam("VectorColor");
        if (color != null) {
            this.field.setColor(color);
        }

        int space = style == 1 ? 20 : 30;
        double[] d = this.getNumericParam("VectorSpacing");
        if (d != null && d.length > 0 && d[0] >= 1.0D) {
            space = (int) Math.round(d[0]);
        }

        this.field.setPixelSpacing(space);
        this.canvas.add(this.field);
        this.curveColor = this.getColorParam("CurveColor", Color.magenta);
        if ("yes".equalsIgnoreCase(this.getParameter("MouseStartsCurves", "yes")) && "yes".equalsIgnoreCase(this.getParameter("DoCurves", "yes"))) {
            this.canvas.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent evt) {
                    CoordinateRect coords = IntegralCurves.this.canvas.getCoordinateRect();
                    double x = coords.pixelToX(evt.getX());
                    double y = coords.pixelToY(evt.getY());
                    if (IntegralCurves.this.xStart != null) {
                        IntegralCurves.this.xStart.setVal(x);
                    }

                    if (IntegralCurves.this.yStart != null) {
                        IntegralCurves.this.yStart.setVal(y);
                    }

                    IntegralCurves.this.startCurve(x, y);
                }
            });
        }

    }

    protected void setUpBottomPanel() {
        double[] DT = this.getNumericParam("DeltaT");
        if (DT != null && DT.length != 0 && !(DT[0] <= 0.0D)) {
            this.dt = DT[0];
        }

        boolean doCurves = "yes".equalsIgnoreCase(this.getParameter("DoCurves", "yes"));
        boolean useInputs = "yes".equalsIgnoreCase(this.getParameter("UseFunctionInput", "yes"));
        if (doCurves || useInputs) {
            this.inputPanel = new JCMPanel();
            this.inputPanel.setBackground(this.getColorParam("PanelBackground", Color.lightGray));
            this.mainPanel.add(this.inputPanel, "South");
            JCMPanel in1 = null;
            JCMPanel in2 = null;
            if (useInputs) {
                if ("yes".equalsIgnoreCase(this.getParameter("UseComputeButton", "yes"))) {
                    String cname = this.getParameter("ComputeButtonName", "New Functions");
                    this.computeButton = new Button(cname);
                    this.computeButton.addActionListener(this);
                }

                this.functionInput = new ExpressionInput(this.getParameter("Function"), this.parser);
                in1 = new JCMPanel();
                in1.add(this.functionInput, "Center");
                in1.add(new Label(this.getParameter("FunctionLabel")), "West");
                this.functionInput.setOnUserAction(this.mainController);
                this.functionInput2 = new ExpressionInput(this.getParameter("Function2"), this.parser);
                in2 = new JCMPanel();
                in2.add(this.functionInput2, "Center");
                in2.add(new Label(this.getParameter("FunctionLabel2")), "West");
                this.functionInput2.setOnUserAction(this.mainController);
            }

            if (!doCurves) {
                Panel p = new JCMPanel(2, 1, 3);
                p.add(in1);
                p.add(in2);
                this.inputPanel.add(p, "Center");
                if (this.computeButton != null) {
                    this.inputPanel.add(this.computeButton, "East");
                }

            } else {
                this.animator = new Animator(8);
                this.animator.setStopButtonName("Stop Curves");
                this.animator.setOnChange(new Computable() {
                    public void compute() {
                        IntegralCurves.this.extendCurves();
                    }
                });
                this.mainController.add(new InputObject() {
                    public void checkInput() {
                        IntegralCurves.this.curves.setSize(0);
                        IntegralCurves.this.animator.stop();
                    }

                    public void notifyControllerOnChange(Controller c) {
                    }
                });
                this.clearButton = new Button("Clear");
                this.clearButton.addActionListener(this);
                Panel bottom = null;
                if ("yes".equalsIgnoreCase(this.getParameter("UseStartInputs", "yes"))) {
                    this.xStart = new VariableInput();
                    this.xStart.addActionListener(this);
                    this.yStart = new VariableInput();
                    this.yStart.addActionListener(this);
                    bottom = new Panel();
                    this.startCurveButton = new Button("Start curve at:");
                    this.startCurveButton.addActionListener(this);
                    bottom.add(this.startCurveButton);
                    bottom.add(new Label(this.xVar.getName() + " ="));
                    bottom.add(this.xStart);
                    bottom.add(new Label(this.yVar.getName() + " ="));
                    bottom.add(this.yStart);
                }

                boolean useChoice = "yes".equalsIgnoreCase(this.getParameter("UseMethodChoice", "yes"));
                boolean useDelta = "yes".equalsIgnoreCase(this.getParameter("UseDeltaInput", "yes"));
                if (!useChoice && !useDelta) {
                    if (bottom == null) {
                        bottom = new Panel();
                    }

                    bottom.add(this.animator);
                    bottom.add(this.clearButton);
                } else {
                    Panel top = new Panel();
                    if (useDelta) {
                        top.add(new Label("dt ="));
                        this.deltaT = new VariableInput(null, "" + this.dt);
                        top.add(this.deltaT);
                    }

                    if (useChoice) {
                        top.add(new Label("Method:"));
                        this.methodChoice = new Choice();
                        this.methodChoice.add("Runge-Kutta 4");
                        this.methodChoice.add("Runge-Kutta 2");
                        this.methodChoice.add("Euler");
                        top.add(this.methodChoice);
                    }

                    top.add(this.animator);
                    top.add(this.clearButton);
                    if (bottom == null) {
                        bottom = top;
                    } else {
                        Panel p = new Panel();
                        p.setLayout(new BorderLayout());
                        p.add(top, "North");
                        p.add(bottom, "Center");
                        bottom = p;
                    }
                }

                this.inputPanel.add(bottom, "Center");
                if (in1 != null) {
                    Panel in = new JCMPanel(1, 2);
                    in.add(in1);
                    in.add(in2);
                    if (this.computeButton != null) {
                        Panel p = new JCMPanel();
                        p.add(in, "Center");
                        p.add(this.computeButton, "East");
                        in = p;
                    }

                    this.inputPanel.add(in, "North");
                }
            }
        }
    }

    public void actionPerformed(ActionEvent evt) {
        Object src = evt.getSource();
        if (src == this.clearButton) {
            this.canvas.clearErrorMessage();
            this.curves.setSize(0);
            this.animator.stop();
            this.canvas.compute();
        } else if (src != this.xStart && src != this.yStart && src != this.startCurveButton) {
            super.actionPerformed(evt);
        } else {
            this.canvas.clearErrorMessage();
            double x = 0.0D;
            double y = 0.0D;

            try {
                this.xStart.checkInput();
                x = this.xStart.getVal();
                this.yStart.checkInput();
                y = this.yStart.getVal();
                this.startCurve(x, y);
                if (this.deltaT != null) {
                    this.deltaT.checkInput();
                    this.dt = this.deltaT.getVal();
                    if (this.dt <= 0.0D) {
                        this.deltaT.requestFocus();
                        throw new JCMError("dt must be positive", this.deltaT);
                    }
                }
            } catch (JCMError var8) {
                this.curves.setSize(0);
                this.animator.stop();
                this.canvas.setErrorMessage(null, "Illegal Data For Curve.  " + var8.getMessage());
            }
        }

    }

    public void startCurve(double x, double y) {
        synchronized (this.curves) {
            if (this.deltaT != null) {
                try {
                    this.deltaT.checkInput();
                    this.dt = this.deltaT.getVal();
                    if (this.dt <= 0.0D) {
                        this.deltaT.requestFocus();
                        throw new JCMError("dt must be positive", this.deltaT);
                    }
                } catch (JCMError var9) {
                    this.curves.setSize(0);
                    this.animator.stop();
                    this.canvas.setErrorMessage(null, "Illegal Data For Curve.  " + var9.getMessage());
                    return;
                }
            }

            IntegralCurves.Curve c = new IntegralCurves.Curve();
            c.dt = this.dt;
            int method = this.methodChoice == null ? 0 : this.methodChoice.getSelectedIndex();
            c.method = method;
            c.x = x;
            c.y = y;
            this.curves.addElement(c);
            this.animator.start();
        }
    }

    public void extendCurves() {
        synchronized (this.curves) {
            if (this.canvas != null && this.canvas.getCoordinateRect() != null) {
                while (this.canvas.getCoordinateRect().getWidth() <= 0) {
                    try {
                        Thread.sleep(200L);
                    } catch (InterruptedException var11) {
                    }
                }

                int size = this.curves.size();

                for (int i = 0; i < size; ++i) {
                    IntegralCurves.Curve curve = (IntegralCurves.Curve) this.curves.elementAt(i);
                    curve.lastX = curve.x;
                    curve.lastY = curve.y;
                    this.nextPoint(curve.x, curve.y, curve.dt, curve.method);
                    curve.x = this.nextPoint[0];
                    curve.y = this.nextPoint[1];
                }

                CoordinateRect c = this.canvas.getCoordinateRect();
                double pixelWidthLimit = 100000.0D * c.getPixelWidth();
                double pixelHeightLimit = 100000.0D * c.getPixelHeight();

                for (int i = size - 1; i >= 0; --i) {
                    IntegralCurves.Curve curve = (IntegralCurves.Curve) this.curves.elementAt(i);
                    if (Double.isNaN(curve.x) || Double.isNaN(curve.y) || Math.abs(curve.x) > pixelWidthLimit || Math.abs(curve.y) > pixelWidthLimit) {
                        this.curves.removeElementAt(i);
                    }
                }

                if (this.curves.size() > 0) {
                    this.canvas.drawTemp(this.curveDrawer);
                } else {
                    this.animator.stop();
                }

            }
        }
    }

    private void nextPoint(double x, double y, double dt, int method) {
        switch (method) {
            case 0:
                this.nextRK4(x, y, dt);
                break;
            case 1:
                this.nextRK2(x, y, dt);
                break;
            case 2:
                this.nextEuler(x, y, dt);
        }

    }

    private void nextEuler(double x, double y, double dt) {
        this.params[0] = x;
        this.params[1] = y;
        double dx = this.xFunc.getVal(this.params);
        double dy = this.yFunc.getVal(this.params);
        this.nextPoint[0] = x + dt * dx;
        this.nextPoint[1] = y + dt * dy;
    }

    private void nextRK2(double x, double y, double dt) {
        this.params[0] = x;
        this.params[1] = y;
        double dx1 = this.xFunc.getVal(this.params);
        double dy1 = this.yFunc.getVal(this.params);
        double x2 = x + dt * dx1;
        double y2 = y + dt * dy1;
        this.params[0] = x2;
        this.params[1] = y2;
        double dx2 = this.xFunc.getVal(this.params);
        double dy2 = this.yFunc.getVal(this.params);
        this.nextPoint[0] = x + 0.5D * dt * (dx1 + dx2);
        this.nextPoint[1] = y + 0.5D * dt * (dy1 + dy2);
    }

    private void nextRK4(double x, double y, double dt) {
        this.params[0] = x;
        this.params[1] = y;
        double dx1 = this.xFunc.getVal(this.params);
        double dy1 = this.yFunc.getVal(this.params);
        double x2 = x + 0.5D * dt * dx1;
        double y2 = y + 0.5D * dt * dy1;
        this.params[0] = x2;
        this.params[1] = y2;
        double dx2 = this.xFunc.getVal(this.params);
        double dy2 = this.yFunc.getVal(this.params);
        double x3 = x + 0.5D * dt * dx2;
        double y3 = y + 0.5D * dt * dy2;
        this.params[0] = x3;
        this.params[1] = y3;
        double dx3 = this.xFunc.getVal(this.params);
        double dy3 = this.yFunc.getVal(this.params);
        double x4 = x + dt * dx3;
        double y4 = y + dt * dy3;
        this.params[0] = x4;
        this.params[1] = y4;
        double dx4 = this.xFunc.getVal(this.params);
        double dy4 = this.yFunc.getVal(this.params);
        this.nextPoint[0] = x + dt / 6.0D * (dx1 + 2.0D * dx2 + 2.0D * dx3 + dx4);
        this.nextPoint[1] = y + dt / 6.0D * (dy1 + 2.0D * dy2 + 2.0D * dy3 + dy4);
    }

    protected void doLoadExample(String example) {
        if (this.animator != null) {
            this.curves.setSize(0);
            this.animator.stop();
        }

        int pos = example.indexOf(";");
        if (pos != -1) {
            String example2 = example.substring(pos + 1);
            example = example.substring(0, pos);
            pos = example2.indexOf(";");
            double[] limits = new double[]{-5.0D, 5.0D, -5.0D, 5.0D};
            StringTokenizer toks = null;
            int ct;
            if (pos > 0) {
                String nums = example2.substring(pos + 1);
                example2 = example2.substring(0, pos);
                toks = new StringTokenizer(nums, " ,");
                if (toks.countTokens() >= 4) {
                    for (ct = 0; ct < 4; ++ct) {
                        try {
                            Double d = Double.valueOf(toks.nextToken());
                            limits[ct] = d;
                        } catch (NumberFormatException var19) {
                        }
                    }
                }

                if (toks.hasMoreTokens()) {
                    double d = 0.0D / 0.0;

                    try {
                        d = Double.valueOf(toks.nextToken());
                    } catch (NumberFormatException var18) {
                    }

                    if (Double.isNaN(d) || d <= 0.0D || d > 100.0D) {
                        d = 0.1D;
                    }

                    if (this.deltaT != null) {
                        this.deltaT.setVal(d);
                    } else {
                        this.dt = d;
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
                } catch (ParseError var17) {
                }
            }

            CoordinateRect coords = this.canvas.getCoordinateRect(0);
            coords.setLimits(limits);
            coords.setRestoreBuffer();
            this.mainController.compute();
            if (this.animator != null && toks != null) {
                ct = 2 * (toks.countTokens() / 2);
                if (ct > 0) {
                    synchronized (this.curves) {
                        for (int i = 0; i < ct; ++i) {
                            try {
                                double x = Double.valueOf(toks.nextToken());
                                double y = Double.valueOf(toks.nextToken());
                                this.startCurve(x, y);
                            } catch (Exception var16) {
                            }
                        }

                        if (this.curves.size() > 0) {
                            try {
                                Thread.sleep(500L);
                            } catch (InterruptedException var15) {
                            }
                        }
                    }
                }
            }

        }
    }

    public void stop() {
        if (this.animator != null) {
            this.curves.setSize(0);
            this.animator.stop();
        }

        super.stop();
    }

    private class Draw implements DrawTemp {
        private Draw() {
        }

        public void draw(Graphics g, CoordinateRect coords) {
            int size = IntegralCurves.this.curves.size();
            g.setColor(IntegralCurves.this.curveColor);

            for (int i = 0; i < size; ++i) {
                IntegralCurves.Curve c = (IntegralCurves.Curve) IntegralCurves.this.curves.elementAt(i);
                if (!Double.isNaN(c.x) && !Double.isNaN(c.y) && !Double.isNaN(c.lastX) && !Double.isNaN(c.lastY)) {
                    int x1 = coords.xToPixel(c.lastX);
                    int y1 = coords.yToPixel(c.lastY);
                    int x2 = coords.xToPixel(c.x);
                    int y2 = coords.yToPixel(c.y);
                    g.drawLine(x1, y1, x2, y2);
                }
            }

        }
    }

    private class Curve {
        double dt;
        int method;
        double x;
        double y;
        double lastX;
        double lastY;

        private Curve() {
            this.lastX = 0.0D / 0.0;
        }
    }
}
