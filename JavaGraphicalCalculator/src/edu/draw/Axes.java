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

import edu.data.NumUtils;

import java.awt.*;

/**
 * A set of horizontal and vertical axes that look OK and
 * have reasonable, labeled tick marks.  The number and spacing of tick
 * marks changes depending on the scale on the axes.  (The heuristics
 * for computing this could use some improvement.)
 */
public class Axes extends Drawable {

   /**
    * Creates axes with no names on the axes.
    */
   public Axes() {
      this(null,null);
   }
   
   /**
    * Creates axes with given names on the axes.
    *
    * @param xlabel   Label for x axis.  If the value is null, no label is drawn.
    * @param ylabel   Label for y axis.  If the value is null, no label is drawn.
    */
   public Axes(String xLabel, String yLabel) {
      this.xLabel = xLabel;
      this.yLabel = yLabel;
   }
   
   /**
    * A constant that can be used in the setYAxisPosition() method to indicate the placement of the y-axis.
    * The axis is placed at the top of the CoordinateRect.
    */
   public static final int TOP = 0;

   /**
    * A constant that can be used in the setYAxisPosition() method to indicate the placement of the y-axs.
    * The axis is placed at the bottom of the CoordinateRect.
    */
   public static final int BOTTOM = 1;

   /**
    * A constant that can be used in the setXAxisPosition() method to indicate the placement of the x-axis.
    * The axis is placed at the left edge of the CoordinateRect.
    */
   public static final int LEFT = 2;

   /**
    * A constant that can be used in the setXAxisPosition() method to indicate the placement of the x-axis.
    * The axis is placed at the right edge of the CoordinateRect.
    */
   public static final int RIGHT = 3;

   /**
    * A constant that can be used in the setXAxisPosition() and setYAxisPosition() methods to indicate the placement of the axes.
    * The axis is placed in the center of the CoordinateRect.
    */
   public static final int CENTER = 4;

   /**
    * A constant that can be used in the setXAxisPosition() and setYAxisPosition() methods to indicate the placement of the axes.
    * The axis is placed at its true x- or y-position, if that lies within the range of values shown on the CoordinateRect.
    * Otherwise, it is placed along an edge of the CoordinateRect.  This is the default value for axis placement.
    */
   public static final int SMART = 5;
       

   private int xAxisPosition = SMART;
   private int yAxisPosition = SMART;
   
   private Color axesColor = new Color(0,0,180);
   
   private Color lightAxesColor = new Color(180,180,255); // Used if real axis is outside the draw rect
   private Color labelColor = Color.black;
   
   private String xLabel = null;
   private String yLabel = null;
   
   
   
   //------------------ Methods for getting/setting properties ----------------
   
   /**
    * Get the color that is used for drawing the axes, when they are drawn in their true position.
    */
   public Color getAxesColor() { 
      return axesColor; 
   }
   
   /**
    * Set the color that is used for drawing the axes, when they are drawn in their true position.
    * The default is blue.
    */
   public void setAxesColor(Color c) { 
      if (c != null && !c.equals(axesColor)) {
         axesColor = c; 
         needsRedraw();
      }
   }
   
   
   /**
    * Get the color that is used for drawing an axis, when it is drawn along an edge of the CoordinateRect
    * instead of in its proper x- or y-position.
    */
   public Color getLightAxesColor() { 
      return lightAxesColor; 
   }
   
   /**
    * Get the color that is used for drawing an axis, when it is drawn along an edge of the CoordinateRect
    * instead of in its proper x- or y-position.  The default is a light blue.
    */
   public void setLightAxesColor(Color c) {
      if (c != null && !c.equals(lightAxesColor)) {
         lightAxesColor = c; 
         needsRedraw();
      }
   }

   
   /**
    * Get the color that is used for drawing the labels on the x- and y-axes.
    */
   public Color getLabelColor() { 
      return labelColor; 
   }
   
   /**
    * Set the color that is used for drawing the labels (usually the names of the variables) on the x- and y-axes.
    * The default is black.
    */
   public void setLabelColor(Color c) { 
      if (c != null && !c.equals(labelColor)) {
         labelColor = c; 
         if (xLabel != null || yLabel != null)
            needsRedraw();
      }
   }

   
   /**
    * Get the positioning constant that tells where the x-axis is drawn.  This can be LEFT, RIGHT, CENTER, or SMART.
    */
   public int getXAxisPosition() { 
      return xAxisPosition; 
   }
   
   /**
    * Set the positioning constant that tells where the x-axis is drawn.  This can be LEFT, RIGHT, CENTER, or SMART.
    * The default is SMART.
    */
   public void setXAxisPosition(int pos) { 
       if ((pos == TOP || pos == BOTTOM || pos == CENTER || pos == SMART) && pos != xAxisPosition) {
          xAxisPosition = pos;
          needsRedraw();
       }
   }    

   
   /**
    * Get the positioning constant that tells where the y-axis is drawn.  This can be TOP, BOTTOM, CENTER, or SMART.
    */
   public int getYAxisPosition() { 
      return yAxisPosition; 
   }
   
   /**
    * Set the positioning constant that tells where the y-axis is drawn.  This can be TOP, BOTTOM, CENTER, or SMART.
    * The default is SMART.
    */
   public void setYAxisPosition(int pos) { 
       if ((pos == LEFT || pos == RIGHT || pos == CENTER || pos == SMART) && pos != yAxisPosition) {
          yAxisPosition = pos;
          needsRedraw();
       }
   }

   
   /**
    * Get the label that appears on the x-axis.  If the value is null, no label appears.
    */
   public String getXLabel() { 
      return xLabel; 
   }
   
   /**
    * Set the label that appears on the x-axis.  If the value is null, no label appears.  This is the default.
    */
   public void setXLabel(String s) { 
      xLabel = s; 
      needsRedraw();
   }

   
   /**
    * Get the label that appears on the y-axis.  If the value is null, no label appears.
    */
   public String getYLabel() { 
      return yLabel;
   }
   
   /**
    * Set the label that appears on the y-axis.  If the value is null, no label appears.  This is the default.
    */
   public void setYLabel(String s) { 
      yLabel = s;
      needsRedraw();
  }
   

   
   //--------------------------------------------------------------------------
 
    
   /**
    * Draw the axes. This is not meant to be called directly.
    *
    */
   public void draw(Graphics g, boolean coordsChanged) {
      if (coords == null) 
         return;
         
      if (coordsChanged || xTicks == null
            || !g.getFont().equals(font)) {  // The second test forces a setup() when the 
                                             // Axes object has been reloaded after serialization. 
                                             // The third test accounts for the fact that the
                                             // font might have changed since the last time
                                             // a setup() was done.
         font = g.getFont();
         FontMetrics fm = g.getFontMetrics(font);
         setup(fm, coords.getXmin(), coords.getXmax(), coords.getYmin(), coords.getYmax(),
                    coords.getLeft(), coords.getTop(), coords.getWidth(), coords.getHeight(), coords.getGap());
      }
      doDraw(g, coords.getXmin(), coords.getXmax(), coords.getYmin(), coords.getYmax(),
                    coords.getLeft(), coords.getTop(), coords.getWidth(), coords.getHeight(), coords.getGap());
   }

   
   private void doDraw(Graphics g, double xmin, double xmax, double ymin, double ymax,
                              int left, int top, int width, int height, int gap) {
         // Draw axes using data computed by setup().  The parameters come from the CoordinateRect.
      if (xAxisPosition == SMART && (ymax < 0 || ymin > 0))
         g.setColor(lightAxesColor);
      else 
         g.setColor(axesColor);
      g.drawLine(left + gap, xAxisPixelPosition, left + width - gap - 1, xAxisPixelPosition);
      for (int i = 0; i < xTicks.length; i++) {
         int a = (xAxisPixelPosition - 2 < top) ? xAxisPixelPosition : xAxisPixelPosition - 2;
         int b = (xAxisPixelPosition + 2 >= top + height)? xAxisPixelPosition : xAxisPixelPosition + 2; 
         g.drawLine(xTicks[i], a, xTicks[i], b);
      }
      for (int i = 0; i < xTickLabels.length; i++)
         g.drawString(xTickLabels[i], xTickLabelPos[i][0], xTickLabelPos[i][1]);
      if (yAxisPosition == SMART && (xmax < 0 || xmin > 0))
         g.setColor(lightAxesColor);
      else 
         g.setColor(axesColor);
      g.drawLine(yAxisPixelPosition, top + gap, yAxisPixelPosition, top + height - gap - 1);
      for (int i = 0; i < yTicks.length; i++) {
         int a = (yAxisPixelPosition - 2 < left) ? yAxisPixelPosition : yAxisPixelPosition - 2;
         int b = (yAxisPixelPosition + 2 >= left + width)? yAxisPixelPosition : yAxisPixelPosition + 2; 
         g.drawLine(a, yTicks[i], b, yTicks[i]);
      }
      for (int i = 0; i < yTickLabels.length; i++)
         g.drawString(yTickLabels[i], yTickLabelPos[i][0], yTickLabelPos[i][1]);
      g.setColor(labelColor);
      if (xLabel != null)
         g.drawString(xLabel, xLabel_x, xLabel_y);
      if (yLabel != null)
         g.drawString(yLabel, yLabel_x, yLabel_y);
   }
   
   
   private transient int[] xTicks;   // Data for drawing axes
   private transient int[] yTicks;
   private transient String[] xTickLabels;
   private transient String[] yTickLabels;
   private transient int[][] xTickLabelPos;
   private transient int[][] yTickLabelPos;
   private transient int xAxisPixelPosition, yAxisPixelPosition;
   private transient int xLabel_x, xLabel_y, yLabel_x, yLabel_y;
   private transient Font font;
   private transient int ascent, descent, digitWidth;
   
   
   void setup(FontMetrics fm, double xmin, double xmax, double ymin, double ymax,
                              int left, int top, int width, int height, int gap) {
         // Set up all data for drawing the axes.
      digitWidth = fm.charWidth('0');
      ascent = fm.getAscent();
      descent = fm.getDescent();
      switch (xAxisPosition) {
         case TOP: 
            xAxisPixelPosition = top + gap; 
            break;
         case BOTTOM: 
            xAxisPixelPosition = top + height - gap - 1; 
            break;
         case CENTER: 
            xAxisPixelPosition = top + height/2; 
            break;
         case SMART:
            if (ymax < 0)
               xAxisPixelPosition = top + gap;
            else if (ymin > 0)
               xAxisPixelPosition = top + height - gap - 1;
            else
               xAxisPixelPosition = top + gap + (int)((height-2*gap - 1) * ymax / (ymax-ymin));
            break;
      }
      switch (yAxisPosition) {
         case LEFT: 
            yAxisPixelPosition = left + gap; 
            break;
         case BOTTOM: 
            yAxisPixelPosition = left + width - gap - 1; 
            break;
         case CENTER: 
            yAxisPixelPosition = left + width/2; 
            break;
         case SMART:
            if (xmax < 0)
               yAxisPixelPosition = left + width - gap - 1;
            else if (xmin > 0)
               yAxisPixelPosition = left + gap;
            else
               yAxisPixelPosition = left + gap - (int)((width-2*gap - 1) * xmin / (xmax-xmin));
            break;
      }
      if (xLabel != null) {
         int size = fm.stringWidth(xLabel);
         if (left + width - gap - size <= yAxisPixelPosition)
            xLabel_x = left + gap;
         else
            xLabel_x = left + width - gap - size;
         if (xAxisPixelPosition + 3 + ascent + descent + gap >= top + height)
            xLabel_y = xAxisPixelPosition - 4;
         else
            xLabel_y = xAxisPixelPosition + 3 + ascent;
      }
      if (yLabel != null) {
         int size = fm.stringWidth(yLabel);
         if (yAxisPixelPosition + 3 + size + gap > left + width)
            yLabel_x = yAxisPixelPosition - size - 3;
         else
            yLabel_x = yAxisPixelPosition + 3;
         if (top + ascent + descent + gap > xAxisPixelPosition)
            yLabel_y = top + height - gap - descent;
         else
            yLabel_y = top + ascent + gap;
      }
      double start = fudgeStart( ((xmax-xmin)*(yAxisPixelPosition - (left + gap)))/(width - 2*gap)  + xmin, 
                                                  0.05*(xmax-xmin) );
      int labelCt = (width - 2*gap) / (10*digitWidth);
      if (labelCt <= 2)
         labelCt = 3;
      else if (labelCt > 20)
         labelCt = 20;
      double interval = fudge( (xmax - xmin) / labelCt );
      for (double mul = 1.5; mul < 4; mul += 0.5) {
         if (fm.stringWidth(NumUtils.realToString(interval+start)) + digitWidth > (interval/(xmax-xmin))*(width-2*gap))  // overlapping labels
             interval = fudge( mul*(xmax - xmin) / labelCt );
         else
            break;
      }
      double[] label = new double[50];
      labelCt = 0;
      double x = start + interval;
      double limit = left + width;
      if (xLabel != null && left + width - gap - fm.stringWidth(xLabel) > yAxisPixelPosition)  // avoid overlap with xLabel
         limit -= fm.stringWidth(xLabel) + gap + digitWidth;
      while (labelCt < 50 && x <= xmax) {
         if (left + gap + (width-2*gap)*(x-xmin)/(xmax-xmin) + fm.stringWidth(NumUtils.realToString(x))/2 > limit)
            break;
         label[labelCt] = x;
         labelCt++;
         x += interval;
      }
      x = start - interval;
      limit = left;
      if (xLabel != null && left + width - gap - fm.stringWidth(xLabel) <= yAxisPixelPosition)  // avoid overlap with xLabel
         limit += fm.stringWidth(xLabel) + digitWidth;
      while (labelCt < 50 && x >= xmin) {
         if (left + gap + (width-2*gap)*(x-xmin)/(xmax-xmin) - fm.stringWidth(NumUtils.realToString(x))/2 < limit)
            break;
         label[labelCt] = x;
         labelCt++;
         x -= interval;
      }
      xTicks = new int[labelCt];
      xTickLabels = new String[labelCt];
      xTickLabelPos = new int[labelCt][2];
      for (int i = 0; i < labelCt; i++) {
         xTicks[i] = (int)(left + gap + (width-2*gap)*(label[i]-xmin)/(xmax-xmin));
         xTickLabels[i] = NumUtils.realToString(label[i]);
         xTickLabelPos[i][0] = xTicks[i] - fm.stringWidth(xTickLabels[i])/2;
         if (xAxisPixelPosition - 4 - ascent >= top)
            xTickLabelPos[i][1] = xAxisPixelPosition - 4;
         else
            xTickLabelPos[i][1] = xAxisPixelPosition + 4 + ascent;
      }
      
      start = fudgeStart( ymax - ((ymax-ymin)*(xAxisPixelPosition - (top + gap)))/(height - 2*gap), 
                                                  0.05*(ymax-ymin) );
      labelCt = (height - 2*gap) / (5*(ascent+descent));
      if (labelCt <= 2)
         labelCt = 3;
      else if (labelCt > 20)
         labelCt = 20;
      interval = fudge( (ymax - ymin) / labelCt );
      labelCt = 0;
      double y = start + interval;
      limit = top + 8 + gap;
      if (yLabel != null && top + gap + ascent + descent <= xAxisPixelPosition)  // avoid overlap with yLabel
          limit = top + gap + ascent + descent;
      while (labelCt < 50 && y <= ymax) {
         if (top + gap + (height-2*gap)*(ymax-y)/(ymax-ymin) - ascent/2 < limit)
            break;
         label[labelCt] = y;
         labelCt++;
         y += interval;
      }
      y = start - interval;
      limit = top + height - gap - 8;
      if (yLabel != null && top + gap + ascent + descent > xAxisPixelPosition)  // avoid overlap with yLabel
          limit = top + height - gap - ascent - descent;
      while (labelCt < 50 && y >= ymin) {
         if (top + gap + (height-2*gap)*(ymax-y)/(ymax-ymin) + ascent/2 > limit)
            break;
         label[labelCt] = y;
         labelCt++;
         y -= interval;
      }
      yTicks = new int[labelCt];
      yTickLabels = new String[labelCt];
      yTickLabelPos = new int[labelCt][2];
      int w = 0;  // max width of tick mark
      for (int i = 0; i < labelCt; i++) {
          yTickLabels[i] = NumUtils.realToString(label[i]);
          int s = fm.stringWidth(yTickLabels[i]);
          if (s > w)
             w = s;  
      }
      for (int i = 0; i < labelCt; i++) {
         yTicks[i] = (int)(top + gap + (height-2*gap)*(ymax-label[i])/(ymax-ymin));
         yTickLabelPos[i][1] = yTicks[i] + ascent/2;
         if (yAxisPixelPosition - 4 - w < left)
            yTickLabelPos[i][0] = yAxisPixelPosition + 4;
         else
            yTickLabelPos[i][0] = yAxisPixelPosition - 4 - fm.stringWidth(yTickLabels[i]);
      }
   } // end setup()
   
   
   /**
    * Translated directly from the Pascal version of xFunctions.
    * Move x to a more "rounded" value; used for labeling axes.
    *
    * @param x   the x coordinate used for labeling axes
    * @return the rounded value of x
    */
   double fudge (double x) { 
      int i, digits;
      double y;
      if (Math.abs(x) < 0.0005 || Math.abs(x) > 500000)
         return x;
      else if (Math.abs(x) < 0.1 || Math.abs(x) > 5000) {
            y = x;
            digits = 0;
            if (Math.abs(y) >= 1) {
               while (Math.abs(y) >= 8.75) {
                     y = y / 10;
                     digits = digits + 1;
               }
            }
            else {
               while (Math.abs(y) < 1) {
                     y = y * 10;
                     digits = digits - 1;
               }
            }
            y = Math.round(y * 4) / 4;
            if (digits > 0) {
               for (int j = 0; j < digits; j++)
                  y = y * 10;
            }
            else if (digits < 0) {
               for (int j = 0; j < -digits; j++)
                  y = y / 10;
            }
            return y;
      }
      else if (Math.abs(x) < 0.5)
         return Math.round(10 * x) / 10.0;
      else if (Math.abs(x) < 2.5)
         return Math.round(2 * x) / 2.0;
      else if (Math.abs(x) < 12)
         return Math.round(x);
      else if (Math.abs(x) < 120) 
         return Math.round(x / 10) * 10.0;
      else if (Math.abs(x) < 1200)
         return Math.round(x / 100) * 100.0;
      else
         return Math.round(x / 1000) * 1000.0;
   }
   
   
   private double fudgeStart(double a, double diff) { 
        // Adapted from the Pascal version of xFunctions.
        // Tries to find a "rounded value" within diff of a.
      if (Math.abs(Math.round(a) - a) < diff)
          return Math.round(a);
      for (double x = 10; x <= 100000; x *= 10) {
          double d = Math.round(a*x) / x;
          if (Math.abs(d - a) < diff)
             return d;
      }
      return a;
   }


}  // end class Axes
   
