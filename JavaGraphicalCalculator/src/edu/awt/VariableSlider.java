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

import edu.data.Constant;
import edu.data.Parser;
import edu.data.Value;
import edu.data.Variable;
import edu.data.*;
import java.awt.*;
import java.awt.event.*;

/**
 * A VariableSlider is a slider (implemented as a Scrollbar) whose 
 * position represents the value of an associated variable. 
 * The range of values represented by the slider is given by a pair of
 * Value objects.  They can be specified in the constructor or later with the 
 * setMin and setMax methods.  A VariableSlider has an associated variable
 * that represents the value of the slider.  Note that the value of the variable
 * can change only when the setInput() or checkInput() method is called.
 * If you want the value of the variable to track the position
 * of the slider as it is is manipulated by the user, add the slider to
 * a Controller and set the Controller as the Slider's "onUserAction" property.
 * This allows other objects that depend on the values of the slider to 
 * be recomputed by the controller when the value changes, as long as they
 * are also registered with the Controller.
 *
 * <p>Some points to note:
 *
 * 1) setVal() can set a value outside the range from min to max,
 *    which will persist until the next time checkInput()
 *    or setVal() is called again.
 * 2) If the value of min or max changes, the value of this variable
 *    will not change EXCEPT that it is clamped to the range between min and max.
 * 3) Min does not have to be less than max.
 * 4) The checkInput() routine only sets the needValueCheck flag to true.
 *    (The setVal() and getVal() routines both set this flag to false.)  This "lazy evaluation" is used because
 *    checkInput() can't compute the new value itself. (The max and min
 *    might depend on Values that are themselves about to change when some
 *    other object's checkInput() mehtod is called.)
 * 5) getVal() returns the current value, as stored in the variable,
 *    UNLESS needValueCheck is true.  In that case, it recomputes
 *    the value first.  getSerialNumber() works similarly.  
 * 6) A VariableSlider never throws JCMErrors.  If an error occurs when min
 *    or max is evaluated, the value of the variable associated with this VariableSlider
 *    becomes undefined.  (The point is, it doesn't generate any errors of its own.
 *    The error would be caused by other InputObjects which should throw
 *    their own errors when their checkInput() methods are called.)
 */
public class VariableSlider extends Scrollbar implements InputObject, Tieable, Value {
   /**
    * The variable associated with this VariableSlider.
    * VS is a nested private class, defined below.
    */
   protected VS variable;
   
   /**
    * The Values that specify the range of values represented
    * by the slider.  min does not have to be less than max.
    */
   protected Value min, max;
                             
   private Controller onUserAction;   // If this is non-null, the compute() method
                                      //   of onUserAction is called when the user
                                      //   changes the position of the slider.

   /**
    * If this is true, then the value of the
    * variable associated with this slider is
    * an integer.  Furthermore, the number of
    * intervals on the slider is set to be
    * the same as the range of possible values
    * (unless this range is too big).   
    */
   protected boolean integerValued;

   /**
    * The number of possible value of the scrollbar
    * (Unless integerValued is true.)   
    */
   protected int intervals;

   /**
    * This increases every time the value of the variable changes.   
    */
   protected long serialNumber;

   /**
    * This is set to true when checkInput() is called
    * to indicate that the min and max values must be
    * checked the next time getVal() is called.
    */
   protected boolean needsValueCheck;

   /**
    * This is the position of the scrollbar the last time
    * getVal() or setVal() was called.  It is used to check
    * whether the user has repositioned the slider.                                       
    */
   protected int oldPosition;

   /**
    * The values found for min and max the last time
    * checkInput() was called.                               
    */
   protected double minVal = Double.NaN, maxVal;

   /**
    * Create a horizontal variable slider with no name and with a default
    * value range of -5 to 5. 
    */
   public VariableSlider() {
      this(null,null,null,null);
   }
   
   /**
    * Create a horizontal variable slider with no name and with the
    * specified range of values.  If min is null, a default
    * value -5 is used.  If max is null, a default value 5 is used.
    */
   public VariableSlider(Value min, Value max) {
      this(null,min,max,null);
   }

   /**   
    * Create a horizontal variable slider with the given name and range of 
    * values, and register it with the given parser (but only if
    * both name and p are non-null).   If min is null, a default
    * value -5 is used.  If max is null, a default value 5 is used.
    */
   public VariableSlider(String name, Value min, Value max, Parser p) {
      this(name,min,max,p,-1,Scrollbar.HORIZONTAL);
   }

   /**   
    * Create a variable slider with the given name and range of 
    * values, and register it with the given parser (but only if
    * both name and p are non-null).   The "intervals" parameter specifes
    * how many different positions there are on the slider.  (The value
    * of the scrollbar ranges from 0 to intervals.)  If intervals is <= 0, 
    * it will be set to 1000.  If it is between 1 and 9, it will be set to 10.
    * The orientation must be either Scrollbar.HORIZONTAL or Scrollbar.VERTICAL.
    * It specifies whether this is a horizontal or vertical scrollbar.
    * If min is null, a default value -5 is used.  If max is null, a default 
    * value 5 is used.
    *
    * @param name name for this VariableSlider.
    * @param min minimum value for slider.
    * @param max maximum value for slider.
    * @param p register VariableSlider with this Parser.
    * @param intervals discrete positions on slider.
    * @param orientation Scrollbar.HORIZONTAL or Scrollbar.VERTICAL.
    */
   public VariableSlider(String name, Value min, Value max, Parser p, int intervals, int orientation) {
      super(orientation);
      setBackground(Color.lightGray);
      setMin(min);
      setMax(max);
      if (intervals <= 0)
         intervals = 1000;
      if (intervals <= 10)
         intervals = 10;
      this.intervals = intervals;
      int visible = (intervals / 50) + 3;
      if (intervals < 100)
         setBlockIncrement(1);
      else
         setBlockIncrement(intervals/100);
      setValues(intervals/2,visible,0,intervals+visible);
      variable = new VS(name);
      if (name != null)
         super.setName(name);
      if (p != null && name != null)
         p.add(variable);
      needsValueCheck = true;  // Force getVal() to compute a new value for the variable.
      oldPosition = -1;
      getVal();
   }

   /**   
    * Set the name of the associated variable. You shouldn't do this
    * if it has been added to a parser.  If name is non-null, then
    * the name of this Component is also set to the specified name.   
    */
   public void setName(String name) {
      variable.setName(name);
      if (name != null)
         super.setName(name);
   }

   /**   
    * A convenience method that registers this VariableSlider's variable
    * with p (but only if both p and the name of the variable are non-null).
    */
   public void addTo(Parser p) {
       if (p != null && variable.getName() != null)
          p.add(variable);
   }
   
   /**
    * Return the variable associated with this VariableSlider.
    */
   public Variable getVariable() {
      return variable;
   }

   /**      
    * If set to true, restrict the values of the variable associated with this
    * slider to be integers.  Furthermore, the number of intervals on the
    * scrollbar will be set to be the same as the size of the range from 
    * min to max (unless this range is too big).  The setVal()
    * method can still set the value of the variable to be a non-integer.
    */
   public void setIntegerValued(boolean b) {
      integerValued = b;
      if (b && !Double.isNaN(minVal) && !Double.isNaN(maxVal))
         checkIntegerLimits(minVal,maxVal);
      needsValueCheck = true;
   }
   
   /**
    * Return a boolean which is true if the VariableSlider restricts ranges of values to integers, false otherwise.
    */
   public boolean getIntegerValued() {
      return integerValued;
   }

   /**   
    * Set the value that the variable has when the slider is at the left (or
    * bottom) of the scrollbar.   If v is null, -5 is used as the default value.
    */
   public void setMin(Value v) {
      min = (v == null)? new Constant(-5) : v;
   }

   /**   
    * Set the value that the variable has when the slider is at the right (or
    * top) of the scrollbar.   If v is null, 5 is used as the default value.
    */
   public void setMax(Value v) {
      max = (v == null)? new Constant(5) : v;
   }

   /**   
    * Get the Value object that gives the value of the variable when the slider is 
    * at the left (or bottom) of the scrollbar.  The Value is always non-null.
    */
   public Value getMin() {
      return min;
   }

   /**   
    * Get the Value object that gives the value of the variable when the slider is 
    * at the right (or top) of the scrollbar.  The Value is always non-null.
    */
   public Value getMax() {
      return max;
   }

   /**   
    * If the Controller, c, is non-null, then its compute method will be called whenever
    * the user adjusts the position of the scroll bar.
    */
   public void setOnUserAction(Controller c) {
      onUserAction = c;
      enableEvents(AWTEvent.ADJUSTMENT_EVENT_MASK);
   }

   /**
    * Method required by InputObject interface; in this class, it simply calls
    * setOnUserAction(c).  This is meant to be called by JCMPanel.gatherInputs().
    */
    public void notifyControllerOnChange(Controller c) {
       setOnUserAction(c);
    }

   /**   
    * Return the Controller, if any, that is notified when the user 
    * adjusts the position of the scroll bar.
    */
   public Controller getOnUserAction() {
      return onUserAction;
   }
   
   /**
    * Return this object's serial number, which is increased every time the
    * value changes.
    */
   public long getSerialNumber() {
      if (needsValueCheck)
         getVal(); // Make sure the value/serialNumber data is up-to-date.
      return serialNumber;
   }

   /**   
    * Change the value and serial number of this object to match
    * those of newest.  See the Tie class for more information.
    * This is not meant to be called directly
    */
   public void sync(Tie tie, Tieable newest) {
      if (newest != this) {
         if (! (newest instanceof Value) )
            throw new IllegalArgumentException("Internal Error:  A VariableSlider can only sync with Value objects.");
         setVal(((Value)newest).getVal());
         serialNumber = newest.getSerialNumber();
      }
   }

   /**   
    * Get the value of this VariableSlider.  (If needsValueCheck is
    * true, then the value is recomputed.  Otherwise, the current
    * value is returned.)
    */
   public double getVal() { 
      if (needsValueCheck) { 
          double newMinVal = Double.NaN;
          double newMaxVal = Double.NaN;
          boolean maxMinChanged = false;
          double value = variable.getVariableValue();  // Current value of the variable.
          try {  // Compute new max/min values.
             newMinVal = min.getVal();
             newMaxVal = max.getVal();
             if (!Double.isNaN(newMinVal) && !Double.isNaN(newMaxVal) && (newMinVal != minVal || newMaxVal != maxVal)) {
                if (integerValued)
                   checkIntegerLimits(newMinVal,newMaxVal);
                minVal = newMinVal;
                maxVal = newMaxVal;
                maxMinChanged = true;
             }
          }
          catch (JCMError e) {  // don't allow error to propagate
          }
          if (Double.isNaN(minVal) || Double.isNaN(maxVal) || Double.isInfinite(minVal) || Double.isInfinite(maxVal)) {
             variable.setVariableValue(Double.NaN);
             if (!Double.isNaN(value))
                serialNumber++; // Value has changed.
             setValue(0);
          }
          else if (oldPosition != getValue()) {  // Position of scroll bar has been changed by user,
                                                 //     so compute a new value for the variable.
             double newVal =  minVal + ((maxVal-minVal)*getValue())/intervals; 
             newVal = clamp(newVal, minVal, maxVal);  
             if (integerValued)
                newVal = Math.round(newVal);
             if (newVal != value) {
                variable.setVariableValue(newVal);
                serialNumber++;
             }
          }
          else if (!Double.isNaN(value) && maxMinChanged) {
                  // Max/min have changed, but user has not changed scroll bar position.
                  // Change the value only if that is necessary to clamp it to the min/max range.
                  // Possibly, we have to change the position of the scroll.
             double newVal = clamp(value,minVal,maxVal);
             if (newVal != value) {
                variable.setVariableValue(newVal);
                serialNumber++;
             }
             if (minVal != maxVal) {
                int pos = (int)( (value - minVal)/(maxVal - minVal)*intervals );
                setValue(pos);  
             }
          }
          oldPosition = getValue(); 
          needsValueCheck = false;        
      }
      return variable.getVariableValue();
   }

   /**   
    * Set the value of the variable to x.  If possible, set the
    * value on the scroll bar to match.
    */
   public void setVal(double x) {
      try {
         double minVal = min.getVal();
         double maxVal = max.getVal();
         if (Double.isNaN(x) || Double.isNaN(minVal) || Double.isNaN(maxVal) ||
                Double.isInfinite(x) || Double.isInfinite(minVal) || Double.isInfinite(maxVal)) {
         }
         else {
            if (integerValued) {
               minVal = Math.round(minVal);
               maxVal = Math.round(maxVal);
            }
            double xpos = clamp(x,minVal,maxVal);
            int pos = (int)((xpos-minVal)/(maxVal-minVal)*intervals);
            setValue(pos);  
         }
      }
      catch (JCMError e) {
      }
      variable.setVariableValue(x);
      needsValueCheck = false;
      oldPosition = getValue();
      serialNumber++;
   }
   
   
   // ------------------ Some implementation details -------------------------
   
   /**
    * From the InputObject interface.  This will force the slider to recompute
    * its max and min values, and possibly clamp its value between these two
    * extremes) the next time the value or serial number is checked.  This is
    * ordinarily called by a Controller.
    */
   public void checkInput() {
      needsValueCheck = true;
   }
   
   /**
    * Modify getPreferredSize to return a width of
    * 200, if the scrollbar is horzontal, or a height
    * of 200, if it is vertical.  This is not meant to
    * be called directly.
    */
   public Dimension getPreferredSize() {
      Dimension d = super.getPreferredSize();
      if (getOrientation() == Scrollbar.HORIZONTAL)
         return new Dimension(200, d.height);
      else
         return new Dimension(d.width,200);
   }
   
   
   private void checkIntegerLimits(double minVal, double maxVal) {
           // Called if integerValued is true to set values on scrollbar.
        int oldpos = getValue();
        minVal = Math.round( minVal );
        maxVal = Math.round( maxVal );
        double value = Math.round(variable.getVariableValue());
        double range = Math.abs(minVal-maxVal);
        if (range > 0 && range != intervals) { 
           intervals = (int)Math.min(range,10000);
           double v = clamp(value, minVal, maxVal);
           int pos = (int)((v-minVal)/(maxVal-minVal)*intervals);
           int visible = (intervals / 50) + 3;
           if (intervals < 10)
              setBlockIncrement(1);
           else if (intervals < 100)
              setBlockIncrement(intervals/10);
           else
              setBlockIncrement(10+intervals/100);
           setValues(pos,visible,0,intervals+visible);  
        }
        if (oldpos == oldPosition)
           oldPosition = getValue();
        else
           oldPosition = -1;
   }
   

   private double clamp(double val, double minVal, double maxVal) {
         // Utility routine used by setVal and getVal.  If val is
         // between minVal and maxVal, it returns val.  Otherwise,
         // it returns one of the endpoints, minVal or maxVal.
         // minVal can be greater than maxVal.
      double newVal = val;
      if (minVal < maxVal) {
         if (newVal < minVal)
            newVal = minVal;
         else if (newVal > maxVal)
            newVal = maxVal;
      }
      else {
         if (newVal < maxVal)
            newVal = maxVal;
         else if (newVal > minVal)
            newVal = minVal;
      }
      return newVal;
   }
   

   /**
    * Overridden to call onUserAction.compute() if onUserAction is non-null.
    * This is not meant to be called directly.
    */
   public void processAdjustmentEvent(AdjustmentEvent evt) {
      if (onUserAction != null)
         onUserAction.compute();
      super.processAdjustmentEvent(evt);
   }
   
   
   private class VS extends Variable {
         // A modified Variable class in which the getVal and
         // setVal methods are redirected to calls to the
         // getVal and setVal methods in the VariableSlider
         // class.  The methods getVariableValue and setVariableValue
         // provide access to the original getVal and setVal of
         // the variable class.
      VS(String name) {
         super(name);
      }
      public double getVal() {
         return VariableSlider.this.getVal();
      }
      public void setVal(double x) {
         VariableSlider.this.setVal(x);
      }
      void setVariableValue(double x) {
         super.setVal(x);
      }
      double getVariableValue() {
         return super.getVal();
      }
   }
   
} // end class VariableSlider

