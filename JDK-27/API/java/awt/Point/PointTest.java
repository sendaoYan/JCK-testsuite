/*
 * @test
 * @summary java.awt.Point conformance: white-box structure + black-box behavior.
 * @run main PointTest
 */

import java.lang.reflect.*;

public class PointTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.Point", false, PointTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.Point");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(),
              "public/protected/member");
        check(c.getEnumConstants() == null, "non-enum returns null constants");
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        check(c.getSuperclass().getName().equals("java.awt.geom.Point2D"),
              "extends java.awt.geom.Point2D");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(!Modifier.isAbstract(m), "concrete class");
        check(c.getField("x").getType() == int.class, "public int x");
        check(c.getField("y").getType() == int.class, "public int y");
        check(c.getConstructor() != null, "ctor ()");
        check(c.getConstructor(int.class, int.class) != null, "ctor (int,int)");
        check(c.getConstructor(java.awt.Point.class) != null, "ctor (Point)");
        // setLocation is overloaded (int,int)/(double,double)/(Point) -> name scan
        for (String n : new String[] {"getX", "getY", "getLocation", "setLocation",
                "move", "translate", "equals", "toString"}) {
            boolean found = java.util.Arrays.stream(c.getMethods())
                    .anyMatch(mm -> mm.getName().equals(n));
            check(found, "method present: " + n);
        }
        // distance family inherited from Point2D
        check(c.getMethod("distance", double.class, double.class) != null,
              "distance(double,double) inherited from Point2D");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // constructors
        java.awt.Point p0 = new java.awt.Point();
        check(p0.x == 0 && p0.y == 0, "default (0,0)");
        java.awt.Point p = new java.awt.Point(3, 4);
        check(p.x == 3 && p.y == 4, "(3,4) ctor");
        java.awt.Point copy = new java.awt.Point(p);
        check(copy.x == 3 && copy.y == 4, "copy ctor");
        copy.x = 100;
        check(p.x == 3, "copy ctor independent");

        // Point2D double view
        check(p.getX() == 3.0 && p.getY() == 4.0, "getX/getY doubles");

        // getLocation returns a new independent point
        java.awt.Point loc = p.getLocation();
        check(loc.x == 3 && loc.y == 4, "getLocation values");
        loc.x = -1;
        check(p.x == 3, "getLocation returns independent object");

        // setLocation overloads
        p.setLocation(10, 20);
        check(p.x == 10 && p.y == 20, "setLocation(int,int)");
        p.setLocation(new java.awt.Point(5, 6));
        check(p.x == 5 && p.y == 6, "setLocation(Point)");
        p.setLocation(3.6, 4.4);
        check(p.x == 4 && p.y == 4, "setLocation(double,double) rounds half up: " + p);
        p.setLocation(-3.6, -4.4);
        check(p.x == -4 && p.y == -4, "setLocation(double,double) negative rounding: " + p);

        // move / translate
        p.move(1, 2);
        check(p.x == 1 && p.y == 2, "move");
        p.translate(10, 20);
        check(p.x == 11 && p.y == 22, "translate adds");
        p.translate(-11, -22);
        check(p.x == 0 && p.y == 0, "translate back to origin");

        // distance family (inherited Point2D math)
        java.awt.Point q = new java.awt.Point(3, 4);
        check(q.distance(0, 0) == 5.0, "3-4-5 distance exact");
        check(q.distanceSq(0, 0) == 25.0, "distanceSq exact");
        java.awt.Point r = new java.awt.Point(6, 8);
        check(q.distance(r) == 5.0, "distance(Point) 3-4-5");
        check(new java.awt.Point(0, 0).distance(0, 0) == 0.0, "zero distance");

        // equals / hashCode
        java.awt.Point a = new java.awt.Point(7, 8);
        java.awt.Point b = new java.awt.Point(7, 8);
        java.awt.Point c2 = new java.awt.Point(8, 7);
        check(a.equals(b), "equal points");
        check(a.hashCode() == b.hashCode(), "equal points equal hashCode");
        check(!a.equals(c2), "swapped coords not equal");
        check(!a.equals(null), "not equal null");
        check(!a.equals("(7,8)"), "not equal unrelated type");
        check(a.equals(a), "reflexive");

        // toString
        check(q.toString().equals("java.awt.Point[x=3,y=4]"), "toString format: " + q);

        // combination chain: move -> translate -> compare -> measure
        java.awt.Point walker = new java.awt.Point(0, 0);
        walker.move(10, 0);
        walker.translate(0, 10);
        walker.translate(-10, 0);
        check(walker.equals(new java.awt.Point(0, 10)), "walked square ends at (0,10)");
        check(walker.distance(0, 0) == 10.0, "distance from origin after walk");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
