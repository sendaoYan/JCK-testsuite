/*
 * @test
 * @summary java.awt.geom.Path2D conformance: white-box structure plus black-box behavior.
 * @run main Path2DTest
 */

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.*;
import java.lang.reflect.*;

public class Path2DTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getMethod(name, params); return true; } catch (NoSuchMethodException e) { return false; }
    }

    static int segCount(PathIterator it) {
        int n = 0;
        double[] c = new double[6];
        while (!it.isDone()) { it.currentSegment(c); n++; it.next(); }
        return n;
    }

    static boolean close(double a, double b) { return Math.abs(a - b) < 1e-9; }

    static void expectThrows(Class<? extends RuntimeException> ex, Runnable r, String msg) {
        try {
            r.run();
            check(false, msg + " (no exception)");
        } catch (RuntimeException e) {
            check(ex.isAssignableFrom(e.getClass()), msg + " (got " + e.getClass().getSimpleName() + ")");
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.geom.Path2D");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "Path2D is public");
        check(Modifier.isAbstract(m), "Path2D is abstract");
        check(c.isAssignableFrom(Path2D.Float.class), "Path2D.Float extends Path2D");
        check(c.isAssignableFrom(Path2D.Double.class), "Path2D.Double extends Path2D");
        check(Shape.class.isAssignableFrom(c), "Path2D implements Shape");
        check(Cloneable.class.isAssignableFrom(c), "Path2D implements Cloneable");

        check(Path2D.WIND_EVEN_ODD == 0, "WIND_EVEN_ODD == 0");
        check(Path2D.WIND_NON_ZERO == 1, "WIND_NON_ZERO == 1");

        check(Modifier.isAbstract(c.getMethod("moveTo", double.class, double.class).getModifiers()), "moveTo abstract");
        check(Modifier.isAbstract(c.getMethod("lineTo", double.class, double.class).getModifiers()), "lineTo abstract");
        check(Modifier.isAbstract(c.getMethod("quadTo", double.class, double.class, double.class, double.class).getModifiers()), "quadTo abstract");
        check(Modifier.isAbstract(c.getMethod("curveTo", double.class, double.class, double.class, double.class, double.class, double.class).getModifiers()), "curveTo abstract");
        check(hasMethod(c, "closePath"), "closePath");
        check(hasMethod(c, "getWindingRule"), "getWindingRule");
        check(hasMethod(c, "setWindingRule", int.class), "setWindingRule");
        check(hasMethod(c, "getCurrentPoint"), "getCurrentPoint");
        check(hasMethod(c, "reset"), "reset");
        check(hasMethod(c, "createTransformedShape", AffineTransform.class), "createTransformedShape");
        check(hasMethod(c, "contains", double.class, double.class), "contains(x,y)");
        check(hasMethod(c, "intersects", double.class, double.class, double.class, double.class), "intersects");
        check(hasMethod(c, "getPathIterator", AffineTransform.class, double.class), "getPathIterator");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // winding rule: outer + inner square same direction
        Path2D.Double nz = new Path2D.Double(Path2D.WIND_NON_ZERO);
        nz.moveTo(0, 0); nz.lineTo(10, 0); nz.lineTo(10, 10); nz.lineTo(0, 10); nz.closePath();
        nz.moveTo(2, 2); nz.lineTo(8, 2); nz.lineTo(8, 8); nz.lineTo(2, 8); nz.closePath();
        check(nz.contains(5, 5), "NON_ZERO same-direction inner square filled");
        check(nz.contains(1, 1), "NON_ZERO ring (between squares) filled");

        Path2D.Double eo = new Path2D.Double(Path2D.WIND_EVEN_ODD);
        eo.moveTo(0, 0); eo.lineTo(10, 0); eo.lineTo(10, 10); eo.lineTo(0, 10); eo.closePath();
        eo.moveTo(2, 2); eo.lineTo(8, 2); eo.lineTo(8, 8); eo.lineTo(2, 8); eo.closePath();
        check(!eo.contains(5, 5), "EVEN_ODD same-direction inner square is hole");
        check(eo.contains(1, 1), "EVEN_ODD ring filled");

        Path2D.Double p = new Path2D.Double();
        check(p.getWindingRule() == Path2D.WIND_NON_ZERO, "default winding rule NON_ZERO");
        p.setWindingRule(Path2D.WIND_EVEN_ODD);
        check(p.getWindingRule() == Path2D.WIND_EVEN_ODD, "setWindingRule EVEN_ODD");
        boolean badRule = false;
        try { p.setWindingRule(99); } catch (IllegalArgumentException e) { badRule = true; }
        check(badRule, "setWindingRule(invalid) throws IllegalArgumentException");

        // current point lifecycle
        Path2D.Double cp = new Path2D.Double();
        cp.moveTo(1, 2);
        check(cp.getCurrentPoint().equals(new Point2D.Double(1, 2)), "current point after moveTo");
        cp.lineTo(3, 4);
        check(cp.getCurrentPoint().equals(new Point2D.Double(3, 4)), "current point after lineTo");
        cp.closePath();
        check(cp.getCurrentPoint().equals(new Point2D.Double(1, 2)), "current point after closePath == subpath start");

        // bounds
        check(cp.getBounds2D().equals(new Rectangle2D.Double(1, 2, 2, 2)), "getBounds2D of two-segment path");
        Rectangle b = cp.getBounds();
        check(b.x == 1 && b.y == 2 && b.width == 2 && b.height == 2, "getBounds integer");

        // reset
        cp.reset();
        check(cp.getCurrentPoint() == null, "reset clears current point");
        check(cp.getBounds2D().isEmpty(), "reset clears geometry");

        // contains / intersects / static helpers
        Path2D.Double sq = new Path2D.Double();
        sq.moveTo(0, 0); sq.lineTo(10, 0); sq.lineTo(10, 10); sq.lineTo(0, 10); sq.closePath();
        check(sq.contains(5, 5), "square contains center");
        check(!sq.contains(15, 5), "square does not contain outside point");
        check(sq.contains(new Point2D.Double(5, 5)), "contains(Point2D)");
        check(sq.contains(1, 1, 2, 2), "contains inner rectangle");
        check(!sq.contains(-1, -1, 2, 2), "does not contain partially-outside rectangle");
        check(sq.intersects(9, 9, 5, 5), "intersects overlapping rectangle");
        check(!sq.intersects(20, 20, 5, 5), "does not intersect far rectangle");

        PathIterator pi = sq.getPathIterator(null);
        check(Path2D.contains(pi, 5, 5), "static contains(PathIterator,x,y)");

        // createTransformedShape
        Shape shifted = sq.createTransformedShape(AffineTransform.getTranslateInstance(5, 5));
        check(shifted.getBounds2D().equals(new Rectangle2D.Double(5, 5, 10, 10)), "createTransformedShape translate");

        // append(Shape, connect)
        Path2D.Double ap = new Path2D.Double();
        ap.moveTo(0, 0);
        ap.append(new Rectangle2D.Double(0, 0, 10, 10), false);
        check(ap.getBounds2D().equals(new Rectangle2D.Double(0, 0, 10, 10)), "append rectangle geometry");
        check(segCount(ap.getPathIterator(null)) >= 5, "append yields iterable segments");

        // append(PathIterator, connect=true) emits a connecting LINETO from current point to sub-path start
        Path2D.Double line1 = new Path2D.Double();
        line1.moveTo(0, 0); line1.lineTo(5, 0);
        Path2D.Double line2 = new Path2D.Double();
        line2.moveTo(5, 5); line2.lineTo(10, 5);
        Path2D.Double connected = new Path2D.Double();
        connected.append(line1.getPathIterator(null), false);
        connected.append(line2.getPathIterator(null), true);
        int moves = 0, lines = 0;
        PathIterator cit = connected.getPathIterator(null);
        double[] cc = new double[6];
        while (!cit.isDone()) {
            int t = cit.currentSegment(cc);
            if (t == PathIterator.SEG_MOVETO) moves++;
            else if (t == PathIterator.SEG_LINETO) lines++;
            cit.next();
        }
        check(moves == 1, "connect=true keeps a single MOVETO (no new sub-path)");
        check(lines == 3, "connect=true adds one joining LINETO (3 total)");

        // append(..., false) starts an independent sub-path instead
        Path2D.Double separate = new Path2D.Double();
        separate.append(line1.getPathIterator(null), false);
        separate.append(line2.getPathIterator(null), false);
        int moves2 = 0;
        PathIterator sit = separate.getPathIterator(null);
        while (!sit.isDone()) {
            if (sit.currentSegment(cc) == PathIterator.SEG_MOVETO) moves2++;
            sit.next();
        }
        check(moves2 == 2, "connect=false starts a second sub-path (2 MOVETOs)");

        // contains honours the winding rule across implementations
        Path2D.Float fnz = new Path2D.Float(Path2D.WIND_NON_ZERO);
        fnz.moveTo(0, 0); fnz.lineTo(10, 0); fnz.lineTo(10, 10); fnz.lineTo(0, 10); fnz.closePath();
        check(fnz.contains(5, 5), "Path2D.Float NON_ZERO square contains center");
    }

    // ===================== 契约深化：未 moveTo 异常、负 flatness、反向绕组、退化路径、变换独立性 =====================
    static void testPathContracts() {
        // 未 moveTo 直接画图：IllegalPathStateException。
        final Path2D.Double fresh = new Path2D.Double();
        expectThrows(IllegalPathStateException.class, new Runnable() { public void run() { fresh.lineTo(1, 1); } },
                     "lineTo before moveTo throws IllegalPathStateException");
        expectThrows(IllegalPathStateException.class, new Runnable() { public void run() { fresh.quadTo(1, 1, 2, 2); } },
                     "quadTo before moveTo throws IllegalPathStateException");
        expectThrows(IllegalPathStateException.class, new Runnable() { public void run() { fresh.curveTo(1, 1, 2, 2, 3, 3); } },
                     "curveTo before moveTo throws IllegalPathStateException");

        // getPathIterator(at, 负 flatness) 抛 IllegalArgumentException。
        Path2D.Double flat = new Path2D.Double();
        flat.moveTo(0, 0); flat.lineTo(10, 0);
        expectThrows(IllegalArgumentException.class, new Runnable() { public void run() { flat.getPathIterator(null, -1.0); } },
                     "getPathIterator negative flatness throws IllegalArgumentException");

        // 反向绕组的内方形：NON_ZERO 与 EVEN_ODD 都成洞（中心不含，环带含）。
        Path2D.Double rev = new Path2D.Double(Path2D.WIND_NON_ZERO);
        rev.moveTo(0, 0); rev.lineTo(10, 0); rev.lineTo(10, 10); rev.lineTo(0, 10); rev.closePath();
        rev.moveTo(2, 2); rev.lineTo(2, 8); rev.lineTo(8, 8); rev.lineTo(8, 2); rev.closePath();
        check(!rev.contains(5, 5), "NON_ZERO reverse-wound inner square is a hole");
        check(rev.contains(1, 1), "NON_ZERO ring between squares still filled");
        Path2D.Double eoRev = new Path2D.Double(Path2D.WIND_EVEN_ODD);
        eoRev.moveTo(0, 0); eoRev.lineTo(10, 0); eoRev.lineTo(10, 10); eoRev.lineTo(0, 10); eoRev.closePath();
        eoRev.moveTo(2, 2); eoRev.lineTo(2, 8); eoRev.lineTo(8, 8); eoRev.lineTo(8, 2); eoRev.closePath();
        check(!eoRev.contains(5, 5), "EVEN_ODD reverse-wound inner square is a hole");
        check(eoRev.contains(1, 1), "EVEN_ODD ring between squares still filled");

        // 单 moveTo 退化路径：无面积，getBounds2D 为空，不含自身点。
        Path2D.Double single = new Path2D.Double();
        single.moveTo(3, 4);
        check(single.getBounds2D().isEmpty(), "single moveTo path has empty bounds");
        check(!single.contains(3, 4), "single moveTo path contains nothing");

        // transform(AffineTransform) 原地平移。
        Path2D.Double tr = new Path2D.Double();
        tr.moveTo(0, 0); tr.lineTo(10, 0);
        tr.transform(AffineTransform.getTranslateInstance(5, 0));
        check(close(tr.getBounds2D().getX(), 5) && close(tr.getBounds2D().getWidth(), 10), "transform translates in place");

        // createTransformedShape 返回同类新对象，且与原对象独立。
        Path2D.Double orig = new Path2D.Double();
        orig.moveTo(0, 0); orig.lineTo(10, 0);
        Shape shifted = orig.createTransformedShape(AffineTransform.getTranslateInstance(0, 5));
        orig.lineTo(100, 100);
        check(shifted instanceof Path2D.Double, "createTransformedShape returns Path2D.Double");
        check(close(shifted.getBounds2D().getX(), 0) && close(shifted.getBounds2D().getY(), 5)
              && close(shifted.getBounds2D().getWidth(), 10), "createTransformedShape independent of original");

        // NaN 点永远 false（contains / 静态 contains）。
        Path2D.Double sq = new Path2D.Double();
        sq.moveTo(0, 0); sq.lineTo(10, 0); sq.lineTo(10, 10); sq.lineTo(0, 10); sq.closePath();
        check(!sq.contains(Double.NaN, 5), "contains(NaN,5) false");
        check(!Path2D.contains(sq.getPathIterator(null), Double.NaN, 5), "static contains(pi, NaN,5) false");

        // 静态 contains/intersects 各种重载。
        check(Path2D.contains(sq.getPathIterator(null), new Point2D.Double(5, 5)), "static contains(pi, Point2D)");
        check(Path2D.contains(sq.getPathIterator(null), new Rectangle2D.Double(1, 1, 2, 2)), "static contains(pi, Rectangle2D)");
        check(Path2D.contains(sq.getPathIterator(null), 1, 1, 2, 2), "static contains(pi, x,y,w,h)");
        check(Path2D.intersects(sq.getPathIterator(null), 9, 9, 5, 5), "static intersects(pi, x,y,w,h)");
        check(!Path2D.intersects(sq.getPathIterator(null), new Rectangle2D.Double(20, 20, 5, 5)),
              "static intersects(pi, Rectangle2D) disjoint");

        // quadTo/curveTo 更新 currentPoint 到终点。
        Path2D.Double cur = new Path2D.Double();
        cur.moveTo(0, 0);
        cur.quadTo(5, 5, 10, 0);
        check(cur.getCurrentPoint().equals(new Point2D.Double(10, 0)), "quadTo updates current point to end");
        cur.curveTo(10, 10, 20, 10, 20, 0);
        check(cur.getCurrentPoint().equals(new Point2D.Double(20, 0)), "curveTo updates current point to end");

        // flatness=0 的直线原样通过（2 段）。
        Path2D.Double line = new Path2D.Double();
        line.moveTo(0, 0); line.lineTo(10, 0);
        check(segCount(line.getPathIterator(null, 0.0)) == 2, "flatness 0 line iterator keeps 2 segments");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testPathContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
