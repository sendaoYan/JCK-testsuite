/*
 * @test
 * @summary java.awt.geom.RoundRectangle2D.Double conformance: white-box structure plus black-box behavior.
 * @run main DoubleTest
 */

import java.awt.geom.*;
import java.io.Serializable;
import java.lang.reflect.*;

public class DoubleTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean close(double a, double b) { return Math.abs(a - b) < 1e-9; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.geom.RoundRectangle2D$Double");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "RoundRectangle2D.Double is public");
        check(!Modifier.isAbstract(m), "RoundRectangle2D.Double is concrete");
        check(c.getSuperclass() == RoundRectangle2D.class, "RoundRectangle2D.Double extends RoundRectangle2D");
        check(Serializable.class.isAssignableFrom(c), "RoundRectangle2D.Double implements Serializable");
        for (String f : new String[]{"x", "y", "width", "height", "arcwidth", "archeight"}) {
            Field fd = c.getField(f);
            check(Modifier.isPublic(fd.getModifiers()) && fd.getType() == double.class, "public double field " + f);
        }
        check(c.getConstructors().length == 2, "two public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        RoundRectangle2D.Double r = new RoundRectangle2D.Double();
        check(r.x == 0 && r.y == 0 && r.width == 0 && r.height == 0 && r.arcwidth == 0 && r.archeight == 0, "default ctor zeros");
        check(r.isEmpty(), "default ctor empty");

        RoundRectangle2D.Double d = new RoundRectangle2D.Double(1, 2, 30, 40, 6, 8);
        check(close(d.getX(), 1) && close(d.getY(), 2), "6-double ctor x/y");
        check(close(d.getWidth(), 30) && close(d.getHeight(), 40), "6-double ctor w/h");
        check(close(d.getArcWidth(), 6) && close(d.getArcHeight(), 8), "6-double ctor arcs");
        check(d.x == 1 && d.width == 30 && d.arcwidth == 6 && d.archeight == 8, "public fields exposed");
        check(!d.isEmpty(), "non-empty");

        check(close(d.getX(), 1) && close(d.getY(), 2) && close(d.getWidth(), 30) && close(d.getHeight(), 40), "getters");

        d.setRoundRect(0, 0, 20, 20, 10, 10);
        check(close(d.getX(), 0) && close(d.getWidth(), 20) && close(d.getArcWidth(), 10), "setRoundRect(double...)");
        d.setRoundRect(new RoundRectangle2D.Float(0, 0, 12, 13, 2, 3));
        check(close(d.getWidth(), 12) && close(d.getArcWidth(), 2), "setRoundRect(RoundRectangle2D)");

        RoundRectangle2D.Double b = new RoundRectangle2D.Double(0, 0, 20, 20, 10, 10);
        Rectangle2D bb = b.getBounds2D();
        check(close(bb.getX(), 0) && close(bb.getY(), 0) && close(bb.getWidth(), 20) && close(bb.getHeight(), 20), "getBounds2D");

        check(b.contains(10, 10), "contains center");
        check(!b.contains(0.5, 0.5), "excludes corner");

        RoundRectangle2D.Double m = new RoundRectangle2D.Double();
        m.x = 1; m.y = 2; m.width = 30; m.height = 40; m.arcwidth = 5; m.archeight = 5;
        check(close(m.getX(), 1) && close(m.getWidth(), 30) && close(m.getArcWidth(), 5), "public field mutation");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
