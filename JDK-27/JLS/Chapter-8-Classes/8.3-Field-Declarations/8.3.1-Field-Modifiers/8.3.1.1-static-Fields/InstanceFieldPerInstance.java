/*
 * @test
 * @summary JLS 8.3.1.1: an instance variable is freshly created for every
 *          instance of the class.
 * @run main InstanceFieldPerInstance
 */

class Point {
    int x, y;
    Point(int x, int y) { this.x = x; this.y = y; }
}

public class InstanceFieldPerInstance {
    public static void main(String[] args) {
        Point p = new Point(1, 1);
        Point q = new Point(2, 2);
        p.x = 3;
        p.y = 3;
        if (q.x != 2 || q.y != 2)
            throw new AssertionError("instance variables must be per-instance");
        System.out.println("PASS");
    }
}
