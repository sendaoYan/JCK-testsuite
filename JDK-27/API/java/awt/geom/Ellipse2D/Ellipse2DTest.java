/*
 * @test
 * @summary java.awt.geom.Ellipse2D conformance: white-box structure plus black-box behavior.
 * @run main Ellipse2DTest
 */

import java.awt.Shape;
import java.awt.geom.*;
import java.lang.reflect.*;

public class Ellipse2DTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean close(double a, double b) { return Math.abs(a - b) < 1e-9; }

    static boolean hasMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getMethod(name, params); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.geom.Ellipse2D");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "Ellipse2D is public");
        check(Modifier.isAbstract(m), "Ellipse2D is abstract");
        check(c.getSuperclass() == RectangularShape.class, "Ellipse2D extends RectangularShape");
        check(Shape.class.isAssignableFrom(c), "Ellipse2D implements Shape");
        check(hasMethod(c, "contains", double.class, double.class), "contains");
        check(hasMethod(c, "intersects", double.class, double.class, double.class, double.class), "intersects");
        check(hasMethod(c, "getPathIterator", AffineTransform.class), "getPathIterator");
        check(hasMethod(c, "equals", Object.class) && hasMethod(c, "hashCode"), "equals/hashCode");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Ellipse2D.Double e = new Ellipse2D.Double(0, 0, 20, 20);

        check(e.contains(10, 10), "ellipse contains center");
        check(!e.contains(20, 10), "ellipse excludes boundary (right)");
        check(!e.contains(10, 0), "ellipse excludes boundary (top)");
        check(!e.contains(0, 0), "ellipse excludes corner");
        check(!e.contains(10, 25), "ellipse excludes outside");
        check(e.contains(5, 5, 10, 10), "contains inner rectangle");
        check(!e.contains(0, 0, 20, 20), "does not contain full frame rectangle");
        check(e.intersects(15, 15, 10, 10), "intersects overlapping rectangle");

        Rectangle2D b = e.getBounds2D();
        check(close(b.getX(), 0) && close(b.getY(), 0) && close(b.getWidth(), 20) && close(b.getHeight(), 20), "getBounds2D");

        // path iterator: MOVETO + 4 CUBICTO + CLOSE
        PathIterator it = e.getPathIterator(null);
        double[] c = new double[6];
        int moveto = 0, cubic = 0, close = 0;
        while (!it.isDone()) {
            int seg = it.currentSegment(c);
            if (seg == PathIterator.SEG_MOVETO) moveto++;
            else if (seg == PathIterator.SEG_CUBICTO) cubic++;
            else if (seg == PathIterator.SEG_CLOSE) close++;
            it.next();
        }
        check(moveto == 1 && cubic == 4 && close == 1, "ellipse iterator MOVETO + 4 CUBICTO + CLOSE");

        // equals / hashCode
        Ellipse2D.Double same = new Ellipse2D.Double(0, 0, 20, 20);
        Ellipse2D.Double diff = new Ellipse2D.Double(0, 0, 10, 20);
        check(e.equals(same), "equal ellipses");
        check(e.hashCode() == same.hashCode(), "equal ellipses hashCode");
        check(!e.equals(diff), "different ellipse not equal");
    }

    // ===================== 契约深化：边界点/退化/NaN/负宽高/equals 跨实现/setFrame/扁平化迭代器 =====================
    static void testEllipseContracts() {
        Ellipse2D.Double e = new Ellipse2D.Double(0, 0, 20, 20);

        // 四边中点均在边界上，contains 严格排除边界（normx^2+normy^2 < 0.25）。
        check(!e.contains(0, 10), "left boundary point excluded");
        check(!e.contains(20, 10), "right boundary point excluded");
        check(!e.contains(10, 0), "top boundary point excluded");
        check(!e.contains(10, 20), "bottom boundary point excluded");
        check(e.contains(10, 10), "center contained");
        check(e.contains(10, 5), "interior point contained");

        // contains(x,y,w,h)：四角都含才算含；零面积矩形在内部点返回 true（四角重合）。
        check(e.contains(5, 5, 10, 10), "inner rect contained");
        check(!e.contains(0, 0, 20, 20), "frame rect not contained (corners on boundary)");
        check(e.contains(5, 5, 0, 0), "zero-area rect at interior point contained");
        check(!e.contains(0, 0, 0, 10), "degenerate rect at corner not contained");

        // intersects：贴右边界（严格不交）false，稍进 true，不相交 false。
        check(e.intersects(15, 15, 10, 10), "overlapping rect intersects");
        check(!e.intersects(20, 10, 5, 5), "rect touching right boundary does not intersect");
        check(e.intersects(19, 10, 5, 5), "rect just inside intersects");
        check(!e.intersects(-5, -5, 4, 4), "disjoint rect does not intersect");

        // NaN 点：严格不等式下恒 false。
        check(!e.contains(Double.NaN, 10), "contains(NaN, y) false");
        check(!e.contains(10, Double.NaN), "contains(x, NaN) false");

        // 退化（宽或高为 0）：contains/intersects 恒 false，isEmpty true，getBounds2D 保留 0 宽。
        Ellipse2D.Double w0 = new Ellipse2D.Double(0, 0, 0, 20);
        check(w0.isEmpty(), "zero width isEmpty");
        check(!w0.contains(0, 10), "zero width contains false");
        check(!w0.intersects(0, 0, 5, 5), "zero width intersects false");
        Rectangle2D b0 = w0.getBounds2D();
        check(close(b0.getX(), 0) && close(b0.getY(), 0) && close(b0.getWidth(), 0) && close(b0.getHeight(), 20), "zero width bounds2D");

        // 负宽/高：isEmpty true，contains false。
        Ellipse2D.Double neg = new Ellipse2D.Double(0, 0, -20, 20);
        check(neg.isEmpty(), "negative width isEmpty");
        check(!neg.contains(5, 5), "negative width contains false");

        // NaN 宽：isEmpty 不置真（NaN <= 0 为 false），contains 因 NaN 比较为 false。
        Ellipse2D.Double nanW = new Ellipse2D.Double(0, 0, Double.NaN, 20);
        check(!nanW.isEmpty(), "NaN width not treated as empty");
        check(Double.isNaN(nanW.getWidth()), "NaN width stored");
        check(!nanW.contains(5, 5), "NaN width contains false");

        // equals 跨 Double/Float（比较 getX/getY/getWidth/getHeight 的 double 值）。
        check(e.equals(new Ellipse2D.Float(0, 0, 20, 20)), "Double equals Float with same coords");
        check(!e.equals(new Ellipse2D.Float(0, 0, 10, 20)), "different size not equal");

        // 非零环绕规则 + 扁平化迭代器（MOVETO + 多个 LINETO + CLOSE，无 CUBICTO）。
        check(e.getPathIterator(null).getWindingRule() == PathIterator.WIND_NON_ZERO, "winding rule WIND_NON_ZERO");
        PathIterator fi = e.getPathIterator(null, 0.5);
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
        check(fmv == 1 && fcl == 1 && fcb == 0 && fln >= 8, "flattened ellipse iterator is LINETO-only");

        // setFrame 重置几何，contains 随之变化。
        Ellipse2D.Double r = new Ellipse2D.Double();
        r.setFrame(0, 0, 10, 10);
        check(close(r.getWidth(), 10) && close(r.getHeight(), 10), "setFrame resets size");
        check(r.contains(5, 5), "setFrame center still contained");
        check(!r.contains(5, 0), "setFrame top boundary excluded");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEllipseContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
