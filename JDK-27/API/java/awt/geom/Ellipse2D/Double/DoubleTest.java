/*
 * @test
 * @summary java.awt.geom.Ellipse2D.Double conformance: white-box structure plus black-box behavior.
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
        Class<?> c = Class.forName("java.awt.geom.Ellipse2D$Double");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "Ellipse2D.Double is public");
        check(!Modifier.isAbstract(m), "Ellipse2D.Double is concrete");
        check(c.getSuperclass() == Ellipse2D.class, "Ellipse2D.Double extends Ellipse2D");
        check(Serializable.class.isAssignableFrom(c), "Ellipse2D.Double implements Serializable");
        for (String f : new String[]{"x", "y", "width", "height"}) {
            Field fd = c.getField(f);
            check(Modifier.isPublic(fd.getModifiers()) && fd.getType() == double.class, "public double field " + f);
        }
        check(c.getConstructors().length == 2, "two public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Ellipse2D.Double e = new Ellipse2D.Double();
        check(e.x == 0 && e.y == 0 && e.width == 0 && e.height == 0, "default ctor zeros");
        check(e.isEmpty(), "default ctor empty");

        Ellipse2D.Double d = new Ellipse2D.Double(1, 2, 30, 40);
        check(close(d.getX(), 1) && close(d.getY(), 2), "4-double ctor x/y");
        check(close(d.getWidth(), 30) && close(d.getHeight(), 40), "4-double ctor w/h");
        check(d.x == 1 && d.y == 2 && d.width == 30 && d.height == 40, "public fields exposed");
        check(!d.isEmpty(), "non-empty");

        check(close(d.getX(), 1) && close(d.getY(), 2) && close(d.getWidth(), 30) && close(d.getHeight(), 40), "getters");

        d.setFrame(0, 0, 20, 20);
        check(close(d.getX(), 0) && close(d.getWidth(), 20), "setFrame");

        Ellipse2D.Double b = new Ellipse2D.Double(0, 0, 20, 20);
        Rectangle2D bb = b.getBounds2D();
        check(close(bb.getX(), 0) && close(bb.getY(), 0) && close(bb.getWidth(), 20) && close(bb.getHeight(), 20), "getBounds2D");

        check(b.contains(10, 10), "contains center");
        check(!b.contains(0, 0), "excludes corner");

        Ellipse2D.Double m = new Ellipse2D.Double();
        m.x = 1; m.y = 2; m.width = 30; m.height = 40;
        check(close(m.getX(), 1) && close(m.getWidth(), 30), "public field mutation");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
