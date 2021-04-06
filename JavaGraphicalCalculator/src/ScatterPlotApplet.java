import edu.awt.*;
import edu.data.Parser;
import edu.data.Value;
import edu.draw.Axes;
import edu.draw.DisplayCanvas;
import edu.draw.ScatterPlot;

import javax.swing.*;
import java.applet.Applet;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Vector;

public class ScatterPlotApplet extends Applet implements ActionListener {
    private Frame frame;
    private String frameTitle;
    private Button launchButton;
    private String launchButtonName;
    private DataTableInput table;
    private ScatterPlot scatterPlot;
    private DisplayCanvas canvas;
    private Button loadFileButton;
    private Choice fileMenu;
    private String[] fileNames;
    private Controller mainController;

    public ScatterPlotApplet() {
    }

    public static void main(String[] a) {
        JFrame f = new JFrame();
        Applet app = new SimpleGraph();
        app.init();
        f.getContentPane().add(app);
        f.pack();
        f.setSize(new Dimension(500, 500));
        f.setVisible(true);
    }

    public void init() {
        this.frameTitle = this.getParameter("FrameTitle");
        int pos;
        if (this.frameTitle == null) {
            this.frameTitle = "Scatter Plots";
            pos = this.frameTitle.lastIndexOf(46);
            if (pos > -1) {
                this.frameTitle = this.frameTitle.substring(pos + 1);
            }
        }

        this.setLayout(new BorderLayout());
        pos = this.getSize().height;
        this.launchButtonName = this.getParameter("LaunchButtonName");
        if ((pos <= 0 || pos > 50) && this.launchButtonName == null) {
            this.add(this.makeMainPanel(), "Center");
        } else {
            if (this.launchButtonName == null) {
                this.launchButtonName = "Launch " + this.frameTitle;
            }

            this.launchButton = new Button(this.launchButtonName);
            this.add(this.launchButton, "Center");
            this.launchButton.addActionListener(this);
        }

    }

    public Panel makeMainPanel() {
        JCMPanel panel = new JCMPanel(2);
        this.mainController = panel.getController();
        panel.setBackground(new Color(0, 0, 180));
        panel.setInsetGap(2);
        this.setLayout(new BorderLayout());
        this.table = new DataTableInput(null, 2);
        this.table.setColumnName(0, this.getParameter("ColumnName1", "X"));
        this.table.setColumnName(1, this.getParameter("ColumnName2", "Y"));
        this.table.setThrowErrors(true);
        if ("yes".equalsIgnoreCase(this.getParameter("ShowColumnTitles", "yes"))) {
            this.table.setShowColumnTitles(true);
        }

        if ("yes".equalsIgnoreCase(this.getParameter("ShowRowNumbers", "yes"))) {
            this.table.setShowRowNumbers(true);
        }

        Parser parser = new Parser();
        this.table.addVariablesToParser(parser);
        ExpressionInput input1 = new ExpressionInput(this.table.getColumnName(0), parser);
        input1.setOnUserAction(this.mainController);
        ExpressionInput input2 = new ExpressionInput(this.table.getColumnName(1), parser);
        input2.setOnUserAction(this.mainController);
        this.scatterPlot = new ScatterPlot(this.table, input1.getExpression(), input2.getExpression());
        if (!"yes".equalsIgnoreCase(this.getParameter("ShowRegressionLine", "yes"))) {
            this.scatterPlot.setShowRegressionLine(false);
        }

        if (!"yes".equalsIgnoreCase(this.getParameter("MissingValueIsError", "yes"))) {
            this.scatterPlot.setMissingValueIsError(false);
        }

        this.canvas = new DisplayCanvas();
        this.canvas.add(new Axes());
        this.canvas.add(this.scatterPlot);
        this.mainController.setErrorReporter(this.canvas);
        ComputeButton computeButton = new ComputeButton("Update Display");
        computeButton.setOnUserAction(this.mainController);
        computeButton.setBackground(Color.lightGray);
        Panel menu = this.makefileMenu();
        JCMPanel inputPanel = null;
        Panel bottom = null;
        if ("yes".equalsIgnoreCase(this.getParameter("UseExpressionInputs", "yes"))) {
            inputPanel = new JCMPanel(1, 2);
            inputPanel.setBackground(Color.lightGray);
            JCMPanel leftInput = new JCMPanel();
            leftInput.add(new Label("  Plot:  "), "West");
            leftInput.add(input1, "Center");
            inputPanel.add(leftInput);
            JCMPanel rightInput = new JCMPanel();
            rightInput.add(new Label(" versus: "), "West");
            rightInput.add(input2, "Center");
            inputPanel.add(rightInput);
            bottom = new JCMPanel(new BorderLayout(12, 3));
            bottom.add(inputPanel, "Center");
            bottom.add(computeButton, "East");
        }

        if (this.scatterPlot.getShowRegressionLine() && "yes".equalsIgnoreCase(this.getParameter("ShowStats", "yes"))) {
            DisplayLabel dl = new DisplayLabel("Slope = #;  Intercept = #;  Correlation = #", new Value[]{this.scatterPlot.getValueObject(1), this.scatterPlot.getValueObject(0), this.scatterPlot.getValueObject(5)});
            dl.setAlignment(1);
            dl.setBackground(Color.lightGray);
            dl.setForeground(new Color(200, 0, 0));
            dl.setFont(new Font("Serif", 0, 14));
            if (bottom != null) {
                bottom.add(dl, "South");
            } else {
                bottom = new JCMPanel(new BorderLayout(12, 3));
                bottom.add(dl, "Center");
                bottom.add(computeButton, "East");
            }
        }

        if (bottom == null) {
            if (menu != null) {
                menu.add(computeButton, "East");
            } else {
                bottom = new Panel();
                bottom.add(computeButton);
            }
        }

        panel.add(this.canvas, "Center");
        panel.add(this.table, "West");
        if (bottom != null) {
            panel.add(bottom, "South");
        }

        if (menu != null) {
            panel.add(menu, "North");
        } else {
            String title = this.getParameter("PanelTitle");
            if (title != null) {
                Label pt = new Label(title, 1);
                pt.setBackground(Color.lightGray);
                pt.setForeground(new Color(200, 0, 0));
                pt.setFont(new Font("Serif", 0, 14));
                panel.add(pt, "North");
            }
        }

        return panel;
    }

    private Panel makefileMenu() {
        Vector names = new Vector();
        this.fileMenu = new Choice();
        String file = this.getParameter("File");
        int ct = 1;
        if (file == null) {
            file = this.getParameter("File1");
            ct = 2;
        }

        int i;
        while (file != null) {
            file = file.trim();
            i = file.indexOf(";");
            String menuEntry;
            if (i == -1) {
                menuEntry = file;
            } else {
                menuEntry = file.substring(0, i).trim();
                file = file.substring(i + 1).trim();
            }

            names.addElement(file);
            this.fileMenu.add(menuEntry);
            file = this.getParameter("File" + ct);
            ++ct;
        }

        if (names.size() == 0) {
            this.fileMenu = null;
            return null;
        } else {
            this.fileNames = new String[names.size()];

            for (i = 0; i < names.size(); ++i) {
                this.fileNames[i] = (String) names.elementAt(i);
            }

            Panel p = new Panel();
            p.setBackground(Color.lightGray);
            p.setLayout(new BorderLayout(5, 5));
            p.add(this.fileMenu, "Center");
            this.loadFileButton = new Button("Load Data File: ");
            this.loadFileButton.addActionListener(this);
            p.add(this.loadFileButton, "West");
            this.fileMenu.setBackground(Color.white);
            return p;
        }
    }

    private void doLoadFile(String name) {
        InputStream in;
        try {
            URL url = new URL(this.getDocumentBase(), name);
            in = url.openStream();
        } catch (Exception var6) {
            this.canvas.setErrorMessage(null, "Unable to open file named \"" + name + "\": " + var6);
            return;
        }

        InputStreamReader inputReader = new InputStreamReader(in);

        try {
            this.table.readFromStream(inputReader);
            inputReader.close();
        } catch (Exception var5) {
            this.canvas.setErrorMessage(null, "Unable to get data from file \"" + name + "\": " + var5.getMessage());
            return;
        }

        this.mainController.compute();
    }

    public synchronized void actionPerformed(ActionEvent evt) {
        Object source = evt.getSource();
        if (this.loadFileButton != null && source == this.loadFileButton) {
            this.doLoadFile(this.fileNames[this.fileMenu.getSelectedIndex()]);
        } else if (source == this.launchButton && this.launchButton != null) {
            this.launchButton.setEnabled(false);
            if (this.frame == null) {
                this.frame = new Frame(this.frameTitle);
                this.frame.add(this.makeMainPanel());
                this.frame.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent evt) {
                        ScatterPlotApplet.this.frame.dispose();
                    }

                    public void windowClosed(WindowEvent evt) {
                        ScatterPlotApplet.this.frameClosed();
                    }
                });
                this.frame.pack();
                this.frame.setLocation(50, 50);
                this.frame.show();
                this.launchButton.setLabel("Close Window");
                this.launchButton.setEnabled(true);
            } else {
                this.frame.dispose();
            }
        }

    }

    private synchronized void frameClosed() {
        this.frame = null;
        this.launchButton.setLabel(this.launchButtonName);
        this.launchButton.setEnabled(true);
    }

    protected String getParameter(String paramName, String defaultValue) {
        String val = this.getParameter(paramName);
        return val == null ? defaultValue : val;
    }
}
