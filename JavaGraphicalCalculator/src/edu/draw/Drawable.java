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

import java.awt.Graphics;

/**
 * A Drawable object can be added to a CoordinateRect, which is itself in 
 * a DisplayCanvas.  Its purpose is, generally, to draw something in the
 * rectangular area represented by the CoordinateRect.  The drawing can
 * use information in the CoordinateRect, which includes both the real
 * number coordinates and the pixel coordinates of the rectangular area.
 */
abstract public class Drawable implements java.io.Serializable {
   
   /**
    * The CoordinateRect for the rectagular area where this
    * Drawable is drawn.  This is set automatically when the
    * Drawable is added to a CoordingteRect and should not be
    * changed.  (It will be changed automatically if the 
    * Drawable is removed from the CoordinateRect.)
    */
   protected CoordinateRect coords;

   /**
    * The canvas on which this Drawable is drawn.  This is set
    * automatically when the Drawable is added to a CoordinateRect
    * and it should not be changed.  (It will be changed automatically
    * if the Drawable is removed from the CoordinateRect.)
    */
   protected DisplayCanvas canvas;
                            
   private boolean visible = true;  // If visible is false, then the CoordinateRect
                                    // that manages this Drawable will ignore it.
                                    // It will not call the draw() routine, so when
                                    // draw is called, it can be assumed that the
                                    // Drawable is visible.
                                    
   /**
     * Draw this drawable in the graphics context g.  This is meant to
     * be called only by the CoordinateRect, coords, that manages this Drawable.
     * The coords contains information about the rectangular area in which
     * this Drawable is displayed, both in terms of pixels and in terms
     * of real (x,y)-coordinates.
     *    The value of coordsChanged is true if any of the values
     *  coords.getXmin(), coords.getXmax(), coords.getYmin(), coords.getYmax(), 
     * coords.getLeft(), coords.getRight(), coords.getTop(), coords.getBottom(), 
     * or coords.getGap() has changed.  Drawables that depend only on this
     * information can check the value of coordsChanged to see whether they
     * need to update any previously computed member variables that
     * depend on these values.  This method is meant to be called only by the system.
     * 
     * @param g The graphics context in which the Drawble is to be drawn.  (The drawing 
     *            can change the color in g, but should not permanently change font, painting mode, etc.
     *            Thus, every drawable is responsible for setting the color it wants to use.)
     * @param coordsChanged Indicates whether the CoordinateRect has changed.
     */
   abstract public void draw(Graphics g, boolean coordsChanged);

   /**
     * Return true if this Drawable is visible, false if it is hidden.
     * A hidden Drawable is ignored by the CoordinateRect that manages it.
     */
   public boolean getVisible() {
      return visible;
   }
   
  /**
   * Set the visibility of this Drawable.  If show is false, then
   * the Drawable is hidden.  If it is true, the Drawable is shown.
   */
   public void setVisible(boolean show) {
      if (show != visible) {
          visible = show;
          needsRedraw();
      }
   }

   /**
    * This routine should be called if the appearance of the Drawable changes
    * so that the rectangular area that it occupies has to be redrawn.
    * The routine is generally meant to be called by the Drawable itself.
    * It will notify the DisplayCanvas, canvas, that the CoordinateRect,
    * coords, needs to be redrawn, where canvas and coords are the member
    * variables in this class.  If canvas is null, nothing happens, since
    * presumably the Drawable is not displayed anywhere in that case.
    */
   public void needsRedraw() {
       if (canvas != null)
          canvas.doRedraw(coords);
   }
   
   /**
    * Sets the values of member variables canvas and coords.  This is
    * designed to be called only by the CoordinateRect class.
    */
   protected void setOwnerData(DisplayCanvas canvas, CoordinateRect coords) {
      this.canvas = canvas;
      this.coords = coords;
   }
   
} // end class Drawable
