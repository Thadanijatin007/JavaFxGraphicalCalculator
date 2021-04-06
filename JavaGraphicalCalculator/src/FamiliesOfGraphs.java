
import edu.awt.DisplayLabel;
import edu.awt.JCMPanel;
import edu.awt.VariableSlider;
import edu.data.Constant;
import edu.data.Function;
import edu.data.ParseError;
import edu.data.SimpleFunction;
import edu.data.Value;
import edu.draw.CoordinateRect;
import edu.draw.Graph1D;
import java.applet.Applet;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.JFrame;

public class FamiliesOfGraphs extends GenericGraphApplet {
    private Function func;
    private Graph1D graph;
    private Vector sliders;

    public FamiliesOfGraphs() {
    }

    protected void setUpParser() {
        this.sliders = new Vector();
        int ct = 0;
        String param = this.getParameter("Parameter");
        if (param == null) {
            ++ct;
            param = this.getParameter("Parameter" + ct);
        }

        while(param != null) {
            this.addParameter(param);
            ++ct;
            param = this.getParameter("Parameter" + ct);
        }

        if (this.sliders.size() == 0) {
            this.addParameter("k");
        }

        super.setUpParser();
        VariableSlider slide = (VariableSlider)this.sliders.elementAt(0);
        String def = this.getParameter("Function", "sin(" + slide.getName() + " * " + this.xVar.getName() + ")");
        this.parameterDefaults = new Hashtable();
        this.parameterDefaults.put("Function", def);
    }

    private void addParameter(String data) {
        double min = -5.0D;
        double max = 5.0D;
        double val = 0.0D;
        data = data.trim();
        int pos = data.indexOf(59);
        if (pos < 0) {
            pos = data.indexOf(32);
        }

        String name;
        if (pos < 0) {
            name = data;
        } else {
            String nums = data.substring(pos + 1);
            name = data.substring(0, pos).trim();
            StringTokenizer toks = new StringTokenizer(nums, " ,\t");

            try {
                if (toks.hasMoreElements()) {
                    min = Double.valueOf(toks.nextToken());
                }

                if (toks.hasMoreElements()) {
                    max = Double.valueOf(toks.nextToken());
                }

                if (toks.hasMoreElements()) {
                    val = Double.valueOf(toks.nextToken());
                }
            } catch (NumberFormatException var13) {
                min = -5.0D;
                max = 5.0D;
                val = 0.0D;
            }
        }

        VariableSlider slide = new VariableSlider(name, new Constant(min), new Constant(max), this.parser);
        slide.setVal(val);
        this.sliders.addElement(slide);
    }

    protected void setUpBottomPanel() {
        super.setUpBottomPanel();
        JCMPanel sliderPanel = new JCMPanel();
        sliderPanel.setLayout(new GridLayout(0, 1, 3, 3));
        sliderPanel.setBackground(this.getColorParam("PanelBackground", Color.lightGray));

        for(int i = 0; i < this.sliders.size(); ++i) {
            JCMPanel p = new JCMPanel();
            VariableSlider slide = (VariableSlider)this.sliders.elementAt(i);
            p.add(slide, "Center");
            p.add(new DisplayLabel("  " + slide.getName() + " = # ", new Value[]{slide.getVariable()}), "East");
            sliderPanel.add(p);
            slide.setOnUserAction(this.mainController);
        }

        if (this.inputPanel != null) {
            this.inputPanel.add(sliderPanel, "South");
        } else {
            this.mainPanel.add(sliderPanel, "South");
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
    }

    protected void doLoadExample(String example) {
        int pos = example.indexOf(";");
        double[] limits = new double[]{-5.0D, 5.0D, -5.0D, 5.0D};
        if (pos > 0) {
            String nums = example.substring(pos + 1);
            example = example.substring(0, pos);
            StringTokenizer toks = new StringTokenizer(nums, " ,");
            int i;
            if (toks.countTokens() >= 4) {
                for(i = 0; i < 4; ++i) {
                    try {
                        Double d = Double.valueOf(toks.nextToken());
                        limits[i] = d;
                    } catch (NumberFormatException var16) {
                    }
                }
            }

            for(i = 0; i < this.sliders.size() && toks.hasMoreElements(); ++i) {
                try {
                    double min = Double.valueOf(toks.nextToken());
                    double max = Double.valueOf(toks.nextToken());
                    double d = Double.valueOf(toks.nextToken());
                    VariableSlider slider = (VariableSlider)this.sliders.elementAt(i);
                    slider.setMin(new Constant(min));
                    slider.setMax(new Constant(max));
                    slider.setVal(d);
                } catch (Exception var15) {
                }
            }
        }

        if (this.functionInput != null) {
            this.functionInput.setText(example);
        } else {
            try {
                this.func = new SimpleFunction(this.parser.parse(example), this.xVar);
                this.graph.setFunction(this.func);
            } catch (ParseError var14) {
            }
        }

        CoordinateRect coords = this.canvas.getCoordinateRect(0);
        coords.setLimits(limits);
        coords.setRestoreBuffer();
        this.mainController.compute();
    }

    public static void main(String[] a) {
        JFrame f = new JFrame();
        Applet app = new FamiliesOfGraphs();
        app.init();
        f.getContentPane().add(app);
        f.pack();
        f.setSize(new Dimension(500, 500));
        f.setVisible(true);
    }
}
