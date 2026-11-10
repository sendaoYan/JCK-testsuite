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

        // cross-implementation equality with java.awt.Rectangle (same doubles, different class)
        java.awt.Rectangle awtR = new java.awt.Rectangle(1, 2, 3, 4);
        Rectangle2D.Double rd = new Rectangle2D.Double(1, 2, 3, 4);
        check(awtR.equals(rd), "java.awt.Rectangle.equals(Rectangle2D.Double) is value-based");
        check(rd.equals(awtR), "Rectangle2D.Double.equals(java.awt.Rectangle) is value-based");
        check(awtR.hashCode() == rd.hashCode(), "cross-impl equal rects share hashCode");
        check(!new java.awt.Rectangle(1, 2, 3, 5).equals(rd), "cross-impl different size not equal");

        // negative extent: empty, contains nothing, intersects nothing (Rectangle2D does NOT normalize)
        Rectangle2D.Double neg = new Rectangle2D.Double(0, 0, -10, 20);
        check(neg.isEmpty(), "negative width isEmpty");
        check(!neg.contains(-1, 5), "negative width contains nothing");
        check(!neg.intersects(0, 0, 5, 5), "negative width intersects nothing");
        check(neg.getBounds2D().getWidth() == -10.0, "negative width preserved (no normalization)");

        // degenerate shapes: zero-height line and zero-size point never contain their own points
        Rectangle2D.Double line = new Rectangle2D.Double(0, 0, 10, 0);
        check(line.isEmpty(), "zero-height line isEmpty");
        check(!line.contains(5, 0), "line does not contain its own points");
        Rectangle2D.Double point = new Rectangle2D.Double(5, 5, 0, 0);
        check(point.isEmpty(), "zero-size point isEmpty");
        check(!point.contains(5, 5), "point does not contain itself");

        // boundary intersections: tangent (shared edge) degenerates to zero width -> empty
        Rectangle2D.Double dst2 = new Rectangle2D.Double();
        Rectangle2D.intersect(new Rectangle2D.Double(0, 0, 10, 10), new Rectangle2D.Double(10, 0, 5, 5), dst2);
        check(close(dst2.getX(), 10) && close(dst2.getWidth(), 0) && dst2.isEmpty(),
              "tangent intersection is zero-width empty");
        // disjoint intersection yields negative extent (documented: result only meaningful if !isEmpty)
        Rectangle2D.intersect(new Rectangle2D.Double(0, 0, 10, 10), new Rectangle2D.Double(20, 20, 5, 5), dst2);
        check(dst2.isEmpty() && dst2.getWidth() < 0, "disjoint intersection empty with negative extent");
        // union with an empty rect is the original
        Rectangle2D.union(new Rectangle2D.Double(0, 0, 10, 10), new Rectangle2D.Double(0, 0, 0, 0), dst2);
        check(close(dst2.getWidth(), 10) && close(dst2.getHeight(), 10), "union with empty rect keeps original");
        // containment case: intersection equals the inner rectangle
        Rectangle2D inner = new Rectangle2D.Double(2, 3, 4, 5).createIntersection(new Rectangle2D.Double(0, 0, 10, 10));
        check(close(inner.getX(), 2) && close(inner.getY(), 3) && close(inner.getWidth(), 4) && close(inner.getHeight(), 5),
              "createIntersection containment returns inner rect");
        // createIntersection with a negative rect keeps negative extent (no normalization)
        Rectangle2D negInter = new Rectangle2D.Double(0, 0, 10, 10).createIntersection(new Rectangle2D.Double(0, 0, -5, 5));
        check(negInter.getWidth() == -5.0, "createIntersection preserves negative extent");

        // outcode corner combinations: left|top, right|bottom, and the excluded right/bottom edges
        Rectangle2D.Double oc = new Rectangle2D.Double(0, 0, 10, 10);
        check(oc.outcode(-1, -1) == (Rectangle2D.OUT_LEFT | Rectangle2D.OUT_TOP), "outcode corner left|top");
        check(oc.outcode(11, 21) == (Rectangle2D.OUT_RIGHT | Rectangle2D.OUT_BOTTOM), "outcode corner right|bottom");
        check(oc.outcode(10, 20) == Rectangle2D.OUT_BOTTOM, "right/bottom edges count as out, left/top do not");
        check(oc.outcode(0, 0) == 0, "top-left corner is inside");

        // serialization round-trip preserves concrete class and doubles
        Rectangle2D.Double sd = roundTrip(new Rectangle2D.Double(1.5, 2.5, 3.5, 4.5));
        check(sd instanceof Rectangle2D.Double, "Rectangle2D.Double round-trip class");
        check(close(sd.getX(), 1.5) && close(sd.getY(), 2.5) && close(sd.getWidth(), 3.5) && close(sd.getHeight(), 4.5),
              "Rectangle2D.Double round-trip fields");
        Rectangle2D.Float sf = roundTrip(new Rectangle2D.Float(1.5f, 2.5f, 3.5f, 4.5f));
        check(sf instanceof Rectangle2D.Float, "Rectangle2D.Float round-trip class");
        check(close(sf.getX(), 1.5) && close(sf.getWidth(), 3.5), "Rectangle2D.Float round-trip fields");
    }

    @SuppressWarnings("unchecked")
    static <T> T roundTrip(T obj) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(bos).writeObject(obj);
        return (T) new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
    }

    // ===================== 契约深化：outcode 边界、NaN/零尺寸判定、退化线、分离并集 =====================
    static void testRectangle2DContracts() {
        // outcode 右/下边（严格大于才算 out）：边界点返回 0，只有严格越界才置位。
        Rectangle2D.Double r = new Rectangle2D.Double(0, 0, 10, 20);
        check(r.outcode(10, 5) == 0, "outcode right edge x=10 is 0 (strictly greater to be out)");
        check(r.outcode(5, 20) == 0, "outcode bottom edge y=20 is 0 (strictly greater to be out)");
        check(r.outcode(10, 20) == 0, "outcode bottom-right corner is 0");
        check(r.outcode(11, 5) == Rectangle2D.OUT_RIGHT, "outcode x=11 is OUT_RIGHT");
        check(r.outcode(5, 21) == Rectangle2D.OUT_BOTTOM, "outcode y=21 is OUT_BOTTOM");
        check(r.outcode(Double.NaN, 5) == 0, "outcode(NaN) is 0 (NaN never out)");

        // NaN 参数在 contains/intersects 恒 false。
        check(!r.contains(Double.NaN, 5), "contains(NaN,5) false");
        check(!r.intersects(Double.NaN, 5, 1, 1), "intersects(NaN rect) false");

        // 零尺寸输入矩形：intersects 恒 false，contains 恒 false。
        check(!r.intersects(0, 0, 0, 0), "intersects zero-size rect false");
        check(!r.contains(0, 0, 0, 0), "contains zero-size rect false");

        // intersectsLine 退化线段（点）：点在内则 true，在外则 false。
        check(r.intersectsLine(5, 5, 5, 5), "intersectsLine degenerate point inside true");
        check(!r.intersectsLine(50, 50, 50, 50), "intersectsLine degenerate point outside false");

        // 分离矩形的静态 union / 实例 createUnion 得到最小外接框。
        Rectangle2D.Double dstU = new Rectangle2D.Double();
        Rectangle2D.union(new Rectangle2D.Double(0, 0, 10, 10), new Rectangle2D.Double(20, 20, 5, 5), dstU);
        check(close(dstU.getX(), 0) && close(dstU.getY(), 0) && close(dstU.getWidth(), 25) && close(dstU.getHeight(), 25),
              "static union of disjoint rects is bounding box");
        Rectangle2D cu = new Rectangle2D.Double(0, 0, 10, 10).createUnion(new Rectangle2D.Double(20, 20, 5, 5));
        check(close(cu.getWidth(), 25) && close(cu.getHeight(), 25), "createUnion of disjoint rects is bounding box");

        // createIntersection 自身返回等值矩形。
        Rectangle2D self = new Rectangle2D.Double(3, 4, 5, 6).createIntersection(new Rectangle2D.Double(3, 4, 5, 6));
        check(close(self.getX(), 3) && close(self.getY(), 4) && close(self.getWidth(), 5) && close(self.getHeight(), 6),
              "createIntersection with itself returns equal rectangle");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testRectangle2DContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
