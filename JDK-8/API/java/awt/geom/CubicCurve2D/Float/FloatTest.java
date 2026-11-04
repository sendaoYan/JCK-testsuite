/*
 * @test
 * @summary java.awt.geom.CubicCurve2D.Float conformance: control points and endpoints.
 * @run main FloatTest
 */


import java.awt.geom.CubicCurve2D;
public class FloatTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        CubicCurve2D c = new CubicCurve2D.Float(0.0f, 0.0f, 1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f);
        check(c.getX1() == 0.0 && c.getY1() == 0.0, "P1");
        check(c.getX2() == 5.0 && c.getY2() == 6.0, "P2");
        check(c.getCtrlX1() == 1.0 && c.getCtrlY1() == 2.0, "ctrlP1");
        check(c.getCtrlX2() == 3.0 && c.getCtrlY2() == 4.0, "ctrlP2");
        check(c.getP1().getX() == 0.0 && c.getP2().getY() == 6.0, "getP1/getP2");
        check(c.getBounds2D().getWidth() == 5.0, "bounds");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
