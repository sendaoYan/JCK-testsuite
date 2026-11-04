/*
 * @test
 * @summary java.awt.LinearGradientPaint conformance: start/end points and fractions.
 * @run main LinearGradientPaintTest
 */


import java.awt.LinearGradientPaint;
import java.awt.Color;
public class LinearGradientPaintTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        float[] frac = {0f, 1f};
        Color[] colors = {Color.RED, Color.BLUE};
        LinearGradientPaint g = new LinearGradientPaint(0f, 0f, 10f, 0f, frac, colors);
        check(g.getStartPoint().getX() == 0.0 && g.getStartPoint().getY() == 0.0, "start point");
        check(g.getEndPoint().getX() == 10.0 && g.getEndPoint().getY() == 0.0, "end point");
        check(g.getColors().length == 2 && g.getColors()[0].equals(Color.RED), "colors");
        check(g.getFractions().length == 2 && g.getFractions()[1] == 1.0f, "fractions");
        check(g.getCycleMethod() == java.awt.MultipleGradientPaint.CycleMethod.NO_CYCLE, "default cycle NO_CYCLE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
