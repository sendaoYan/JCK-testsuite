/*
 * @test
 * @summary java.awt.geom.Line2D.Double conformance: white-box structure plus black-box behavior.
 * @run main DoubleTest
 */

import java.awt.geom.*;
import java.io.Serializable;
import java.lang.reflect.*;

public class DoubleTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean close(double a, double b) { return Math.abs(a - b) < 1e-9; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.geom.Line2D$Double");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "Line2D.Double is public");
        check(!Modifier.isAbstract(m), "Line2D.Double is concrete");
        check(c.getSuperclass() == Line2D.class, "Line2D.Double extends Line2D");
        check(Serializable.class.isAssignableFrom(c), "Line2D.Double implements Serializable");
        for (String f : new String[]{"x1", "y1", "x2", "y2"}) {
            Field fd = c.getField(f);
            check(Modifier.isPublic(fd.getModifiers()), "public field " + f);
            check(fd.getType() == double.class, "field " + f + " is double");
        }
        check(c.getConstructors().length == 3, "three public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Line2D.Double l = new Line2D.Double();
        check(l.x1 == 0 && l.y1 == 0 && l.x2 == 0 && l.y2 == 0, "default ctor zeros");

        Line2D.Double d = new Line2D.Double(1, 2, 3, 4);
        check(close(d.getX1(), 1) && close(d.getY1(), 2), "4-double ctor start");
        check(close(d.getX2(), 3) && close(d.getY2(), 4), "4-double ctor end");
        check(d.x1 == 1 && d.y1 == 2 && d.x2 == 3 && d.y2 == 4, "public fields exposed");

        Line2D.Double p = new Line2D.Double(new Point2D.Double(5, 6), new Point2D.Double(7, 8));
        check(close(p.getX1(), 5) && close(p.getX2(), 7), "Point2D ctor");

        check(d.getP1().equals(new Point2D.Double(1, 2)), "getP1");
        check(d.getP2().equals(new Point2D.Double(3, 4)), "getP2");

        d.setLine(10, 20, 30, 40);
        check(close(d.getX1(), 10) && close(d.getY1(), 20) && close(d.getX2(), 30) && close(d.getY2(), 40), "setLine(double)");
        d.setLine(new Point2D.Double(1, 2), new Point2D.Double(3, 4));
        check(close(d.getX1(), 1) && close(d.getX2(), 3), "setLine(Point2D,Point2D)");
        d.setLine(new Line2D.Float(9, 8, 7, 6));
        check(close(d.getX1(), 9) && close(d.getY2(), 6), "setLine(Line2D)");

        Line2D.Double m = new Line2D.Double();
        m.x1 = 1; m.y1 = 2; m.x2 = 5; m.y2 = 9;
        check(close(m.getX1(), 1) && close(m.getY2(), 9), "public field mutation");

        Line2D.Double b = new Line2D.Double(0, 0, 10, 0);
        Rectangle2D bb = b.getBounds2D();
        check(close(bb.getX(), 0) && close(bb.getY(), 0) && close(bb.getWidth(), 10) && close(bb.getHeight(), 0), "getBounds2D");

        Line2D.Double diag = new Line2D.Double(10, 20, 0, 0);
        Rectangle2D db = diag.getBounds2D();
        check(close(db.getX(), 0) && close(db.getY(), 0) && close(db.getWidth(), 10) && close(db.getHeight(), 20), "diagonal bounds normalize");

        check(close(new Line2D.Double(0, 0, 10, 0).ptSegDist(5, 3), 3.0), "inherited ptSegDist");

        PathIterator it = b.getPathIterator(null);
        double[] c = new double[6];
        check(it.currentSegment(c) == PathIterator.SEG_MOVETO, "iterator MOVETO");
        it.next();
        check(it.currentSegment(c) == PathIterator.SEG_LINETO && close(c[0], 10), "iterator LINETO");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
