/*
 * @test
 * @summary java.awt.geom.Arc2D.Float conformance: white-box structure plus black-box behavior.
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
        Class<?> c = Class.forName("java.awt.geom.Arc2D$Float");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "Arc2D.Float is public");
        check(!Modifier.isAbstract(m), "Arc2D.Float is concrete");
        check(c.getSuperclass() == Arc2D.class, "Arc2D.Float extends Arc2D");
        check(Serializable.class.isAssignableFrom(c), "Arc2D.Float implements Serializable");
        for (String f : new String[]{"x", "y", "width", "height", "start", "extent"}) {
            Field fd = c.getField(f);
            check(Modifier.isPublic(fd.getModifiers()) && fd.getType() == float.class, "public float field " + f);
        }
        check(c.getConstructors().length == 4, "four public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Arc2D.Float a = new Arc2D.Float();
        check(a.getArcType() == Arc2D.OPEN, "default ctor arc type OPEN");
        check(a.isEmpty(), "default ctor empty");
        check(a.x == 0f && a.y == 0f && a.width == 0f && a.height == 0f && a.start == 0f && a.extent == 0f, "default ctor zero fields");

        Arc2D.Float f = new Arc2D.Float(1f, 2f, 30f, 40f, 10f, 80f, Arc2D.CHORD);
        check(close(f.getX(), 1) && close(f.getY(), 2), "6-float ctor x/y");
        check(close(f.getWidth(), 30) && close(f.getHeight(), 40), "6-float ctor w/h");
        check(close(f.getAngleStart(), 10) && close(f.getAngleExtent(), 80), "6-float ctor angles");
        check(f.getArcType() == Arc2D.CHORD, "6-float ctor type");
        check(f.x == 1f && f.width == 30f && f.start == 10f && f.extent == 80f, "public fields exposed");
        check(!f.isEmpty(), "non-empty arc");

        Arc2D.Float r = new Arc2D.Float(new Rectangle2D.Double(0, 0, 20, 20), 0f, 90f, Arc2D.PIE);
        check(close(r.getWidth(), 20) && close(r.getHeight(), 20) && r.getArcType() == Arc2D.PIE, "Rectangle2D ctor");

        Arc2D.Float typed = new Arc2D.Float(Arc2D.OPEN);
        check(typed.getArcType() == Arc2D.OPEN, "int-type ctor");

        // getters
        check(close(f.getX(), 1) && close(f.getY(), 2) && close(f.getWidth(), 30) && close(f.getHeight(), 40), "getters");

        // setArc
        f.setArc(5, 6, 7, 8, 20, 60, Arc2D.PIE);
        check(close(f.getX(), 5) && close(f.getWidth(), 7) && close(f.getAngleStart(), 20) && close(f.getAngleExtent(), 60) && f.getArcType() == Arc2D.PIE, "setArc");

        // setAngleStart/Extent
        f.setAngleStart(30);
        f.setAngleExtent(120);
        check(close(f.getAngleStart(), 30) && close(f.getAngleExtent(), 120), "setAngleStart/Extent");

        // setFrame
        f.setFrame(0, 0, 100, 50);
        check(close(f.getX(), 0) && close(f.getWidth(), 100) && close(f.getHeight(), 50), "setFrame");

        // inherited setArcByCenter
        Arc2D.Float c = new Arc2D.Float();
        c.setArcByCenter(0, 0, 10, 0, 90, Arc2D.OPEN);
        check(close(c.getX(), -10) && close(c.getWidth(), 20), "inherited setArcByCenter frame");
        check(close(c.getStartPoint().getX(), 10) && close(c.getEndPoint().getX(), 0), "inherited start/end points");

        // direct field mutation
        Arc2D.Float m = new Arc2D.Float();
        m.x = 1; m.y = 2; m.width = 30; m.height = 40; m.start = 45; m.extent = 90;
        check(close(m.getX(), 1) && close(m.getWidth(), 30) && close(m.getAngleStart(), 45) && close(m.getAngleExtent(), 90), "public field mutation");

        // containsAngle via inherited
        check(m.containsAngle(60), "containsAngle inside");
        check(!m.containsAngle(0), "containsAngle outside");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
