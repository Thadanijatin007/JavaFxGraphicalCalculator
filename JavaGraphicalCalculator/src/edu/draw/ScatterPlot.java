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

import java.awt.*;

import edu.awt.Computable;
import edu.awt.DataTableInput;
import edu.awt.JCMError;
import edu.data.Expression;
import edu.data.Value;
import edu.data.*;
import edu.awt.*;


/**
 *  A ScatterPlot graphs data taken from a DataTableInput.  The data
 *  in the form of a small plus sign at each (x,y) in the data.
 *  The x and y values can be taken directly from two specified
 *  columns in the table.  They can also be computed by expressions
 *  that can use column names from the table as well as the special
 *  variable rowNumber.  For example, if column names are X and Y,
 *  then it could plot sqrt(X) versus rowNumber*(X+Y).
 *  <p>Optionally, a ScatterPlot will also draw a regression line
 *  for the data.  Certain statistical values about the data points
 *  are available as Value objects by calling the getValueObject()
 *  method.
 */

public class ScatterPlot extends Drawable implements Computable {

   /**
    *  A constant that can be used in the getValueObject() method to
    *  indicate which statistic the object should represent.
    */
   public static final int INTERCEPT = 0, SLOPE = 1, DATACT = 2, MISSINGCT = 3,
                              STANDARDERROR = 4, CORRELATION = 5;
   
   private DataTableInput table;  // The table from which the data for the plot is taken
   private long lastTableSN; // serial number from table when getData() was last done
   
   private boolean autoChangeLimits = true;  // If true, then the limits on the coords will
                                             // be changed so that the data pretty much
                                             // fills the coordinate rect.
   
   private int column1, column2;   // Column numbers that specify which columns from
                                   // the table will be plotted.  These are ignored
                                   // if exp1 and exp2 are non-null.

   private Expression exp1, exp2;  // Expressions that give data to be plotted, or
                                   // null if column numbers are to be used.

   private boolean showRegressionLine = true;  // If true, a regression is drawn
   
   private boolean missingValueIsError = true;  // If true and if any of the data values is Double.NaN,
                                               // then an error is thrown.
   
   private double slope=Double.NaN;   // Values of statistics.
   private double intercept=Double.NaN;
   private int dataCt;
   private int missingCt;
   private double correlation=Double.NaN;
   private double standardError=Double.NaN;
   
   private double[][] data;  // The actual data values to be drawn (computed in getData())
   
   private Color lineColor = Color.black;  // Color of regression line.
   private Color dataColor = Color.red;    // Color of data points.
   
   private static final int crossHalfSize = 2;  // Size of one arm of the plus sign that is drawn
                                                // to represent a data point.
   
   /**
    *  Default constructor.  A data table, at least, must be specified before anything can be drawn.
    *  The first two columns of the table will be plotted (once a table is specified).
    */
   public ScatterPlot() {
      this(null,0,1);
   }
   
   /**
    *  Create a scatter plot to plot data from the specified table.  Initially, it is configured
    *  to plot data from the first two columns in the table.
    */
   public ScatterPlot(DataTableInput table) {
      this(table,0,1);
   }
   
   /**
    *  Create a scatter plot to plot data from two specified columns in a table.
    *  Columns are numbered starting from zero.
    */
   public ScatterPlot(DataTableInput table, int column1, int column2) {
      this.table = table;
      this.column1 = column1;
      this.column2 = column2;
   }
   
   /**
    *  Create  scatter plot to plot specified expressions using data from a table.
    *  The expressions should include references to the column names from the table
    *  and can also refer to the special variable "rowNumber".
    */
   public ScatterPlot(DataTableInput table, Expression exp1, Expression exp2) {
      this.table = table;
      this.exp1 = exp1;
      this.exp2 = exp2;
      column1 = 0;
      column2 = 1;
   }
   
   /**
    *  Specify the table from which the plotted data is to be taken.  The data from the
    *  first two columns of the table will be plotted, unless this is changed by
    *  calling setColumns() or setExpressions().
    */
   public void setTable(DataTableInput table) {
      if (table == this.table)
         return;
      this.table = table;
      lastTableSN = 0;
      column1 = 0;
      column2 = 1;
      checkData();
   }
   
   /**
    *  Get the DataTableInput from which the plotted data is obtained.
    */
   public DataTableInput getTable() {
      return table;
   }
   
   /**
    *  Specify that the data to be plotted should be taken from the specified
    *  columns in the table.  Note that columns are numbered starting from zero.
    *  The parameters must be within the range of column numbers in the table.
    */
   public void setColumns(int c1, int c2) {
      column1 = c1;
      column2 = c2;
      exp1 = exp2 = null;
      lastTableSN = 0;  // force checkData to recompute
      checkData();
   }
   
   /**
    *  Specify the data for the the plot is to be obtained by evaluating
    *  the two expressions that are given as parameters.  Both expressions
    *  should be non-null.  The expressions can only be created by a Parser
    *  to which the variables from the table have been added by calling
    *  the method DataTableInput.addVariablesToParser().  The expressions
    *  are evaluated once for each row in the table to obtain the data to be ploted.  They can include
    *  references to the column names from the table and to the special
    *  variable "rowNumber", which represents the number of the current row.
    */
   public void setExpressions(Expression exp1, Expression exp2) {
      this.exp1 = exp1;
      this.exp2 = exp2;
      lastTableSN = 0;  // force checkData to recompute
      checkData();
   }   
   
   /**
    *  If the parameter is true, then a regression line for the data is drawn.
    *  The default value is true.
    */
   public void setShowRegressionLine(boolean line) {
      if (line != showRegressionLine) {
         showRegressionLine = line;
         needsRedraw();
      }
   }
   
   /**
    *  The return value tells whether a regression line is drawn.
    */
   public boolean getShowRegressionLine() {
      return showRegressionLine;
   }
   
   /**
    *  If the parameter is true, then a missing data value (an empty cell or
    *  an undefined value for one of the expressions) is considered to be an
    *  error, and a JCMError is thrown when it is encountered.  If the value is
    *  false, missing data are ignored, and the value of MISSINGCT gives the 
    *  number of points for which the data was missing.  Note that invalid 
    *  data (a cell that contains text that does not represent a number) is 
    *  always considered to be an error.  Also note that completely empty
    *  rows at the bottom of a DataTableInput are always ignored and are
    *  never considered to be an error.
    *  The default value of missingValueIsError is true, so that missing data
    *  is considered to be an error unless you turn off this option.
    */ 
   public void setMissingValueIsError(boolean isError) {
      missingValueIsError = isError;
   }

   /**
    *  The return value indicates whether missing data is considered to be
    *  an error.
    */   
   public boolean getMissingValueIsError() {
      return missingValueIsError;
   }
   
   /**
    *  If the parameter is true, then the limits on the CoordinateRect that contains
    *  this ScatterPlot are automatically adjusted whenever the data is recomputed.
    *  The default value is true.
    */ 
   public void setAutoChangeLimits(boolean set) {
      autoChangeLimits = set;
   }

   /**
    *  The return value indicates whether the limits on the CoordinateRect are
    *  automatically adjusted when the data is recomputed.
    */   
   public boolean getAutoChangeLimits() {
      return autoChangeLimits;
   }
   
   /**
    *  Get the color that is used to draw the points in the data.
    */
   public Color getDataColor() {
      return dataColor;
   }
   
   /**
    *  Set the color that is used to draw the points in the data.
    *  The default value is red.
    */
   public void setDataColor(Color color) {
      if (color != null)
         dataColor = color;
   }
   
   /**
    * Get the color that is used to draw the regression line. 
    */
   public Color getLineColor() {
      return lineColor;
   }
   
   /**
    *  Set the color that is used to draw the regression line.
    *  The default value is black.
    */
   public void setLineColor(Color color) {
      if (color != null)
         lineColor = color;
   }
   
   /**
    *  Get a Value that represents a statistic about the data that is shown
    *  in the scatter plot.  The parameter specifies which statistic is represented.
    *  It can be one of the constants defined in this class:  SLOPE (of regression line),
    *  INTERCEPT (y-intercept of regression line), DATACT (number of data points),
    *  MISSINGCT (number of missing data; alwasy zero if the missingValueIsError property
    *  is true), STANDARDERROR (standard error of regression line), and CORRELATION
    *  (correlation coefficient between first and second coordintes of data points).
    */
   public Value getValueObject(int valueCode) {
      if (valueCode < 0 || valueCode > 5)
         throw new IllegalArgumentException("Unknown code (" + valueCode + ") for type of value object.");
      return new SPV(valueCode);
   }
   
   /**
    *  Check data from table and recompute everything if it has changed.
    */
   private void checkData() {
      if (table != null && lastTableSN == table.getSerialNumber())
         return;
      try {
         compute();
      }
      catch (JCMError e) {
         canvas.setErrorMessage(null,e.getMessage());
      }
   }
   
   /**
    *  Recompute the data for the scatter plot.  This is generally
    *  not called directly.
    */
   public void compute() {
      double[] desiredLimits = getData();
      if (table != null)
         lastTableSN = table.getSerialNumber();
      if (desiredLimits == null || !needsNewLimits(desiredLimits,coords))
         needsRedraw();
      else
         coords.setLimits(desiredLimits);
   }
   
   /**
    *  Draw the data points and regression line.  Not meant to be called directly.
    */
   public void draw(Graphics g, boolean coordsChanged) {
      g.setColor(dataColor);
      if (table == null) {
         g.drawString("No table has been specified.",20,27);
         return;
      }
      if (column1 < 0 || column1 >= table.getColumnCount() 
                   || column2 < 0 || column2 >= table.getColumnCount()) {
         g.drawString("Illegal column numbers.",20,27);
         return;
      }
      if (data == null || data.length == 0) {
         g.drawString("No data available.",20,27);
         return;
      }
      checkData();
      for (int i = 0; i < data.length; i++) {
         int x = coords.xToPixel(data[i][0]);
         int y = coords.yToPixel(data[i][1]);
         g.drawLine(x-crossHalfSize,y,x+crossHalfSize,y);
         g.drawLine(x,y-crossHalfSize,x,y+crossHalfSize);
      }
      if (showRegressionLine && !Double.isNaN(slope)) {
         g.setColor(lineColor);
         if (Double.isInfinite(slope)) {
            int x = coords.xToPixel(data[0][0]);
            g.drawLine(x,coords.getTop(),x,coords.getTop()+coords.getHeight());
         }
         else {
            double x1 = coords.pixelToX(coords.getLeft());
            double x2 = coords.pixelToX(coords.getLeft()+coords.getWidth());
            double y1 = slope*x1 + intercept;
            double y2 = slope*x2 + intercept;
            g.drawLine(coords.xToPixel(x1), coords.yToPixel(y1)-1,
                          coords.xToPixel(x2), coords.yToPixel(y2)-1);
         }
      }
   }
   
   /**
    *  Get the data for the plot, and recompute the statistics.
    *  Also, compute the appropriate limits for the CoordinateRect.
    *  The return value represents these limits.
    */
   private double[] getData() {
      int rows = (table == null)? 0 : table.getNonEmptyRowCount();
      double[] desiredLimits = null;
      if ( table == null || rows == 0 || ( (exp1 == null || exp2 == null) && 
                               (column1 < 0 || column1 >= table.getColumnCount() ||
                                  column2 < 0 || column2 >= table.getColumnCount()) ) ) {
         data = new double[0][2];
         dataCt = 0;
         missingCt = 0;
         slope = Double.NaN;
         intercept = Double.NaN;
         correlation = Double.NaN;
         standardError = Double.NaN;
         return null;
      }
      data = new double[rows][2];
      dataCt = 0;
      missingCt= 0;
      if (exp1 == null || exp2 == null) {
         for (int i = 0; i < rows; i++) {
            double x = table.getCellContents(i+1,column1);
            double y = table.getCellContents(i+1,column2);
            if (Double.isNaN(x) || Double.isNaN(y) || Double.isInfinite(x) || Double.isInfinite(y)) {
               if (missingValueIsError)
                  throw new JCMError("Missing data in row " + table.getCurrentRowNumber() + " of table.", this);  
               missingCt++;
            }
            else {
               data[dataCt][0] = x;
               data[dataCt][1] = y;
               dataCt++;
            }
         }
      }
      else {
         for (int i = 0; i < rows; i++) {
            table.setCurrentRowNumber(i+1);
            double x = exp1.getVal();
            double y = exp2.getVal();
            if (Double.isNaN(x) || Double.isNaN(y) || Double.isInfinite(x) || Double.isInfinite(y)) {
               if (missingValueIsError)
                  throw new JCMError("Missing data or undefined expression value for row " + table.getCurrentRowNumber() + " of table.", this);  
               missingCt++;
            }
            else {
               data[dataCt][0] = x;
               data[dataCt][1] = y;
               dataCt++;
            }
         }
      }
      if (dataCt < data.length) {
         double[][] d = new double[dataCt][2];
         for (int i = 0; i < dataCt; i++)
            d[i] = data[i];
         data = d;
      }
      getRegressionStats();
      if (autoChangeLimits)
         desiredLimits = computeDesiredLimits();
      return desiredLimits;
   }
   
   private void getRegressionStats() {
        // Compute statistics, based on data in data array.
      if (dataCt == 0) {
         slope = intercept = correlation = standardError = Double.NaN;
         return;
      }
      boolean allSameX = true,
              allSameY = true;
      double sumx = data[0][0], 
             sumy = data[0][1], 
             sumxy = data[0][0]*data[0][1], 
             sumx2 = data[0][0]*data[0][0],
             sumy2 = data[0][1]*data[0][1];
      for (int i = 1; i < dataCt; i++) {
         if (data[0][0] != data[i][0])
            allSameX = false;
         if (data[0][1] != data[i][1])
            allSameY = false;
         sumx += data[i][0];
         sumy += data[i][1];
         sumxy += data[i][0] * data[i][1];
         sumx2 += data[i][0] * data[i][0];
         sumy2 += data[i][1] * data[i][1];
      }
      double denomx = dataCt * sumx2 - sumx*sumx;
      double denomy = dataCt * sumy2 - sumy*sumy;
      double numer = dataCt * sumxy - sumx*sumy;
      if (allSameX && allSameY) {
         slope = 0;
         intercept = data[0][1];
         correlation = standardError = Double.NaN;
      }
      else if (allSameX) {
         slope = Double.POSITIVE_INFINITY;
         intercept = correlation = standardError = Double.NaN;
      }
      else if (denomx == 0) {
         slope = intercept = correlation = standardError = Double.NaN;
      }
      else {
         slope = numer / denomx;
         intercept = (sumy - slope * sumx) / dataCt;
         if (denomy == 0)
            correlation = Double.NaN;
         else
            correlation = numer / Math.sqrt(denomx*denomy);
         if (dataCt <= 2)
            standardError = Double.NaN;
         else {
            double sum = 0;
            for (int i = 0; i < dataCt; i++) {
               double x = data[i][1] - (slope*data[i][0] + intercept);
               sum += x*x;
            }
            standardError = Math.sqrt(sum/(dataCt-2));
         }
      }
   }
   
   private double[] computeDesiredLimits() {
         // Compute desired limits, based on data in data array
      if (data.length == 0)
         return null;
      double xmin=Double.MAX_VALUE, xmax=-Double.MAX_VALUE, 
             ymin=Double.MAX_VALUE, ymax=-Double.MAX_VALUE;
      for (int i = 0; i < dataCt; i++) {
         double x = data[i][0];
         double y = data[i][1];
         if (x > xmax)
            xmax = x;
         if (x < xmin)
            xmin = x;
         if (y > ymax)
            ymax = y;
         if (y < ymin)
            ymin = y;
      }
      if (xmin > 0 && (xmax - xmin) > xmax/2)
         xmin = 0;
      if (ymin > 0 && (ymax - ymin) > ymax/2)
         ymin = 0;
      if (ymax < 0)
         ymax = 0;
      if (xmax < 0)
         xmax = 0;
      if (xmax == xmin) {
         xmax += 1;
         xmin -= 1;
      }
      else {
         double spread = (xmax - xmin) / 15;
         xmax += spread;
         xmin -= spread;
      }
      if (ymax == ymin) {
         ymax += 1;
         ymin -= 1;
      }
      else {
         double spread = (ymax - ymin) / 15;
         ymax += spread;
         ymin -= spread;
      }
      return new double[] { xmin, xmax, ymin, ymax };
   }
   
   private boolean needsNewLimits(double[] desiredLimits, CoordinateRect coords) {
         // Check if limits should actually be changed; avoid changing them if
         // they are close to the desired limits.
       double[] limits = new double[] { coords.getXmin(), coords.getXmax(),
                                        coords.getYmin(), coords.getYmax() } ;
       return (desiredLimits[0] < limits[0]
                   || desiredLimits[1] > limits[1]
                   || desiredLimits[2] < limits[2]
                   || desiredLimits[3] > limits[3]
                   || (limits[1] - limits[0]) > 1.3*(desiredLimits[1] - desiredLimits[0])
                   || (limits[3] - limits[2]) > 1.3*(desiredLimits[3] - desiredLimits[2]) 
                   || (limits[1] - limits[0]) < (desiredLimits[1] - desiredLimits[0]) / 1.3
                   || (limits[3] - limits[2]) < (desiredLimits[3] - desiredLimits[2]) / 1.3
              );
   }
   
   private class SPV implements Value {
        // Represents one of the value objects that can
        // be returned by the getValueObject() method.
      private int code;  // Which statisitic does this Value represent?
      SPV(int code) {
         this.code = code;
      }
      public double getVal() {
         checkData();
         switch (code) {
            case INTERCEPT:   return intercept;
            case SLOPE:       return slope;
            case DATACT:      return dataCt;
            case MISSINGCT:   return missingCt;
            case STANDARDERROR: return standardError;
            default:          return correlation;
         }
      }
   }

}  // end class ScatterPlot
