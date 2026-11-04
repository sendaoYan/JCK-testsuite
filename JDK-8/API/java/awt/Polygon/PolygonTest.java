/*
 * @test
 * @summary java.awt.Polygon conformance: points and contains.
 * @run main PolygonTest
 */


import java.awt.Polygon;
public class PolygonTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        Polygon p = new Polygon();
        check(p.npoints == 0, "empty initially");
        p.addPoint(0, 0);
        p.addPoint(10, 0);
        p.addPoint(0, 10);
        check(p.npoints == 3, "three points");
        check(p.xpoints[1] == 10 && p.ypoints[2] == 10, "points stored");
        check(p.contains(2, 2), "contains interior");
        check(!p.contains(9, 9), "does not contain exterior");
        java.awt.Rectangle b = p.getBounds();
        check(b.x == 0 && b.y == 0 && b.width == 10 && b.height == 10, "bounds");
        check(p.getBoundingBox().equals(b), "getBoundingBox");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
