/*
 * @test
 * @summary java.awt.Point conformance: coordinates and translate.
 * @run main PointTest
 */


import java.awt.Point;
public class PointTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        Point p = new Point(3, 4);
        check(p.x == 3 && p.y == 4, "x/y");
        check(p.getX() == 3.0 && p.getY() == 4.0, "getX/getY");
        p.translate(1, 2);
        check(p.x == 4 && p.y == 6, "translate");
        p.move(10, 20);
        check(p.x == 10 && p.y == 20, "move");
        check(p.distance(10, 24) == 4.0, "distance");
        check(p.equals(new Point(10, 20)), "equals");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
