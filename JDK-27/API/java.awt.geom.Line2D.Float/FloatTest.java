/*
 * @test
 * @summary java.awt.geom.Line2D.Float conformance: white-box structure plus black-box behavior.
 * @run main FloatTest
 */

import java.awt.geom.*;
import java.io.Serializable;
import java.lang.reflect.*;

public class FloatTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean close(double a, double b) { return Math.abs(a - b) < 1e-9; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.geom.Line2D$Float");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "Line2D.Float is public");
        check(!Modifier.isAbstract(m), "Line2D.Float is concrete");
        check(c.getSuperclass() == Line2D.class, "Line2D.Float extends Line2D");
        check(Serializable.class.isAssignableFrom(c), "Line2D.Float implements Serializable");
        for (String f : new String[]{"x1", "y1", "x2", "y2"}) {
            Field fd = c.getField(f);
            check(Modifier.isPublic(fd.getModifiers()), "public field " + f);
            check(fd.getType() == float.class, "field " + f + " is float");
        }
        check(c.getConstructors().length == 3, "three public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Line2D.Float l = new Line2D.Float();
        check(l.x1 == 0f && l.y1 == 0f && l.x2 == 0f && l.y2 == 0f, "default ctor zeros");

        Line2D.Float f = new Line2D.Float(1f, 2f, 3f, 4f);
        check(close(f.getX1(), 1) && close(f.getY1(), 2), "4-float ctor start");
        check(close(f.getX2(), 3) && close(f.getY2(), 4), "4-float ctor end");
        check(f.x1 == 1f && f.y1 == 2f && f.x2 == 3f && f.y2 == 4f, "public fields exposed");

        Line2D.Float p = new Line2D.Float(new Point2D.Double(5, 6), new Point2D.Double(7, 8));
        check(close(p.getX1(), 5) && close(p.getX2(), 7), "Point2D ctor");

        check(f.getP1().equals(new Point2D.Double(1, 2)), "getP1");
        check(f.getP2().equals(new Point2D.Double(3, 4)), "getP2");

        // setLine overloads
        f.setLine(10f, 20f, 30f, 40f);
        check(close(f.getX1(), 10) && close(f.getY1(), 20) && close(f.getX2(), 30) && close(f.getY2(), 40), "setLine(float)");
        f.setLine(0.5, 1.5, 2.5, 3.5);
        check(close(f.getX1(), 0.5) && close(f.getY2(), 3.5), "setLine(double)");
        f.setLine(new Point2D.Double(1, 2), new Point2D.Double(3, 4));
        check(close(f.getX1(), 1) && close(f.getX2(), 3), "setLine(Point2D,Point2D)");
        f.setLine(new Line2D.Double(9, 8, 7, 6));
        check(close(f.getX1(), 9) && close(f.getY2(), 6), "setLine(Line2D)");

        // direct public field mutation
        Line2D.Float m = new Line2D.Float();
        m.x1 = 1; m.y1 = 2; m.x2 = 5; m.y2 = 9;
        check(close(m.getX1(), 1) && close(m.getY2(), 9), "public field mutation reflected in getters");

        // bounds
        Line2D.Float b = new Line2D.Float(0, 0, 10, 0);
        Rectangle2D bb = b.getBounds2D();
        check(close(bb.getX(), 0) && close(bb.getY(), 0) && close(bb.getWidth(), 10) && close(bb.getHeight(), 0), "getBounds2D");

        // diagonal bounds ordering
        Line2D.Float d = new Line2D.Float(10, 20, 0, 0);
        Rectangle2D db = d.getBounds2D();
        check(close(db.getX(), 0) && close(db.getY(), 0) && close(db.getWidth(), 10) && close(db.getHeight(), 20), "diagonal bounds normalize");

        // inherited distance methods
        check(close(new Line2D.Float(0, 0, 10, 0).ptSegDist(5, 3), 3.0), "inherited ptSegDist");

        // path iterator
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
