/*
 * @test
 * @summary java.awt.geom.CubicCurve2D conformance: white-box structure plus black-box behavior.
 * @run main CubicCurve2DTest
 */

import java.awt.Shape;
import java.awt.geom.*;
import java.lang.reflect.*;

public class CubicCurve2DTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean close(double a, double b) { return Math.abs(a - b) < 1e-9; }

    static void checkClose(double a, double b, String m) { check(close(a, b), m + " (got " + a + ", want ~" + b + ")"); }
    static boolean closeLoose(double a, double b) { return Math.abs(a - b) < 1e-6; }

    static boolean hasMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getMethod(name, params); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.geom.CubicCurve2D");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "CubicCurve2D is public");
        check(Modifier.isAbstract(m), "CubicCurve2D is abstract");
        check(Shape.class.isAssignableFrom(c), "CubicCurve2D implements Shape");
        check(Cloneable.class.isAssignableFrom(c), "CubicCurve2D implements Cloneable");
        check(Modifier.isProtected(c.getDeclaredConstructor().getModifiers()), "protected no-arg constructor");
        check(hasMethod(c, "getFlatnessSq", double[].class, int.class), "static getFlatnessSq");
        check(hasMethod(c, "getFlatness", double[].class, int.class), "static getFlatness");
        check(hasMethod(c, "subdivide", CubicCurve2D.class, CubicCurve2D.class, CubicCurve2D.class), "static subdivide");
        check(hasMethod(c, "solveCubic", double[].class), "solveCubic(double[])");
        check(hasMethod(c, "solveCubic", double[].class, double[].class), "solveCubic(double[],double[])");
        check(hasMethod(c, "getCtrlX1") && hasMethod(c, "getCtrlY1") && hasMethod(c, "getCtrlX2") && hasMethod(c, "getCtrlY2"), "control point accessors");
        check(hasMethod(c, "setCurve", double.class, double.class, double.class, double.class, double.class, double.class, double.class, double.class), "setCurve");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        CubicCurve2D.Double cu = new CubicCurve2D.Double(0, 0, 10, 20, 30, -20, 40, 0);

        checkClose(cu.getFlatnessSq(), 400.0, "getFlatnessSq");
        checkClose(cu.getFlatness(), 20.0, "getFlatness");
        double[] cc = {0, 0, 10, 20, 30, -20, 40, 0};
        checkClose(CubicCurve2D.getFlatnessSq(cc, 0), 400.0, "static getFlatnessSq");
        checkClose(CubicCurve2D.getFlatness(cc, 0), 20.0, "static getFlatness");

        // subdivide instance
        CubicCurve2D.Double l = new CubicCurve2D.Double(), r = new CubicCurve2D.Double();
        cu.subdivide(l, r);
        check(close(l.getCtrlX1(), 5) && close(l.getCtrlY1(), 10) && close(l.getCtrlX2(), 12.5) && close(l.getCtrlY2(), 5) && close(l.getX2(), 20) && close(l.getY2(), 0),
              "subdivide left curve");
        check(close(r.getCtrlX1(), 27.5) && close(r.getCtrlY1(), -5) && close(r.getCtrlX2(), 35) && close(r.getCtrlY2(), -10) && close(r.getX2(), 40),
              "subdivide right curve");

        // subdivide static (arrays)
        double[] src = {0, 0, 10, 20, 30, -20, 40, 0};
        double[] la = new double[8], ra = new double[8];
        CubicCurve2D.subdivide(src, 0, la, 0, ra, 0);
        check(close(la[6], 20) && close(la[7], 0), "static subdivide array left endpoint");
        check(close(ra[2], 27.5) && close(ra[3], -5), "static subdivide array right ctrl1");

        // solveCubic
        double[] eq = {-6, 11, -6, 1}; // x^3 - 6x^2 + 11x - 6
        double[] roots = new double[3];
        int n = CubicCurve2D.solveCubic(eq, roots);
        check(n == 3, "solveCubic returns three roots");
        java.util.Arrays.sort(roots, 0, n);
        checkClose(roots[0], 1.0, "solveCubic root 1");
        checkClose(roots[1], 2.0, "solveCubic root 2");
        checkClose(roots[2], 3.0, "solveCubic root 3");

        // setCurve overloads
        CubicCurve2D.Double sc = new CubicCurve2D.Double();
        sc.setCurve(new double[]{1, 2, 3, 4, 5, 6, 7, 8}, 0);
        check(close(sc.getX1(), 1) && close(sc.getCtrlX1(), 3) && close(sc.getCtrlX2(), 5) && close(sc.getX2(), 7), "setCurve(double[],0)");
        sc.setCurve(new Point2D.Double(0, 0), new Point2D.Double(1, 1), new Point2D.Double(2, 2), new Point2D.Double(3, 3));
        check(close(sc.getCtrlX1(), 1) && close(sc.getCtrlX2(), 2) && close(sc.getX2(), 3), "setCurve(Point2D x4)");
        sc.setCurve(new Point2D[]{new Point2D.Double(0, 0), new Point2D.Double(1, 1), new Point2D.Double(2, 2), new Point2D.Double(9, 9)}, 0);
        check(close(sc.getCtrlX2(), 2) && close(sc.getX2(), 9), "setCurve(Point2D[],0)");
        sc.setCurve(new CubicCurve2D.Float(0, 0, 1, 1, 2, 2, 8, 8));
        check(close(sc.getCtrlX1(), 1) && close(sc.getX2(), 8), "setCurve(CubicCurve2D)");

        // bounds: JDK-8 returns the control-point hull (y in [-20,20]).
        // (JDK-9+ tightened curve getBounds2D to the exact curve envelope:
        //  height 11.547005383792634 for this curve — covered on the JDK-27 side.)
        Rectangle2D b = cu.getBounds2D();
        check(close(b.getX(), 0) && close(b.getWidth(), 40), "cubic bounds x/width");
        check(close(b.getY(), -20) && close(b.getHeight(), 40), "cubic bounds y/height == control hull");

        // intersects
        check(cu.intersects(5, -5, 30, 10), "cubic intersects rectangle");

        // flattened iterator
        int segs = 0;
        PathIterator it = cu.getPathIterator(null, 1.0);
        double[] c = new double[6];
        while (!it.isDone()) { it.currentSegment(c); segs++; it.next(); }
        check(segs > 5, "cubic flatten subdivides (" + segs + ")");

        // clone
        CubicCurve2D.Double cl = (CubicCurve2D.Double) cu.clone();
        check(cl != cu && close(cl.getCtrlX1(), 10) && close(cl.getX2(), 40), "clone equal and distinct");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
