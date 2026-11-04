/*
 * @test
 * @summary java.awt.geom.Line2D conformance: white-box structure plus black-box behavior.
 * @run main Line2DTest
 */

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.*;
import java.lang.reflect.*;

public class Line2DTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean close(double a, double b) { return Math.abs(a - b) < 1e-9; }

    static void checkClose(double a, double b, String m) { check(close(a, b), m + " (got " + a + ", want ~" + b + ")"); }

    static boolean hasMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getMethod(name, params); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.geom.Line2D");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "Line2D is public");
        check(Modifier.isAbstract(m), "Line2D is abstract");
        check(Shape.class.isAssignableFrom(c), "Line2D implements Shape");
        check(Cloneable.class.isAssignableFrom(c), "Line2D implements Cloneable");
        check(Modifier.isProtected(c.getDeclaredConstructor().getModifiers()), "protected no-arg constructor");
        check(hasMethod(c, "relativeCCW", double.class, double.class, double.class, double.class, double.class, double.class), "static relativeCCW");
        check(hasMethod(c, "linesIntersect", double.class, double.class, double.class, double.class, double.class, double.class, double.class, double.class), "static linesIntersect");
        check(hasMethod(c, "ptSegDistSq", double.class, double.class, double.class, double.class, double.class, double.class), "static ptSegDistSq");
        check(hasMethod(c, "ptSegDist", double.class, double.class, double.class, double.class, double.class, double.class), "static ptSegDist");
        check(hasMethod(c, "ptLineDistSq", double.class, double.class, double.class, double.class, double.class, double.class), "static ptLineDistSq");
        check(hasMethod(c, "ptLineDist", double.class, double.class, double.class, double.class, double.class, double.class), "static ptLineDist");
        check(hasMethod(c, "getX1") && hasMethod(c, "getY1") && hasMethod(c, "getX2") && hasMethod(c, "getY2"), "abstract coordinate getters");
        check(hasMethod(c, "getPathIterator", AffineTransform.class, double.class), "getPathIterator");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // relativeCCW: directed line (0,0)->(1,0)
        check(Line2D.relativeCCW(0, 0, 1, 0, 0.5, 1) == -1, "relativeCCW below-right (y-down) == -1");
        check(Line2D.relativeCCW(0, 0, 1, 0, 0.5, -1) == 1, "relativeCCW above-left (y-down) == 1");
        check(Line2D.relativeCCW(0, 0, 1, 0, 2, 0) == 1, "relativeCCW collinear beyond P2 == 1");
        check(Line2D.relativeCCW(0, 0, 1, 0, 1, 0) == 0, "relativeCCW collinear on segment == 0");

        // linesIntersect
        check(Line2D.linesIntersect(0, 0, 2, 2, 0, 2, 2, 0), "linesIntersect crossing diagonals");
        check(!Line2D.linesIntersect(0, 0, 1, 1, 2, 2, 3, 3), "linesIntersect disjoint collinear");

        // distances on segment (0,0)-(10,0)
        Line2D.Double seg = new Line2D.Double(0, 0, 10, 0);
        checkClose(seg.ptSegDist(5, 3), 3.0, "ptSegDist perpendicular");
        checkClose(seg.ptSegDistSq(5, 3), 9.0, "ptSegDistSq perpendicular");
        checkClose(seg.ptLineDist(5, 3), 3.0, "ptLineDist perpendicular");
        checkClose(seg.ptLineDistSq(5, 3), 9.0, "ptLineDistSq perpendicular");
        checkClose(seg.ptLineDist(20, 3), 3.0, "ptLineDist infinite line");
        checkClose(seg.ptSegDist(20, 3), Math.sqrt(109.0), "ptSegDist clamps to endpoint");
        checkClose(seg.ptSegDistSq(20, 3), 109.0, "ptSegDistSq clamps to endpoint");

        Line2D.Double seg2 = new Line2D.Double(0, 0, 10, 0);
        checkClose(seg2.ptSegDist(new Point2D.Double(5, 3)), 3.0, "ptSegDist(Point2D)");
        checkClose(seg2.ptLineDist(new Point2D.Double(5, 3)), 3.0, "ptLineDist(Point2D)");

        // instance relativeCCW
        Line2D.Double dir = new Line2D.Double(0, 0, 1, 0);
        check(dir.relativeCCW(0.5, 1) == -1, "instance relativeCCW(double,double)");
        check(dir.relativeCCW(new Point2D.Double(0.5, -1)) == 1, "instance relativeCCW(Point2D)");

        // bounds / iterator
        Rectangle b = seg.getBounds();
        check(b.x == 0 && b.y == 0 && b.width == 10 && b.height == 0, "getBounds integer");
        check(seg.getBounds2D().equals(new Rectangle2D.Double(0, 0, 10, 0)), "getBounds2D");
        PathIterator it = seg.getPathIterator(null);
        double[] c = new double[6];
        check(it.currentSegment(c) == PathIterator.SEG_MOVETO && close(c[0], 0) && close(c[1], 0), "iterator MOVETO");
        it.next();
        check(it.currentSegment(c) == PathIterator.SEG_LINETO && close(c[0], 10) && close(c[1], 0), "iterator LINETO");

        // contains / intersects
        check(!seg.contains(5, 0), "line contains(point) always false");
        check(seg.intersects(0, 0, 5, 5), "line intersects rectangle");

        // clone
        Line2D.Double cl = (Line2D.Double) seg.clone();
        check(cl != seg && cl.getX1() == 0 && cl.getX2() == 10, "clone independent and equal");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
