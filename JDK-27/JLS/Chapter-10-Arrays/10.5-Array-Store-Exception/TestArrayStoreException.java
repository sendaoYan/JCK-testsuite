/*
 * @test
 * @summary JLS 10.5 (Example 10.5-1): assignment to an array component is checked
 *          at run time; an incompatible value throws ArrayStoreException.
 * @run main TestArrayStoreException
 */

public class TestArrayStoreException {
    static class Point { int x, y; }
    static class ColoredPoint extends Point { int color; }

    public static void main(String[] args) {
        ColoredPoint[] cpa = new ColoredPoint[10];
        Point[] pa = cpa;
        if (pa[1] != null) throw new AssertionError("uninitialized element");
        try {
            pa[0] = new Point();
            throw new AssertionError("expected ArrayStoreException");
        } catch (ArrayStoreException e) {
            // expected: a Point is not a ColoredPoint
        }
        System.out.println("PASS");
    }
}
