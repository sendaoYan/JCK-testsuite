/*
 * @test
 * @summary java.awt.geom.Area conformance: boolean operations.
 * @run main AreaTest
 */


import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
public class AreaTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        Area a = new Area(new Rectangle2D.Double(0, 0, 10, 10));
        check(!a.isEmpty(), "not empty");
        Area b = new Area(new Rectangle2D.Double(5, 0, 10, 10));
        a.add(b);
        check(a.getBounds2D().getWidth() == 15.0, "union width");
        Area c = new Area(new Rectangle2D.Double(0, 0, 10, 10));
        c.subtract(new Area(new Rectangle2D.Double(0, 0, 5, 10)));
        check(c.getBounds2D().getWidth() == 5.0, "subtract");
        Area d = new Area(new Rectangle2D.Double(0, 0, 10, 10));
        d.intersect(new Area(new Rectangle2D.Double(5, 0, 10, 10)));
        check(d.getBounds2D().getWidth() == 5.0, "intersect");
        Area empty = new Area();
        check(empty.isEmpty(), "default empty");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
