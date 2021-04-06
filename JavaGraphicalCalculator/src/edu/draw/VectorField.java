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
import edu.data.Function;
import edu.data.*;
import edu.awt.*;
import java.awt.*;

/**
 * A VectorField displays lines or arrows on a grid of points where the direction
 * and/or lengths are given by two functions (f1(x,y),f2(x,y)).  This is probably
 * more useful as a "direction field" than as a "vector field."
 *
 */

public class VectorField extends Drawable implements Computable {

   /**
    * One of the possible styles for displaying a VectorField: as a direction field shown as
    * arrows of equal length.  The point where the vector is computed is the tail of the arrow.
    */
   public static final int ARROWS = 0;
   /**
    * One of the possible styles for displaying a VectorField: as a direction field shown as
    * tangent lines.  The point where the vector is computed is the center of the line.
    */
   public static final int LINES = 1;
   /**
    * One of the possible styles for displaying a VectorField: as a vector field where a vector is shown as
    * an arrow from (x,y) to (x+xFunc(x,y),y+xFunc(x,y)), except that a maximum length is imposed.
    */
   public static final int CLAMPED_VECTORS = 2;
   /**
    * One of the possible styles for displaying a VectorField: as a field of tangent lines where the length
    * of the line is proportional to the length of the vector.
    */
   public static final int SCALED_LINES = 3;
   /**
    * One of the possible styles for displaying a VectorField: as a vector field where a vector is shown as
    * an arrow with length proportional to the length of the vector.  The lengths are scaled so that
    * the longest arrow has length equal to the grid spacing.
    */
   public static final int SCALED_VECTORS = 4;

   private int style;  // The style in which the vector field is drawn.

   private Function xFunc, yFunc; // The vector field is (xFunc(x,y),yfunc(x,y)).
   

   private Color graphColor = Color.lightGray; //Color of the vectors.
   

   private boolean changed; // Used internally to indicate that data has to be recomputed.


   private transient int[][] data; // Pre-computed data for the vectors.
   
   private int pixelSpacing = 30;  // Desired number of pixels between grid points, clamped to the range 5 to 200.
   
    
   /**
    * Create a VectorField object with nothing to graph.  The functions and other values
    * can be set later.  The default display style is as a direction field of equal-length arrows.
    */
   public VectorField() {
      this(null,null,ARROWS);
   }
   
   /**
    * Create a VectorField that will be displayed using the default style, as a direction field of 
    * equal-length arrows.  If either of the functions is null, nothing will be displayed. If non-null,
    * the functions must be functions of two variables.
    */
   public VectorField(Function xFunc, Function yFunc) {
      this(xFunc,yFunc,ARROWS);
   }
      
   /**
    * Create a VectorField with the specified functions and style.
    *
    * @param xFunc A Function of two variables giving the x-component of the vector field.  If this
    *              is null, then nothing will be drawn.
    * @param yFunc A Function of two variables giving the y-component of the vector field.  If this
    *              is null, then nothing will be drawn.
    * @param style The style in which the direction field is drawn.  This can be one of the
         constants ARROWS (a direction field of equal-lenth arrows), LINES (equal length lines),
         CLAMPED_VECTORS (vectors drawn at actual length, unless too long), SCALED_VECTORS (vectors scaled so longest has
         length equal to the grid spacing), or SCALED_LINES (lines scaled so longest has length
         equal to the grid spacing).
    */
   public VectorField(Function xFunc, Function yFunc, int style) {
      if ( (xFunc != null && xFunc.getArity() != 2) || (yFunc != null && yFunc.getArity() != 2) )
         throw new IllegalArgumentException("Internal Error:  The functions that define a vector must be functions of two variables.");
      this.xFunc = xFunc;
      this.yFunc = yFunc;
      this.style = style;
      changed = true;
   }
   
   /**
    * Set the color to be used for drawing the vector field.  The default color is light gray.
    */
   public void setColor(Color c) { 
      if (c != null & !c.equals(graphColor)) {
         graphColor = c;
         needsRedraw();
      }
   }   
   
   /**
    * Get the color that is used to draw the vector field.
    */
   public Color getColor() { 
      return graphColor; 
   }
   
   /**
    * Sets the functions that give the components of the vector field.  If either function is
    * null, then nothing is drawn.  If non-null, each function must be a function of two variables.
    */
   synchronized public void setFunctions(Function dx, Function dy) { 
      setXFunction(dx);
      setYFunction(dy);
   }
   
   /**
    * Set the function that gives the x-component of the vector field.  If this is
    * null, then nothing is drawn.  If non-null, it must be a function of two variables.
    */
   synchronized public void setXFunction(Function dx) {
      if (dx != null && dx.getArity() != 2)
         throw new IllegalArgumentException("Internal Error:  VectorField can only use functions of two variables.");
      if (dx != xFunc) {
         xFunc = dx;
         changed = true;
         needsRedraw();
      }
   }
   
   /**
    * Set the function that gives the y-component of the vector field.  If this is
    * null, then nothing is drawn.  If non-null, it must be a function of two variables.
    */
   synchronized public void setYFunction(Function dy) {
      if (dy != null && dy.getArity() != 1)
         throw new IllegalArgumentException("Internal Error:  VectorField can only use functions of two variables.");
      if (dy != yFunc) {
         yFunc = dy;
         changed = true;
         needsRedraw();
      }
   }
   
   /**
    *  Get the (possibly null) function that gives the x-component of the vector field.
    */
   public Function getXFunction() { 
      return xFunc;
   }
   
   /**
    *  Get the (possibly null) function that gives the y-component of the vector field.
    */
   public Function getYFunction() { 
      return yFunc;
   }
   
   /**
    * Get the style in which the vector field is displayed.
    */
   public int getStyle() {
      return style;
   }
   
   /**
    * Set the style in which the vector field is displayed. This should be one of the
    * constants ARROWS, LINES, CLAMPED_VECTORS, SCALED_LINES, or SCALED_VECTORS.
    */
   public void setStyle(int style) {
      if (this.style != style) {
         this.style = style;
         changed = true;
         needsRedraw();
      }
   }
   
   /**
    * Get the value of the pixelSpacing property, which determines the grid spacing for the vector field.
    */
   public int getPixelSpacing() {
      return pixelSpacing;
   }
   
   /**
    * Set the value of the pixelSpacing property, which determines the grid spacing for the vector field.
    * The value will be clamped to the range from 5 to 200.  The default value is 30.
    */
   public void setPixelSpacing(int spacing) {
      if (spacing < 5)
         spacing = 5;
      else if (spacing > 200)
         spacing = 200;
      if (spacing != pixelSpacing) {
         pixelSpacing = spacing;
         changed = true;
         needsRedraw();
      }
   }
   
   //------------------ Implementation details -----------------------------
   
   /**
    * Recompute data for the vector field and make sure that the area of the display canvas
    * that shows the vector field is redrawn.  This method is ordinarily called by a
    * Controller.
    */
   synchronized public void compute() {
      setup();
      needsRedraw();
      changed = false;
   }
   
   /**
    * Draw the vector field (possibly recomputing the data if the CoordinateRect has changed).
    *
    */
   synchronized public void draw(Graphics g, boolean coordsChanged) {
      if (changed || coordsChanged || data == null) {
         setup();
         changed = false;
      }
      if (data == null)
         return;
      g.setColor(graphColor);
      boolean arrows = style == ARROWS || style == CLAMPED_VECTORS || style == SCALED_VECTORS;
      for (int i = 0; i < data.length; i++) {
         int[] c = data[i];
         if (c[0] != Integer.MIN_VALUE) { // Otherwise, vector is undefined
            g.drawLine(c[0],c[1],c[2],c[3]);
            if (arrows && c[4] != Integer.MIN_VALUE) {  // Otherwise, there is no arrowhead
               g.drawLine(c[2],c[3],c[4],c[5]);
               g.drawLine(c[2],c[3],c[6],c[7]);
            }  
         }
      }
   }
   
   // ------------------------- Computing the data for the vector field -----------------------
   
   
   private void setup() {
      if (xFunc == null || yFunc == null || coords == null) {
          data = null;  // Nothing will be drawn
          return;
      }
      boolean arrows = style == ARROWS || style == CLAMPED_VECTORS || style == SCALED_VECTORS;
      int xCt, yCt; // number of points in x and y directions.
      double xStart, yStart; // Starting values for x,y, at lower left corner of grid.
      double dx, dy;  // Change in x and y between grid points.
      double[] params = new double[2];
      
      xCt = (coords.getWidth()) / pixelSpacing + 2;
      yCt = (coords.getHeight()) / pixelSpacing + 2;
      dx = pixelSpacing*coords.getPixelWidth();
      dy = pixelSpacing*coords.getPixelHeight();
      xStart = (coords.getXmax() + coords.getXmin() - xCt*dx)/2;
      yStart = (coords.getYmax() + coords.getYmin() - yCt*dy)/2;
      
      data = new int[xCt*yCt][arrows? 8 : 4];
      double[][] xVec = new double[xCt][yCt];  // Vector field scaled so pixelsize is one unit.
      double[][] yVec = new double[xCt][yCt];
      double pixelWidth = coords.getPixelWidth();
      double pixelHeight = coords.getPixelHeight();
      double maxLength = 0;
      for (int i = 0; i < xCt; i++) {
         double x = xStart + i*dx;
         params[0] = x;
         for (int j = 0; j < yCt; j++) {
            double y = yStart + j*dy;
            params[1] = y;
            xVec[i][j] = xFunc.getVal(params);
            yVec[i][j] = yFunc.getVal(params);
            if ( ! (Double.isNaN(xVec[i][j]) || Double.isNaN(yVec[i][j]) || 
                            Double.isInfinite(xVec[i][j]) || Double.isInfinite(yVec[i][j])) ) {
                xVec[i][j] = xVec[i][j]/pixelWidth;  // size in terms of pixels
                yVec[i][j] = -yVec[i][j]/pixelHeight;  // sign change because pixels are numbered from top down
                double length = xVec[i][j]*xVec[i][j] + yVec[i][j]*yVec[i][j];
                if (length > maxLength)
                  maxLength = length;
            }
         }
      }
      maxLength = Math.sqrt(maxLength);
      
      int ct = 0; // which item of data are we working on?
      for (int i = 0; i < xCt; i++) {
         double x = xStart + i*dx;
         int xInt = coords.xToPixel(x);
         for (int j = 0; j < yCt; j++) {
            double y = yStart + j*dy;
            int yInt = coords.yToPixel(y);
            int[] d = data[ct];
            ct++;
            if ( Double.isNaN(xVec[i][j]) || Double.isNaN(yVec[i][j]) || 
                            Double.isInfinite(xVec[i][j]) || Double.isInfinite(yVec[i][j]) ) {
                d[i] = Integer.MIN_VALUE; // signal that vector is undefined at this point
            }
            else {
               double length = Math.sqrt(xVec[i][j]*xVec[i][j] + yVec[i][j]*yVec[i][j]);
               if (length < 1e-15 || (maxLength == 0 && (style == SCALED_LINES || style == SCALED_VECTORS))) {  // no arrow.
                  d[0] = d[2] = xInt;
                  d[1] = d[3] = yInt;
                  if (arrows)
                     d[4] = Integer.MIN_VALUE;
               }
               else {
                  double sdx, sdy;  // dx and dy scaled to a vector of right length
                  double alength;   // length of arrow or line
                  boolean clamped = false;
                  switch (style) {
                     case ARROWS:
                        sdx = 0.8 * pixelSpacing * xVec[i][j]/length;
                        sdy = 0.8 * pixelSpacing * yVec[i][j]/length;
                        d[0] = xInt;
                        d[1] = yInt;
                        d[2] = (int)(xInt + sdx);
                        d[3] = (int)(yInt + sdy);
                        break;
                     case LINES:
                        sdx = 0.8 * pixelSpacing * xVec[i][j]/length/2;
                        sdy = 0.8 * pixelSpacing * yVec[i][j]/length/2;
                        d[0] = (int)(xInt - sdx);
                        d[1] = (int)(yInt - sdy);
                        d[2] = (int)(xInt + sdx);
                        d[3] = (int)(yInt + sdy);
                        break;
                     case CLAMPED_VECTORS:
                        alength = length;
                        if  (alength > 0.9*pixelSpacing) {
                           alength = 0.9*pixelSpacing;
                           clamped = true;
                        }
                        sdx = xVec[i][j]/length*alength;
                        sdy = yVec[i][j]/length*alength;
                        d[0] = xInt;
                        d[1] = yInt;
                        d[2] = (int)(xInt + sdx);
                        d[3] = (int)(yInt + sdy);
                        break;
                     case SCALED_LINES:
                        alength = (length/maxLength)*pixelSpacing;
                        sdx = xVec[i][j]/length*alength/2;
                        sdy = yVec[i][j]/length*alength/2;
                        d[0] = (int)(xInt - sdx);
                        d[1] = (int)(yInt - sdy);
                        d[2] = (int)(xInt + sdx);
                        d[3] = (int)(yInt + sdy);
                        break;
                     case SCALED_VECTORS:
                        alength = (length/maxLength)*pixelSpacing;
                        sdx = xVec[i][j]/length*alength;
                        sdy = yVec[i][j]/length*alength;
                        d[0] = xInt;
                        d[1] = yInt;
                        d[2] = (int)(xInt + sdx);
                        d[3] = (int)(yInt + sdy);
                        break;
                  }
                  if (arrows) { // add an arrowhead
                     int d1 = (d[2] - d[0])/5;
                     int d2 = (d[3] - d[1])/5;
                     if (clamped || d1 == 0 && d2 == 0)
                        d[4] = Integer.MIN_VALUE;  // no arrowhead
                     else {
                        d[4] = d[2] + d2 - d1;
                        d[5] = d[3] - d1 - d2;
                        d[6] = d[2] - d1 - d2;
                        d[7] = d[3] + d1 - d2;
                     }
                  }
               }
            }
         }
      }
      
   } // end setup()
   
   
} // end class VectorField



