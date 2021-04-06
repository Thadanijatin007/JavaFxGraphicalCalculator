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


package edu.draw;

import java.awt.*;
import java.awt.event.*;

import edu.awt.Controller;
import edu.awt.InputObject;
import edu.awt.Tie;
import edu.awt.Tieable;
import edu.data.*;
import edu.data.*;
import edu.awt.*;

/**
 * A DraggablePoint can be added to a DisplayCanvas, where it appears as a small disk, square, or
 * cross.  (The visual style is a settable property.)  This object can be dragged with the mouse,
 * within the limits of the CoordinateRect that contains the DraggablePoint.  Either the x- or
 * y-value of the point can be clamped to a specified Value.  Typically, the y-value might
 * be given by some function of the x-value.  In that case, the point is constrained to move
 * along the graph of the function.  Or the x- or y-value can be clamped to a constant to make
 * the point move along a vertical or horizontal line.  Two Variables are associated with
 * the DraggablePoint.  These Variables represent the x- and y- values of the point.  Each Variable
 * implements the Tieable interface, so it can be synchronized with other Tieable values such as
 * a VariableIput or VariableSlider.
 */

public class DraggablePoint extends Drawable implements InputObject, Draggable {

   /**
    * A style constant that specifies the visual appearance of a DraggablePoint to be a disk.
    */
   public static final int DISK = 0;

   /**
    * A style constant that specifies the visual appearance of a DraggablePoint to be a square.
    */
   public static final int SQUARE = 1;

   /**
    * A style constant that specifies the visual appearance of a DraggablePoint to be a cross.
    */
   public static final int CROSS = 2;

   private int radius;    // Radius of the point.
   private Color color;   // Color of the point.
   private Color ghostColor;   // Color used for point when it is undefined or outside the CoordinateRect.
   private int style;     // One of the above style constants, DISK by default.
   private double xLoc, yLoc;  // The current x- and y-values of the point.
   private int xPosition, yPosition;  // The pixel position of the point.
   private boolean useGhost;   // This is true if the point is a "ghost" (undefined or outside the CoordinateRect).
   private DPV xVar, yVar;  // The Variables that represent the x- and y-values; DPV is a private nested class, defined below.
   private Controller onUserAction;  // A Controller whose compute method is called when the user drags the point.
   private Value clampX, clampY;  // Values used to clamp the x- and y-values.  Only one can be non-null.

   /**
    * Create a DraggablePoint with default values for style, radius, color.  The point appears as a dark gray disk of radius 4.
    */
   public DraggablePoint() {
      this(DISK);
   }
   
   /**
    * Create a DraggablePoint with specified visual style.  Radius is 4, color is darkGray, and
    * ghostColor is lightGray.
    *
    * @param style One of the style constants DraggablePoint.DISK, DraggablePoint.SQUARE, or DraggablePoint.CROSS.
    */
   public DraggablePoint(int style) {
      if (style >= 0 && style <= 2)
         this.style = style;
      setColor(Color.darkGray);
      setGhostColor(Color.lightGray);
      radius = 4;
      xPosition = -10000;
      xLoc = Double.NaN;
      yLoc = Double.NaN;
      xVar = new DPV(true);
      yVar = new DPV(false);
   }
   
   /**
    * Clamp the x-value of the point to v.  That is, if v is not null, then whenever the location of the point
    * changes, its x-value is modified to v.getVal().  Note that if v is non-null then any clamp Value
    * specified for y will be cleared since x and y cannot both be clamped.
    */
   public void clampX(Value v) {
      clampX = v;
      if (v != null)
         clampY = null;
      checkClamp();
      needsRedraw();
   }
   
   /**
    * Clamp the y-value of the point to v.  That is, if v is not null, then whenever the location of the point
    * changes, its y-value is modified to v.getVal().  Note that if v is non-null then any clamp Value
    * specified for x will be cleared since x and y cannot both be clamped.
    */
   public void clampY(Value v) {
      clampY = v;
      if (v != null)
         clampX = null;
      checkClamp();
      needsRedraw();
   }
   
   /**
    * Clamp the x-value of the point to the constant x, so that the point is constrained to a vertical line.
    */
   public void clampX(double x) {
      clampX(new Constant(x));
   }
   
   /**
    * Clamp the y-value of the point to the constant y, so that the point is constrained to a horizontal line.
    */
   public void clampY(double y) {
      clampY(new Constant(y));
   }
   
   /**
    * Clamp the x-value of the point to the function f, so that the point is constrained to move along the graph of x = f(y).
    * f must be a function of one variable.
    */
   public void clampX(Function f) {
      if (f != null)
         clampX(new ValueMath(f,xVar));
   }
   
   /**
    * Clamp the y-value of the point to the function f, so that the point is constrained to move along the graph of y = f(x).
    * f must be a function of one variable.
    */
   public void clampY(Function f) {
      if (f != null)
         clampY(new ValueMath(f,xVar));
   }
   
   /**
    * Get the radius used for drawing the point.  The point's height and width are given by two times the radius.
    */   
   public int getRadius() {
      return radius;
   }
   
   /**
    * Set the radius that determines the size of the point when it is drawn.
    * The point's height and width are given by two times the radius.
    */   
   public void setRadius(int r) {
      if (r > 0) {
         radius = r;
         needsRedraw();
      }
   }
   
   /**
    *  Set the visual style of the point.  The style should be one of the constants
    *  DraggablePoint.DISK, DraggablePoint.SQUARE, or DraggablePoint.CROSS.  If it is not,
    *  then nothing is done.
    */
   public void setStyle(int style) {
      if (style >= 0 && style <= 2) {
         this.style = style;
         needsRedraw();
      }
   }
   
   /**
    *  Get the visual style of the point, which must be one of the constants
    *  DraggablePoint.DISK, DraggablePoint.SQUARE, or DraggablePoint.CROSS.
    */
   public int getStyle() {
      return style;
   }
   
   /**
    * Get the variable that represents the current x-value of the point. (Note that this
    * variable can be type-cast to type Tieable.)
    */
   public Variable getXVar() {
      return xVar;
   }
   
   /**
    * Get the variable that represents the current y-value of the point. (Note that this
    * variable can be type-cast to type Tieable.)
    */
   public Variable getYVar() {
      return yVar;
   }
   
   /**
    * Get the color used for drawing the point.
    */
   public Color getColor() {
      return color;
   }
   
   /**
    * Set the color to be used for drawing the point.  If the specified Color value is
    * null, then nothing is done.
    */
   public void setColor(Color c) {
      if (c != null) {
         color = c;
         needsRedraw();
      }
   }
   
   /**
    * Get the "ghostColor" of the point. This color is used for drawing the point when its x-value
    * or y-value is undefined or outside the range of values on the CoordinateRect that contains
    * the point.  (This can happen because of clamping of values.  It can also happen if the limits
    * on the CoordinateRect are changed.)
    */
   public Color getGhostColor() {
      return ghostColor;
   }
   
   /**
    * Set the ghoseColor to be used for drawing the point when it location is undefined or is outside the
    * proper limits.  If the specified Color value is null, then nothing is done.
    */
   public void setGhostColor(Color c) {
      if (c != null) {
         ghostColor = c;
         needsRedraw();
      }
   }
   
   /**
    * Set the Controller that is to be notified when the user drags the point.  (The compute() method
    * of the Controller is called.)  If the Controller value is null, then no notification is done.
    */
   public void setOnUserAction(Controller c) {
      onUserAction = c;
   }
   
   /**
    * Method required by InputObject interface; in this class, it simply calls
    * setOnUserAction(c).  This is meant to be called by JCMPanel.gatherInputs().
    */
    public void notifyControllerOnChange(Controller c) {
       setOnUserAction(c);
    }

   /**
    * Get the Controller that is notified when the user drags the point.  A null value means that
    * no notification is done.
    */
   public Controller getOnUserAction(Controller c) {
      return onUserAction;
   }
   
   /**
    *  Move the point to (x,y), then "clamp" the value of x or y, if a clamp Value has been set.
    */
   public void setLocation(double x, double y) {
      xLoc = x;
      yLoc = y;
      xVar.setVariableValue(x);
      yVar.setVariableValue(y);
      xVar.serialNumber++;
      yVar.serialNumber++;
      checkClamp();
      needsRedraw();
   }
   
   private void checkClamp() {  
         // Apply the clamping values.
      if (clampX != null) {
         xLoc = clampX.getVal();
         xVar.setVariableValue(xLoc);
      }
      else if (clampY != null) {
         yLoc = clampY.getVal();
         yVar.setVariableValue(yLoc);
      }
   }

   /**
    *  This method is required by the InputObject interface.  In this case, it just applies the
    *  clamping Values if any are specified.
    */   
   public void checkInput() {
      xVar.needsClamp = true;
      yVar.needsClamp = true;
   }
   
   /**
    *  This method, from the Drawable interface, draws the point.  It is not usually called directly.
    */
   public void draw(Graphics g, boolean coordsChanged) {
      if (coords == null)
         return;
      checkPosition();
      if (useGhost)
         g.setColor(getGhostColor());
      else
         g.setColor(color);
      switch (style) {
         case DISK:
            g.fillOval(xPosition-radius,yPosition-radius,2*radius+1,2*radius+1);
            break;
         case SQUARE:
            g.fillRect(xPosition-radius,yPosition-radius,2*radius+1,2*radius+1);
            break;
         case CROSS:
            g.drawLine(xPosition-radius,yPosition,xPosition+radius,yPosition);
            g.drawLine(xPosition,yPosition-radius,xPosition,yPosition+radius);
            break;
      }
   }
   
   private void checkPosition() {
          // compute (xPosition, yPosition), the position where point is actually drawn
      useGhost = false;
      xVar.getVal();  // Forces recompute, if needsClamp
      yVar.getVal();
      if (Double.isNaN(xLoc) || Double.isNaN(yLoc)) {
         if (xPosition == -10000) {  // otherwise, use previous position
            xPosition = coords.getLeft() + coords.getWidth()/2;
            yPosition = coords.getTop() + coords.getHeight()/2;
         }
         useGhost = true;
      }
      else {
         xPosition = coords.xToPixel(xLoc);
         yPosition = coords.yToPixel(yLoc);
      }
      if (xPosition <= coords.getLeft()) {
         useGhost = true;
         xPosition = coords.getLeft() + 1;
      }
      else if (xPosition >= coords.getLeft() + coords.getWidth()) {
         useGhost = true;
         xPosition = coords.getLeft() + coords.getWidth() - 1;
      }
      if (yPosition <= coords.getTop()) {
         useGhost = true;
         yPosition = coords.getTop() + 1;
      }
      else if (yPosition >= coords.getTop() + coords.getHeight()) {
         useGhost = true;
         yPosition = coords.getTop() + coords.getHeight() - 1;
      }
   }
   
   //------------------ Dragging the point ---------------------------
   
   private boolean dragging; // True if the point is being dragged.
   
   /**
    *  Check whether a mouse click (as specified in the MouseEvent parameter) is a 
    *  click on this DraggablePoint.  If so, return true, and start a drag operation.
    *  It is expected that the continueDrag() and finishDrag() will be called to
    *  complete the drag operation.  This is only meant to be called from
    *  the checkDraggables() method in class CoordinateRect.
    */
   public boolean startDrag(MouseEvent evt) {
      dragging = false;
      if (evt.isConsumed() || !getVisible() || coords == null)
         return false;
      checkPosition();
      if (evt.getX() < xPosition - radius || evt.getX() >= xPosition + radius
                      || evt.getY() < yPosition - radius || evt.getY() >= yPosition + radius)
         return false;
      dragging = true;
      evt.consume();
      return true;
   }
   
   /**
    *  Continue a drag operation begun in startDrag().  This is not meant to be called directly.
    */
   public void continueDrag(MouseEvent evt) {
      if (!dragging)
         return;
      int xInt = evt.getX();
      int yInt = evt.getY();
      double x = coords.pixelToX(evt.getX());
      double y = coords.pixelToY(evt.getY());
      if (x < coords.getXmin())
         x = coords.getXmin();
      else if (x > coords.getXmax())
         x = coords.getXmax();
      if (y < coords.getYmin())
         y = coords.getYmin();
      else if (y > coords.getYmax())
         y = coords.getYmax();
      setLocation(x,y);
      if (Double.isNaN(xLoc) || Double.isNaN(yLoc)) {
         xPosition = xInt;
         yPosition = yInt;
      }
      if (onUserAction != null)
         onUserAction.compute();
   }
      
   /**
    *  Finish a drag operation begun in startDrag().  This is not meant to be called directly.
    */
   public void finishDrag(MouseEvent evt) {
       dragging = false;
   }
      
   private class DPV extends Variable implements Tieable {
         
      private boolean isXVar;   // True for xVar; false for yVar.
      
      long serialNumber;  // This object's serial number.
      boolean needsClamp; // Set to true by DraggablePoint().checkInput().
      
      DPV(boolean isXVar) {
            // Create the variable.
         super(isXVar? "xDrag" : "yDrag");
         this.isXVar = isXVar;
         super.setVal(Double.NaN);
      }
      
      public double getVal() {
           // Return the value, after applying clamping, if necessary.
           // (It's done this way because checkInput() can't use values of
           // other objects, but after it's called, any call to getVal()
           // should return the new correct value.)
         if (needsClamp) {
            if (isXVar) {
               if (clampX != null) {
                  xLoc = clampX.getVal();
                  setVariableValue(xLoc);
               }
            }
            else {
               if (clampY != null) {
                  yLoc = clampY.getVal();
                  setVariableValue(yLoc);
               }
            } 
            needsClamp = false;
         }
         return super.getVal();
      }

      public void setVal(double val) {
            // Set the value of the variable, and set the point's
            // location to reflect new value.  (setLocation ups serial number
            // and calls setVariableValue() to set the actual variable value.)
         if (isXVar)
            setLocation(val,yVar.getVal());
         else
            setLocation(xVar.getVal(),val);
      }
      
      void setVariableValue(double val) {
            // Call the setVal() routine from the superclass.
         super.setVal(val); 
         needsClamp = false;
      }
      
      public long getSerialNumber() {
              // Return this Tieable object's serial number.
         return serialNumber;
      }
      
      public void sync(Tie tie, Tieable newest) {
             // Synchronize values and serial numbers with newest.
         if ( ! (newest instanceof Value) )
            throw new IllegalArgumentException("Internal Error:  A MouseTracker variable can only be tied to a Value object.");
         if (newest != this) {
            setVal(((Value)newest).getVal());
            serialNumber = newest.getSerialNumber();
         }
      }
      
   }
      

} // end class DraggablePoint

