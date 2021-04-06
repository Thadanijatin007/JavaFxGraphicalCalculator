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

import edu.data.Function;
import edu.data.Value;
import edu.data.ValueMath;
import edu.data.*;

/**
 * A Crosshair is a small cross, 15 pixels wide and high, that is drawn in
 * a CoordinateRect at a specified point.
 *     A Crosshair is a Computable object, so should be added to a Controller to be 
 * recomputed when the coordinates of the point change. 
 */

public class Crosshair extends DrawGeometric {

   /**
    * Create a cross that appears at the point with coordinates (x,y).
    */
   public Crosshair(Value x, Value y) {
      super(CROSS, x, y, 7, 7);
   }
   
   /**
    * Create a cross that appears on the graph of the function y=f(x)
    * at the point with coordinates (x,f(x)).  f should be a function
    * of one variable.
    */
   public Crosshair(Value x, Function f) {
      super(CROSS, x, new ValueMath(f,x), 7, 7);
   }
   
}

