/*
 * @test
 * @summary java.awt.geom.QuadCurve2D.Double conformance: control point and endpoints.
 * @run main DoubleTest
 */


import java.awt.geom.QuadCurve2D;
public class DoubleTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        QuadCurve2D q = new QuadCurve2D.Double(0.0, 0.0, 5.0, 10.0, 10.0, 0.0);
        check(q.getX1() == 0.0 && q.getY1() == 0.0, "P1");
        check(q.getX2() == 10.0 && q.getY2() == 0.0, "P2");
        check(q.getCtrlX() == 5.0 && q.getCtrlY() == 10.0, "ctrl");
        check(q.getCtrlPt().getY() == 10.0, "getCtrlPt");
        check(q.getBounds2D().getWidth() == 10.0, "bounds width");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
