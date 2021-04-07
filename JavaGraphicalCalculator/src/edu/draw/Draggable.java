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
import java.awt.event.MouseEvent;

/**
 *  An interface that can be implemented by an object that can be dragged
 *  with the mouse.
 */
public interface Draggable {

   /**
    *  Tell the object that a drag operation might be beginning.
    *  The Draggable object can decide whether it really wants
    *  to be dragged, based on the MouseEvent.  It should return
    *  true to indicate that a drag should really be started, and
    *  false if it wants to ignore the MouseEvent.
    */
   public boolean startDrag(MouseEvent evt);
   
   /**
    *  Continue a drag that was started in startDrag().  Presumably
    *  the event is a mouseDragged event.
    */
   public void continueDrag(MouseEvent evt);
   
   /**
    *  Finish a draw that was started in startDrag().  Presumably
    *  the event is a mouseReleased event.
    */
   public void finishDrag(MouseEvent evt);

}
