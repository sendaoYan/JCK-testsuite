/*
 * @test
 * @summary java.awt.geom.QuadCurve2D.Float conformance: control point and endpoints.
 * @run main FloatTest
 */


import java.awt.geom.QuadCurve2D;
public class FloatTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        QuadCurve2D q = new QuadCurve2D.Float(0.0f, 0.0f, 5.0f, 10.0f, 10.0f, 0.0f);
        check(q.getX1() == 0.0 && q.getY1() == 0.0, "P1");
        check(q.getX2() == 10.0 && q.getY2() == 0.0, "P2");
        check(q.getCtrlX() == 5.0 && q.getCtrlY() == 10.0, "ctrl");
        check(q.getCtrlPt().getY() == 10.0, "getCtrlPt");
        check(q.getBounds2D().getWidth() == 10.0, "bounds width");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
