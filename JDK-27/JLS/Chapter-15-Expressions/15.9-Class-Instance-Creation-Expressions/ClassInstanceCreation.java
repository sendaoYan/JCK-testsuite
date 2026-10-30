/*
 * @test
 * @summary JLS 15.9: a class instance creation expression allocates an object and
 *          invokes the chosen constructor; the result denotes a reference to the
 *          new object.
 * @run main ClassInstanceCreation
 */

public class ClassInstanceCreation {
    static class Point {
        int x, y;
        Point() { this(0, 0); }
        Point(int x, int y) { this.x = x; this.y = y; }
    }

    public static void main(String[] args) {
        Point p = new Point(3, 4);
        if (p.x != 3 || p.y != 4) throw new AssertionError("two-arg constructor");

        Point q = new Point();
        if (q.x != 0 || q.y != 0) throw new AssertionError("no-arg constructor");

        Object o = new Point(1, 2);
        if (!(o instanceof Point)) throw new AssertionError("creation expression result");

        System.out.println("PASS");
    }
}
