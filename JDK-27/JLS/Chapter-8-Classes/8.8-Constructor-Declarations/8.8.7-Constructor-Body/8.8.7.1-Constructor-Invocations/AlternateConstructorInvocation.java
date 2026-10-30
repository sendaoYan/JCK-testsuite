/*
 * @test
 * @summary JLS 8.8.7.1: an alternate constructor invocation begins with this and
 *          invokes another constructor of the same class.
 * @run main AlternateConstructorInvocation
 */

class Point {
    int x, y;
    Point(int x, int y) { this.x = x; this.y = y; }
    Point() { this(0, 0); }   // alternate constructor invocation
}

public class AlternateConstructorInvocation {
    public static void main(String[] args) {
        Point p = new Point();
        if (p.x != 0 || p.y != 0) throw new AssertionError("defaults");
        System.out.println("PASS");
    }
}
