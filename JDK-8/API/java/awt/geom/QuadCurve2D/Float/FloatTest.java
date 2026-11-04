/*
 * @test
 * @summary java.awt.geom.QuadCurve2D.Float conformance: white-box structure plus black-box behavior.
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
        Class<?> c = Class.forName("java.awt.geom.QuadCurve2D$Float");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "QuadCurve2D.Float is public");
        check(!Modifier.isAbstract(m), "QuadCurve2D.Float is concrete");
        check(c.getSuperclass() == QuadCurve2D.class, "QuadCurve2D.Float extends QuadCurve2D");
        check(Serializable.class.isAssignableFrom(c), "QuadCurve2D.Float implements Serializable");
        for (String f : new String[]{"x1", "y1", "ctrlx", "ctrly", "x2", "y2"}) {
            Field fd = c.getField(f);
            check(Modifier.isPublic(fd.getModifiers()) && fd.getType() == float.class, "public float field " + f);
        }
        check(c.getConstructors().length == 2, "two public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        QuadCurve2D.Float q = new QuadCurve2D.Float();
        check(q.x1 == 0f && q.y1 == 0f && q.ctrlx == 0f && q.ctrly == 0f && q.x2 == 0f && q.y2 == 0f, "default ctor zeros");

        QuadCurve2D.Float f = new QuadCurve2D.Float(0f, 0f, 10f, 10f, 20f, 0f);
        check(close(f.getX1(), 0) && close(f.getY1(), 0), "6-float ctor start");
        check(close(f.getCtrlX(), 10) && close(f.getCtrlY(), 10), "6-float ctor control");
        check(close(f.getX2(), 20) && close(f.getY2(), 0), "6-float ctor end");
        check(f.x1 == 0f && f.ctrlx == 10f && f.x2 == 20f, "public fields exposed");

        check(f.getP1().equals(new Point2D.Double(0, 0)), "getP1");
        check(f.getCtrlPt().equals(new Point2D.Double(10, 10)), "getCtrlPt");
        check(f.getP2().equals(new Point2D.Double(20, 0)), "getP2");

        // setCurve overloads
        f.setCurve(1f, 2f, 3f, 4f, 5f, 6f);
        check(close(f.getX1(), 1) && close(f.getCtrlX(), 3) && close(f.getX2(), 5), "setCurve(float...)");
        f.setCurve(0.5, 1.5, 2.5, 3.5, 4.5, 5.5);
        check(close(f.getX1(), 0.5) && close(f.getY2(), 5.5), "setCurve(double...)");

        // inherited setCurve variants
        f.setCurve(new QuadCurve2D.Double(0, 0, 9, 9, 10, 10));
        check(close(f.getCtrlX(), 9) && close(f.getX2(), 10), "inherited setCurve(QuadCurve2D)");

        // direct field mutation
        QuadCurve2D.Float m = new QuadCurve2D.Float();
        m.x1 = 0; m.y1 = 0; m.ctrlx = 10; m.ctrly = 10; m.x2 = 20; m.y2 = 0;
        check(close(m.getCtrlX(), 10) && close(m.getX2(), 20), "public field mutation");

        // flatness
        checkClose(m.getFlatness(), 10.0, "getFlatness");
        checkClose(m.getFlatnessSq(), 100.0, "getFlatnessSq");

        // bounds
        Rectangle2D b = m.getBounds2D();
        check(close(b.getX(), 0) && close(b.getWidth(), 20) && close(b.getY(), 0), "getBounds2D");

        // contains
        check(m.contains(10, 4), "contains inside lens");
        check(!m.contains(10, 8), "excludes outside lens");

        // path iterator
        PathIterator it = m.getPathIterator(null);
        double[] c = new double[6];
        check(it.currentSegment(c) == PathIterator.SEG_MOVETO && close(c[0], 0) && close(c[1], 0), "iterator MOVETO");
        it.next();
        check(it.currentSegment(c) == PathIterator.SEG_QUADTO && close(c[0], 10) && close(c[1], 10) && close(c[2], 20) && close(c[3], 0), "iterator QUADTO");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
