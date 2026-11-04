/*
 * @test
 * @summary java.awt.GradientPaint conformance: start/end points and colors.
 * @run main GradientPaintTest
 */


import java.awt.GradientPaint;
import java.awt.Color;
public class GradientPaintTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        GradientPaint g = new GradientPaint(0f, 0f, Color.RED, 10f, 10f, Color.BLUE);
        check(g.getPoint1().getX() == 0.0 && g.getPoint1().getY() == 0.0, "point1");
        check(g.getPoint2().getX() == 10.0 && g.getPoint2().getY() == 10.0, "point2");
        check(g.getColor1().equals(Color.RED), "color1");
        check(g.getColor2().equals(Color.BLUE), "color2");
        check(!g.isCyclic(), "not cyclic by default");
        GradientPaint cyclic = new GradientPaint(0f, 0f, Color.RED, 10f, 10f, Color.BLUE, true);
        check(cyclic.isCyclic(), "cyclic");
        check(g.getTransparency() == java.awt.Transparency.OPAQUE, "opaque transparency");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
