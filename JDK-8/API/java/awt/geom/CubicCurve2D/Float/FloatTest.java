/*
 * @test
 * @summary java.awt.geom.CubicCurve2D.Float conformance: white-box structure plus black-box behavior.
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

    static void checkClose(double a, double b, String m) { check(close(a, b), m + " (got " + a + ", want ~" + b + ")"); }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.geom.CubicCurve2D$Float");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "CubicCurve2D.Float is public");
        check(!Modifier.isAbstract(m), "CubicCurve2D.Float is concrete");
        check(c.getSuperclass() == CubicCurve2D.class, "CubicCurve2D.Float extends CubicCurve2D");
        check(Serializable.class.isAssignableFrom(c), "CubicCurve2D.Float implements Serializable");
        for (String f : new String[]{"x1", "y1", "ctrlx1", "ctrly1", "ctrlx2", "ctrly2", "x2", "y2"}) {
            Field fd = c.getField(f);
            check(Modifier.isPublic(fd.getModifiers()) && fd.getType() == float.class, "public float field " + f);
        }
        check(c.getConstructors().length == 2, "two public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        CubicCurve2D.Float q = new CubicCurve2D.Float();
        check(q.x1 == 0f && q.y1 == 0f && q.ctrlx1 == 0f && q.ctrly1 == 0f && q.ctrlx2 == 0f && q.ctrly2 == 0f && q.x2 == 0f && q.y2 == 0f,
              "default ctor zeros");

        CubicCurve2D.Float f = new CubicCurve2D.Float(0f, 0f, 10f, 20f, 30f, -20f, 40f, 0f);
        check(close(f.getX1(), 0) && close(f.getY1(), 0), "8-float ctor start");
        check(close(f.getCtrlX1(), 10) && close(f.getCtrlY1(), 20), "8-float ctor ctrl1");
        check(close(f.getCtrlX2(), 30) && close(f.getCtrlY2(), -20), "8-float ctor ctrl2");
        check(close(f.getX2(), 40) && close(f.getY2(), 0), "8-float ctor end");
        check(f.x1 == 0f && f.ctrlx1 == 10f && f.ctrlx2 == 30f && f.x2 == 40f, "public fields exposed");

        check(f.getP1().equals(new Point2D.Double(0, 0)), "getP1");
        check(f.getCtrlP1().equals(new Point2D.Double(10, 20)), "getCtrlP1");
        check(f.getCtrlP2().equals(new Point2D.Double(30, -20)), "getCtrlP2");
        check(f.getP2().equals(new Point2D.Double(40, 0)), "getP2");

        f.setCurve(1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f);
        check(close(f.getX1(), 1) && close(f.getCtrlX1(), 3) && close(f.getCtrlX2(), 5) && close(f.getX2(), 7), "setCurve(float...)");
        f.setCurve(0.5, 1.5, 2.5, 3.5, 4.5, 5.5, 6.5, 7.5);
        check(close(f.getX1(), 0.5) && close(f.getY2(), 7.5), "setCurve(double...)");

        CubicCurve2D.Float m = new CubicCurve2D.Float();
        m.x1 = 0; m.y1 = 0; m.ctrlx1 = 10; m.ctrly1 = 20; m.ctrlx2 = 30; m.ctrly2 = -20; m.x2 = 40; m.y2 = 0;
        check(close(m.getCtrlX1(), 10) && close(m.getX2(), 40), "public field mutation");

        checkClose(m.getFlatness(), 20.0, "getFlatness");
        Rectangle2D b = m.getBounds2D();
        check(close(b.getX(), 0) && close(b.getWidth(), 40), "getBounds2D");

        PathIterator it = m.getPathIterator(null);
        double[] c = new double[6];
        check(it.currentSegment(c) == PathIterator.SEG_MOVETO && close(c[0], 0) && close(c[1], 0), "iterator MOVETO");
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
