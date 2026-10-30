/*
 * @test
 * @summary JLS 8.4.8.1: an instance method mC declared in class C overrides m1
 *          from A; at run time the overriding method is selected by dynamic
 *          dispatch.
 * @run main OverridingDispatch
 */

class Point {
    int x, y;
    void move(int dx, int dy) { x += dx; y += dy; }
    int x() { return x; }
    int y() { return y; }
}

class SlowPoint extends Point {
    int xLimit, yLimit;
    void move(int dx, int dy) {
        super.move(limit(dx, xLimit), limit(dy, yLimit));
    }
    static int limit(int d, int limit) {
        return d > limit ? limit : d < -limit ? -limit : d;
    }
}

public class OverridingDispatch {
    public static void main(String[] args) {
        Point p = new SlowPoint();   // xLimit and yLimit default to 0
        p.move(100, 200);            // dynamic dispatch selects SlowPoint.move
        if (p.x() != 0 || p.y() != 0) throw new AssertionError("move not limited");
        System.out.println("PASS");
    }
}
