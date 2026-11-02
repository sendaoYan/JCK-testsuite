/*
 * @test
 * @summary java.awt.geom.RoundRectangle2D.Double conformance: rounded corners.
 * @run main DoubleTest
 */


import java.awt.geom.RoundRectangle2D;
public class DoubleTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        RoundRectangle2D r = new RoundRectangle2D.Double(0.0, 0.0, 20.0, 10.0, 4.0, 6.0);
        check(r.getX() == 0.0 && r.getY() == 0.0, "x/y");
        check(r.getWidth() == 20.0 && r.getHeight() == 10.0, "width/height");
        check(r.getArcWidth() == 4.0 && r.getArcHeight() == 6.0, "arc width/height");
        check(r.contains(5.0, 5.0), "contains center");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
