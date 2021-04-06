/*************************************************************************
*                                                                        *
*   1) This source code file, in unmodified form, and compiled classes   *
*      derived from it can be used and distributed without restriction,  *
*      including for commercial use.  (Attribution is not required       *
*      but is appreciated.)                                              *
*                                                                        *
*    2) Modified versions of this file can be made and distributed       *
*       provided:  the modified versions are put into a Java package     *
*       different from the original package, edu.hws;  modified          *
*       versions are distributed under the same terms as the original;   *
*       and the modifications are documented in comments.  (Modification *
*       here does not include simply making subclasses that belong to    *
*       a package other than edu.hws, which can be done without any      *
*       restriction.)                                                    *
*                                                                        *
*   David J. Eck                                                         *
*   Department of Mathematics and Computer Science                       *
*   Hobart and William Smith Colleges                                    *
*   Geneva, New York 14456,   USA                                        *
*   Email: eck@hws.edu          WWW: http://math.hws.edu/eck/            *
*                                                                        *
*************************************************************************/

// November 2005: Removed processKeyEvent to get rid of bogus "beep" when shift key is pressed.
// (this also lets illegal characters into the input box)

package edu.awt;

import edu.data.*;
import edu.data.*;
import java.awt.*;
import java.awt.event.*;

/**
 * A VariableInput is an input box into which the user can type a real
 * number value, which becomes the value of an associated Variable.
 * The value of the Variable can change only when the VariableInput's
 * checkInput() method is called (usually by a Controller).  See the Controller
 * class for more information.
 *
 * <p>Whenever checkInput is called, an error of type JCMError
 * might be generated.  If throwErrors is true, this error is
 * thrown; if it is false, the error is caught, the value
 * of the variable is set to Double.NaN, and no error is thrown.
 * The error message associated with the error can be retrieved by 
 * calling getErrorMessage(), if desired.  (This value is null if
 * no error occurred the last time checkInput was called.) 
 *
 * <p>A VariableInput is a Value, so it can be used directly where
 * a Value object is needed.
 *
 * <p>An VariableInput will ordinarily be registered with
 * a Controller in TWO ways:  It's added to a Controller
 * with the Controller's add() method.  This makes the Controller
 * call the VariableInput's checkInput() method during the
 * Controller's compute() method.  Secondly, a Controller
 * is set as the "onUserAction" property.  This causes the
 * Controller's compute() method to be called when the user
 * presses return in the VariableInput box.  This is optional--
 * you might, for example, only want the Controller to compute()
 * when a Compute button is pressed.  You can also set the
 * VariableInput's onTextChange property to a Controller
 * that you want to compute every time the text in the box
 * changes.
 *
 * <p>After the VariableInput is created, it is possible to specify the
 * largest and smallest allowed values for the variable.  It is also
 * possible to specify what sytle of input is allowed.  The style
 * can be to allow any constant expression, constant real numbers only,
 * or integers only.  Set these parameters with setMin(), setMax(),
 * and setInputStyle().  For setInputStyle(), the legal parameter
 * values are VariableInput.EXPRESSION, VariableInput.REAL, and
 * VariableInput.INTEGER.  The default input style is EXPRESSION.
 *
 */
public class VariableInput extends TextField implements InputObject, Tieable, Value {

   /**
    * The Variable that represents
    * the value of this input box.  (VI is
    * a private nested class inside VariableInput.)
    */
   protected VI variable;                                   

   /**
    * True if an error should be thrown
    * when checkInput() is calles and
    * the contents do not define a
    * legal number.  True by default.
    */
   protected boolean throwErrors;

   /**
    * Error message from the most recent
    * time checkInput() as called.
    * Null if there was no error.
    */
   protected String errorMessage;                                   

   /**   
    * This serial number is increased
    * each time the value of the variable
    * changes.
    */
   protected long serialNumber;

   /**
    * This is set to true if the text in the
    * box has been changed since the last time
    * the value of the variable was checked by checkInput().
    */
   //protected boolean hasChanged;
   protected String previousContents;
                                  
   private Controller onUserAction;   // If this is non-null, the compute() method
                                      //   of onUserAction is called when the user
                                      //   presses return in this input-box.
   
   private Controller onTextChange;   // If this is non-null, the compute() method
                                      //   of onTextChange is called when the text
                                      //   in this input box changes
                                   
   /**
    * Smallest allowable value.
    */
   protected double minValue = -Double.MAX_VALUE;

   /**
    * Largest allowable value.
    */
   protected double maxValue = Double.MAX_VALUE;

   /**
    * One of the constant values EXPRESSION, REAL, or
    * INTEGER, specifying the style of input.   
    */
   protected int inputStyle = 0;
   
   /**
    * A constant for use in the setInputStyle() method.  Any constant expression is allowed.
    */
   public static final int EXPRESSION = 0;

   /**
    * A constant for use in the setInputStyle() method.  Only real numbers are allowed.
    */
   public static final int REAL = 1;

   /**
    * A constant for use in the setInputStyle() method.  Only integers are allowed.
    */
   public static final int INTEGER = 2;

   /**   
    * Create an unnamed VariableInput with initial contents "0".
    */
   public VariableInput() {
      this(null,null);
   }

   /**   
    * Construct a VariableInput with the given name
    * and initial String (which can both be null).
    * If initialString is null, the string "0" is used.
    * No error occurs in the constructor if the initialString
    * does not represent a legal value  (A string rather than a
    * double is used for initialization since the initial
    * content can be an expression such as "pi/2".)
    * If name is not null, it is used as the name of
    * the VariableInput component as well as the name
    * of the associated variable.
    */
   public VariableInput(String name, String initialString) {
      super((initialString == null)? "0" : initialString, 12);
      setBackground(Color.white);
      variable = new VI(name);
      if (name != null)
         super.setName(name);
      //hasChanged = true;
      previousContents = null;
      variable.checkInput();  // Won't throw an error, since throwErrors is false.
      //enableEvents(AWTEvent.KEY_EVENT_MASK);
      throwErrors = true;
   }

   /**   
    * Create a VariableInput just as in the constructor
    * VariableInput(String,String).  Then, if both parser and
    * name are non-null, register the associated variable
    * with the parser.
    */
   public VariableInput(String name, String initialString, Parser parser) {
      this(name, initialString);
      addTo(parser);
   }

   /**   
    * Get the associated variable for the VariableInput box.  You will need
    * this, for example, if you want to register the variable with a Parser.
    */
   public Variable getVariable() {
      return variable;
   }
   
   /**
    * Convenience method for creating a component containing
    * this VariableInput together with a label of the form
    * "<name> = ".  This version uses default colors for the
    * label, which are inherited from the containing
    * component.
    */
   public JCMPanel withLabel() {
      return withLabel(null,null);
   }

   /**   
    * Convenience method for creating a component containing
    * this VariableInput together with a label of the form
    * "name = ".  Uses the given background and foreground
    * colors for the label and the panel.  The colors can be 
    * null to use the defaults, which will be inherited from the
    * containing Component.
    */
   public JCMPanel withLabel(Color back, Color fore) {
      Label label =  new Label(" " + variable.getName() + " =");
      JCMPanel panel = new JCMPanel();
      if (back != null) {
         panel.setBackground(back);
         label.setBackground(back);
      }
      if (fore != null) {
         panel.setForeground(fore);
         label.setBackground(fore);
      }
      panel.add(label, BorderLayout.WEST);
      panel.add(this, BorderLayout.CENTER);
      return panel;
   }
   
   /**
    * Set the name of the variable.  This should not be called
    * while the variable is registered with a Parser.
    * The name of the VariableInput Component is also set to name,
    * if the name is non=null.
    */
   public void setName(String name) {
      variable.setName(name);
      if (name != null)
         super.setName(name);
   }
      
   /**
    * A convenience method that registers this VariableInput's variable
    * with Parser p (but only if both p and the name of the variable are non-null).
    */
   public void addTo(Parser p) {
       if (p != null && variable.getName() != null)
          p.add(variable);
   }
      
   /**
    * If the Controller, c, is non-null, then its compute() method will be called whenever
    * the user presses the return key while typing in this text-input box.
    */
   public void setOnUserAction(Controller c) {
      onUserAction = c;
      enableEvents(AWTEvent.ACTION_EVENT_MASK);
   }
   
   /**
    * Return the Controller, if any, that is notified when the user 
    * presses return in this text-input box.
    */
   public Controller getOnUserAction() {
      return onUserAction;
   }
   
   /**
    * Method required by InputObject interface; in this class, it simply calls
    * setOnUserAction(c).  This is meant to be called by JCMPanel.gatherInputs().
    */
    public void notifyControllerOnChange(Controller c) {
       setOnUserAction(c);
    }

   /**
    * If the Controller, cm is non-null, then its compute() method will be called whenever
    * the text in this input box changes.  Furthermore, the throwErrors
    * property will be set to false, to avoid throwing multiple errors
    * while the user is typing.  (You can change it back to true if
    * you want by calling setThrowErrors(true).)
    */
   public void setOnTextChange(Controller c) {
      onTextChange = c;
      enableEvents(AWTEvent.TEXT_EVENT_MASK);
      if (c != null)
         throwErrors = false;
   }
   
   /**
    * Return the Controller, if any, that is notified when the text
    * in this input box changes
    */
   public Controller getOnTextChange() {
      return onTextChange;
   }

   /**   
    * Return the value of the associated variable, which might not
    * reflect the value of the contents of the input box.  The value
    * of the variable changes only when the checkInput() method is called,
    * or when the setVal() method is called.
    * Call checkInput() first, if you want to be sure of getting the
    * same value that is currently shown in the box.
    */
   public double getVal() {
      return variable.getVal();
   }
   
   /**
    * Set the value of the associated variable.
    * Also sets the content of the input box.
    */
   public void setVal(double d) {
      variable.setVal(d);
   }
   
   /**   
    * Set the throwErrors property.  If the value is true, then
    * an error will be thrown by the checkInput() method when the
    * contents of the VariableInput box are not legal.  Otherwise,
    * no error is thrown; the value of the variable is just set
    * to Double.NaN.
    */
   public void setThrowErrors(boolean throwErrors) {
      this.throwErrors = throwErrors;
   }

   /**   
    * Return the value of the throwErrors property.
    */
   public boolean getThrowErrors() {
      return throwErrors;
   }

   /**   
    * Specify the smallest allowed value for the content of this VariableInput box.
    */
   public void setMin(double min) {
      if (!Double.isNaN(min)) {
         minValue = min;
         //hasChanged = true; 
         previousContents = null;// (force recheck of contents)
      }
   }

   /**      
    * Return the minimum value that will be accepted in this VariableInput box.
    */
   public double getMin() {
      return minValue;
   }

   /**   
    * Specify the largest allowed value for the content of this VariableInput box.
    */
   public void setMax(double max) {
      if (!Double.isNaN(max)) {
         maxValue = max;
         //hasChanged = true; 
         previousContents = null;// (force recheck of contents)
      }
   }

   /**      
    * Return the maximum value that will be accepted in this VariableInput box.
    */
   public double getMax() {
      return maxValue;
   }
   
   /**
    * Specify what types of things are allowed in the input box.
    * The value of the parameter, style, must be one of the constants VariableInput.EXPRESSION,
    * VariableInput.REAL, or VariableInput.INTEGER.  If not, the call to setInputStyle is ignored.
    */
   public void setInputStyle(int style) {
      if (style == EXPRESSION || style == REAL || style == INTEGER) {
         if (style != inputStyle) {
            //hasChanged = true;
            previousContents = null;// (force recheck of contents)
            inputStyle = style;
         }
      }
   }

   /**   
    * Return the input style, which determines what types of things
    * are allowed in the input box.  The returned value is one
    * of the contstants EXPRESSION, REAL, or INTEGER
    */
   public int getInputStyle() {
      return inputStyle;
   }

   /**
    * Get error message from previous call to checkInput().
    * Returns null if there was no error.
    */
   public String getErrorMessage() {
      return errorMessage;
   }
   
   //--------------------- Implementation Details -------------------------------------------

   /**
    * Check whether the contents are valid, and change the value
    * of the associated variable if the new contents do not match
    * the current value.  This might throw an error of type JCMError,
    * if throwErrors is true.  This is usually called by a Controller.
    */
   public void checkInput() {
      variable.checkInput();
   }
   
   /**
    * Return this object's serial number, which increases whenever the
    * value of the associated variable changes.
    */
   public long getSerialNumber() {
      return serialNumber;
   }

   /**   
    * Synchronize serial number and value with newest, unless
    * this VariableInput is itself newest.  This is required by
    * the Tieable interface, and is usually called by an object of type Tie.
    */
   public void sync(Tie tie, Tieable newest) {
      if (newest == this)
         return;
      if (! (newest instanceof Value) )
         throw new IllegalArgumentException("Internal Error:  A VariableInput can only sync with Value objects.");
      variable.setVal(((Value)newest).getVal());
      serialNumber = newest.getSerialNumber();
   }
      

   private class VI extends Variable {
           // This class is used to define a Variable object associated
           // with this VariableInput. 
      VI(String name) {
         super(name);
      }
      public void setVal(double d) {
            // If d is different from the current value of the variable,
            // set the value of the variable to d, set the displayed text,
            // and increment the serial number of the variable.
         double oldVal = this.getVal();
         boolean hasChanged = previousContents == null || !previousContents.equals(getText());
         if ( !hasChanged && ((Double.isNaN(d) && Double.isNaN(oldVal)) || (d == oldVal)) )
            return;  // Value is not actually changing.
         serialNumber++;
         justSetText(NumUtils.realToString(d));
         //hasChanged = false;
         previousContents = getText();
         errorMessage = null;
         super.setVal(d);
      }
      void checkInput() {
            // If the contents of the input box have changed, change
            // the value of the variable to match.  If this is an actual
            // change in the value of the variable, then the serialNumber
            // is incremented.
         boolean hasChanged = previousContents == null || !previousContents.equals(getText());
         if (!hasChanged)
            return;
         errorMessage = null;
         String content = getText();
         try {
            double d = convertInput(content);
            double oldVal = this.getVal();
            if ( (Double.isNaN(d) && Double.isNaN(oldVal)) || (d == oldVal) )
               return;  // Value is not actually changing.
            serialNumber++;
            super.setVal(d);
         }
         catch (JCMError e) {
            if (!Double.isNaN(this.getVal()))
               serialNumber++;
            super.setVal(Double.NaN);  // Value becomes undefined.
            if (throwErrors)
               throw e;
         }
      }
   }
   
   
   private transient Parser constantParser; // To be used to process constant expressions;
                                            // will not know about any variables or user functions.

   /**                                            
    * Convert a string into a real value.  The parameter is taken from the input box when
    * this method is called by VI.checkInput()
    * Throw a JCMError if any error is found in the input.
    *
    * @param num String to be converted
    * @return the real value.
    */
   protected double convertInput(String num) {
      double ans = Double.NaN;  // The value.
      if (inputStyle == EXPRESSION) {
         if (constantParser == null)
            constantParser = new Parser();
         try {
            Expression exp = constantParser.parse(num);
            ans = exp.getVal();
         }
         catch (ParseError e) {
            errorMessage = "Illegal constant expression:  " + e.getMessage();
            if (throwErrors) {
               setCaretPosition(e.context.pos);
               requestFocus();
            }
         }
      }
      else if (inputStyle == REAL) {
         try {
            Double d = Double.valueOf(num);
            ans = d.doubleValue();
         }
         catch (NumberFormatException e) {
            errorMessage = "Value is not a legal real number.";
            if (throwErrors) {
               requestFocus();
            }
         }
      }
      else {  // inputStyle is INTEGER
         try {
            ans = Long.parseLong(num);
         }
         catch (NumberFormatException e) {
            errorMessage = "Value is not a legal integer.";
            if (throwErrors) {
               requestFocus();
            }
         }
      }
      if (errorMessage == null) {
         if (ans < minValue || ans > maxValue) {
            errorMessage = "Value outside legal range. It should be ";
            if (inputStyle == INTEGER)
               errorMessage += "an integer ";
            else if (inputStyle == REAL)
               errorMessage += "a real number ";
            if (minValue > -Double.MAX_VALUE && maxValue < Double.MAX_VALUE)
               errorMessage += "between " + NumUtils.realToString(minValue) + " and " + NumUtils.realToString(maxValue);
            else if (minValue > -Double.MAX_VALUE)
               errorMessage += "greater than or equal to " + NumUtils.realToString(minValue);
            else
               errorMessage += "less than or equal to " + NumUtils.realToString(maxValue);
            if (throwErrors) {
               requestFocus();
            }
         }
      }
      if (errorMessage != null)
         throw new JCMError(errorMessage,this);
      return ans;
   }
   
   /*
    * Override processKeyEvent to only allow characters
    * that are legal in this VariableInput.
    *
    * @param evt used internally.
   public void processKeyEvent(KeyEvent evt) {
      if (evt.getID() == KeyEvent.KEY_PRESSED) {
         int ch = evt.getKeyCode();
         char chr = evt.getKeyChar();
         boolean use = (chr != 0 && Character.isDigit(chr)
                            || chr == '-' || chr == '+') 
                            || ch == KeyEvent.VK_DELETE
                            || ch == KeyEvent.VK_BACK_SPACE;
         if (inputStyle != INTEGER)
            use = use || chr == '.' || chr == 'e' || chr == 'E';
         if (inputStyle == EXPRESSION)
            use = use || Character.isLetter(chr) 
                            || chr == '(' || chr == ')' || chr == '*'
                            || chr == '/' || chr == '^'
                            || chr == ':' || chr == '?' || chr == '|'
                            || chr == '&' || chr == '~'  || chr == '='
                            || chr == '<' || chr == '>' || chr == '!'
                            || ch == KeyEvent.VK_SPACE;
         boolean useControl = use || ch == KeyEvent.VK_TAB 
                                  || ch ==KeyEvent.VK_ENTER || chr == 0;
         if (!useControl) {
            evt.consume();
            Toolkit.getDefaultToolkit().beep();
         }
         else if (use) {
            hasChanged = true;
         }            
      }
      super.processKeyEvent(evt);
   }
*/
   
   /**
    * This overrides the setText() method from the TextField class so that
    * it will also force the contents to be checked the next time
    * the checkInput() method is called. 
    *
    * @param text change text to this.
    */
   public void setText(String text) {
      super.setText(text);
      //hasChanged = true;
      previousContents = null;
   }
   
   private void justSetText(String text) {
        // Call super.setText().
      super.setText(text);
   }
   
   /**
    * Overridden to call onUserAction.compute() if onUserAction is non-null.
    * This is not meant to be called directly.
    */
   public void processActionEvent(ActionEvent evt) {
      if (onUserAction != null)
         onUserAction.compute();
      super.processActionEvent(evt);
   }
   
   /**
    * Overridden to call onUserAction.compute() if onUserAction is non-null.
    * This is not meant to be called directly.
    */
   public void processTextEvent(TextEvent evt) {
      //hasChanged = true;
      previousContents = null;
      if (onTextChange != null)
         onTextChange.compute();
      super.processTextEvent(evt);
   }

}  // end class VariableInput

