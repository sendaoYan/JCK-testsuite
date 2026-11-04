/*
 * @test
 * @summary java.awt.geom.RoundRectangle2D.Float conformance: white-box structure plus black-box behavior.
 * @run main FloatTest
 */

import java.awt.geom.*;
import java.io.Serializable;
import java.lang.reflect.*;

public class FloatTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean close(double a, double b) { return Math.abs(a - b) < 1e-9; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.geom.RoundRectangle2D$Float");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "RoundRectangle2D.Float is public");
        check(!Modifier.isAbstract(m), "RoundRectangle2D.Float is concrete");
        check(c.getSuperclass() == RoundRectangle2D.class, "RoundRectangle2D.Float extends RoundRectangle2D");
        check(Serializable.class.isAssignableFrom(c), "RoundRectangle2D.Float implements Serializable");
        for (String f : new String[]{"x", "y", "width", "height", "arcwidth", "archeight"}) {
            Field fd = c.getField(f);
            check(Modifier.isPublic(fd.getModifiers()) && fd.getType() == float.class, "public float field " + f);
        }
        check(c.getConstructors().length == 2, "two public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        RoundRectangle2D.Float r = new RoundRectangle2D.Float();
        check(r.x == 0f && r.y == 0f && r.width == 0f && r.height == 0f && r.arcwidth == 0f && r.archeight == 0f, "default ctor zeros");
        check(r.isEmpty(), "default ctor empty");

        RoundRectangle2D.Float f = new RoundRectangle2D.Float(1f, 2f, 30f, 40f, 6f, 8f);
        check(close(f.getX(), 1) && close(f.getY(), 2), "6-float ctor x/y");
        check(close(f.getWidth(), 30) && close(f.getHeight(), 40), "6-float ctor w/h");
        check(close(f.getArcWidth(), 6) && close(f.getArcHeight(), 8), "6-float ctor arcs");
        check(f.x == 1f && f.width == 30f && f.arcwidth == 6f && f.archeight == 8f, "public fields exposed");
        check(!f.isEmpty(), "non-empty");

        check(close(f.getX(), 1) && close(f.getY(), 2) && close(f.getWidth(), 30) && close(f.getHeight(), 40), "getters");

        f.setRoundRect(0f, 0f, 20f, 20f, 10f, 10f);
        check(close(f.getX(), 0) && close(f.getWidth(), 20) && close(f.getArcWidth(), 10), "setRoundRect(float...)");
        f.setRoundRect(2.5, 3.5, 4.5, 5.5, 6.5, 7.5);
        check(close(f.getX(), 2.5) && close(f.getWidth(), 4.5) && close(f.getArcHeight(), 7.5), "setRoundRect(double...)");
        f.setRoundRect(new RoundRectangle2D.Double(0, 0, 12, 13, 2, 3));
        check(close(f.getWidth(), 12) && close(f.getArcWidth(), 2), "setRoundRect(RoundRectangle2D)");

        // bounds
        RoundRectangle2D.Float b = new RoundRectangle2D.Float(0, 0, 20, 20, 10, 10);
        Rectangle2D bb = b.getBounds2D();
        check(close(bb.getX(), 0) && close(bb.getY(), 0) && close(bb.getWidth(), 20) && close(bb.getHeight(), 20), "getBounds2D");

        // contains
        check(b.contains(10, 10), "contains center");
        check(!b.contains(0.5, 0.5), "excludes corner");

        // direct field mutation
        RoundRectangle2D.Float m = new RoundRectangle2D.Float();
        m.x = 1; m.y = 2; m.width = 30; m.height = 40; m.arcwidth = 5; m.archeight = 5;
        check(close(m.getX(), 1) && close(m.getWidth(), 30) && close(m.getArcWidth(), 5), "public field mutation");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
