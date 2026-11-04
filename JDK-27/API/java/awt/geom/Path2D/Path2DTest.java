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
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
