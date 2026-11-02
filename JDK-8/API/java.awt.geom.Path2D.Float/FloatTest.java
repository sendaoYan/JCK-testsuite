/*
 * @test
 * @summary java.awt.geom.Path2D.Float conformance: path building and containment.
 * @run main FloatTest
 */


import java.awt.geom.Path2D;
public class FloatTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        Path2D p = new Path2D.Float();
        p.moveTo(0.0, 0.0);
        p.lineTo(10.0, 0.0);
        p.lineTo(0.0, 10.0);
        p.closePath();
        check(p.contains(2.0, 2.0), "contains interior");
        check(!p.contains(9.0, 9.0), "exterior");
        check(p.getBounds2D().getWidth() == 10.0, "bounds width");
        check(p.getWindingRule() == Path2D.WIND_NON_ZERO, "winding rule");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
