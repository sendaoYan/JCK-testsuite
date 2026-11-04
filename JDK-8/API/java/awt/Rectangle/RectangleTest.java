/*
 * @test
 * @summary java.awt.Rectangle conformance: bounds, contains, intersection.
 * @run main RectangleTest
 */


import java.awt.Rectangle;
public class RectangleTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        Rectangle r = new Rectangle(10, 20, 30, 40);
        check(r.x == 10 && r.y == 20 && r.width == 30 && r.height == 40, "fields");
        check(r.contains(15, 25), "contains interior");
        check(!r.contains(5, 5), "does not contain outside");
        check(r.contains(new Rectangle(11, 21, 5, 5)), "contains rect");
        Rectangle inter = r.intersection(new Rectangle(30, 30, 100, 100));
        check(inter.x == 30 && inter.y == 30 && inter.width == 10 && inter.height == 30, "intersection");
        Rectangle u = r.union(new Rectangle(100, 100, 5, 5));
        check(u.x == 10 && u.y == 20 && u.width == 95 && u.height == 85, "union");
        check(!r.isEmpty(), "not empty");
        check(new Rectangle().isEmpty(), "default empty");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
