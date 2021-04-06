import edu.awt.DisplayLabel;
import edu.awt.JCMPanel;
import edu.awt.VariableInput;
import edu.data.ParseError;
import edu.data.Parser;
import edu.functions.SummationParser;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.StringTokenizer;

public class Evaluator extends Applet implements ActionListener {

    private Frame frame;
    private String frameTitle;
    private Button launchButton;
    private String launchButtonName;
    private final String[] colorNames = {"black", "red", "blue", "green", "yellow",
            "cyan", "magenta", "gray", "darkgray",
            "lightgray", "pink", "orange", "white"};
    private final Color[] colors = {Color.black, Color.red, Color.blue, Color.green, Color.yellow,
            Color.cyan, Color.magenta, Color.gray, Color.darkGray,
            Color.lightGray, Color.pink, Color.orange, Color.white};

    public static void main(String[] a) {
        javax.swing.JFrame f = new javax.swing.JFrame();
        Applet app = new Evaluator();
        app.init();

        f.getContentPane().add(app);

        f.pack();
        f.setSize(new Dimension(500, 500));
        f.setVisible(true);
    }

    public void init() {

        if (frameTitle == null) {
            frameTitle = "Calculator";
            int pos = frameTitle.lastIndexOf('.');
            if (pos > -1)
                frameTitle = frameTitle.substring(pos + 1);
        }
        setLayout(new BorderLayout());
        int height = getSize().height;
        launchButtonName = "Launch";
        if ((height > 0 && height <= 35) || launchButtonName != null) {

            if (launchButtonName == null)
                launchButtonName = "Launch " + frameTitle;
            launchButton = new Button(launchButtonName);
            add(launchButton, BorderLayout.CENTER);
            launchButton.addActionListener(this);
        } else {

            add(makeMainPanel(), BorderLayout.CENTER);
        }
    }

    /*
     * Create the main panel of the applet.
     */
    public JCMPanel makeMainPanel() {


        Color background = getColorParam("BackgroundColor", Color.gray);
        Color labelBackground = getColorParam("LabelBackground", new Color(225, 225, 225));
        Color labelForeground = getColorParam("LabelForeground", new Color(0, 0, 200));
        Color answerBackground = getColorParam("AnswerBackground", labelBackground);
        Color answerForeground = getColorParam("AnswerForeground", Color.red);
        Color inputBackground = getColorParam("InputBackground", Color.white);
        Color inputForeground = getColorParam("InputForeground", Color.black);


        JCMPanel panel = new JCMPanel(5);
        panel.setBackground(background);
        panel.setInsetGap(3);
        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);
        JCMPanel left = new JCMPanel(0, 1, 3);
        panel.add(left, BorderLayout.CENTER);
        JCMPanel right = new JCMPanel(0, 1, 3);
        panel.add(right, BorderLayout.EAST);


        Parser parser = new Parser();
        parser.addOptions(Parser.FACTORIAL);
        parser.add(new SummationParser());


        int ct = 0;
        String variableName = null;


        variableName = "x";

        String firstVar = variableName;
        while (variableName != null) {
            String valString = "0";
            variableName = variableName.trim();
            int pos = variableName.indexOf(" ");
            if (pos > 0) {

                valString = variableName.substring(pos + 1).trim();
                variableName = variableName.substring(0, pos);
            }
            Label lab = new Label(" Input:  " + variableName + " =  ", Label.RIGHT);
            lab.setBackground(labelBackground);
            lab.setForeground(labelForeground);
            left.add(lab);
            VariableInput v = new VariableInput(variableName, valString, parser);
            v.setBackground(inputBackground);
            v.setForeground(inputForeground);
            v.setThrowErrors(false);
            v.setOnTextChange(panel.getController());
            v.setOnUserAction(panel.getController());
            right.add(v);
            ct++;

        }


        ct = 0;
        String function = getParameter("Expression");
        if (function == null) {
            function = getParameter("Expression1");
            if (function == null)
                function = "log2(" + firstVar + ")";
            else
                ct = 1;
        }
        while (function != null) {
            Label lab = new Label(" " + function + " =  ", Label.RIGHT);
            lab.setBackground(labelBackground);
            lab.setForeground(labelForeground);
            left.add(lab);
            try {
                DisplayLabel d = new DisplayLabel("#", parser.parse(function));
                d.setBackground(answerBackground);
                d.setForeground(answerForeground);
                d.setAlignment(Label.CENTER);
                right.add(d);
            } catch (ParseError e) {
                right.add(new Label("invalid function"));
            }
            ct++;
            function = getParameter("Expression" + ct);
        }

        return panel;

    }

    synchronized public void actionPerformed(ActionEvent evt) {
        Object source = evt.getSource();
        if (source == launchButton && launchButton != null) {
            launchButton.setEnabled(false);
            if (frame == null) {
                frame = new Frame(frameTitle);
                frame.add(makeMainPanel());
                frame.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent evt) {
                        frame.dispose();
                    }

                    public void windowClosed(WindowEvent evt) {
                        frameClosed();
                    }
                });
                frame.pack();
                frame.setLocation(50, 50);
                frame.show();
                launchButton.setLabel("Close Window");
                launchButton.setEnabled(true);
            } else {
                frame.dispose();
            }
        }
    }

    synchronized private void frameClosed() {

        frame = null;
        launchButton.setLabel(launchButtonName);
        launchButton.setEnabled(true);
    }

    protected Color getColorParam(String paramName, Color defaultColor) {
        String data = null;
        if (data == null || data.trim().length() == 0)
            return defaultColor;
        data = data.trim();
        if (Character.isLetter(data.charAt(0))) {
            for (int i = 0; i < colorNames.length; i++)
                if (data.equalsIgnoreCase(colorNames[i]))
                    return colors[i];
            return defaultColor;
        } else {
            StringTokenizer tokenizer = new StringTokenizer(data, " \t,;");
            int count = tokenizer.countTokens();
            if (count < 3)
                return defaultColor;
            double[] nums = new double[3];
            for (int i = 0; i < 3; i++) {
                try {
                    Double d = Double.valueOf(tokenizer.nextToken());
                    nums[i] = d.doubleValue();
                } catch (NumberFormatException e) {
                    return defaultColor;
                }
            }
            if (nums[0] < 0 || nums[0] > 255 || nums[1] < 0 || nums[1] > 255 || nums[2] < 0 || nums[2] > 255)
                return defaultColor;
            return new Color((int) Math.round(nums[0]), (int) Math.round(nums[1]), (int) Math.round(nums[2]));
        }
    }

}
