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
 * The Limits interface is implemented by edu.data.CoordinateRect
 * and by other objects that can be "Tied" to a CoordinateRect, such as
 * LimitControlPanel. This will be used to synchronize the (x,y)
 * limits on the CoordinateRect with limit values obtained elsewhere.
 * See the Tie class for more information.
 *
 * @author David Eck
 */
public interface Limits extends java.io.Serializable {

   /**
    * Return a 4-element array containing xmin, xmax, ymin, and ymax. 
    */
   public double[] getLimits();
   
   /**
    * Set the current limits.
    *
    * @param A 4-element array containing the new xmin, xmax, ymin, and ymax.
    */
   public void setLimits(double[] limits);

}
