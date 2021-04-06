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
import java.util.Vector;
import java.awt.event.*;

import edu.awt.*;
import edu.awt.*;
import edu.data.*;

/**
 * A LimitControlPanel has four input boxes for specifying the xmin, xmax, ymin, and ymax 
 * of a CoodinateRect.  You can actually add more than one CoordinteRect to the LimitControlPanel.
 * This will synchronize the coordinate systems on the all the CoordinateRects that is controlls.
 * 
 * <p>A LimitControlPanel can also contain a number of standard buttons, such as buttons for
 * zooming the coordinates in and out.  The buttons are specfied using constants
 * defined in this class.  It is possible to obtain standard buttons so that they can
 * be displayed outside the LimitControlPanel.  Furthermore, it is also possible to add other components
 * to the panel, using the addRange(), addComponent(), and addComponentPair() methods.
 * (The standard add() method from the Component class is overridded to call
 * addComponent().) Any VariableInput added to the LimitControl Panel will appear with its name
 * as a label, just above the input box.
 * 
 * <p>Ordinarily, all the components are just stacked up vertically.  However, if
 * you set the useTwoColumnsIfPossible property to true, then they will be in two columns, unless
 * the width of the Panel is too small for two columns.  Pairs of items added
 * with addRange() or addComponentPair() will appear on the same row.  An item
 * added with addComponent() will appear on a row by itself.  As for the standard
 * buttons, the following pairs will appear together, IF they are added at the
 * same time:  SET_LIMITS and EQUALUIZE; ZOOM_IN and ZOOM_OUT; SAVE and RESTORE.
 *
 * <p>A LimitControlPanel can have an error reporter, which is used to report
 * any errors that are found in the input boxes for xmin, xmax, ymin, ymax
 * (or other VariableInputs added with addRange()).  Except for these
 * input boxes, other coponents are NOT checked for errors. 
 */

public class LimitControlPanel extends Panel implements InputObject, Tieable, Limits, ActionListener {

     
            
            /**
             * A constant that can be used in the addButton() method to add a button to the LimitControlPanel.
             * This represents a button that will set the limits using the values in the input boxes.
             * (This is also done when the user presses return in one of the boxes.)
             */
  public final static int  SET_LIMITS = 1;      
            
            /**
             * A constant that can be used in the addButton() method to add a button to the LimitControlPanel.
             * This represents a button that will equalize the scales on the axes (of the first
             * CoordinateRect that was added to this panel).
             */
  public final static int  EQUALIZE = 2;        
            
            /**
             * A constant that can be used in the addButton() method to add a button to the LimitControlPanel.
             * This represents a button that will zoom in on the center of the coordinate rect.
             */
  public final static int  ZOOM_IN = 4;    

            /**
             * A constant that can be used in the addButton() method to add a button to the LimitControlPanel.
             * This represents a button that will zoom out from the center of the coordinate rect.
             */
  public final static int  ZOOM_OUT = 8;   
            
            /**
             * A constant that can be used in the addButton() method to add a button to the LimitControlPanel.
             * This represents a button that will save the current limits, so they can be
             * restored later with the restore button.
             */
  public final static int  SAVE = 16;            
            
            /**
             * A constant that can be used in the addButton() method to add a button to the LimitControlPanel.
             * This represents a button that will restore previously saved coordinates.
             * The coords are those that were saved with the save button,
             * or if none were saved in that way, then the original
             * coordinates that the CoordinateRect had when it was created.
             */
  public final static int  RESTORE = 32;  
               
   /**
    * A constant that can be used in the addButton() method to add all possible buttons to the LimitControlPanel.
    */        
   public final static int ALL_BUTTONS = 0x3F;  

            
   private final static String[] buttonNames = {
             "Set Limits",
             "Equalize Axes",
             "Zoom In",
             "Zoom Out",
             "Save Limits",
             "Restore Limits"
           };
            
   /**
    * Set of installed buttons.
    */
   protected int buttons;      
   
   /**
    * Use two columns for display, if possible.
    */
   protected boolean twoColumn; 

   /** 
    * The input boxes for the x- and y-value ranges.
    */
   protected VariableInput xmin, xmax, ymin, ymax;

   /**
    * This is increased when the user changes the limits.
    * (The -1 will make this LimitControlPanel get its limits
    * from the first CoordinateRect that is added to it.)
    * This variable is used to implement syncronization of limits
    * with the limits on CoordinateRects.
    */
   protected long serialNumber = -1;  
   
   /**
    * A Tie holding this panel and the CoordinateRects that it controls.
    */
   protected Tie syncWith;

   /**
    * For reporting errors in user input.
    */
   protected ErrorReporter errorReporter;  
   
   /**
    * The first CoordinateRect tied to this LimitControlPanel.
    */
   protected CoordinateRect coords;  

   /**
    * Vector of components and component pairs that have
    * been added to this panel, including at least the xmin, xmax, ymin, ymax
    * input boxes.
    */
   protected Vector items = new Vector();      


   /**
    * Create a LimitControlPanel containing input boxes labeled
    * "xmin", "xmax", "ymin", "ymax" and a SET_LIMITS button.  The
    * components are shown in a single column.
    */
   public LimitControlPanel() {
      this("xmin","xmax","ymin","ymax",SET_LIMITS, false);
   }
   
   /**
    * Create a LimitControlPanel containing input boxes labeled
    * "xmin", "xmax", "ymin", "ymax" and whatever buttons are in the
    * set specified by the first parameter.
    * 
    * @param buttonsToAdd The set of buttons to be added to the panel.  Can consist of one or
    *         more of the constants SET_LIMITS, EQUALIZE, ZOOM_IN, ZOOM_OUT, SAVE, and RESTORE,
    *         or'ed together.
    * @param useTwoColumnsIfPossible If this is true, then the components in the panel will
    *        be arranged into two columns instead of one (assuming that there is room).
    */
   public LimitControlPanel(int buttonsToAdd, boolean useTwoColumnsIfPossible) {
      this("xmin","xmax","ymin","ymax",buttonsToAdd,useTwoColumnsIfPossible);
   }
   
   /**
    * Create a LimitControlPanel containing input boxes labeled 
    * with the given names and containing whatever buttons are in the
    * set buttonsToAdd.  buttonsToAdd should be formed by or-ing together
    * constants such as SET_LIMITS from this class.  The last parameter
    * specifies whether to show the components in two columns.
    * @param xminName Name to be used as a label for the xmin input box.
    * @param xmaxName Name to be used as a label for the xmax input box.
    * @param yminName Name to be used as a label for the ymin input box.
    * @param xmaxName Name to be used as a label for the ymax input box.
    *
    * @param buttonsToAdd The set of buttons to be added to the panel.  Can consist of one or
    *         more of the constants SET_LIMITS, EQUALIZE, ZOOM_IN, ZOOM_OUT, SAVE, and RESTORE,
    *         or'ed together.
    * @param useTwoColumnsIfPossible If this is true, then the components in the panel will
    *        be arranged into two columns instead of one (assuming that there is room).
    */
   public LimitControlPanel(String xminName, String xmaxName, String yminName, 
                                String ymaxName, int buttonsToAdd, boolean useTwoColumnsIfPossible) {
      setLayout(null);
      enableEvents(AWTEvent.COMPONENT_EVENT_MASK);
      xmin = new VariableInput(xminName,"-5");
      xmax = new VariableInput(xmaxName,"5");
      addRange(xmin,xmax);
      ymin = new VariableInput(yminName,"-5");
      ymax = new VariableInput(ymaxName,"5");
      addRange(ymin,ymax);
      addButtons(buttonsToAdd);
      twoColumn = useTwoColumnsIfPossible;
   }
   
   /**
    * Add a CoordinateRect to be controlled by this LimitControlPanel.  When the user changes
    * the limits in this LimitControlPanel, the limits are also changed on the CoordinateRect
    * to match.  If the limits on the CoordinateRect change for some other reason, then
    * the limits in the panel are changed to match.  If multiple CoordinateRects are added,
    * the limits on all the CoordinateRects will be synchronized with each other and with
    * the limits in the panel.
    */
   public void addCoords(CoordinateRect coords) {
      if (syncWith == null)
         syncWith = new Tie(this);
      syncWith.add(coords);
      coords.setSyncWith(syncWith);
      if (this.coords == null)
          this.coords = coords;
   }
   
   /**
    * Add the first CoordinateRect from the canvas to be controlled
    * by this LimitControlPanel.  (Just calls addCoords(canvas.getCoordinateRect()).)
    */
   public void addCoords(DisplayCanvas canvas) {
      addCoords(canvas.getCoordinateRect());
   }
   
   /**
    * Set the ErrorReporter that is used to report errors in the
    * user's input.  Note that only the input boxes for
    * xmin, xmax, ymin, and ymax and any VariableInputs
    * added with the addRange() method are checked.
    *
    */
   public void setErrorReporter(ErrorReporter rep) {
      errorReporter = rep;
   }
   
   /**
    * Get the ErrorReporter that is used to report errors in the
    * user's input.  Note that only the input boxes for
    * xmin, xmax, ymin, and ymax and any VariableInputs
    * added with the addRange() method are checked.
    *
    */
   public ErrorReporter getErrorReporter() {
      return errorReporter;
   }
   
   /**
    * Set to true if you want the components to be shown in
    * two columns (provided the panel is wide enough).
    *
    */
   public void setUseTwoColumnsIfPossible(boolean two) {
      twoColumn = two;
   }
   
   /**
    * Get the value of the "useTwoColumnsIfPossible" property of this panel.
    */
   public boolean getUseTwoColumnsIfPossible() {
      return twoColumn;
   }
   
   /**
    * Add a component to the panel.  If two-column format is used, it will
    * be shown on a line by itself.  Note that the component shouldn't be too
    * wide, or it will make the Panel stretch.  This component
    * is NOT checked for input errors.  If it is an input object
    * or a computable, it should be added to a Controller.  For an
    * input object, some Controller should be set up to be notified when
    * the value changes. (You have to do this by hand, even if you use JCMPanels!!)
    */
   public void addComponent(Component c) {
      super.add(c);
      items.addElement(c);
   }
   
   
   /**
    * Add two components to the panel.  If two-column format is used, they will
    * be shown on the same row.  Note that the components shouldn't be too
    * wide, or they will make the Panel stretch.  These components
    * are NOT checked for input errors.  If they are input objects
    * or computables, they  should be added to another Controller. For an
    * input object, some Controller should be set up to be notified when
    * the value changes. (You have to do this by hand, even if you use JCMPanels!!)
    */
   public void addComponentPair(Component c1, Component c2) {
      super.add(c1);
      super.add(c2);
      items.addElement( new Component[] { c1, c2 } );
   }
   
   
   /**
    * Add two VariableInputs to the panel.  These ARE checked for input when
    * the user presses return  or clicks the SET_LIMITS button.
    * Furthermore, it is checked that the value in the second input box is greater than
    * the value in the first, and an error is reported if it is not.
    * This method is used to add the xmin, xmax, ymin, and ymax
    * boxes.  It could possibly be used to add tmin and tmax boxes
    * for the limits on the parameter of a parametric curve,
    * for example.
    */
   public void addRange(final VariableInput v1, final VariableInput v2) {
      super.add(v1);
      super.add(v2);
      v1.addActionListener(this);
      v2.addActionListener(this);
      items.addElement( new Component[] { v1, v2, null } );
   }
   
   /**
    * Add the buttons in buttonSet to the panel, if they are not
    * already there.  buttonSet should be formed by or-ing 
    * together some of the constants SET_LIMITS, ZOOM_IN, etc.
    */
   public void addButtons(int buttonSet) {
      if ( (buttonSet & SET_LIMITS) != 0 && (buttons & SET_LIMITS) == 0
                    && (buttonSet & EQUALIZE) != 0 && (buttons & EQUALIZE) == 0 )
         addComponentPair( makeButton(0), makeButton(1) );
      else if ( (buttonSet & SET_LIMITS) != 0 && (buttons & SET_LIMITS) == 0 )
         addComponent( makeButton(0) );
      else if ( (buttonSet & EQUALIZE) != 0 && (buttons & EQUALIZE) == 0 )
         addComponent( makeButton(1) );
         
      if ( (buttonSet & ZOOM_IN) != 0 && (buttons & ZOOM_IN) == 0
                    && (buttonSet & ZOOM_OUT) != 0 && (buttons & ZOOM_OUT) == 0 )
         addComponentPair( makeButton(2), makeButton(3) );
      else if ( (buttonSet & ZOOM_IN) != 0 && (buttons & ZOOM_IN) == 0 )
         addComponent( makeButton(2) );
      else if ( (buttonSet & ZOOM_OUT) != 0 && (buttons & ZOOM_OUT) == 0 )
         addComponent( makeButton(3) );
              
      if ( (buttonSet & SAVE) != 0 && (buttons & SAVE) == 0
                    && (buttonSet & RESTORE) != 0 && (buttons & RESTORE) == 0 )
         addComponentPair( makeButton(4), makeButton(5) );
      else if ( (buttonSet & SAVE) != 0 && (buttons & SAVE) == 0 )
         addComponent( makeButton(4) );
      else if ( (buttonSet & RESTORE) != 0 && (buttons & RESTORE) == 0 )
         addComponent( makeButton(5) );
              
      buttons = buttons | buttonSet;
   }
   
   
   /**
    *  Get a Button corresponding to one of the six button types defined by the constants
    *  SET_LIMITS, EQUALIZE, ZOOM_IN, ZOOM_OUT, SAVE, and RESTORE in this class.  The button
    *  can be added to a different panel, but it will still affect this LimitControlPanel in
    *  the usual way.  It is possible to change the name of the button, and it will still
    *  work correctly.  Each call to this method creates a new button, even if multiple buttons
    *  of the same type are created.
    *
    *  @param buttonCode one of the constants from this class (SET_LIMITS, EQUALIZE, etc.) specifying
    *          one of the types of button for controlling limits.  If the parameter is not one of
    *          these constants, and IllegalArgumentException will be thrown.
    */
   public Button getButton(int buttonCode) {
      int buttonNum;
      if (buttonCode == SET_LIMITS)
         buttonNum = 0;
      else if (buttonCode == EQUALIZE)
         buttonNum = 1;
      else if (buttonCode == ZOOM_IN)
         buttonNum = 2;
      else if (buttonCode == ZOOM_OUT)
         buttonNum = 3;
      else if (buttonCode == SAVE)
         buttonNum = 4;
      else if (buttonCode == RESTORE)
         buttonNum = 5;
      else
         throw new IllegalArgumentException("Unknown button code passed to getButton().");
      Button b = makeButton(buttonNum);
      b.setActionCommand(buttonNames[buttonNum]);  // So command won't change if button name is changed.
      return b;
   }
   
   // ---------------- Implementation details ------------------------------------------
   
   private Button makeButton(int i) {
         // Make one of the limit control buttons.  Parameter is an index into the buttonNames 
         // array, not one of the constants SET_LIMITS, EQUALIZE, etc.
      Button b = new Button(buttonNames[i]);
      b.setBackground(Color.lightGray);
      b.addActionListener(this);
      return b;
   }
   
   /**
    * Redefine this method from the Component class to make it a synonym for addComponent(c);
    */
   public Component add(Component c) {
         // Redefine this, in case someone uses it.
      addComponent(c);
      return c;
   }
   
   /**
    *  Method required by CheckInput interface.  In this class, it does nothing because
    *  responses to inputs are handled by the LimitControlPanel itself.
    */
   public void notifyControllerOnChange(Controller c) {
   }
   
   /**
    * Check the input boxes in this panel.  This is generally not meant to be
    * called from outside this class, except by a Controller.
    */
   public void checkInput() {
      try {
         boolean changed = false;
         for (int i = 0; i < items.size(); i++) {
             Object obj = items.elementAt(i);
             if (obj instanceof Component[] && ((Component[])obj).length == 3) {
                VariableInput v1 = (VariableInput)((Component[])obj)[0];
                VariableInput v2 = (VariableInput)((Component[])obj)[1];
                double x, x1, x2;
                x = v1.getVal();
                v1.checkInput();
                x1 = v1.getVal();
                if (x != x1)
                   changed = true;
                x = v2.getVal();
                v2.checkInput();
                x2 = v2.getVal();
                if (x != x2)
                   changed = true;
                if (x1 >= x2)
                   throw new JCMError("The value of " + v2.getName() + " must be greater than the value of " + 
                                             v1.getName() + ".", v2);
             }
         }
         if (errorReporter != null)
            errorReporter.clearErrorMessage();
         if (changed) {
            serialNumber++;
            if (syncWith != null)
               syncWith.check();
         }
      }
      catch (JCMError e) {
         if (errorReporter != null)
            errorReporter.setErrorMessage(null,e.getMessage());
         else
            System.out.println("***** Error:  " + e.getMessage());
         if (e.object instanceof TextField) {
            ((TextField)e.object).selectAll();
            ((TextField)e.object).requestFocus();
         }
      }
      catch(RuntimeException e) {
         if (errorReporter != null)
            errorReporter.setErrorMessage(null,e.toString());
         e.printStackTrace();
      }
   }

   /**
    * Part of the Tieable interface, and not meant to be called directly.
    */
   public long getSerialNumber() {
      return serialNumber;
   }
   
   /**
    * Part of the Tieable interface, and not meant to be called directly.
    */
   public void sync(Tie t, Tieable newest) {
      if (newest == this)
         return;
      if ( ! (newest instanceof Tieable) )
         throw new IllegalArgumentException("Internal Error:  A LimitControlPanel can only sync with a Limits object.");
      setLimits(((Limits)newest).getLimits());
      serialNumber = newest.getSerialNumber();
   }
   
   /**
    * Get the values in the xmin, xmax, ymin, and ymax input boxes.  Note that this can
    * throw a JCMError.
    */
   public double[] getLimits() {
      double[] limits = new double[4];
      limits[0] = xmin.getVal();
      limits[1] = xmax.getVal();
      limits[2] = ymin.getVal();
      limits[3] = ymax.getVal();
      return limits;
   }
   
   /**
    * Set the values in the xmin, xmax, ymin, and ymax input boxes.
    */
   public void setLimits(double[] limits) {
      if (limits == null || limits.length < 4)
         throw new IllegalArgumentException("Internal Error:  Not enough values supplied for setLimits.");
      for (int i = 0; i < 4; i++)
         if (Double.isNaN(limits[i]) || Double.isInfinite(limits[i]))
            return;
      boolean changed = false;
      if (limits[0] != xmin.getVal()) {
         changed = true;
         xmin.setVal(limits[0]);
      }
      if (limits[1] != xmax.getVal()) {
         changed = true;
         xmax.setVal(limits[1]);
      }
      if (limits[2] != ymin.getVal()) {
         changed = true;
         ymin.setVal(limits[2]);
      }
      if (limits[3] != ymax.getVal()) {
         changed = true;
         ymax.setVal(limits[3]);
      }
      if (changed)
         serialNumber++;
   }
   
   /**
    * Handle a click on one of the standard buttons.
    * Not meant to be called directly.
    */
   public void actionPerformed(ActionEvent evt) {
      String cmd = evt.getActionCommand();
      if (evt.getSource() instanceof VariableInput || cmd.equals(buttonNames[0]))
         checkInput();
      else if (coords == null)
         return;
      else if (cmd.equals(buttonNames[1]))
         coords.equalizeAxes();
      else if (cmd.equals(buttonNames[2]))
         coords.zoomIn();
      else if (cmd.equals(buttonNames[3]))
         coords.zoomOut();
      else if (cmd.equals(buttonNames[4]))
         coords.setRestoreBuffer();
      else if (cmd.equals(buttonNames[5]))
         coords.restore();
   }
   
   /**
    * Draw the input box labels.
    * Not meant to be called directly.
    */
   public void paint(Graphics g) {
      int n = getComponentCount();
      for (int i = 0; i < n; i++) {
         Component c = getComponent(i);
         if (c instanceof VariableInput) {
            Point topLeft = c.getLocation();
            g.drawString( c.getName(), topLeft.x + 4, topLeft.y - 4 );
         }
      }
   }
   
   /**
    * Compute the preferred size of this panel.
    * Not meant to be called directly.
    */
   public Dimension getPreferredSize() {
      int width = 0;
      int height = 5;
      FontMetrics fm = getFontMetrics(getFont());
      int lineHeight = (fm == null)? 12 : 4 + fm.getAscent();
      for (int i = 0; i < items.size(); i++) {
         Object obj = items.elementAt(i);
         if (obj instanceof Component) {
            Component c = (Component)obj;
            Dimension d = c.getPreferredSize();
            height += d.height + 5;
            if (c instanceof VariableInput)
               height += lineHeight;
            if (d.width > width)
               width = d.width;
         }
         else {
            Component[] pair = (Component[])obj;
            Dimension d1 = pair[0].getPreferredSize();
            if (pair[0] instanceof VariableInput)
               d1.height += lineHeight;
            Dimension d2 = pair[1].getPreferredSize();
            if (pair[1] instanceof VariableInput)
               d2.height += lineHeight;
            if (twoColumn) {
               width = Math.max(width,d1.width+d2.width);
               height = height + Math.max(d1.height,d2.height) + 5;
            }
            else {
               height += d1.height + d2.height + 10;
               width = Math.max(width,d1.width);
               height = Math.max(height,d2.height) ;
            }
         }
      }
      return new Dimension(width + (twoColumn? 15 : 10), height);
   }
   
   /**
    * Recompute component locations when the panel is resized.
    * Not meant to be called directly.
    */
   public void processComponentEvent(ComponentEvent evt) {
      if (evt.getID() == ComponentEvent.COMPONENT_RESIZED) {
         Dimension size = getSize();
         Dimension preferredSize = getPreferredSize();
         boolean two = twoColumn;  // Do we really use two columns?
         if (two && size.width < preferredSize.width - 20) {
            two = false;
            twoColumn = false;
            preferredSize = getPreferredSize();
            twoColumn = true;
         }
         int count = items.size();
         if (!two)
            for (int i = 0; i < items.size(); i++)
               if (items.elementAt(i) instanceof Component[])
                  count++;
         double scale, vspace;
         if (size.height >= preferredSize.height) {
            scale = 1;
            vspace = 5 + (size.height - preferredSize.height)/(count+2);
            if (vspace > 15)
               vspace = 15;
         }
         else if (size.height >= preferredSize.height - 4*(count+2)) {
            scale = 1;
            vspace = (size.height - (preferredSize.height - 5*(count+2)))/(count+2);
         }
         else {
            scale = (double)size.height / (preferredSize.height - 4*(count+2));
            vspace = 1;
         }
         int hspace = (size.width - (preferredSize.width - (two? 15 : 10)))/(two? 3 : 2);
         if (hspace < 1)
            hspace = 1;
         else if (hspace > 10)
            hspace = 10;
         double y = vspace;
         int lineHeight = 4 + (getFontMetrics(getFont())).getAscent();
         for (int i = 0; i < items.size(); i++) {
            Object obj = items.elementAt(i);
            if (obj instanceof Component) {
               Component c = (Component)obj;
               Dimension p = c.getPreferredSize();
               if (c instanceof VariableInput)
                  y += lineHeight*scale;
               if (p.width + 2*hspace < size.width - 10)
                  c.setBounds((size.width - p.width)/2, (int)y, p.width, (int)(p.height*scale));
               else
                  c.setBounds(hspace, (int)y, p.width - 2*hspace, (int)(p.height*scale));
               y += scale*(p.height) + vspace;
            }
            else {
               Component[] pair = (Component[])obj;
               Dimension d1 = pair[0].getPreferredSize();
               Dimension d2 = pair[1].getPreferredSize();
               if (two) {
                  if (pair[0] instanceof VariableInput || pair[1] instanceof VariableInput)
                     y += lineHeight*scale;
                  int h = (int)(scale*Math.max(d1.height,d2.height));
                  pair[0].setBounds(hspace,(int)y,(size.width-3*hspace)/2,h);
                  pair[1].setBounds(hspace*2+(size.width-3*hspace)/2,(int)y,(size.width-3*hspace)/2,h);
                  y += scale*Math.max(d1.height,d2.height) + vspace;
               }
               else {
                  if (pair[0] instanceof VariableInput)
                     y += lineHeight*scale;
                  pair[0].setBounds(hspace,(int)y,size.width-2*hspace,(int)(d1.height*scale));
                  y += d1.height*scale + vspace;
                  if (pair[1] instanceof VariableInput)
                     y += lineHeight*scale;
                  pair[1].setBounds(hspace,(int)y,size.width-2*hspace,(int)(d2.height*scale));
                  y += d2.height*scale + vspace;
               }
            }
         }
      }
      super.processComponentEvent(evt);
   }

}
