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
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 3: contract deepening ----
    static java.awt.Point serPoint(java.awt.Point p) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(bos).writeObject(p);
        return (java.awt.Point) new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
    }
    static void testContract() throws Exception {
        // setLocation(double,double) rounds to nearest (Math.floor(v+0.5)), unlike truncate
        java.awt.Point p = new java.awt.Point();
        p.setLocation(3.6, 4.4);  check(p.x == 4 && p.y == 4, "setLocation(double) 3.6->4, 4.4->4");
        p.setLocation(-3.6, -4.4); check(p.x == -4 && p.y == -4, "setLocation(double) Math.round -3.6->-4, -4.4->-4: " + p);
        p.setLocation(0.5, 1.5);   check(p.x == 1 && p.y == 2, "setLocation(double) half-up: " + p);
        // setLocation overloads
        p.setLocation(7, 8);       check(p.x == 7 && p.y == 8, "setLocation(int,int)");
        p.setLocation(new java.awt.Point(-1, -2)); check(p.x == -1 && p.y == -2, "setLocation(Point)");
        java.awt.Point src = new java.awt.Point(50, 60);
        p.setLocation(src); src.x = 0;
        check(p.x == 50, "setLocation(Point) copies values");
        // move vs translate
        p.move(10, 10); p.translate(-3, 4);
        check(p.x == 7 && p.y == 14, "move+translate arithmetic");
        // getLocation returns fresh independent object
        java.awt.Point loc = p.getLocation();
        check(loc.equals(p), "getLocation equal");
        loc.x = 999;
        check(p.x == 7, "getLocation independent copy");
        // equals across super/subclass implementations (Point vs Point2D.Double)
        java.awt.geom.Point2D.Double pd = new java.awt.geom.Point2D.Double(7.0, 14.0);
        check(p.equals(pd), "Point equals Point2D.Double with same coords");
        check(pd.equals(p), "Point2D.Double equals Point (symmetric)");
        check(!p.equals(new java.awt.geom.Point2D.Double(7.0, 13.0)), "different y not equal");
        // Point has no hashCode override: identity hash - equal points may hash differently.
        // Contract: hashCode is NOT overridden (lock absence).
        boolean hasHash = false;
        try { java.awt.Point.class.getDeclaredMethod("hashCode"); hasHash = true; } catch (NoSuchMethodException e) { }
        check(!hasHash, "Point does not override hashCode (identity hash)");
        // distance family: exact pythagorean
        java.awt.Point q = new java.awt.Point(3, 4);
        check(q.distance(0, 0) == 5.0, "distance 3-4-5");
        check(q.distanceSq(0, 0) == 25.0, "distanceSq exact");
        check(q.distance(new java.awt.Point(0, 0)) == 5.0, "distance(Point)");
        check(q.distance(q) == 0.0, "self distance zero");
        check(q.distance(3.0, 4.0) == 0.0, "distance(double,double) self");
        // Integer.MIN_VALUE overflow behavior is defined by int fields; just move to extremes
        java.awt.Point ext = new java.awt.Point(Integer.MIN_VALUE, Integer.MAX_VALUE);
        check(ext.getX() == (double) Integer.MIN_VALUE, "MIN_VALUE x preserved");
        check(ext.getY() == (double) Integer.MAX_VALUE, "MAX_VALUE y preserved");
        // serialization round-trip incl. negative and extreme values
        check(serPoint(q).equals(q), "serialization round-trip");
        check(serPoint(new java.awt.Point(-5, -6)).x == -5, "serialization negative");
        java.awt.Point s2 = serPoint(new java.awt.Point(Integer.MIN_VALUE, Integer.MAX_VALUE));
        check(s2.x == Integer.MIN_VALUE && s2.y == Integer.MAX_VALUE, "serialization extremes");
        // serialVersionUID stability
        check(java.io.ObjectStreamClass.lookup(java.awt.Point.class).getSerialVersionUID()
              == -5276940640259749850L, "serialVersionUID");
        // toString exact format
        check(q.toString().equals("java.awt.Point[x=3,y=4]"), "toString exact: " + q);
        // toString of deserialized still canonical
        check(serPoint(q).toString().equals("java.awt.Point[x=3,y=4]"), "toString after round-trip");
        // no-arg vs zero-arg equality
        check(new java.awt.Point().equals(new java.awt.Point(0, 0)), "default ctor equals (0,0)");
        // null equals
        check(!q.equals(null), "equals(null) false");
        check(!q.equals("java.awt.Point[x=3,y=4]"), "equals unrelated type false");
    }

}
