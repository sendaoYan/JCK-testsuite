/*
 * @test
 * @summary java.awt.geom.GeneralPath conformance: path building and containment.
 * @run main GeneralPathTest
 */


import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;
public class GeneralPathTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        GeneralPath p = new GeneralPath();
        p.moveTo(0, 0);
        p.lineTo(10, 0);
        p.lineTo(0, 10);
        p.closePath();
        check(p.contains(2.0, 2.0), "contains interior");
        check(!p.contains(9.0, 9.0), "exterior");
        Rectangle2D b = p.getBounds2D();
        check(b.getX() == 0.0 && b.getWidth() == 10.0, "bounds");
        Point2D cur = p.getCurrentPoint();
        check(cur.getX() == 0.0 && cur.getY() == 0.0, "current point after close");
        check(p.getWindingRule() == java.awt.geom.Path2D.WIND_NON_ZERO, "winding rule");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
