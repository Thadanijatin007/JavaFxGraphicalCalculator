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
 * To allow different styles of reporting errors, a
 * Controller uses an ErrorReporter to report any
 * errors that are thrown during its checkInput/compute
 * cycle.  The DisplayCanvas and MessagePopup classes
 * implement this interface.
 *
 * @author David Eck
 */
public interface ErrorReporter {

   /**
    * Report the specifed message as an error.  If source is non-null,
    * then it is the Controller that called this routine.  In that case,
    * if the error reporter is capable of clearing its own error
    * condition, it should call source.errorCleared() when it does so.
    *
    * @param source Controller that called this method (if non-null).
    * @param message error message to report.
    */
   public void setErrorMessage(Controller source, String message);

   /**
    * Clear the error reprort, if there is one.
    */
   public void clearErrorMessage();

   /**
    * Get the error message that is currently being displayed, or
    * return null if there is no error message.
    */
   public String getErrorMessage();
   
}

