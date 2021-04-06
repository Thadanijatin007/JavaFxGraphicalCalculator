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

// November 2005: Removed "beep" from processKeyEvent

package edu.awt;

import edu.data.*;
import edu.data.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import java.io.*;


/**
 *  A DataTableInput lets the user input a grid of real numbers arranged
 *  in rows and columns.  Each column has a name, and rows are numberd
 *  starting from 1.  The column names and row numbers can be
 *  displayed, optionally.  By default, a new row is added automatically
 *  if the user moves down out of the last row by pressing return
 *  or down-arrow, provided the last row is non-empty.  Rows can also be
 *  added programmatically.  Columns are usually added in the constructor,
 *  but they can also be added later.  If the user leaves a cell
 *  at a time when the content of that cell does not represent a legal
 *  number, then the message "bad input" will be displayed in the cell.
 *  <p>A DataTableInput can be given a name and can then be added to a parser. 
 *  The parser will then recognize the name of the table.  In an expression, the table name
 *  can be followed by a ".", then one of the column names from table, then
 *  an expression in parentheses.  This represents a reference to the number in
 *  one of the cells in the table.  The expression gives the number of
 *  the row, where rows are numbered starting from 1.  For example:
 *  "data.A(3)" represents the third number in column "A" in a table
 *  named data.  The table name and "." can also be followed by the word "sum" and
 *  an expression in parentheses.  An expression used in this way
 *  can include references to the column names in the table and to
 *  the special value "rowNumber".  The value of expression is computed
 *  for each row in the table and the sum of the values is computed.  In the expression that
 *  is being summed, a column name represents the 
 *  number in that column and rowNumber represents the number of
 *  the row.  For example:  "data.sum(A^2)".  Finally, the "." can be
 *  followed by the word "count".  "count" can, optionally be followed
 *  by an empty pair of parentheses.  This represents the number of
 *  rows.  For example:  "data.count".   Empty rows at the bottom
 *  of the table are ignored in both "sum" and "count".
 *  <p>Note that rows are numbered starting from 1 because row numbers
 *  can be visible to the user, who shouldn't be subjected to zero-based
 *  numbering.  However, columns are always numbered from zero.
 */
public class DataTableInput extends Panel implements ParserExtension {

   private String objectName;    // The name of the DataTableObject

   private Vector rows;          // Vector of double[].  Each entry holds
                                 // the values in a row of a table.  An
                                 // entry is null for an empty row.
                                   
   private Vector rowStrings;    // Row values as Strings.  The string "bad input"
                                 // means that the string in the cell does not
                                 // represent a legal number
   
   private String[] columnName;  // The name to be used to refer to the column
                                 // in expressions.  Also used as a label at the
                                 // top of the column.
                                
   private int columnCount;      // Number of columns
   
   private int currentRow = 1;   // The number of the "currentRow" which is
                                 // specified in the setCurrentRowNumber() method.
   
   private double emptyCellValue = Double.NaN;  // An empty cell is considered to have this value.

   private boolean throwErrors = false;  // Should an error be thrown when getValue(col,row) 
                                         // sees "bad input" in the cell.  If not, then the
                                         // value is considered to be Double.NaN.
                                   
   private boolean autoAddRows = true;  // If this is true, an empty row is added
                                        // when the user presses enter or hits
                                        // down-arrow while in the last row, if that row
                                        // is empty.
                                   
   private boolean showColumnTitles;    // If true, column names are shown above the columns

   private boolean showRowNumbers;      // If true, row numbers are shown to left of each row

   private DisplayPanel canvas;  // Where the table appears; nested class DisplayPanel is defined below.
   
   private long serialNumber;    // This is incremented each time the table changes.

   private Color labelBackground = new Color(220,220,220);  // Colors of various things.
   private Color cellBackground = new Color(255,255,220);
   private Color blankBackground = Color.gray;
   private Color gridColor = Color.blue;
   
   /**
    *  Create a DataTableInput with no columns.  Columns can be added later
    *  using the addColumn() methods.  The table initially has no name.
    */
   public DataTableInput() {
      this(null,0);
   }
   

   /**  
    *   Create a table with the specified column names.  If columnNames
    *   is null, the number of columns is zero.  The name can be null, if you
    *   don't need a name for the table.  The length of the array determines
    *   the number of columns in the table.
    */  
   public DataTableInput(String name, String[] columnNames) {
      this(name,columnNames == null ? 0 : columnNames.length);
      if (columnNames != null)
         for (int i = 0; i < columnNames.length; i++)
            setColumnName(i,columnNames[i]);
   }   
   
   /**  
    *   Create a table with the specified number of columns,
    *   named "A", "B", etc.  The name can be null, if you
    *   don't need a name for the table.  The number of columns can be zero.
    */  
   public DataTableInput(String name, int columns) {
      if (columns < 0)
         columns = 0;
      setName(name);
      rowStrings = new Vector();
      rows = new Vector();
      rowStrings.addElement(null);
      rows.addElement(null);
      columnName = new String[columns];
      for (int i = 0; i < columns; i++)
         columnName[i] = "" + (char)( (int)'A' + i );
      canvas = new DisplayPanel();
      setLayout(new BorderLayout());
      setBackground(Color.lightGray);
      add(canvas,BorderLayout.CENTER);
      add(canvas.vScroll,BorderLayout.EAST);
      columnCount = columns;
   }

   //------------------------- Data access and variables
   
   /**
    *   Required by the ParserExtension interface and not meant to be called directly.
    *   This is called by a parser if it encounters the name of the table in an
    *   expression.  It parses the complete table reference, such as "data.A(3)"
    *   or "data.sum(A^2)".
    */
   public void doParse(Parser parser, ParserContext context) {
      int tok = context.next();
      if (tok != ParserContext.OPCHARS || !context.tokenString.equals("."))
         throw new ParseError("Expected a '.' after the name of a data table.",context);
      tok = context.next();
      if (tok != ParserContext.IDENTIFIER)
         throw new ParseError("Expected 'sum', 'count', or the name of a column after data table name.", context);
      String commandName = context.tokenString;
      int command = -10;
      for (int i = 0; i < columnCount; i++)
         if (commandName.equalsIgnoreCase(getColumnName(i))) {
            command = i;
            break;
         }
      if (command == -10) {
         if (commandName.equalsIgnoreCase("sum"))
            command = -1;
         else if (commandName.equalsIgnoreCase("count"))
            command = -2;
      }
      if (command == -10)
         throw new ParseError("Unrecognized table command \""+ commandName + "\".", context);
      if (command == -2) {
         if (context.look() == ParserContext.OPCHARS && context.tokenString.equals("(")) {
           context.next();
           if (context.next() != ParserContext.OPCHARS || !context.tokenString.equals(")"))
              throw new ParseError("Missing right parenthesis; \"count\" does not take a parameter.", context);
         }
         context.prog.addCommandObject(new DTEC(-2,null));
         return;
      }
      if (context.next() != ParserContext.OPCHARS || !context.tokenString.equals("("))
         throw new ParseError("Expected a left parentheses after table command \"" + commandName + "\".", context);
      ExpressionProgram saveProg = context.prog;
      ExpressionProgram tableProg = new ExpressionProgram();
      context.prog = tableProg;
      if (command == -1) {
         context.mark();
         for (int i = 0; i < columnCount; i++)
            context.add(getColumnVariable(i));
         context.add(getRowNumberVariable());
      } 
      parser.parseExpression(context);
      context.prog = saveProg;
      if (context.next() != ParserContext.OPCHARS || !context.tokenString.equals(")"))
         throw new ParseError("Missing right parenthesis.", context);
      context.prog.addCommandObject(new DTEC(command,tableProg));
      if (command == -1)
         context.revert();
   }
   
   /**
    *  Return the number of rows in the table, ignoring empty rows at the bottom
    *  of the table.  Note that an empty row that precedes some non-empty row
    *  is included in the count.
    */
   public int getNonEmptyRowCount() {
      int rowCt = rows.size();
      while (rowCt > 0 && rows.elementAt(rowCt-1) == null)
         rowCt--;
      return rowCt;
   }

   /**  
    *   Get the number in the specified row and column.  Rows are numberd starting
    *   from 1, but columns are numbered starting from zero.  If the specified
    *   cell does not exist in the table, Double.NaN is returned.  If the cell is empty,
    *   emptyCellValue is returned.  If the content of the cell does not define
    *   a legal real number, then the action depends on the value of the missingValueIsError
    *   property:  If this property is true, then a JCMError is thrown; if it is
    *   false, then Double.NaN is returned.
    */
   public double getCellContents(int row, int col) {
      if (row < 1 || row > rows.size() || col < 0 || col > columnCount)
         return Double.NaN;
      return canvas.getValue(col,row);
   }
   
   /**
    *  Put the given real number, val, in the cell in the specified row
    *  and column, where rows are numbered starting from 1 and columns are
    *  numbered starting from zero.  This is ignored if the specified row
    *  and column do not exist in the table.
    */
   public void setCellContents(int row, int col, double val) {
      if (row < 1 || row > rows.size() || col < 0 || col > columnCount)
         return;
      canvas.setValue(col,row,val);
   }
      
   /**
    *  Set the current row in the table.  If the parameter is less than 1, the current
    *  row is set to 1.
    *  (The table keeps track of a "current row number", which is always greater than
    *  or equal to 1.  This row number is used when a column variable or row number
    *  variable is evaluated.  These variables can be added to a parser using the
    *  addVariablesToParser method, and can then be used in expressions parsed by
    *  that parser.  When the row number variable, which is named rowNumber, is 
    *  evaluated, its value is the current row number in the table.  When a column
    *  variable is evaluated, its value is the number in the cell in the associated
    *  column and in the current row.  The setCurrentRowNumber() method, in combination
    *  with the getNonEmptyRowCount() method allow you to iterate through the rows
    *  of the table and evaluate the expression for each row.)
    */
   public void setCurrentRowNumber(int i) {
      currentRow = (i < 1)? 1 : i;
   }
   
   /**
    *  Return the current row number.
    */
   public int getCurrentRowNumber() {
      return currentRow;
   } 
   
   /**
    *  Return a column variable for the specified column, where columns are
    *  numbered starting from 1.  The value of this variable is the number
    *  in the specified column and in the current row of the table (as set
    *  by the setCurrentRowNumber() method.)  The name of the variable is
    *  the name of the column.  This method is protected since variables are
    *  not meant to be used as regular variables.  But they can be added to
    *  a Parser by calling the addVariablesToParser() method.)
    */
   protected Variable getColumnVariable(final int columnNum) {
      if (columnNum < 0 || columnNum >= columnCount)
         throw new IllegalArgumentException("Column number out of range.");
      return new Variable(getColumnName(columnNum),0) {
            public void setVal(double v) {
                if (currentRow < rows.size())
                   canvas.setValue(columnNum,currentRow,v);
                super.setVal(v);
            }
            public double getVal(){
               if (currentRow > rows.size())
                  return Double.NaN;
               else
                  return canvas.getValue(columnNum,currentRow);
            }
         };
   }
   
   /**
    *  Get a variable that represents the current row number in the table,
    *  as set by the setCurrentRowNumber() method.  The name of the
    *  variable is rowNumber.
    */
   protected Variable getRowNumberVariable() {
      return new Variable("rowNumber",0) {
            public void setVal(double v) {
               int val = (int)(v + 0.5);
               if (val < 1 || val > getNonEmptyRowCount())
                  val = getNonEmptyRowCount() + 1;
               currentRow = val;
               super.setVal(val);
            }
            public double getVal(){
               return currentRow;
            }
         };
   }
   
   /**
    *  Add a row number variable (from the getRowNumberVariable() method) and
    *  a column variable for each column (from the getColumnVariable() method)
    *  to the parser.  The parser will then be able to parse expressions that
    *  refer to these variables.  The value of such an expression depends on
    *  the current row number, as set by setCurrentRowNumber().
    */
   public void addVariablesToParser(Parser p) {
      p.add(getRowNumberVariable());
      for (int i = 0; i < columnCount; i++) {
         p.add(getColumnVariable(i));
      }
   }
   
   /**
    *  Get the serial number of the table.  This is incremented each time the
    *  table changes in any way.
    */
   public long getSerialNumber() {
      return serialNumber;
   }
   
   //-------------------------
   
   /**
    *  Set the throwErrors property.  If this is true, then a JCMError is thrown when
    *  an attempt is made to use the value of a cell that contains an invalid String.
    *  Note that referring to an empty cell is not an error.  The default value is true. 
    */   
   public void setThrowErrors(boolean throwErr){
      throwErrors = throwErr;
   }
   
   /**
    *  Get the value of the throwErrors property, which determines whether an error
    *  is thrown when an attempt is made to refer to the value of a cell that
    *  contains an invalid string.
    */
   public boolean getThrowErrors(){
      return throwErrors;
   }
   
   /**
    *  Set the value that should be returned when the value of an empty cell is
    *  requested.  The default value is Double.NaN.  Another plausible value, in
    *  some circumstances, would be zero.
    */
   public void setEmptyCellValue(double val){
      emptyCellValue = val;
   }
   
   /**
    *  Get the value that is represented by an empty cell.
    */
   public double getEmptyCellValue() {
      return emptyCellValue;
   }

   /**
    *  If the value of autoAddRows is true, then an empty row is added to the table
    *  automatically when the user attempts to move down from the last row of
    *  the table, provided that the last row is non-empty (so there can only be
    *  one auto-added row at a time).  If the user leaves this row while it is
    *  still empty, it will automatically be deleted.  The default value is true.
    */
   public void setAutoAddRows(boolean auto) {
         // Set the autoAddRows property. 
      autoAddRows = auto;
      canvas.lastRowAutoAdded = false;
   }
   
   /**
    *  Get the value of the autoAddRows property, which determines whether empty 
    *  rows are automatically added to the bottom of the table when needed.
    */
   public boolean getAutoAddRows() {
      return autoAddRows;
   }
   
   /**
    *  Set the name of this DataTableInput.  This is only needed if the table is
    *  to be added to a parser.  The name should be a legal identifier.
    */
   public void setName(String name) {
      objectName = name;
   }
   
   /**
    *  Get the name of the DataInputTable (which might be null).
    */
   public String getName() {
      return objectName;
   }
   
   /**
    *  Set the name of column number i, where columns are numbered starting
    *  from zero.  If column variables are to be used or if
    *  the DataTableInput itself is to be added to a parser, then the name should
    *  be a legal identifier.  If the showColumnTitles property is set to true,
    *  then column names are shown at the top of the table.
    */
   public void setColumnName(int i, String name) {
      if (name != null)
         columnName[i] = name;
   }
   
   /**
    *  Get the name of column number i, where columns are numbered starting from zero.
    */
   public String getColumnName(int i) {
      return columnName[i];
   }
   
   /**
    *  Add the specified number of empty rows at the bottom of the table.  If you
    *  want a table with a fixed number of rows, add them with this method and
    *  set the autoAddRows property to false.
    */
   public void addRows(int rowCt) {
      canvas.addRows(rowCt,rows.size());
   }
   
   /**
    *  Insert a row before the row that contains the cell that the user is editing.
    */
   public void insertRow() {
      canvas.addRows(1,canvas.activeRow);
   }
   
   /**
    *  Delete the row that contains the cell that the user is editing.  However,
    *  if that is the only row in the table, just make the row empty.
    */
   public void deleteCurrentRow() {
      if (canvas.activeRow == rows.size() - 1 && rows.size() > 1) {
         canvas.setActive(canvas.activeRow-1,canvas.activeColumn);
         rows.removeElementAt(canvas.activeRow + 1);
         rowStrings.removeElementAt(canvas.activeRow + 1);
      }
      else {
         rows.removeElementAt(canvas.activeRow);
         rowStrings.removeElementAt(canvas.activeRow);
      }
      if (rows.size() == 0) {
         rows.addElement(null);
         rowStrings.addElement(null);
      }
      String[] vals = (String[])rowStrings.elementAt(canvas.activeRow);
      if (vals == null || vals[canvas.activeColumn] == null)
         canvas.input.setText("");
      else
         canvas.input.setText(vals[canvas.activeColumn]);
      canvas.checkScroll();
      canvas.repaint();
      if (canvas.rowLabelCanvas != null)
         canvas.rowLabelCanvas.repaint();
      if (canvas.columnLabelCanvas != null)
         canvas.columnLabelCanvas.repaint();
      serialNumber++;
   }
      
   /**
    *  Remove all rows from the table, leaving just one empty row.
    */
   public void clear() {
      rows = new Vector();
      rowStrings = new Vector();
      rows.addElement(null);
      rowStrings.addElement(null);
      canvas.setActive(0,0);
      canvas.checkScroll();
      canvas.repaint();
      if (canvas.rowLabelCanvas != null)
         canvas.rowLabelCanvas.repaint();
      if (canvas.columnLabelCanvas != null)
         canvas.columnLabelCanvas.repaint();
      serialNumber++;
   }
   
   /**
    * Get the number of columns in the table.
    */
   public int getColumnCount() {
      return columnName.length;
   }
   
   /**
    *  Add a column at the right side of the table, with all cells initially
    *  empty.  The name of the columns will be single letter such as 'A', 'B', ...
    */
   public int addColumn() {
      return addColumn(null);
   }
   
   /**
    *  Add a column with the specified name at the right side of the table, with all cells initially
    *  empty.  This is inefficient if the table already contains a bunch of non-empty rows.
    */
   public int addColumn(String name) {
      int newSize = columnName.length + 1;
      String[] newNames = new String[newSize];
      for (int i = 0; i < columnName.length; i++)
         newNames[i] = columnName[i];
      if (name == null)
         newNames[newSize-1] = "" + (char)( (int)'A' + newSize - 1 );
      else
         newNames[newSize-1] = name;
      columnName = newNames;
      int rowCt = rows.size();
      for (int i = 0; i < rowCt; i++) {
         if (rows.elementAt(i) != null) {
            double[] oldRow = (double[])rows.elementAt(i);
            double[] newRow = new double[newSize];
            for (int j = 0; j < oldRow.length; j++)
               newRow[j] = oldRow[j];
            newRow[newSize - 1] = Double.NaN;
            rows.setElementAt(newRow,i);
         }
         if (rowStrings.elementAt(i) != null) {
            String[] oldRow = (String[])rows.elementAt(i);
            String[] newRow = new String[newSize];
            for (int j = 0; j < oldRow.length; j++)
               newRow[j] = oldRow[j];
            rowStrings.setElementAt(newRow,i);
         }
      }
      if (canvas.hScroll != null)
         canvas.checkScroll();
      canvas.repaint();
      if (canvas.columnLabelCanvas != null)
         canvas.columnLabelCanvas.repaint();
      columnCount = columnName.length;
      serialNumber++;
      return columnCount - 1;
   }
   
   /**
    *  Test whether the column name is shown at the top of each column.
    */
   public boolean getShowColumnTitles() {
      return showColumnTitles;
   }
   
   /**
    *  If set to true, then the column name is shown at the top of each column.  The
    *  default value is false.  This is meant to be called before the table has been
    *  shown on the screen, such as in the init() method of an applet.  If you call it
    *  after the table has already been shown, you will have to validate the panel
    *  yourself.
    */
   public void setShowColumnTitles(boolean show) {
      if (show == showColumnTitles)
         return;
      showColumnTitles = show;
      if (showColumnTitles) {
         canvas.makeColumnLabelCanvas();
         add(canvas.columnLabelCanvas, BorderLayout.NORTH);
      }
      else {
         remove(canvas.columnLabelCanvas);
         canvas.columnLabelCanvas = null;
      }
   }
   
   /**
    *  Test whether row numbers are shown.
    */
   public boolean getShowRowNumbers() {
      return showRowNumbers;
   }
   
   /**
    *  If set to true, then the row number is shown at the left of each row.  The
    *  default value is false.  This is meant to be called before the table has been
    *  shown on the screen, such as in the init() method of an applet.  If you call it
    *  after the table has already been shown, you will have to validate the panel
    *  yourself.
    */
   public void setShowRowNumbers(boolean show) {
      if (show == showRowNumbers)
         return;
      showRowNumbers = show;
      if (showRowNumbers) {
         canvas.makeRowLabelCanvas();
         add(canvas.rowLabelCanvas, BorderLayout.WEST);
      }
      else {
         remove(canvas.rowLabelCanvas);
         canvas.rowLabelCanvas = null;
      }
   }
   
   /**
    *   Returns the color that is used as a background for row numbers and column titles.
    */
   public Color getLabelBackground() {
      return labelBackground;
   }
   
   /**
    *  Set the color to be used as a background for row numbers and column titles.
    *  The default is a very light gray.
    */
   public void setLabelBackground(Color color) {
      if (color != null)
         labelBackground = color;
   }
   
   /**
    *   Returns the color that is used as a background for cells in the table.
    */
   public Color getCellBackground() {
      return cellBackground;
   }
   
   /**
    *  Set the color to be used as a background for cells in the table.
    *  The default is a light yellow.
    */
   public void setCellBackground(Color color) {
      if (color != null)
         cellBackground = color;
   }
   
   /**
    *  Returns the color that is used for blank areas in the table, below the
    *  rows of cells.
    */
   public Color getBlankBackground() {
      return blankBackground;
   }
   
   /**
    *  Get the color to be used as a background blank areas in the table, below the
    *  rows of cells.  The default is a gray.
    */
   public void setBlankBackground(Color color) {
      if (color != null)
         blankBackground = color;
   }
   
   /**
    *  Returns the color that is used for the lines between cells in the table.
    */
   public Color getGridColor() {
      return gridColor;
   }
   
   /**
    *  Get the color to be used for the lines between cells in the table.
    *  The default is a blue.
    */
   public void setGridColor(Color color) {
      if (color != null)
         gridColor = color;
   }
   
   /**
    *  Read data for table from the specified Reader.  One row is filled
    *  from each non-empty line of input.  The line should contain 
    *  numbers separated by spaces/tabs/commas.  The word "undefined"
    *  can be used to represent an empty cell.  Otherwise, if a non-number is
    *  encountered, an error occurs.  If not enough numbers are
    *  found on a line, the extra columns are filled with empties.  After
    *  filling all columns, extra data on the line is ignored.
    *  Data currently in the table is removed and replaced (if no error 
    *  occurs during reading).  In the case of an error, if throwErrors is
    *  true, then a JCMError is thrown; if throwErrors is false, no
    *  error is thrown, but the return value is false.  If no error occurs,
    *  the return value is true.  If an error occurs, the previous data
    *  in the table is left unchanged.
    */
   public boolean readFromStream(Reader in) {  
      Vector newRows = new Vector(); 
      int cols = columnCount;
      try {
         StreamTokenizer tokenizer = new StreamTokenizer(in);
         tokenizer.resetSyntax();
         tokenizer.eolIsSignificant(true);
         tokenizer.whitespaceChars(',', ',');
         tokenizer.whitespaceChars(' ', ' ');
         tokenizer.whitespaceChars('\t', '\t');
         tokenizer.wordChars('a','z');
         tokenizer.wordChars('A','Z');
         tokenizer.wordChars('0','9');
         tokenizer.wordChars('.','.');
         tokenizer.wordChars('+','+');
         tokenizer.wordChars('-','-');
         int token = tokenizer.nextToken();
         while ( true ) {
            while (token == StreamTokenizer.TT_EOL) // ignore empty lines
               token = tokenizer.nextToken();  
            if (token == StreamTokenizer.TT_EOF)
               break;
            double[] row = new double[cols];
            for (int i = 0; i < cols; i++) {
               if (token == StreamTokenizer.TT_EOL || token == StreamTokenizer.TT_EOF)
                  row[i] = Double.NaN;
               else if (token == StreamTokenizer.TT_WORD) {
                  if (tokenizer.sval.equalsIgnoreCase("undefined"))
                     row[i] = Double.NaN;
                  else {
                     try {
                        Double d = Double.valueOf(tokenizer.sval);
                        row[i] = d.doubleValue();
                     }
                     catch (NumberFormatException e) {
                        throw new IOException("Illegal non-numeric data (" +
                                          tokenizer.sval + ") encountered.");
                     }
                  }
                  token = tokenizer.nextToken();
               }
               else
                  throw new IOException("Illegal non-numeric data encountered.");
            }
            newRows.addElement(row);
            while (token != StreamTokenizer.TT_EOL && token != StreamTokenizer.TT_EOF)
               token = tokenizer.nextToken();
         }
         if (rows.size() == 0)
            throw new IOException("Empty data was found.");
      }
      catch (Exception e) {
         if (throwErrors)
            throw new JCMError("Error while reading data:  " + e, this);
         return false;
      }
      canvas.setActive(0,0);
      rows = newRows;
      rowStrings = new Vector();
      for (int i = 0; i < rows.size(); i++) {
         String[] s = new String[cols];
         double[] d = (double[])rows.elementAt(i);
         for (int col = 0; col < cols; col++)
            if (Double.isNaN(d[col]))
               s[col] = null;
            else
               s[col] = NumUtils.realToString(d[col]);
         rowStrings.addElement(s);
      }
      canvas.input.setText( ((String[])rowStrings.elementAt(0))[0] );
      if (canvas.hScroll != null)
         canvas.hScroll.setValue(0);
      canvas.vScroll.setValue(0);
      canvas.checkScroll();
      canvas.repaint();
      if (canvas.rowLabelCanvas != null)
         canvas.rowLabelCanvas.repaint();
      if (canvas.columnLabelCanvas != null)
         canvas.columnLabelCanvas.repaint();
      serialNumber++;
      return true;
   }

   //------------------------------ private nested classes -------------------------------
   
   private class InputBox extends TextField {
        // An object of type InputBox is used for user input
        // of numbers in the table.  There is only one input box
        // and it moves around.
      InputBox() {
         super(12);
         setBackground(Color.white);
         setForeground(Color.black);
         enableEvents(AWTEvent.KEY_EVENT_MASK + AWTEvent.MOUSE_EVENT_MASK);
      }
      public void processKeyEvent(KeyEvent evt) {
         if (evt.getID() == KeyEvent.KEY_PRESSED) {
            int ch = evt.getKeyCode();
            char chr = evt.getKeyChar();
            boolean use = (chr != 0 && Character.isDigit(chr) || chr == '.' || chr == 'E'
                               || chr == '-' || chr == '+'
                               || chr == 'e') || ch == KeyEvent.VK_DELETE ||
                               ch == KeyEvent.VK_BACK_SPACE;
            boolean useControl = use || chr == 0;
            if (!useControl || ch == KeyEvent.VK_ENTER || ch == KeyEvent.VK_DOWN
                    || ch == KeyEvent.VK_UP || ch == KeyEvent.VK_TAB) {
               if (ch == KeyEvent.VK_ENTER || ch == KeyEvent.VK_DOWN)
                  canvas.doRowDown();
               else if (ch == KeyEvent.VK_UP)
                  canvas.doRowUp();
               else if (ch == KeyEvent.VK_TAB)
                  canvas.doColumnRight();
               /* else
                  Toolkit.getDefaultToolkit().beep(); */
               evt.consume();
            }
            else if (ch == KeyEvent.VK_LEFT && getCaretPosition() == 0) {
               canvas.doColumnLeft();
               evt.consume();
            }
            else if (ch == KeyEvent.VK_RIGHT && getCaretPosition() == getText().length()) {
               canvas.doColumnRight();
               evt.consume();
            }
         }
         super.processKeyEvent(evt);
      }
      public void processMouseEvent(MouseEvent evt) {
         if (evt.getID() == MouseEvent.MOUSE_PRESSED)
            canvas.ensureActiveVisible();
         super.processMouseEvent(evt);
      }      
   } // end nested class InputBox
   
      
   private class DisplayPanel extends Panel 
         implements TextListener,MouseListener,AdjustmentListener,ComponentListener {
         
            // An object of this class is the actual table seen by the user.
            // The panel itself is just the grid of cells.  The row and column
            // labels and the scroll bars are variables in the DisplayPanel
            // ojbect, but they are added to the containing Panel in the 
            // constructor for DataTableInput above.
            

      InputBox input;  // Text field for user input, from nested class defined above.
      
      int activeRow = 0, activeColumn = 0;  // Where the Input box is.
      
      int rowHeight=-1, columnWidth;  // Size of each cell.  rowHeight = -1 indiacates
                                      // the size is not yet known.

      Scrollbar hScroll,vScroll;  // for scrolling through the grid of cells.

      Canvas rowLabelCanvas, columnLabelCanvas;  // These canvasses hold the row
                                                 // and column lables and are displayed
                                                 // to the left of and above the grid of cells.
                                                 // They scroll along with the grid.
                                                 // They are null and are enabled by
                                                 // methods setShowRowNumbers and setShowColumnTitles
   
      boolean lastRowAutoAdded;  // True if last row was auto added.
                                    // If the user leaves this row while 
                                    // it is still empty, it will be auto deleted.
                                    // Empty rows added with the addRows() method
                                    // are not deleted in this way.

      DisplayPanel() {
         setBackground(cellBackground);
         input = new InputBox();
         vScroll = new Scrollbar(Scrollbar.VERTICAL);
         vScroll.setBackground(Color.lightGray);
         input.addTextListener(this);
         vScroll.addAdjustmentListener(this);
         addMouseListener(this);
         setLayout(null);
         add(input);
         addComponentListener(this);
      }
      
      void makeRowLabelCanvas() {
         rowLabelCanvas = new Canvas() {  // canvas for showing row labels
            public void paint(Graphics g) {
               int topRow = vScroll.getValue() / rowHeight;
               int rowCt = getSize().height / rowHeight + 1;
               int tableRows = rows.size();
               FontMetrics fm = g.getFontMetrics();
               int textOffset = (rowHeight + fm.getAscent()) / 2;
               int vScrollVal = vScroll.getValue();
               for (int i = topRow; i < rowCt + topRow && i < tableRows; i++) {
                  String rs = "" + (i+1);
                  int os = (getSize().width - fm.stringWidth(rs)) / 2;
                  g.drawString(rs,os,textOffset + rowHeight*i - vScrollVal);            
               }
            }
            public Dimension getPreferredSize() {
               return new Dimension(35,50);
            }
         };
         rowLabelCanvas.setBackground(labelBackground);
      }
      
      void makeColumnLabelCanvas() {
         columnLabelCanvas = new Canvas() {  // canvas for showing column labels
            public void paint(Graphics g) {
               int leftColumn = 0;
               if (hScroll != null)
                  leftColumn = hScroll.getValue() / columnWidth;
               int blank = (rowLabelCanvas == null)? 0 : 35;  // width of rowLabelCanvas
               int columnCt = (getSize().width-blank) / columnWidth + 1;
               FontMetrics fm = g.getFontMetrics();
               int textOffset = (getSize().height + fm.getAscent()) / 2;
               int hScrollVal = hScroll == null ? 0 : hScroll.getValue();
               for (int i = leftColumn; i < leftColumn + columnCt && i < columnCount; i++) {
                  String s = getColumnName(i);
                  int os = (columnWidth - fm.stringWidth(s)) / 2;
                  g.drawString(s,blank + i*columnWidth + os - hScrollVal,textOffset);            
               }
               g.setColor(Color.gray);
               g.fillRect(0,0,blank,getSize().height);
            }
            public Dimension getPreferredSize() {
               return new Dimension(50,20);
            }
         };
         columnLabelCanvas.setBackground(labelBackground);
      }
      
      public void addNotify() {
           // Determine the size of a cell, which is based on the preferred size
           // of the input box (which can't be determined until after the peer
           // is added.  (I hope this works on all platforms!)
         super.addNotify();
         if (rowHeight != -1)
            return;
         Dimension size = input.getPreferredSize();
         rowHeight = size.height-1;
         columnWidth = size.width-1;
         input.setBounds(1,1,columnWidth+1,rowHeight+1);
      }
      
      public void update(Graphics g) {
           // Don't fill in with background before painting.
         paint(g);
      }
      
      public void paint(Graphics g) {
              // Draw the grid of cells, in position based on scroll bar values.
         int hScrollVal = (hScroll == null)? 0 : hScroll.getValue();
         int vScrollVal = vScroll.getValue();
         int width = getSize().width;    // width and height of component
         int height = getSize().height;
         int tableWidth = columnCount*columnWidth + 2;
         int tableHeight = rows.size()*rowHeight + 2;
         int tableRows = rows.size();   // Number of rows in table
         Rectangle clip = g.getClipBounds();  // Try to avoid uncessary painting by checking clip rect.
         int topRow, rowCt, leftColumn, columnCt;
         if (clip != null) { // change data about included rows, columns
            topRow = (vScrollVal + clip.y) / rowHeight;
            rowCt = clip.height / rowHeight + 1;
            leftColumn = (hScrollVal + clip.x) / columnWidth;
            columnCt = clip.width / columnWidth + 1;
         }
         else {
            topRow = vScrollVal / rowHeight;      // top visible row
            rowCt = height / rowHeight + 1;               // num of rows possibly visible
            leftColumn = hScrollVal / columnWidth;// leftmost visible column
            columnCt = width / columnWidth + 1;           // num of columns visible
         }
         FontMetrics fm = g.getFontMetrics();
         int textOffset = (rowHeight + fm.getAscent()) / 2; // from top of box to text baseline
         for (int i = topRow; i < topRow + rowCt && i < tableRows; i++ ) {
            String[] contents = (String[])rowStrings.elementAt(i);
            for (int c = 0; c < columnCount; c++)
               if (c != activeColumn || i != activeRow) {
                  g.setColor(cellBackground);
                  g.fillRect(1+c*columnWidth-hScrollVal,1+i*rowHeight-vScrollVal,columnWidth,rowHeight);
                  g.setColor(getForeground());
                  if (contents != null && contents[c] != null && contents[c].length() > 0) {
                     String s = contents[c];
                     g.drawString(s,c*columnWidth + 5 - hScrollVal, textOffset + i*rowHeight - vScrollVal);
                  }
               }
         }
         if (width > tableWidth) {
            g.setColor(blankBackground);
            g.fillRect(tableWidth,0,width-tableWidth,height);
         }
         if (height > tableHeight) {
            g.setColor(blankBackground);
            g.fillRect(0,tableHeight,width,height-tableHeight);
         }
         g.setColor(gridColor);
         g.drawRect(0,0,tableWidth, tableHeight);
         g.drawRect(1,1, tableWidth - 2, tableHeight - 2);
         for (int i =  - 1; i < topRow + rowCt && i < tableRows; i++)
            g.drawLine(0, 1 + (i+1)*rowHeight - vScrollVal, tableWidth - 1, 1 + (i+1)*rowHeight - vScrollVal);
         for (int j = 0; j <= columnCount; j++)
            g.drawLine(1 + j*columnWidth - hScrollVal, 0, 1 + j*columnWidth - hScrollVal, tableHeight - 1);
      }
      
      void setActive(int row, int column) {
            // Move the input box to the specified row and column, and
            // move the focus to the input box.
         if (row != activeRow || column != columnWidth) {
            int topOffset = vScroll.getValue();
            int leftOffset = (hScroll == null)? 0 : hScroll.getValue();
            int y = -topOffset + row*rowHeight + 1;
            int x = -leftOffset + column*columnWidth + 1;
            input.setLocation(x,y);
            activeRow = row;
            activeColumn = column;
            String[] contents = (String[])rowStrings.elementAt(activeRow);
            if (contents == null || contents[activeColumn] == null)
               input.setText("");
            else
               input.setText(contents[activeColumn]);
         }
         ensureActiveVisible();
         input.selectAll();
         input.requestFocus();
      }
      
      void doRowDown() {
            // Move active box down one row.  Create a new row if in the
            // last row, that row is non-empty, and autoAddRows is true.
         int tableRows = rows.size();
         if (activeRow == tableRows - 1 && autoAddRows && rows.elementAt(tableRows-1) != null) {
            addRows(1,tableRows);
            lastRowAutoAdded = true;
         }
         if (activeRow < rows.size() - 1)
            setActive(activeRow+1,activeColumn);
         else {
           ensureActiveVisible();
           input.requestFocus();
         }
      }
      
      void doRowUp() {
            // Move up one row.  If leaving an empty row that was autoadded, delete it.
         if (activeRow == 0)
            return;
         setActive(activeRow - 1, activeColumn);
         if (autoAddRows && lastRowAutoAdded == true && activeRow == rows.size() - 2 && rows.elementAt(activeRow+1) == null) {
               // delete empty row from bottom of table
            rows.removeElementAt(rows.size() - 1);
            rowStrings.removeElementAt(rowStrings.size() - 1);
            checkScroll();
            repaint();
            if (rowLabelCanvas != null)
               rowLabelCanvas.repaint();
            input.requestFocus();
         }
         lastRowAutoAdded = false;
      }
      
      void doColumnRight() {
            // Move active box right to the next column, possibly
            // wrapping around to the first column.
         int c = activeColumn + 1;
         if (c >= columnCount)
           c = 0;
         setActive(activeRow,c);
      }
      
      void doColumnLeft() {
            // Move active box left to the next column, possibly
            // wrapping around to the last column.
         int c = activeColumn - 1;
         if (c < 0)
           c = columnCount - 1;
         setActive(activeRow,c);
      }
      
      void ensureActiveVisible() {
            // Make sure that the entire input box is visible.
         int x = columnWidth*activeColumn + 1;
         int y = rowHeight*activeRow + 1;
         int visibleLeft = (hScroll == null)? 0 : hScroll.getValue();
         int visibleTop = vScroll.getValue();
         int visibleRight = visibleLeft + getSize().width;
         int visibleBottom = visibleTop + getSize().height;
         int offsetX = 0;
         int offsetY = 0;
         if (x + columnWidth > visibleRight)
            offsetX = -(x + columnWidth - visibleRight);
         if (x < visibleLeft)
            offsetX = visibleLeft - x;
         if (y + rowHeight > visibleBottom)
            offsetY = -(y + rowHeight - visibleBottom);
         if (y < visibleTop)
            offsetY = visibleTop - y;
         if (offsetX == 0 && offsetY == 0)
            return;
         if (offsetX != 0) {
            if (hScroll != null)
               hScroll.setValue(visibleLeft - offsetX);
            if (columnLabelCanvas != null)
               columnLabelCanvas.repaint();
         }
         if (offsetY != 0) {
            vScroll.setValue(visibleTop - offsetY);
            if (rowLabelCanvas != null)
               rowLabelCanvas.repaint();
         }
         input.setLocation(x - ( hScroll == null ? 0 : hScroll.getValue() ),y - vScroll.getValue());
         repaint();
      }
      
      void addRows(int num, int before) {
            // Add specified number of rows to table, before row
            // number before.  If before is after the last existing
            // row, the rows are added at the end of the table.
         serialNumber++;
         if (num <= 0)
            return;
         if (before >= rows.size()) {
            for (int i = 0; i < num; i++) {
               rows.addElement(null);
               rowStrings.addElement(null);
               lastRowAutoAdded = false;
            }
         }
         else {
            if (before < 0)
               before = 0;
            for (int i = 0; i < num; i++) {
               rows.insertElementAt(null,before);
               rowStrings.insertElementAt(null,before);
            }
            if (activeRow >= before) { // data in active cell changes
               String[] vals = (String[])rowStrings.elementAt(activeRow);
               if (vals == null || vals[activeColumn] == null)
                  input.setText("");
               else
                  input.setText(vals[activeColumn]);
            }
         }
         checkScroll();
         repaint();
         if (rowLabelCanvas != null)
            rowLabelCanvas.repaint();
      }
            
      void setValue(int column, int row, double value) {
            // set the value in the column at given row and column.
            // Note that row numbers start at 1 in this method!!
            // Row and column numbers are assumed to be in legal range!!
         String num = NumUtils.realToString(value);
         setRowData(row-1,column,num,value);
         if (column == activeColumn && row-1 == activeRow)
            input.setText(num);
         else {
            repaintItem(row-1,column);
         }
      }
      
      double getValue(int column, int row) {
            // Get the value from the table in the given row and column.
            // Note that row numbers start at 1 in this method!!
            // Row and column numbers are assumed to be in legal range!!
         if (rows.elementAt(row-1) == null)
            return emptyCellValue;
         else {
            double d = ((double[])rows.elementAt(row-1))[column];
            if ( ! Double.isNaN(d) )
               return d;
            else {
               String val = ((String[])rowStrings.elementAt(row-1))[column];
               if (val == null || val.length() == 0)
                  return emptyCellValue;
               else  // val is "bad input", the only other possibility if d is NaN
                  if (throwErrors)
                     throw new JCMError("Invalid numerical input in data table, column \""
                                        + getColumnName(column) + "\", row " + row  + ".", this);
                  else
                     return Double.NaN;
            }
         }
      }
      
      public void textValueChanged(TextEvent txt) {
             // From TextListener interface.  When text in input box changes,
             // change the stored string and stored value for that position to match.
         String num = input.getText().trim();
         if (num.length() == 0)
            setRowData(activeRow,activeColumn,num,Double.NaN);
         else {
            double x;
            try {
               Double d = Double.valueOf(num);
               x = d.doubleValue();
            }
            catch (NumberFormatException e) {
               x = Double.NaN;
            }
            if (Double.isNaN(x))
               setRowData(activeRow,activeColumn,"bad input",x);
            else
               setRowData(activeRow,activeColumn,num,x);
         }
      }
      
      void setRowData(int row, int col, String num, double val) {
            // puts num, val into rows, rowStrings vectors at position row,col
            // Empty rows are always represented by null's in the rowVals
            // and rowStrings vectores.  This requires a bit of care.
         serialNumber++;
         double[] rowVals = (double[])rows.elementAt(row);
         String[] rowStr  = (String[])rowStrings.elementAt(row); 
         if (num.length() == 0) {
            if (rowStr == null || rowStr[col] == null)
               return;
            rowStr[col] = null;
            rowVals[col] = Double.NaN;
            boolean empty = true;
            for (int i = 0; i < rowStr.length; i++)
               if (rowStr[i] != null) {
                  empty = false;
                  break;
               }
            if (empty) {
               rows.setElementAt(null,row);
               rowStrings.setElementAt(null,row);
            }
         }
         else {
            if (num.length() > 12)
               num = NumUtils.realToString(val,12);
            if (rowStr == null) {
               int ct = columnCount;
               rowVals = new double[ct];
               rowStr = new String[ct];
               for (int i = 0; i < ct; i++)
                  rowVals[i] = Double.NaN;
               rows.setElementAt(rowVals,row);
               rowStrings.setElementAt(rowStr,row);
            }
            rowStr[col] = num;
            rowVals[col] = val;
         }
      }
      
      protected void repaintItem(int row, int column) {
            // forces a repaint for just the specified cell
         int y = row*rowHeight - vScroll.getValue();
         int x = column*columnWidth;
         if (hScroll != null)
            x -= hScroll.getValue();
         repaint(x+1,y+1,columnWidth-1,rowHeight-1);
      }
      
      public void adjustmentValueChanged(AdjustmentEvent evt) {
            // From the AdjustmentListener interface.  React to
            // change in scroll positions.
         repaint();
         if (evt.getSource() == vScroll) {
            if (rowLabelCanvas != null)
               rowLabelCanvas.repaint();
         }
         else {
            if (columnLabelCanvas != null) 
               columnLabelCanvas.repaint();
         }
         int x = columnWidth*activeColumn + 1;
         if (hScroll != null)
            x -= hScroll.getValue();
         int y = rowHeight*activeRow + 1 - vScroll.getValue();
         input.setLocation(x,y);
      }

      public void mousePressed(MouseEvent evt) {
            // From the MouseListener interface.  Move the active
            // cell to an input cell close to mouse click. 
         int hOffset = (hScroll == null)? 0 : hScroll.getValue();
         int vOffset = vScroll.getValue();
         int row = (evt.getY() + vOffset - 1) / rowHeight;
         if (row < 0)
            row = 0;
         else if (row >= rows.size())
            row = rows.size() - 1;
         int col = (evt.getX() + hOffset - 1) / columnWidth;
         if (col < 0)
            col = 0;
         else if (col >= columnCount)
            col = columnCount - 1;
         if (row == activeRow && col == activeColumn)
            ensureActiveVisible();  // want to avoid doing selectAll if box doesn't move
         else
            setActive (row,col);
         int emptyRow = rows.size() - 1;
         if (!lastRowAutoAdded  || emptyRow == row)
            return;
         lastRowAutoAdded = false;
         if (rows.elementAt(emptyRow) != null)
            return;
         rows.removeElementAt(emptyRow);
         rowStrings.removeElementAt(emptyRow);
         checkScroll();
         repaint();
         if (rowLabelCanvas != null)
            rowLabelCanvas.repaint();
      }
      
      public void componentResized(ComponentEvent evt) {
           // From ComponentListener interface.  Fix scroll bars after resize.
         checkScroll();
      }
      
      void checkScroll() {
            // Make sure scoll bars are OK after resize, adding rows, etc.
         int width = DisplayPanel.this.getSize().width;
         int height = DisplayPanel.this.getSize().height;
         if (rowHeight == -1 || width <= 1)
            return;
         int tableWidth = columnWidth*columnCount + 2;
         int tableHeight = rowHeight*rows.size() + 2;
         int oldTop = vScroll.getValue();
         int oldLeft = (hScroll == null)? 0 : hScroll.getValue();
         boolean revalidate = false;
         if (width >= tableWidth - 2) {
            if (hScroll != null) {
               int scrollHeight = hScroll.getPreferredSize().height;
               DataTableInput.this.remove(hScroll);
               hScroll = null;
               height += scrollHeight;
               revalidate = true;
            }
         }
         else {
            if (hScroll == null) {
               hScroll = new Scrollbar(Scrollbar.HORIZONTAL);
               hScroll.setBackground(Color.lightGray);
               int scrollHeight = hScroll.getPreferredSize().height;
               height -= scrollHeight;
               DataTableInput.this.add(hScroll, BorderLayout.SOUTH);
               hScroll.addAdjustmentListener(this);
               revalidate = true;
            }
            if (oldLeft > tableWidth - width)
               hScroll.setValues(tableWidth-width,width,0,tableWidth);
            else
               hScroll.setValues(oldLeft,width,0,tableWidth);
            hScroll.setUnitIncrement(columnWidth / 4);
            if (width > 1)
               hScroll.setBlockIncrement((3*width)/4);
         }
         if (height >= tableHeight - 2) {
            vScroll.setEnabled(false);
            vScroll.setValues(0,1,0,1);
         }
         else {
            if (oldTop > tableHeight - height)
               vScroll.setValues(tableHeight-height,height,0,tableHeight);
            else
               vScroll.setValues(oldTop,height,0,tableHeight);
            vScroll.setUnitIncrement(rowHeight);
            if (height > 1)
               vScroll.setBlockIncrement((3*height)/4);
            vScroll.setEnabled(true);
         }
         int x = columnWidth*activeColumn + 1;
         if (hScroll != null)
            x -= hScroll.getValue();
         int y = rowHeight*activeRow + 1 - vScroll.getValue();   
         input.setLocation(x,y);
         if (revalidate)
            DataTableInput.this.validate();
      }  
      
      public Dimension getPreferredSize() {
         if (rowHeight == -1)
            return new Dimension(350,200);
         else if (columnCount >= 4)
            return new Dimension(4*columnWidth+2,6*rowHeight+2);
         else
            return new Dimension(columnCount*columnWidth+2,6*rowHeight+2);
      }

      public void mouseEntered(MouseEvent evt) {}  // Other methods from listener interfaces.
      public void mouseExited(MouseEvent evt) {}
      public void mouseClicked(MouseEvent evt) {}
      public void mouseReleased(MouseEvent evt) {}
      public void componentHidden(ComponentEvent evt) {}
      public void componentShown(ComponentEvent evt) {}
      public void componentMoved(ComponentEvent evt) {}
      
   }  // end nested class Display Panel
   
   
   private class DTEC implements ExpressionCommand {
   
        // This is used in the doParse() method.  When a reference to a DataInputTable
        // is found by a parser, the doParse() method will add an object of this
        // type to the ExpressionProgram that the parseris producing.  A DTEC represents
        // a sub-exprssion such as "data.A(3)" or "data.sum(A^2)"
   
      ExpressionProgram prog;  // The expression inside the parentheses

      int command;  // The column number for an expression such as "data.A(3)" which
                    // represents the value of a cell in column "A".  For the sum
                    // function, command is -1.  For the count funtion, it is -2.
   
      DTEC(int command, ExpressionProgram prog) {
         this.command = command;
         this.prog = prog;
      }
      
      public void apply(StackOfDouble stack, Cases cases) {
         if (command >= 0) {  // Reference to a column.  Value of prog gives row number.
            double loc = prog.getVal();
            if (Double.isNaN(loc) || loc < 0.5 || loc >= rows.size() + 0.5)
               stack.push(Double.NaN);
            else
               stack.push(canvas.getValue(command,(int)(loc+0.5)));
         }
         else if (command == -1) {  // sum of the prog expression for all rows in the table.
            double sum = 0;
            int top = getNonEmptyRowCount();
            for (int row = 1; row <= top; row++) {
               setCurrentRowNumber(row);
               sum += prog.getVal();
            }
            stack.push(sum);
         }
         else if (command == -2) {  // the count of rows in the table
            stack.push(getNonEmptyRowCount());
         }
      }
      
      public void compileDerivative(ExpressionProgram prog, int myIndex, ExpressionProgram deriv, Variable wrt) {
         if (command != -1) {
            deriv.addConstant(0);
         }
         else {
            ExpressionProgram d = (ExpressionProgram)this.prog.derivative(wrt);
            deriv.addCommandObject( new DTEC(command,d) );
         }
      }
      
      public int extent(ExpressionProgram prog, int myIndex) {
         return 1;
      }
      
      public boolean dependsOn(Variable x) {
         if (command == -2)
            return false;
         else
            return prog.dependsOn(x);
      }

      public void appendOutputString(ExpressionProgram prog, int myIndex, StringBuffer buffer) {
         buffer.append(getName());
         buffer.append('.');
         if (command == -2)
            buffer.append("count");
         else if (command == -1)
            buffer.append("sum");
         else
            buffer.append(getColumnName(command));
         buffer.append("(");
         if (command != -2)
            buffer.append(this.prog.toString());
         buffer.append(")");
      }
      
   }  // end nested class DTEC
   
} // end class DataTableInput
