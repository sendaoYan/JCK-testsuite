/*
 * @test
 * @summary java.awt.geom.RoundRectangle2D.Float conformance: rounded corners.
 * @run main FloatTest
 */


import java.awt.geom.RoundRectangle2D;
public class FloatTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        RoundRectangle2D r = new RoundRectangle2D.Float(0.0f, 0.0f, 20.0f, 10.0f, 4.0f, 6.0f);
        check(r.getX() == 0.0 && r.getY() == 0.0, "x/y");
        check(r.getWidth() == 20.0 && r.getHeight() == 10.0, "width/height");
        check(r.getArcWidth() == 4.0 && r.getArcHeight() == 6.0, "arc width/height");
        check(r.contains(5.0, 5.0), "contains center");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
