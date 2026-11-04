/*
 * @test
 * @summary java.awt.color.ICC_ColorSpace conformance: sRGB profile and conversions.
 * @run main ICC_ColorSpaceTest
 */


import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
public class ICC_ColorSpaceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        ICC_ColorSpace cs = new ICC_ColorSpace(ICC_Profile.getInstance(ICC_ColorSpace.CS_sRGB));
        check(cs.getType() == java.awt.color.ColorSpace.TYPE_RGB, "type RGB");
        check(cs.getNumComponents() == 3, "three components");
        check(cs.getProfile() != null, "profile");
        float[] rgb = cs.toRGB(new float[]{0.0f, 0.0f, 0.0f});
        check(rgb.length == 3 && rgb[0] == 0.0f, "toRGB black");
        float[] cmyk = cs.fromRGB(new float[]{1.0f, 1.0f, 1.0f});
        check(cmyk.length == 3, "fromRGB white");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
