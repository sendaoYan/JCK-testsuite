/*
 * @test
 * @summary JLS 8.3.1.1: a static field has exactly one incarnation no matter how
 *          many instances of the class are created.
 * @run main StaticFieldSingleIncarnation
 */

class Point {
    int x, y;
    static int totalUseCount = 0;
    Point(int x, int y) { this.x = x; this.y = y; }
}

public class StaticFieldSingleIncarnation {
    public static void main(String[] args) {
        Point p = new Point(1, 1);
        Point q = new Point(2, 2);
        p.totalUseCount = 7;
        if (q.totalUseCount != 7 || Point.totalUseCount != 7)
            throw new AssertionError("static field must have one incarnation");
        System.out.println("PASS");
    }
}
