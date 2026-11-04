/*
 * @test
 * @summary java.awt.geom.CubicCurve2D.Double conformance: white-box structure plus black-box behavior.
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

    static void checkClose(double a, double b, String m) { check(close(a, b), m + " (got " + a + ", want ~" + b + ")"); }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.geom.CubicCurve2D$Double");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "CubicCurve2D.Double is public");
        check(!Modifier.isAbstract(m), "CubicCurve2D.Double is concrete");
        check(c.getSuperclass() == CubicCurve2D.class, "CubicCurve2D.Double extends CubicCurve2D");
        check(Serializable.class.isAssignableFrom(c), "CubicCurve2D.Double implements Serializable");
        for (String f : new String[]{"x1", "y1", "ctrlx1", "ctrly1", "ctrlx2", "ctrly2", "x2", "y2"}) {
            Field fd = c.getField(f);
            check(Modifier.isPublic(fd.getModifiers()) && fd.getType() == double.class, "public double field " + f);
        }
        check(c.getConstructors().length == 2, "two public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        CubicCurve2D.Double q = new CubicCurve2D.Double();
        check(q.x1 == 0 && q.y1 == 0 && q.ctrlx1 == 0 && q.ctrly1 == 0 && q.ctrlx2 == 0 && q.ctrly2 == 0 && q.x2 == 0 && q.y2 == 0,
              "default ctor zeros");

        CubicCurve2D.Double d = new CubicCurve2D.Double(0, 0, 10, 20, 30, -20, 40, 0);
        check(close(d.getX1(), 0) && close(d.getY1(), 0), "8-double ctor start");
        check(close(d.getCtrlX1(), 10) && close(d.getCtrlY1(), 20), "8-double ctor ctrl1");
        check(close(d.getCtrlX2(), 30) && close(d.getCtrlY2(), -20), "8-double ctor ctrl2");
        check(close(d.getX2(), 40) && close(d.getY2(), 0), "8-double ctor end");
        check(d.x1 == 0 && d.ctrlx1 == 10 && d.ctrlx2 == 30 && d.x2 == 40, "public fields exposed");

        check(d.getP1().equals(new Point2D.Double(0, 0)), "getP1");
        check(d.getCtrlP1().equals(new Point2D.Double(10, 20)), "getCtrlP1");
        check(d.getCtrlP2().equals(new Point2D.Double(30, -20)), "getCtrlP2");
        check(d.getP2().equals(new Point2D.Double(40, 0)), "getP2");

        d.setCurve(1, 2, 3, 4, 5, 6, 7, 8);
        check(close(d.getX1(), 1) && close(d.getCtrlX1(), 3) && close(d.getCtrlX2(), 5) && close(d.getX2(), 7), "setCurve(double...)");

        CubicCurve2D.Double m = new CubicCurve2D.Double();
        m.x1 = 0; m.y1 = 0; m.ctrlx1 = 10; m.ctrly1 = 20; m.ctrlx2 = 30; m.ctrly2 = -20; m.x2 = 40; m.y2 = 0;
        check(close(m.getCtrlX1(), 10) && close(m.getX2(), 40), "public field mutation");

        checkClose(m.getFlatness(), 20.0, "getFlatness");
        Rectangle2D b = m.getBounds2D();
        check(close(b.getX(), 0) && close(b.getWidth(), 40), "getBounds2D");

        PathIterator it = m.getPathIterator(null);
        double[] c = new double[6];
        check(it.currentSegment(c) == PathIterator.SEG_MOVETO, "iterator MOVETO");
        it.next();
        check(it.currentSegment(c) == PathIterator.SEG_CUBICTO && close(c[0], 10) && close(c[1], 20) && close(c[2], 30) && close(c[3], -20) && close(c[4], 40) && close(c[5], 0),
              "iterator CUBICTO coords");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
