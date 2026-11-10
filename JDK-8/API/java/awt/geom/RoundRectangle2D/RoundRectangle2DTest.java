/*
 * @test
 * @summary java.awt.geom.RoundRectangle2D conformance: white-box structure plus black-box behavior.
 * @run main RoundRectangle2DTest
 */

import java.awt.Shape;
import java.awt.geom.*;
import java.lang.reflect.*;

public class RoundRectangle2DTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String m) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(m + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(m + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static boolean close(double a, double b) { return Math.abs(a - b) < 1e-9; }

    static boolean hasMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getMethod(name, params); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.geom.RoundRectangle2D");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "RoundRectangle2D is public");
        check(Modifier.isAbstract(m), "RoundRectangle2D is abstract");
        check(c.getSuperclass() == RectangularShape.class, "RoundRectangle2D extends RectangularShape");
        check(Shape.class.isAssignableFrom(c), "RoundRectangle2D implements Shape");
        check(hasMethod(c, "getArcWidth") && hasMethod(c, "getArcHeight"), "abstract arc getters");
        check(hasMethod(c, "setRoundRect", double.class, double.class, double.class, double.class, double.class, double.class), "setRoundRect");
        check(hasMethod(c, "setRoundRect", RoundRectangle2D.class), "setRoundRect(RoundRectangle2D)");
        check(hasMethod(c, "contains", double.class, double.class), "contains");
        check(hasMethod(c, "intersects", double.class, double.class, double.class, double.class), "intersects");
        check(hasMethod(c, "getPathIterator", AffineTransform.class), "getPathIterator");
        check(hasMethod(c, "equals", Object.class) && hasMethod(c, "hashCode"), "equals/hashCode");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        RoundRectangle2D.Double rr = new RoundRectangle2D.Double(0, 0, 20, 20, 10, 10);
        check(close(rr.getArcWidth(), 10) && close(rr.getArcHeight(), 10), "getArcWidth/getArcHeight");

        check(rr.contains(10, 10), "rounded rect contains center");
        check(rr.contains(10, 1), "rounded rect contains edge midpoint");
        check(!rr.contains(0.5, 0.5), "rounded rect excludes corner");

        check(rr.contains(5, 5, 10, 10), "contains inner rectangle");
        check(!rr.contains(-1, -1, 2, 2), "does not contain rectangle crossing corner");
        check(rr.intersects(15, 15, 10, 10), "intersects overlapping rectangle");
        check(!rr.intersects(-5, -5, 5, 5), "does not intersect corner-only rectangle");

        Rectangle2D b = rr.getBounds2D();
        check(close(b.getX(), 0) && close(b.getY(), 0) && close(b.getWidth(), 20) && close(b.getHeight(), 20), "getBounds2D");

        // path iterator structure: MOVETO + 4 LINETO + 4 CUBICTO + CLOSE
        PathIterator it = rr.getPathIterator(null);
        double[] c = new double[6];
        int moveto = 0, lineto = 0, cubicto = 0, close = 0;
        while (!it.isDone()) {
            int seg = it.currentSegment(c);
            if (seg == PathIterator.SEG_MOVETO) moveto++;
            else if (seg == PathIterator.SEG_LINETO) lineto++;
            else if (seg == PathIterator.SEG_CUBICTO) cubicto++;
            else if (seg == PathIterator.SEG_CLOSE) close++;
            it.next();
        }
        check(moveto == 1 && lineto == 4 && cubicto == 4 && close == 1, "round rect iterator segment counts");

        // setRoundRect overloads
        rr.setRoundRect(1, 2, 30, 40, 5, 5);
        check(close(rr.getX(), 1) && close(rr.getWidth(), 30) && close(rr.getArcWidth(), 5) && close(rr.getArcHeight(), 5), "setRoundRect(double...)");
        rr.setRoundRect(new RoundRectangle2D.Float(0, 0, 10, 20, 2, 2));
        check(close(rr.getWidth(), 10) && close(rr.getHeight(), 20) && close(rr.getArcWidth(), 2), "setRoundRect(RoundRectangle2D)");

        // setFrame keeps arc
        rr.setFrame(5, 6, 50, 60);
        check(close(rr.getX(), 5) && close(rr.getY(), 6) && close(rr.getWidth(), 50) && close(rr.getHeight(), 60), "setFrame");
        check(close(rr.getArcWidth(), 2), "setFrame preserves arc");

        // equals / hashCode
        RoundRectangle2D.Double e1 = new RoundRectangle2D.Double(0, 0, 20, 20, 10, 10);
        RoundRectangle2D.Double e2 = new RoundRectangle2D.Double(0, 0, 20, 20, 10, 10);
        RoundRectangle2D.Double e3 = new RoundRectangle2D.Double(0, 0, 20, 20, 5, 5);
        check(e1.equals(e2), "equal round rects");
        check(e1.hashCode() == e2.hashCode(), "equal round rects hashCode");
        check(!e1.equals(e3), "different arc not equal");
    }

    // ===================== 契约深化：边界含/不含/圆角弧/arc=0/负弧/超宽弧/退化/NaN/扁平化 =====================
    static void testRoundRectContracts() {
        RoundRectangle2D.Double rr = new RoundRectangle2D.Double(0, 0, 20, 20, 10, 10);

        // 左/上边界含，右/下边界不含（bounding 判定 x >= rrx1 || y >= rry1 拒绝）。
        check(rr.contains(0, 10), "left edge contained");
        check(rr.contains(10, 0), "top edge contained");
        check(!rr.contains(20, 10), "right edge excluded");
        check(!rr.contains(10, 20), "bottom edge excluded");

        // 圆角弧：弧边界点含（<= 1.0），角点外不含，弧内点含。
        check(rr.contains(0, 5), "left arc boundary contained");
        check(rr.contains(5, 0), "top arc boundary contained");
        check(!rr.contains(0, 0), "corner outside arc excluded");
        check(rr.contains(2, 2), "point inside arc contained");
        check(!rr.contains(0.5, 0.5), "point outside arc excluded");
        check(rr.contains(10, 10), "center contained");

        // arc=0 退化为直角矩形：左/上角含，右/下角不含。
        RoundRectangle2D.Double r0 = new RoundRectangle2D.Double(0, 0, 20, 20, 0, 0);
        check(r0.contains(0, 0), "arc=0 top-left corner contained");
        check(r0.contains(19, 19), "arc=0 interior contained");
        check(!r0.contains(20, 20), "arc=0 bottom-right corner excluded");
        check(!r0.contains(0, 20), "arc=0 bottom-left edge excluded");

        // 负弧宽：按绝对值取 aw，行为与正弧一致。
        RoundRectangle2D.Double rneg = new RoundRectangle2D.Double(0, 0, 20, 20, -10, 10);
        check(rneg.contains(2, 2), "negative arcwidth treated as |arc|");
        check(!rneg.contains(0, 0), "negative arcwidth corner still excluded");

        // 超宽弧（arcwidth > width）：aw 被钳制到 width/2。
        RoundRectangle2D.Double rbig = new RoundRectangle2D.Double(0, 0, 20, 20, 100, 100);
        check(rbig.contains(10, 10), "oversized arc clamped, center contained");

        // 退化（宽或高为 0）：isEmpty true，contains/intersects 恒 false。
        RoundRectangle2D.Double rw0 = new RoundRectangle2D.Double(0, 0, 0, 20, 10, 10);
        check(rw0.isEmpty(), "zero width isEmpty");
        check(!rw0.contains(0, 10), "zero width contains false");
        check(!rw0.intersects(0, 0, 5, 5), "zero width intersects false");

        // NaN 点：两坐标均 NaN 时中心带/圆角判定皆失败，恒 false。
        check(!rr.contains(Double.NaN, Double.NaN), "contains(NaN, NaN) false");

        // intersects：贴右边界（严格不交）false，不相交 false。
        check(!rr.intersects(20, 10, 5, 5), "rect touching right boundary does not intersect");
        check(!rr.intersects(-5, -5, 4, 4), "disjoint rect does not intersect");
        check(rr.intersects(15, 15, 10, 10), "overlapping rect intersects");

        // equals 跨 Double/Float（比较含弧宽弧高的 double 值）。
        check(rr.equals(new RoundRectangle2D.Float(0, 0, 20, 20, 10, 10)), "Double equals Float with same values");
        check(!rr.equals(new RoundRectangle2D.Float(0, 0, 20, 20, 5, 10)), "different arc not equal");

        // setRoundRect(RoundRectangle2D) 传 null 抛 NPE。
        checkThrows(NullPointerException.class, () -> rr.setRoundRect((RoundRectangle2D) null), "setRoundRect(null) NPE");

        // 非零环绕规则 + 扁平化迭代器（MOVETO + 多个 LINETO + CLOSE，无 CUBICTO）。
        check(rr.getPathIterator(null).getWindingRule() == PathIterator.WIND_NON_ZERO, "winding rule WIND_NON_ZERO");
        PathIterator fi = rr.getPathIterator(null, 0.5);
        double[] fc = new double[6];
        int fmv = 0, fln = 0, fcb = 0, fcl = 0;
        while (!fi.isDone()) {
            int seg = fi.currentSegment(fc);
            if (seg == PathIterator.SEG_MOVETO) fmv++;
            else if (seg == PathIterator.SEG_LINETO) fln++;
            else if (seg == PathIterator.SEG_CUBICTO) fcb++;
            else if (seg == PathIterator.SEG_CLOSE) fcl++;
            fi.next();
        }
        check(fmv == 1 && fcl == 1 && fcb == 0 && fln >= 8, "flattened round rect iterator is LINETO-only");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testRoundRectContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
