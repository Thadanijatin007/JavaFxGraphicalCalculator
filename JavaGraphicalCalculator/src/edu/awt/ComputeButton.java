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
import java.awt.*;
import java.awt.event.*;

/**
 * A compute button is a button that can have an associated Controller.
 * When the user clicks the button, the compute() method of the 
 * Controller is called.  This class really just exists for convenience.
 */
public class ComputeButton extends Button {

   private Controller onUserAction;  // The Controller whose compute()
                                     // method is called when the user clicks
                                     // the button.

   /**   
    * Create a Compute button labeled "Compute!".
    */
   public ComputeButton() {
      this("Compute!");
   }
   
   /**
    * Create a Compute button displaying the given text.
    */
   public ComputeButton(String label) {
      super(label);
      setBackground(Color.lightGray);
      enableEvents(AWTEvent.ACTION_EVENT_MASK);
   }
   
   /**
    * Set the controller whose compute() method is called
    * when the user clicks this button.
    */
   public void setOnUserAction(Controller c) {
      onUserAction = c;
   }

   /**   
    * Return the controlller whose compute() method is
    * called when the user clicks this button.
    */
   public Controller getOnUserAction() {
      return onUserAction;
   }
   
   /**
    * This is called by the system when the user clicks the
    * button.  Not meant to be called directly.
    */
   public void processActionEvent(ActionEvent evt) {
      if (onUserAction != null)
         onUserAction.compute();
      super.processActionEvent(evt);
   }   

} // end class ComputeButton

