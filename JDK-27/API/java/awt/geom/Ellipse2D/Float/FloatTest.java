/*
 * @test
 * @summary java.awt.geom.Ellipse2D.Float conformance: white-box structure plus black-box behavior.
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
        Class<?> c = Class.forName("java.awt.geom.Ellipse2D$Float");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "Ellipse2D.Float is public");
        check(!Modifier.isAbstract(m), "Ellipse2D.Float is concrete");
        check(c.getSuperclass() == Ellipse2D.class, "Ellipse2D.Float extends Ellipse2D");
        check(Serializable.class.isAssignableFrom(c), "Ellipse2D.Float implements Serializable");
        for (String f : new String[]{"x", "y", "width", "height"}) {
            Field fd = c.getField(f);
            check(Modifier.isPublic(fd.getModifiers()) && fd.getType() == float.class, "public float field " + f);
        }
        check(c.getConstructors().length == 2, "two public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Ellipse2D.Float e = new Ellipse2D.Float();
        check(e.x == 0f && e.y == 0f && e.width == 0f && e.height == 0f, "default ctor zeros");
        check(e.isEmpty(), "default ctor empty");

        Ellipse2D.Float f = new Ellipse2D.Float(1f, 2f, 30f, 40f);
        check(close(f.getX(), 1) && close(f.getY(), 2), "4-float ctor x/y");
        check(close(f.getWidth(), 30) && close(f.getHeight(), 40), "4-float ctor w/h");
        check(f.x == 1f && f.y == 2f && f.width == 30f && f.height == 40f, "public fields exposed");
        check(!f.isEmpty(), "non-empty");

        check(close(f.getX(), 1) && close(f.getY(), 2) && close(f.getWidth(), 30) && close(f.getHeight(), 40), "getters");

        f.setFrame(0f, 0f, 20f, 20f);
        check(close(f.getX(), 0) && close(f.getWidth(), 20), "setFrame(float...)");
        f.setFrame(2.5, 3.5, 4.5, 5.5);
        check(close(f.getX(), 2.5) && close(f.getWidth(), 4.5) && close(f.getHeight(), 5.5), "setFrame(double...)");

        Ellipse2D.Float b = new Ellipse2D.Float(0, 0, 20, 20);
        Rectangle2D bb = b.getBounds2D();
        check(close(bb.getX(), 0) && close(bb.getY(), 0) && close(bb.getWidth(), 20) && close(bb.getHeight(), 20), "getBounds2D");

        check(b.contains(10, 10), "contains center");
        check(!b.contains(0, 0), "excludes corner");

        Ellipse2D.Float m = new Ellipse2D.Float();
        m.x = 1; m.y = 2; m.width = 30; m.height = 40;
        check(close(m.getX(), 1) && close(m.getWidth(), 30), "public field mutation");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
