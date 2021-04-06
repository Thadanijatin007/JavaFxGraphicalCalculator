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
import edu.data.Value;
import edu.data.*;
import edu.awt.*;
import java.awt.*;

/**
 * A DrawGeometric object is a geometic figure such as a line or rectangle that can
 * be drawn in a CoordinateRect.  The data for the object always consists of four
 * numbers, which are interpreted differenetly depending on the object.  These numbers
 * can be specified as Value objects.  A DrawGeometric is a Computable, and the 
 * Values will be re-computed when its compute() method is called.  It should be
 * added to a Controller that can respond to any changes in the data that define
 * the Values.  If one of the Value objects has an undefined value, nothing will be drawn.
 * <p>The type of object is given as one of the constants defined in this class:
 * LINE_ABSOLUTE, OVAL_RELATIVE, CROSS, and so on.  In the descriptions of these
 * constants, x1, x2, y1, and y2 refer to the values of Value objects that provide data
 * for the DrawGeomentric while h and v refer to int's that can be specified in place of
 * x2 and y2 for certain types of figures.  For those figures, h or v is used if
 * x2 or y2, respectively, is null.
 *
 * @author David Eck
 */


public class DrawGeometric extends Drawable implements Computable {

            
            /**
             * Specifies a line segment from (x1,y1) to (x2,y2).
             */
   public static final int LINE_ABSOLUTE = 0;
            
            /**
             * Specifies a  line that extends through the points (x1,y1) and (x2,y2) and beyond.
             */
   public static final int INFINITE_LINE_ABSOLUTE = 1; 
            
            /**
             * Specifies a  rectangle with corners at (x1,y1) and (x2,y2).
             */
   public static final int RECT_ABSOLUTE = 2;
            
            /**
             * Specifies an oval that just fits in the rectangle with corners at (x1,y1) and (x2,y2).
             */
   public static final int OVAL_ABSOLUTE = 3;  
            
            /**
             * Specifies a  line segment from (x1,y1) to (x1+x2,y1+y2), or to (x1+h,y1+v) if x2,y2 are null.
             * (Note that h,v are given in terms of pixels while x1,x2,y1,y2 are given 
             * in terms of the CoordinateRect.  If you use h,v, you get a line
             * of a fixed size and direction.)
             */
   public static final int LINE_RELATIVE = 4;           
                  
            /**
             * Specifies an infinite line through (x1,y1) and (x1+x2,y1+y2), or through (x1,y1) and (x1+h,y1+v) if x2,y2 are null.
             */
   public static final int INFINITE_LINE_RELATIVE = 5;
         
            /**
             * Specifies a  rectangle with one corner at (x1,y1), and with width given by x2, or h if
             * if x2 is null, and with height given by y2, or by v if y2 is null.
             */         
   public static final int RECT_RELATIVE = 6;           
                 
            /**
             * Specifies an oval that just fits inside the rect specified by RECT_RELATIVE.
             */
   public static final int OVAL_RELATIVE = 7; 
            
            /**
             * Specifies a  line segment centered on (x1,y1).  The amount it extends in each direction
             * is given by x2,y2 or by h,v 
             */
   public static final int LINE_CENTERED = 8;            
                
            /**
             * Specifies a  Rectangle centered on (x1,y1).  The amount it extends in each direction
             * is given by x2,y2 or by h,v.  (Thus, x2 or h is the HALF-width and y2 or v is the HALF-height.)
             */
   public static final int RECT_CENTERED = 9;                
                                          
            /**
             * Specifies an oval that just fits inside the rect specified by RECT_CENTERED.
             */
   public static final int OVAL_CENTERED = 10; 
            
            /**
             * Specifies a cross centered on the point (x1,y1).  Its arms extend horizontally
             * by a distance of x2, or h, in each direction.  Its vertical
             * arms extend y2, or v, in each direction.
             */
   public static final int  CROSS = 11;                  
                                          
                                          
   /**
    * One of the constants such as OVAL_CENTERED, specifying the shape to be drawn
    */
   protected int shape;

   /**
    * One of the Value objects that determine the shape that is drawn.
    * The shape is specified by two points, (x1,y1) and (x2,y2).
    * x1 must be non-null.
    */
   protected Value x1;

   /**
    * One of the Value objects that determine the shape that is drawn.
    * The shape is specified by two points, (x1,y1) and (x2,y2).
    * x2 must be non-null
    * for the "ABSOLUTE" shapes.  (If not, they revert to
    * "RELATIVE" shapes and use h,v as the offset values.)
    */
   protected Value x2;

   /**
    * One of the Value objects that determine the shape that is drawn.
    * The shape is specified by two points, (x1,y1) and (x2,y2).
    * y1 must be non-null.
    */
   protected Value y1;

   /**
    * One of the Value objects that determine the shape that is drawn.
    * The shape is specified by two points, (x1,y1) and (x2,y2).
    * y2 must be non-null
    * for the "ABSOLUTE" shapes.  (If not, they revert to
    * "RELATIVE" shapes and use h,v as the offset values.)
    */
   protected Value y2;  
   
   /**
    * Integer that gives horizontal pixel offset from x1.
    * This is only used if x2 is null.
    */
   protected int h = 10;
   
   /**
    * Integer that gives vertical pixel offset fromy1.
    * This is only used if y2 is null.
    */
   protected int v = 10; 
 
   /**
    * Value of x1. This is re-computed when the compute() method is called.
    */
   protected double a = Double.NaN;

   /**
    * Value of y1. This is re-computed when the compute() method is called.
    */
   protected double b;

   /**
    * Value of x2. This is re-computed when the compute() method is called.
    */
   protected double c;

   /**
    * Value of y2. This is re-computed when the compute() method is called.
    */
   protected double d;  
    
   /**
    * Color of the shappe.  Color will be black if this is null.  For shapes that
    * have "insides", such as rects, this is the color of the outline.
    */
   protected Color color = Color.black; 
   
   /**
    * Rects and ovals are filled with this color, if it is non-null.
    * If this is null, only the outline of the shape is drawn.
    */
   protected Color fillColor;  
   
   /**
    * The width, in pixels, of lines, including the outlines
    * of rects and ovals.  It is restricted to being an integer
    * in the range from 0 to 10.  A value of 0 means that lines
    * won't be drawn at all; this would only be useful for a filled
    * shape that has a colored interior.
    */
   protected int lineWidth = 1; 
   
   private boolean changed = true;  // set to true when values have to be recomputed.
   
   /**
    * Create a DrawGeometric object.  By default, it is a LINE_ABSOLUTE.  However,
    * nothing will be drawn as long as x1,y1,x2,y2 are null.
    */
   public DrawGeometric() {}
   
   /**
    * Create a DrawGeometric with the specified shape and values for x1,x2,y1,y2
    * Any of the shapes makes sense in this context.
    *
    * @param shape One of the shape constants such as LINE_ABSOLUTE or RECT_RELATIVE.
    */
   public DrawGeometric(int shape, Value x1, Value y1, Value x2, Value y2) {
      setShape(shape);
      setPoints(x1,y1,x2,y2);
   }
   
   /**
    * Create a DrawGeometric with a specified shape and values.  The last two parameters
    * give pixel offsets from x1,y1.  The "ABSOLUTE" shapes don't make
    * sense in this context.  (They will be treated as the corresponding
    * "RELATIVE" shapes.)
    *
    * @param shape One of the "RELATIVE" or "CENTERED" shape constants such as LINE_RELATIVE or OVAL_CENTERED or CROSS.
    */
   public DrawGeometric(int shape, Value x1, Value y1, int h, int v) {
      setShape(shape);
      setPoints(x1,y1,h,v);
   }
   

   // ---------------- Routines for getting and setting properties --------------------------
   
   /**
    * Set the shape, which should be given as one of the shape constants such as LINE_ABSOLUTE or CROSS.
    */
   public void setShape(int shape) {
      if (shape < 0 || shape > CROSS)
         throw new IllegalArgumentException("Internal error:  Illegal value for shape of DrawGeometric object.");
      this.shape = shape;
      needsRedraw();
   }
   
   /**
    * Set the Value objects that specify the two points that determine the shape.
    * The first two parameters, x1 and y1, must be non-null.
    */
   public void setPoints(Value x1, Value y1, Value x2, Value y2) {
      this.x1 = x1;
      this.y1 = y1;
      this.x2 = x2;
      this.y2 = y2;
      compute();
   }
   
   /**
    * Set the values that specify a point (x1,y1) and an offset (h,v) from that point.
    * This only makes sense for RELATIVE shapes.  The Value objects x1 and y1 must be non-null
    */
   public void setPoints(Value x1, Value y1, int h, int v) {
      this.x1 = x1;
      this.y1 = y1;
      this.x2 = null;
      this.y2 = null;
      this.h = h;
      this.v = v;
      compute();
   }
   
   /**
    * Set the value that gives the x-coordinate of the first point that determines the shape.
    * This must be non-null, or nothing will be drawn.
    */
   public void setX1(Value x) {
      x1 = x;
      compute();
   }
   
   /**
    * Get the value that gives the x-coordinate of the first point that determines the shape.
    */
   public Value getX1() {
      return x1;
   }


   /**
    * Set the value that gives the x-coordinate of the second point that determines the shape.
    * If this is null, then the value of h is used instead.
    */
   public void setX2(Value x) {
      x2 = x;
      compute();
   }

   /**
    * Get the value that gives the x-coordinate of the second point that determines the shape.
    */
   public Value getX2() {
      return x2;
   }

   /**
    * Set the value that gives the y-coordinate of the first point that determines the shape.
    * This must be non-null, or nothing will be drawn.
    */
   public void setY1(Value y) {
      y1 = y;
      compute();
   }

   /**
    * Get the value that gives the y-coordinate of the first point that determines the shape.
    */
   public Value getY1() {
      return y1;
   }

   /**
    * Set the value that gives the y-coordinate of the second point that determines the shape.
    * If this is null, then the value of v is used instead.
    */
   public void setY2(Value y) {
      y2 = y;
      compute();
   }
   
   /**
    * Get the value that gives the y-coordinate of the second point that determines the shape.
    */
   public Value getY2() {
      return y2;
   }

   /**
    * Set the integer that gives the horizontal offset from (x1,y1). 
    * This only makes sense for RELATIVE shapes.  This method also sets x2 to null,
    * since the h value is only used when x2 is null.
    */
   public void setH(int x) {
      h = x;
      x2 = null;
      compute();
   }
   
   /**
    * Get the horizontal offset from (x1,y1). 
    */
   public int getH() {
      return h;
   }

   /**
    * Set the integer that gives the vertical offset from (x1,y1). 
    * This only makes sense for RELATIVE shapes. This method also sets y2 to null,
    * since the v value is only used when y2 is null.
    */
   public void setV(int y) {
      v = y;
      y2 = null;
      needsRedraw();
   }
   
   /**
    * Get the vertical offset from (x1,y1). 
    */
   public int getV() {
      return v;
   }

    /**
    * Set the color that is used for drawing the shape.  If the color is null, black is used.
    * For shapes that have interiors, such as rects, this is only the color of the outline of the shaape.
    */
  public void setColor(Color c) {
      color = (c == null)? Color.black : c;
      needsRedraw();
   }
   
   /**
    * Get the non-null color that is used for drawing the shape.
    */
   public Color getColor() {
      return color;
   }
   
   /**
    * Set the color that is used for filling ovals and rects.  If the color is null, only the outline of the shape is drawn.
    */
   public void setFillColor(Color c) {
      fillColor = c;
      needsRedraw();
   }
   
   /**
    * Get the color that is used for filling ovals and rects.  If null, no fill is done.
    */
   public Color getFillColor() {
      return fillColor;
   }
   
   /**
    * Set the width, in pixels, of lines that are drawn.  This is also used for outlines of rects and ovals.
    */
   public void setLineWidth(int width) {
      if (width != lineWidth) {
         lineWidth = width;
         if (lineWidth > 10)
            lineWidth = 10;
         else if (lineWidth < 0)
            lineWidth = 0;
         needsRedraw();
      }
   }
   
   /**
    * Get the width, in pixels, of lines that are drawn.  This is also used for outlines of rects and ovals.
    */
   public int getLineWidth() {
      return lineWidth;
   }
   
   // ------------------------- Implementation details ------------------------------------
   
   /**
    * Recompute the values that define the size/postion of the DrawGeometric.
    * This is ordinarily only called by a Controller.
    */
   public void compute() {
      changed = true;
      needsRedraw();
   }
   
   private void doValues() {
      if (x1 != null)
         a = x1.getVal();
      if (y1 != null)
         b = y1.getVal();
      if (x2 != null)
         c = x2.getVal();
      if (y2 != null)
         d = y2.getVal();
      changed = false;
   }
   
   /**
    * Do the drawing.  This is not meant to be called directly.
    */ 
   public void draw(Graphics g, boolean coordsChanged) {
       if (changed)
          doValues();
       if (coords == null || x1 == null || y1 == null ||
              Double.isNaN(a) || Double.isNaN(b) ||
              Double.isInfinite(a) || Double.isInfinite(b) )
          return;
       if (x2 != null && (Double.isNaN(c) || Double.isInfinite(c)))
          return;
       if (y2 != null && (Double.isNaN(d) || Double.isInfinite(d)))
          return;
          
       // Get the four real numbers that determine the shape, in terms of pixels.
          
       double A,B,W,H;
       
       A = xToPixelDouble(a);
       B = yToPixelDouble(b);
       if (x2 == null)
          W = h;
       else if (shape <= OVAL_ABSOLUTE)
          W = xToPixelDouble(c) - A;
       else
          W = c/coords.getPixelWidth();
       if (y2 == null)
          H = -v;
       else if (shape <= OVAL_ABSOLUTE)
          H = yToPixelDouble(d) - B;
       else
          H = -d/coords.getPixelHeight();
       
       if (shape == INFINITE_LINE_ABSOLUTE || shape == INFINITE_LINE_RELATIVE)
          drawInfiniteLine(g, A, B, W, H);
       else if (shape == CROSS)
          drawCross(g, (int)A, (int)B, (int)(Math.abs(W)+0.5), (int)(Math.abs(H)+0.5));
       else if (shape == LINE_RELATIVE || shape == LINE_ABSOLUTE)
          drawLine(g, (int)A, (int)B, (int)(A+W), (int)(B+H));
       else if (shape == LINE_CENTERED)
          drawLine(g,(int)(A-Math.abs(W)+1),(int)(B-Math.abs(H)+1),(int)(A+Math.abs(W)),(int)(B+Math.abs(H)));
       else if (shape <= OVAL_RELATIVE) {
          if (W < 0) {
             W = -W;
             A = A - W;
          }
          if (H < 0) {
             H = -H;
             B = B - H;
          }
          drawShape(g, (int)A, (int)B, (int)(W+0.5), (int)(H+0.5));
       }
       else
          drawShape(g,(int)(A-Math.abs(W)+1),(int)(B-Math.abs(H)+1),(int)(2* Math.abs(W)-0.5),(int)(2*Math.abs(H)-0.5));
          
   }
   
   private double xToPixelDouble(double x) {
      return coords.getLeft() + coords.getGap() + ((x - coords.getXmin())/(coords.getXmax() - coords.getXmin()) * (coords.getWidth()-2*coords.getGap()-1));
   }
   
   private double yToPixelDouble(double y) {
      return coords.getTop() + coords.getGap() + ((coords.getYmax() - y)/(coords.getYmax() - coords.getYmin()) * (coords.getHeight()-2*coords.getGap()-1));   
   }
   
   private void drawLine(Graphics g, int x1, int y1, int x2, int y2) {
      int width = Math.abs(x2 - x1);
      int height = Math.abs(y2 - y1);
      g.setColor( color );
      if (width == 0 && height == 0)
         g.drawLine(x1,y1,x1,y1);
      else if (width > height) {
         for (int i = 0; i < lineWidth; i++)
            g.drawLine(x1,y1-lineWidth/2+i,x2,y2-lineWidth/2+i);
      }
      else {
         for (int i = 0; i < lineWidth; i++)
            g.drawLine(x1-lineWidth/2+i,y1,x2-lineWidth/2+i,y2);
      }
   }
   
   
   /**
    * Draws a rect or oval.
    *
    * @param x the top-left x value of the rect or the rect that contains the oval
    * @param y the top-left y value of the rect or the rect that contains the oval
    * @param width   width of the rect
    * @param height  height of the rect
    */
   private void drawShape(Graphics g, int x, int y, int width, int height) {
      if (x > coords.getLeft() + coords.getWidth() || y > coords.getTop() + coords.getHeight()
                                  || x + width < coords.getLeft() || y + height < coords.getTop()) {
         return;
      }
      if (fillColor != null) {
         g.setColor(fillColor);
         if (shape == RECT_ABSOLUTE || shape == RECT_RELATIVE || shape == RECT_CENTERED)
            g.fillRect(x,y,width,height);
         else
            g.fillOval(x,y,width,height);
      }
      g.setColor( color );  
      if (shape == RECT_ABSOLUTE || shape == RECT_RELATIVE || shape == RECT_CENTERED) {
         for (int i = 0; i < lineWidth; i++)
            g.drawRect(x+i,y+i,width-2*i,height-2*i);
      }
      else {
         for (int i = 0; i < lineWidth; i++)
            g.drawOval(x+i,y+i,width-2*i,height-2*i);         
      }
   }

   private void drawCross(Graphics g, int x, int y, int width, int height) {
      if (x - width> coords.getLeft() + coords.getWidth() || y - height > coords.getTop() + coords.getHeight()
                                  || x + width < coords.getLeft() || y + height < coords.getTop()) {
         return;
      }
      int left = x - lineWidth/2;
      int top = y - lineWidth/2;
      g.setColor( color );
      for (int i = 0; i < lineWidth; i++)
         g.drawLine(x-width,top+i,x+width,top+i);
      for (int i = 0; i < lineWidth; i++)
         g.drawLine(left+i,y-height,left+i,y+height);
   }

   private void drawInfiniteLine(Graphics g, double x, double y, double dx, double dy) {
      if (Math.abs(dx) < 1e-10 && Math.abs(dy) < 1e-10)
         return;
      g.setColor( color ); 
      if (Math.abs(dy) > Math.abs(dx)) {
         double islope = dx / dy;
         int y1 = coords.getTop() - 5;
         int y2 = coords.getTop() + coords.getHeight() + 5;
         int x1 = (int)(islope*(y1 - y) + x);
         int x2 = (int)(islope*(y2 - y) + x);
         if (Math.abs(x1) < 20000 && Math.abs(x2) < 20000)
            for (int i = 0; i < lineWidth; i++)
               g.drawLine(x1-lineWidth/2+i,y1,x2-lineWidth/2+i,y2);
      }
      else {
         double slope = dy / dx;
         int x1 = coords.getLeft() - 5;
         int x2 = coords.getLeft() + coords.getWidth() + 5;
         int y1 = (int)(slope*(x1 - x) + y);
         int y2 = (int)(slope*(x2 - x) + y);
         if (Math.abs(y1) < 20000 && Math.abs(y2) < 20000)
            for (int i = 0; i < lineWidth; i++)
               g.drawLine(x1,y1-lineWidth/2+i,x2,y2-lineWidth/2+i);
      }
   }


} // end class DrawGeometric

