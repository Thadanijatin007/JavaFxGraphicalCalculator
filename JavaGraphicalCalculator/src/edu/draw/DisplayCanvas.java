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
import edu.awt.Controller;
import edu.awt.ErrorReporter;
import edu.awt.InputObject;
import edu.awt.*;
import java.util.StringTokenizer;
import java.util.Vector;
import java.awt.*;
import java.awt.event.*;

/**
 * A DisplayCanvas is a drawing area that can contain one or more CoordinateRects.  
 * Each CoordinateRect can, in turn, contain Drawable items.  If you only want
 * one CoordinateRect that fills the whole canvas, you can for the most part
 * ignore the CoordinateRect and work only with the canvas.
 * The option of using an offscreen image for double buffering is provided.
 * By default, this option is on.  
 * <p>If a DisplayCanvas is added to a
 * Controller, then it will take care of calling the checkInput()
 * and compute() methods of the InputObjects and Computables that
 * it contains, so there is no need to add them individually to
 * the Controller.  If the DisplayCanvas is added to a JCMPanel, it
 * is automatically added to the Controller for that JCMPanel.
 * (On the other hand, if a DisplayCanvas is added to a Controller,
 * this means that all the
 * items in the DisplayCanvas will be recomputed, even if only some
 * of them need to be.)
 * <p>The canvas can display an error message that goes away
 * when the canvas is clicked or when clearErrorMessage()
 * is called.  This allows the Canvas to be used as an 
 * ErrorReporter for a Controller or LimitControlPanel.
 * <p>When any changes are made to the contents of the Canvas,
 * doRedraw() must be called for those changes to take effect.  
 * Normally, doRedraw() is called by the CoordinateRect or by
 * one of the Drawables in a CoordinateRect.  Use doRedraw(CoordinateRect c) 
 * or doRedraw(int index) to redraw a single CoordinateRect. Note that
 * repainting the canvas is not enough, because this will not automatically
 * refresh the off-screen image.
 *
 */

public class DisplayCanvas extends Canvas
                 implements ErrorReporter, InputObject, Computable {


   private Vector coordinateRects;  
      // Contains all the CoordinateRects that have been added to this canvas.
      // The elements are members of the private static nested class CRData.
   
   private boolean useOffscreenCanvas = true;  // If true, double buffering is used.
   
   private boolean handleMouseZooms = false;   // If true, clicking on a CoordinateRect
                                               //   will zoom in on the clicked point, or
                                               //   will zoom out if the shift key is down.
   
   /**
    * Create a DisplayCanvas with a white background containing no CoordinateRects.
    */   
   public DisplayCanvas() {
      setBackground(Color.white);
      enableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
   }
   
   /**
    * Create a DisplayCanvas with a white background and containing the
    * specified CoordinateRect.  The CoordinateRect fills the entire canvas.
    *
    * @param c The CoordinateRect that will fill the canvas.  If c is null, no CoordinateRect is added to the canvas.
    */   
   public DisplayCanvas(CoordinateRect c) {
      this();
      if (c != null)
         addCoordinateRect(c);
   }

   /**
    * Release the memory used the by the off-screen image, if any, that is used for
    * double-buffering.  It's a good idea to call this if the DisplayCanvas is in
    * an applet and the applet is stopped.
    */   
   public void releaseResources() {
      OSC = null;
      OSG = null;
   }
   

   /**
    * Set the "handleMouseZooms" property of this DisplayCanvas.  IF the value is true,
    * then clicking on the canvas will zoom in on the point that is clicked and shift-clicking
    * will zoom out from that point.  Only the CoordinateRect, if any, that contains the point
    * is zoomed.  Furthermore, if the user clicks-and-drags, a rectangle
    * is drawn.  When the mouse is released, the interior of the rectangle is zoomed to fill
    * the CoordinateRect.  This property is false by default.
    */
   public void setHandleMouseZooms(boolean handle) {
      handleMouseZooms = handle;
   }
   
   /**
    * Get the "handleMouseZooms" property of this DisplayCanvas, which determines whether the
    * canvas reacts to mouse events by zooming the CoordinateRect that is clicked.
    */
   public boolean getHandleMouseZooms() {
      return handleMouseZooms;
   }
   
   /**
    * Set the "useOffscreenCanvas" property of this DisplayCanvas.  IF the value is true,
    * an off-screen image is used for double buffering.  This property is true by default.
    */
   public boolean getUseOffscreenCanvas() { 
      return useOffscreenCanvas;
   }

   /**
    * Get the "useOffscreenCanvas" property of this DisplayCanvas, which determines whether
    * double-buffering is used.
    */
   public void setUseOffscreenCanvas(boolean use) {
      useOffscreenCanvas = use;
      if (!use) {
         OSC = null;
         OSG = null;
      }
   }
   
   // ----------- For managing CoordinateRects ------------------------
     
   /**
    * This private subclass of DisplayCanvas holds the data for one CoordinateRect 
    * contained in a DisplayCanvas.
    */
   private static class CRData implements java.io.Serializable {  
            // Data for one coordinate rect
      CoordinateRect coords;
      double xmin, xmax, ymin, ymax; 
               // Values between 0 and 1 that
               // specify the region of the canvas occupied by this
               // CoordinateRect.
      Color background;  // Color to fill area with before drawing.
                         // If it's null, no fill is done.  (The display color
                         // of the Canvas shows through.)
   }
   
   /**
    * Add the specified Drawable item to the first CoordinateRect in this DisplayCanvas.
    * If no CoordinateRect is associated with the canvas, one is created to fill the
    * entire canvas.
    */
   public void add(Drawable d) {
      if (coordinateRects == null)
         addCoordinateRect(new CoordinateRect());
      CoordinateRect c = ((CRData)coordinateRects.elementAt(0)).coords;
      c.add(d);
   }
   
   /**
    * Add a Drawable item to one of the CoordinateRects associated with the Canvas.
    *
    * @param d The Drawable item to be added to a CoordinateRect
    * @param coordRectIndex The index of the CoordinateRect, where the index of the first
    *    CoordinateRect that was added to the cavas is zero, the index of the second is one,
    *    and so on.  A CoordinateRect with the specified index must already exist in the
    *    canvas, or an IllegalArgumentException is thrown.
    */
   public void add(Drawable d, int coordRectIndex) {
      if (coordinateRects == null || coordRectIndex < 0 || coordRectIndex >= coordinateRects.size())
         throw new IllegalArgumentException("Internal programming error:  CoordinateRect index (" + coordRectIndex + ")out of range.");
      CoordinateRect c = ((CRData)coordinateRects.elementAt(coordRectIndex)).coords;
      c.add(d);
   }

   /**
    * Add the specified CoordinateRect to this DisplayCanvas, filling the entire canvas,
    * and with background color equal to the background color of the canvas.
    * 
    * @param c the CoordinateRect to be added.  If null, an IllegalArgumentException is thrown.
    */
   public void addCoordinateRect(CoordinateRect c) {
      addCoordinateRect(c,0,1,0,1,null);
   }

   /**
    * Add a CoordinateRect to the canvas, occupying a specified region of the canvas.
    *
    * @param coords The CoordinateRect to be added.  If this is null, an IllegalArgumentExceptionis thrown.
    * @param hmin Specifies the left edge of the CoordinateRect in the canvas, as a fraction of the size of the canvas.
    *             This must be in the range form 0 to 1, or an IllegalArgumentException is thrown.
    * @param hmax Specifies the right edge of the CoordinateRect in the canvas, as a fraction of the size of the canvas.
    *             This must be in the range form 0 to 1 and must be strictly greater than hmin, or an IllegalArgumentException is thrown.
    * @param vmin Specifies the top edge of the CoordinateRect in the canvas, as a fraction of the size of the canvas.
    *             This must be in the range form 0 to 1, or an IllegalArgumentException is thrown.
    * @param vmax Specifies the bottom edge of the CoordinateRect in the canvas, as a fraction of the size of the canvas.
    *             This must be in the range form 0 to 1 and must be strictly greater than vmin, or an IllegalArgumentException is thrown.
    * @param background The background color of the CoordinateRect.  The CoordinateRect is filled with this color
    *             before the Drawables that it contains are drawn.  If background is null, no filling takes place
    *             and the canvas shows through.
    */
   public void addCoordinateRect(CoordinateRect coords, double hmin, double hmax,
                                     double vmin, double vmax, Color background) {
      if (hmin < 0 || hmin > 1 || hmax < 0 || hmax > 1 || hmin >= hmax ||
               vmin < 0 || vmin > 1 || vmax < 0 || vmax > 1 || vmin >= vmax)
         throw new IllegalArgumentException("Illegal values for area covered by CoordinateRect.");
      if (coords == null)
         throw new IllegalArgumentException("Can't add null CoordinateRect to DisplayCanvas.");
      CRData c = new CRData();
      c.coords = coords;
      c.xmin = hmin;
      c.xmax = hmax;
      c.ymin = vmin;
      c.ymax = vmax;
      c.background = background;
      if (coordinateRects == null)
         coordinateRects = new Vector();
      coordinateRects.addElement(c);
      coords.setOwner(this);
   }
   
   /**
    * Add a newly created CoordinateRect covering the specified section of
    * the canvas.  hmin, hmax, vmin, vmax must be in the range 0 to 1.
    * The index of the new CoordinateRect is returned.
    */
   public int addNewCoordinateRect(double hmin, double hmax, double vmin, double vmax) {
      CoordinateRect c = new CoordinateRect();
      addCoordinateRect(c,hmin,hmax,vmin,vmax,null);   
      return coordinateRects.size() - 1;
   }
   
   /**
    * Add a newly created CoordinateRect covering the specified section of
    * the canvas, with the specfied background color.  hmin, hmax, vmin, vmax must be in the range 0 to 1.
    * The index of the new CoordinateRect is returned.
    */
   public int addNewCoordinateRect(double hmin, double hmax, double vmin, double vmax, Color background) {
      CoordinateRect c = new CoordinateRect();
      addCoordinateRect(c,hmin, hmax, vmin, vmax,background);   
      return coordinateRects.size() - 1;
   }
   
   /**
    * Get the first CoordinateRect in this canvas.  (If none exists, one is created and
    * added to the canvas.)
    */
   public CoordinateRect getCoordinateRect() {
      return getCoordinateRect(0);
   }

   /**
    * Get the i-th CoordinateRect in this DisplayCanvas.  They are numbered staring from zero.
    * If there is no i-th rect, null is returned, except that if there
    * are NO coordinate rects and a request for rect 0 is received,
    * then a new CoordinateRect is added to fill the entire canvas.
    */
   public CoordinateRect getCoordinateRect(int i) {
      if (i == 0 && (coordinateRects == null || coordinateRects.size() == 0))
         addNewCoordinateRect(0,1,0,1);
      if (coordinateRects == null || i < 0 || i >= coordinateRects.size())
         return null;
      else
         return ((CRData)coordinateRects.elementAt(i)).coords;
   }
    
   /**  
    * Return CoordinateRect that contains the specified pixel, or
    * null if there is none.  The CoordinateRects are searched in
    * reverse order, so that the "top" CoordinateRect at that point is
    * returned.  Note that this method only makes sense if the canvas
    * has already been displayed.
    * (Mostly, this is for internal use in this class.)
    */
   public CoordinateRect findCoordinateRectAt(int pixelX, int pixelY) {
      if (coordinateRects == null)
         return null;
      for (int i = coordinateRects.size() - 1; i >= 0; i--) {
         CRData c = (CRData)coordinateRects.elementAt(i);
         int width = getSize().width;
         if (width <= 0)
            return null;
         int height = getSize().height;
         int x = (int)(c.xmin*width);
         int y = (int)(c.ymin*height);
         int r = (int)(c.xmax*width);
         int b = (int)(c.ymax*height);
         if (pixelX >= x && pixelX < r && pixelY >= y && pixelY < b)
            return c.coords;
      }
      return null;
   }
    
   /**
    * Should be called whenever the contents of the canvas have changed and so
    * it needs to need to be redrawn.  (This causes the off-screen image to be redrawn.
    * A simple call to repaint() does not do this.)
    * If only one CoordinateRect needs to be repainted, you can call doRedraw(int i) or
    * or doRedraw(CoordinateRect c), which can be more efficient than redrawing the whole canvas.
    * If an error message is displayed, it will be cleared.
    */
   synchronized public void doRedraw() {
        // Should be called whenever the coordinate rects need to be repainted.
        // If only one needs to be repainted, you can call doRedraw(int i) or
        // or doRedraw(CoordinateRect c), which can be more efficient.
        // If an error message is displayed, this will not take effect until
        // the error message is cleared.
      OSCvalid = false;
      if (errorMessage != null)
         clearErrorMessage();  // does repaint
      else
         repaint();
   }
   
   /**
    * To be called when the contents of one of the CordinateRects have changed and so
    * it needs to need to be redrawn.  (This causes the off-screen image to be redrawn.
    * A simple call to repaint() does not do this.)
    * If an error message is displayed, it will be cleared.
    *
    * @param coordRectIndex The index of the CoordinateRect to be redrawn, where the first CoordinateRect is at index zero.
    *        If there is no such CoordinateRect, then nothing is done.
    */
   synchronized public void doRedraw(int coordRectIndex) {
      if (coordinateRects != null && coordRectIndex >= 0 && coordRectIndex < coordinateRects.size()) {
         CRData c = (CRData)coordinateRects.elementAt(coordRectIndex);
         OSCvalid = false;
         if (errorMessage != null) 
            clearErrorMessage();  // does repaint
         else {
            int width = getSize().width;
            int height = getSize().height;
            int x = (int)(c.xmin*width);
            int y = (int)(c.ymin*height);
            int w = (int)(c.xmax*width) - x;
            int h = (int)(c.ymax*height) - y;
            repaint(x,y,w,h);
         }
      }
   }
   
   /**
    * To be called when the contents of one of the CordinateRects have changed and so
    * it needs to need to be redrawn.  (This causes the off-screen image to be redrawn.
    * A simple call to repaint() does not do this.)
    * If an error message is displayed, it will be cleared.
    *
    * @param coords The CoordinateRect to be redrawn.  If coords is not in this DisplayCanvas, nothing is done.
    */
   synchronized public void doRedraw(CoordinateRect coords) {
      int size = (coordinateRects == null)? -1 : coordinateRects.size();
      for (int i = 0; i < size; i++)
         if (((CRData)coordinateRects.elementAt(i)).coords == coords) {
            doRedraw(i);
            break;
         }
   }
   

//------------------- InputObject/Computable interfaces ---------------------
   
   /**
    * This is generally called by a Controller.  It calls the checkInput() method of
    * any InputObject displayed on this Canvas.
    */
   synchronized public void checkInput() {
      if (coordinateRects != null) {
         int top = coordinateRects.size();
         for (int i = 0; i < top; i++)
            ((CRData)coordinateRects.elementAt(i)).coords.checkInput();
      }
   }
   
   /**
    * This is generally called by a Controller.  It calls the compute() method of
    * any InputObject displayed on this Canvas.
    */
   synchronized public void compute() {
      if (coordinateRects != null) {
         int top = coordinateRects.size();
         for (int i = 0; i < top; i++)
            ((CRData)coordinateRects.elementAt(i)).coords.compute();
      }
   }
   
   /**
    * Method required by InputObject interface; in this class, calls the same method
    * recursively on any CoordinateRects contained in this DisplayCanvas.  This is meant to 
    * be called by JCMPanel.gatherInputs().
    */
    public void notifyControllerOnChange(Controller c) {
      if (coordinateRects != null) {
         int top = coordinateRects.size();
         for (int i = 0; i < top; i++)
            ((CRData)coordinateRects.elementAt(i)).coords.notifyControllerOnChange(c);
      }
    }

//-------------------- Error Reporter Stuff ------------------------------------

   /** 
    * Get color that is used as a background when the canvas displays an error message.
    */
   public Color getErrorBackground() { 
      return errorBackground; 
   }
 
   /** 
    * Set color to be used as a background when the canvas displays an error message.
    * The default is a light green.  If the specified Color value is null, nothing is done.
    */
   public void setErrorBackground(Color c) { 
      if (c != null)
         errorBackground = c; 
   }
   
   /** 
    * Get color that is used for the text when the canvas displays an error message.
    */
   public Color getErrorForeground() { 
     return errorForeground; 
   }
   
   /** 
    * Set color to be used for the text when the canvas displays an error message.
    * The default is a dark green.  If the specified Color value is null, nothing is done.
    */
   public void setErrorForeground(Color c) { 
      if (c != null)
         errorForeground = c; 
   }

   /**
    * Get the error message that is currently displayed on the canvas.  If no error
    * is displyed, the return value is null.
    */
   synchronized public String getErrorMessage() { 
      return errorMessage;
   }

   /**
    * Set an error message to be displayed on the canvas.  This method is generally called by
    * a Controller or a LimitControlPanel.  If you call it directly, use null as the first parameter.
    * 
    * @param c The Controller, if any, that is calling this routine.  This controller will be notified
    *             when the error message is cleared.  If the method is not being called by a contrller, this
    *             parameter should be set to null.
    * @param message The error message to be displayed.  If the value is null or is a blank string, 
    *                the current message, if any, is cleared.
    */
   synchronized public void setErrorMessage(Controller c, String message) { 
      if (message == null || message.trim().length() == 0) {
         if (errorMessage != null) {
               clearErrorMessage();
            if (errorSource != c)
               errorSource.errorCleared();
            repaint();
         }
      }
      else {
         errorMessage = message.trim();
         errorSource = c;
         OSCvalid = false;
         repaint();
      }
   }

   /**
    * Clear the error message, if any, that is currently displayed on the canvas.
    */
   synchronized public void clearErrorMessage() {
      if (errorMessage == null)
         return;
      errorMessage = null;
      if (errorSource != null)
         errorSource.errorCleared();
      errorSource = null;
      repaint();
   }

   // ---------------------- ErrorReporter Implementation ----------------------------

   private Color errorBackground = new Color(220,255,220);
   private Color errorForeground = new Color(0,120,0);
   private String errorMessage;
   private Controller errorSource;
   private Draggable dragged;  // The draggable object, if any, that is being dragged.

   /**
    * This has been overridden to handle the mouse zoom feature.
    * Not meant to be called directly.
    */
   public void processMouseEvent(MouseEvent evt) {
         // If an error message is displayed, get rid of it and
         // ignore the mouse click.
      if (evt.getID() == MouseEvent.MOUSE_PRESSED) {
         dragging = false;  // Shouldn't be possible, but some old, buggy versions of Java made it so.
         dragged = null;
         if (errorMessage != null) {
            if (errorSource != null)
               errorSource.errorCleared();
            errorSource = null;
            errorMessage = null;
            repaint();
            evt.consume();
            return;
         }
         CoordinateRect c = findCoords(evt);
         if (c != null)
            dragged = c.checkDraggables(evt);
         if (dragged != null)
            return;
         if (handleMouseZooms && !(evt.getClickCount() > 1 || evt.isAltDown() || evt.isMetaDown() || evt.isControlDown())) {
            super.processMouseEvent(evt);
            if (!evt.isConsumed())
               doMouseZoom_pressed(evt);
            return;
         }
      }
      else if (evt.getID() == MouseEvent.MOUSE_RELEASED && handleMouseZooms && dragged != null) {
         dragged.finishDrag(evt);
         dragged = null;
         return;
      }
      else if (evt.getID() == MouseEvent.MOUSE_RELEASED && handleMouseZooms && dragging) {
         doMouseZoom_released(evt);
         return;
      }
      super.processMouseEvent(evt);
   }
   
   /**
    * This has been overridden to handle the mouse zoom feature.
    * Not meant to be called directly.
    */
   public void processMouseMotionEvent(MouseEvent evt) {
      if (dragged != null && evt.getID() == MouseEvent.MOUSE_DRAGGED)
         dragged.continueDrag(evt);
      else if (dragging && evt.getID() == MouseEvent.MOUSE_DRAGGED)
         doMouseZoom_moved(evt);
      else
         super.processMouseMotionEvent(evt);
   }

   private void drawErrorMessage(Graphics g) {
      if (errorMessage == null)
         return;

      Font font = new Font("Helvetica",Font.BOLD,12);
      FontMetrics fm = g.getFontMetrics(font);
      
      int width = getSize().width;
      int height = getSize().height;
      int lineHeight = fm.getHeight();
      int leading = fm.getLeading();
      int messageWidth = width - 80;
      int left = 30;
      int maxLines = (height - 60 - lineHeight) / lineHeight;
      if (maxLines <= 0)
         maxLines = 1;
            
      StringTokenizer t = new StringTokenizer(errorMessage, " \t\r\n");
      int lineCt = 0;
      String[] errorMessageList = new String[maxLines];
      String line = "   ";  // indent first line
      while (t.hasMoreTokens())  {
          String word = t.nextToken();
          if (fm.stringWidth(word) > messageWidth) {
              String w = "";
              int dots = fm.stringWidth("...");
              for (int c = 0; c < word.length(); c++) {
                 w += word.charAt(c);
                 if (fm.stringWidth(w) + dots > messageWidth)
                    break;
              }
              word = w;
          }
          String linePlusWord = line + " " + word;
          if (fm.stringWidth(linePlusWord) > messageWidth) {
             errorMessageList[lineCt] = line;
             lineCt++;
             if (lineCt == maxLines)
                break;
             line = word;
          }
          else {
             line = linePlusWord;
          }
      }
      if (lineCt < maxLines) {
         errorMessageList[lineCt] = line;
         lineCt++;
      }
      if (lineCt == 1)
         errorMessageList[0] += "    ";  // for proper centering
      
      int boxWidth = width - 60;
      int boxHeight = (lineCt+1)*lineHeight + 50;
      int top = height/2 - boxHeight/2;
      if (top < 0)
         top = 0;
      
      g.setColor(getBackground());
      g.fillRect(0,0,width,height);
      g.setColor(errorBackground);
      g.fillRect(left,top,boxWidth,boxHeight);
      g.setColor(errorForeground);
      g.drawRect(left,top,boxWidth,boxHeight);
      g.drawRect(left+1,top+1,boxWidth-2,boxHeight-2);
      g.drawLine(left,top + 23 + lineHeight, left + boxWidth - 2, top + 23 + lineHeight); 
      g.drawLine(left,top + 24 + lineHeight, left + boxWidth - 2, top + 24 + lineHeight);
      g.setFont(font);
      g.drawString("ERROR MESSAGE",width/2 - fm.stringWidth("(Error Message)")/2, top + 10 + lineHeight); 
      if (lineCt == 1)
         g.drawString(errorMessageList[0], width/2 - fm.stringWidth(errorMessageList[0])/2, top + 35 + 2*lineHeight);
      else {
         for (int i = 0; i < lineCt; i++) {
            g.drawString(errorMessageList[i], left + 10, top + 35 + (i+2)*lineHeight - leading);
         }
      }
   }  // end drawErrorMessage();
   

   // ------------ Handle Mouse Zooming -----------------------------------------
   
   private transient boolean dragging, draggingZoomWindow;
   private transient CRData draggingInRect;
   private transient int dragXmax, dragXmin, dragYmax, dragYmin;
   private transient int lastX, lastY, startX, startY;
   
   private CoordinateRect findCoords(MouseEvent evt) {
          // Find coord rect containing the mouse.
      int xMouse = evt.getX();
      int yMouse = evt.getY();
      int size = (coordinateRects == null)? -1 : coordinateRects.size();
      int width = getSize().width;
      int height = getSize().height;
      for (int i = size-1; i >= 0; i--) {
         CRData c = (CRData)coordinateRects.elementAt(i);
         double xmin = (int)(c.xmin*width);
         double ymin = (int)(c.ymin*height);
         double xmax = (int)(c.xmax*width) - 1;
         double ymax = (int)(c.ymax*height) - 1;
         if (xMouse >= xmin && xMouse <= xmax && yMouse >= ymin && yMouse <= ymax)
            return c.coords;
      }
      return null;
   }
   
   private synchronized void doMouseZoom_pressed(MouseEvent evt) {
         // Called from processMouseEvent, above.
         // Ignore multiple clicks and clicks with other than button 1
         // and clicks modified with any key except shift.
      if (evt.getClickCount() > 1 || evt.isAltDown() || evt.isMetaDown() || evt.isControlDown())
         return;
      int xMouse = evt.getX();
      int yMouse = evt.getY();
      int size = (coordinateRects == null)? -1 : coordinateRects.size();
      int width = getSize().width;
      int height = getSize().height;
      for (int i = size-1; i >= 0; i--) {
         CRData c = (CRData)coordinateRects.elementAt(i);
         dragXmin = (int)(c.xmin*width);
         dragYmin = (int)(c.ymin*height);
         dragXmax = (int)(c.xmax*width) - 1;
         dragYmax = (int)(c.ymax*height) - 1;
         if (xMouse >= dragXmin && xMouse <= dragXmax && yMouse >= dragYmin && yMouse <= dragYmax) {
            dragging = true;
            draggingZoomWindow = false;
            draggingInRect = c;
            startX = xMouse;
            startY = yMouse;
            lastX = xMouse;
            lastY = yMouse;
            break;
         } 
      }
   }
   
   private synchronized void doMouseZoom_released(MouseEvent evt) {
      Graphics g = getGraphics();
      putDragRect(g);
      g.dispose();
      CoordinateRect c = draggingInRect.coords;
      if ( (Math.abs(lastX - startX) < 4 && Math.abs(lastY - startY) < 4)
                || Math.abs(startX - lastX) < 2 || Math.abs(startY - lastY) < 2 ) {
         if (draggingZoomWindow)
             return;
         if (evt.isShiftDown())
             c.zoomOutFromPixel(startX,startY);
         else
             c.zoomInOnPixel(startX,startY);
      }
      else {
         c.setLimits( c.pixelToX(startX), c.pixelToX(lastX), c.pixelToY(startY), c.pixelToY(lastY) );
      }
      dragging = false;
   }
   
   private synchronized void doMouseZoom_moved(MouseEvent evt) {
      Graphics g = getGraphics();
      putDragRect(g);
      lastX = evt.getX();
      lastY = evt.getY();
      putDragRect(g);
      g.dispose();
   }
   
   private void putDragRect(Graphics g) {  // (Assume dragging = true)
      if (lastX < dragXmin)
         lastX = dragXmin;
      if (lastX > dragXmax)
         lastX = dragXmax;
      if (lastY < dragYmin)
         lastY = dragYmin;
      if (lastY > dragYmax)
         lastY = dragYmax;
      if ( (Math.abs(startX - lastX) < 4 && Math.abs(startY - lastY) < 4)
                || Math.abs(startX - lastX) < 2 || Math.abs(startY - lastY) < 2 )
         return;  
      draggingZoomWindow = true;
       Color bc = draggingInRect.background;
       if (bc == null)
          bc = getBackground();
       g.setXORMode(bc);
       if (bc.getRed() <= 100 && bc.getGreen() <= 100 && bc.getBlue() <= 150)
          g.setColor(Color.white);
       else
          g.setColor(Color.black);
       int x, y, w, h;
       if (startX < lastX) {
          x = startX;
          w = lastX - startX;
       }
       else {
          x = lastX;
          w = startX - lastX;
       }
       if (startY < lastY) {
          y = startY;
          h = lastY - startY;
       }
       else {
          y = lastY;
          h = startY - lastY;
       }
       g.drawRect(x,y,w,h);
       g.setPaintMode();
   }
   
   //--------- More implementation details... ----------------------------------
   
   /**
    * This has been overridden to return a default size of 350-by-350 pixels.
    * Not usually called directly.
    */
   public Dimension getPreferredSize() {
      return new Dimension(350,350);
   }

   private transient Image OSC;
   private transient Graphics OSG;
   private transient boolean OSCvalid;
   private transient int OSCwidth = -1, OSCheight = -1;
      
   private void drawCoordinateRects(Graphics g, int width, int height, Rectangle clip) {
      g.setColor(getBackground());
      g.fillRect(0,0,width,height);
      int count = (coordinateRects == null)? -1 : coordinateRects.size();
      try {
         for (int i = 0; i < count; i++) {
            CRData c = (CRData)coordinateRects.elementAt(i);
            Rectangle bounds = new Rectangle();
            bounds.x = (int)(c.xmin*width);
            bounds.y = (int)(c.ymin*height);
            bounds.width = (int)(c.xmax*width) - bounds.x;
            bounds.height =  (int)(c.ymax*height) - bounds.y;
            Rectangle clipThisRect = (clip == null)? bounds : bounds.intersection(clip);
            if (clip == null || !clipThisRect.isEmpty()) {
               g.setClip(clipThisRect);
               if (c.background != null) {
                  g.setColor(c.background);
                  g.fillRect(bounds.x,bounds.y,bounds.width,bounds.height);
               }
               c.coords.draw(g,bounds.x,bounds.y,bounds.width,bounds.height);
            }
         }
      }
      finally {
         g.setClip(clip);
      }
   }
   
   /**
    * This has been overridden to implemnt double-buffering.
    * Not meant to be called directly.
    */
   public void update(Graphics g) {
      paint(g);
   }

   /**
    * Draw the contents of the DisplayCanvas.
    * Not usually called directly.
    */
   synchronized public void paint(Graphics g) {
      if (errorMessage == null) {
         try {
            checkOSC();
            if (OSC != null)
               g.drawImage(OSC,0,0,this);
            else 
               drawCoordinateRects(g,getSize().width,getSize().height,g.getClipBounds());
            if (dragging)
               putDragRect(g);
         }
         catch (RuntimeException e) {
            errorMessage = "Internal Error? (stack trace on System.out):  " + e.toString();
            e.printStackTrace();
            g.setClip(0,0,getSize().width,getSize().height);
         }
      }
      if (errorMessage != null) {
         drawErrorMessage(g);
         OSCvalid = false;
         return;
      }
   }
   
   /**
    * Draws the specified item in the first CoordinateRect in this canvas.
    * It is drawn on screen and on the off-screen canvas, if there is one.
    * However, no information is kept about this item, so the drawing will
    * disappear the next time the off-screen canvas is re-drawn (if there
    * is an off-screen canvas) or the next time the canvas is repainted
    * (is there is no off-screen canvas).  If the canvas contains no
    * CoordinateRect when this is called, a new one is added.  Note that
    * this method should only be called after the canvas has appeared on 
    * the screen.
    */
   public void drawTemp(DrawTemp drawItem) {
      if ( coordinateRects == null || coordinateRects.size() == 0)
         addCoordinateRect(new CoordinateRect());
      drawTemp(drawItem,0);
   }
   
   /**
    * Draws the specified item in the specified CoordinateRect in this canvas.
    * It is drawn on screen and on the off-screen canvas, if there is one.
    * However, no information is kept about this item, so the drawing will
    * disappear the next time the off-screen canvas is re-drawn (if there
    * is an off-screen canvas) or the next time the canvas is repainted
    * (is there is no off-screen canvas).   Note that
    * this method should only be called after the canvas has appeared on 
    * the screen.
    *
    * @param drawItem The non-null object that is to be drawn
    * @param coordRectIndex The index of the CoordinateRect in which it 
    *        is to be drawn, where the index of the fist CoordinateRect
    *        added to the canvas is zero, and so on.  If there is
    *        no CoordinateRect with the specified index, an IllegalArgumentException
    *        is thrown.
    */
   synchronized public void drawTemp(DrawTemp drawItem, int coordRectIndex) {
      if (coordRectIndex < 0 || coordRectIndex >= coordinateRects.size())
         throw new IllegalArgumentException("Invalid CoordinateRect index, " + coordRectIndex);
      Graphics g = getGraphics();
      if (g == null)
         return;
      CRData c = (CRData)coordinateRects.elementAt(coordRectIndex);
      Rectangle bounds = new Rectangle();
      bounds.x = (int)(c.xmin*getSize().width);
      bounds.y = (int)(c.ymin*getSize().height);
      bounds.width = (int)(c.xmax*getSize().width) - bounds.x;
      bounds.height =  (int)(c.ymax*getSize().height) - bounds.y;
      g.setClip(bounds);
      drawItem.draw(g,c.coords);
      g.dispose();
      if (useOffscreenCanvas && OSCvalid && OSC != null) {
         g = OSC.getGraphics();
         g.setClip(bounds);
         drawItem.draw(g,c.coords);
         g.dispose();
      }
   }
   
   
   synchronized private void checkOSC() {  // make off-screen image, if necessary
      if (!useOffscreenCanvas || (OSCvalid == true && OSC != null && 
                                    OSCwidth == getSize().width && OSCheight == getSize().height))
         return;
      int width = getSize().width;
      int height = getSize().height;
      if (OSC == null || width != OSCwidth || height != OSCheight) {
         OSCvalid = false;
         OSCwidth = width;
         OSCheight = height;
         try {
            OSC = createImage(OSCwidth,OSCheight);
            OSG = OSC.getGraphics();
         }
         catch (OutOfMemoryError e) {
            OSC = null;
            OSG = null;
         }
      }
      if (OSC == null || OSCvalid)
         return;
      OSCvalid = true;
      OSG.setClip(0,0,width,height);
      drawCoordinateRects(OSG,width,height,null);
   }


} // end class DisplayCanvas

