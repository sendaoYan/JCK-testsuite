/*
 * @test
 * @summary JLS 8.4.5: an overriding method may return a subtype of the return
 *          type of the overridden method (covariant return type), and dispatch
 *          still selects the overriding method.
 * @run main CovariantReturnType
 */

class Point {
    Point position() { return this; }
}

class RealPoint extends Point {
    RealPoint position() { return this; }   // covariant return type
}

public class CovariantReturnType {
    public static void main(String[] args) {
        RealPoint rp = new RealPoint();
        if (rp.position() != rp) throw new AssertionError("direct call");
        // Through a Point reference the overriding method is still invoked.
        Point p = rp;
        if (p.position() != rp) throw new AssertionError("dispatched call");
        System.out.println("PASS");
    }
}
