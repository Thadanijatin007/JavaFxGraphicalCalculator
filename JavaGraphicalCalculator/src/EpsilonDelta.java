import edu.awt.Controller;
import edu.awt.JCMPanel;
import edu.awt.Tie;
import edu.awt.VariableInput;
import edu.awt.VariableSlider;
import edu.data.Constant;
import edu.data.Function;
import edu.data.ParseError;
import edu.data.SimpleFunction;
import edu.data.Value;
import edu.data.ValueMath;
import edu.data.Variable;
import edu.draw.CoordinateRect;
import edu.draw.DrawGeometric;
import edu.draw.DrawString;
import edu.draw.Graph1D;
import edu.functions.WrapperFunction;
import java.applet.Applet;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Label;
import java.util.StringTokenizer;
import javax.swing.JFrame;

public class EpsilonDelta extends GenericGraphApplet {
    private VariableInput xInput;
    private VariableInput epsilonInput;
    private VariableInput deltaInput;
    private VariableInput limitInput;
    private VariableSlider xSlider;
    private VariableSlider epsilonSlider;
    private VariableSlider deltaSlider;
    private VariableSlider limitSlider;
    private Controller subController;
    private Variable xValue;
    private Variable limitValue;
    private Function func;
    private Graph1D graph;

    public EpsilonDelta() {
    }

    protected void setUpBottomPanel() {
        super.setUpBottomPanel();
        this.subController = new Controller();
        this.mainController.add(this.subController);
        JCMPanel inputs = new JCMPanel(3);
        this.subController.add(inputs);
        inputs.setBackground(this.getColorParam("PanelBackground", Color.lightGray));
        if (this.inputPanel == null) {
            this.mainPanel.add(inputs, "South");
        } else {
            this.inputPanel.add(inputs, "South");
        }

        JCMPanel left = new JCMPanel(0, 1, 2);
        JCMPanel right = new JCMPanel(0, 1, 2);
        JCMPanel middle = new JCMPanel(0, 1, 2);
        inputs.add(middle, "Center");
        inputs.add(left, "West");
        inputs.add(right, "East");
        double[] a = this.getNumericParam("AValue");
        double avalue = a != null && a.length >= 1 ? a[0] : 0.0D;
        if ("yes".equalsIgnoreCase(this.getParameter("UseAInput", "yes"))) {
            this.xSlider = new VariableSlider();
            this.xInput = new VariableInput();
            this.xInput.setVal(avalue);
            this.xSlider.setVal(avalue);
            this.xInput.setThrowErrors(false);
            this.subController.add(new Tie(this.xSlider, this.xInput));
            this.xValue = this.xInput.getVariable();
            left.add(new Label("limit at a = ", 2));
            middle.add(this.xSlider);
            right.add(this.xInput);
        } else {
            this.xValue = new Variable();
            this.xValue.setVal(avalue);
        }

        a = this.getNumericParam("LimitValue");
        double Lvalue = a != null && a.length >= 1 ? a[0] : 1.0D;
        if ("yes".equalsIgnoreCase(this.getParameter("UseLimitInput", "yes"))) {
            this.limitSlider = new VariableSlider();
            this.limitInput = new VariableInput();
            this.limitInput.setVal(Lvalue);
            this.limitSlider.setVal(Lvalue);
            this.limitInput.setThrowErrors(false);
            this.subController.add(new Tie(this.limitSlider, this.limitInput));
            this.limitValue = this.limitInput.getVariable();
            left.add(new Label(" test limit L = ", 2));
            middle.add(this.limitSlider);
            right.add(this.limitInput);
        } else {
            this.limitValue = new Variable();
            this.limitValue.setVal(Lvalue);
        }

        a = this.getNumericParam("EpsilonValue");
        double epsilonValue = a != null && a.length >= 1 ? a[0] : 0.25D;
        this.epsilonSlider = new VariableSlider(new Constant(0.0D), new Constant(2.0D));
        this.epsilonInput = new VariableInput();
        this.epsilonInput.setVal(epsilonValue);
        this.epsilonSlider.setVal(epsilonValue);
        this.epsilonInput.setThrowErrors(false);
        this.subController.add(new Tie(this.epsilonSlider, this.epsilonInput));
        left.add(new Label("epsilon = ", 2));
        middle.add(this.epsilonSlider);
        right.add(this.epsilonInput);
        a = this.getNumericParam("DeltaValue");
        double deltaValue = a != null && a.length >= 1 ? a[0] : 1.0D;
        this.deltaSlider = new VariableSlider(new Constant(0.0D), new Constant(2.0D));
        this.deltaInput = new VariableInput();
        this.deltaInput.setVal(deltaValue);
        this.deltaSlider.setVal(deltaValue);
        this.deltaInput.setThrowErrors(false);
        this.subController.add(new Tie(this.deltaSlider, this.deltaInput));
        left.add(new Label("delta = ", 2));
        middle.add(this.deltaSlider);
        right.add(this.deltaInput);
    }

    protected void setUpCanvas() {
        if (this.functionInput != null) {
            this.func = this.functionInput.getFunction(this.xVar);
        } else {
            String def = this.getParameter("Function", "abs(" + this.xVar.getName() + ") ^ " + this.xVar.getName());
            Function f = new SimpleFunction(this.parser.parse(def), this.xVar);
            this.func = new WrapperFunction(f);
        }

        this.graph = new Graph1D(this.func);
        this.graph.setColor(this.getColorParam("GraphColor", Color.black));
        Value xMinusDelta = new ValueMath(this.xValue, this.deltaInput, '-');
        Value xPlusDelta = new ValueMath(this.xValue, this.deltaInput, '+');
        Value limitMinusEpsilon = new ValueMath(this.limitValue, this.epsilonInput, '-');
        Value limitPlusEpsilon = new ValueMath(this.limitValue, this.epsilonInput, '+');
        Value xmin = this.canvas.getCoordinateRect().getValueObject(0);
        Value xmax = this.canvas.getCoordinateRect().getValueObject(1);
        Value ymin = this.canvas.getCoordinateRect().getValueObject(2);
        Value ymax = this.canvas.getCoordinateRect().getValueObject(3);
        if (this.xSlider != null) {
            this.xSlider.setMin(xmin);
            this.xSlider.setMax(xmax);
        }

        if (this.limitSlider != null) {
            this.limitSlider.setMin(ymin);
            this.limitSlider.setMax(ymax);
        }

        DrawGeometric deltaBox = new DrawGeometric(2, xMinusDelta, ymin, xPlusDelta, ymax);
        deltaBox.setFillColor(new Color(225, 255, 225));
        deltaBox.setLineWidth(0);
        DrawGeometric epsilonBox = new DrawGeometric(2, xmin, limitMinusEpsilon, xmax, limitPlusEpsilon);
        epsilonBox.setFillColor(new Color(255, 230, 230));
        epsilonBox.setLineWidth(0);
        DrawGeometric overlap = new DrawGeometric(2, xMinusDelta, limitMinusEpsilon, xPlusDelta, limitPlusEpsilon);
        overlap.setFillColor(new Color(255, 255, 225));
        overlap.setColor(Color.yellow);
        DrawGeometric xLine = new DrawGeometric(0, this.xValue, ymin, this.xValue, ymax);
        xLine.setColor(new Color(130, 255, 130));
        DrawGeometric limitLine = new DrawGeometric(0, xmin, this.limitValue, xmax, this.limitValue);
        limitLine.setColor(new Color(255, 150, 150));
        this.canvas.add(deltaBox);
        this.canvas.add(epsilonBox);
        this.canvas.add(overlap);
        this.canvas.add(xLine);
        this.canvas.add(limitLine);
        DrawString ds = new DrawString("a = #\nL = #\nf(a) = #", 0, new Value[]{this.xValue, this.limitValue, new ValueMath(this.func, this.xValue)});
        ds.setBackgroundColor(Color.white);
        ds.setFrameWidth(1);
        this.subController.add(ds);
        this.subController.add(deltaBox);
        this.subController.add(epsilonBox);
        this.subController.add(overlap);
        this.subController.add(xLine);
        this.subController.add(limitLine);
        this.mainController.remove(this.canvas);
        this.mainController.add(this.graph);
        this.canvas.getCoordinateRect().setOnChange(this.mainController);
        this.deltaSlider.setOnUserAction(this.subController);
        this.epsilonSlider.setOnUserAction(this.subController);
        this.deltaInput.setOnTextChange(this.subController);
        this.epsilonInput.setOnTextChange(this.subController);
        this.subController.add(this.deltaSlider);
        this.subController.add(this.epsilonSlider);
        this.subController.add(this.deltaInput);
        this.subController.add(this.epsilonInput);
        if (this.xInput != null) {
            this.xSlider.setOnUserAction(this.subController);
            this.xInput.setOnTextChange(this.subController);
            this.subController.add(this.xSlider);
            this.subController.add(this.xInput);
        }

        if (this.limitInput != null) {
            this.limitSlider.setOnUserAction(this.subController);
            this.limitInput.setOnTextChange(this.subController);
            this.subController.add(this.limitSlider);
            this.subController.add(this.limitInput);
        }

        super.setUpCanvas();
        this.canvas.add(this.graph);
        this.canvas.add(ds);
    }

    protected void doLoadExample(String example) {
        int pos = example.indexOf(";");
        double[] limits = new double[]{-5.0D, 5.0D, -5.0D, 5.0D};
        if (pos > 0) {
            String limitsText = example.substring(pos + 1);
            example = example.substring(0, pos);
            StringTokenizer toks = new StringTokenizer(limitsText, " ,");
            double[] nums = new double[toks.countTokens()];

            int i;
            for(i = 0; i < nums.length; ++i) {
                try {
                    nums[i] = Double.valueOf(toks.nextToken());
                } catch (Exception var10) {
                    nums[i] = 0.0D / 0.0;
                }
            }

            for(i = 0; i < 4; ++i) {
                if (nums.length >= i && !Double.isNaN(nums[i])) {
                    limits[i] = nums[i];
                }
            }

            if (nums.length > 4 && !Double.isNaN(nums[4])) {
                this.xValue.setVal(nums[4]);
            } else {
                this.xValue.setVal((limits[0] + limits[1]) / 2.0D);
            }

            if (nums.length > 5 && !Double.isNaN(nums[5])) {
                this.limitValue.setVal(nums[5]);
            } else {
                this.limitValue.setVal((limits[0] + limits[1]) / 2.0D);
            }

            if (nums.length > 8 && !Double.isNaN(nums[8])) {
                this.epsilonSlider.setMax(new Constant(nums[8]));
            } else {
                this.epsilonSlider.setMax(new Constant(Math.abs(limits[2] - limits[3]) / 2.0D));
            }

            if (nums.length > 9 && !Double.isNaN(nums[9])) {
                this.deltaSlider.setMax(new Constant(nums[9]));
            } else {
                this.deltaSlider.setMax(new Constant(Math.abs(limits[0] - limits[1]) / 2.0D));
            }

            if (nums.length > 6 && !Double.isNaN(nums[6])) {
                this.epsilonInput.setVal(nums[6]);
                this.epsilonSlider.setVal(nums[6]);
            } else {
                this.epsilonInput.setVal(Math.abs(limits[2] - limits[3]) / 8.0D);
                this.epsilonSlider.setVal(Math.abs(limits[2] - limits[3]) / 8.0D);
            }

            if (nums.length > 7 && !Double.isNaN(nums[7])) {
                this.deltaInput.setVal(nums[7]);
                this.deltaSlider.setVal(nums[7]);
            } else {
                this.deltaInput.setVal(Math.abs(limits[0] - limits[1]) / 8.0D);
                this.deltaSlider.setVal(Math.abs(limits[0] - limits[1]) / 8.0D);
            }
        }

        if (this.functionInput != null) {
            this.functionInput.setText(example);
        } else {
            try {
                Function f = new SimpleFunction(this.parser.parse(example), this.xVar);
                ((WrapperFunction)this.func).setFunction(f);
            } catch (ParseError var9) {
            }
        }

        CoordinateRect coords = this.canvas.getCoordinateRect(0);
        coords.setLimits(limits);
        coords.setRestoreBuffer();
        this.mainController.compute();
    }

    public static void main(String[] a) {
        JFrame f = new JFrame();
        Applet app = new EpsilonDelta();
        app.init();
        f.getContentPane().add(app);
        f.pack();
        f.setSize(new Dimension(500, 500));
        f.setVisible(true);
    }
}
