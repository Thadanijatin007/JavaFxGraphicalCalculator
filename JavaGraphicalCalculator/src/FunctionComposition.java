
import edu.awt.Controller;
import edu.awt.ExpressionInput;
import edu.data.Constant;
import edu.data.Function;
import edu.data.ParseError;
import edu.data.Parser;
import edu.data.SimpleFunction;
import edu.data.Value;
import edu.data.ValueMath;
import edu.data.Variable;
import edu.draw.CoordinateRect;
import edu.draw.DisplayCanvas;
import edu.draw.DraggablePoint;
import edu.draw.DrawBorder;
import edu.draw.DrawGeometric;
import edu.draw.DrawString;
import edu.draw.Graph1D;
import edu.draw.Grid;
import edu.draw.Panner;
import edu.draw.TangentLine;
import edu.functions.ExpressionFunction;
import edu.functions.TableFunction;
import edu.functions.TableFunctionGraph;
import edu.functions.WrapperFunction;
import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.StringTokenizer;
import javax.swing.JFrame;

public class FunctionComposition extends GenericGraphApplet implements ActionListener, ItemListener {
    Button zoomInButton;
    Button zoomOutButton;
    Button restoreButton;
    Button equalizeButton;
    Button fComputeButton;
    Button gComputeButton;
    Variable pointX;
    Checkbox fCheck;
    Checkbox gCheck;
    ExpressionInput fInput;
    ExpressionInput gInput;
    Function fFunc;
    Function gFunc;
    Graph1D fGraph;
    Graph1D gGraph;
    TableFunction fTable;
    TableFunction gTable;
    TableFunctionGraph fTableGraph;
    TableFunctionGraph gTableGraph;
    boolean fTableShown;
    boolean gTableShown;
    String fSaveText;
    String gSaveText;
    WrapperFunction fWrapper;
    WrapperFunction gWrapper;

    public FunctionComposition() {
    }

    public void setUpMainPanel() {
        this.mainController = new Controller();
        this.defaultFrameSize = new int[]{606, 306};
        Color textColor = this.getColorParam("TextColor", Color.black);
        Color canvasBackground = this.getColorParam("CanvasColor", Color.white);
        boolean useInputs = !"no".equalsIgnoreCase(this.getParameter("UseFunctionInput", "yes"));
        double[] gap = this.getNumericParam("Insets");
        if (gap != null && gap.length != 0 && !(gap[0] < 0.0D) && !(gap[0] > 50.0D)) {
            this.mainPanel.setInsetGap((int)Math.round(gap[0]));
        } else {
            this.mainPanel.setInsetGap(3);
        }

        this.parser = new Parser((Parser)null, 0);
        this.setUpParser();
        this.setUpExampleMenu();
        this.setUpTopPanel();
        Color color = this.getColorParam("BackgroundColor", Color.gray);
        this.mainPanel.setBackground(color);
        color = this.getColorParam("ForegroundColor", Color.black);
        this.mainPanel.setForeground(color);
        double[] limits = this.getNumericParam("Limits");
        if (limits == null || limits.length < 4) {
            limits = new double[]{-5.0D, 5.0D, -5.0D, 5.0D};
        }

        this.canvas = new DisplayCanvas();
        this.mainPanel.add(this.canvas, "Center");
        this.canvas.setBackground(canvasBackground);
        if (!"no".equalsIgnoreCase(this.getParameter("UseMouseZoom", "no"))) {
            this.canvas.setHandleMouseZooms(true);
        }

        if (!"no".equalsIgnoreCase(this.getParameter("UseOffscreenCanvas", "yes"))) {
            this.canvas.setUseOffscreenCanvas(true);
        }

        this.canvas.addCoordinateRect(new CoordinateRect(limits[0], limits[1], limits[2], limits[3]), 0.0D, 0.3333333333333333D, 0.0D, 1.0D, (Color)null);
        this.canvas.addCoordinateRect(new CoordinateRect(limits[0], limits[1], limits[2], limits[3]), 0.3333333333333333D, 0.6666666666666666D, 0.0D, 1.0D, (Color)null);
        this.canvas.addCoordinateRect(new CoordinateRect(limits[0], limits[1], limits[2], limits[3]), 0.6666666666666666D, 1.0D, 0.0D, 1.0D, (Color)null);
        if (!"no".equalsIgnoreCase(this.getParameter("UseGrid", "no"))) {
            color = this.getColorParam("GridColor");
            Grid g = new Grid();
            if (color != null) {
                g.setColor(color);
            }

            this.canvas.add(g, 0);
            g = new Grid();
            if (color != null) {
                g.setColor(color);
            }

            this.canvas.add(g, 1);
            g = new Grid();
            if (color != null) {
                g.setColor(color);
            }

            this.canvas.add(g, 2);
        }

        this.canvas.add(this.makeAxes(), 0);
        this.canvas.add(this.makeAxes(), 1);
        this.canvas.add(this.makeAxes(), 2);
        this.fSaveText = this.getParameter("Function", " 3 - " + this.xVar.getName() + "^2/2");
        this.gSaveText = this.getParameter("SecondFunction", " sin(" + this.xVar.getName() + ")");
        if (useInputs) {
            this.fInput = new ExpressionInput(this.fSaveText, this.parser);
            this.gInput = new ExpressionInput(this.gSaveText, this.parser);
            this.fFunc = this.fInput.getFunction(this.xVar);
            this.gFunc = this.gInput.getFunction(this.xVar);
        } else {
            this.fFunc = new SimpleFunction(this.parser.parse(this.fSaveText), this.xVar);
            this.gFunc = new SimpleFunction(this.parser.parse(this.gSaveText), this.xVar);
        }

        this.fGraph = new Graph1D(this.fFunc);
        this.gGraph = new Graph1D(this.gFunc);
        this.fWrapper = new WrapperFunction(this.fFunc);
        this.fWrapper.setName("f");
        this.gWrapper = new WrapperFunction(this.gFunc);
        this.gWrapper.setName("g");
        Parser p1 = new Parser();
        p1.add(this.fWrapper);
        p1.add(this.gWrapper);
        ExpressionFunction comp = new ExpressionFunction("h", new String[]{"x"}, "g(f(" + this.xVar.getName() + "))", p1);
        Graph1D compositionGraph = new Graph1D(comp);
        this.fTableShown = this.gTableShown = false;
        String tf = this.getParameter("TableFunction");
        if (tf != null) {
            try {
                this.fTable = this.parseTableFuncDef(tf);
            } catch (Exception var43) {
                tf = null;
            }
        }

        if (tf == null) {
            this.fTable = new TableFunction();
            this.fTable.addIntervals(6, -5.0D, 5.0D);
        }

        this.fTableGraph = new TableFunctionGraph(this.fTable);
        this.fTableGraph.setInteractive(true);
        if (this.getParameter("Function") == null && tf != null) {
            this.fGraph.setVisible(false);
            this.fTableShown = true;
            this.fWrapper.setFunction(this.fTable);
            if (this.fInput != null) {
                this.fInput.setEnabled(false);
                this.fInput.setThrowErrors(false);
                this.fInput.setText("Drag points to modify function.");
            }
        } else {
            this.fTableGraph.setVisible(false);
        }

        tf = this.getParameter("SecondTableFunction");
        if (tf != null) {
            try {
                this.gTable = this.parseTableFuncDef(tf);
            } catch (Exception var42) {
                tf = null;
            }
        }

        if (tf == null) {
            this.gTable = new TableFunction();
            this.gTable.addIntervals(6, -5.0D, 5.0D);
        }

        this.gTableGraph = new TableFunctionGraph(this.gTable);
        this.gTableGraph.setInteractive(true);
        if (this.getParameter("SecondFunction") == null && tf != null) {
            this.gGraph.setVisible(false);
            this.gTableShown = true;
            this.gWrapper.setFunction(this.gTable);
            if (this.gInput != null) {
                this.gInput.setEnabled(false);
                this.gInput.setThrowErrors(false);
                this.gInput.setText("Drag points to modify function.");
            }
        } else {
            this.gTableGraph.setVisible(false);
        }

        DraggablePoint point = new DraggablePoint(1);
        Color pointColor1 = this.getColorParam("PointColor1", Color.red);
        Color pointColor2 = this.getColorParam("PointColor2", new Color(0, 200, 0));
        Color pointColor3 = this.getColorParam("PointColor3", new Color(100, 100, 255));
        point.setColor(pointColor1);
        point.clampY(0.0D);
        point.setLocation(1.0D, 0.0D);
        this.canvas.add(point, 0);
        this.pointX = point.getXVar();
        Value fOfX = new ValueMath(this.fWrapper, this.pointX);
        Value gOfFOfX = new ValueMath(this.gWrapper, fOfX);
        DrawGeometric line1 = new DrawGeometric(0, this.pointX, new Constant(0.0D), this.pointX, fOfX);
        line1.setColor(pointColor1);
        this.canvas.add(line1, 0);
        DrawGeometric line2 = new DrawGeometric(0, this.pointX, fOfX, new Constant(0.0D), fOfX);
        line2.setColor(pointColor2);
        this.canvas.add(line2, 0);
        DrawGeometric line3 = new DrawGeometric(0, fOfX, new Constant(0.0D), fOfX, gOfFOfX);
        line3.setColor(pointColor2);
        this.canvas.add(line3, 1);
        DrawGeometric line4 = new DrawGeometric(0, fOfX, gOfFOfX, new Constant(0.0D), gOfFOfX);
        line4.setColor(pointColor3);
        this.canvas.add(line4, 1);
        DrawGeometric line5 = new DrawGeometric(0, this.pointX, new Constant(0.0D), this.pointX, gOfFOfX);
        line5.setColor(pointColor1);
        this.canvas.add(line5, 2);
        DrawGeometric line6 = new DrawGeometric(0, this.pointX, gOfFOfX, new Constant(0.0D), gOfFOfX);
        line6.setColor(pointColor3);
        this.canvas.add(line6, 2);
        line1.setLineWidth(2);
        line2.setLineWidth(2);
        line3.setLineWidth(2);
        line4.setLineWidth(2);
        line5.setLineWidth(2);
        line6.setLineWidth(2);
        Color gc = this.getColorParam("GraphColor", Color.magenta);
        this.fGraph.setColor(gc);
        this.gGraph.setColor(gc);
        this.fTableGraph.setColor(gc);
        this.gTableGraph.setColor(gc);
        compositionGraph.setColor(gc);
        this.canvas.add(this.fGraph, 0);
        this.canvas.add(this.fTableGraph, 0);
        this.canvas.add(this.gGraph, 1);
        this.canvas.add(this.gTableGraph, 1);
        this.canvas.add(compositionGraph, 2);
        TangentLine tangent1 = null;
        TangentLine tangent2 = null;
        TangentLine tangent3 = null;
        DrawString ts1 = null;
        DrawString ts2 = null;
        DrawString ts3 = null;
        if (!"no".equalsIgnoreCase(this.getParameter("ShowTangents", "no"))) {
            Color tangentColor = this.getColorParam("TangentColor", Color.gray);
            tangent1 = new TangentLine(this.pointX, this.fWrapper);
            tangent1.setColor(tangentColor);
            this.canvas.add(tangent1, 0);
            tangent2 = new TangentLine(fOfX, this.gWrapper);
            tangent2.setColor(tangentColor);
            this.canvas.add(tangent2, 1);
            tangent3 = new TangentLine(this.pointX, comp);
            tangent3.setColor(tangentColor);
            this.canvas.add(tangent3, 2);
            if ("yes".equalsIgnoreCase(this.getParameter("ShowSlopes", "yes"))) {
                ts1 = new DrawString("slope = #", 2, new Value[]{new ValueMath(this.fWrapper.derivative(1), this.pointX)});
                ts1.setColor(textColor);
                ts1.setNumSize(6);
                this.canvas.add(ts1, 0);
                ts2 = new DrawString("slope = #", 2, new Value[]{new ValueMath(this.gWrapper.derivative(1), fOfX)});
                ts2.setColor(textColor);
                ts2.setNumSize(6);
                this.canvas.add(ts2, 1);
                ts3 = new DrawString("slope = #", 2, new Value[]{new ValueMath(comp.derivative(1), this.pointX)});
                ts3.setColor(textColor);
                ts3.setNumSize(6);
                this.canvas.add(ts3, 2);
            }
        }

        DrawString ds1;
        if ("yes".equalsIgnoreCase(this.getParameter("ShowFunctionNames", "yes"))) {
            ds1 = new DrawString("y=f(" + this.xVar.getName() + ")");
            ds1.setColor(textColor);
            this.canvas.add(ds1, 0);
            ds1 = new DrawString("y=g(" + this.xVar.getName() + ")");
            ds1.setColor(textColor);
            this.canvas.add(ds1, 1);
            ds1 = new DrawString("y=g(f(" + this.xVar.getName() + "))");
            ds1.setColor(textColor);
            this.canvas.add(ds1, 2);
        }

        ds1 = null;
        DrawString ds2 = null;
        DrawString ds3 = null;
        if ("yes".equalsIgnoreCase(this.getParameter("ShowCoordinates", "yes"))) {
            ds1 = new DrawString("f(#) = #", 9, new Value[]{this.pointX, fOfX});
            ds1.setNumSize(6);
            ds1.setColor(textColor);
            ds1.setBackgroundColor(canvasBackground);
            this.canvas.add(ds1, 0);
            ds2 = new DrawString("g(#) = #", 9, new Value[]{fOfX, gOfFOfX});
            ds2.setNumSize(6);
            ds2.setColor(textColor);
            ds2.setBackgroundColor(canvasBackground);
            this.canvas.add(ds2, 1);
            ds3 = new DrawString("g(f(#)) = #", 9, new Value[]{this.pointX, gOfFOfX});
            ds3.setNumSize(6);
            ds3.setColor(textColor);
            ds3.setBackgroundColor(canvasBackground);
            this.canvas.add(ds3, 2);
        }

        if (!"no".equalsIgnoreCase(this.getParameter("UsePanner", "no"))) {
            this.canvas.add(new Panner(), 0);
            this.canvas.add(new Panner(), 1);
            this.canvas.add(new Panner(), 2);
        }

        double[] bw = this.getNumericParam("BorderWidth");
        int borderWidth;
        if (bw != null && bw.length != 0 && !(bw[0] > 25.0D)) {
            borderWidth = (int)Math.round(bw[0]);
        } else {
            borderWidth = 1;
        }

        if (borderWidth > 0) {
            Color bc = this.getColorParam("BorderColor", Color.black);
            this.canvas.add(new DrawBorder(bc, borderWidth), 0);
            this.canvas.add(new DrawBorder(bc, borderWidth), 1);
            this.canvas.add(new DrawBorder(bc, borderWidth), 2);
        }

        if (useInputs) {
            Panel bottom = new Panel();
            bottom.setLayout(new BorderLayout(3, 3));
            bottom.setBackground(this.getColorParam("PanelBackground", Color.lightGray));
            this.mainPanel.add(bottom, "South");
            Panel left = new Panel();
            left.setLayout(new GridLayout(0, 1));
            bottom.add(left, "Center");
            Panel right = new Panel();
            right.setLayout(new GridLayout(0, 2));
            bottom.add(right, "East");
            Panel fPanel = new Panel();
            fPanel.setLayout(new BorderLayout());
            fPanel.add(new Label(" f(" + this.xVar.getName() + ") = "), "West");
            fPanel.add(this.fInput, "Center");
            Panel fp = new Panel();
            fp.setLayout(new GridLayout(1, 2));
            this.fCheck = new Checkbox("Use Mouse");
            if (this.fTableShown) {
                this.fCheck.setState(true);
            }

            this.fCheck.addItemListener(this);
            fp.add(this.fCheck);
            this.fComputeButton = new Button("New f(" + this.xVar.getName() + ")");
            this.fComputeButton.addActionListener(this);
            fp.add(this.fComputeButton);
            fPanel.add(fp, "East");
            left.add(fPanel);
            Panel gPanel = new Panel();
            gPanel.setLayout(new BorderLayout());
            gPanel.add(new Label(" g(" + this.xVar.getName() + ") = "), "West");
            gPanel.add(this.gInput, "Center");
            Panel gp = new Panel();
            gp.setLayout(new GridLayout(1, 2));
            this.gCheck = new Checkbox("Use Mouse");
            if (this.gTableShown) {
                this.gCheck.setState(true);
            }

            this.gCheck.addItemListener(this);
            gp.add(this.gCheck);
            this.gComputeButton = new Button("New g(" + this.xVar.getName() + ")");
            this.gComputeButton.addActionListener(this);
            gp.add(this.gComputeButton);
            gPanel.add(gp, "East");
            left.add(gPanel);
            this.zoomInButton = new Button("Zoom In");
            right.add(this.zoomInButton);
            this.zoomInButton.addActionListener(this);
            this.zoomOutButton = new Button("Zoom Out");
            right.add(this.zoomOutButton);
            this.zoomOutButton.addActionListener(this);
            this.equalizeButton = new Button("EqualizeAxes");
            this.equalizeButton.addActionListener(this);
            right.add(this.equalizeButton);
            this.restoreButton = new Button("Restore Limits");
            right.add(this.restoreButton);
            this.restoreButton.addActionListener(this);
            this.fInput.setOnUserAction(this.mainController);
            this.gInput.setOnUserAction(this.mainController);
            this.mainController.add(this.fInput);
            this.mainController.add(this.gInput);
        }

        this.fTableGraph.setOnDrag(this.mainController);
        this.gTableGraph.setOnDrag(this.mainController);
        this.mainController.add(this.canvas);
        this.mainController.setErrorReporter(this.canvas);
        Controller lineController = new Controller();
        this.mainController.add(lineController);
        point.setOnUserAction(lineController);
        lineController.add(point);
        lineController.add(line1);
        lineController.add(line2);
        lineController.add(line3);
        lineController.add(line4);
        lineController.add(line5);
        lineController.add(line6);
        if (ds1 != null) {
            lineController.add(ds1);
            lineController.add(ds2);
            lineController.add(ds3);
        }

        if (tangent1 != null) {
            lineController.add(tangent1);
            lineController.add(tangent2);
            lineController.add(tangent3);
        }

        if (ts1 != null) {
            lineController.add(ts1);
            lineController.add(ts2);
            lineController.add(ts3);
        }

    }

    public void itemStateChanged(ItemEvent evt) {
        Object src = evt.getSource();
        boolean check;
        if (src == this.fCheck) {
            check = this.fCheck.getState();
            if (check == this.fTableShown) {
                return;
            }

            this.fTableShown = check;
            this.fGraph.setVisible(!this.fTableShown);
            this.fTableGraph.setVisible(this.fTableShown);
            if (this.fTableShown) {
                this.fWrapper.setFunction(this.fTable);
                this.fSaveText = this.fInput.getText();
                this.fInput.setText("Drag points to modify function.");
                this.fInput.setThrowErrors(false);
                this.fInput.setEnabled(false);
            } else {
                this.fWrapper.setFunction(this.fFunc);
                this.fInput.setText(this.fSaveText);
                this.fInput.setThrowErrors(true);
                this.fInput.setEnabled(true);
            }

            this.mainController.compute();
        } else if (src == this.gCheck) {
            check = this.gCheck.getState();
            if (check == this.gTableShown) {
                return;
            }

            this.gTableShown = check;
            this.gGraph.setVisible(!this.gTableShown);
            this.gTableGraph.setVisible(this.gTableShown);
            if (this.gTableShown) {
                this.gWrapper.setFunction(this.gTable);
                this.gSaveText = this.gInput.getText();
                this.gInput.setText("Drag points to modify function.");
                this.gInput.setThrowErrors(false);
                this.gInput.setEnabled(false);
            } else {
                this.gWrapper.setFunction(this.gFunc);
                this.gInput.setText(this.gSaveText);
                this.gInput.setThrowErrors(true);
                this.gInput.setEnabled(true);
            }

            this.mainController.compute();
        }

    }

    public void actionPerformed(ActionEvent evt) {
        Object src = evt.getSource();
        if (src == this.zoomInButton) {
            this.canvas.getCoordinateRect(0).zoomIn();
            this.canvas.getCoordinateRect(1).zoomIn();
            this.canvas.getCoordinateRect(2).zoomIn();
        } else if (src == this.zoomOutButton) {
            this.canvas.getCoordinateRect(0).zoomOut();
            this.canvas.getCoordinateRect(1).zoomOut();
            this.canvas.getCoordinateRect(2).zoomOut();
        } else if (src == this.restoreButton) {
            this.canvas.getCoordinateRect(0).restore();
            this.canvas.getCoordinateRect(1).restore();
            this.canvas.getCoordinateRect(2).restore();
        } else if (src == this.equalizeButton) {
            this.canvas.getCoordinateRect(0).equalizeAxes();
            this.canvas.getCoordinateRect(1).equalizeAxes();
            this.canvas.getCoordinateRect(2).equalizeAxes();
        } else {
            int ct;
            double val;
            int i;
            if (src == this.fComputeButton) {
                if (this.fTableShown) {
                    ct = this.fTable.getPointCount();
                    if (!(0.0D < this.canvas.getCoordinateRect(0).getYmin()) && !(0.0D > this.canvas.getCoordinateRect(0).getYmax())) {
                        val = 0.0D;
                    } else {
                        val = this.canvas.getCoordinateRect(0).getYmin();
                    }

                    for(i = 0; i < ct; ++i) {
                        this.fTable.setY(i, val);
                    }
                }

                this.mainController.compute();
            } else if (src == this.gComputeButton) {
                if (this.gTableShown) {
                    ct = this.gTable.getPointCount();
                    if (!(0.0D < this.canvas.getCoordinateRect(1).getYmin()) && !(0.0D > this.canvas.getCoordinateRect(1).getYmax())) {
                        val = 0.0D;
                    } else {
                        val = this.canvas.getCoordinateRect(1).getYmin();
                    }

                    for(i = 0; i < ct; ++i) {
                        this.gTable.setY(i, val);
                    }
                }

                this.mainController.compute();
            } else {
                super.actionPerformed(evt);
            }
        }

    }

    protected void doLoadExample(String example) {
        int pos = example.indexOf(";");
        if (pos == -1) {
            System.out.println("Illegal example -- must have two functions");
        } else {
            String example2 = example.substring(pos + 1);
            example = example.substring(0, pos).trim();
            pos = example2.indexOf(";");
            double[] limits = new double[]{-5.0D, 5.0D, -5.0D, 5.0D};
            if (pos > 0) {
                String nums = example2.substring(pos + 1);
                example2 = example2.substring(0, pos).trim();
                StringTokenizer toks = new StringTokenizer(nums, " ,");
                if (toks.countTokens() >= 4) {
                    for(int i = 0; i < 4; ++i) {
                        try {
                            Double d = Double.valueOf(toks.nextToken());
                            limits[i] = d;
                        } catch (NumberFormatException var14) {
                        }
                    }
                }

                if (toks.hasMoreTokens()) {
                    try {
                        double d = Double.valueOf(toks.nextToken());
                        this.pointX.setVal(d);
                    } catch (NumberFormatException var13) {
                    }
                }
            }

            TableFunction tg;
            SimpleFunction g;
            if (example.startsWith("table")) {
                try {
                    tg = this.parseTableFuncDef(example);
                    this.fTable = tg;
                    this.fTableGraph.setFunction(tg);
                    this.fWrapper.setFunction(tg);
                    if (!this.fTableShown) {
                        if (this.fCheck != null) {
                            this.fCheck.setState(true);
                        }

                        this.fGraph.setVisible(false);
                        this.fTableGraph.setVisible(true);
                        this.fTableShown = true;
                        if (this.fInput != null) {
                            this.fSaveText = this.fInput.getText();
                            this.fInput.setText("Drag points to modify function.");
                            this.fInput.setThrowErrors(false);
                            this.fInput.setEnabled(false);
                        }
                    }
                } catch (ParseError var12) {
                    System.out.println("Illegal table function for f(x) in example.");
                }
            } else {
                try {
                    if (this.fInput != null) {
                        this.fInput.setText(example);
                    } else {
                        g = new SimpleFunction(this.parser.parse(example), this.xVar);
                        this.fFunc = g;
                        this.fGraph.setFunction(g);
                        this.fWrapper.setFunction(g);
                    }

                    if (this.fTableShown) {
                        if (this.fCheck != null) {
                            this.fCheck.setState(false);
                        }

                        this.fGraph.setVisible(true);
                        this.fTableGraph.setVisible(false);
                        this.fTableShown = false;
                        if (this.fInput != null) {
                            this.fInput.setThrowErrors(true);
                            this.fInput.setEnabled(true);
                        }
                    }
                } catch (ParseError var11) {
                    System.out.println("Parse error for f(x) in example.");
                }
            }

            if (example2.startsWith("table")) {
                try {
                    tg = this.parseTableFuncDef(example2);
                    this.gTable = tg;
                    this.gTableGraph.setFunction(tg);
                    this.gWrapper.setFunction(tg);
                    if (!this.gTableShown) {
                        if (this.gCheck != null) {
                            this.gCheck.setState(true);
                        }

                        this.gGraph.setVisible(false);
                        this.gTableGraph.setVisible(true);
                        this.gTableShown = true;
                        if (this.gInput != null) {
                            this.gSaveText = this.gInput.getText();
                            this.gInput.setText("Drag points to modify function.");
                            this.gInput.setThrowErrors(false);
                            this.gInput.setEnabled(false);
                        }
                    }
                } catch (ParseError var10) {
                    System.out.println("Illegal table function for g(x) in example.");
                }
            } else {
                try {
                    if (this.gInput != null) {
                        this.gInput.setText(example2);
                    } else {
                        g = new SimpleFunction(this.parser.parse(example2), this.xVar);
                        this.gFunc = g;
                        this.gGraph.setFunction(g);
                        this.gWrapper.setFunction(g);
                    }

                    if (this.gTableShown) {
                        if (this.gCheck != null) {
                            this.gCheck.setState(false);
                        }

                        this.gGraph.setVisible(true);
                        this.gTableGraph.setVisible(false);
                        this.gTableShown = false;
                        if (this.gInput != null) {
                            this.gInput.setThrowErrors(true);
                            this.gInput.setEnabled(true);
                        }
                    }
                } catch (ParseError var9) {
                    System.out.println("Parse error for g(x) in example.");
                }
            }

            CoordinateRect coords = this.canvas.getCoordinateRect(0);
            coords.setLimits(limits);
            coords.setRestoreBuffer();
            coords = this.canvas.getCoordinateRect(1);
            coords.setLimits(limits);
            coords.setRestoreBuffer();
            coords = this.canvas.getCoordinateRect(2);
            coords.setLimits(limits);
            coords.setRestoreBuffer();
            this.mainController.compute();
        }
    }

    public static void main(String[] a) {
        JFrame f = new JFrame();
        Applet app = new FunctionComposition();
        app.init();
        f.getContentPane().add(app);
        f.pack();
        f.setSize(new Dimension(500, 500));
        f.setVisible(true);
    }
}
