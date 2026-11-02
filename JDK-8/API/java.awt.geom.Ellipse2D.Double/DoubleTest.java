/*
 * @test
 * @summary java.awt.geom.Ellipse2D.Double conformance: ellipse bounds and containment.
 * @run main DoubleTest
 */


import java.awt.geom.Ellipse2D;
public class DoubleTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        Ellipse2D e = new Ellipse2D.Double(0.0, 0.0, 10.0, 10.0);
        check(e.getX() == 0.0 && e.getY() == 0.0, "x/y");
        check(e.getWidth() == 10.0 && e.getHeight() == 10.0, "width/height");
        check(e.contains(5.0, 5.0), "contains center");
        check(!e.contains(10.0, 10.0), "does not contain corner");
        check(e.getBounds2D().getWidth() == 10.0, "bounds");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
