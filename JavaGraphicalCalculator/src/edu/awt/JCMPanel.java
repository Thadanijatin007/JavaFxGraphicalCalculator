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

package edu.awt;

import edu.data.*;
import java.awt.*;
import java.awt.event.*;

/**
 * A JCMPanel is a Panel with an associated Controller.  When an InputObject or
 * Computable is added to the JCMPanel, it is automatically added to the controller.
 * When a sub-JCMPanel is added, the Controller of the sub-panel is "attatched" to
 * the controller of the main panel so that objects in the sub-panel will also
 * be controlled by the Controller of the main panel.  So, if you build an
 * interface entirely from JCMPanels, a lot of the control setup is done
 * automatically.  Note that to make this work, you will need
 * a "mainPanel" that fills the entire window or applet (or at least the part that
 * holds JCM components).  You should also call the gatherInputs() method of
 * the main JCMPanel after it is completely set up, so that changes in input objects
 * will cause the panel's controller to be notified, or, alternatively, you
 * can register the Controller by hand with InputObjects so that the
 * Controller will be notified when they change.
 * 
 * <p>The disadvantage of this is that all the data used in the interface is recomputed,
 * even if the input objects that they depend on have not changed.  For example.
 * if the user changes the value in a VarialbleInput, all the points on a graph
 * will be recomputed even if the function has not changed.  The alternative is
 * to use regular Panels for all or part of the interface and configure some
 * Controllers by hand.
 *
 */
public class JCMPanel extends Panel {

   private int insetGap;           // Size of gap, in pixels, around the edges of the
                                   // Panel, where the background color shows through.
                                   
   private Controller controller;  // The controller associated with this panel.
   
   
   /**
    * Create a JCMPanel that uses a given layout manager.
    *
    * @param layout layout manager to use.  (This could be null.)
    */
   public JCMPanel(LayoutManager layout) {
      enableEvents(ContainerEvent.CONTAINER_EVENT_MASK);
      setLayout(layout);
   }

   /**   
    * Create a JCMPanel that uses a BorderLayout with horizontal and veritcal
    * gaps of 3 pixels.
    */
   public JCMPanel() {
      this(3);
   }

   /**   
    * Create a JCMPanel that uses a BorderLayout with horizontal and vertical
    * gaps of "gap" pixels.
    *
    * @param gap inset gap to use.
    */
   public JCMPanel(int gap) {
      this(new BorderLayout(gap,gap));
   }

   /**   
    * Create a JCMPanel that uses a GridLayout with the specified number of rows
    * and columns and with horizontal and veritcal gaps of 3 pixels between components.
    *
    * @param rows number of rows in the GridLayout.
    * @param columns number of columns in the GridLayout.
    */
   public JCMPanel(int rows, int columns) {
      this(rows,columns,3);
   }

   /**   
    * Create a JCMPanel that uses a GridLayout with the specified number of rows
    * and columns and with horizontal and vertical gaps of "gap" pixels.
    *
    * @param rows number of rows in the GridLayout.
    * @param columns number of columns in the GridLayout.
    * @param gap number of pixels between rows and columns
    */
   public JCMPanel(int rows, int columns, int gap) {
      this(new GridLayout(rows,columns,gap,gap));
   }

   /**   
    * Set the size of the "Insets" for this JCMPanel.  This is the gap, in pixels, around the edges of the
    * Panel, where the background color shows through.
    *
    * @param x inset gap to use.
    */
   public void setInsetGap(int x) {
      insetGap = x;
   }
   
   /**
    * Called by the system to determine how much of a gap to leave
    * on each edge of the panel.  Not meant to be called directly
    */
   public Insets getInsets() {
      return new Insets(insetGap,insetGap,insetGap,insetGap);
   }
   
   /**
    * Return the controller associated with this JCMPanel.
    */
   public Controller getController() {
      if (controller == null)
         controller = new Controller();
      return controller;
   }
   
   /**
    * This method will set all the input objects in this JCMPanel
    * and in sub-JCMPanels, as well as any other input objects that have been
    * added to the panels' Controllers, to notify the Controller of this JCMPanel 
    * when they change.  It does this by calling setOnUserAction(c) -- or
    * a corresponding method -- for each input object c.  This is meant to
    * be used, ordinarily, at the end of an applet's init() method, as
    * an alternative to adding each of the input objects to the controller
    * by hand.
    */
   public void gatherInputs() {
      Controller c = getController();
      c.notifyControllerOnChange(c);
   }
   
   /**
    * Called by the system when a component is added to or removed from
    * this panel.  This takes care of automatically adding and removing
    * things from this Panel's Controller.  This is not meant to be called directly
    */
   public void processContainerEvent(ContainerEvent evt) {
      Component child = evt.getChild();
      if (child instanceof JCMPanel) {
         if (evt.getID() == ContainerEvent.COMPONENT_ADDED)
            getController().add(((JCMPanel)child).getController());
         else if (evt.getID() == ContainerEvent.COMPONENT_REMOVED)
            getController().remove(((JCMPanel)child).getController());
      }
      else if (child instanceof Computable || child instanceof InputObject) {
         if (evt.getID() == ContainerEvent.COMPONENT_ADDED)
            getController().add(child);
         else if (evt.getID() == ContainerEvent.COMPONENT_REMOVED)
            getController().remove(child);
      }
   }
}  // end class JCMPanel
