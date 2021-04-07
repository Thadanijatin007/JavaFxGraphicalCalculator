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

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

/**
 * The class MessagePopup represents a Window that pops up to display an error
 * message.  A MessagePopup object is created by a "source" component.  If 
 * that component is contained in a Frame, then the popup will be a modal dialog 
 * box with that Parent.  If the component is not in a Frame (or is null), then an 
 * independent Frame is used.  The message box is popped up when reportError() is
 * called.  It is closed either when the user clicks the OK button,
 * or if clearErrorMessage() is called.
 */
public class MessagePopup implements ActionListener, ErrorReporter {
   private String errorMessage;
   private Controller errorSource;
   private Component source;
   private Window popup;

   /**
    * Create a MessagePopup with the give source component.  If source is null, then
    * an independent window will always be used to show the error message.
    */
   public MessagePopup(Component source) {
      this.source = source;
   }

   /**
    * Show the given message in a dialog box or independent window,
    * depending on whether the source component is contained in
    * a Frame or not.
    *
    * @param c The Controller that calls this method, or null if it is not called by a Controller.
    *          (The Controller, if any, will be notified when the error message is cleared.)
    * @param message The message to display.
    */
   public void setErrorMessage(Controller c, String message) {
      if (popup != null)
         clearErrorMessage();
      if (message == null)
         return;
      errorSource = c;
      errorMessage = message;
      Component parent = source;
      while (parent != null && !(parent instanceof Frame))
         parent = parent.getParent();
      if (parent != null)
         popup = new Dialog((Frame)parent,"Error Message",true); // modal dialog
      else
         popup = new Frame("Error Message");  // independent window
      popup.setBackground(Color.white);
      popup.add(new MC(message), BorderLayout.CENTER);
      Panel buttonBar = new Panel();
      buttonBar.setLayout(new FlowLayout(FlowLayout.RIGHT,10,10));
      Button OK = new Button("    OK    ");
      OK.addActionListener(this);
      buttonBar.add(OK);
      popup.add(buttonBar, BorderLayout.SOUTH);
      popup.pack();
      if (parent == null) 
         popup.setLocation(100,80);
      else
         popup.setLocation(parent.getLocation().x+50,parent.getLocation().y+30);
      popup.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
               popup.dispose();
            }
        });
      popup.show();  // make the dialog visible.
   }

   /**
    * Get the currently displayed error message. The return value is null if no error message is being displayed.
    */
   public String getErrorMessage() {
      return errorMessage;
   }

   /**   
    * Clear the error message and close the window.  This can be
    * called from outside this class.  It is called automatically
    * when the user clicks the OK button or close box of the window
    * that displays the error message.
    */
   synchronized public void clearErrorMessage() {
      if (popup == null)
         return;
      popup.dispose();
      errorMessage = null;
      if (errorSource != null)
         errorSource.errorCleared();
      errorSource = null;
      popup = null;
   }

   /**
    *  Respond when user clicks OK.  This is not meant to be called directly.
    */
   public void actionPerformed(ActionEvent evt) {
      clearErrorMessage();
   }
   

   /**
    * The nested class MC (Message Canvas) displays the message passed
    * to it in the constructor.  Unless the message is very short,
    * it will be broken into multiple lines.
    */
   private static class MC extends Canvas {
    
      private String message;  // A copy of the message
      
      // The following data is computed in makeStringList()
      
      private Vector messageStrings;  // The message broken up into lines.
      private int messageWidth;       // The width in pixels of the message display.
      private int messageHeight;      // The height in pixels of the message display.
      private Font font;              // The font that will be used to display the message.
      private int lineHeight;         // The height of one line in that font.
      private int fontAscent;         // The font ascent of the font (disance from the
                                      //   baseline to the top of a tall character.)
   
      /**
       * Constructor: store the message.
       *
       * @param message message to store.
       */
      MC(String message) {
         if (message == null)
            this.message = "";  // this.message can't be null.
         else 
            this.message = message;
      }
   

      /**
       * Return the message size, as determined by makeStringList(), allowing
       * space for a border around the message.
       *
       * @return the message size.
       */
      public Dimension getPreferredSize() {
         if (messageStrings == null)
            makeStringList();
         return new Dimension(messageWidth + 20, messageHeight + 20);
      }
      
      /**
       * Display the message using data stored in instance variables.
       *
       * @param g the Graphics context.
       */
      public void paint(Graphics g) {
         if (messageStrings == null)
            makeStringList();
         int y = (getSize().height - messageHeight)/2 + fontAscent;
         if (y < fontAscent)
            y = fontAscent;
         int x = (getSize().width - messageWidth)/2;
         if (x < 0)
            x = 0;
         g.setFont(font);
         for (int i = 0; i < messageStrings.size(); i++) {
            g.drawString( (String)messageStrings.elementAt(i), x, y);
            y += lineHeight;
         }
      }
      
      /**
       * Compute all the instance variables necessary for displaying
       * the message.  If the total width of the message in pixels
       * would be more than 280, break it up into several lines.
       */
      private void makeStringList() {
         messageStrings = new Vector();
         font = new Font("Dialog", Font.PLAIN, 12);
         FontMetrics fm = getFontMetrics(font);
         lineHeight = fm.getHeight() + 3;
         fontAscent = fm.getAscent();
         int totalWidth = fm.stringWidth(message);
         if (totalWidth <= 280) {
            messageStrings.addElement(message);
            messageWidth = 280;
            messageHeight = lineHeight;
         }
         else {
            if (totalWidth > 1800)
               messageWidth = Math.min(500, totalWidth/6);
            else
               messageWidth = 300;
            int actualWidth = 0;
            String line = "    ";
            String word = "";
            message += " ";   // this forces word == "" after the following for loop ends.
            for (int i = 0; i < message.length(); i++) {
               if (message.charAt(i) == ' ') {
                  if (fm.stringWidth(line + word) > messageWidth + 8) {
                      messageStrings.addElement(line);
                      actualWidth = Math.max(actualWidth,fm.stringWidth(line));
                      line = "";
                  }
                  line += word;
                  if (line.length() > 0)
                     line += ' ';
                  word = "";
               }
               else {
                  word += message.charAt(i);
               }
            }
            if (line.length() > 0) {
                messageStrings.addElement(line);
                actualWidth = Math.max(actualWidth, fm.stringWidth(line));
                   
            }
            messageHeight = lineHeight*messageStrings.size() - fm.getLeading();
            messageWidth = Math.max(280,actualWidth);
         }
      }
   
   }  // end nested class MC
   
   
}  // end class MessageDialog
