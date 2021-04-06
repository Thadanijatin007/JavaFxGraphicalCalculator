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

import edu.awt.Computable;
import edu.data.Function;
import edu.data.Value;
import edu.data.*;
import edu.awt.*;

/**
 * A RiemannSumRects calculates a Riemann sum for a function.  It implements
 * Computable and InputObject.  You can specify and change the number of
 * intervals in the sum, as well as the method used to calculate the sum.  
 * Functions exist to return Value objects for the sum using different
 * computations.  This class was written by Gabriel Weinstock, with some
 * modifications by David Eck
 */
public class RiemannSumRects extends Drawable implements Computable {

   private double[] rectHeights;
   private int method;
   private Color color = new Color(255, 255, 180);
   private Color outlineColor = new Color(180,180,0);   
   private double []endpointVals, maxVals, minVals, midpointVals;
   private Value intervalCount;
   private Function func, deriv;  // derivative is used in max/min computations

   // store sum data here:
   private double[] sum;
   private double[] param = new double[1];
   private boolean changed = true;
      
   /**
    * Summation method type.
    */
   public static final int LEFTENDPOINT = 0, RIGHTENDPOINT = 1, MIDPOINT = 2, 
      CIRCUMSCRIBED = 3, INSCRIBED = 4, TRAPEZOID = 5;
      
   /**
    *  For use in getValueObject(), to indicate whatever summation method is currently set for drawing.
    */
   public static final int CURRENT_METHOD = -1;
   
   /**
    * Get the current color used to draw the rectangles
    */
   public Color getColor() { 
      return color;
   }
   
   /**
    * Set the color used to draw the rectangles.  The default color is a light yellow.
    */
   public void setColor(Color c) {
      if (c != null) {
         color = c; 
         needsRedraw();
      }
   }
   
   /**
    *  Set the color that will be used to draw outlines around the rects.  If this is null,
    *  then no outlines are drawn.  The default is a medium-dark red that looks brownish next to the default yellow fill color.
    */
   public void setOutlineColor(Color c) {
      outlineColor = c;
      needsRedraw();
   }
   
   /**
    *  Get the color that is used to draw outlines around the rects.  If this is null, then
    *  no outlines are drawn.
    */
   public Color getOutlineColor() {
      return outlineColor;
   }  
    
   /**
    * Set the function whose Riemann sums are to be computed.  If null, nothing is drawn.
    * The function, if non-null, must have arity 1, or an IllegalArgumentException is thrown.
    */
   public void setFunction(Function func) {
      if (func != null && func.getArity() != 1)
         throw new IllegalArgumentException("Function for Riemann sums must have arity 1.");
      this.func = func;
      deriv = (func == null)? null : func.derivative(1);
      changed = true;
      needsRedraw();
   }
   
   /**
    * Returns the function whose Riemann sums are computed.  Can be null.
    */
   public Function getFuction() {
      return func;
   }
   
   /**
    * Set the method used to calculate the rectangles.
    * @param m can be: LEFTENDPOINT, RIGHTENDPOINT, MIDPOINT, CIRCUMSCRIBED,
    * INSCRIBED or TRAPEZOID (these are integers ranging from 0 to 5, 
    * respectively)
    */
   public void setMethod(int m) {
      method = m; 
      changed = true; 
      needsRedraw();
   }
   
   /**
    * Return the current method used to find the rectangle sums
    */
   public int getMethod() { 
      return method;
   }
   
   /**
    *  This is generally called by a Controller.  Indicates that all data should be recomputed
    *  because input values that the data depends on might have changed.
    */
   public void compute() {
      changed = true;
      needsRedraw();
   }
   
   /**
    * Get the number of intervals used.
    * @return a Value object representing the number of intervals
    */
   public Value getIntervalCount() { 
      return intervalCount;  
   }
   
   /**
    * Set the interval count (the RiemannSumRects will be redrawn after this function
    * is called).  The value will be clamped to be a value between 1 and 5000.
    * If the value is null, the default number of intervals, five, is used.
    * @param c a Value object representing the interval count
    */
   public void setIntervalCount(Value c) {
      changed = true;
      intervalCount = c;
      needsRedraw();
   }
   
   /**
    *  Construct a RiemannSumRects object that initially has nothing to draw and that
    *  is set up to use the default number of intervals, 5.
    */
   public RiemannSumRects() {
      this(null,null);
   }

   /**
    * Construct a new RiemannSumRects object.
    * @param i a Value object representing the number of intervals.  If null, five intervals are used.
    * @param f a Function object used to derive the Riemann sum. If null, nothing is drawn.
    */
   public RiemannSumRects(Function f, Value i) {
      intervalCount = i;
      func = f;
      if (f != null)
         deriv = func.derivative(1);
      sum = new double[6];
      method = LEFTENDPOINT;
   }
   
   /**
    *  Draw the Rieman sum rects.  This is generally called by an object of class CoordinateRect
    */
   public void draw(Graphics g, boolean coordsChanged) {
      if (func == null || coords == null)
         return;
      if (changed || rectHeights == null || coordsChanged)
         setSumData();
      int intervals = ((method == 5 || method == 0 || method == 1) ? 
         (rectHeights.length - 1) : rectHeights.length);
      double x = coords.getXmin();
      double dx = (coords.getXmax() - x) / intervals;
      int zero = coords.yToPixel(0);
      g.setColor(color);
      if(method == 5) // trapezoids
      { 
         int []xp = new int[4];
         int []yp = new int[4];
         xp[1] = coords.xToPixel(x);
         yp[0] = yp[1] = zero;
         yp[2] = coords.yToPixel(rectHeights[0]);
         for(int i = 0; i < intervals; i++)
         {
            x += dx;
            xp[0] = xp[3] = xp[1];
            xp[1] = xp[2] = coords.xToPixel(x);
            yp[3] = yp[2];
            yp[2] = coords.yToPixel(rectHeights[i + 1]);
            g.fillPolygon(xp, yp, 4);
            if (outlineColor != null) {
                g.setColor(outlineColor);
                g.drawPolygon(xp, yp, 4);
                g.setColor(color);
            }
         }
      }
      else 
      {
         int left = coords.xToPixel(x);
         for(int i = 0; i < intervals; i++) {
            int right = coords.xToPixel(x + dx);
            int width = right - left + 1;
            int top = coords.yToPixel(rectHeights[(method == 1)? i + 1 : i]);
            int height = zero - top;
            if(height > 0)
               g.fillRect(left, top, width, height);
            else if(height == 0)
               g.drawLine(left, zero, left + width - 1, zero);
            else
               g.fillRect(left, zero, width, -height);
            if (outlineColor != null) {
                g.setColor(outlineColor);
               if(height > 0)
                  g.drawRect(left, top, width, height);
               else if(height == 0)
                  g.drawLine(left, zero, left + width - 1, zero);
               else
                  g.drawRect(left, zero, width, -height);
                g.setColor(color);
            }
            x += dx;
            left = right;
         }
      }
   }
      
   private void setSumData() {
        // Recompute all data.
      changed = false;
      double intCtD = (intervalCount == null)? 5 : (intervalCount.getVal()+0.5);
      if (Double.isNaN(intCtD) || Double.isInfinite(intCtD))
         intCtD = 5;
      else if (intCtD < 0)
         intCtD = 1;
      else if (intCtD > 5000)
         intCtD = 5000;
      int intCt = (int)intCtD;
      endpointVals = new double[intCt + 1];
      maxVals = new double[intCt];
      minVals = new double[intCt];
      midpointVals = new double[intCt];
      double x = coords.getXmin();
      double dx = (coords.getXmax() - x) / intCt;
      param[0] = x;
      endpointVals[0] = func.getVal(param);
              
      int ptsPerInterval = 200 / intCt;
      double smalldx;
      if(ptsPerInterval < 1)
      {
         ptsPerInterval = 1;
         smalldx = dx; 
      }
      else
         smalldx = dx / ptsPerInterval;

      boolean increasingleft;
      boolean increasingright = deriv.getVal(param) > 0;

      for(int i = 1; i <= intCt; i++)
      {
         x += dx;
         param[0] = x;
         endpointVals[i] = func.getVal(param);
         param[0] = x - dx / 2;
         midpointVals[i - 1] = func.getVal(param);
         
         // maxmin stuff
         double max, min;
         max = min = endpointVals[i - 1];
         for(int j = 1; j <= ptsPerInterval; j++) // looking for turning points in the interval
         {
            increasingleft = increasingright;
            double xright = (x - dx) + j * smalldx;
            param[0] = xright;
            increasingright = deriv.getVal(param) > 0;
            if(increasingleft != increasingright)
            {
               if(increasingleft)
               {
                  double z = searchMax(xright - smalldx, xright, 1);
                  if(z > max)
                     max = z;
               }
               else 
               {
                  double z = searchMin(xright - smalldx, xright, 1);
                  if (z < min)
                     min = z;
               }
            }
         }
         if(endpointVals[i] > max)
            max = endpointVals[i];
         else if(endpointVals[i] < min)
            min = endpointVals[i];
         minVals[i - 1] = min;
         maxVals[i - 1] = max;
      }
         
      double y = endpointVals[0];
      
      double leftsum = 0, midpointsum = 0, rightsum = 0, maxsum = 0, minsum = 0;
      for(int i = 0; i < intCt; i++)
      {
         leftsum += endpointVals[i];
         midpointsum += midpointVals[i];
         maxsum += maxVals[i];
         minsum += minVals[i];
      }
      rightsum = leftsum - endpointVals[0] + endpointVals[intCt];
      
      // calculate sums
      sum[LEFTENDPOINT] = leftsum * dx;
      sum[RIGHTENDPOINT] = rightsum * dx;
      sum[MIDPOINT] = midpointsum * dx;
      sum[CIRCUMSCRIBED] = maxsum * dx;
      sum[INSCRIBED] = minsum * dx;
      sum[TRAPEZOID] = (leftsum + rightsum) / 2 * dx;
      
      setRectData();
   }
   
   private void setRectData() {
      if (method == 3)
         setRectHeights(maxVals);
      else if(method == 4)
         setRectHeights(minVals);
      else if (method == 2)
         setRectHeights(midpointVals);
      else
         setRectHeights(endpointVals);
   }

   private void setRectHeights(double[] e) { 
      rectHeights = e; 
      changed = true;
   }
   
   private double searchMin(double x1, double x2, int depth) {
         // find an approximate minimum of func in the interval (x1,x2)
      double mid = (x1 + x2) / 2;
      param[0] = mid;
      if(depth >= 13)
         return func.getVal(param);
      double slope = deriv.getVal(param);
      if(slope < 0)
         return searchMin(mid, x2, depth + 1);
      else
         return searchMin(x1, mid, depth + 1);
   }
   
   private double searchMax(double x1, double x2, int depth) {
         // find an approximate maximum of func in the interval (x1,x2)
      double mid = (x1 + x2) / 2;
      param[0] = mid;
      if(depth >= 13)
         return func.getVal(param);
      double slope = deriv.getVal(param);
      if(slope > 0)
         return searchMin(mid, x2, depth + 1);
      else
         return searchMin(x1, mid, depth + 1);
   }
   
   /**
    * Gets a Value object that gives the value of the Riemann sum for the specified method.
    * @return a Value object representing the sum for the given method
    * @param which integer stating the method used to derive the sum; one of the
    *        constants LEFTENDPOINT, RIGHTENDPOINT, MIDPOINT, 
    *        CIRCUMSCRIBED, INSCRIBED, TRAPEZOID, or CURRENT_METHOD.
    */
   public Value getValueObject(final int which) {
      return new Value() {
         public double getVal() {
            if (func == null || coords == null)
               return Double.NaN;
            if (changed)
               setSumData();
            if (which == CURRENT_METHOD)
               return sum[method];
            else
               return sum[which];
         }
      };
   }
   
   
} // end class RiemannSumRects

