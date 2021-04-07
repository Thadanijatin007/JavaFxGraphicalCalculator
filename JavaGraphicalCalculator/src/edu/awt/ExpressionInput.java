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
 * An ExpressionInput is an input box that allows the user
 * input a mathematical expression.  There is an associated
 * object that belongs to the class Expression.  The
 * value of this object can change only when checkInput()
 * is called.  The checkInput() method is usually called by a Controller.
 *    <p>An ExpressionInput will ordinarily be registered with
 * a Controller in TWO ways:  It's added to a Controller
 * with the Controller's add() method.  This makes the Contrller
 * call the ExpressionInput's checkInput() method during the
 * Controller's compute() method.  Secondly, the Controller
 * is set as the "onUserAction" property.  This causes the
 * Controller's compute() method to be called when the user
 * presses return in the ExpressionInput box.  This is optional--
 * you might, for example, only want the Controller to compute()
 * when a Compute button is pressed.  You can also set the
 * ExpressionInput's onTextChange property to a Controller
 * if you want it to compute every time the text in the box
 * changes.
 *    <p>Use the function getFunction() if you want to
 * use an ExpressionInput as a way of inputting a function.
 *
 */
public class ExpressionInput extends TextField implements InputObject, Value {

   /**
    * The Expression associate with this input box.  
    * Class EI is a private nested class.
    */
   protected EI expr;

   /**
    * A parser for parsing the user's input
    * expression.  If this is null,
    * a default parser will be used and
    * only constant expressions will
    * be allowed.   
    */
   protected Parser parser;

   //protected boolean hasChanged;
   protected String previousContents;   
   

   /**  
    * True if an error should be thrown
    * when checkInput() is called,
    * but the content of the box is not
    * a legal expression.  Otherwise, the
    * expression will become a constant
    * expression with value Double.NaN.   
    */
   protected boolean throwErrors;
                                      
   private Controller onUserAction;   // If this is non-null, the compute() method
                                      //   of onUserAction is called when the user
                                      //   presses return in this input-box.
   
   private Controller onTextChange;   // If this is non-null, the compute() method
                                      //   of onTextChange is called when the text
                                      //   in this input box changes
                                   
   /**
    * Error message from the most recent
    * time the input was checked by a
    * call to checkInput().  If this is
    * null, then no error occurred.
    */
   protected String errorMessage;
                                      
   private long serialNumber;         // This goes up by one every time checkInput()
                                      //   is called and finds a change in the
                                      //   user's input;


   /**
    * Create a new ExpressionFunction with no associated parser.  This can only
    * be used to input constant expressions, unless you set a parser later with setParser().
    */
   public ExpressionInput() {
      this("",null);
   }
   
   /**
    * Create an ExpressionInputBox with initial contents given by initialValue.
    * (If initialValue is null, the empty string is used.)  If p is not null,
    * then p will be used to parse the contents of the box.
    *
    * @param initialValue initial contents of ExpressionInputBox.
    * @param p if non-null, this parser will be used to parse contents of the ExpressionInputBox.
    */
   public ExpressionInput(String initialValue, Parser p) {
      super(30);
      expr = new EI();
      if (initialValue == null)
         initialValue = "";
      super.setText(initialValue);
      setBackground(Color.white);
      //enableEvents(KeyEvent.KEY_EVENT_MASK);
      setParser(p);  // (Sets previousContents to null, so checkInput() will actually check the input.)
      checkInput();  // Won't throw an error, since throwErrors is false.
      throwErrors = true;
   }

   /**   
    * Set the parser that is used to parse the user's input strings.
    * If this is null, then a default parser is used that will
    * only allow constant expressions.
    *
    * @param p parser to register with user's input strings.
    */
   public void setParser(Parser p) {
      parser = (p == null)? new Parser() : p;
      //hasChanged = true;  // force re-compute when checkInput() is next called.
      previousContents = null;
   }
   
   /**
    * Get the Expression associated with this ExpressionInput.
    *
    */
   public Expression getExpression() {
      return expr;
   }

   /**      
    * Get a function of one variable whose value at a real number
    * x is computed by assigning x to the variable v and then
    * returning the value of the expression associated with this
    * ExpressionInput.  Of couse, v should be one of the variables
    * registered with the Parser for this ExpressionInput, or else
    * in can never occur in the expression.
    *    Note that the value of the variable v changes TEMPORARILY
    * when the function is evaluated.  (So you should not use
    * a variable where setting the value has a side effect,
    * such as the variable associated with a SliderVariable.)
    *
    * @param v The function that is returned in a function of this variable.
    */    
   public Function getFunction(Variable v) {
      return new SimpleFunction(expr,v);
   }
      
   /**
    * Get a function of one or more variables whose value at arguments
    * x1, x2, ... is computed by assigning the x's to the variables and then
    * returning the value of the expression associated with this
    * ExpressionInput.  Of couse, each v[i] should be one of the variables
    * registered with the Parser for this ExpressionInput.
    *    Note that the value of the variables change TEMPORARILY
    * when the function is evaluated.
    *
    * @param v The function that is returned is a function of the variables in this array.
    */
   public Function getFunction(Variable[] v) {
      return new SimpleFunction(expr,v);
   }

   /**      
    * Return the current value of the expression associated with
    * this ExpressionInput.
    */
   public double getVal() {
      return expr.getVal();
   }
   
   /**   
    * If the parameter c is non-null, then its compute method will be called whenever
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
    * If the parameter, c, is non-null, then its compute method will be called whenever
    * the text in this input box changes.  Furthermore, the throwErrors
    * property will be set to false, to avoid throwing multiple errors
    * while the user is typing.  (You can change it back to true if
    * you want by calling setThrowErrors(true).)  It would only rarely make sense to
    * use this feature.
    */
   public void setOnTextChange(Controller c) {
      onTextChange = c;
      enableEvents(AWTEvent.TEXT_EVENT_MASK);
      if (c != null)
         throwErrors = false;
   }
   
   /**
    * Return the Controller, if any, that is notified whenever the text
    * in this input box changes
    */
   public Controller getOnTextChange() {
      return onTextChange;
   }

   /**   
    * Set the throwErrors property.  When this is true, a JCMError can be thrown
    * when checkInput() is called an a parse error is found in the contents of the input box.
    * If throwErrors is false, no error is thrown.  Instead,
    * the expression is set to a constant expression with value Double.NaN.
    */
   public void setThrowErrors(boolean throwErrors) {
      this.throwErrors = throwErrors;
   }
   
   /**
    * Return the value of the throwErrors property, which determines whether errors
    * can be thrown when checkInput() is called.
    */
   public boolean getThrowErrors() {
      return throwErrors;
   }

   /**   
    * Get error message from previous call to checkInput().
    * Returns null if and only if there was no error.
    */
   public String getErrorMessage() {
      return errorMessage;
   }
   
   
   //---------------- Some implementation details -------------------------------------------------

   /**      
    * Get the expression from the box, maybe throw a JBCError
    * if a ParseError occurs.  This is meant to be called by a Controller, in general.
    * The expression associated with this ExpressionInput can only change when this
    * method is called; it DOES NOT change continuously as the user types.
    */
   public void checkInput() {
      boolean hasChanged = previousContents == null || !previousContents.equals(getText());
      if (!hasChanged)
         return;
      expr.serialNumber++;
      String contents = getText();
      try {
         expr.exp = parser.parse(contents);
         errorMessage = null;
         previousContents = getText();
      }
      catch (ParseError e) {
         expr.exp = null;
         if (throwErrors) {
            errorMessage = "Error in expression: " + e.getMessage();
            setCaretPosition(e.context.pos);
            requestFocus();
            throw new JCMError(e.getMessage(),this);
         }
         else
            errorMessage = "Error in expression at position " + e.context.pos + ": " + e.getMessage();
      }
   }   

   /**   
    * Set the text displayed in this input box.  This overrides TextField.setText 
    * to make sure that the expression will be recomputed the next time
    * checkInput() is called.   
    */
   public void setText(String str) {
      super.setText(str);
      //hasChanged = true;
      previousContents = null;
   }

   /**
    * Override processKeyEvent to only allow characters
    * that are legal in expressions. This is not meant to be called directly. 
    
   public void processKeyEvent(KeyEvent evt) {
      if (evt.getID() == KeyEvent.KEY_PRESSED) {
         int ch = evt.getKeyCode();
         char chr = evt.getKeyChar();
         boolean use = (chr != 0 && (Character.isDigit(chr) || Character.isLetter(chr))
                            || chr == '.' || chr == '(' || chr == ')'
                            || chr == '-' || chr == '+' || chr == '*'
                            || chr == '/' || chr == '^' || chr == ','
                            || chr == ':' || chr == '?' || chr == '|'
                            || chr == '&' || chr == '~' || chr == '='
                            || chr == '<' || chr == '>' || chr == '!')
                            || ch == KeyEvent.VK_DELETE || ch == KeyEvent.VK_SPACE 
                            || ch == KeyEvent.VK_BACK_SPACE;
         boolean useControl = use || ch == KeyEvent.VK_TAB 
                                  || ch ==KeyEvent.VK_ENTER || chr == 0;
         if (!useControl) {
            evt.consume();
            Toolkit.getDefaultToolkit().beep();
         }
         else if (use)
            hasChanged = true;
      }
      super.processKeyEvent(evt);
   }
   */

   /**
    * Overridden to call onUserAction.compute() if onUserAction is non-null.
    * This is not meant to be called directly
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
      if (onTextChange != null)
         onTextChange.compute();
      super.processTextEvent(evt);
   }

   /**   
    * The expression associated with an ExpressionInput belongs to this class.
    * So is any derivative of such a function.  Note that derivatives
    * must be recomputed when the expression changes.  This is done
    * via "lazy evaluation", that is, only when necessary.  When
    * a derivative is used, it tests whether it is out of date
    * by comparing its serialNumber to the serial number of the
    * expression that it is the derivative of.  If they don't match,
    * then the expression is recomputed and the serial number is updated.
    * The serial number and defintion of the main expresssion is changed by
    * checkInput() whenever the user's input has changed.
    */
   protected class EI implements Expression {
      /**
       * The actual expression, or null if the
       * expression is undefined.  If this is a
       * derivative of another EI, this will be
       * recomputed as necessary when the expression is used
       * in some way.
       */
      ExpressionProgram exp;

      /**
       * This is null for the original expression input by the
       * user.  If this EI was formed by taking the derivative
       * of anotehr EI, that EI is stored here. 
       */
      EI derivativeOf;

      /**
       * Which Variable is this a derivative with respect to?
       * If derivativeOf is null, so is wrt.      
       */
      Variable wrt;

      /**
       * For the original expression input by the user, this
       * goes up by one each time checkInput() is called and
       * finds a change in the user's input.  For derivative 
       * EI, this is the serial number of "derivativeOf" at
       * the time this derivative expression was last computed.      
       */
      int serialNumber;
                         
      EI() {
          serialNumber = -1; // Forces exp to be computed the first time it is needed.
      }
      public double getVal() {
         checkForChanges();
         if (exp == null)
            return Double.NaN;
         return exp.getVal();
      }
      public double getValueWithCases(Cases c) {
         checkForChanges();
         if (exp == null)
            return Double.NaN;
         return exp.getValueWithCases(c);
      }
      public String toString() {
         checkForChanges();
         if (exp == null)
            return "(undefined)";
         return exp.toString();
      }
      public Expression derivative(Variable wrt) {
         EI deriv = new EI();
         deriv.derivativeOf = this;
         deriv.wrt = wrt;
         return deriv;
      }
      public boolean dependsOn(Variable x) {
         checkForChanges();
         return exp.dependsOn(x);
      }
      void checkForChanges() {
         if (derivativeOf != null) {
            derivativeOf.checkForChanges();
            if (serialNumber != derivativeOf.serialNumber) {
               serialNumber = derivativeOf.serialNumber;
               if (errorMessage != null)
                  exp = null;
               else
                  exp = (ExpressionProgram)derivativeOf.exp.derivative(wrt);
            }
         }
      }
   }  // end nested class EI
   
} // end class ExpressionInput

