/*
 * @test
 * @summary java.awt.RadialGradientPaint conformance: center/focus/radius.
 * @run main RadialGradientPaintTest
 */


import java.awt.RadialGradientPaint;
import java.awt.Color;
public class RadialGradientPaintTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        float[] frac = {0f, 1f};
        Color[] colors = {Color.WHITE, Color.BLACK};
        RadialGradientPaint g = new RadialGradientPaint(5f, 5f, 10f, frac, colors);
        check(g.getCenterPoint().getX() == 5.0 && g.getCenterPoint().getY() == 5.0, "center point");
        check(g.getFocusPoint().getX() == 5.0 && g.getFocusPoint().getY() == 5.0, "focus defaults to center");
        check(g.getRadius() == 10f, "radius");
        check(g.getColors().length == 2, "colors");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
