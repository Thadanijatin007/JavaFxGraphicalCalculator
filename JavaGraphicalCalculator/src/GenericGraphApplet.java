
import edu.awt.Controller;
import edu.awt.ExpressionInput;
import edu.awt.JCMPanel;
import edu.data.ParseError;
import edu.data.Parser;
import edu.data.ParserContext;
import edu.data.Variable;
import edu.draw.Axes;
import edu.draw.DisplayCanvas;
import edu.draw.DrawBorder;
import edu.draw.Grid;
import edu.draw.LimitControlPanel;
import edu.draw.Panner;
import edu.functions.ExpressionFunction;
import edu.functions.SummationParser;
import edu.functions.TableFunction;
import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Label;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

public class GenericGraphApplet extends Applet implements ActionListener, ItemListener {
    protected Parser parser;
    protected JCMPanel mainPanel;
    protected Controller mainController;
    protected DisplayCanvas canvas;
    protected LimitControlPanel limitsPanel;
    protected ExpressionInput functionInput;
    protected Variable xVar;
    protected JCMPanel inputPanel;
    protected JCMPanel exampleMenuPanel;
    protected Button computeButton;
    protected String frameTitle;
    protected int[] defaultFrameSize = new int[]{550, 400};
    protected Hashtable parameterDefaults;
    private Choice exampleMenu;
    private Button loadExampleButton;
    private Button launchButton;
    private String launchButtonName;
    private Frame frame;
    private Vector exampleStrings;
    private String[] colorNames = new String[]{"black", "red", "blue", "green", "yellow", "cyan", "magenta", "gray", "darkgray", "lightgray", "pink", "orange", "white"};
    private Color[] colors;

    public GenericGraphApplet() {
        this.colors = new Color[]{Color.black, Color.red, Color.blue, Color.green, Color.yellow, Color.cyan, Color.magenta, Color.gray, Color.darkGray, Color.lightGray, Color.pink, Color.orange, Color.white};
    }

    public void init() {
        this.setUpParameterDefaults();
        this.frameTitle = "Title";
        int pos;
        if (this.frameTitle == null) {
            this.frameTitle = this.getClass().getName();
            pos = this.frameTitle.lastIndexOf(46);
            if (pos > -1) {
                this.frameTitle = this.frameTitle.substring(pos + 1);
            }
        }

        this.setLayout(new BorderLayout());
        pos = this.getSize().height;
        this.launchButtonName = "Launch Button";
        if ((pos <= 0 || pos >= 100) && this.launchButtonName == null) {
            this.mainPanel = new JCMPanel();

            try {
                this.setUpMainPanel();
                this.add(this.mainPanel, "Center");
            } catch (Exception var4) {
                System.out.println("Error while opening applet:");
                var4.printStackTrace();
                TextArea message = new TextArea("An error occurred while setting up the applet:\n\n");
                message.append(var4.toString());
                this.add(message, "Center");
            }
        } else {
            if (this.launchButtonName == null) {
                this.launchButtonName = "Launch " + this.frameTitle;
            }

            this.launchButton = new Button(this.launchButtonName);
            this.add(this.launchButton, "Center");
            this.launchButton.addActionListener(this);
        }

    }

    protected void setUpMainPanel() {
        this.parser = new Parser((Parser)null, 0);
        this.setUpParser();
        this.setUpExampleMenu();
        double[] gap = this.getNumericParam("Insets");
        if (gap != null && gap.length != 0 && !(gap[0] < 0.0D) && !(gap[0] > 50.0D)) {
            this.mainPanel.setInsetGap((int)Math.round(gap[0]));
        } else {
            this.mainPanel.setInsetGap(3);
        }

        Color color = this.getColorParam("BackgroundColor", Color.gray);
        this.mainPanel.setBackground(color);
        color = this.getColorParam("ForegroundColor", Color.black);
        this.mainPanel.setForeground(color);
        this.canvas = new DisplayCanvas();
        double[] limits = this.getNumericParam("Limits");
        if (limits != null && limits.length >= 4) {
            this.canvas.getCoordinateRect().setLimits(limits);
        }

        String loadDefault;
        if ("yes".equalsIgnoreCase("yes")) {
            loadDefault = null;
            if (loadDefault == null) {
                loadDefault = "x";
            }

            String v = "y";
            this.limitsPanel = new LimitControlPanel(loadDefault + "min", loadDefault + "max", v + "min", v + "max", 0, false);
        }

        this.mainController = this.mainPanel.getController();
        this.setUpBottomPanel();
        this.setUpTopPanel();
        this.setUpCanvas();
        this.addCanvasBorder();
        if (this.limitsPanel != null) {
            this.setUpLimitsPanel();
        }

        loadDefault = this.loadExampleButton == null ? "yes" : "no";
        if (this.exampleStrings != null && this.exampleStrings.size() > 0 && !"no".equalsIgnoreCase(this.getParameter("LoadFirstExample", loadDefault))) {
            this.doLoadExample((String)this.exampleStrings.elementAt(0));
        }

    }

    protected void setUpCanvas() {
        Color color = this.getColorParam("CanvasColor");
        if (color != null) {
            this.canvas.setBackground(color);
        }

        if (!"no".equalsIgnoreCase(this.getParameter("UsePanner", "no"))) {
            this.canvas.add(new Panner());
        }

        if (!"no".equalsIgnoreCase(this.getParameter("UseGrid", "no"))) {
            Grid g = new Grid();
            color = this.getColorParam("GridColor");
            if (color != null) {
                g.setColor(color);
            }

            this.canvas.add(g);
        }

        this.canvas.add(this.makeAxes());
        if (!"no".equalsIgnoreCase(this.getParameter("UseMouseZoom", "no"))) {
            this.canvas.setHandleMouseZooms(true);
        }

        if ("yes".equalsIgnoreCase(this.getParameter("UseOffscreenCanvas", "yes"))) {
            this.canvas.setUseOffscreenCanvas(true);
        }

        this.mainController.setErrorReporter(this.canvas);
        this.mainPanel.add(this.canvas, "Center");
    }

    protected Axes makeAxes() {
        Axes axes = new Axes();
        Color color = this.getColorParam("AxesColor");
        if (color != null) {
            axes.setAxesColor(color);
        }

        color = this.getColorParam("AxesLightColor");
        if (color != null) {
            axes.setLightAxesColor(color);
        }

        String str = this.getParameter("XLabel");
        if (str != null) {
            axes.setXLabel(str);
        }

        str = this.getParameter("YLabel");
        axes.setYLabel(str);
        color = this.getColorParam("LabelColor");
        if (color != null) {
            axes.setLabelColor(color);
        }

        return axes;
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
            this.canvas.add(new DrawBorder(this.getColorParam("BorderColor", Color.black), borderWidth));
        }

    }

    protected void setUpBottomPanel() {
        if ("yes".equalsIgnoreCase(this.getParameter("UseFunctionInput", "yes"))) {
            String func = this.getParameter("Function");
            String varName = this.xVar.getName();
            if (func == null) {
                func = "abs(" + varName + " ) ^ " + varName;
            }

            this.functionInput = new ExpressionInput(func, this.parser);
            this.inputPanel = new JCMPanel();
            this.inputPanel.setBackground(this.getColorParam("PanelBackground", Color.lightGray));
            this.inputPanel.add(this.functionInput, "Center");
            String flabel;
            if ("yes".equalsIgnoreCase(this.getParameter("UseComputeButton", "yes"))) {
                flabel = this.getParameter("ComputeButtonName", "New Function");
                this.computeButton = new Button(flabel);
                this.inputPanel.add(this.computeButton, "East");
                this.computeButton.addActionListener(this);
            }

            flabel = this.getParameter("FunctionLabel");
            if (flabel == null) {
                flabel = " f(" + varName + ") = ";
            }

            if (!"none".equalsIgnoreCase(flabel)) {
                this.inputPanel.add(new Label(flabel), "West");
            }

            this.mainPanel.add(this.inputPanel, "South");
            this.functionInput.setOnUserAction(this.mainPanel.getController());
        }

    }

    protected void setUpLimitsPanel() {
        this.limitsPanel.addCoords(this.canvas);
        if (!"no".equalsIgnoreCase(this.getParameter("TwoLimitsColumns", "no"))) {
            this.limitsPanel.setUseTwoColumnsIfPossible(true);
        }

        int buttons = 0;
        if ("yes".equalsIgnoreCase(this.getParameter("UseSetLimitsButton", "yes"))) {
            buttons |= 1;
        }

        if (!"no".equalsIgnoreCase(this.getParameter("UseZoomButtons", "no"))) {
            buttons |= 12;
        }

        if (!"no".equalsIgnoreCase(this.getParameter("UseEqualizeButton", "no"))) {
            buttons |= 2;
        }

        if (!"no".equalsIgnoreCase(this.getParameter("UseRestoreButton", "no"))) {
            buttons |= 32;
        }

        if (buttons != 0) {
            this.limitsPanel.addButtons(buttons);
        }

        this.limitsPanel.setBackground(this.getColorParam("PanelBackground", Color.lightGray));
        if (!"yes".equalsIgnoreCase(this.getParameter("LimitsOnLeft", "no"))) {
            this.mainPanel.add(this.limitsPanel, "East");
        } else {
            this.mainPanel.add(this.limitsPanel, "West");
        }

        this.limitsPanel.setErrorReporter(this.canvas);
    }

    protected void setUpTopPanel() {
        if (this.exampleMenuPanel != null) {
            this.mainPanel.add(this.exampleMenuPanel, "North");
        } else {
            String title = this.getParameter("PanelTitle");
            if (title != null) {
                Label titleLabel = new Label(title, 1);
                titleLabel.setForeground(this.getColorParam("TitleForeground", new Color(200, 0, 0)));
                titleLabel.setBackground(this.getColorParam("TitleBackground", Color.lightGray));
                titleLabel.setFont(new Font("Serif", 0, 14));
                this.mainPanel.add(titleLabel, "North");
            }
        }

    }

    protected void setUpExampleMenu() {
        Vector strings = new Vector();
        Vector names = new Vector();
        int ct = 0;
        String paramName = "Example";
        String param = this.getParameter(paramName);
        if (param == null) {
            ++ct;
            paramName = "Example" + ct;
            param = this.getParameter(paramName);
        }

        while(param != null) {
            int pos = param.indexOf(59);
            if (pos < 0) {
                strings.addElement(param);
                names.addElement(param);
            } else {
                strings.addElement(param.substring(pos + 1));
                names.addElement(param.substring(0, pos));
            }

            ++ct;
            paramName = "Example" + ct;
            param = this.getParameter(paramName);
        }

        if (strings.size() != 0) {
            this.exampleStrings = strings;
            this.exampleStrings.trimToSize();
            this.exampleMenuPanel = new JCMPanel();
            if ("yes".equalsIgnoreCase(this.getParameter("UseLoadButton", "yes"))) {
                this.loadExampleButton = new Button("Load Example: ");
                this.loadExampleButton.setBackground(Color.lightGray);
                this.loadExampleButton.addActionListener(this);
            }

            Object list;
            if (names.size() == 1) {
                list = new Label((String)names.elementAt(0), 1);
            } else {
                this.exampleMenu = new Choice();
                list = this.exampleMenu;

                for(int i = 0; i < names.size(); ++i) {
                    this.exampleMenu.add((String)names.elementAt(i));
                }

                if (this.loadExampleButton == null) {
                    this.exampleMenu.addItemListener(this);
                }
            }

            ((Component)list).setBackground(Color.white);
            this.exampleMenuPanel.add((Component)list, "Center");
            if (this.loadExampleButton != null) {
                this.exampleMenuPanel.add(this.loadExampleButton, "West");
            }

        }
    }

    protected void setUpParser() {
        if ("yes".equalsIgnoreCase(this.getParameter("StandardFunctions", "yes"))) {
            this.parser.addOptions(1024);
        }

        if ("yes".equalsIgnoreCase(this.getParameter("Booleans", "yes"))) {
            this.parser.addOptions(32);
        }

        if (!"no".equalsIgnoreCase(this.getParameter("OptionalStars", "no"))) {
            this.parser.addOptions(2);
        }

        if (!"no".equalsIgnoreCase(this.getParameter("OptionalParens", "no"))) {
            this.parser.addOptions(512);
        }

        if (!"no".equalsIgnoreCase(this.getParameter("Factorials", "no"))) {
            this.parser.addOptions(64);
        }

        if ("yes".equalsIgnoreCase(this.getParameter("Summations", "yes"))) {
            this.parser.add(new SummationParser());
        }

        String str = this.getParameter("Define");
        if (str != null) {
            this.define(str);
        }

        int ct = 1;

        while(true) {
            str = this.getParameter("Define" + ct);
            if (str == null) {
                this.xVar = new Variable(this.getParameter("Variable", "x"));
                this.parser.add(this.xVar);
                return;
            }

            this.define(str);
            ++ct;
        }
    }

    protected void doLoadExample(String example) {
    }

    protected void setUpParameterDefaults() {
    }

    public String getParameter(String paramName) {
        return null;
    }

    protected String getParameter(String paramName, String defaultValue) {
        String val = this.getParameter(paramName);
        return val == null ? defaultValue : val;
    }

    protected double[] getNumericParam(String paramName) {
        return this.getNumericParam(paramName, (double[])null);
    }

    protected double[] getNumericParam(String paramName, double[] defaults) {
        String data = this.getParameter(paramName);
        if (data == null) {
            return defaults;
        } else {
            StringTokenizer tokenizer = new StringTokenizer(data, " \t,;");
            int count = tokenizer.countTokens();
            if (count == 0) {
                return defaults;
            } else {
                double[] numbers = new double[count];

                for(int i = 0; i < count; ++i) {
                    try {
                        Double d =  Double.valueOf(tokenizer.nextToken());
                        numbers[i] = d;
                    } catch (NumberFormatException var9) {
                        return defaults;
                    }
                }

                return numbers;
            }
        }
    }

    protected Color getColorParam(String data) {
        return this.getColorParam(data, (Color)null);
    }

    protected Color getColorParam(String paramName, Color defaultColor) {
        String data = this.getParameter(paramName);
        if (data != null && data.trim().length() != 0) {
            data = data.trim();
            if (Character.isLetter(data.charAt(0))) {
                for(int i = 0; i < this.colorNames.length; ++i) {
                    if (data.equalsIgnoreCase(this.colorNames[i])) {
                        return this.colors[i];
                    }
                }

                return defaultColor;
            } else {
                double[] nums = this.getNumericParam(paramName, (double[])null);
                if (nums != null && nums.length >= 3) {
                    return !(nums[0] < 0.0D) && !(nums[0] > 255.0D) && !(nums[1] < 0.0D) && !(nums[1] > 255.0D) && !(nums[2] < 0.0D) && !(nums[2] > 255.0D) ? new Color((int)Math.round(nums[0]), (int)Math.round(nums[1]), (int)Math.round(nums[2])) : defaultColor;
                } else {
                    return defaultColor;
                }
            }
        } else {
            return defaultColor;
        }
    }

    public void stop() {
        if (this.canvas != null && this.frame == null) {
            this.canvas.releaseResources();
        }

    }

    public synchronized void destroy() {
        if (this.frame != null) {
            this.frame.dispose();
        }

    }

    private void define(String str) {
        String funcDef = str;

        try {
            int pos = funcDef.indexOf("=");
            if (pos < 0) {
                throw new ParseError("Missing \"=\"", (ParserContext)null);
            } else {
                String def = funcDef.substring(pos + 1).trim();
                funcDef = funcDef.substring(0, pos);
                String name;
                if (def.toLowerCase().startsWith("table")) {
                    name = funcDef;
                    pos = funcDef.indexOf("(");
                    if (pos > 0) {
                        name = funcDef.substring(0, pos).trim();
                    }

                    TableFunction tf = this.parseTableFuncDef(def);
                    tf.setName(name);
                    this.parser.add(tf);
                } else {
                    pos = funcDef.indexOf("(");
                    if (pos < 0) {
                        throw new ParseError("Missing \"(\"", (ParserContext)null);
                    }

                    name = funcDef.substring(0, pos).trim();
                    if (name.length() == 0) {
                        throw new ParseError("Missing function name", (ParserContext)null);
                    }

                    funcDef = funcDef.substring(pos + 1);
                    pos = funcDef.indexOf(")");
                    if (pos < 0) {
                        throw new ParseError("Missing \")\"", (ParserContext)null);
                    }

                    funcDef = funcDef.substring(0, pos).trim();
                    if (funcDef.length() == 0) {
                        throw new ParseError("Missing parameter names", (ParserContext)null);
                    }

                    StringTokenizer toks = new StringTokenizer(funcDef, ",");
                    int ct = toks.countTokens();
                    String[] paramNames = new String[ct];

                    for(int i = 0; i < ct; ++i) {
                        paramNames[i] = toks.nextToken();
                    }

                    new ExpressionFunction(name, paramNames, def, this.parser);
                }

            }
        } catch (ParseError var10) {
            throw new IllegalArgumentException("Error parsing function \"" + str + "\":" + var10.getMessage());
        }
    }

    protected TableFunction parseTableFuncDef(String def) {
        try {
            TableFunction func = new TableFunction();
            StringTokenizer toks = new StringTokenizer(def, " \t,");
            String tok = null;
            if (toks.hasMoreTokens()) {
                tok = toks.nextToken();
                if (tok.equalsIgnoreCase("table") && toks.hasMoreTokens()) {
                    tok = toks.nextToken();
                }
            }

            if ("step".equalsIgnoreCase(tok)) {
                func.setStyle(2);
                if (toks.hasMoreTokens()) {
                    tok = toks.nextToken();
                }
            } else if ("linear".equalsIgnoreCase(tok)) {
                func.setStyle(1);
                if (toks.hasMoreTokens()) {
                    tok = toks.nextToken();
                }
            } else if ("smooth".equalsIgnoreCase(tok) && toks.hasMoreTokens() && toks.hasMoreTokens()) {
                tok = toks.nextToken();
            }

            boolean useIntervals = "intervals".equalsIgnoreCase(tok);
            if (useIntervals && toks.hasMoreTokens()) {
                tok = toks.nextToken();
            }

            double[] nums = new double[toks.countTokens() + 1];

            try {
                nums[0] = Double.valueOf(tok);
            } catch (NumberFormatException var13) {
                throw new ParseError("Unexpected token \"" + tok + "\".", (ParserContext)null);
            }

            int ct;
            try {
                for(ct = 1; ct < nums.length; ++ct) {
                    nums[ct] = Double.valueOf(toks.nextToken());
                }
            } catch (NumberFormatException var14) {
                throw new ParseError("Illegal number.", (ParserContext)null);
            }

            if (useIntervals) {
                ct = nums.length == 0 ? 6 : (int)Math.round(nums[0]);
                if (ct < 1 || ct > 500) {
                    ct = 6;
                }

                double xmin = nums.length < 2 ? -5.0D : nums[1];
                double xmax = nums.length < 3 ? xmin + 10.0D : nums[2];
                if (xmax <= xmin) {
                    throw new ParseError("xmax in table must be greater than xmin", (ParserContext)null);
                }

                func.addIntervals(ct, xmin, xmax);

                for(int i = 3; i < nums.length && i - 3 <= ct; ++i) {
                    if (i - 3 < ct) {
                        func.setY(i - 3, nums[i]);
                    }
                }
            } else {
                if (nums.length < 4) {
                    throw new ParseError("At least two points must be provided for table function.", (ParserContext)null);
                }

                if (nums.length % 2 == 1) {
                    throw new ParseError("Can't define an table function with an odd number of values.", (ParserContext)null);
                }

                for(ct = 0; ct < nums.length / 2; ++ct) {
                    func.addPoint(nums[2 * ct], nums[2 * ct + 1]);
                }
            }

            return func;
        } catch (Exception var15) {
            throw new ParseError("Error while parsing table function: " + var15.getMessage(), (ParserContext)null);
        }
    }

    public void actionPerformed(ActionEvent evt) {
        Object source = evt.getSource();
        if (source == this.computeButton && this.computeButton != null) {
            this.mainController.compute();
        } else if (source == this.launchButton && this.launchButton != null) {
            this.doLaunchButton();
        } else if (source == this.loadExampleButton && this.exampleStrings != null) {
            if (this.exampleStrings.size() == 1) {
                this.doLoadExample((String)this.exampleStrings.elementAt(0));
            } else {
                this.doLoadExample((String)this.exampleStrings.elementAt(this.exampleMenu.getSelectedIndex()));
            }
        }

    }

    public void itemStateChanged(ItemEvent evt) {
        if (evt.getSource() == this.exampleMenu) {
            this.doLoadExample((String)this.exampleStrings.elementAt(this.exampleMenu.getSelectedIndex()));
        }

    }

    private synchronized void doLaunchButton() {
        this.launchButton.setEnabled(false);
        if (this.frame == null) {
            this.frame = new Frame(this.frameTitle);
            this.mainPanel = new JCMPanel();

            try {
                this.setUpMainPanel();
                this.frame.add(this.mainPanel, "Center");
            } catch (Throwable var3) {
                System.out.println("Error while opening window:");
                var3.printStackTrace();
                TextArea message = new TextArea("An error occurred while setting up this window:\n\n");
                message.append(var3.toString());
                this.frame.add(message, "Center");
            }

            this.frame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent evt) {
                    GenericGraphApplet.this.frame.dispose();
                }

                public void windowClosed(WindowEvent evt) {
                    GenericGraphApplet.this.frameClosed();
                }
            });
            double[] frameSize = this.getNumericParam("FrameSize");
            if (frameSize != null && frameSize.length >= 2 && !(frameSize[0] < 100.0D) && !(frameSize[0] > 800.0D) && !(frameSize[1] < 100.0D) && !(frameSize[1] > 600.0D)) {
                this.frame.setSize((int)Math.round(frameSize[0]), (int)Math.round(frameSize[1]));
            } else {
                this.frame.setSize(this.defaultFrameSize[0], this.defaultFrameSize[1]);
            }

            this.frame.setLocation(50, 50);
            this.frame.show();
            this.launchButton.setLabel("Close Window");
            this.launchButton.setEnabled(true);
        } else {
            this.frame.dispose();
        }

    }

    private synchronized void frameClosed() {
        this.frame = null;
        this.launchButton.setLabel(this.launchButtonName);
        this.launchButton.setEnabled(true);
        this.mainPanel = null;
        this.canvas = null;
        this.limitsPanel = null;
        this.inputPanel = null;
        this.exampleMenuPanel = null;
        this.loadExampleButton = null;
        this.computeButton = null;
        this.parser = null;
    }
}
