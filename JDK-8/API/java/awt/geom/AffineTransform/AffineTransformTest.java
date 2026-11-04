/*
 * @test
 * @summary java.awt.geom.AffineTransform conformance: translate/rotate/scale/inverse.
 * @run main AffineTransformTest
 */


import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
public class AffineTransformTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        AffineTransform t = new AffineTransform();
        check(t.isIdentity(), "identity initially");
        t.translate(10.0, 20.0);
        check(t.getTranslateX() == 10.0 && t.getTranslateY() == 20.0, "translate");
        AffineTransform s = new AffineTransform();
        s.scale(2.0, 3.0);
        check(s.getScaleX() == 2.0 && s.getScaleY() == 3.0, "scale");
        AffineTransform r = new AffineTransform();
        r.rotate(Math.PI / 2);
        Point2D rp = r.transform(new Point2D.Double(1.0, 0.0), null);
        check(Math.abs(rp.getX()) < 1e-9 && Math.abs(rp.getY() - 1.0) < 1e-9, "rotate 90 maps (1,0)->(0,1)");
        Point2D p = t.transform(new Point2D.Double(0.0, 0.0), null);
        check(p.getX() == 10.0 && p.getY() == 20.0, "point transform translation");
        AffineTransform inv = t.createInverse();
        check(inv.getTranslateX() == -10.0 && inv.getTranslateY() == -20.0, "inverse");
        check(new AffineTransform(t).equals(t), "copy equals");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
