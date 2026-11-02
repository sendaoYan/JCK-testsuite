/*
 * @test
 * @summary java.awt.Dimension conformance: width/height and copy.
 * @run main DimensionTest
 */


import java.awt.Dimension;
public class DimensionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        Dimension d = new Dimension(30, 40);
        check(d.width == 30 && d.height == 40, "width/height fields");
        check(d.getWidth() == 30.0 && d.getHeight() == 40.0, "getWidth/getHeight");
        Dimension copy = new Dimension(d);
        check(copy.width == 30 && copy.height == 40, "copy ctor");
        d.setSize(50, 60);
        check(d.getSize().width == 50 && d.getSize().height == 60, "setSize");
        check(d.equals(new Dimension(50, 60)), "equals");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
