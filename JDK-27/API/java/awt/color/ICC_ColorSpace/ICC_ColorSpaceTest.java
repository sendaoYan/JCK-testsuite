/*
 * @test
 * @summary java.awt.color.ICC_ColorSpace conformance: white-box structure plus black-box behavior.
 * @run main ICC_ColorSpaceTest
 */

import java.awt.color.*;
import java.lang.reflect.*;

public class ICC_ColorSpaceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean close(double a, double b, double tol) { return Math.abs(a - b) < tol; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.color.ICC_ColorSpace");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public class");
        check(!c.isInterface() && !c.isEnum(), "class, not interface/enum");
        check(ColorSpace.class.isAssignableFrom(c), "extends ColorSpace");
        check(c.getConstructor(ICC_Profile.class) != null, "constructor ICC_Profile");
        check(hasMethod(c, "getProfile"), "getProfile()");
        check(hasMethod(c, "toRGB", float[].class), "toRGB");
        check(hasMethod(c, "fromRGB", float[].class), "fromRGB");
        check(hasMethod(c, "toCIEXYZ", float[].class), "toCIEXYZ");
        check(hasMethod(c, "fromCIEXYZ", float[].class), "fromCIEXYZ");
    }

    static boolean hasMethod(Class<?> c, String n, Class<?>... p) {
        try { c.getMethod(n, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ICC_Profile srgb = ICC_Profile.getInstance(ColorSpace.CS_sRGB);
        ICC_ColorSpace cs = new ICC_ColorSpace(srgb);
        check(cs.getProfile() == srgb, "getProfile returns the profile instance");
        check(cs.getType() == ColorSpace.TYPE_RGB, "sRGB ICC_ColorSpace type TYPE_RGB");
        check(cs.getNumComponents() == 3, "3 components");
        check(ColorSpace.getInstance(ColorSpace.CS_sRGB).isCS_sRGB(), "sRGB singleton isCS_sRGB");
        check(!cs.isCS_sRGB(), "wrapped ICC profile is not the sRGB singleton");
        check(cs.getMinValue(0) == 0.0f && cs.getMaxValue(0) == 1.0f, "min/max 0..1");
        check("Red".equals(cs.getName(0)), "component 0 name");

        // toRGB / fromRGB round-trip
        float[] c = {0.2f, 0.5f, 0.8f};
        float[] rgb = cs.toRGB(c);
        check(rgb.length == 3, "toRGB returns 3 comps");
        float[] back = cs.fromRGB(rgb);
        check(close(back[0], 0.2f, 1e-3) && close(back[1], 0.5f, 1e-3) && close(back[2], 0.8f, 1e-3),
              "toRGB/fromRGB round-trip");

        // toCIEXYZ / fromCIEXYZ round-trip (sRGB -> XYZ -> sRGB)
        float[] xyz = cs.toCIEXYZ(c);
        check(xyz.length == 3, "toCIEXYZ returns 3 comps");
        float[] backXyz = cs.fromCIEXYZ(xyz);
        check(close(backXyz[0], 0.2f, 1e-2) && close(backXyz[1], 0.5f, 1e-2) && close(backXyz[2], 0.8f, 1e-2),
              "toCIEXYZ/fromCIEXYZ round-trip");

        // gray profile color space
        ICC_ColorSpace grayCs = new ICC_ColorSpace(ICC_Profile.getInstance(ColorSpace.CS_GRAY));
        check(grayCs.getType() == ColorSpace.TYPE_GRAY && grayCs.getNumComponents() == 1,
              "gray ICC_ColorSpace type/components");
        float[] g = grayCs.toRGB(new float[]{1.0f});
        check(g.length == 3, "gray toRGB 3 comps");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
