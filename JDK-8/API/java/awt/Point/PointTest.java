/*
 * @test
 * @summary java.awt.Point conformance: coordinates and translate.
 * @run main PointTest
 */


import java.awt.Point;
public class PointTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Point p = new Point(3, 4);
        check(p.x == 3 && p.y == 4, "x/y");
        check(p.getX() == 3.0 && p.getY() == 4.0, "getX/getY");
        p.translate(1, 2);
        check(p.x == 4 && p.y == 6, "translate");
        p.move(10, 20);
        check(p.x == 10 && p.y == 20, "move");
        check(p.distance(10, 24) == 4.0, "distance");
        check(p.equals(new Point(10, 20)), "equals");
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
