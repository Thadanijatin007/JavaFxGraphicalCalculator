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

import edu.awt.Controller;
import edu.awt.InputObject;
import edu.awt.Tie;
import edu.awt.Tieable;
import edu.data.Value;
import edu.data.Variable;
import edu.awt.*;
import edu.data.*;
import java.awt.*;
import java.awt.event.*;

/** 
 * A MouseTracker can be added to a CoordinateRect in a DisplayCanvas to respond to user
 * mouse actions in the rectangular area occupied by the CoordinateRect.  Two
 * Variable objects, which can be retrieved by calling getXVar() and getYVar(),
 * represent the location of the most recent mouse action in terms of the coordinates
 * of the CoordinateRect.  Note that these variables are Tieable objects, so they
 * can be synchronized with other means of inputting the same information.
 * The default names of the variables, if you don't change them, are "xMouse"
 * and "yMouse".
 * 
 * <p>A MouseTracker is an InputObject. The values of the variables associated with the
 * MouseTracker can change only when the checkInput() method is called (or when
 * the setVal() method of the variable is called to set its value explicitely).
 * If you want the value of the variables to track the mouse, you must 
 * add the MouseTracker (or the DisplayCanvas that contains it) to a Controller 
 * and set that Controller to listen for changes from the MouseTracker object by 
 * passing the Controller to the setOnUserAction() method of this class.
 * 
 */

public class MouseTracker extends Drawable implements MouseListener, MouseMotionListener, InputObject {

   /**
    * If true, the MouseTracker responds to both clicks and drags.
    * If false, it responds only to clicks.
    */
   protected boolean listenForDrags;   

   /**
    * If true, the values of the associated variables are
    * undefined except during the time that the user is
    * clicking and dragging the mouse.  This is ignored
    * if listenForDrags is false.
    */
   protected boolean undefinedWhenNotDragging;   

   /**
    * If this is non-null, then its compute() method is called
    * when the user clicks the mouse and, if listenForDrags is also
    * true, when the user drags and releases the mouse.
    */
   protected Controller onUserAction;
   
   /**
    * If thie is true, then the value of the variable associated with
    * the x-ccordinate of the mouse is clamped to lie within the
    * xmin and xmax of the coordinate rect.
    */                                     
   protected boolean clampX = true;  

   /**
    * If thie is true, then the value of the variable associated with
    * the y-ccordinate of the mouse is clamped to lie within the
    * ymin and ymax of the coordinate rect.
    */
   protected boolean clampY = true;   

   private MTVariable xVar, yVar;    //The variables associated with this MouseTracker.  The class MTVarible
                                     // is a private nested class defined below.

   private int xClick, yClick;   //Pixel where the mose recent user mouse action occured.
 
   private boolean inRect; //This is set to true while the user is dragging (if listenForDrags is true).
          
                          
   /**
    * Create a MouseTracker that responds to both clicks and drags.  The values of the
    * associated variables remain defined even after the user stops dragging.
    */
   public MouseTracker() {
      this(true,false);
   }
   
   /**
    * Creates a mouse tracker.  The first parameter specifies whether the values of
    * the variables change when the user drags the mouse, or only when the user clicks.
    * The second parameter is only used if the first is true.  It specifies whether
    * the values of the variables become undefined after the user stops dragging the
    * mouse.
    *
    */
   public MouseTracker(boolean listenForDrags, boolean undefinedWhenNotDragging) {
      this.listenForDrags = listenForDrags;
      this.undefinedWhenNotDragging = undefinedWhenNotDragging;
      xVar = new MTVariable(true);
      yVar = new MTVariable(false);
   }
   
   /**
    * Get the variable whose value represents the x-coordinate of the MouseTracker.
    * Note that this variable implements the Tieable interface, so can legally
    * be type-cast to type Tieable.  It can be tied to other objects that
    * implement the Tieable and Value interfaces to synchronize their values.
    */
   public Variable getXVar() {
      return xVar;
   }
   
   /**
    * Get the variable whose value represents the y-coordinate of the MouseTracker.
    * Note that this variable implements the Tieable interface, so can legally
    * be type-cast to type Tieable.  It can be tied to other objects that
    * implement the Tieable and Value interfaces to synchronize their values.
    */
   public Variable getYVar() {
      return yVar;
   }

   /**
    * Sets the "listenForDrags" property of the MouseTracker.
    * If set to true, then the MouseTracker responds to both clicks and drags if false,
    * it responds only to clicks.
    *
    */
   public void setListenForDrags(boolean listen) {
      if (listen != listenForDrags) {
         listenForDrags = listen;
         if (canvas != null) {
            if (listen)
               canvas.addMouseMotionListener(this);
            else
               canvas.removeMouseMotionListener(this);
         }
      }
   }
   
   /**
    * Gets the "listenForDrags" property of the MouseTracker, which determines
    * if the MouseTracker responds to both clicks and drags, or only to clicks.
    */
   public boolean getListenForDrags() {
      return listenForDrags;
   }
   
   /**
    * Sets the "undefinedWhenNotDragging" property of the MouseTracker.
    * This is ignored if the MouseTracker is not listening for drags.
    * If set to true, the values of the variables associated with this
    * variable become undefined when the user is not dragging.
    *
    */
   public void setUndefinedWhenNotDragging(boolean b) {
      undefinedWhenNotDragging = b;
   }
   
   /**
    * Gets the "undefinedWhenNotDragging" property of the MouseTracker.
    *
    */
   public boolean getUndefinedWhenNotDragging() {
      return undefinedWhenNotDragging;
   }
   
   /**
    * Set a Controller to respond to user mouse actions tracked
    * by this MouseTracker.  The MouseTracker should also be added
    * to the Controller, so that the values of its variables will
    * actually change when a user action occurs.
    *
    */
   public void setOnUserAction(Controller onUserAction) {
      this.onUserAction = onUserAction;
   }
   
   /**
    * Method required by InputObject interface; in this class, it simply calls
    * setOnUserAction(c).  This is meant to be called by JCMPanel.gatherInputs().
    */
    public void notifyControllerOnChange(Controller c) {
       setOnUserAction(c);
    }

   /**
    * Get the Controller that responds when a user mouse action is detected by this MouseTracker.
    */
   public Controller getOnUserAction() {
      return onUserAction;
   }
   
   /**
    * Set the "clampX" property of the MouseTracker.
    * If set to true, which is the default, the value of
    * the variable associated with the horizontal position of
    * the mouse is clamped to lie within the containing 
    * CoordinateRect.
    * 
    */
   public void setClampX(boolean clamp) {
      clampX = clamp;
   }
   
   /**
    * Get the "clampX" property of the MouseTracker.
    */
   public boolean getClampX() {
      return clampX;
   }
   
   /**
    * Set the "clampY" property of the MouseTracker.
    * If set to true, which is the default, the value of
    * the variable associated with the vertical position of
    * the mouse is clamped to lie within the containing 
    * CoordinateRect.
    *
    */
   public void setClampY(boolean clamp) {
      clampY = clamp;
   }
   
   /**
    * Get the "clampY" property of the MouseTracker.
    */
   public boolean getClampY() {
      return clampX;
   }
   
   
   //------------------ Implementation details --------------------------------------------
   
   /**
    * Set the values of the associated variables.  This is part of the InputObject interface,
    * and it is meant to be called by a Controller.
    */
   public void checkInput() {
      if (coords == null || (undefinedWhenNotDragging && !inRect)) { ;
         xVar.setVal(Double.NaN);
         yVar.setVal(Double.NaN);
      }
      else {
         double newX, newY;  // The new values.
         newX = coords.pixelToX(xClick);
         if (clampX) {
            if (newX < coords.getXmin())
               newX = coords.getXmin();
            else if (newX > coords.getXmax()) 
               newX = coords.getXmax();
         }
         xVar.setVal(newX);
         newY = coords.pixelToY(yClick);
         if (clampY) {
            if (newY < coords.getYmin())
               newY = coords.getYmin();
            else if (newY > coords.getYmax()) 
               newY = coords.getYmax();
         }
         yVar.setVal(newY);
      }
   }
   
   /**
    * A MouseTracker doesn't actually draw anything, but this method is required in 
    * a Drawable object.
    */
   public void draw(Graphics g, boolean coordsChanged) {
   }
   
   /**
    * This is called automatically by CoordinateRect when the
    * MouseTracker is added to the CoordinateRect.  It is not
    * meant to be used directly.
    *
    */
   protected void setOwnerData(DisplayCanvas canvas, CoordinateRect coords) {
      if (this.canvas != null) {
         canvas.removeMouseListener(this);
         canvas.removeMouseMotionListener(this);
      }
      this.canvas = canvas;
      this.coords = coords;
      canvas.addMouseListener(this);
      if (listenForDrags)
        canvas.addMouseMotionListener(this);
   }
   
   /**
    * Responds when the user clicks the mouse in the rectangular
    * area occupied by the CoordinateRect that contains this MouseTracker.
    * Since the MouseTracker listens for clicks on the whole DisplayCanvas
    * and the CoordinateRect might only occupy part of that, it is necessary
    * to check whether the user click was in that rect.  This is not meant to be called directly.
    *
    */
   public void mousePressed(MouseEvent evt) {
      if (evt.isConsumed() || coords == null)
         return;
      inRect = (evt.getX() >= coords.getLeft() && evt.getX() <= coords.getLeft() + coords.getWidth()
                   && evt.getY() >= coords.getTop() && evt.getY() <= coords.getTop() + coords.getHeight());
      if (!inRect)
         return;
      evt.consume();
      xClick = evt.getX();
      yClick = evt.getY();
      xVar.serialNumber++;
      yVar.serialNumber++;
      if (onUserAction != null)
         onUserAction.compute();
   }
   
   /**
    * Responds when the user releases the mouse.  This is not meant to be called directly.
    *
    */
   public void mouseReleased(MouseEvent evt) { 
      if (inRect == false)
         return;
      inRect = false;  
      if (listenForDrags && undefinedWhenNotDragging) {
         xVar.serialNumber++;
         yVar.serialNumber++;
         if (onUserAction != null)
            onUserAction.compute();
      }
   }

   /**
    * Responds when the user drags the mouse.  This is not meant to be called directly.
    * 
    */
   public void mouseDragged(MouseEvent evt) {
      if (listenForDrags && inRect) {
         xClick = evt.getX();
         yClick = evt.getY();
         xVar.serialNumber++;
         yVar.serialNumber++;
         if (onUserAction != null)
            onUserAction.compute();
      }
   }
   
   /**
    * Empty method, required by MouseListener interface.
    */
   public void mouseClicked(MouseEvent evt) { }  
   /**
    * Empty method, required by MouseMotionListener interface.
    */
   public void mouseEntered(MouseEvent evt) { }  
   /**
    * Empty method, required by MouseMotionListener interface.
    */
   public void mouseExited(MouseEvent evt) { }
   /**
    * Empty method, required by MouseMotionListener interface.
    */
   public void mouseMoved(MouseEvent evt) { }


   //The class to which the variables associated with this MouseTracker belong.
   
   private class MTVariable extends Variable implements Tieable {
      
      //True for xVar; false for yVar.
      private boolean isXVar; 
      
      //This object's serial number, which can 
      //change in MouseTracker.checkInput() as
      //well as in the setVal() and sync() methods in this class.
      long serialNumber;    
      
      //Create the variable.
      MTVariable(boolean isXVar) {
         super(isXVar? "xMouse" : "yMouse");
         this.isXVar = isXVar;
         super.setVal(Double.NaN);
      }
   
      
      //Set the value of the variable.  Note that the
      //value can be set to lie outside the coordinate rect,
      //even if clampX and clampY are true.  The next checkInput(),
      //however, will apply the clamp.
      public void setVal(double val) {
         if (isXVar) {
            if (coords != null)  // set xClick to match the value.
               xClick = coords.xToPixel(val);
         }
         else {
            if (coords != null)  // set yClick to match the value
               yClick = coords.yToPixel(val);
         }
         super.setVal(val);
      }
      
      // Return this Tieable object's serial number.
      public long getSerialNumber() {
         return serialNumber;
      }
      
      //Synchronize values and serial numbers with newest.
      public void sync(Tie tie, Tieable newest) {
         if ( ! (newest instanceof Value) )
            throw new IllegalArgumentException("Internal Error:  A MouseTracker variable can only be tied to a Value object.");
         if (newest != this) {
            setVal(((Value)newest).getVal());
            serialNumber = newest.getSerialNumber();
         }
      }
      
   }   // end nested class MTVariable


} // end class MouseTracker

