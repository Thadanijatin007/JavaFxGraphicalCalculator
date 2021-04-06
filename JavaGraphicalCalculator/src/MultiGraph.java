import edu.awt.DisplayLabel;
import edu.awt.ExpressionInput;
import edu.awt.JCMPanel;
import edu.awt.VariableSlider;
import edu.data.*;
import edu.draw.CoordinateRect;
import edu.draw.Graph1D;

import java.applet.Applet;
import java.awt.*;
import java.util.StringTokenizer;
import java.util.Vector;

public class MultiGraph extends GenericGraphApplet {


    private Vector sliders;
    private ExprIn[] inputs;
    private Graph1D[] graphs;
    private int functionCt;

    private Color[] graphColors = {Color.magenta, new Color(0, 180, 0),
            Color.red, new Color(0, 200, 200),
            Color.orange, Color.gray, Color.blue, Color.pink};

    public static void main(String[] a) {
        javax.swing.JFrame f = new javax.swing.JFrame();
        Applet app = new MultiGraph();
        app.init();

        f.getContentPane().add(app);

        f.pack();
        f.setSize(new Dimension(500, 500));
        f.setVisible(true);
    }

    protected void setUpParser() {


        sliders = new Vector();
        int ct = 0;
        String param = getParameter("Parameter");
        if (param == null) {
            ct++;
            param = getParameter("Parameter" + ct);
        }
        while (true) {
            if (param == null)
                break;
            addParameter(param);
            ct++;
            param = getParameter("Parameter" + ct);
        }

        super.setUpParser();


    }

    private void addParameter(String data) {


        double min = -5, max = 5, val = 0;

        data = data.trim();
        int pos = data.indexOf(';');
        if (pos < 0)
            pos = data.indexOf(' ');

        String name;

        if (pos < 0) {

            name = data;
        } else {

            String nums = data.substring(pos + 1);
            name = data.substring(0, pos).trim();
            StringTokenizer toks = new StringTokenizer(nums, " ,\t");
            try {
                if (toks.hasMoreElements())
                    min = Double.valueOf(toks.nextToken());
                if (toks.hasMoreElements())
                    max = Double.valueOf(toks.nextToken());
                if (toks.hasMoreElements())
                    val = Double.valueOf(toks.nextToken());
            } catch (NumberFormatException e) {
                min = -5;
                max = 5;
                val = 0;
            }
        }


        VariableSlider slide = new VariableSlider(name, new Constant(min), new Constant(max), parser);
        slide.setVal(val);

        sliders.addElement(slide);

    }

    private void getColors() {

        Vector vec = new Vector();
        int ct = 0;
        Color c = getColorParam("GraphColor");
        if (c == null) {
            ct++;
            c = getColorParam("GraphColor" + ct);
        }
        while (true) {
            if (c == null)
                break;
            vec.addElement(c);
            ct++;
            c = getColorParam("GraphColor" + ct);
        }
        if (vec.size() > 0) {
            graphColors = new Color[vec.size()];
            for (int i = 0; i < vec.size(); i++)
                graphColors[i] = (Color) vec.elementAt(i);
        }
    }

    private Vector getFunctions() {

        Vector functions = new Vector();
        int ct = 0;
        String c = getParameter("Function");
        if (c == null) {
            ct++;
            c = getParameter("Function" + ct);
        }
        while (true) {
            if (c == null)
                break;
            functions.addElement(c);
            ct++;
            c = getParameter("Function" + ct);
        }
        if (functions.size() == 0)
            functions.addElement(" abs( " + xVar.getName() + ") ^ " + xVar.getName());
        double[] d = getNumericParam("FunctionCount");
        if (d == null || d.length == 0 || d[0] <= 0.5)
            functionCt = functions.size();
        else {
            functionCt = (int) Math.round(d[0]);
            if (functionCt < functions.size()) {
                functionCt = functions.size();
            } else {
                int extra = functionCt - functions.size();
                for (int i = 0; i < extra; i++)
                    functions.addElement("");
            }
        }
        return functions;
    }

    private Panel makeFunctionInput(Vector functions, int funcNum) {


        Graph1D graph = new Graph1D();
        graph.setColor(graphColors[funcNum % graphColors.length]);
        ExprIn in = new ExprIn((String) functions.elementAt(funcNum), parser, graph, xVar);
        in.setOnUserAction(mainController);
        JCMPanel p = new JCMPanel();
        p.add(in, BorderLayout.CENTER);
        String name;
        if (functions.size() > 1)
            name = " " + getParameter("FunctionName", "f") + (funcNum + 1) + "(" + xVar.getName() + ") = ";
        else
            name = " " + getParameter("FunctionName", "f") + "(" + xVar.getName() + ") = ";
        p.add(new Label(name), BorderLayout.WEST);
        if (graphColors.length > 1 && functions.size() > 1)
            p.add(new ColorPatch(graphColors[funcNum % graphColors.length]), BorderLayout.EAST);
        inputs[funcNum] = in;
        return p;
    }

    protected void setUpBottomPanel() {


        boolean funcInput = "yes".equalsIgnoreCase(getParameter("UseFunctionInput", "yes"));

        if (funcInput && "yes".equalsIgnoreCase(getParameter("UseComputeButton", "yes"))) {
            String cname = getParameter("ComputeButtonName", "New Functions");
            computeButton = new Button(cname);
            computeButton.addActionListener(this);
        }
        Panel firstPanel = null;

        getColors();
        Vector functions = getFunctions();

        if (!funcInput && sliders.size() == 0)
            return;

        JCMPanel panel = new JCMPanel();
        if (!"no".equalsIgnoreCase(getParameter("TwoInputColumns", "no")))
            panel.setLayout(new GridLayout(0, 2, 12, 3));
        else
            panel.setLayout(new GridLayout(0, 1, 3, 3));
        panel.setBackground(getColorParam("PanelBackground", Color.lightGray));

        if (funcInput) {
            inputs = new ExprIn[functions.size()];
            for (int i = 0; i < functions.size(); i++) {
                Panel p = makeFunctionInput(functions, i);
                if (firstPanel == null)
                    firstPanel = p;
                panel.add(p);
            }
        } else {
            graphs = new Graph1D[functions.size()];
            for (int i = 0; i < functions.size(); i++) {
                graphs[i] = new Graph1D();
                graphs[i].setColor(graphColors[i % graphColors.length]);
                String def = ((String) functions.elementAt(i)).trim();
                if (def.length() > 0) {
                    Function f = new SimpleFunction(parser.parse(def), xVar);
                    graphs[i].setFunction(f);
                }
            }
        }

        for (int i = 0; i < sliders.size(); i++) {
            JCMPanel p = new JCMPanel();
            VariableSlider slide = (VariableSlider) sliders.elementAt(i);
            p.add(slide, BorderLayout.CENTER);
            p.add(new DisplayLabel("  " + slide.getName() + " = # ", new Value[]{slide.getVariable()}),
                    BorderLayout.EAST);
            panel.add(p);
            slide.setOnUserAction(mainController);
        }

        if (computeButton != null) {
            if (functions.size() == 1)
                firstPanel.add(computeButton, BorderLayout.EAST);
            else if (limitsPanel == null) {
                Panel p = new Panel();
                p.add(computeButton);
                panel.add(p);
            }

        }

        mainPanel.add(panel, BorderLayout.SOUTH);

    }

    protected void setUpLimitsPanel() {
        super.setUpLimitsPanel();
        if (limitsPanel != null && computeButton != null && functionCt != 1)
            limitsPanel.addComponent(computeButton);
    }

    protected void setUpCanvas() {

        super.setUpCanvas();


        if (graphs != null) {
            for (int i = 0; i < graphs.length; i++)
                canvas.add(graphs[i]);
        } else {
            for (int i = 0; i < inputs.length; i++)
                canvas.add(inputs[i].graph);
        }

    }

    protected void doLoadExample(String example) {


        int pos = example.indexOf(";");

        double[] limits = {-5, 5, -5, 5};

        if (pos > 0) {

            String nums = example.substring(0, pos);
            example = example.substring(pos + 1);
            StringTokenizer toks = new StringTokenizer(nums, " ,");
            if (toks.countTokens() >= 4) {
                for (int i = 0; i < 4; i++) {
                    try {
                        Double d = Double.valueOf(toks.nextToken());
                        limits[i] = d.doubleValue();
                    } catch (NumberFormatException e) {
                    }
                }
            }
            int i = 0;
            while (i < sliders.size() && toks.hasMoreElements()) {

                try {
                    double min = (Double.valueOf(toks.nextToken())).doubleValue();
                    double max = (Double.valueOf(toks.nextToken())).doubleValue();
                    double d = (Double.valueOf(toks.nextToken())).doubleValue();
                    VariableSlider slider = ((VariableSlider) sliders.elementAt(i));
                    slider.setMin(new Constant(min));
                    slider.setMax(new Constant(max));
                    slider.setVal(d);
                } catch (Exception e) {
                }
                i++;
            }
        }


        StringTokenizer toks = new StringTokenizer(example, ";");
        int funcNum = 0;
        while (funcNum < functionCt) {
            if (toks.hasMoreElements()) {
                String def = toks.nextToken();
                if (graphs != null) {
                    try {
                        graphs[funcNum].setFunction(new SimpleFunction(parser.parse(def), xVar));
                    } catch (ParseError e) {
                        graphs[funcNum].setFunction(null);
                    }
                } else
                    inputs[funcNum].setText(def);
            } else {
                if (graphs != null)
                    graphs[funcNum].setFunction(null);
                else
                    inputs[funcNum].setText("");
            }
            funcNum++;
        }

        CoordinateRect coords = canvas.getCoordinateRect(0);
        coords.setLimits(limits);
        coords.setRestoreBuffer();
        mainController.compute();

    }

    private static class ColorPatch extends Canvas {

        ColorPatch(Color c) {
            setBackground(c);
        }

        public Dimension getPreferredSize() {
            return new Dimension(25, 10);
        }

        public void paint(Graphics g) {
            g.drawRect(0, 0, getSize().width - 1, getSize().height - 1);
        }
    }

    private static class ExprIn extends ExpressionInput {

        Graph1D graph;
        Function func;

        ExprIn(String definition, Parser p, Graph1D g, Variable v) {
            super(definition, p);
            graph = g;
            func = getFunction(v);
            if (definition.trim().length() > 0)
                graph.setFunction(func);
        }

        public void checkInput() {
            boolean hasChanged = previousContents == null || !previousContents.equals(getText());
            if (!hasChanged)
                return;
            String text = getText().trim();
            if (text.length() == 0) {
                if (graph != null)
                    graph.setFunction(null);
                previousContents = getText();
            } else {
                super.checkInput();
                if (graph != null)
                    graph.setFunction(func);
            }
        }
    }

} 
