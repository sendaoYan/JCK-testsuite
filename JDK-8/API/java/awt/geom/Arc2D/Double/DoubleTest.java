/*
 * @test
 * @summary java.awt.geom.Arc2D.Double conformance: arc bounds and angles.
 * @run main DoubleTest
 */


import java.awt.geom.Arc2D;
public class DoubleTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        Arc2D a = new Arc2D.Double(0.0, 0.0, 10.0, 20.0, 30.0, 60.0, Arc2D.PIE);
        check(a.getX() == 0.0 && a.getY() == 0.0, "x/y");
        check(a.getWidth() == 10.0 && a.getHeight() == 20.0, "width/height");
        check(a.getAngleStart() == 30.0, "angle start");
        check(a.getAngleExtent() == 60.0, "angle extent");
        check(a.getArcType() == Arc2D.PIE, "arc type PIE");
        check(!a.getBounds2D().isEmpty(), "non-empty bounds");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
