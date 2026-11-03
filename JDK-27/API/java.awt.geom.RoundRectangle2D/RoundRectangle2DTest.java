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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
