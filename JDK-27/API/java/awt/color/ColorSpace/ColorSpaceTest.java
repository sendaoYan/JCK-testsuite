/*
 * @test
 * @summary java.awt.color.ColorSpace conformance: white-box structure plus black-box behavior.
 * @run main ColorSpaceTest
 */

import java.awt.color.ColorSpace;
import java.io.Serializable;
import java.lang.reflect.*;

public class ColorSpaceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n, Class<?>... p) {
        try { c.getMethod(n, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.color.ColorSpace");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) && Modifier.isAbstract(m), "abstract public class");
        check(!c.isInterface() && !c.isEnum(), "class, not interface/enum");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
        String[] types = {"TYPE_XYZ","TYPE_RGB","TYPE_GRAY","TYPE_HSV","TYPE_CMYK","TYPE_2CLR","TYPE_FCLR"};
        for (String t : types) {
            Field f = c.getField(t);
            check(Modifier.isPublic(f.getModifiers()) && Modifier.isStatic(f.getModifiers())
                  && f.getType() == int.class, "constant " + t);
        }
        String[] css = {"CS_sRGB","CS_LINEAR_RGB","CS_CIEXYZ","CS_PYCC","CS_GRAY"};
        for (String t : css) {
            Field f = c.getField(t);
            check(Modifier.isPublic(f.getModifiers()) && Modifier.isStatic(f.getModifiers())
                  && f.getType() == int.class, "constant " + t);
        }
        check(hasMethod(c, "getInstance", int.class), "static getInstance(int)");
        check(hasMethod(c, "toRGB", float[].class), "toRGB(float[])");
        check(hasMethod(c, "fromRGB", float[].class), "fromRGB(float[])");
        check(hasMethod(c, "toCIEXYZ", float[].class), "toCIEXYZ(float[])");
        check(hasMethod(c, "fromCIEXYZ", float[].class), "fromCIEXYZ(float[])");
        check(hasMethod(c, "getType"), "getType()");
        check(hasMethod(c, "getNumComponents"), "getNumComponents()");
        check(hasMethod(c, "getName", int.class), "getName(int)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(ColorSpace.TYPE_XYZ == 0 && ColorSpace.TYPE_RGB == 5 && ColorSpace.TYPE_GRAY == 6
              && ColorSpace.TYPE_HSV == 7 && ColorSpace.TYPE_CMYK == 9, "type constant values");

        ColorSpace srgb = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        check(srgb.getType() == ColorSpace.TYPE_RGB, "sRGB type == TYPE_RGB");
        check(srgb.getNumComponents() == 3, "sRGB 3 components");
        check(srgb.isCS_sRGB(), "sRGB isCS_sRGB");
        check(srgb.getMinValue(0) == 0.0f && srgb.getMaxValue(0) == 1.0f, "sRGB min/max 0..1");
        float[] white = srgb.toRGB(new float[]{1, 1, 1});
        check(close(white[0], 1) && close(white[1], 1) && close(white[2], 1), "sRGB toRGB white stays white");

        ColorSpace gray = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        check(gray.getType() == ColorSpace.TYPE_GRAY, "gray type == TYPE_GRAY");
        check(gray.getNumComponents() == 1, "gray 1 component");
        check("Gray".equals(gray.getName(0)), "gray component name");
        float[] grayWhite = gray.toRGB(new float[]{1});
        check(grayWhite.length == 3 && close(grayWhite[0], 1), "gray toRGB returns 3 comps");

        // round-trip: toRGB/fromRGB on sRGB
        float[] c = {0.25f, 0.5f, 0.75f};
        float[] back = srgb.fromRGB(srgb.toRGB(c));
        check(close(back[0], 0.25f) && close(back[1], 0.5f) && close(back[2], 0.75f),
              "sRGB toRGB/fromRGB round-trip");

        // custom anonymous subclass: default getType/getNumComponents from ctor args
        ColorSpace custom = new ColorSpace(ColorSpace.TYPE_RGB, 3) {
            public float[] toRGB(float[] v) { return v.clone(); }
            public float[] fromRGB(float[] v) { return v.clone(); }
            public float[] toCIEXYZ(float[] v) { return v.clone(); }
            public float[] fromCIEXYZ(float[] v) { return v.clone(); }
        };
        check(custom.getType() == ColorSpace.TYPE_RGB && custom.getNumComponents() == 3,
              "custom subclass type/components");
        check(custom.getName(0) != null, "custom component name non-null");
        check(!custom.isCS_sRGB(), "custom not CS_sRGB");
        float[] round = custom.fromRGB(custom.toRGB(c));
        check(close(round[0], 0.25f) && close(round[1], 0.5f) && close(round[2], 0.75f),
              "custom identity round-trip");
    }

    static boolean close(double a, double b) { return Math.abs(a - b) < 1e-4; }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
