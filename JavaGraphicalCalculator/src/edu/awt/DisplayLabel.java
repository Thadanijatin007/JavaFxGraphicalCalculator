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

package edu.awt;

import java.awt.*;

import edu.data.NumUtils;
import edu.data.Value;
import edu.data.*;

/**
 * A DisplayLabel is a label that can display numbers embedded in
 * strings.  The text for the label can include '#' characters, which
 * are substituted by values of specified Value objects.  (A doubled
 * ## is interpreted as a single literal # to be displayed rather than
 * substituted.)  You should provide as many Values as there
 * are #'s in the text.  However, no errors are generated if this
 * is not the case.  Extra expressions are ignored; extra #'s are
 * shown as "undefined" in the display.  In fact, DisplayLabels
 * do not ever generat JCMErrors.  Note that Value objects include
 * objects of type Constant, Variable, and Expression, for example.  Value
 * is just an interface defined in package edu.data.
 *
 * <p>The values displayed in a DisplayLabel are recomputed when
 * the Label's compute() method is called.  Usually, this is
 * done by a Controller that the DisplayLabel is registered with.
 * See the Controller class for more information.
 */
public class DisplayLabel extends Label implements Computable {

   /**
    * Unsubstituted text for display.
    */
   protected String text;
   
   /**
    * Desired maximum number of characters in displayed numbers.   
    */
   protected int numSize = 10;

   /**
    * Value objects whose values will be
    * substituted for #'s in text.   
    */
   protected Value[] values;

   /**                            
    * Create a label with no expressions set up to display a
    * single number.  Initial value is "undefined";  Use the
    * setValue() method to set the value to be displayed.
    */
   public DisplayLabel() {
      this(null,(Value[])null);
   }
   
   /**
    * Convenience method for making a DisplayLabel with just one value to display.
    *
    * @param text Text to display.  It shoud contain a single '#', which will be substituted by the value.
    * @param val a Value object whose value is substituted for the # in the text.
    */
   public DisplayLabel(String text, Value val) {
      this(text, (val == null)? null : new Value[] { val });
   }

   /** 
    * Create a DisplayLabel to display one or more values.
    * Text and vals can be null.  If not, text should have
    * as many (single) #'s as there are expressions.  The
    * values of the Value objects are substituted for the 
    * #'s in the display.
    *
    * @param text The text to display.  If this is null, it is set to "#".
    * @param vals The Value object(s) whose values are substituted for #'s in the text.  If this is null,
    *             the values shoud be set later by calling the setValues() method.
    */
   public DisplayLabel(String text, Value[] vals) {
      this.text = (text == null)? "#" : text;
      setValues(vals);
   }

   /**   
    * The compute method recalculates the displayed Values
    * and changes the text of the label to show the new values.
    * This is usually called by a Controller.
    */
   public void compute() {
      super.setText(getSubstitutedText());
   }

   /**   
    * Get the array of Value objects whose values are displayed
    * in this DisplayLabel.
    */
   public Value[] getValues() {
      return values;
   }
   
   /**
    *  A convenience method that can be used when the display string contains
    *  just a single #.  This sets the Value object whose value is substituted
    *  for that #.
    */
   public void setValue(Value val) {
      if (val == null)
         values = null;
      else
         values = new Value[] { val };
      super.setText(getSubstitutedText());
   }

   /**   
    * Set the array of Value objects whose values are displayed
    * in this DisplayLabel, and change the display to show
    * the new values.  (The contents of the array, vals, are
    * copied into a newly created array.)
    */
   public void setValues(Value[] vals) {
      if (vals == null)
         values = null;
      else {
         values = new Value[vals.length];
         System.arraycopy(vals,0,values,0,vals.length);
      }
      super.setText(getSubstitutedText());
   }

   /**   
    * Set the desired maximum number of characters in displayed numbers.
    * Actual size might be larger.  Value is clamped to the range
    * 6 to 25.
    */
   public void setNumSize(int size) {
      numSize = Math.min(Math.max(size,6),25);
   }
   
   /**
    * Return the desired maximum number of characters in displayed numbers.
    */
   public int getNumSize() {
      return numSize;
   }

   /**   
    * Return the basic text, including the #'s where Values
    * are inserted in the displayed text.  Note that the
    * getText() method from the Label class will return the actual 
    * displayed text, including the substitited values.
    */
   public String getBaseText() {
      return text;
   }

   /**   
    * Compute the string that is obtained by substituting values for #'s in text.
    * Will NOT throw any errors. (Any errors that occur when the
    * Value objects are evaluated are caught and translated
    * into "undefined" values.)
    */
   private String getSubstitutedText() {
      StringBuffer b = new StringBuffer();
      int valCt = 0;
      for (int i = 0; i < text.length(); i++) {
         if (text.charAt(i) == '#') {
            if (i != text.length() - 1 && text.charAt(i+1) == '#') {
               b.append('#');
               i++;
            }
            else if (values == null || valCt >= values.length)
                  b.append("undefined");
            else {
               try {
                  b.append(NumUtils.realToString(values[valCt].getVal(),numSize));
               }
               catch (JCMError e) {
                  b.append("undefined");
               }
               valCt++;
            }
         }
         else
            b.append(text.charAt(i));
      }
      return b.toString();
   }

   /**   
    * Set text for display -- text should include as many (single) #'s
    * as there are values to display.
    */
   public void setText(String text) {
      this.text = text;
      super.setText(getSubstitutedText());
   }

   /**   
    * Return the preferred size of this DisplayLabel.
    * Allow space for up to numSize (or 8, whichever is larger) characters for 
    * each (single) # in the text.  This is not meant to be called directly.
    */
   public Dimension getPreferredSize() {
      Dimension size = super.getPreferredSize();
      int ct = 0;  // Number of (single) #'1 in the text. 
      if (text == null || text.length() == 0)
         ct = 1;
      else {
         for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '#')
               if (i < text.length() - 1 && text.charAt(i+1) == '#')
                  i++;
               else
                  ct++;  
         }
      }
      FontMetrics fm = getFontMetrics(getFont());
      int perChar = fm.charWidth('0');
      int w = 10 + (int)(perChar * Math.max(8,numSize) * ct + fm.stringWidth(text));  // allowing extra space for numbers
      return new Dimension(w,size.height);
   }
   
} // end class DisplayLabel

