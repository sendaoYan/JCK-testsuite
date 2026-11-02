/*
 * @test
 * @summary java.awt.geom.Arc2D.Float conformance: arc bounds and angles.
 * @run main FloatTest
 */


import java.awt.geom.Arc2D;
public class FloatTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        Arc2D a = new Arc2D.Float(0.0f, 0.0f, 10.0f, 20.0f, 30.0f, 60.0f, Arc2D.PIE);
        check(a.getX() == 0.0 && a.getY() == 0.0, "x/y");
        check(a.getWidth() == 10.0 && a.getHeight() == 20.0, "width/height");
        check(a.getAngleStart() == 30.0, "angle start");
        check(a.getAngleExtent() == 60.0, "angle extent");
        check(a.getArcType() == Arc2D.PIE, "arc type PIE");
        check(!a.getBounds2D().isEmpty(), "non-empty bounds");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
