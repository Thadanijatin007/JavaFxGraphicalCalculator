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
import edu.data.Value;
import edu.data.Variable;
import edu.data.*;
import java.awt.*;
import java.awt.event.*;

/**
 * An Animator can change a value continuously, without user intervention, by running
 * a separate Thread.  By default, an animator appears as a "Start" button.  When the
 * button is pressed, the value of the animator starts counting 0, 1, 2, ...  The button
 * changes to a "Stop" button.  When this is pressed, the value stops changing.  A Controller
 * can be set, by calling the setOnChange() method, to be notified whenever the value is
 * changed.  If this is done, then the value of the Animator will only change when its
 * checkInput() method is called, so it should be added to a Controller which will call
 * this method.
 *
 * <p>The getValueAsVariable() method can be called to get a Variable whose value is
 * the value of the Animator.  This variable can then be added to a Parser, so it can
 * be used in expressions.  An Animator is "Tieable", so it can share its value
 * with another InputObject, such as a VariableSlider or a VariableIput.
 *
 * <p>There are many options:  If maximum and minimum values are both specified, then the value
 * of the Animator ranges between these values.  By default, this interval is divided into
 * 100 sub-intervals, so that there are 101 frames.  However, the number of intervals can
 * also be set.  If no min or max is specified but a number of intervals is specified,
 * then the value is an integer which ranges from 0 up to the specified number of intervals.
 * If the number of frames is finite, then there are three possibities when the last
 * frame is reached:  The animation can stop; it can loop back to the the starting
 * frame, or it can reverse direction and cycle back and forth.  The behavior is controlled
 * with the setLoopStyle() method.
 *
 * <p>An Animator is actually a Panel which can contain other controls in addition to or
 * instead of the Start/Stop button.  For example, it can contain a "Next" button or
 * a pop-up menu to control the speed.
 * 
 */
public class Animator extends Panel
          implements Value, Tieable, InputObject, ActionListener, ItemListener, Runnable {

   /**
    * Used to add a component to the Animator Panel; can be used in a constructor
    * or in the addControl() method.  Can also be used in the getControl() method
    * to specify which component is to be retrieved.
    */
   public static final int START_STOP_BUTTON = 1,
                           START_BUTTON = 2,
                           PAUSE_BUTTON = 4,
                           STOP_BUTTON = 8,
                           NEXT_BUTTON = 16,
                           PREV_BUTTON = 32,
                           SPEED_CHOICE = 64,
                           LOOP_CHOICE = 128;
   
   /**
    * Indicates that the components in the Animator panel are to be stacked vertically.
    * (Can be used in a constructor and in the setOrientation method.)
    */
   public static final int VERTICAL = 1;

   /**
    * Indicates that the components in the Animator panel are to be in a horizontal row.
    * (Can be used in a constructor and in the setOrientation method.)
    */
   public static final int HORIZONTAL = 0;
   
   /**
    * Represents a loop style in which the animation is played once.  When the final frame
    * is reached, the animation ends.  Use in the setLoopStyle() method.
    */
   public static final int ONCE = 0;

   /**
    * Represents a loop style in which the animation is played repeatedly.  When the final frame
    * is reached, the animation returns to the first frame.  Use in the setLoopStyle() method.
    */
   public static final int LOOP = 1;

   /**
    * Represents a loop style in which the animation is cycled back and forth.  When the final frame
    * is reached, the animation reverses direction.  Use in the setLoopStyle() method.
    */
   public static final int BACK_AND_FORTH = 2;
   
   private Button startStopButton, startButton, stopButton,
                  pauseButton, nextButton, prevButton;
   private Choice speedChoice, loopChoice;
   private int orientation;
   private String startButtonName = "Start";
   private String stopButtonName = "Stop";
   
   private volatile int loopStyle;
   private boolean runningBackwards;
   private volatile int millisPerFrame = 100;
   private volatile int frame;
   private int maxFrame;
   private double value;
   
   private volatile long serialNumber = 1;
   
   private Computable onChange;
   
   private Value min, max; // If both are non-null, give the max and min values of the animator.
   private Value intervals;  // If non-null, gives the number of sub-intervals.  Number of frames is this value plus one.
   private boolean needsValueCheck = true;
   private double min_val, max_val; // Values of min and max.
   private int intervals_val; // Value of intervals.
   
   private static int START = 0, PAUSE = 1, NEXT = 2, PREV = 3, STOP = 4, RUN = 5; // possible values of thread status
   private Thread runner;
   private volatile int status = STOP;
   
   private boolean undefinedWhenNotRunning;
   
   /**
    * Create a default Animator.  If no changes are made by calling other methods, it will appear as
    * a Start/Stop button.  When Start is pressed, the value will count 0, 1, 2, 3, ..., until the Stop
    * button is pressed.  Restarting the animation starts the value again at zero.
    */
   public Animator() {
      this(START_STOP_BUTTON,HORIZONTAL);
   }
   
   /**
    * Create an Animator containing the specified control.  The parameter can consist of one or
    * more of the following constants, or'ed together:  START_STOP_BUTTON, START_BUTTON, STOP_BUTTON,
    * PAUSE_BUTTON, NEXT_BUTTON, PREV_BUTTON, SPEED_CHOICE, LOOP_CHOICE.  If no changes are made
    * by calling other methods, the value of the Animator will be 0, 1, 2, 3, ....  The components
    * are arranged into one horizontal row, using a GridLayout.
    */
   public Animator(int controls) {
      this(controls,HORIZONTAL,null,null,null);
   }
 
   /**
    * Create an Animator containing specified controls.  (See the one-parameter constructor.)
    * The second parameter should be one of the constants HORIZONTAL or VERTICAL, to specify
    * how the components are arranged in the Animator panel.
    */ 
   public Animator(int controls, int orientation) {
      this(controls,orientation,null,null,null);
   }
   
   /** Create an Animator with specified controls, orienation, range limits and number of intervals
    *
    * @param controls Specify the controls to add to the Animator.  Can consist of one or
    *      more of the following constants, or'ed together:  START_STOP_BUTTON, START_BUTTON, STOP_BUTTON,
    *      PAUSE_BUTTON, NEXT_BUTTON, PREV_BUTTON, SPEED_CHOICE, LOOP_CHOICE.
    * @param orientation How the controls are arranged in the panel.  One of the constants VERTICAL or HORIZONTAL.
    * @param min If BOTH min and max are non-null, they specify the range of values of the Animator.
    * @param max If BOTH min and max are non-null, they specify the range of values of the Animator.
    * @param intervals If non-null, specifies the number of intervals into which the range of values
    *      is divided.  Note that the value will be rounded to the nearest integer and clamped to the
    *      range 0 to 100000.   The number of frames is the number of intervals, plus one.  If min and max are
    *      non-null and intervals is null, then a default value of 100 is used.  If either min or max is
    *      null and intervals is non-null, then the Animator takes on the values 0, 1, 2, ..., intervals.
    */
   public Animator(int controls, int orientation, Value min, Value max, Value intervals) {
      this.min = min;
      this.max = max;
      this.intervals = intervals;
      this.orientation = orientation;
      if (orientation == VERTICAL)
         setLayout(new GridLayout(0,1));
      else
         setLayout(new GridLayout(1,0));
      for (int i = 1; i <= LOOP_CHOICE; i <<= 1)
         if ( (i & controls) != 0 )
            addControl(i);
   }
   
   //-------------- Accessor methods for public properties ------------------
   
   /**
    * Get one of controls associated with the Animator.  Usually, these are displayed
    * in the Animator panel, but you could get a control and add it to another panel if you
    * want.  Even if you do this, the control will still be managed by the Animator (which
    * will respond to it and enable/disable it, for example).  You might also want to get
    * one of the Animator's buttons so that you can change its label.  The value
    * of the parameter should be one of the constants START_STOP_BUTTON, START_BUTTON, STOP_BUTTON,
    * PAUSE_BUTTON, NEXT_BUTTON, PREV_BUTTON, SPEED_CHOICE, LOOP_CHOICE.  If the parameter is
    * not one of these values, then null is returned.
    */
   public Component getControl(int controlCode) {
      switch (controlCode) {
         case START_STOP_BUTTON:
            if (startStopButton == null) {
               startStopButton = new Button(startButtonName);
               startStopButton.setBackground(Color.lightGray);
               startStopButton.addActionListener(this);
            }
            return startStopButton;
         case START_BUTTON:
            if (startButton == null) {
               startButton = new Button(startButtonName);
               startButton.setBackground(Color.lightGray);
               startButton.addActionListener(this);
            }
            return startButton;
         case STOP_BUTTON:
            if (stopButton == null) {
               stopButton = new Button(stopButtonName);
               stopButton.setBackground(Color.lightGray);
               stopButton.addActionListener(this);
               stopButton.setEnabled(false);
            }
            return stopButton;
         case PAUSE_BUTTON:
            if (pauseButton == null) {
               pauseButton = new Button("Pause");
               pauseButton.setBackground(Color.lightGray);
               pauseButton.addActionListener(this);
               pauseButton.setEnabled(false);
            }
            return pauseButton;
         case NEXT_BUTTON:
            if (nextButton == null) {
               nextButton = new Button("Next");
               nextButton.setBackground(Color.lightGray);
               nextButton.addActionListener(this);
            }
            return nextButton;
         case PREV_BUTTON:
            if (prevButton == null) {
               prevButton = new Button("Prev");
               prevButton.setBackground(Color.lightGray);
               prevButton.addActionListener(this);
            }
            return prevButton;
         case SPEED_CHOICE:
            if (speedChoice == null) {
               speedChoice = new Choice();
               speedChoice.add("Fastest");
               speedChoice.add("Fast");
               speedChoice.add("Moderate");
               speedChoice.add("Slow");
               speedChoice.add("Slower");
               speedChoice.select(2);
               speedChoice.addItemListener(this);
            }
            return speedChoice;
         case LOOP_CHOICE:
            if (loopChoice == null) {
               loopChoice = new Choice();
               loopChoice.add("Play Once");
               loopChoice.add("Loop");
               loopChoice.add("Back and Forth");
               loopChoice.addItemListener(this);
            }
            return loopChoice;
         default:
            return null;
      }
   }
   
   /**
    * Add one of the possible control buttons or pop-up menus to the Animator. The possible values
    * of the parameter and their meanings are as follows:
    * <p>START_STOP_BUTTON: When clicked, animation starts and name of button changes; when clicked again, animation stops.
    * <p>START_BUTTON: When clicked, animation starts.
    * <p>STOP_BUTTON: When clicked, animaton stops.
    * <p>PAUSE_BUTTON: When clicked, animation is paused; this is different from stopping the animation since
    *   a paused animation can be resumed from the same point while a stopped animation can only be restarted
    *   from the beginning.
    * <p>NEXT_BUTTON: When clicked, the animation advances one frame; this is disabled when the animation is running.
    * <p>PREV_BUTTON: When clicked, the animation is moved back one frame;  this is disabled when the animation is running.
    * <p>SPEED_CHOICE: A pop-up menu whose value controls the speed at which the animation plays.
    * <P>LOOP_CHOICE: A pop-up menu that controls the style of animation, that is, what happens when the 
    *  animation reaches its final frame; values are Play Once, Loop, and Back and Forth.
    * <p>If the parameter is not one of these constants, then nothing is done.  Ordinarily, this
    * will be called during initialization.  (If you call it at some other time, you will have
    * to validate the panel yourself.)  The return value is the component that is added, or null
    * if the parameter value is not legal.
    */
   public Component addControl(int controlCode) {
      Component c = getControl(controlCode);
      if (c == null)
         return null;
      else {
         add(c);
         return c;
      }
   }
   
   /**
    * The name of the Start/Stop button is managed by the Animator, so changing it directly makes
    * no sense.  This method can be used to specify the label displayed by the Start/Stop button
    * when the animation is NOT running.  This name is also used for the Start button.  This method
    * should ordinarily be called during initialization.  In any case, it should not be called while
    * an animation is running, since it changes the name of the Start/Stop button to the specified value.
    */
   public void setStartButtonName(String name) {
      if (name != null) {
         startButtonName = name;
         if (startStopButton != null)
            startStopButton.setLabel(name);
         if (startButton != null)
            startButton.setLabel(name);  
      }
   }
   
   /**
    * The name of the Start/Stop button is managed by the Animator, so changing it directly makes
    * no sense.  This method can be used to specify the label displayed by the Start/Stop button
    * when the animation IS running.  This name is also used for the Stop button.  This method
    * should ordinarily be called during initialization.  In any case, it should not be called while
    * an animation is running, since it does not change the name  of the Start/Stop button.
    */
   public void setStopButtonName(String name) {
      if (name != null) {
         stopButtonName = name;
         if (stopButton != null)
            stopButton.setLabel(name);
      }
   }
   
   /**
    * Get the constant, VERTICAL or HORIZONTAL, that was used to specify whether the components
    * in the animator are arranged veritcally or horizontally.
    */
   public int getOrientation() {
      return orientation;
   }

   /**
    *  Set the orientation of the components in the Animator panel.  The parameter should be one
    *  of the constants HORIZONTAL or VERTICAL.  This just sets the layout for the panel to
    *  be a GridLayout with one row or one column and validates the panel.  You could also
    *  set the layout to be something else, such as a FlowLayout, using the setLayout() method.
    */   
   public void setOrientation(int orientation) {
      if (orientation != this.orientation && (orientation == HORIZONTAL || orientation == VERTICAL)) {
         this.orientation = orientation;
         if (orientation == VERTICAL)
            setLayout(new GridLayout(0,1));
         else
            setLayout(new GridLayout(1,0));
         validate();
      }
   }
   
   /**
    * Get the Value object that specifies the final value of the Animator.  This object can be null.
    */
   public Value getMax() {
      return max;
   }
   
   /**
    * Set the Value object that gives the final value of the Animator.  If both min and max are 
    * non-null, the value of the Animator ranges from min to max as the animation procedes.  (It is not required
    * that max be greater than min.  They should probably be calles startVal and endVal.)
    */
   public void setMax(Value max) {
      this.max = max;
      needsValueCheck = true;
   }
   
   /**
    * A convenience method that simply calls setMax(new Constant(d)).
    */
   public void setMax(double d) {
      setMax(new Constant(d));
   }
   
   /**
    * Get the Value object that specifies the starting value of the Animator.  This object can be null.
    */
   public Value getMin() {
      return min;
   }
   
   /**
    * Set the Value object that gives the starting value of the Animator.  If both min and max are 
    * non-null, the value ranges from min to max as the animation procedes.  (It is not required
    * that max be greater than min.)
    */
   public void setMin(Value min) {
      this.min = min;
      needsValueCheck = true;
   }
   
   /**
    * A convenience method that simply calls setMin(new Constant(d)).
    */
   public void setMin(double d) {
      setMin(new Constant(d));
   }
   
   /**
    * Get the Value object that specifies the number of frames in the animation.  This can be null.
    */   
   public Value getIntervals() {
      return intervals;
   }
   
   /**
    * Set the Value object that specifies the number of frames in the animation.  If non-null, then
    * the value is rounded to the nearest integer and clamped to the range 1 to 100000.  If it is
    * null and min and max are non-null, then a default value of 100 is used.  If it is null and
    * min or max is null, then the number of frames is unlimited and the values taken on by the
    * animator are 0, 1, 2, 3, ..., that is, the value of the animator is the frame number.
    * If min or max is null and intervals is non-null, then the values taken on by the
    * animator are 0, 1, 2, ..., intervals.  Note that the number of frames is (intervals+1).
    */
   public void setIntervals(Value intervals) {
      this.intervals = intervals;
      needsValueCheck = true;
   }
   
   /**
    * A convenience method that simply calls setIntervals(new Constant(d)).
    */
   public void setIntervals(int intervals) {
      setIntervals(new Constant(intervals));
   }
   
   /**
    * Get the nominal number of milliseconds per frame.  The actual time between frames
    * can be longer because of the work that is done processing the frame or on other tasks.
    */
   public int getMillisPerFrame() {
      return millisPerFrame;
   }
   
   /**
    * Set the nominal number of milliseconds per frame.  The actual time between frames
    * can be longer because of the work that is done processing the frame or on other tasks.
    * Values less than 5 are effectively equivalent to 5.  Realistic values are 25 or more,
    * but it depends on what system the program is running on and how complicated each frame is.
    */
   public void setMillisPerFrame(int millis) {
      millisPerFrame = millis;
   }
   
   /**
    * Get the loop style, which determines what happens when the final frame of the animation is reached.
    */
   public int getLoopStyle() {
      return loopStyle;
   }

   /**
    * Set the loop style, which determines what happens when the final frame of the animation is reached.
    * The parameter can be one of the constants: ONCE (animation stops when final frame is reached),
    * LOOP (animation cycles back to the first frame and continues from there); or BACK_AND_FORTH (animation reverses direction
    * and cycles back and forth).
    */   
   public void setLoopStyle(int style) {
      if (style >= 0 && style <= 2 && style != loopStyle) {
         loopStyle = style;
         if (loopChoice != null)
            loopChoice.select(style);
         runningBackwards = false;
      }
   }
   
   /**
    * Set the value of the animation.  Note that the value does not have to be one of
    * the values that would ordinarily occur in the animation.  Of course, if the animation
    * is running, then the new value won't be around for long since it will change as
    * soon as the next frame comes up.
    */
   synchronized public void setVal(double val) {
      if (needsValueCheck)
         checkValue(); // make sure min,max,intervals are evaluated if necessary
      value = val;
      serialNumber++;
      needsValueCheck = false;
      // Try to make the frame number match the value as closely as possible
      if (!Double.isNaN(val)) {
         if (min == null || max == null)
            frame = (int)Math.round(val);
         else if (!Double.isNaN(min_val) && !Double.isNaN(max_val) && min_val != max_val)
            frame = (int)Math.round( (val - min_val)/(max_val - min_val) * maxFrame );
         if (frame < 0)
             frame = 0;
         else if (maxFrame > 0 && frame > maxFrame)
            frame = maxFrame;
      }
   }
   
   /**
    * Get the current value of the Animator.
    */ 
   public double getVal() {
      if (needsValueCheck)
         checkValue();
      return value;
   }
   
   /**
    * Get a variable whose value is always equal to the value of the animator.
    * The name of the variable will be k.
    */
   public Variable getValueAsVariable() {
      return getValueAsVariable("k");
   }
   
   /**
    * Get a variable whose value is always equal to the value of the animator.
    * The name of the variable is specified by the parameter.
    */
   public Variable getValueAsVariable(String name) {
      return new Variable(name) {
         public void setVal(double val) {
            Animator.this.setVal(val);
         }
         public double getVal() {
            return Animator.this.getVal();
         }
      };
   }
   
   /**
    * Get the Controller that is notified (by calling its compute() method) whenever
    * the frame changes.  This can be null.
    */
   public Computable getOnChange() {
      return onChange;
   }
   
   /**
    * Set the Controller that is notified (by calling its compute() method) whenever
    * the frame changes.  If null, no Controller is notified.  NOTE:  Animators are
    * different from InputObjects in that when onChange is null, the value of animator
    * and its associated Variable will change without checkInput() being called.
    * However, if the onChange is not null, then checkInput() must be called for
    * the value to change. (So the Animation to be added to the Controller by
    * calling the Controller's add() method.  Then, the Controller will call
    * the checkInput() method.)
    */
   public void setOnChange(Computable onChange) {
      this.onChange = onChange;
   }
   
   /**
    * Method required by the InputObject interface.  It just calls setOnChange(c).
    * This is meant to be called by the gatherInputs() method in JCMPanel.
    */
   public void notifyControllerOnChange(Controller c) {
      setOnChange(c);
   }
   
   /**
    *  Get the value of the undefinedWhenNotRunning property.
    */
   public boolean getUndefinedWhenNotRunning() {
      return undefinedWhenNotRunning;
   }
   
   /**
    * Set the value of the undefinedWhenNotRunning property.  If this is true,
    * then the value of the Animator is Double.NaN except when the animation is
    * running (or paused), (or if it has been set by a call to the setVal() method).
    * The default value is false.
    */
   public void setUndefinedWhenNotRunning(boolean undefinedWhenNotRunning) {
      this.undefinedWhenNotRunning = undefinedWhenNotRunning;
   }
   
   //--------------- play control --------------------------------------
   
   /**
    *  Start the animation from the first frame, or continue it if it was paused.
    *  This is called when the Start button or Start/Stop button is pressed, but
    *  it could also be called directly.
    */
   synchronized public void start() {
      if (runner != null && runner.isAlive() && status == STOP){ 
              // A previous run is stopping.  Give it a chance to stop.
         try {
            wait(1000);
         }
         catch (InterruptedException e) {
         }
         if (runner != null && runner.isAlive()) {
            runner.stop();  // bad form, but what choice do I have?
            runner = null;
         }
      }
      if (runner == null || !runner.isAlive()) {
         runner = new Thread(this);
         status = START;
         runner.start();
      }
      else if (status != RUN) {
         status = START;
         notify();
      }
   }
   
   /**
    *  Pause the animation, if it is running.
    *  This is called when the Pause button is pressed, but
    *  it could also be called directly.
    */
   synchronized public void pause() {
      if (status == RUN) {
         status = PAUSE;
         notify();
      }
   }
   
   /**
    *  Advance the animation by one frame.  This will start the animation from the
    *  first frame if it is stopped.  This has no effect unless the animation is
    *  stopped or paused. This is called when the Next button  pressed, but
    *  it could also be called directly.
    */
   synchronized public void next() {
      if (runner == null || !runner.isAlive()) {
         runner = new Thread(this);
         status = NEXT;
         runner.start();
      }
      else if (status == PAUSE) {
         status = NEXT;
         notify();
      }
   }
   
   /**
    *  Advance the animation BACK one frame.  This will start the animation from the
    *  first frame if it is stopped.  This has no effect unless the animation is
    *  stopped or paused. This is called when the Prev button  pressed, but
    *  it could also be called directly.
    */
   synchronized public void prev() {
      if (runner == null || !runner.isAlive()) {
         runner = new Thread(this);
         status = PREV;
         runner.start();
      }
      else if (status == PAUSE) {
         status = PREV;
         notify();
      }
   }
   
   /**
    *  Stop the animation, if it is running or paused.  This is called when the Stop button 
    *  or the StartStop button is pressed, but it could also be called directly.
    *  NOTE: If the Animator is in an applet, then it is a good idea to call the stop()
    *  method of the Animator from the applet's destroy() method.
    */
   synchronized public void stop() {
      status = STOP;
      if (runner == null || !runner.isAlive())
         return;
      notify();
   }
   
   //-----------------Implementation details --------------------------

   /**
    * Respond to button clicks.  This is not meant to be called directly.
    */   
   synchronized public void actionPerformed(ActionEvent evt) {
       Object src = evt.getSource();
       if (src == null)
          return;
       if (src == startStopButton) {
          if (status == RUN)
             stop();
          else
             start();
       }
       else if (src == startButton) {
          start();
       }
       else if (src == stopButton) {
          stop();
       }
       else if (src == nextButton) {
          next();
       }
       else if (src == prevButton) {
          prev();
       }
       else if (src == pauseButton) {
          pause();
       }
   }
   
   /**
    * Respond to clicks on pop-up menus.  This is not meant to be called directly.
    */   
   synchronized public void itemStateChanged(ItemEvent evt) {
      if (evt.getSource() == loopChoice && loopChoice != null) {
         setLoopStyle(loopChoice.getSelectedIndex());
      }
      else if (evt.getSource() == speedChoice && speedChoice != null) {
         switch (speedChoice.getSelectedIndex()) {
            case 0: millisPerFrame = 0; break;
            case 1: millisPerFrame = 30; break;
            case 2: millisPerFrame = 100; break;
            case 3: millisPerFrame = 500; break;
            case 4: millisPerFrame = 2000; break;
         }
      }
   }
   
   /**
    * Part of the IputObject interface.  This is meant to be called by a Controller.
    */
   public void checkInput() {
      needsValueCheck = true;
   }
   
   /**
    *  Part of the Tieable interface.  This is meant to be called by other Tieable objects
    *  as part of object synchronization.
    */
   public long getSerialNumber() {
      if (needsValueCheck)
         checkValue();
      return serialNumber;
   }
   
   /**
    *  Part of the Tieable interface.  This is meant to be called by Tie objects
    *  as part of object synchronization.
    */
   public void sync(Tie tie, Tieable newest) {
      if (newest != this) {
         if (! (newest instanceof Value) )
            throw new IllegalArgumentException("Internal Error:  An Animator can only sync with Value objects.");
         setVal(((Value)newest).getVal());
         serialNumber = newest.getSerialNumber();
      }
   }
   
   synchronized private void checkValue() { // Recompute the value, which might have changed.
      double newVal;
      if (min != null)
         min_val = min.getVal();
      if (max != null)
         max_val = max.getVal();
      if (intervals == null)
         intervals_val = 0;
      else {
         double d= intervals.getVal();
         if (Double.isNaN(d) || d <= 0.5)
            intervals_val = 0;
         else if (d > 100000)
            intervals_val = 100000;
         else
            intervals_val = (int)Math.round(d);
      }
      maxFrame = intervals_val;
      if (min == null || max == null) { // value is frame number
         newVal = frame;
      }
      else if (Double.isNaN(min_val) || Double.isNaN(max_val) || Double.isInfinite(min_val) || Double.isInfinite(max_val)) {
         newVal = Double.NaN;
      }
      else if (intervals_val > 0) {
         newVal = min_val + (frame*(max_val-min_val)) / intervals_val;
      }
      else { // Assume 100 intervals if 
         maxFrame = 100;
         newVal = min_val + (frame*(max_val-min_val)) / 100;
      }
      if (undefinedWhenNotRunning && status == STOP)
         newVal = Double.NaN;
      value = newVal;
      needsValueCheck = false;
   }
   
   private void doControlStatus(int status) {  
        // Enable/disable buttons according to Thread status.
        // status should be START or STOP or PAUSE
     if (startStopButton != null)
        startStopButton.setLabel((status == START)? stopButtonName : startButtonName);
     if (startButton != null)
        startButton.setEnabled(status != START);
     if (stopButton != null)
        stopButton.setEnabled(status != STOP);
     if (nextButton != null)
        nextButton.setEnabled(status != START);
     if (prevButton != null)
        prevButton.setEnabled(status != START);
     if (pauseButton != null)
        pauseButton.setEnabled(status == START);
   }
   
   private void doAdvanceFrame(int amt) {   
         // Move on to next or previous frame.
         // amt is +1 or -1
      serialNumber++;
      if (loopStyle == BACK_AND_FORTH && runningBackwards)
         frame -= amt;
      else
         frame += amt;
      if (frame < 0) {
         if (loopStyle == LOOP)
            frame = maxFrame;  // might be 0, which is OK I guess
         else if (loopStyle == BACK_AND_FORTH) {
            frame = 1;
            if (amt == 1)
               runningBackwards = false;
            else
               runningBackwards = true;
         }
         else
            frame = 0;
      }
      else if (maxFrame > 0 && frame > maxFrame) {
         if (loopStyle == LOOP)
            frame = 1;  // Don't use 0, because usually frames 0 and maxFrame will be the same
         else if (loopStyle == ONCE) {
            frame = maxFrame;
            status = STOP;
            return; // exit at once
         }
         else { // loopStyle == BACK_AND_FORTH
            frame = maxFrame - 1;
            if (amt == 1)
               runningBackwards = true;
            else
               runningBackwards = false;
         }
      }
      if (onChange != null)
         onChange.compute();
      else
         needsValueCheck = true;
   }
   
   /**
    * The method that is run by the animation thread.  This is not meant to be called directly.
    */
   public void run() {
      int localstatus = status;
      long lastFrameTime = 0;
      runningBackwards = false;
      if (frame != 0 || undefinedWhenNotRunning) {
         frame = 0;
         serialNumber++;
         lastFrameTime = System.currentTimeMillis();
         if (onChange != null)
            onChange.compute();
         else
            needsValueCheck = true;
         if (status == PREV || status == NEXT)
            status = PAUSE;
      }
      try {
         while (true) {
            synchronized(this) {
               while (status == PAUSE) {
                  if (localstatus != PAUSE) {
                     doControlStatus(PAUSE);
                     localstatus = PAUSE;
                  }
                  try {
                     wait();
                  }
                  catch (InterruptedException e) {
                  }
               }
               if (status == STOP)
                  break;
               localstatus = status;
               if (needsValueCheck)
                  checkValue();  // make sure maxFrame is correct
            }
            if (localstatus == START) {
               doControlStatus(START);
               localstatus = status = RUN;
            }
            if (localstatus == RUN) {
               long sinceLastFrame = System.currentTimeMillis() - lastFrameTime;
               long waitTime = millisPerFrame - sinceLastFrame;
               if (waitTime <= 5)
                  waitTime = 5;
               try {
                  synchronized(this) {
                     wait(waitTime);
                  }
               }
               catch (InterruptedException e) {
               }
               lastFrameTime = System.currentTimeMillis();
               if (status == RUN)
                  doAdvanceFrame(1);
            }
            else if (localstatus == NEXT) {
               doAdvanceFrame(1);
               if (status != STOP)  // status can become stop in doAdvanceFrame
                  status = localstatus = PAUSE;
            }
            else if (localstatus == PREV) {
               doAdvanceFrame(-1);
               if (status != STOP)  // status can become stop in doAdvanceFrame (but shouldn't happen here)
                   status = localstatus = PAUSE;
            }
         }
      }
      finally {
         synchronized(this) {
            status = STOP;
            doControlStatus(STOP);
            frame = 0;
            serialNumber++;
            if (onChange != null)
               onChange.compute();
            else
               needsValueCheck = true;
            runner = null;
            notify(); // in case start() method is waiting for thread to end
         }
      }
   }
   
} // class Animator
