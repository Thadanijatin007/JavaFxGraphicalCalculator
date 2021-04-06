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
*       restriction.)                                                   *
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

import java.awt.event.*;
import java.util.Vector;

/**
 * Controllers are the focus of all the action in the JCM system.  A Controller can be
 * set to listen for changes (generally changes in user input).  This is done by
 * registering the Controller with (usally) an InputObject.  For example, if a Controller, c,
 * is to respond when the user presses return in a VariableInput, v, then
 * v.setOnUserAction(c) should be called to arrange to have the Controller listen
 * for such actions.   VariableSliders, ExpressionInputs, MouseTrackers, Animators have a similar
 * methods.  It is also possible to set the Controller to listen for events of
 * type AdjustmentEvent, ActionEvent, TextEvent, or ItemEvent (but this feature is
 * left over from an older version of JCM, and I'm not sure whether it's necessary).
 * Whenever a Controller learns of some change, it will process any InputObjects,
 * Ties, and Computables that have been registered with it.
 *   
 * <p>InputObjects and Computables have to be added to a Controller to be processed,
 * using the Controller's add method.  (If you build your inteface out of JCMPanels,
 * then this is done automatically.)  (Note that an InputObject is added to a Controller
 * to have its value checked -- This is separate from registering the Controller to
 * listen for changes in the InputObject.  Often, you have to do both.)  The gatherInputs()
 * method in class JCMPanel can be used to do most of this registration automaticaly.
 *   
 * <p>A Tie that synchronizes two or more Values, to be effective, has to be added to a Controller.
 * See the Tie class for inforamtion about what Ties are and how they are used.
 *   
 * <p>A Controller can have an associated ErrorReporter, which is used to report any
 * errors that occur during the processing.  Currently, an ErrorReporter is either
 * a DisplayCanvas or a MessagePopup.
 *   
 * <p>A Controller can be added to another Controller, which then becomes a sub-controller.
 * Whenever the main Controller hears some action, it also notifies all its sub-controllers
 * about the action.  Furthermore, it can report errors that occur in the sub-controllers,
 * if they don't have their own error reporters.  (Usually, you will just set an error 
 * reporter for the top-level Controller.)
 **/
public class Controller implements java.io.Serializable, Computable, InputObject,
                      AdjustmentListener, ActionListener, TextListener, ItemListener {
                      
   /**
    * Computable objects controlled by this controller.  Note that Controllers
    * are Computables, so this list can include sub-controllers.
    */
   protected Vector computables;

   /**
    * InputObjects controlled by this controller.  Note that Controllers
    * are InputObjects, so this list can include sub-controllers.
    */
   protected Vector inputs;
                                            
   /**
    * Ties that have been added to this controller.
    */
   protected Vector ties;
   
   /**
    * Used for reporting errors that occur in the
    * compute() method of this controller.  If the errorReporter
    * is null and if this controller has a parent,
    * then the parent will report the error.  If
    * no ancestor has an errorReporter, the error
    * message is written to standard output.
    */
   protected ErrorReporter errorReporter;
                                            
   /**
    * The parent of this controller, if any.
    * This is set automatically when one
    * controller is added to another.
    */
   protected Controller parent;

   /**
    * If non-null, this is an error message
    * that has been reported and not yet cleared.
    */
   protected String errorMessage;
   
   /**
    * Create a Controller.
    */
   public Controller() {
   }

   /**   
    * Set the ErrorReporter used to report errors that occur when the
    * compute() method of this Controller is executed.
    */
   public void setErrorReporter(ErrorReporter r) {
      errorReporter = r;
   }
   
   /**
    * Get the ErrorReporter for this Controller.  Return null if there is none.
    */
   public ErrorReporter getErrorReporter() {
      return errorReporter;
   }

   /**   
    * Add an object to be controlled by this controller.  It should be of
    * one or more of the types InputObject, Computable, Tie.  If it is
    * a Controller, then this Controller becomes its parent.
    */
   public void add(Object obj) {
      if (obj == null)
         return;
      if (obj instanceof Controller) {
          Controller c = (Controller)obj;
          if (c.parent != null)
             c.parent.remove(this);
          c.parent = this;
      }
      if (obj instanceof Computable) {
         if (computables == null)
            computables = new Vector();
         computables.addElement(obj);
      }
      if (obj instanceof InputObject) {
         if (inputs == null)
            inputs = new Vector();
         inputs.addElement(obj);
      }
      if (obj instanceof Tie) {
         if (ties == null)
            ties = new Vector();
         ties.addElement(obj);
      }
   }
   
   /**
    * Remove the object from the controller (if present).
    */
   public void remove(Object obj) {
      if (obj == null)
         return;
      if (computables != null) {
         computables.removeElement(obj);
         if (computables.size() == 0)
            computables = null;
      }
      if (inputs != null) {
         inputs.removeElement(obj);
         if (inputs.size() == 0)
            inputs = null;
      }
      if (ties != null) {
         ties.removeElement(obj);
         if (ties.size() == 0)
            ties = null;
      }
      if (obj instanceof Controller && ((Controller)obj).parent == this)
         ((Controller)obj).parent = null;
   }
   
   /**
    * If this controller has a parent, remove it from its parent.  (Then, a call to the
    * former parent's compute() method will not call this controller's compute().)
    */
   public void removeFromParent() {
      if (parent != null)
         parent.remove(this);
   }
   
   // ----------------- Listening for events ----------------------
   
   /**
    *  Simply calls compute when the Controller hears an ActionEvent.
    *  This is not meant to be called directly.
    */
   public void actionPerformed(ActionEvent evt) {
      compute();
   }
   
   /**
    *  Simply calls compute when the Controller hears a TextEvent.
    *  This is not meant to be called directly.
    */
   public void textValueChanged(TextEvent evt) {
      compute();
   }
   
   /**
    *  Simply calls compute when the Controller hears an AdjustmantEvent.
    *  This is not meant to be called directly.
    */
   public void adjustmentValueChanged(AdjustmentEvent evt) {
      compute();
   }
   
   /**
    *  Simply calls compute when the Controller hears an ItemEvent.
    *  This is not meant to be called directly.
    */
   public void itemStateChanged(ItemEvent evt) {
      compute();
   }

   // -------------- Implementation and error-handling ----------------------

   /**   
    * When an contoller computes, it first calls checkInput() for any
    * InputOjects that it controls (including those in sub-controllers).
    * It then handles any Ties.  Finally,
    * it calls the compute() method of any Computables.  If an error
    * occurs, it reports it.  JCMErrors (which should represent errors
    * on the part of the user) will generally only occur during the
    * checkInput() phase.  Internal, programmer errors can occur at
    * any time and might leave the sytem in an unhappy state.  They are
    * reported as debugging aids for the programmer.  When one occurs,
    * a stack trace is printed to standard output.
    */
   synchronized public void compute() {
      try {
         checkInput();
         doTies();
         clearErrorMessage();
         doCompute();
      }
      catch (JCMError e) { 
         if (errorMessage == null || !errorMessage.equals(e.getMessage()))
            reportError(e.getMessage());
      }
      catch (RuntimeException e) {
         reportError("Internal programmer's error detected?  " + e);
         e.printStackTrace();
      }
   }

   /**   
    * Call checkInput() of each InputObject.  Can throw a JCMError.
    * This is mostly meant to be called by Controller.compute().
    * Note that this will recurse though any sub-controllers of
    * this controller, so that when comput() is called,
    * all the InputObjects in the sub-controllers
    * are processed before ANY Tie or Computable is processed.
    * Similarly, the Ties and Computables in the sub-controllers
    * are processed in separate passes.
    */
   public void checkInput() {
      if (inputs != null) {
         int top = inputs.size();
         for (int i = 0; i < top; i++)
            ((InputObject)inputs.elementAt(i)).checkInput();
      }
   }
   
   /**
    * Check the Ties in this controller and its sub-controllers.
    */
   protected void doTies() {
      if (inputs != null) {
         int top = inputs.size();
         for (int i = 0; i < top; i++)
            if (inputs.elementAt(i) instanceof Controller)
               ((Controller)inputs.elementAt(i)).doTies();
      }
      if (ties != null) {
         int top = ties.size();
         for (int i = 0; i < top; i++)
            ((Tie)ties.elementAt(i)).check();
      }
   }
   
   /**
    * Compute the Computables in this controller and its sub-controllers.
    */
   protected void doCompute() {
      if (computables != null) {
         int top = computables.size();
         for (int i = 0; i < top; i++) {
            Object obj = computables.elementAt(i);
            if (obj instanceof Controller)
               ((Controller)obj).doCompute();
            else
               ((Computable)obj).compute();
         } 
      }
   }
   
   /**
    * Report the specified error message.
    */
   public void reportError(String message) {
      if (message == null)
         clearErrorMessage();
      if (errorReporter != null) {
         errorReporter.setErrorMessage(this,message);
         errorMessage = message;
      }
      else if (parent != null)
         parent.reportError(errorMessage);
      else {
         errorMessage = message;
         System.out.println("***** Error:  " + errorMessage);
      }
   }
   
   /**
    * Clear the error message.
    */
   protected void clearErrorMessage() {
      if (errorReporter != null)
         errorReporter.clearErrorMessage();
      else if (parent != null)
            parent.clearErrorMessage();
      errorMessage = null;
   }
   
   /**
    * Should be called by the ErrorReporter if the ErrorReporter clears the error itself.
    * (This is only used to avoid repeatedly setting the same error message, during an
    * animation for example.)
    */
   public void errorCleared() {
      errorMessage = null;
   }
   
   /**
    * Method required by InputObject interface; in this class, calls the same method
    * recursively on any input objects controlled by this controller.  This is meant to 
    * be called by JCMPanel.gatherInputs().
    */
    public void notifyControllerOnChange(Controller c) {
      if (inputs != null) {
         int top = inputs.size();
         for (int i = 0; i < top; i++)
            ((InputObject)inputs.elementAt(i)).notifyControllerOnChange(c);
      }
    }
    
    /**
     * Calles notifyControllerOnChange(this).  That is, it sets all the InputObjects in
     * this Controller, and in subcontrollers, to notify this Controller when they change.
     */
    public void gatherInputs() {
       notifyControllerOnChange(this);
    }

} // end class Controller
