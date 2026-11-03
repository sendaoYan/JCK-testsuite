/*
 * @test
 * @summary java.awt.geom.Rectangle2D conformance: white-box structure plus black-box behavior.
 * @run main Rectangle2DTest
 */

import java.awt.geom.*;
import java.lang.reflect.*;

public class Rectangle2DTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean close(double a, double b) { return Math.abs(a - b) < 1e-9; }
    static boolean hasMethod(Class<?> c, String n, Class<?>... p) {
        try { c.getMethod(n, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.geom.Rectangle2D");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) && Modifier.isAbstract(m), "abstract public class");
        check(RectangularShape.class.isAssignableFrom(c), "extends RectangularShape");
        check(hasMethod(c, "setRect", double.class, double.class, double.class, double.class), "abstract setRect");
        check(hasMethod(c, "outcode", double.class, double.class), "abstract outcode(double,double)");
        check(hasMethod(c, "createIntersection", Rectangle2D.class), "abstract createIntersection");
        check(hasMethod(c, "createUnion", Rectangle2D.class), "abstract createUnion");
        check(hasMethod(c, "intersect", Rectangle2D.class, Rectangle2D.class, Rectangle2D.class), "static intersect");
        check(hasMethod(c, "union", Rectangle2D.class, Rectangle2D.class, Rectangle2D.class), "static union");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(Rectangle2D.OUT_LEFT == 1 && Rectangle2D.OUT_TOP == 2
              && Rectangle2D.OUT_RIGHT == 4 && Rectangle2D.OUT_BOTTOM == 8,
              "outcode constant values");

        Rectangle2D.Double r = new Rectangle2D.Double(0, 0, 10, 20);
        check(close(r.getX(), 0) && close(r.getY(), 0) && close(r.getWidth(), 10) && close(r.getHeight(), 20),
              "ctor components");

        // outcode
        check(r.outcode(5, 10) == 0, "inside outcode 0");
        check(r.outcode(-1, 5) == Rectangle2D.OUT_LEFT, "left outcode");
        check(r.outcode(5, -1) == Rectangle2D.OUT_TOP, "top outcode");
        check(r.outcode(11, 5) == Rectangle2D.OUT_RIGHT, "right outcode");
        check(r.outcode(5, 21) == Rectangle2D.OUT_BOTTOM, "bottom outcode");
        check(r.outcode(new Point2D.Double(5, 10)) == 0, "outcode(Point2D) inside");

        // contains
        check(r.contains(5, 10), "contains inside");
        check(!r.contains(-1, 0), "contains left edge excl");
        check(!r.contains(10, 5), "contains right edge excl");
        check(r.contains(0, 0), "contains top-left inclusive");
        check(r.contains(9.999, 19.999), "contains near bottom-right");
        check(r.contains(1, 1, 8, 18), "contains inner rect");
        check(!r.contains(1, 1, 12, 18), "contains wider rect false");

        // intersects
        check(r.intersects(5, 5, 20, 20), "intersects overlapping");
        check(!r.intersects(20, 20, 5, 5), "intersects disjoint");

        // intersectsLine
        check(r.intersectsLine(5, 25, 5, -5), "vertical line crosses");
        check(!r.intersectsLine(50, 50, 60, 60), "line outside");
        check(r.intersectsLine(new Line2D.Double(5, 25, 5, -5)), "intersectsLine(Line2D)");

        // static intersect / union
        Rectangle2D.Double a = new Rectangle2D.Double(0, 0, 10, 10);
        Rectangle2D.Double b = new Rectangle2D.Double(5, 5, 10, 10);
        Rectangle2D.Double dst = new Rectangle2D.Double();
        Rectangle2D.intersect(a, b, dst);
        check(close(dst.getX(), 5) && close(dst.getY(), 5) && close(dst.getWidth(), 5) && close(dst.getHeight(), 5),
              "static intersect result");
        Rectangle2D.union(a, b, dst);
        check(close(dst.getX(), 0) && close(dst.getY(), 0) && close(dst.getWidth(), 15) && close(dst.getHeight(), 15),
              "static union result");

        // instance createIntersection / createUnion
        Rectangle2D inter = a.createIntersection(b);
        check(close(inter.getX(), 5) && close(inter.getWidth(), 5), "createIntersection");
        Rectangle2D uni = a.createUnion(b);
        check(close(uni.getX(), 0) && close(uni.getWidth(), 15), "createUnion");
        check(a.createIntersection(new Rectangle2D.Double(20, 20, 5, 5)).isEmpty(),
              "disjoint createIntersection empty");

        // add
        Rectangle2D.Double acc = new Rectangle2D.Double(0, 0, 0, 0);
        acc.add(3, 4);
        acc.add(new Point2D.Double(1, 1));
        acc.add(new Rectangle2D.Double(-2, -2, 1, 1));
        check(close(acc.getMinX(), -2) && close(acc.getMinY(), -2)
              && close(acc.getMaxX(), 3) && close(acc.getMaxY(), 4), "add family expands bounds");

        // setRect(Rectangle2D) and setFrame
        r.setRect(new Rectangle2D.Double(1, 2, 3, 4));
        check(close(r.getX(), 1) && close(r.getY(), 2) && close(r.getWidth(), 3) && close(r.getHeight(), 4),
              "setRect(Rectangle2D)");
        r.setFrame(0, 0, 30, 40);
        check(close(r.getWidth(), 30) && close(r.getHeight(), 40), "setFrame updates width/height");

        // equals / hashCode
        check(new Rectangle2D.Double(0, 0, 10, 20).equals(new Rectangle2D.Double(0, 0, 10, 20)), "equal rects");
        check(!new Rectangle2D.Double(0, 0, 10, 20).equals(new Rectangle2D.Double(0, 0, 9, 20)), "different rects");
        check(new Rectangle2D.Double(0, 0, 10, 20).hashCode()
              == new Rectangle2D.Double(0, 0, 10, 20).hashCode(), "equal rects same hashCode");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
