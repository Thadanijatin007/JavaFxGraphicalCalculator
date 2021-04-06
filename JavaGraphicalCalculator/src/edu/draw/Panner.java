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


/**
 *  When a Panner object is added to a CoordinateRect, it becomes possible to 
 *  "grab" the coordinate rectangle and pan it (that is, slide it around by 
 *  moving it with the mouse).  By default, the user must right-click-and-drag
 *  to pan the coordinates, but this can be changed by providing an argument to
 *  the constructor.  It is possible to set a Controller to be notified each time
 *  the mouse moves while the user is dragging.  Alternatively, or in addition,
 *  you can set a Controller to be notified when the user finishes dragging.
 *  However, for the most part this is unnecessary, since the Drawables in the
 *  CoordinateRect will for the most part redraw themselves properly when the
 *  limits on the CoordinateRect change.  However, if you have Computable objects 
 *  that depend on the coordinate limits, then they will need to be recomputed.
 *  (This will be the case if you use value objects returned by the
 *  getValueObject() method in the CoordinateRect class.)
 *  <p>A Panner, p, is inactive if its "visible" property has been set to false.
 *  (This is done by calling p.setVisible(false).)
 */
public class Panner extends Drawable implements MouseListener, MouseMotionListener {

   private int modifiers;  // Combination of MouseEvent.SHIFT_MASK, MouseEvent.CTRL_MASK,
                           //      MouseEvent.META_MASK, and MouseEvent.ALT_MASK that must be
                           //      present in the mouse-pressed event for a drag to start.
                           
   private Controller onUserAction;  // notified each time the mouse moves during a drag
   private Controller onFinishDrag;  // notified when the user finishes a drag
   
   /**
    *  Create a Panner object that will let the user pan the CoordinateRect 
    *  that contains the Panner by
    *  right-clicking and dragging (or, on Macintosh, command-clicking).
    */
   public Panner() {
      this(MouseEvent.META_MASK);
   }
   
   
   /**
    *  Create a Panner object that will let the user click-and-drag to pan the CoordinateRect
    *  that contains the Panner.  The mouse-pressed event must have the specfied set of
    *  modifiers set.
    *
    *  @param modifiers If the value is zero, the user drags the CoordinateRect by clicking without
    *           pressing any modifier keys.  Otherwise, the value should be a combination of
    *           one or more of the constants MouseEvent.SHIFT_MASK, MouseEvent.CTRL_MASK,
    *           MouseEvent.META_MASK, and MouseEvent.ALT_MASK, or'ed together.  (Remember
    *           that right-clicking sets META_MASK and clicking with a middle mouse button
    *           sets ALT_MASK.)
    *  
    */ 
   public Panner(int modifierSet) {
      modifiers = modifierSet & (MouseEvent.SHIFT_MASK | MouseEvent.CTRL_MASK
                                    | MouseEvent.META_MASK | MouseEvent.ALT_MASK);
   }
   
   /**
    *  Set a Controller that will be notified (by calling its compute method) whenever
    *  the user moves the mouse during a drag.  If the value is null, no Controller is
    *  notified.  Note that Drawables generally redraw themselvs correctly during the
    *  drag anyway, without any Controller being involved.  Even if there are other
    *  things that need to be computed, it's probably better to compute them only once
    *  at the end of the drag.  Do this by calling setOnFinishDrag() instead of this method.
    */
   public void setOnUserAction(Controller c) {
      onUserAction = c;
   }
   
   /**
    *  Get the Controller that is notified when the user moves the mouse during a drag.
    *  Returns null if no Controller is notified.
    */ 
   public Controller getOnUserAction() {
      return onUserAction;
   }
   
   /**
    *  Set a Controller that will be notified (by calling its compute method) whenever
    *  the user finishes a drag operation.  If the value is null, no Controller is notified.
    *  You only need to do this if you have to recompute some object that depends on the
    *  coordinate limits of the CoordinateRect that contains this Panner object.
    *  Presumably, this will be an object that depends on one if the Value objects returned
    *  by the getValueObject() method in the CoordinatRect class.
    */
   public void setOnFinishDrag(Controller c) {
      onFinishDrag = c;
   }
   
   /**
    *  Get the Controller that is notified when the user finishs a drag.
    *  Returns null if no Controller is notified.
    */ 
   public Controller getOnFinishDrag() {
      return onFinishDrag;
   }
   
   /**
    *  Called when this object is added to a DisplayCanvas.  Not meant to be called directly.
    */
   protected void setOwnerData(DisplayCanvas canvas, CoordinateRect coords) {
         // Called automatically when this object is added to canvas.
      if (canvas != null) {
         canvas.removeMouseListener(this);
         canvas.removeMouseMotionListener(this);
      }
      super.setOwnerData(canvas,coords);
      if (canvas != null) {
         canvas.addMouseListener(this);
         canvas.addMouseMotionListener(this);
      }
   }

   /**
    * Override the abstract draw() method from the Drawable class.  This
    * is defined to be empty since a Panner object has no visible representation.
    */
   public void draw(Graphics g, boolean coordsChanged) {
   }
   
   private boolean dragging;
   private int prevX, prevY;

   /**
    *  Responds to a mouse-press.  Not meant to be called directly.
    */   
   public void mousePressed(MouseEvent evt) {
      dragging = false;
      if (evt.isConsumed())
         return;
      if (!getVisible() || canvas == null || coords == null)
         return;
      if ( (evt.getModifiers() & modifiers) != modifiers )
         return;
      prevX = evt.getX();
      prevY = evt.getY();
      if (prevX < coords.getLeft() || prevX >= coords.getLeft() + coords.getWidth()
           || prevY < coords.getTop() || prevY >= coords.getTop() + coords.getHeight())
         return;
      evt.consume();
      dragging = true;
   }

   /**
    *  Responds to a mouse-drag.  Not meant to be called directly.
    */   
   public void mouseDragged(MouseEvent evt) {
      if (!dragging)
         return;
      evt.consume();
      if (evt.getX() == prevX && evt.getY() == prevY)
         return;
      double[] limits = coords.getLimits();
      if (limits == null)
         return;
      double xOffset = (evt.getX() - prevX) * coords.getPixelWidth();
      double yOffset = (evt.getY() - prevY) * coords.getPixelHeight();
      coords.setLimits(limits[0] - xOffset, limits[1] - xOffset, limits[2] + yOffset, limits[3] + yOffset);
      needsRedraw();
      if (onUserAction != null)
         onUserAction.compute();
      prevX = evt.getX();
      prevY = evt.getY();
   }

   /**
    *  Responds to a mouse-release.  Not meant to be called directly.
    */   
   public void mouseReleased(MouseEvent evt) {
      if (!dragging)
         return;
      evt.consume();
      mouseDragged(evt);
      dragging = false;
      if (onFinishDrag != null)
         onFinishDrag.compute();
   }
   
   /**
    *  Responds to a mouse-click.  Not meant to be called directly.
    *  Defined to be empty in this class.
    */   
   public void mouseClicked(MouseEvent evt) { }
   
   /**
    *  Responds when mouse moves.  Not meant to be called directly.
    *  Defined to be empty in this class.
    */   
   public void mouseMoved(MouseEvent evt) { }
   
   /**
    *  Responds to a mouse-enter event.  Not meant to be called directly.
    *  Defined to be empty in this class.
    */   
   public void mouseEntered(MouseEvent evt) { }

   /**
    *  Responds to a mouse-exit event.  Not meant to be called directly.
    *  Defined to be empty in this class.
    */   
   public void mouseExited(MouseEvent evt) { }

} // end class Panner