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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
