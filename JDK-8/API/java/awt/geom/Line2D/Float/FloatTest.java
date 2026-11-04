/*
 * @test
 * @summary java.awt.geom.Line2D.Float conformance: line endpoints and distance.
 * @run main FloatTest
 */


import java.awt.geom.Line2D;
public class FloatTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        Line2D l = new Line2D.Float(0.0f, 0.0f, 3.0f, 4.0f);
        check(l.getX1() == 0.0 && l.getY1() == 0.0, "P1");
        check(l.getX2() == 3.0 && l.getY2() == 4.0, "P2");
        check(l.getP1().distance(0.0, 0.0) == 0.0, "getP1");
        check(l.ptSegDist(0.0, 0.0) == 0.0, "ptSegDist endpoint");
        check(l.getBounds2D().getWidth() == 3.0, "bounds width");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
