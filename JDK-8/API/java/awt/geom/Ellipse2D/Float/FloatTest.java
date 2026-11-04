/*
 * @test
 * @summary java.awt.geom.Ellipse2D.Float conformance: ellipse bounds and containment.
 * @run main FloatTest
 */


import java.awt.geom.Ellipse2D;
public class FloatTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        Ellipse2D e = new Ellipse2D.Float(0.0f, 0.0f, 10.0f, 10.0f);
        check(e.getX() == 0.0 && e.getY() == 0.0, "x/y");
        check(e.getWidth() == 10.0 && e.getHeight() == 10.0, "width/height");
        check(e.contains(5.0, 5.0), "contains center");
        check(!e.contains(10.0, 10.0), "does not contain corner");
        check(e.getBounds2D().getWidth() == 10.0, "bounds");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
