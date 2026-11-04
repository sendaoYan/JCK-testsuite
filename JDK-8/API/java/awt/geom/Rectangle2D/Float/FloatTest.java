/*
 * @test
 * @summary java.awt.geom.Rectangle2D.Float conformance: bounds and containment.
 * @run main FloatTest
 */


import java.awt.geom.Rectangle2D;
public class FloatTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        Rectangle2D r = new Rectangle2D.Float(10.0f, 20.0f, 30.0f, 40.0f);
        check(r.getX() == 10.0 && r.getY() == 20.0, "x/y");
        check(r.getWidth() == 30.0 && r.getHeight() == 40.0, "width/height");
        check(r.contains(15.0, 25.0), "contains interior");
        check(!r.contains(5.0, 5.0), "exterior");
        check(r.intersects(30.0, 30.0, 10.0, 10.0), "intersects");
        check(r.getMaxX() == 40.0 && r.getMaxY() == 60.0, "max x/y");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
