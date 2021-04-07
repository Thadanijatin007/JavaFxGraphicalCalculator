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

import edu.awt.Computable;
import edu.data.Cases;
import edu.data.Function;

import java.awt.*;
import java.util.Vector;

/**
 * A Graph1D represents the graph of a function of one variable, to be
 * displayed in a given CoordinateRect.  A Graph1D is a Computable.
 * The data for the graph is recomputed when its compute() method is
 * called.  It will also be recomputed, before it is drawn, if the
 * coordinate rect has changed in some way.
 */

public class Graph1D extends Drawable implements Computable {


    private final static int UNDEFINED = 0, ABOVE = 1, BELOW = 2, ONSCREEN = 3;
    private static final int MAX_DEPTH = 10;
    private Function func; //The function that is graphed.
    private Color graphColor = Color.magenta; //Color of the graph.
    //for points where function is undefined.
    private boolean changed; //Used internally to indicate that data has to be recomputed.
    private transient int[] xcoord, ycoord;  //points on graph; xcoord[i] == Integer.MIN_VALUE
    private double absoluteYmax, onscreenymax, absoluteYmin, onscreenymin;
    private final double[] v = new double[1];
    private Cases case1 = new Cases();
    private Cases case2 = new Cases();

    //------------------ Implementation details -----------------------------

    /**
     * Create a Graph1D with no function to graph.  One can be set
     * later with setFunction();
     */
    public Graph1D() {
    }

    /**
     * Create a graph of the specified function.
     *
     * @param func The function to be graphed.  If func is null, nothing is drawn.
     *             If func is non-null, it must be a function of one variable.
     */
    public Graph1D(Function func) {
        setFunction(func);
    }

    // ------------------------- Computing the points on the graph -----------------------

    /**
     * Get the color that is used to draw the graph.
     */
    public Color getColor() {
        return graphColor;
    }

    /**
     * Set the color to be used for drawing the graph.  The default color is magenta.
     */
    public void setColor(Color c) {
        if (c != null & !c.equals(graphColor)) {
            graphColor = c;
            needsRedraw();
        }
    }

    /**
     * Get the (possibly null) function whose graph is drawn.
     */
    public Function getFunction() {
        return func;
    }

    /**
     * Set the function to be graphed.  If it is null, nothing is drawn.
     * If it is non-null, it must be a function of one variable, or an error will occur.
     */
    synchronized public void setFunction(Function f) {
        if (f != null && f.getArity() != 1)
            throw new IllegalArgumentException("Internal Error:  Graph1D can only graph a function of one variable.");
        if (f != func) {
            func = f;
            changed = true;
            needsRedraw();
        }
    }

    /**
     * Recompute data for the graph and make sure that the area of the display canvas
     * that shows the graph is redrawn.  This method is ordinarily called by a
     * Controller.
     */
    synchronized public void compute() {
        setup(coords);
        needsRedraw();
    }

    /**
     * Draw the graph (possibly recomputing the data if the CoordinateRect has changed).
     * This is not usually called directly.
     */
    synchronized public void draw(Graphics g, boolean coordsChanged) {
        if (changed || coordsChanged || xcoord == null || ycoord == null) {
            setup(coords);
            changed = false;
        }
        if (xcoord.length == 0)
            return;
        g.setColor(graphColor);
        int x = xcoord[0];
        int y = ycoord[0];
        for (int i = 1; i < xcoord.length; i++) {
            if (xcoord[i] == Integer.MIN_VALUE) {
                do {
                    i++;
                } while (i < xcoord.length && xcoord[i] == Integer.MIN_VALUE);
                if (i < xcoord.length) {
                    x = xcoord[i];
                    y = ycoord[i];
                }
            } else {
                int x2 = xcoord[i];
                int y2 = ycoord[i];
                g.drawLine(x, y, x2, y2);
                x = x2;
                y = y2;
            }
        }
    }

    private double eval(double x, Cases c) {
        v[0] = x;
        if (c != null)
            c.clear();
        double y = func.getValueWithCases(v, c);
        if (Double.isInfinite(y) || Double.isNaN(y))
            return Double.NaN;
        else if (y > absoluteYmax)
            return absoluteYmax;
        else if (y < absoluteYmin)
            return absoluteYmin;
        else
            return y;
    }

    private int getStatus(double y) {
        if (Double.isNaN(y))
            return UNDEFINED;
        else if (y > onscreenymax)
            return ABOVE;
        else if (y < onscreenymin)
            return BELOW;
        else
            return ONSCREEN;
    }

    private void setup(CoordinateRect c) {
        if (func == null || c == null) {
            xcoord = ycoord = new int[0];
            return;
        }
        Vector points = new Vector();
        double pixelWidth = (c.getXmax() - c.getXmin()) / (c.getWidth() - 2 * c.getGap() - 1);
        onscreenymax = c.getYmax() + (100 + c.getGap()) * pixelWidth;
        onscreenymin = c.getYmin() - (100 + c.getGap()) * pixelWidth;
        absoluteYmax = c.getYmax() + 5000 * pixelWidth;
        absoluteYmin = c.getYmin() - 5000 * pixelWidth;

        double prevx, prevy, x, y, lastx;
        int status, prevstatus;

        int pixelx = c.getLeft();
        int pixely;

        int xHoldOffscreen = Integer.MIN_VALUE;
        int yHoldOffscreen = 0;
        int statusHoldOffscreen = 0;

        x = c.pixelToX(pixelx);
        y = eval(x, case1);
        status = getStatus(y);
        if (status == ONSCREEN) {
            points.addElement(new Point(pixelx, c.yToPixel(y)));
        } else if (status != UNDEFINED) {
            xHoldOffscreen = pixelx;
            yHoldOffscreen = c.yToPixel(y);
            statusHoldOffscreen = status;
        }

        int limitx = c.getLeft() + c.getWidth() - 1;
        while (pixelx < limitx) {
            prevx = x;
            prevy = y;
            prevstatus = status;
            pixelx += 3;
            if (pixelx > limitx)
                pixelx = limitx;
            x = c.pixelToX(pixelx);
            y = eval(x, case2);
            status = getStatus(y);
            if (status == UNDEFINED) {
                if (prevstatus != UNDEFINED) {
                    if (prevstatus == ONSCREEN)
                        domainEndpoint(c, points, prevx, x, prevy, y, prevstatus, status, 1);
                    else if (xHoldOffscreen != Integer.MIN_VALUE)
                        points.addElement(new Point(xHoldOffscreen, yHoldOffscreen));
                    xHoldOffscreen = Integer.MIN_VALUE;
                    points.addElement(new Point(Integer.MIN_VALUE, 0));
                }
            } else if (prevstatus == UNDEFINED) {
                if (status == ONSCREEN) {
                    domainEndpoint(c, points, prevx, x, prevy, y, prevstatus, status, 1);
                    points.addElement(new Point(pixelx, c.yToPixel(y)));
                    xHoldOffscreen = Integer.MIN_VALUE;
                } else {// note: status != UNDEFINED
                    xHoldOffscreen = pixelx;
                    yHoldOffscreen = c.yToPixel(y);
                    statusHoldOffscreen = status;
                }
                // xHoldOffscreen is already Integer.MIN_VALUE
            } else if (case1.equals(case2)) {
                if (status == ONSCREEN) {
                    if (xHoldOffscreen != Integer.MIN_VALUE) {
                        points.addElement(new Point(xHoldOffscreen, yHoldOffscreen));
                        xHoldOffscreen = Integer.MIN_VALUE;
                    }
                    points.addElement(new Point(pixelx, c.yToPixel(y)));
                } else {
                    pixely = c.yToPixel(y);
                    if (xHoldOffscreen != Integer.MIN_VALUE) {
                        if (status != statusHoldOffscreen) { // one ABOVE, one BELOW
                            points.addElement(new Point(xHoldOffscreen, yHoldOffscreen));
                            points.addElement(new Point(pixelx, pixely));
                            points.addElement(new Point(Integer.MIN_VALUE, 0));
                        }
                    } else
                        points.addElement(new Point(pixelx, pixely)); // first jump to offscreen
                    xHoldOffscreen = pixelx;
                    yHoldOffscreen = pixely;
                    statusHoldOffscreen = status;
                }
            } else {  // discontinuity
                if (prevstatus == ABOVE || prevstatus == BELOW) {
                    if (status == prevstatus) {
                        if (xHoldOffscreen != Integer.MIN_VALUE) { // should be false
                            points.addElement(new Point(xHoldOffscreen, yHoldOffscreen));
                            points.addElement(new Point(Integer.MIN_VALUE, 0));
                        }
                        xHoldOffscreen = pixelx;          // don't worry about offscreen discontinuity
                        yHoldOffscreen = c.yToPixel(y);
                        statusHoldOffscreen = status;
                    } else if (status == ONSCREEN) {  // possible visible discontinuity
                        if (xHoldOffscreen != Integer.MIN_VALUE) {
                            points.addElement(new Point(xHoldOffscreen, yHoldOffscreen));
                            xHoldOffscreen = Integer.MIN_VALUE;
                        }
                        discontinuity(c, points, prevx, x, prevy, y, prevstatus, status, 1);
                        y = eval(x, case2);  // reset cases, for next check
                        points.addElement(new Point(pixelx, c.yToPixel(y)));
                    } else {  // status == ABOVE or BELOW, opposit to prevstatus; just do a jump
                        if (xHoldOffscreen != Integer.MIN_VALUE)
                            points.addElement(new Point(xHoldOffscreen, yHoldOffscreen));
                        points.addElement(new Point(Integer.MIN_VALUE, 0));
                        xHoldOffscreen = pixelx;
                        yHoldOffscreen = c.yToPixel(y);
                        statusHoldOffscreen = status;
                    }
                } else {  // prevstatus is ONSCREEN; possible visible discontinuity
                    discontinuity(c, points, prevx, x, prevy, y, prevstatus, status, 1);
                    y = eval(x, case2);  // reset cases, for next check
                    if (status == ONSCREEN) {
                        points.addElement(new Point(pixelx, c.yToPixel(y)));
                        xHoldOffscreen = Integer.MIN_VALUE;
                    } else {
                        xHoldOffscreen = pixelx;
                        yHoldOffscreen = c.yToPixel(y);
                        statusHoldOffscreen = status;
                    }
                }
            }
            Cases temp = case2;
            case2 = case1;
            case1 = temp;
        }  // end while (pixel < limitx)
        xcoord = new int[points.size()];
        ycoord = new int[points.size()];
        for (int i = 0; i < ycoord.length; i++) {
            Point p = (Point) points.elementAt(i);
            xcoord[i] = p.x;
            ycoord[i] = p.y;
        }
    }

    // Status of one endpoints is ONSCREEN; other is ABOVE,BELOW, or ONSCREEN
    private void discontinuity(CoordinateRect c, Vector points, double x1, double x2, double y1, double y2,
                               int status1, int status2, int depth) {

        //System.out.println("In discontinuity, depth = " + depth);
        if (depth == MAX_DEPTH) {
            points.addElement(new Point(c.xToPixel(x1), c.yToPixel(y1)));
            points.addElement(new Point(Integer.MIN_VALUE, 0));
            points.addElement(new Point(c.xToPixel(x2), c.yToPixel(y2)));
        } else {
            double xmid = (x1 + x2) / 2.0;
            y1 = eval(x1, case1);
            double ymid = eval(xmid, case2);
            boolean cases1 = case1.equals(case2);
            y2 = eval(x2, case1);
            boolean cases2 = case1.equals(case2);
            int statusmid = getStatus(ymid);
            if (statusmid == UNDEFINED) { // hope it doesn't happen
                if (status1 == ONSCREEN)
                    domainEndpoint(c, points, x1, xmid, y1, ymid, status1, statusmid, 1);
                points.addElement(new Point(Integer.MIN_VALUE, 0));
                if (status2 == ONSCREEN)
                    domainEndpoint(c, points, xmid, x2, ymid, y2, statusmid, status2, 1);

            } else if (cases1 == false) {
                discontinuity(c, points, x1, xmid, y1, ymid, status1, statusmid, depth + 1);
                if (cases2 == false) // double discontinuity
                    discontinuity(c, points, xmid, x2, ymid, y2, statusmid, status2, depth + 1);
            } else if (cases2 == false)
                discontinuity(c, points, xmid, x2, ymid, y2, statusmid, status2, depth + 1);
            else
                System.out.println("Impossible error?  no discontinuity found in discontinuity for " + x1 + ',' + x2);
        }
    }


    //One of status1 and status2 is UNDEFINED, one is ONSCREEN.
    //This always adds a point to points.
    private void domainEndpoint(CoordinateRect c, Vector points, double x1, double x2, double y1, double y2,
                                int status1, int status2, int depth) {
        //System.out.println("IN domainEndpoint, ......... depth = " + depth);
        if (depth == MAX_DEPTH * 2) {
            if (status1 == ONSCREEN)
                points.addElement(new Point(c.xToPixel(x1), c.yToPixel(y1)));
            else  // status2 == ONSCREEN
                points.addElement(new Point(c.xToPixel(x2), c.yToPixel(y2)));
        } else {
            double xmid = (x1 + x2) / 2.0;
            double ymid = eval(xmid, null);
            int statusmid = getStatus(ymid);
            if (statusmid == ABOVE || statusmid == BELOW)
                points.addElement(new Point(c.xToPixel(xmid), c.yToPixel(ymid)));
            else if (statusmid == status1) // statusmid is ONSCREEN or UNDEFINED
                domainEndpoint(c, points, xmid, x2, ymid, y2, statusmid, status2, depth + 1);
            else
                domainEndpoint(c, points, x1, xmid, y1, ymid, status1, statusmid, depth + 1);
        }
    }

} // end class Graph1D



