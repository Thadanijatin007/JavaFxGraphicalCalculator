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

import edu.awt.*;
import edu.awt.*;
import edu.data.Value;
import java.awt.*;
import java.util.Vector;

/**
 * A CoordinateRect represents a rectagular region in the xy-plane, specified
 * by values xmin,xmax,ymin,ymax.  The conditions ymin < ymax and xmin < xmax
 * are enforced.  (Values are swapped if necessary, and if min==max, they are
 * reset to -1 and +1.  If any of the values are set to an infinite or NaN
 * value, then the coordinate rect won't display anything except the message
 * "Error: undefined limits".)
 *   <P> When the Rect is mapped onto the screen, there can be a gap of a specified
 * number of pixels between the min,max values and the edges of the rectangle
 * on the screen.  If the gap is non-zero, then the actual range of coordinates
 * on the rect is larger than the range from the specifed min to max.  (This is 
 * done mainly so I could have axes that don't quite reach the edges of the rect.)
 *   <P>A CoordinateRect maintains a list of Drawable items.  When the Rect's
 * draw() method is called, it calls the draw() method of each of the Drawable
 * items it contains.  When its compute() method is called, it calls the 
 * compute() method of any Drawable that is a Computable.  When its checkInput()
 * method is called, it calls the checkInput() method of any Drawable that is
 * an InputObject.
 *   <P>A CoordinateRect represents a rectangular region in a DisplayCanvas.
 * It has a reference to that Canvas, which is set automatically when it is
 * added to the canvas.  If the size, range, or gap on the CoordinateRect
 * change, it will ask the Canvas to redraw the area it occupies.
 *
 * <P>The values of xmin, xmax, ymin, ymax are exported as Value objects,
 * which can be used elsewhere in your program.  The Value objects can
 * be obtained by calling getValueObject().  If you do this, you should
 * add the objects that depend on those values to a Controller and
 * register the Controller to listen for changes from this CoordinateRect
 * by calling the CoordinateRect.setOnChange(Controller) method.
 */

public class CoordinateRect implements Tieable, Limits, Computable, InputObject {

   

   private double xmin,xmax,ymin,ymax; // Range of x and y values on the Rect (not counting the gap).
   
   private int gap = 5; //Extra pixels around the edges, outside the specifed range of x,y values.
   //Note: xmin,xmax,ymin,ymax are the limits on a rectangle that
   //is inset from the drawing rect by gap pixels on each edge.

   /**
    * Drawable items contained in this CoordinateRect
    */
   protected Vector drawItems = new Vector(); 
   
   /**
    * Set to true when one of the limits or the gap has changed.
    */
   protected boolean changed; 

   private long serialNumber; // This value is increased whenever xmin,xmax,ymin,ymax,gap change
                              // or when the size of the rectangle in pixels changes.
                                           
   /**
    *  This contains other Limit objects with which the CoordinateRect is
    *  synchronizing.  This is ordinarily managed by a LimitControlPanel,
    *  so you don't have to worry about it. (However, you can also sync
    *  several CoordinateRects even in the absense of a LimitControlPanel.
    *  To do so, create the Tie that ties the CoordinateRect and pass it to 
    *  the setSyncWith() method of each CoordinateRect.  It is NOT necessary
    *  to add the Tie to a Controller.  Synchronization is handled by the
    *  CoordinateRects themselves.   
    */
   protected Tie syncWith;

   /**
    * Create a CoordinateRect with default limits: -5, 5, -5, 5.
    */
   public CoordinateRect() {
      this(-5,5,-5,5);
   }
   
   /**
    * Create a CoordinateRect with specified limits.
    */
   public CoordinateRect(double xmin, double xmax, double ymin, double ymax) {
      setLimits(xmin,xmax,ymin,ymax);
      serialNumber = 0;
      setRestoreBuffer();     // Restore buffer holds original limits, util it is reset
   }
   
   //--------- Methods for getting and setting xmin, xmax, ymin, ymax, and gap.------

   /**
    * Get the mimimum x-coordinate.
    */
   public double getXmin() { return xmin; }
   
   /**
    * Get the maximum x-coordinate.
    */
   public double getXmax() { return xmax; }

   /**
    * Get the mimimum y-coordinate.
    */
   public double getYmin() { return ymin; }

   /**
    * Get the maximum x-coordinate.
    */
   public double getYmax() { return ymax; }

   /**
    * Get the gap, in pixels, between the edges of 
    * the CoordinateRect and the limits specified by xmin, xmax, ymin, and ymax.
    */
   public int getGap() { 
      return gap; 
   }

   /**
    * Set the gap. This is ignored if g is less than zero.  This gap is the number of pixels
    * between the edges of the CoordinateRect and the limits specified by xmin, xmax, ymin, and ymax.
    * The default value is 5.
    *
    */
   public void setGap(int g) { 
      if (g >= 0 && gap!= g)  { 
         int oldgap = gap;
         gap = g; 
         changed = true;
         serialNumber++;
         needsRedraw();
      }
   }

   /**
    * Get an array containing the limits on the CoordinateRect in the order xmin, xmax, ymin, ymax.
    */
   public double[] getLimits() {  
      return new double[] { xmin, xmax, ymin, ymax };
   }

   /**
    * Set the limits on the CoordinteRect
    *
    * @param xmin the minimum x-coordinate on the CoordinateRect
    * @param xmax the maximum x-coordinate on the CoordinateRect
    * @param ymin the minimum y-coordinate on the CoordinateRect
    * @param ymax the maximum y-coordinate on the CoordinateRect
    */
   public void setLimits(double xmin, double xmax, double ymin, double ymax) { 
      double[] oldLimits = getLimits();
      this.xmin = xmin;
      this.xmax = xmax;
      this.ymin = ymin;
      this.ymax = ymax;
      checkLimits();
      double[] newLimits = getLimits();
      if (oldLimits[0] == newLimits[0] && oldLimits[1] == newLimits[1] &&
             oldLimits[2] == newLimits[2] && oldLimits[3] == newLimits[3])
         return;
      changed = true;
      serialNumber++;
      if (syncWith != null)
         syncWith.check();
      if (onChange != null)
         onChange.compute();
      needsRedraw();
   }

   /**
    * Set the coordinate limits from array; extra elements in array are ignored.
    * This is ignored if the array is null or has fewer than 4 members.
    * The order of values in the array is xmin, xmax, ymin, ymax.
    *
    */
   public void setLimits(double[] d) {  
      if (d != null && d.length >= 4)
         setLimits(d[0],d[1],d[2],d[3]);
   }
   
   /**
    * Specify a controller to be notified when the limits on this
    * CoordinateRect change.
    */
   public void setOnChange(Controller c) {
      onChange = c;
   }
   
   /**
    * Get the controller that is notified when the limits on this
    * CoordinateRect change.  This can be null.
    */
   public Controller getOnChange() {
      return onChange;
   }
   
   /**
    * Get a Value object representing one of the limits on this CoordinateRect.
    * The parameter should be one of the constants CoordinateRect.XMIN,
    * CoordinateRect.XMAX, CoordinateRect.YMIN, or CoordinateRect.YMAX.
    * (If not, it is treated the same as YMAX).
    * 
    */
   public Value getValueObject(final int which) {
      return new Value() {
         public double getVal() {
            switch (which) {
               case XMIN: return getXmin();
               case XMAX: return getXmax();
               case YMIN: return getYmin();
               default:   return getYmax();
            }
         }
      };
   }

   /**
    * Return the serial number of the CoordinateRect, which is incremented each time the limits change.
    * Part of the Tieable interface.
    * Not meant to be called directly.
    */ 
   public long getSerialNumber() {
      return serialNumber;
   }
   
   /**
    * Set the Tie object that is used to synchronize this CoordinareRect with other objects.
    * This is ordinarily called by a LimitControlPanel, so you don't have to worry about it.
    */ 
   public void setSyncWith(Tie tie) {
      syncWith = tie;
   }
      
   /**
    * Part of the Tieable interface.
    * Not meant to be called directly.
    */ 
   public void sync(Tie tie, Tieable newest) {
      if (newest != this) {
         if ( !(newest instanceof Limits) )
            throw new IllegalArgumentException("Internal programming error:  A CoordinateRect can only be tied to a Limits object.");
         double[] d = ((Limits)newest).getLimits();
         if (d != null && d.length >= 4) {
             double[] oldLimits = getLimits();
             if (d[0] == oldLimits[0] && d[1] == oldLimits[1] &&  d[2] == oldLimits[2] && d[3] == oldLimits[3])
                return;
             xmin = d[0];
             xmax = d[1];
             ymin = d[2];
             ymax = d[3];
             checkLimits();
             serialNumber = newest.getSerialNumber();  
             changed = true;
             if (onChange != null)
                onChange.compute();
             needsRedraw();
         }
      }
   }

   private void checkLimits() { //Make sure limits satisfy constraints.
      if (xmin == xmax) {
         xmin -= 1;
         xmax += 1;
      }
      else if (xmin > xmax) {
         double temp = xmin;
         xmin = xmax;
         xmax = temp;
      }
      if (ymin == ymax) {
         ymin -= 1;
         ymax += 1;
      }
      if (ymin > ymax) {
         double temp = ymin;
         ymin = ymax;
         ymax = temp;
      }
   }
   
   // -------------- Value objects corresponding to xmin, xmax, ymin, ymax -------------
   
   /**
    * A constant for use with the getValueObject() method to specify which Value is to be returned.
    * XMIN specifies that the Value is the minimum x-coordinate on the CoordinateRect.
    */ 
   public final static int XMIN = 0;
   
   /**
    * A constant for use with the getValueObject() method to specify which Value is to be returned.
    * XMAX specifies that the Value is the maximum x-coordinate on the CoordinateRect.
    */ 
   public final static int XMAX = 1; 

   /**
    * A constant for use with the getValueObject() method to specify which Value is to be returned.
    * YMIN specifies that the Value is the minimum y-coordinate on the CoordinateRect.
    */ 
   public final static int YMIN = 2;

   /**
    * A constant for use with the getValueObject() method to specify which Value is to be returned.
    * YMAX specifies that the Value is the maximum y-coordinate on the CoordinateRect.
    */ 
   public final static int YMAX = 3;
   
   /**
    * If non-null, this is the Controller that is notified when the limits change. 
    */
   protected Controller onChange;
   
   
   // ---------------------- Methods for working with Pixels ----------------------
   // Note: This stuff is only valid if the CoordinateRect is
   // displayed in a Graphics context.  I.E., after a call to draw();
   // It is meant to be used by Drawables when their draw() methods are called.

   private int left, top, width = -1, height = -1;  // Not setable; these are valid only during drawing and are meant to be used
                                                    // by the Drawables in this Coorfdinate Rect.
   
   /**
    * Get the left edge of this CoordinateRect in the DisplayCanvas that contains it.
    * (This is only valid when the CoordinateRect has actually been displayed.  It is meant
    * mainly to be used by Drawables in this CoordinateRect.)
    */
   public int getLeft() { return left; }
   
   /**
    * Get the width in pixels of this CoordinateRect in the DisplayCanvas that contains it.
    * (This is only valid when the CoordinateRect has actually been displayed.  It is meant
    * mainly to be used by Drawables in this CoordinateRect.)
    */
   public int getWidth() { return width; }
   
   /**
    * Get the top edge of this CoordinateRect in the DisplayCanvas that contains it.
    * (This is only valid when the CoordinateRect has actually been displayed.  It is meant
    * mainly to be used by Drawables in this CoordinateRect.)
    */
   public int getTop() { return top; }
   
   /**
    * Get the height in pixels of this CoordinateRect in the DisplayCanvas that contains it.
    * (This is only valid when the CoordinateRect has actually been displayed.  It is meant
    * mainly to be used by Drawables in this CoordinateRect.)
    */
   public int getHeight() { return height; }
   
   /**
    * Return the width of one pixel in this coordinate system.
    * (This is only valid when the CoordinateRect has actually been displayed.  It is meant
    * mainly to be used by Drawables in this CoordinateRect.)
    *
    */
   public double getPixelWidth() {
      return (xmax - xmin)/(width-2*gap-1);
   }
   
   /**
    * Return the height of one pixel in this coordinate system.
    * (This is only valid when the CoordinateRect has actually been displayed.  It is meant
    * mainly to be used by Drawables in this CoordinateRect.)
    * 
    */
   public double getPixelHeight() {
      return (ymax - ymin)/(height-2*gap-1);
   }
   

   /**
    * Convert an x-coodinate into a horizontal pixel coordinate.
    * (This is only valid when the CoordinateRect has actually been displayed.  It is meant
    * mainly to be used by Drawables in this CoordinateRect.)
    * 
    */
   public int xToPixel(double x) {
      int xInt = left + gap + (int)((x - xmin)/(xmax - xmin) * (width-2*gap-1));
      if (xInt < -32000)
         return -32000;
      else if (xInt > 32000)
         return 32000;
      else
         return xInt;
   }
   
   /**
    * Convert a y-coodinate into a vertical pixel coordinate.
    * (This is only valid when the CoordinateRect has actually been displayed.  It is meant
    * mainly to be used by Drawables in this CoordinateRect.)
    * 
    */
   public int yToPixel(double y) {
      int yInt =  top + gap + (int)((ymax - y)/(ymax - ymin) * (height-2*gap-1));   
      if (yInt < -32000)
         return -32000;
      else if (yInt > 32000)
         return 32000;
      else
         return yInt;
   }
   
   /**
    * Convert a horizontal pixel coordinate into an x-coordinate.
    * (This is only valid when the CoordinateRect has actually been displayed.  It is meant
    * mainly to be used by Drawables in this CoordinateRect.)
    * 
    */
   public double pixelToX(int h) {
      return xmin + ((h-left-gap)*(xmax-xmin)) / (width-2*gap-1);
   }
   
   /**
    * Convert a vertical pixel coordinate into a y-coordinate.
    * (This is only valid when the CoordinateRect has actually been displayed.  It is meant
    * mainly to be used by Drawables in this CoordinateRect.)
    * 
    */
   public double pixelToY(int y) {
      return ymax - ((y-top-gap)*(ymax-ymin)) / (height-2*gap-1);
   }
   
   // ---------------------- Save/Restore limits -------------------------

   
   private double restore_xmin = Double.NaN, restore_xmax, restore_ymin, restore_ymax;
   
   
   /**
    * A CoordinateRect can store its current limits in a buffer.  These limits
    * can be restored by a call to this method.  Only one level of 
    * save/restore is provided. If limits have not been saved, then nothing happens.
    * The original limits on the CoordinateRect are saves automatically when
    * the CoordinateRect is first created.
    *
    * @return an array containing new limits.
    */
   public double[] restore() { 
      if (Double.isNaN(restore_xmin))
         return null;
      setLimits(restore_xmin,restore_xmax,restore_ymin,restore_ymax);
      return getLimits();
   }
   
   /**
    * A CoordinateRect can store its current limits in a buffer.  This method
    * clears that buffer.
    */
   public void clearRestoreBuffer() {
      restore_xmin = Double.NaN;
   }
   
   /**
    * Save current limits in buffer.  They can be restored later by a call
    * to the restore() method. Only one level of 
    * save/restore is provided.
    */
   public void setRestoreBuffer() {  
      if (badData())
         return;
      checkLimits();
      restore_xmin = xmin;
      restore_xmax = xmax;
      restore_ymin = ymin;
      restore_ymax = ymax;
   }
   
   /**
    * Used to test if any of the limit data are infinite or NaN.
    */
   private boolean badData() {
      return Double.isNaN(xmin) || Double.isInfinite(xmin) || Double.isNaN(ymin) || Double.isInfinite(ymin) || 
            Double.isNaN(xmax) || Double.isInfinite(xmax) || Double.isNaN(ymax) || Double.isInfinite(ymax);
   }

   // ----------- Zoom in and out ---------------
   
   /**
    * Change limits to zoom in by a factor of 2.  A maximal zoom is enforced.
    * The center of the rectangle does not move.
    *
    * @return an array of the new limits, or null if limits don't change.
    */
   public double[] zoomIn() { 
      if (badData())
         return getLimits();
      double halfwidth = (xmax - xmin)/4.0;
      double halfheight = (ymax - ymin)/4.0;
      double centerx = (xmin + xmax)/2.0;
      double centery = (ymin + ymax)/2.0;
      if (Math.abs(halfheight) < 1e-100 || Math.abs(halfwidth) < 1e-100)
           return null;
      setLimits(centerx - halfwidth, centerx + halfwidth, centery - halfheight, centery + halfheight);
      return getLimits();
   }
   
   /**
    * Change limits to zoom out by a factor of 2.  A maximal zoom is enforced.
    * The center of the rectangle does not move.
    * 
    * @return an array of the new limits, or null if limits don't change.
    */
   public double[] zoomOut() {
      if (badData())
         return getLimits();
      double halfwidth = (xmax - xmin);
      double halfheight = (ymax - ymin);
      double centerx = (xmin + xmax)/2.0;
      double centery = (ymin + ymax)/2.0;
      if (Math.abs(halfwidth) > 1e100 || Math.abs(halfheight) > 1e100)
         return null;
      setLimits(centerx - halfwidth, centerx + halfwidth, centery - halfheight, centery + halfheight);
      return getLimits();
   }
   
   /**
    * Change limits to zoom in by a factor of 2, centered on a specified point.  A maximal zoom is enforced.
    * The point does not move.  Only valid when CoordinateRect is
    * displayed in a rectangle on the screen.
    *
    * @param x the horizontal pixel coordinate of the center point of the zoom
    * @param y the vertical pixel coordinate of the center point of the zoom
    *
    * @return an array of the new limits, or null if limits don't change.
    */
   public double[] zoomInOnPixel(int x, int y) {
      if (badData())
         return getLimits();
      double halfwidth = (xmax - xmin)/4.0;
      double halfheight = (ymax - ymin)/4.0;
      if (Math.abs(halfheight) < 1e-100 || Math.abs(halfwidth) < 1e-100)
           return null;
      double xclick = pixelToX(x);
      double yclick = pixelToY(y);
      double centerx = (xmin+xmax)/2;
      double centery = (ymin+ymax)/2;
      double newCenterx = (centerx+xclick)/2;
      double newCentery = (centery+yclick)/2;
      setLimits(newCenterx - halfwidth, newCenterx + halfwidth,
                   newCentery - halfheight, newCentery + halfheight);
      return getLimits();      
   }
   
   /**
    * Change limits to zoom out by a factor of 2, centered on a specified point.  A maximal zoom is enforced.
    * The point (x,y) does not move.  Valid only if CoordinateRect has been drawn.
    *
    * @param x the horizontal pixel coordinate of the center point of the zoom
    * @param y the vertical pixel coordinate of the center point of the zoom
    *
    * @return an array of the new limits, or null if limits don't change.
    */
   public double[] zoomOutFromPixel(int x, int y) {
      if (badData())
         return getLimits();
      double halfwidth = (xmax - xmin);
      double halfheight = (ymax - ymin);
      if (Math.abs(halfwidth) > 1e100 || Math.abs(halfheight) > 1e100)
         return null;
      double xclick = pixelToX(x);
      double yclick = pixelToY(y);
      double centerx = (xmin+xmax)/2;
      double centery = (ymin+ymax)/2;
      double newCenterx = 2*centerx - xclick;
      double newCentery = 2*centery - yclick;
      setLimits(newCenterx - halfwidth, newCenterx + halfwidth,
                   newCentery - halfheight, newCentery + halfheight);
      return getLimits();      
   }
   
   /**
    * Reset limits, if necessary, so scales on the axes are the same.
    * Only valid of the CoordinateRect has been drawn.  
    *
    * @return an array with the new limits, or null if limits don't change.
    */
   public double[] equalizeAxes() {
      if (badData())
         return getLimits();
      double w = xmax - xmin;
      double h = ymax - ymin;
      double pixelWidth = w / (width - 2*gap - 1);
      double pixelHeight = h / (height - 2*gap - 1);
      double newXmin, newXmax, newYmin, newYmax;
      if (pixelWidth < pixelHeight) {
         double centerx = (xmax + xmin) / 2;
         double halfwidth = w/2 * pixelHeight/pixelWidth;
         newXmax = centerx + halfwidth;
         newXmin = centerx - halfwidth;
         newYmin = ymin;
         newYmax = ymax;
      }
      else if (pixelWidth > pixelHeight) {
         double centery = (ymax + ymin) / 2;
         double halfheight = h/2 * pixelWidth/pixelHeight;
         newYmax = centery + halfheight;
         newYmin = centery - halfheight;
         newXmin = xmin;
         newXmax = xmax;
      }
      else
         return null;
      setLimits(newXmin, newXmax, newYmin, newYmax);
      return getLimits();      
   }


   // ------------------------------ Drawing ----------------------------
   
   private DisplayCanvas canvas;   // The canvas in which this CoordinateRect is displayed.  This is set
                                   // automatically when the CoordinateRect is added to or removed from
                                   // a DisplayCanvas, and it should not be changed.
   
   /**
    * This is meant to be called only by the DisplayCanvas class,
    * when this CoordinateRect is added to ta DisplayCanvas.
    * 
    */
   void setOwner(DisplayCanvas canvas) {
      this.canvas = canvas;
   }
                                  
   private void needsRedraw() { //Notifies the canvas that the area occupied by this CoodinateRect
      if (canvas != null)       //needs to be redrawn.
         canvas.doRedraw(this);
   }
   
   /**
    * When this is called, the CoordinateRect will call the
    * checkInput method of any Drawable it contains that is
    * also an InputObject.  This is ordinarly only called by a DisplayCanvas.
    */
   public void checkInput() {
      int ct = drawItems.size();
      for (int i = 0; i < ct; i++)
         if (drawItems.elementAt(i) instanceof InputObject)
            ((InputObject)drawItems.elementAt(i)).checkInput();
   }

   /**
    * When this is called, the CoordinateRect will call the compute method
    * of any Drawable it contains that is also a Computable.
    *  This is ordinarly only called by a DisplayCanvas.
    */ 
   public void compute() {
      int ct = drawItems.size();
      for (int i = 0; i < ct; i++)
         if (drawItems.elementAt(i) instanceof Computable)
            ((Computable)drawItems.elementAt(i)).compute();
   }

   /**
    * Method required by InputObject interface; in this class, it calls the same method
    * recursively on any input objects containted in this CoordinateRect.  This is meant to 
    * be called by JCMPanel.gatherInputs().
    */
    public void notifyControllerOnChange(Controller c) {
      int ct = drawItems.size();
      for (int i = 0; i < ct; i++)
         if (drawItems.elementAt(i) instanceof InputObject)
            ((InputObject)drawItems.elementAt(i)).notifyControllerOnChange(c);
    }

   /**
    * Add a drawable item to the CoordinateRect.
    *
    */
   synchronized public void add(Drawable d) { 
      if (d != null && !drawItems.contains(d)) {
         d.setOwnerData(canvas,this);
         drawItems.addElement(d);
      }
   }
   
   /** 
    * Remove the given Drawable item, if present in this CoordinateRect.
    * 
    */
   synchronized public void remove(Drawable d) {  
      if (d != null && drawItems.removeElement(d))
         d.setOwnerData(null,null);
   }
   
   /**
    * Returns the number of Drawable items that are in this CoordinateRect.
    */ 
   public int getDrawableCount() {
      return (drawItems == null)? 0 : drawItems.size();
   }
   
   /**
    * Get the i-th Drawable in this Rect, or null if i is less than zero
    * or greater than or equal to the number of items.
    *
    * @param i The number of the item to be returned, where the first item is number zero.
    */
   public Drawable getDrawable(int i) {
      if (drawItems != null && i >= 0 && i < drawItems.size())
         return (Drawable)drawItems.elementAt(i);
      else
         return null;
   }
   
   /**
    * Check whether a mouse click (as specified in the MouseEvent parameter) is
    * a click on a Draggable item that wants to be dragged.  If so, return that item.  If not, return null.
    * This is meant to be called only by DisplayCanvas.
    */
   Draggable checkDraggables(java.awt.event.MouseEvent evt) {
      int top = drawItems.size();
      for (int i = top-1; i >= 0; i--)
         if ( drawItems.elementAt(i) instanceof Draggable ) {
             if ( ((Draggable)drawItems.elementAt(i)).startDrag(evt) )
                return (Draggable)drawItems.elementAt(i);
         }
      return null; 
   }
   
   /**
    * Draw in rect with upperleft corner (0,0) and specified width,height.
    * This is not ordinarily called directly.
    *
    */
   public void draw(Graphics g, int width, int height) {
      draw(g,0,0,width,height);
   }
   
   /**
    * Draw in specified rect.  This is not ordinarily called directly.
    */
   synchronized public void draw(Graphics g, int left, int top, int width, int height) {
      if (badData()) {
         g.setColor(Color.red);
         g.drawRect(left,top,width-1,height-1);
         g.drawString("(undefined limits)",left+6,top+15);
      }
      if (changed || this.left != left || this.top != top 
                               || this.width != width || this.height != height) {
         this.width = width;
         this.height = height;
         this.left = left;
         this.top = top;
         checkLimits();
         changed = true;
      }
      doDraw(g);
      changed = false;
   }
   
   /**
    * Draw all the Drawable items.  This is called by the draw() method and is not 
    * meant to be called directly.  However, it might be overridden in a subclass.
    *
    */
   protected void doDraw(Graphics g) {
      int ct = drawItems.size();
      for (int i = 0; i < ct; i++) {
         Drawable d = (Drawable)drawItems.elementAt(i);
            if (d.getVisible())
               d.draw(g,changed);
      }
   }
   
} // end class CoordinateRect
