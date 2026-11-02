/*
 * @test
 * @summary java.awt.geom.Point2D.Float conformance: coordinates and distance.
 * @run main FloatTest
 */


import java.awt.geom.Point2D;
public class FloatTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        Point2D p = new Point2D.Float(3.0f, 4.0f);
        check(p.getX() == 3.0 && p.getY() == 4.0, "getX/getY");
        check(p.distance(0.0, 0.0) == 5.0, "distance 3-4-5");
        p.setLocation(10.0, 20.0);
        check(p.getX() == 10.0 && p.getY() == 20.0, "setLocation");
        check(p.equals(new Point2D.Float(10.0f, 20.0f)), "equals");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
