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
import edu.data.NumUtils;
import edu.data.Value;
import edu.data.*;
import edu.awt.*;
import java.util.StringTokenizer;

/** 
 * A DrawString object displays a string, possibly multi-line, in a DisplayCanvas,
 * inside the rectangular region of a CoordinateRect.  The location of the string
 * can be specified in two ways.  First, by giving the coordinates of a reference
 * point together with a constant that says how the string is positioned with
 * respect to that reference point.  The coordintes are given as Value objects
 * and the values are interepreted in the coordinate system of the CoordinateRect.
 * The positioning object is one of the constants TOP_LEFT, TOP_CENTER, ...,
 * BOTTOM_RIGHT defined in this class.  This says where the REFERENCE POINT
 * is -- at the top left of the string, at the top center, etc.
 * <p>The second way to specify the position of the string is to set the reference
 * point coordinates to null.  In that case, the postioning constant gives
 * the location of the STRING in the CorrdinateRect.   A value of TOP_LEFT
 * says that the string is in the top left corner of the rect, etc.
 * 
 * <p>An array of Value objects can be specified to be displayed in the string.
 * Their values are substituted for #'s in the string.  (A double # in the string,
 * however, is displayed as a literal single #.)  
 * 
 * <p>It is possible to set the color, font and justification of the string. 
 *
 * <p>A DisplayString implements the Computable interface, so it can be added to
 * a Controller.  The values of the Value objects used by the string are recomputed
 * only when its compute() method is called.
 */

public class DrawString extends Drawable implements Computable {

   /**
    * Specify string location in rect
    */
   public static final int TOP_LEFT      = 4*0 + 0,
                           TOP_CENTER    = 4*0 + 1,
                            //value/4 gives vertical position
                           TOP_RIGHT     = 4*0 + 2,
                            // value%4 gives horizontal position
                           CENTER_LEFT   = 4*1 + 0, 
                           CENTER_CENTER = 4*1 + 1,
                           CENTER_RIGHT  = 4*1 + 2,
                           BOTTOM_LEFT   = 4*2 + 0,
                           BOTTOM_CENTER = 4*2 + 1,
                           BOTTOM_RIGHT  = 4*2 + 2;

                           /**
                            * For specifying justification of lines in multiline strings.
                            * (But can also be used as a synonym for CENTER_CENTER to specify the position of the string).
                            */
    public static final int CENTER        = CENTER_CENTER;

                           /**
                            * For specifying justification of lines in multiline strings.
                            * (But can also be used as a synonym for TOP_LEFT to specify the position of the string).
                            */
    public static final int LEFT          = TOP_LEFT;  

                           /**
                            * For specifying justification of lines in multiline strings.
                            * (But can also be used as a synonym for TOP_RIGHT to specify the position of the string).
                            */
    public static final int RIGHT         = TOP_RIGHT;

   /**
    * one of the constants defined in this class for specifying position
    */
   protected int position;  
   
   /**
    * String, possibly with \n and #'s.  This is used as a base to get the actual string that is drawn.
    */
   protected String baseString;  
   
   /**
    * The actual lines to draw, derived from baseString.
    */
   protected String[] strings;  
   
   /**
    * Values to be substituted for #'s in the baseString.
    */
   protected Value[] values;
   
   /**
    * xy-coords for drawing the string.  If non-null then relative positioning is used.
    * If null, then positioning is absolute.
    */
   protected Value xPos,yPos;   
                               
   /**
    * Color of string.  If null, black is used as the default.
    */
   protected Color color;  
   
   /**
    * Font for drawing string.  If null, get font from graphics context.
    */
   protected Font font;  
   
   /**
    * If absolute positioning is used, then this gives a gap between the string and edge of rect.
    * For relative positioning, this gives an offset from the value of xPos yPos.
    */
   protected int offset = 3;  
   
   /**
    * If true, the string is clamped to lie within the CoordinateRect.
    */
   protected boolean clamp = true;  
   
   /**
    * Left, right, or center justification of lines in the text.
    */
   protected int justification = LEFT; 
   
   /**
    * Maximum number of characters desired in numbers; actual number might actually be larger.
    */
   protected int numSize = 10;  

   /**
    *  If backgroundColor is non-null, then a rectangle of this color is filled
    *  as a background for the string;
    */ 
   protected Color backgroundColor;
   
   /**
    *  If frameWidth is greater than zero, then a frame of this width is drawn around the
    *  string in the color given by frameColor.
    */
   protected int frameWidth;
   
   /**
    *  If frameWidth is greate than zero, then a frame is drawn around the string in this
    *  color.  If the value is null, then the color will be the same as the color of the string.
    */
   protected Color frameColor;

   // In the following, note that str can contain \n to break up the
   // string into multiple lines.
   
   private double xRef,yRef; //Coordinates of reference point where string is drawn.
   private boolean changed = true;  // set to true when strings need to be recomputed
  
   
   /**
    * Create a DrawString object that initially has no string to draw.
    */ 
   public DrawString() {
      this(null,TOP_LEFT,(Value[])null);
   }
   
   /**
    * Create a DrawString for drawing a black string in the top left corner of the coordinate rect.
    *
    * @param str The string to draw, which can contain \n's to indicate line breaks.   
    */
   public DrawString(String str) {
      this(str,TOP_LEFT,(Value[])null);
   }
   
   /**
    * Create a DrawString for drawing a black string in the position specified.
    *
    * @param str The string to draw, which can contain \n's to indicate line breaks.   
    * @param pos The positioning of the string in the coordinate rect.  One of the positioning constants such as TOP_LEFT or BOTTOM_RIGHT.   
    */
   public DrawString(String str, int pos) {
      this(str,pos,(Value[])null);
   }
   
   /**
    * Create a DrawString for drawing a black string in the specified position.
    * The number of #'s in the string should match values.length.  The values
    * are computed and substituted for the #'s.
    *
    * @param str The string to draw, which can contain \n's to indicate line breaks and #'s to be replaced by numeric values.   
    * @param pos The positioning of the string in the coordinate rect.  One of the positioning constants such as TOP_LEFT or BOTTOM_RIGHT.   
    * @param values Value objects associated with #'s in the string.
    */
   public DrawString(String str, int pos, Value[] values) {
      position = pos;
      this.values = values;
      setString(str);
   }
   
   /**
    * Create a string that is displayed at the reference point (xPos,yPos);
    * The positioning constant, pos, gives the positioning relative to this point, if xPos or yPos is non-null.
    *
    * @param str The string to draw, which can contain \n's to indicate line breaks and #'s to be replaced by numeric values.   
    * @param pos The positioning of the string.  One of the positioning constants such as TOP_LEFT or BOTTOM_RIGHT.
               If xPos or yPos is non-nul, this is interpreted relative to their values.
    * @param xPos x-coordinate relative to which the string is drawn (or null for absolute hoizontal positioning).
    * @param yPos y-coordinate relative to which the string is drawn (or null for absolute vertical positioning).
    * @param values Value objects associated with #'s in the string.
    */
   public DrawString(String str, int pos, Value xPos, Value yPos, Value[] values) {
      setReferencePoint(xPos,yPos);
      position = pos;
      this.values = values;
      setString(str);  
   }
   
   /**
    * Set the color for the string.  If c is null, Color.black is used.
    *
    */
   public void setColor(Color c) {
      color = c;
      needsRedraw();
   }
   
   /**
    * Get the non-null color that is used for drawing the string.
    *
    */
   public Color getColor() {
      return (color == null)? Color.black : color;
   }
   
   /**
    * Set the font that is used for drawing this string.  If f is null,
    * then the font is obtained from the Graphics context in which the
    * string is drawn.
    *
    */
   public void setFont(Font f) {
      font = f;
      needsRedraw();
   }
   
   /** 
    * Return the font that is used for drawing the string.  If the return
    * value is null, then the font is taken from the Graphics context.
    *
    */
   public Font getFont() {
      return font;
   }
   
   /**
    * Set the Values that are substituted for (single) #'s in the string.
    * If the array of Values is null, then no substitution is done.  The length of the array should match
    * the number of #'s, but it is not an error if they do not match.
    * Extra values will be ignored; extra #'s will be shown as "undefined".
    *
    */
   public void setValues(Value[] v) {
      values = v;
      changed = true;
      needsRedraw();
   }
   
   /**
    * Return the array of values that are substituted for #'s in the string.
    *
    */
   public Value[] getValues() {
      return  values;
   }
   
   /**
    * Set the positioning of the string.  The parameter should be one of the positioning
    * contstants defined in this class, such as TOP_LEFT.  (If it is not,
    * TOP_LEFT is used by default.) 
    *
    */
   public void setPositioning(int pos) {
       position = pos;
       needsRedraw();
   }
   
   /**
    * Return the positioning, as set by setPositioning().
    *
    */
   public int getPositioning() {
      return position;
   }
   
   /**
    * Set the values of the (x,y) coordinates of the 
    * reference point for the stirng.  If a value is null,
    * absolute positioning is used.  If a value is
    * undefined, the string is not drawn.
    *
    */
   public void setReferencePoint(Value x, Value y) {
      xPos = x;
      yPos = y;
      try {
         if (xPos != null)
            xRef = xPos.getVal();
         if (yPos != null)
            yRef = yPos.getVal();
      }
      catch (RuntimeException e) {
      }
      needsRedraw();
   }
   
   /**
    * Return the Value object that gives the x-coordinate of the reference
    * point of this string.
    *
    */
   public Value getXPos() {
      return xPos;
   }
   
   /**
    * Return the Value object that gives the y-coordinate of the reference
    * point of this string.point of this string.
    *
    */
   public Value getYPos() {
      return yPos;
   }
   
   /**
    * Set the string that is displayed.  Note that it can include '\n' to
    * represent a line break, and it can contain #'s which will be replaced
    * by computed values.
    *
    */
   public void setString(String str) {
      baseString = str;
      strings = null;
      changed = true;
      needsRedraw();
   }
   
   /**
    * Get a copy of the display string (with \n's #'s, not with substitued values.)
    *
    */
   public String getString() {
      return baseString;
   }
   
   /**
    * Set the distance of the bounding box of the string from the reference
    * point where it is drawn.  The default value is 3.
    *
    */
   public void setOffset(int b) {
      offset = b;
      needsRedraw();
   }
   
   /**
    * Get the distance of the bounding box of the string from the reference
    * point where it is drawn.
    *
    */
   public int getOffset() {
      return offset;
   }
   
   /**
    * Set the "clamp" property of the DrawString.
    * If set to true, the string will be clamped to lie entirely within the CoordinateRect
    * (unless it doens't fit -- then it can stick out on the right and bottom).
    * The default value is true.
    *
    */
   public void setClamp(boolean clamp) {
      this.clamp = clamp;
      needsRedraw();
   }
   
   /**
    * Returns true if the string is set to be clamped to lie within the CoordinateRect.
    *
    */
   public boolean getClamp() {
      return clamp;
   }
   
   /**
    * Set the justification to be used if there are multiple lins in the string.
    * Possible value are DrawString.LEFT, DrawString.RIGHT, and DrawString.CENTER.
    *
    */
   public void setJustification(int j) {
      if (j == RIGHT || j == CENTER)
         justification = j;
      else 
         justification = LEFT;
      needsRedraw();
   }
   
   /**
    * Get the justification that is used for a multiple-line string.  The value
    * is one of the constants DrawString.LEFT, DrawString.RIGHT, or DrawString.CENTER
    */
   public int getJustification() {
      return justification;
   }
   
   /**
    * Set the desired maximum number of characters in displayed numbers.
    * Actual size might be larger.  Value is clamped to the range
    * 6 to 25.
    *
    */
   public void setNumSize(int size) {
      numSize = Math.min(Math.max(size,6),25);
      changed = true;
      needsRedraw();
   }
   
   /**
    * Return the desired maximum number of characters in displayed numbers.
    *
    */
   public int getNumSize() {
      return numSize;
   }
   
   /**
    * Get the color that is used to fill a rectangle on which the string is drawn.  Null
    * indicates that no rectangle is filled so the stuff in back of the string shows though.
    * The default value is null.
    */
   public Color getBackgroundColor() {
      return backgroundColor;
   }
   
   /**
    * Set the color that is used to fill a rectangle on which the string is drawn.  If the
    * value is null, no rectangle is filled and the string just overlays whatever is in back
    * of it on the canvas.
    */
   public void setBackgroundColor(Color color) {
      backgroundColor = color;
      needsRedraw();
   }
   
   /**
    * Get the color that is used to draw a frame around the string.  This is only done if the
    * frameWidth property is greater than zero.  If the value is null, the frame is the same color
    * as the string.
    */
   public Color getFrameColor() {
      return frameColor;
   }
   
   /**
    * Set the color that is used to draw a frame around the string.  This is only done if the
    * frameWidth property is greater than zero.  If the value is null, the frame is the same color
    * as the string.
    */
   public void setFrameColor(Color color) {
      frameColor = color;
      needsRedraw();
   }
   
   /**
    * Get the width, in pixels, of the frame that is drawn around the string.   
    * The default width is zero.  The largest possible value is 25.
    */
   public int getFrameWidth() {
      return frameWidth;
   }
   
   /**
    * Set the width, in pixels, of a frame to draw around the string.  If the value is zero,
    * no frame is drawn.  The default value is zero.  The the value is clamped
    * to the range 0 to 25.
    */
   public void setFrameWidth(int width) {
      if (width < 0)
         frameWidth = 0;
      else if (width > 25)
         frameWidth = 25;
      else
         frameWidth = width;
      needsRedraw();
   }
   
   /**
    * The compute method sets up the array of strings that is actually displayed.
    * This is required by the Computable interface and is usually called by a
    * Controller rather than directly.
    */
   public void compute() { 
      changed = true;
      needsRedraw(); 
   }
   

   private void getSubstitutedText() { //Get the strings obtained by substituting values for #'s in text.
      changed = false;
      if (xPos != null)
         xRef = xPos.getVal();
      if (yPos != null)
         yRef = yPos.getVal();
      if (values == null && strings != null)  // no need to recompute, since there is no #-substitution to do.
         return;
      if (baseString == null || baseString.trim().length() == 0) {
         strings = null;
         return;
      }
      StringTokenizer tok = new StringTokenizer(baseString, "\n");
      int count = tok.countTokens();
      strings = new String[count];
      if (values == null) {
         for (int i = 0; i < count; i++)
            strings[i] = tok.nextToken();
         return;
      }
      StringBuffer b = new StringBuffer();
      int expCt = 0;
      for (int strNum = 0; strNum < count; strNum++) {
         String text = tok.nextToken();
         for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '#') {
               if (i != text.length() - 1 && text.charAt(i+1) == '#') {
                  b.append('#');
                  i++;
               }
               else {
                  if (expCt < values.length) {
                     try {
                        b.append(NumUtils.realToString(values[expCt].getVal(),numSize));
                     }
                     catch (RuntimeException e) {
                        b.append("(error)");
                     }
                     expCt++;
                  }
                  else
                     b.append("undefined");
               }
            }
            else
               b.append(text.charAt(i));
         }
         strings[strNum] = b.toString();
         b.setLength(0);
      }
   }

   /**
    * Draws the string.
    */
   public void draw(Graphics g, boolean coordsChanged) { 

      if (changed)
         getSubstitutedText();
         
      if (strings == null)
         return;
         
      if (xPos != null && (Double.isNaN(xRef) || Double.isInfinite(xRef)))
         return;
      if (yPos != null && (Double.isNaN(yRef) || Double.isInfinite(yRef)))
         return;
         
      int trueOffset = offset;  // offset allowing for frame and background
      if (backgroundColor != null || frameWidth > 0)
         trueOffset += 3;
      trueOffset += frameWidth;
         
      Font saveFont = null;
      FontMetrics fm;
      if (font != null) {
         saveFont = g.getFont();
         g.setFont(font);
         fm = g.getFontMetrics(font);
      }
      else
         fm = g.getFontMetrics(g.getFont());
      int lineHeight = fm.getHeight();

      int xmin = coords.getLeft();
      int width = coords.getWidth();
      int ymin = coords.getTop();
      int height = coords.getHeight();
      int xmax = xmin+width;
      int ymax = ymin+height;
      int stringWidth = 0;
      for (int i = 0; i < strings.length; i++)
         stringWidth = Math.max(stringWidth,fm.stringWidth(strings[i]));
      int stringHeight = strings.length*lineHeight;
      if (backgroundColor == null && frameWidth <= 0)
         stringHeight = stringHeight - fm.getLeading() - fm.getDescent();
      int xInt=0,yInt=0;

      int hPos = position % 4;
      int vPos = position / 4;
      if (position < 0 || hPos > 2 || vPos > 2) {  // Use TOP_LEFT as default, if position is not a legal value.
         hPos = 0;
         vPos = 0;
      }

      if (xPos == null) {
         if (hPos == 0)
           xInt = xmin + trueOffset;
         else if (hPos == 1)
           xInt = (xmin + xmax - stringWidth) / 2;
         else
           xInt = xmax - stringWidth - trueOffset;
      }
      else {
         if (hPos == 0)
           xInt = coords.xToPixel(xRef) + trueOffset;
         else if (hPos == 1)
           xInt = coords.xToPixel(xRef) - stringWidth / 2;
         else
           xInt = coords.xToPixel(xRef) - stringWidth - trueOffset;
      }
         
      if (yPos == null) {
         if (vPos == 0)
           yInt = ymin + trueOffset;
         else if (vPos == 1)
           yInt = (ymin + ymax - stringHeight) / 2;
         else
           yInt = ymax - stringHeight - trueOffset;
      }
      else {
         if (vPos == 0)
           yInt = coords.yToPixel(yRef) + trueOffset;
         else if (vPos == 1)
           yInt = coords.yToPixel(yRef) - stringHeight / 2;
         else
           yInt = coords.yToPixel(yRef) - stringHeight - trueOffset;
      }
      
      if (clamp) {
         if (xInt + stringWidth > xmax)
            xInt = xmax - stringWidth;
         if (xInt < xmin)
            xInt = xmin;
         if (yInt + stringHeight > ymax)
            yInt = ymax - stringHeight;
         if (yInt < ymin)
            yInt = ymin;
      }
      
      if (backgroundColor != null) {
         g.setColor(backgroundColor);
         g.fillRect(xInt-3, yInt-3, stringWidth+6, stringHeight+6);
      }
      
      if (frameWidth > 0) {
         if (frameColor != null)
            g.setColor(frameColor);
         else if (color != null)
            g.setColor(color);
         else
            g.setColor(Color.black);
         for (int k = 1; k <= frameWidth; k++)
            g.drawRect(xInt-3-k,yInt-3-k,stringWidth+5+2*k,stringHeight+5+2*k);
      }

      if (color != null)
         g.setColor(color);
      else
         g.setColor(Color.black);

      yInt += fm.getAscent();
      for (int i = 0; i < strings.length; i++) {
         int x = xInt;
         if (justification == CENTER)
            x = x + (stringWidth - fm.stringWidth(strings[i]))/2;
         else if (justification == RIGHT)
            x = x + stringWidth - fm.stringWidth(strings[i]);
         g.drawString(strings[i],x,yInt);
         yInt += lineHeight;
      }
      
      if (saveFont != null)
         g.setFont(saveFont);
      
   }
      
} // end class DrawString

