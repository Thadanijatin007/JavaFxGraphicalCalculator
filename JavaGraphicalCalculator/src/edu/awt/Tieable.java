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
 * A Tieable object has an associated serial number.  The value of the serial
 * should increase when the value of the object changes.  A Tieable can "sync" with another
 * Tieable, presumably by copying its serial number and other information.  
 * A given Tieable might only be able to synchronize with other Tiebles of
 * certain types.  If its sync() method is called with an object of the wrong 
 * type, it should probably thrown an IllegalArguemntException. 
 *
 * See the "Tie" and "Controller" classes for information about how Tieable 
 * are used. 
 *
 */
public interface Tieable extends java.io.Serializable {

   /**
    * Get the serial number associated with this Tieable.  If the
    * value of this Tieable changes, then the serial number should
    * increase.
    */
   public long getSerialNumber();

   /**      
    * This routine is called to tell this Tieable that the serial
    * numbers of the Tieables that have been added to the Tie do not
    * match.  newest has a serial number that is at least as
    * large as the serial number of any other Tieable in the Tie.
    * This Tieable should synchronize its value and serial number
    * with the "newest" Tieables.
    *    (Note:  So far, I haven't found any reason to use
    * the Tie parameter in this method!  Maybe it should be removed.)
    */
   public void sync(Tie tie, Tieable newest);

}
