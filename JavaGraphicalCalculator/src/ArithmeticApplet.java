
import edu.awt.Controller;
import edu.awt.DisplayLabel;
import edu.awt.JCMPanel;
import edu.awt.VariableInput;
import edu.data.Parser;
import edu.data.Value;
import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Label;
import javax.swing.JFrame;

public class ArithmeticApplet extends Applet {
    public ArithmeticApplet() {
    }

    Label makeLabel(String str) {
        Label lab = new Label(str, 2);
        lab.setBackground(new Color(255, 255, 220));
        lab.setForeground(Color.blue);
        return lab;
    }

    Label makeDisplayLabel(Value val) {
        Label lab = new DisplayLabel("#", val);
        lab.setBackground(new Color(255, 255, 220));
        lab.setForeground(Color.red);
        lab.setAlignment(1);
        return lab;
    }

    public void init() {
        this.setBackground(Color.blue);
        this.setLayout(new BorderLayout());
        JCMPanel main = new JCMPanel(6, 2, 2);
        this.add(main, "Center");
        main.setInsetGap(2);
        Parser parser = new Parser();
        VariableInput xInput = new VariableInput("x", "0", parser);
        VariableInput yInput = new VariableInput("y", "0", parser);
        main.add(this.makeLabel("Input x = "));
        main.add(xInput);
        main.add(this.makeLabel("Input y = "));
        main.add(yInput);
        main.add(this.makeLabel("x + y = "));
        main.add(this.makeDisplayLabel(parser.parse("x+y")));
        main.add(this.makeLabel("x - y = "));
        main.add(this.makeDisplayLabel(parser.parse("x-y")));
        main.add(this.makeLabel("x * y = "));
        main.add(this.makeDisplayLabel(parser.parse("x*y")));
        main.add(this.makeLabel("x / y = "));
        main.add(this.makeDisplayLabel(parser.parse("x/y")));
        Controller c = main.getController();
        xInput.setOnTextChange(c);
        yInput.setOnTextChange(c);
    }

    public static void main(String[] a) {
        JFrame f = new JFrame();
        Applet app = new ArithmeticApplet();
        app.init();
        f.getContentPane().add(app);
        f.pack();
        f.setSize(new Dimension(500, 500));
        f.setVisible(true);
    }
}
