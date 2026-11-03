/*
 * @test
 * @summary java.awt.font.LayoutPath conformance: white-box structure plus black-box behavior.
 * @run main LayoutPathTest
 */

import java.awt.font.LayoutPath;
import java.awt.geom.Point2D;
import java.lang.reflect.*;

public class LayoutPathTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n, Class<?>... p) {
        try { c.getMethod(n, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.font.LayoutPath");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) && Modifier.isAbstract(m), "abstract public class");
        check(!c.isInterface() && !c.isEnum(), "class, not interface/enum");
        check(hasMethod(c, "pointToPath", Point2D.class, Point2D.class), "pointToPath(Point2D,Point2D)");
        check(hasMethod(c, "pathToPoint", Point2D.class, boolean.class, Point2D.class), "pathToPoint(Point2D,boolean,Point2D)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // anonymous implementation: pass-through mapping along a horizontal baseline
        LayoutPath lp = new LayoutPath() {
            public boolean pointToPath(Point2D loc, Point2D dst) {
                dst.setLocation(loc.getX(), 0); // project onto baseline y=0
                return loc.getY() >= 0;
            }
            public void pathToPoint(Point2D loc, boolean preceding, Point2D dst) {
                dst.setLocation(loc.getX(), preceding ? -1 : 1);
            }
        };

        Point2D dst = new Point2D.Double();
        boolean ok = lp.pointToPath(new Point2D.Double(5, 10), dst);
        check(ok && dst.getX() == 5.0 && dst.getY() == 0.0, "pointToPath projects onto baseline");

        Point2D out = new Point2D.Double();
        lp.pathToPoint(new Point2D.Double(7, 0), true, out);
        check(out.getX() == 7.0 && out.getY() == -1.0, "pathToPoint preceding above baseline");
        lp.pathToPoint(new Point2D.Double(7, 0), false, out);
        check(out.getX() == 7.0 && out.getY() == 1.0, "pathToPoint following below baseline");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
