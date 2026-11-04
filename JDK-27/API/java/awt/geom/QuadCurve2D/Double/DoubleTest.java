/*
 * @test
 * @summary java.awt.geom.QuadCurve2D.Double conformance: white-box structure plus black-box behavior.
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
        Class<?> c = Class.forName("java.awt.geom.QuadCurve2D$Double");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "QuadCurve2D.Double is public");
        check(!Modifier.isAbstract(m), "QuadCurve2D.Double is concrete");
        check(c.getSuperclass() == QuadCurve2D.class, "QuadCurve2D.Double extends QuadCurve2D");
        check(Serializable.class.isAssignableFrom(c), "QuadCurve2D.Double implements Serializable");
        for (String f : new String[]{"x1", "y1", "ctrlx", "ctrly", "x2", "y2"}) {
            Field fd = c.getField(f);
            check(Modifier.isPublic(fd.getModifiers()) && fd.getType() == double.class, "public double field " + f);
        }
        check(c.getConstructors().length == 2, "two public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        QuadCurve2D.Double q = new QuadCurve2D.Double();
        check(q.x1 == 0 && q.y1 == 0 && q.ctrlx == 0 && q.ctrly == 0 && q.x2 == 0 && q.y2 == 0, "default ctor zeros");

        QuadCurve2D.Double d = new QuadCurve2D.Double(0, 0, 10, 10, 20, 0);
        check(close(d.getX1(), 0) && close(d.getY1(), 0), "6-double ctor start");
        check(close(d.getCtrlX(), 10) && close(d.getCtrlY(), 10), "6-double ctor control");
        check(close(d.getX2(), 20) && close(d.getY2(), 0), "6-double ctor end");
        check(d.x1 == 0 && d.ctrlx == 10 && d.x2 == 20, "public fields exposed");

        check(d.getP1().equals(new Point2D.Double(0, 0)), "getP1");
        check(d.getCtrlPt().equals(new Point2D.Double(10, 10)), "getCtrlPt");
        check(d.getP2().equals(new Point2D.Double(20, 0)), "getP2");

        d.setCurve(1, 2, 3, 4, 5, 6);
        check(close(d.getX1(), 1) && close(d.getCtrlX(), 3) && close(d.getX2(), 5), "setCurve(double...)");
        d.setCurve(new QuadCurve2D.Float(0, 0, 9, 9, 10, 10));
        check(close(d.getCtrlX(), 9) && close(d.getX2(), 10), "inherited setCurve(QuadCurve2D)");

        QuadCurve2D.Double m = new QuadCurve2D.Double();
        m.x1 = 0; m.y1 = 0; m.ctrlx = 10; m.ctrly = 10; m.x2 = 20; m.y2 = 0;
        check(close(m.getCtrlX(), 10) && close(m.getX2(), 20), "public field mutation");

        checkClose(m.getFlatness(), 10.0, "getFlatness");
        checkClose(m.getFlatnessSq(), 100.0, "getFlatnessSq");

        Rectangle2D b = m.getBounds2D();
        check(close(b.getX(), 0) && close(b.getWidth(), 20), "getBounds2D");

        check(m.contains(10, 4), "contains inside lens");
        check(!m.contains(10, 8), "excludes outside lens");

        PathIterator it = m.getPathIterator(null);
        double[] c = new double[6];
        check(it.currentSegment(c) == PathIterator.SEG_MOVETO, "iterator MOVETO");
        it.next();
        check(it.currentSegment(c) == PathIterator.SEG_QUADTO && close(c[0], 10) && close(c[1], 10) && close(c[2], 20), "iterator QUADTO");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
