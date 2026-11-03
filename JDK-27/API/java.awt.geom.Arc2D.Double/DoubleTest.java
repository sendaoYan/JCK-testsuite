/*
 * @test
 * @summary java.awt.geom.Arc2D.Double conformance: white-box structure plus black-box behavior.
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
        Class<?> c = Class.forName("java.awt.geom.Arc2D$Double");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "Arc2D.Double is public");
        check(!Modifier.isAbstract(m), "Arc2D.Double is concrete");
        check(c.getSuperclass() == Arc2D.class, "Arc2D.Double extends Arc2D");
        check(Serializable.class.isAssignableFrom(c), "Arc2D.Double implements Serializable");
        for (String f : new String[]{"x", "y", "width", "height", "start", "extent"}) {
            Field fd = c.getField(f);
            check(Modifier.isPublic(fd.getModifiers()) && fd.getType() == double.class, "public double field " + f);
        }
        check(c.getConstructors().length == 4, "four public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Arc2D.Double a = new Arc2D.Double();
        check(a.getArcType() == Arc2D.OPEN, "default ctor arc type OPEN");
        check(a.isEmpty(), "default ctor empty");
        check(a.x == 0 && a.y == 0 && a.width == 0 && a.height == 0 && a.start == 0 && a.extent == 0, "default ctor zero fields");

        Arc2D.Double d = new Arc2D.Double(1, 2, 30, 40, 10, 80, Arc2D.CHORD);
        check(close(d.getX(), 1) && close(d.getY(), 2), "6-double ctor x/y");
        check(close(d.getWidth(), 30) && close(d.getHeight(), 40), "6-double ctor w/h");
        check(close(d.getAngleStart(), 10) && close(d.getAngleExtent(), 80), "6-double ctor angles");
        check(d.getArcType() == Arc2D.CHORD, "6-double ctor type");
        check(d.x == 1 && d.width == 30 && d.start == 10 && d.extent == 80, "public fields exposed");
        check(!d.isEmpty(), "non-empty arc");

        Arc2D.Double r = new Arc2D.Double(new Rectangle2D.Double(0, 0, 20, 20), 0, 90, Arc2D.PIE);
        check(close(r.getWidth(), 20) && close(r.getHeight(), 20) && r.getArcType() == Arc2D.PIE, "Rectangle2D ctor");

        Arc2D.Double typed = new Arc2D.Double(Arc2D.OPEN);
        check(typed.getArcType() == Arc2D.OPEN, "int-type ctor");

        check(close(d.getX(), 1) && close(d.getY(), 2) && close(d.getWidth(), 30) && close(d.getHeight(), 40), "getters");

        d.setArc(5, 6, 7, 8, 20, 60, Arc2D.PIE);
        check(close(d.getX(), 5) && close(d.getWidth(), 7) && close(d.getAngleStart(), 20) && close(d.getAngleExtent(), 60) && d.getArcType() == Arc2D.PIE, "setArc");

        d.setAngleStart(30);
        d.setAngleExtent(120);
        check(close(d.getAngleStart(), 30) && close(d.getAngleExtent(), 120), "setAngleStart/Extent");

        d.setFrame(0, 0, 100, 50);
        check(close(d.getX(), 0) && close(d.getWidth(), 100) && close(d.getHeight(), 50), "setFrame");

        Arc2D.Double c = new Arc2D.Double();
        c.setArcByCenter(0, 0, 10, 0, 90, Arc2D.OPEN);
        check(close(c.getX(), -10) && close(c.getWidth(), 20), "inherited setArcByCenter frame");
        check(close(c.getStartPoint().getX(), 10) && close(c.getEndPoint().getX(), 0), "inherited start/end points");

        Arc2D.Double m = new Arc2D.Double();
        m.x = 1; m.y = 2; m.width = 30; m.height = 40; m.start = 45; m.extent = 90;
        check(close(m.getX(), 1) && close(m.getWidth(), 30) && close(m.getAngleStart(), 45) && close(m.getAngleExtent(), 90), "public field mutation");

        check(m.containsAngle(60), "containsAngle inside");
        check(!m.containsAngle(0), "containsAngle outside");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
