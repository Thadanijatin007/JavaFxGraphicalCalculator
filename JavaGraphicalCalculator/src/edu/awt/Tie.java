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

import java.util.Vector;

/**
 * A Tie associates several Tieable objects.  When the check() mehtod of the
 * Tie is called, it determines which of the Tieables has the largest serial number.
 * It then tells each Tieable to synchronize with that object.  Ordinarily, the
 * Tie is added to a Controller, which is responsible for calling the Tie's
 * check() method.
 *  
 * <p>This is meant to be used, for example, to Tie together two InputObjects to synchronize
 * the values that they represent.  For example, you might want a VariableSlider
 * and a VariableInput to be alternative ways of inputting the same value.  If so,
 * you can put them in a Tie and add that Tie to any Controller that is set to
 * respond to changes in the VariableSlider or VariableInput.
 * The x- and y- variables of a MouseTracker are also Tieable objects, so you
 * can synchronize the values of two MouseTrackers (in different CoordinateRects,
 * presumably) and you can synchronize the value of a MouseTracker variable with
 * a VariableInput or VariableSlider.
 *  
 * <p>CoordinateRects and LimitControlPanels are also Tieable (to each other -- not
 * to Value objects).  This is used to allow the LimitControlPanel to synchronize
 * with the Limits on the CoordinateRects that it controls.  It could also 
 * synchronize the Limits on two CoordinateRects, even in the absense of a
 * LimitControlPanel.
 *
 * @author David Eck
 */
public class Tie {
   
   /**
    * The Tieables in this Tie.
    */
   protected Vector items = new Vector(2);
   
   /**
    * Create a Tie, initially containing no objects.
    */
   public Tie() {
   }
   
   /**
    * Create a Tie initally containing only the object item.
    * item should be non-null.
    *
    * @param item the only initial item in this tieable.
    */
   public Tie(Tieable item) {
      add(item);
   }

   /**   
    * Create a Tie initially containing item1 and item2.
    * The items should be non-null.  The items will be
    * synced with each other at the time the Tie is created.
    */
   public Tie(Tieable item1, Tieable item2) {
      add(item1);
      add(item2);
   }

   /**   
    * Add item to the tie, and sync it with the items that are
    * already in the Tie.  It should be non-null.  Note that synchronization
    * of the objects is forced even if they all have the same serial number,
    * since the values might not be the same when they are first added to
    * the Tie.
    */
   public void add(Tieable item) {
      if (item != null) {
         items.addElement(item);
         forcecheck();
      }
   }
   
   /**
    * If this Tie contains more than one item, find the newest
    * one and sync all the items with that item.  If the serial
    * numbers of all the items are already the same, nothing is
    * done.
    */
   public void check() { 
      int top = items.size();
      if (top < 2)
         return;
      long maxSerialNumber = ((Tieable)items.elementAt(0)).getSerialNumber();
      int indexOfMax = 0;
      boolean outOfSync = false;
      for (int i = 1; i < top; i++) {
         long sn = ((Tieable)items.elementAt(i)).getSerialNumber();
         if (sn != maxSerialNumber)
            outOfSync = true;
         if (sn > maxSerialNumber) {
            maxSerialNumber = sn;
            indexOfMax = i;
         }
      }
      if (!outOfSync)  // if serialnumbers are the same, no sync is necessary.
         return;
      Tieable newest = (Tieable)items.elementAt(indexOfMax);
      for (int i = 0; i < top; i++)
         ((Tieable)items.elementAt(i)).sync(this, newest);
   }
   
   private void forcecheck() {  // Synchronize the items in this Tie, even if serial numbers are the same.
      int top = items.size();
      if (top < 2)
         return;
      long maxSerialNumber = ((Tieable)items.elementAt(0)).getSerialNumber();
      int indexOfMax = 0;
      boolean outOfSync = false;
      for (int i = 1; i < top; i++) {
         long sn = ((Tieable)items.elementAt(i)).getSerialNumber();
         if (sn != maxSerialNumber)
            outOfSync = true;
         if (sn > maxSerialNumber) {
            maxSerialNumber = sn;
            indexOfMax = i;
         }
      }
      Tieable newest = (Tieable)items.elementAt(indexOfMax);
      for (int i = 0; i < top; i++)
         ((Tieable)items.elementAt(i)).sync(this, newest);
   }
   
} // end class Tie
