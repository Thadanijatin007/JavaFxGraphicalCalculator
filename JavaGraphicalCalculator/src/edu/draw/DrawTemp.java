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

import java.awt.Graphics;

/**
 * An object that implements this interface can draw itself, using information
 * from a CoordinateRect (in which it presumably appears).  This interface is
 * meant to be used with the method drawTemp() in DisplayCanvas.
 */
public interface DrawTemp extends java.io.Serializable {

   /**
    * Draw this item in the specified graphics context, possibly using information
    * from the specified CoordinateRect.  Note that the drawTemp() method
    * in class DisplayCanvas creates a new graphics context every time it
    * is called, just for drawing this item.
    */
   public void draw(Graphics g, CoordinateRect coords);

}


