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

/**
 * An InputObject represents some sort of value that can be changed
 * by, for example, user interaction with a GUI element.  The value can
 * actually change only when the checkInput() method is called.  Generally,
 * an InputObject is a GUI element with an associated MathObject such as
 * a Variable or Expression.  For example, a VariableInput is a text-input
 * box where the user can enter the value of a Variable.  However, the
 * input is only checked and the value of the variable can only change
 * when the VariableInput's checkInput() method is called.  The checkInput()
 * method is generally meant to be called by a Controller object.  The
 * checkInput() method should throw a JCMError if an error occurs.
 * See the Controller class for more information.
 *
 * @author David Eck
 */
public interface InputObject extends java.io.Serializable {

   /**
    * Check and possibly change the value associated with this InputObject.
    */
   public void checkInput();
   
   /**
    * This method was introduced to provide a common interface for setting
    * a Controller that is to be notified when there is a change in the
    * InputObject.  (This was introduced late in development, to be used
    * by JCMPanel.gatherInputs().  In all the standard
    * classes that implement the InputObject interface, this method 
    * simply calls a setOnChange or setOnUserAction method.)
    */
   public void notifyControllerOnChange(Controller c);

} // end interface InputObject
