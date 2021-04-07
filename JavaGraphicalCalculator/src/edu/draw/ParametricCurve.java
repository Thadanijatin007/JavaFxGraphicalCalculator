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

import edu.awt.Computable;
import edu.data.Cases;
import edu.data.Function;
import edu.data.Value;
import edu.data.*;
import edu.awt.*;
import java.awt.*;
import java.util.Vector;

/**
 * A ParametricCurve is defined by two functions, x(t) and y(t) of a variable, t,
 * for t in a specified interval.  The curve is simply defined as a sequence of line
 * segments connecting points of the form (x(t),y(t)), except where one of the functions
 * is undefined.  Also, in some cases a
 * discontinuity will be detected and no line will be drawn between two of the points.
 */

public class ParametricCurve extends Drawable implements Computable {


   private Function xFunc, yFunc; //The functions of t that are graphed.
   

   private Color graphColor = Color.magenta; //Color of the graph.
   

   private boolean changed; // Used internally to indicate that data has to be recomputed.


   private transient int[] xcoord, ycoord;  //points on graph; xcoord[i] == Integer.MIN_VALUE
                                            //for points where a gap occurs.
                                            
   private Value tmin, tmax;  // Value objects giving the minimum and maximum value of t.
   
   private Value intervals;  // Value object giving the number of intervals into which the
                             // interval (tmin,tmax) is to be divided.
                                            
   private double tmin_val, tmax_val;  // The values of tmin and tmax.
                                       // (tmin_val is set to Double.NaN if any of the values are bad, and nothing is drawn.)
   private int intervals_val;          // The value of intervals.
    
   /**
    * Create a ParametricCurve with nothing to graph.  The functions and other values
    * can be set later.
    */
   public ParametricCurve() {
      this(null,null,null,null,null);
   }

   public ParametricCurve(Function xFunc, Function yFunc) {
      this(xFunc,yFunc,null,null,null);
   }

   public ParametricCurve(Function xFunc, Function yFunc, Value tmin, Value tmax, Value intevals) {
      if ( (xFunc != null && xFunc.getArity() != 1) || (yFunc != null && yFunc.getArity() != 1) )
         throw new IllegalArgumentException("Internal Error:  The functions that define a parametric curve must be functions of one variable.");
      this.xFunc = xFunc;
      this.yFunc = yFunc;
      this.tmin = tmin;
      this.tmax = tmax;
      this.intervals = intervals;
      changed = true;
   }
   
   /**
    * Set the color to be used for drawing the graph.
    */
   public void setColor(Color c) { 
      if (c != null & !c.equals(graphColor)) {
         graphColor = c;
         needsRedraw();
      }
   }   
   
   /**
    * Get the color that is used to draw the graph.
    */
   public Color getColor() { 
      return graphColor; 
   }
   
   /**
    * Sets the functions that gives the coordinates of the curve to be graphed.  If either function is
    * null, then nothing is drawn.  If non-null, each function must be a function of one variable.
    */
   synchronized public void setFunctions(Function x, Function y) { 
      setXFunction(x);
      setYFunction(y);
   }
   
   /**
    * Set the function that gives the x-coordinate of the curve to be graphed.  If this is
    * null, then nothing is drawn.  If non-null, it must be a function of one variable.
    */
   synchronized public void setXFunction(Function x) {
      if (x != null && x.getArity() != 1)
         throw new IllegalArgumentException("Internal Error:  ParametricCurve can only graph functions of one variable.");
      if (x != xFunc) {
         xFunc = x;
         changed = true;
         needsRedraw();
      }
   }
   
   /**
    * Set the function that gives the y-coordinate of the curve to be graphed.  If this is
    * null, then nothing is drawn.  If non-null, it must be a function of one variable.
    */
   synchronized public void setYFunction(Function y) {
      if (y != null && y.getArity() != 1)
         throw new IllegalArgumentException("Internal Error:  ParametricCurve can only graph functions of one variable.");
      if (y != yFunc) {
         yFunc = y;
         changed = true;
         needsRedraw();
      }
   }
   
   /**
    *  Get the (possibly null) function that gives the x-coordinate of the curve.
    */
   public Function getXFunction() { 
      return xFunc;
   }
   
   /**
    *  Get the (possibly null) function that gives the y-coordinate of the curve.
    */
   public Function getYFunction() { 
      return yFunc;
   }
   
   /**
    * Specify the number of subintervals into which the domain of the parametric curve is divided.
    * The interval (tmin,tmax) is divided into subintervals.  X and y coordinates of the parametric curve
    * are computed at each endpoint of these subintervals, and then the points are connected by lines.
    * If the parameter of this function is null, or if no interval count is ever specified, then a 
    * default value of 200 is used.
    */
   public void setIntervals(Value intervalCount) {
      intervals = intervalCount;
      changed = true;
   }
   
   /**
    *  Get the value object, possibly null, that determines the number of points on the curve.
    */
   public Value getIntervals() {
      return intervals;
   }
   
   /** 
    * Set the Value objects that specify the domain of the paratmeter.
    */
   public void setLimits(Value tmin, Value tmax) {
      setTMin(tmin);
      setTMax(tmax);
   }
   
   /**
    * Get the Value object, possibly null, that gives the left endpoint of the domain of the parameter.
    */
   public Value getTMin() {
      return tmin;
   }
   
   /**
    * Get the Value object, possibly null, that gives the right endpoint of the domain of the parameter.
    */
   public Value getTMax() {
      return tmax;
   }
   
   /**
    *  Set the Value object that gives the left endpoint of the domain of the parameter.  If this is null,
    *  then a default value of -5 is used for the left endpoint. (Note: actually, it's not required that
    *  tmin be less than tmax, so this might really be the "right" endpoint.)
    */
   public void setTMin(Value tmin) {
      this.tmin = tmin;
      changed = true;
   }
   
   /**
    *  Set the Value object that gives the right endpoint of the domain of the parameter.  If this is null,
    *  then a default value of 5 is used for the right endpoint. (Note: actually, it's not required that
    *  tmin be less than tmax, so this might really be the "left" endpoint.)
    */
   public void setTMax(Value tmax) {
      this.tmax = tmax;
      changed = false;
   }
   
   //------------------ Implementation details -----------------------------
   
   /**
    * Recompute data for the graph and make sure that the area of the display canvas
    * that shows the graph is redrawn.  This method is ordinarily called by a
    * Controller.
    */
   synchronized public void compute() {
      setup();
      needsRedraw();
      changed = false;
   }
   
   /**
    * Draw the graph (possibly recomputing the data if the CoordinateRect has changed).
    * This is not usually called directly.
    *
    */
   synchronized public void draw(Graphics g, boolean coordsChanged) {
      if (changed || coordsChanged || xcoord == null || ycoord == null) {
         setup();
         changed = false;
      }
      if (xcoord == null || xcoord.length == 0)
         return;
      g.setColor(graphColor);
      int x = xcoord[0];
      int y = ycoord[0];
      for (int i = 1; i < xcoord.length; i++) {
         if (xcoord[i] == Integer.MIN_VALUE) {
            do {
               i++;
            } while (i < xcoord.length && xcoord[i] == Integer.MIN_VALUE);
            if (i < xcoord.length) {
               x = xcoord[i];
               y = ycoord[i];
            }
         }
         else {
            int x2 = xcoord[i];
            int y2 = ycoord[i];
            g.drawLine(x,y,x2,y2);
            x = x2;
            y = y2;
         }
      }
   }
   
   // ------------------------- Computing the points on the graph -----------------------
   
   
   private double[] v = new double[1];
   private Cases case1x = new Cases();
   private Cases case2x = new Cases();
   private Cases case1y = new Cases();
   private Cases case2y = new Cases();
   private Cases case3x = new Cases();
   private Cases case3y = new Cases();
   
   private Vector points = new Vector(250);
   
   private Point eval(double t, Cases xcases, Cases ycases) {
      v[0] = t;
      if (xcases != null)
         xcases.clear();
      if (ycases != null)
         ycases.clear();
      double x = xFunc.getValueWithCases(v,xcases);
      double y = yFunc.getValueWithCases(v,ycases);
      if (Double.isNaN(x) || Double.isNaN(y))
         return null;
      int xInt = coords.xToPixel(x);
      int yInt = coords.yToPixel(y);
      if (Math.abs(xInt) > 10000 || Math.abs(yInt) > 10000)
         return null;
      return new Point(xInt,yInt);
   }
   
   private void setup() {
       if (xFunc == null || yFunc == null || coords == null) {
          xcoord = ycoord = new int[0];  // Nothing will be drawn
          return;
       }
      double intervals_val_d;
      if (tmin == null)
         tmin_val = -5;
      else
         tmin_val = tmin.getVal();
      if (tmax == null)
         tmax_val = 5;
      else
         tmax_val = tmax.getVal();
      if (intervals == null)
         intervals_val_d = 200;
      else
         intervals_val_d = intervals.getVal();
      if (Double.isInfinite(tmin_val) || Double.isInfinite(tmax_val) || Double.isInfinite(intervals_val_d) 
                    || Double.isNaN(tmax_val) || Double.isNaN(intervals_val_d))
         tmin_val = Double.NaN;  // Signal that data is bad, so nothing will be drawn.
      if (intervals_val_d < 1)
          intervals_val = 1;
      else if (intervals_val > 10000)
         intervals_val = 10000;
      else
         intervals_val = (int)Math.round(intervals_val_d);
      if (Double.isNaN(tmin_val)) {  // data is bad, don't draw
         xcoord = ycoord = new int[0];
         return;
      }

       points.setSize(0);
       
       double delta = (tmax_val - tmin_val) / intervals_val;
       double prevx, prevy, x, y, lastT;
       Point point, prevpoint;
       
       double t = tmin_val;
       prevpoint = eval(t,case1x,case1y);
       if (prevpoint != null)
          points.addElement(prevpoint);

       for (int i = 1; i <= intervals_val; i++) {
          t = tmin_val + i * delta;
          point = eval(t,case2x,case2y);
          if (point != null && prevpoint != null) {
             if (!case1x.equals(case2x) || !case1y.equals(case2y))
                 // A discontinuity between two "onscreen" points.
                 discontinuity(prevpoint,tmin_val+(i-1)*delta,point,t,0);
             else
                 points.addElement(point);
          }
          else if (prevpoint == null && point != null) {
             becomesDefined(prevpoint,tmin_val+(i-1)*delta,point,t,0);
          }
          else if (prevpoint != null && point == null) {
             becomesUndefined(prevpoint,tmin_val+(i-1)*delta,point,t,0);
          }

          prevpoint = point;
          Cases temp = case1x;
          case1x = case2x;
          case2x = temp;
          temp = case1y;
          case1y = case2y;
          case2y = temp;
          
       } // end for

       xcoord = new int[points.size()];
       ycoord = new int[points.size()];
       for (int i = 0; i < ycoord.length; i++) {
          Point p = (Point)points.elementAt(i);
          xcoord[i] = p.x;
          ycoord[i] = p.y;
       }
       
   } // end setup();
  

   private static int MAXDEPTH = 10;  // maximum depth of recursion in the next three methods.
   
   void discontinuity(Point p1, double t1, Point p2, double t2, int depth) {
         // Both p1 and p2 are non-null; "cases" data at these two points does not agree;
         // Original point p1 (from case depth=1) is in points vector.  Case data for p1 and p2
         // is contained in case1x,case1y and case2x,case2y respectively.
      if (depth >= MAXDEPTH || (Math.abs(p1.x - p2.x) < 2 && Math.abs(p1.y - p2.y) < 2)) {
         if (points.elementAt(points.size()-1) != p1)
            points.addElement(p1);
         if (depth >= MAXDEPTH)
            points.addElement(new Point(Integer.MIN_VALUE,0));
         points.addElement(p2);
         return;  
      }
      double t = (t1+t2)/2;
      Point p = eval(t,case3x,case3y);
      if (p == null) {
         becomesUndefined(p1,t1,p,t,depth+1);
         becomesDefined(p,t,p2,t2,depth+1);
      }
      else if (case3x.equals(case1x) && case3y.equals(case1y)) {
         discontinuity(p,t,p2,t2,depth+1);
      }
      else if (case3x.equals(case2x) && case3y.equals(case2y)) {
         discontinuity(p1,t1,p,t,depth+1);
      }
      else {
         discontinuity(p1,t1,p,t,depth+2);
         discontinuity(p,t,p2,t2,depth+2);
      }
   }
   
   void becomesUndefined(Point p1, double t1, Point p2, double t2, int depth) {
         // p1 is non-null; p2 is null.  Original point p1 is in points vector.
      if (depth >= MAXDEPTH) {
         if (points.elementAt(points.size()-1) != p1)
            points.addElement(p1);
         points.addElement(new Point(Integer.MIN_VALUE,0));
         return;
      }
      double t = (t1+t2)/2;
      Point p = eval(t,null,null);
      if (p == null)
         becomesUndefined(p1,t1,p,t,depth+1);
      else
         becomesUndefined(p,t,p2,t2,depth+1);
   }
   
   void becomesDefined(Point p1, double t1, Point p2, double t2, int depth) {
         // p1 is null; p2 is non-null
      if (depth >= MAXDEPTH) {
         if (points.size() > 0)
            points.addElement(new Point(Integer.MIN_VALUE,0));
         points.addElement(p2);
         return;
      }
      double t = (t1+t2)/2;
      Point p = eval(t,null,null);
      if (p != null)
         becomesDefined(p1,t1,p,t,depth+1);
      else
         becomesDefined(p,t,p2,t2,depth+1);
   }
   
   
   
} // end class ParametricCurve


