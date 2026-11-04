/*
 * @test
 * @summary java.awt.Color conformance: RGB components, constants, HSB round-trip.
 * @run main ColorTest
 */


import java.awt.Color;
public class ColorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        Color c = new Color(255, 128, 0);
        check(c.getRed() == 255, "red");
        check(c.getGreen() == 128, "green");
        check(c.getBlue() == 0, "blue");
        check(c.getAlpha() == 255, "default alpha opaque");
        check((c.getRGB() & 0x00FFFFFF) == 0xFF8000, "RGB packed");
        Color ca = new Color(255, 128, 0, 100);
        check(ca.getAlpha() == 100, "explicit alpha");
        check(Color.RED.getRed() == 255 && Color.RED.getGreen() == 0 && Color.RED.getBlue() == 0, "RED constant");
        Color decoded = Color.decode("#00FF00");
        check(decoded.equals(Color.GREEN), "decode");
        float[] hsb = Color.RGBtoHSB(255, 0, 0, null);
        check(hsb[0] == 0.0f && hsb[1] == 1.0f && hsb[2] == 1.0f, "RGBtoHSB red hue");
        int rgb = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
        check((rgb & 0xFF0000) == 0xFF0000 && (rgb & 0x00FF00) == 0, "HSBtoRGB red");
        check(c.brighter().getRed() == 255, "brighter clamps");
        check(c.darker().getRed() < 255, "darker");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
