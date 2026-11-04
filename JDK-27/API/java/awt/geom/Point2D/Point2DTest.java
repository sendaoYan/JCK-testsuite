/*
 * @test
 * @summary java.awt.geom.Point2D conformance: white-box structure plus black-box behavior.
 * @run main Point2DTest
 */

import java.awt.geom.Point2D;
import java.lang.reflect.*;

public class Point2DTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean close(double a, double b) { return Math.abs(a - b) < 1e-9; }
    static boolean hasMethod(Class<?> c, String n, Class<?>... p) {
        try { c.getMethod(n, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.geom.Point2D");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) && Modifier.isAbstract(m), "abstract public class");
        check(!c.isInterface() && !c.isEnum(), "class, not interface/enum");
        check(Cloneable.class.isAssignableFrom(c), "implements Cloneable");
        check(hasMethod(c, "getX"), "getX()");
        check(hasMethod(c, "getY"), "getY()");
        check(hasMethod(c, "setLocation", double.class, double.class), "setLocation(double,double)");
        check(hasMethod(c, "setLocation", Point2D.class), "setLocation(Point2D)");
        check(hasMethod(c, "distanceSq", double.class, double.class, double.class, double.class), "static distanceSq");
        check(hasMethod(c, "distance", double.class, double.class, double.class, double.class), "static distance");
        check(hasMethod(c, "distanceSq", Point2D.class), "distanceSq(Point2D)");
        check(hasMethod(c, "distance", Point2D.class), "distance(Point2D)");
        check(hasMethod(c, "clone"), "clone()");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Point2D.Double p = new Point2D.Double(3, 4);
        check(close(p.getX(), 3) && close(p.getY(), 4), "ctor + getX/getY");

        // static distance
        checkClose(Point2D.distance(0, 0, 3, 4), 5.0, "static distance(0,0,3,4)==5");
        checkClose(Point2D.distanceSq(0, 0, 3, 4), 25.0, "static distanceSq(0,0,3,4)==25");

        // instance distance
        checkClose(p.distance(0, 0), 5.0, "instance distance(0,0)==5");
        checkClose(p.distance(new Point2D.Double(0, 0)), 5.0, "instance distance(Point2D)==5");
        checkClose(p.distanceSq(0, 0), 25.0, "instance distanceSq(double,double)");
        checkClose(p.distanceSq(new Point2D.Double(0, 0)), 25.0, "instance distanceSq(Point2D)");
        checkClose(p.distance(new Point2D.Double(6, 8)), 5.0, "distance to (6,8)==5");

        // setLocation
        p.setLocation(10, 20);
        check(close(p.getX(), 10) && close(p.getY(), 20), "setLocation(double,double)");
        p.setLocation(new Point2D.Double(7, 8));
        check(close(p.getX(), 7) && close(p.getY(), 8), "setLocation(Point2D)");

        // clone
        Point2D.Double q = new Point2D.Double(7, 8);
        Point2D cl = (Point2D) q.clone();
        check(cl != q && close(cl.getX(), 7) && close(cl.getY(), 8), "clone independent but equal");

        // equals / hashCode
        check(new Point2D.Double(7, 8).equals(new Point2D.Double(7, 8)), "equal points equal");
        check(!new Point2D.Double(7, 8).equals(new Point2D.Double(8, 7)), "different points not equal");
        check(!new Point2D.Double(7, 8).equals(null), "equals(null) false");
        check(!new Point2D.Double(7, 8).equals("7,8"), "equals(non-Point2D) false");
        check(new Point2D.Double(7, 8).hashCode() == new Point2D.Double(7, 8).hashCode(),
              "equal points same hashCode");
        // cross float/double equality: Point2D.Double.equals(Point2D.Float) by value
        check(new Point2D.Double(3, 4).equals(new Point2D.Float(3, 4)), "Double equals Float by value");

        // cross-implementation equality with java.awt.Point (same value, different concrete class)
        java.awt.Point awtP = new java.awt.Point(1, 2);
        Point2D.Double pd = new Point2D.Double(1, 2);
        check(awtP.equals(pd), "java.awt.Point.equals(Point2D.Double) is value-based");
        check(pd.equals(awtP), "Point2D.Double.equals(java.awt.Point) is value-based");
        check(awtP.hashCode() == pd.hashCode(), "cross-impl equal points share hashCode");
        check(awtP.equals(new Point2D.Float(1f, 2f)), "java.awt.Point.equals(Point2D.Float) is value-based");

        // degenerate distance: to self is exactly 0
        checkClose(new Point2D.Double(3, 4).distance(3, 4), 0.0, "distance to self == 0");
        checkClose(Point2D.distance(3, 4, 3, 4), 0.0, "static distance to same point == 0");

        // serialization round-trip preserves class and coordinates
        Point2D.Double sd = roundTrip(new Point2D.Double(3.5, 4.5));
        check(sd instanceof Point2D.Double, "Point2D.Double round-trip class");
        checkClose(sd.getX(), 3.5, "Point2D.Double round-trip x");
        checkClose(sd.getY(), 4.5, "Point2D.Double round-trip y");
        Point2D.Float sf = roundTrip(new Point2D.Float(3.5f, 4.5f));
        check(sf instanceof Point2D.Float, "Point2D.Float round-trip class");
        checkClose(sf.getX(), 3.5, "Point2D.Float round-trip x");
        checkClose(sf.getY(), 4.5, "Point2D.Float round-trip y");
        check(roundTrip(new Point2D.Double(1, 2)).equals(new Point2D.Double(1, 2)),
              "Point2D round-trip equals original");
    }

    @SuppressWarnings("unchecked")
    static <T> T roundTrip(T obj) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(bos).writeObject(obj);
        return (T) new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
    }

    static void checkClose(double a, double b, String m) {
        check(close(a, b), m + " (got " + a + ", want ~" + b + ")");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
