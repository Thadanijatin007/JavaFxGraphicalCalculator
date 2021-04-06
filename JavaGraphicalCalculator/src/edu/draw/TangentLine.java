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

import edu.data.Constant;
import edu.data.Function;
import edu.data.Value;
import edu.data.ValueMath;
import edu.data.*;

/**
 * A Tangent line is a line that is tangent to the graph of a specified function of one argument
 * at a specified value of its argument.  If added to a CoordinateRect, it will appear
 * as a line.   
 *    A TangentLine is a Computable object, so should be added to a Controller to be 
 * recomputed when the Value or Function changes.
 *
 */
 
public class TangentLine extends DrawGeometric {

   /**
    * Create a tangent line to the graph of a function.
    *
    * @param x The x-coordinate where the tangent is drawn.
    * @param f The line is tangent to the graph of this function.  This should be a function of one variable.
    */
   public TangentLine(Value x, Function f) {
      super(INFINITE_LINE_RELATIVE, x, new ValueMath(f,x), new Constant(1), new ValueMath(f.derivative(1), x));
   }
   
}

