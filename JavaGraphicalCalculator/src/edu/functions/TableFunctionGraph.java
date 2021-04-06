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


package edu.functions;

import java.awt.*;
import java.awt.event.*;

import edu.awt.Computable;
import edu.draw.CoordinateRect;
import edu.draw.DisplayCanvas;
import edu.draw.Drawable;
import edu.data.*;
import edu.awt.*;
import edu.draw.*;


/**
 *  A TableFunctionGraph is a Drawable object that can be added to a CoordinateRect (or DisplayCanvas).
 *  It draws the graph of a specified TableFunction.
 *  A TableFunction is a function and can also be graphed by an object of the class Graph1D.
 *  However, a TableFunctionGraph offers the option of showing the points from the table that defines
 *  the function as small disks (true by default) and the option of making the graph "interactive" so 
 *  that the user can drag the points (false by default).
 */
public class TableFunctionGraph extends Drawable implements MouseListener, MouseMotionListener {
 
   private TableFunction function;      // The function that is drawn.
   private boolean showPoints;          // Are the points from the table shown as little disks?
   private boolean interactive;         // Can user drag points?
   private Computable onDrag;           // If interactive is true and onDrag is not null, then this
                                        //  Computable's compute() method is called each time a
                                        //  point moves as the user drags it.
   private Computable onFinishDrag;     // If interactive is true and onFinishDraw is not null, then this
                                        //  Computable's compute() method is called when the user
                                        //  finishes a drag operation (if the point is actually moved).
   private Color color;                 // non-null color of the graph.
   
   /**
    * Create a TableFunctionGraph that initially draws no function.  A function can be set
    * later with setFunction.
    */        
   public TableFunctionGraph() {
      this(null);
   }
   
   /**
    * Create a TableFunctionGraph to draw the specified TableFunction.
    */
   public TableFunctionGraph(TableFunction function) {
      this.function = function;
      this.color = Color.magenta;
      showPoints = true;
   }
   
   /**
    *  Set the function whose graph is drawn by this TableFunctionGraph.  If the value is null,
    *  nothing is drawn
    */
   public void setFunction(TableFunction function) {
      this.function = function;
      needsRedraw();
   }
   
   /**
    *  Get the TableFunction whose graph is drawn by this TableFunctionGraph.  If the value is null,
    *  then no graph is drawn.
    */
   public TableFunction getFunction() {
      return function;
   }

   /**
    * Specify a controller whose compute() method will be called repeatedly
    * as the user drags one of the points from the table function.  This only
    * applies if the "interactive" property is true.
    */
   public void setOnDrag(Computable c) {
      onDrag = c;
   }
   
   /**
    * Get the Computable that is notified as the user drags a point.
    */ 
   public Computable getOnDrag() {
      return onDrag;
   }
   
   /**
    * Specify a controller whose compute() method will be called once
    * when the user finishes dragging one of the points from the table function.
    * This only applies if the "interactive" property is true.
    */
   public void setOnFinishDrag(Computable c) {
      onFinishDrag = c;
   }
   
   /**
    * Get the Computable that is notified when the user finishes dragging a point.
    */ 
   public Computable getOnFinishDrag() {
      return onFinishDrag;
   }
   
   /**
    *  Set the value of the interactive property, which is true if the user can
    *  modify the function by dragging the points from the table.  The default is false.
    */
   public void setInteractive(boolean interactive) {
      if (this.interactive == interactive)
         return;
      if (this.interactive && canvas != null) {
          canvas.removeMouseListener(this);
          canvas.removeMouseMotionListener(this);  
      }
      this.interactive = interactive;
      if (this.interactive && canvas != null) {
          canvas.addMouseListener(this);
          canvas.addMouseMotionListener(this);  
      }
   }
   
   /**
    *  Get the value of the interactive property, which is true if the user can
    *  modify the function by dragging the points from the table.
    */
   public boolean getInteractive() {
      return interactive;
   }
   
   
   /**
    *  Set the showPoints property, which determines whether the points
    *  from the table that defines the function are visible as little
    *  disks.  The default is true;
    */
   public void setShowPoints(boolean show) {
      showPoints = show;
      needsRedraw();
   }
   

   /**
    *  Get the showPoints property, which determines whether the points
    *  from the table that defines the function are visible as little
    *  disks.
    */
   public boolean getShowPoints() {
      return showPoints;
   }


   /**
    * Set the color that is used for drawing the graph.  The defalt is magenta.
    * If the specified Color value is null, the call to setColor is ignored.
    */
   public void setColor(Color c) {
      if (c != null) {
         color = c;
         needsRedraw();
      }
   }

   /** 
    *  Get the non-null color that is used for drawing the graph.
    */
   public Color getColor() {
      return color;
   }   
   
   /**
    * Sets the values of member variables canvas and coords.  This is
    * designed to be called only by the CoordinateRect class.  This overrides
    * Drawable.setOwnerData();
    */
   protected void setOwnerData(DisplayCanvas canvas, CoordinateRect coords) {
      if (interactive && this.canvas != null) {
          canvas.removeMouseListener(this);
          canvas.removeMouseMotionListener(this);  
      }
      super.setOwnerData(canvas,coords);
      if (interactive && this.canvas != null) {
          canvas.addMouseListener(this);
          canvas.addMouseMotionListener(this);  
      }
   }
   
   /**
    *  Provided as a convenience.  If the function for this TableFunctionGraph is non-null,
    *  its style is set to the specified style, and the graph is redrawn.  The parameter
    *  should be one of the constants TableFunction.SMOOTH, TableFunction.PIECEWISE_LINEAR,
    *  TableFunction.STEP, TableFunction.STEP_LEFT, or TableFunction.STEP_RIGHT.
    */
   public void setFunctionStyle(int style) {
      if (function != null && function.getStyle() != style) {
         function.setStyle(style);
         needsRedraw();
      }
   }
   
   
   /**
    * Override the draw() method from class Drawable to draw the function.
    * This is not meant to be called directly.
    */ 
   public void draw(Graphics g, boolean coordsChanged) {
      if (function == null || coords == null)
         return;
      int ct = function.getPointCount();
      if (ct == 0)
         return;
      int startPt;  // The index of first point from the table that we have to consider
      int endPt;    // The index of the last point from the table that we have to consider
      double xmin = coords.pixelToX(coords.getLeft());
      double xmax = coords.pixelToX(coords.getLeft() + coords.getWidth());
      if (function.getX(0) > xmax || function.getX(ct-1) < xmin)
         return;
      startPt = 0;
      while (startPt < ct-1 && function.getX(startPt+1) <= xmin)
         startPt++;
      endPt = ct-1;
      while (endPt > 1 && function.getX(endPt-1) >= xmax)
         endPt--;
      double x,y, a,b;  // usually, two consecutive points on curve
      int xInt,yInt, aInt, bInt;  // usually, pixel coordinates for the two points.
      g.setColor(color);
      switch (function.getStyle()) {
         case TableFunction.SMOOTH: {
            if (endPt > startPt) {
               x = function.getX(startPt);
               y = function.getVal(x);
               xInt = coords.xToPixel(x);
               yInt = coords.yToPixel(y);
               double limit = xmax;
               if (function.getX(endPt) < limit)
                  limit = function.getX(endPt);
               coords.xToPixel(function.getX(ct-1));
               aInt = xInt;
               while (x < limit) {
                  aInt += 3;
                  a = coords.pixelToX(aInt);
                  if (a > limit)
                     a = limit;
                  b = function.getVal(a);
                  bInt = coords.yToPixel(b);
                  g.drawLine(xInt,yInt,aInt,bInt);
                  x = a;
                  xInt = aInt;
                  yInt = bInt;
               }
            }
            break;
         }
         case TableFunction. PIECEWISE_LINEAR: {
            x = function.getX(startPt);
            xInt = coords.xToPixel(x);
            y = function.getY(startPt);
            yInt = coords.yToPixel(y);
            for (int i = startPt+1; i <= endPt; i++) {
               a = function.getX(i);
               aInt = coords.xToPixel(a);
               b = function.getY(i);
               bInt = coords.yToPixel(b);
               g.drawLine(xInt,yInt,aInt,bInt);
               xInt = aInt;
               yInt = bInt;
            }
            break;
         }
         case TableFunction.STEP: {
            x = function.getX(startPt);
            xInt = coords.xToPixel(x);
            for (int i = startPt; i <= endPt; i++) {
               if (i < endPt) {
                  double nextX = function.getX(i+1);
                  a = (x + nextX)/2;
                  x = nextX;
               }
               else
                  a = x;
               aInt = coords.xToPixel(a);
               y = function.getY(i);
               yInt = coords.yToPixel(y);
               g.drawLine(xInt,yInt,aInt,yInt);
               xInt = aInt;
            }
            break;
         }
         case TableFunction.STEP_LEFT: {
            x = function.getX(startPt);
            xInt = coords.xToPixel(x);
            for (int i = startPt+1; i <= endPt; i++) {
               a = function.getX(i);
               aInt = coords.xToPixel(a);
               y = function.getY(i-1);
               yInt = coords.yToPixel(y);
               g.drawLine(xInt,yInt,aInt,yInt);
               xInt = aInt;
            }
            break;
         }
         case TableFunction.STEP_RIGHT: {
            x = function.getX(startPt);
            xInt = coords.xToPixel(x);
            for (int i = startPt+1; i <= endPt; i++) {
               a = function.getX(i);
               aInt = coords.xToPixel(a);
               y = function.getY(i);
               yInt = coords.yToPixel(y);
               g.drawLine(xInt,yInt,aInt,yInt);
               xInt = aInt;
            }
            break;
         }
      }
      if (!showPoints)
         return;
      for (int i = startPt; i <= endPt; i++) {
         x = function.getX(i);
         y = function.getY(i);
         xInt = coords.xToPixel(x);
         yInt = coords.yToPixel(y);
         g.fillOval(xInt-2,yInt-2,5,5);  
      }
   } // end draw();


   //-------------------- Dragging --------------------------
   
   private int dragPoint = -1;  // -1 if no point is being dragged;
                                // Otherwise, the index of the point being dragged.
                                
   private int startX, startY;  // Point where mouse was clicked at start of drag.
   
   private int prevY;  // Previous position of mouse during dragging.

   private boolean moved;  // Becomes true once the clicked point has actually
                           // been dragged a bit.  If the mouse is released before
                           // the point is moved at least 3 pixels, then the associated 
                           // y-value is not changed.
   
   /**
    * Method required by the MouseListener interface.  Defined here to
    * support dragging of points on the function's graph.  Not meant to be called directly.
    */
   public void mousePressed(MouseEvent evt) {
      dragPoint = -1;
      if (function == null || getVisible() == false || canvas == null || coords == null || evt.isConsumed())
         return;
      if (evt.isShiftDown() || evt.isMetaDown() || evt.isControlDown() || evt.isAltDown())
         return;
      moved = false;
      int ct = function.getPointCount();
      for (int i = 0; i < ct; i++) {
         int x = coords.xToPixel(function.getX(i));
         int y = coords.yToPixel(function.getY(i));
         if (evt.getX() >= x-3 && evt.getX() <= x+3 && evt.getY() >= y-3 && evt.getY() <= y+3) {
            startX = evt.getX();
            prevY = startY = evt.getY();
            dragPoint = i;
            evt.consume();
            return;
         }
      }
   }
   
   /**
    * Method required by the MouseListener interface.  Defined here to
    * support dragging of points on the function's graph.  Not meant to be called directly.
    */
   public void mouseReleased(MouseEvent evt) {
      if (dragPoint == -1)
         return;
      evt.consume();
      if (!moved) {
         dragPoint = -1;
         return;
      }
      mouseDragged(evt);
      dragPoint = -1;
      if (onFinishDrag != null)
         onFinishDrag.compute();
   }
   
   /**
    * Method required by the MouseListener interface.  Defined here to
    * support dragging of points on the function's graph.  Not meant to be called directly.
    */
   public void mouseDragged(MouseEvent evt) {
      if (dragPoint == -1 || prevY == evt.getY())
         return;
      evt.consume();
      if (!moved && Math.abs(evt.getY() - startY) < 3)
         return;
      moved = true;
      int y = evt.getY();
      if (y < coords.getTop() + 4)
         y = coords.getTop() + 4;
      else if (y > coords.getTop() + coords.getHeight() - 4)
         y = coords.getTop() + coords.getHeight() - 4;
      if (Math.abs(evt.getX() - startX) > 72)
         y = startY;
      if (y == prevY)
         return;
      prevY = y;
      function.setY(dragPoint, coords.pixelToY(prevY));
      needsRedraw();
      if (onDrag != null)
         onDrag.compute();
   }
   
   /**
    * Empty method, required by the MouseListener interface.
    */
   public void mouseClicked(MouseEvent evt) { }

   /**
    * Empty method, required by the MouseMotionListener interface.
    */
   public void mouseEntered(MouseEvent evt) { }

   /**
    * Empty method, required by the MouseMotionListener interface.
    */
   public void mouseExited(MouseEvent evt) { }

   /**
    * Empty method, required by the MouseMotionListener interface.
    */
   public void mouseMoved(MouseEvent evt) { }
   
   

} // end class TableFunctionGraph
