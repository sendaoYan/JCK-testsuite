/*
 * @test
 * @summary java.awt.geom.QuadCurve2D conformance: white-box structure plus black-box behavior.
 * @run main QuadCurve2DTest
 */

import java.awt.Shape;
import java.awt.geom.*;
import java.lang.reflect.*;

public class QuadCurve2DTest {
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
        Class<?> c = Class.forName("java.awt.geom.QuadCurve2D");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "QuadCurve2D is public");
        check(Modifier.isAbstract(m), "QuadCurve2D is abstract");
        check(Shape.class.isAssignableFrom(c), "QuadCurve2D implements Shape");
        check(Cloneable.class.isAssignableFrom(c), "QuadCurve2D implements Cloneable");
        check(Modifier.isProtected(c.getDeclaredConstructor().getModifiers()), "protected no-arg constructor");
        check(hasMethod(c, "getFlatnessSq", double[].class, int.class), "static getFlatnessSq(double[],int)");
        check(hasMethod(c, "getFlatness", double[].class, int.class), "static getFlatness(double[],int)");
        check(hasMethod(c, "subdivide", QuadCurve2D.class, QuadCurve2D.class, QuadCurve2D.class), "static subdivide");
        check(hasMethod(c, "solveQuadratic", double[].class), "solveQuadratic(double[])");
        check(hasMethod(c, "solveQuadratic", double[].class, double[].class), "solveQuadratic(double[],double[])");
        check(hasMethod(c, "getCtrlX") && hasMethod(c, "getCtrlY") && hasMethod(c, "getCtrlPt"), "control point accessors");
        check(hasMethod(c, "setCurve", double.class, double.class, double.class, double.class, double.class, double.class), "setCurve");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        QuadCurve2D.Double q = new QuadCurve2D.Double(0, 0, 10, 10, 20, 0);

        // flatness
        checkClose(q.getFlatnessSq(), 100.0, "getFlatnessSq");
        checkClose(q.getFlatness(), 10.0, "getFlatness");
        double[] coords = {0, 0, 10, 10, 20, 0};
        checkClose(QuadCurve2D.getFlatnessSq(coords, 0), 100.0, "static getFlatnessSq");
        checkClose(QuadCurve2D.getFlatness(coords, 0), 10.0, "static getFlatness");

        // subdivide (instance)
        QuadCurve2D.Double l = new QuadCurve2D.Double();
        QuadCurve2D.Double r = new QuadCurve2D.Double();
        q.subdivide(l, r);
        check(close(l.getX1(), 0) && close(l.getY1(), 0) && close(l.getCtrlX(), 5) && close(l.getCtrlY(), 5) && close(l.getX2(), 10) && close(l.getY2(), 5),
              "subdivide left curve");
        check(close(r.getX1(), 10) && close(r.getY1(), 5) && close(r.getCtrlX(), 15) && close(r.getCtrlY(), 5) && close(r.getX2(), 20) && close(r.getY2(), 0),
              "subdivide right curve");

        // subdivide (static, 3-arg)
        QuadCurve2D.Double ls = new QuadCurve2D.Double(), rs = new QuadCurve2D.Double();
        QuadCurve2D.subdivide(q, ls, rs);
        check(close(ls.getCtrlX(), 5) && close(rs.getX1(), 10), "static subdivide(QuadCurve2D...)");

        // subdivide (static, arrays)
        double[] src = {0, 0, 10, 10, 20, 0};
        double[] la = new double[6], ra = new double[6];
        QuadCurve2D.subdivide(src, 0, la, 0, ra, 0);
        check(close(la[2], 5) && close(la[3], 5) && close(la[4], 10) && close(la[5], 5), "static subdivide array left");
        check(close(ra[2], 15) && close(ra[3], 5) && close(ra[4], 20) && close(ra[5], 0), "static subdivide array right");

        // solveQuadratic
        double[] eq = {1, -3, 2}; // 1 - 3x + 2x^2 = 0 -> x = 0.5, 1
        double[] roots = new double[2];
        int n = QuadCurve2D.solveQuadratic(eq, roots);
        check(n == 2, "solveQuadratic returns two roots");
        java.util.Arrays.sort(roots, 0, n);
        checkClose(roots[0], 0.5, "solveQuadratic root0");
        checkClose(roots[1], 1.0, "solveQuadratic root1");

        double[] eq2 = {1, 0, -1}; // 1 - x^2 = 0 -> x = -1, 1
        double[] roots2 = new double[2];
        int n2 = QuadCurve2D.solveQuadratic(eq2, roots2);
        check(n2 == 2, "solveQuadratic two roots again");
        java.util.Arrays.sort(roots2, 0, n2);
        checkClose(roots2[0], -1.0, "solveQuadratic root -1");
        checkClose(roots2[1], 1.0, "solveQuadratic root 1");

        // setCurve overloads
        QuadCurve2D.Double sc = new QuadCurve2D.Double();
        sc.setCurve(new double[]{1, 2, 3, 4, 5, 6}, 0);
        check(close(sc.getX1(), 1) && close(sc.getX2(), 5) && close(sc.getCtrlY(), 4), "setCurve(double[],0)");
        sc.setCurve(new Point2D.Double(0, 0), new Point2D.Double(1, 1), new Point2D.Double(2, 2));
        check(close(sc.getCtrlX(), 1) && close(sc.getX2(), 2), "setCurve(Point2D,Point2D,Point2D)");
        sc.setCurve(new Point2D[]{new Point2D.Double(0, 0), new Point2D.Double(7, 7), new Point2D.Double(8, 8)}, 0);
        check(close(sc.getCtrlX(), 7), "setCurve(Point2D[],0)");
        sc.setCurve(new QuadCurve2D.Float(0, 0, 5, 5, 10, 10));
        check(close(sc.getCtrlX(), 5) && close(sc.getX2(), 10), "setCurve(QuadCurve2D)");

        // contains / intersects
        check(q.contains(10, 4), "quad contains point inside lens");
        check(!q.contains(10, 8), "quad excludes point above lens");
        check(q.intersects(5, 0, 10, 10), "quad intersects rectangle");

        // bounds
        Rectangle2D b = q.getBounds2D();
        check(close(b.getX(), 0) && close(b.getWidth(), 20), "quad bounds x/width");
        check(closeLoose(b.getHeight(), 5.0), "quad bounds height ~5");

        // flattened path iterator subdivides
        int segs = 0;
        PathIterator it = q.getPathIterator(null, 0.5);
        double[] c = new double[6];
        while (!it.isDone()) { it.currentSegment(c); segs++; it.next(); }
        check(segs > 4, "quad flatten subdivides into many segments (" + segs + ")");

        // clone
        QuadCurve2D.Double cl = (QuadCurve2D.Double) q.clone();
        check(cl != q && cl.getX1() == 0 && cl.getX2() == 20 && close(cl.getCtrlX(), 10), "clone equal and distinct");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
