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

    // ===================== 契约深化：端点 getter/setLine 三形态/intersectsLine/退化与反向边界 =====================
    static void testLineContracts() {
        // 端点 getter（Double 返回 Point2D.Double 且坐标保真）。
        Line2D.Double d = new Line2D.Double(1.5, 2.5, 3.5, 4.5);
        Point2D p1 = d.getP1();
        Point2D p2 = d.getP2();
        check(p1 instanceof Point2D.Double && p1.getX() == 1.5 && p1.getY() == 2.5,
              "getP1 returns Point2D.Double with coords");
        check(p2 instanceof Point2D.Double && p2.getX() == 3.5 && p2.getY() == 4.5,
              "getP2 returns Point2D.Double with coords");

        // setLine 三形态。
        Line2D.Double s = new Line2D.Double();
        s.setLine(1, 2, 3, 4);
        check(s.getX1() == 1 && s.getY1() == 2 && s.getX2() == 3 && s.getY2() == 4,
              "setLine(4 doubles)");
        s.setLine(new Point2D.Double(5, 6), new Point2D.Double(7, 8));
        check(s.getX1() == 5 && s.getY1() == 6 && s.getX2() == 7 && s.getY2() == 8,
              "setLine(Point2D, Point2D)");
        Line2D.Double other = new Line2D.Double(-1, -2, -3, -4);
        s.setLine(other);
        check(s.getX1() == -1 && s.getY1() == -2 && s.getX2() == -3 && s.getY2() == -4,
              "setLine(Line2D) copies coords");
        check(s.getX1() == other.getX1() && s.getX2() == other.getX2(), "setLine copies, not aliases");

        // intersectsLine（含端点相触、平行不相交、真穿越、对象形态）。
        Line2D.Double h = new Line2D.Double(0, 0, 10, 0);
        check(h.intersectsLine(5, -5, 5, 5), "intersectsLine(4 doubles) crossing");
        check(!h.intersectsLine(0, 1, 10, 1), "intersectsLine parallel disjoint");
        check(h.intersectsLine(10, 0, 20, 0), "intersectsLine sharing endpoint counts as intersect");
        check(h.intersectsLine(new Line2D.Double(5, -5, 5, 5)), "intersectsLine(Line2D) crossing");
        check(!h.intersectsLine(new Line2D.Double(0, 1, 10, 1)), "intersectsLine(Line2D) parallel");

        // relativeCCW：共线在 P1 之前 == -1；退化线段恒 0；竖直线左右两侧。
        check(Line2D.relativeCCW(0, 0, 1, 0, -1, 0) == -1, "relativeCCW collinear before P1 == -1");
        check(Line2D.relativeCCW(0, 0, 0, 0, 1, 1) == 0, "relativeCCW degenerate line == 0");
        check(Line2D.relativeCCW(0, 0, 0, 1, 1, 0) == 1, "relativeCCW vertical: +x side == 1");
        check(Line2D.relativeCCW(0, 0, 0, 1, -1, 0) == -1, "relativeCCW vertical: -x side == -1");

        // 退化线段 ptSegDist 退化为点距；ptLineDist 因无限直线无定义返回 NaN。
        Line2D.Double deg = new Line2D.Double(0, 0, 0, 0);
        checkClose(deg.ptSegDist(3, 4), 5.0, "degenerate ptSegDist == distance to point");
        check(Double.isNaN(deg.ptLineDist(3, 4)), "degenerate ptLineDist == NaN");

        // 反向线段 bounds 归一为正宽/高；getBounds2D 对称。
        Line2D.Double rev = new Line2D.Double(10, 0, 0, 0);
        Rectangle rb = rev.getBounds();
        check(rb.x == 0 && rb.y == 0 && rb.width == 10 && rb.height == 0, "reversed getBounds normalized");
        check(rev.getBounds2D().equals(new Rectangle2D.Double(0, 0, 10, 0)), "reversed getBounds2D normalized");

        // contains(Point2D) 与 contains(Rectangle2D) 恒 false（线无面积）。
        check(!h.contains(new Point2D.Double(5, 0)), "contains(Point2D) false on the line");
        check(!h.contains(new Rectangle2D.Double(0, -1, 10, 2)), "contains(Rectangle2D) false");

        // getPathIterator(AffineTransform) 施加平移变换。
        PathIterator ti = h.getPathIterator(AffineTransform.getTranslateInstance(1, 1));
        double[] cc = new double[6];
        check(ti.currentSegment(cc) == PathIterator.SEG_MOVETO && close(cc[0], 1) && close(cc[1], 1),
              "translated iterator MOVETO at (1,1)");
        ti.next();
        check(ti.currentSegment(cc) == PathIterator.SEG_LINETO && close(cc[0], 11) && close(cc[1], 1),
              "translated iterator LINETO at (11,1)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testLineContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
