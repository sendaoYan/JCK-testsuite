/*
 * @test
 * @summary java.awt.Color conformance: white-box structure + black-box behavior.
 * @run main ColorTest
 */

import java.lang.reflect.*;

public class ColorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean approx(float a, float b) {
        return Math.abs(a - b) < 1e-6f;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.Color", false, ColorTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.Color");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public class");
        check(!Modifier.isAbstract(m), "concrete class");
        check(!c.isInterface(), "not interface");
        check(c.getEnumConstants() == null, "non-enum");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(java.awt.Paint.class.isAssignableFrom(c), "implements java.awt.Paint");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");

        check(c.getField("white").getType() == java.awt.Color.class, "static Color white");
        check(c.getField("WHITE").getType() == java.awt.Color.class, "static Color WHITE");
        check(c.getField("black").getType() == java.awt.Color.class, "static Color black");
        check(c.getField("red").getType() == java.awt.Color.class, "static Color red");

        check(c.getConstructor(int.class, int.class, int.class) != null, "ctor (int,int,int)");
        check(c.getConstructor(int.class, int.class, int.class, int.class) != null, "ctor (int,int,int,int)");
        check(c.getConstructor(int.class) != null, "ctor (int)");
        check(c.getConstructor(int.class, boolean.class) != null, "ctor (int,boolean)");
        check(c.getConstructor(float.class, float.class, float.class) != null, "ctor (float,float,float)");
        check(c.getConstructor(float.class, float.class, float.class, float.class) != null, "ctor (float,float,float,float)");
        check(c.getConstructor(java.awt.color.ColorSpace.class, float[].class, float.class) != null,
              "ctor (ColorSpace,float[],float)");

        for (String name : new String[] {"getRed", "getGreen", "getBlue", "getAlpha", "getRGB",
                "brighter", "darker", "decode", "getColor", "HSBtoRGB", "RGBtoHSB", "getHSBColor",
                "getRGBComponents", "getRGBColorComponents", "getComponents", "getColorComponents",
                "getColorSpace", "getTransparency", "equals", "hashCode", "toString"})
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(mm -> mm.getName().equals(name)), "method present: " + name);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // constants
        check(java.awt.Color.white.getRGB() == 0xffffffff, "WHITE rgb");
        check(java.awt.Color.black.getRGB() == 0xff000000, "BLACK rgb");
        check(java.awt.Color.red.getRGB() == 0xffff0000, "RED rgb");
        check(java.awt.Color.green.getRGB() == 0xff00ff00, "GREEN rgb");
        check(java.awt.Color.blue.getRGB() == 0xff0000ff, "BLUE rgb");
        check(java.awt.Color.white == java.awt.Color.WHITE, "lowercase/uppercase same instance");
        check(java.awt.Color.black == java.awt.Color.BLACK, "BLACK same instance");
        check(java.awt.Color.orange.getRed() == 255 && java.awt.Color.orange.getGreen() == 200
              && java.awt.Color.orange.getBlue() == 0, "orange components");
        check(java.awt.Color.pink.getRed() == 255 && java.awt.Color.pink.getGreen() == 175
              && java.awt.Color.pink.getBlue() == 175, "pink components");

        // int constructors
        java.awt.Color c = new java.awt.Color(1, 2, 3);
        check(c.getRed() == 1 && c.getGreen() == 2 && c.getBlue() == 3, "(int,int,int) components");
        check(c.getAlpha() == 255, "default alpha 255");
        check(c.getRGB() == 0xff010203, "getRGB ARGB: " + Integer.toHexString(c.getRGB()));
        java.awt.Color ca = new java.awt.Color(10, 20, 30, 128);
        check(ca.getAlpha() == 128, "(int,int,int,int) alpha");
        check(ca.getRGB() == 0x800a141e, "ARGB with alpha: " + Integer.toHexString(ca.getRGB()));

        // single-int constructors
        check(new java.awt.Color(0x00ff0000).getRGB() == 0xffff0000, "single-int rgb forces opaque alpha");
        check(new java.awt.Color(0xff000000, true).getRGB() == 0xff000000, "(int,true) preserves alpha bits");
        check(new java.awt.Color(0xff000000, true).getAlpha() == 255, "(int,true) alpha read back");
        check(new java.awt.Color(0x00ff0000, false).getAlpha() == 255, "(int,false) forces opaque");

        // float constructors (values stored, exact 0.5 style)
        java.awt.Color f = new java.awt.Color(0.5f, 0.25f, 0.75f);
        check(f.getRed() == 128 && f.getGreen() == 64 && f.getBlue() == 191,
              "float ctor quantized RGB: " + f.getRed() + "," + f.getGreen() + "," + f.getBlue());
        float[] fcomp = f.getRGBComponents(null);
        check(fcomp.length == 4, "float getRGBComponents length 4");
        check(fcomp[0] == 0.5f && fcomp[1] == 0.25f && fcomp[2] == 0.75f && fcomp[3] == 1.0f,
              "float color stores exact original components");

        // brighter / darker
        java.awt.Color black = new java.awt.Color(0, 0, 0);
        check(black.brighter().equals(new java.awt.Color(3, 3, 3)), "black.brighter() -> (3,3,3)");
        java.awt.Color red = new java.awt.Color(255, 0, 0);
        check(red.brighter().equals(red), "pure red brighter stays red");
        check(red.darker().getRed() == 178 && red.darker().getGreen() == 0,
              "red.darker() -> (178,0,0)");
        java.awt.Color half = new java.awt.Color(100, 100, 100, 128);
        check(half.brighter().getAlpha() == 128, "brighter preserves alpha");
        check(half.darker().getAlpha() == 128, "darker preserves alpha");

        // equals / hashCode
        check(new java.awt.Color(255, 0, 0).equals(java.awt.Color.red), "equal to constant");
        check(new java.awt.Color(255, 0, 0).hashCode() == java.awt.Color.red.hashCode(), "equal hashCodes");
        check(!new java.awt.Color(255, 0, 0).equals(new java.awt.Color(255, 0, 1)), "differing blue not equal");
        check(!new java.awt.Color(255, 0, 0).equals(null), "not equal null");
        check(!new java.awt.Color(255, 0, 0).equals("red"), "not equal unrelated type");

        // toString
        check(new java.awt.Color(255, 0, 0).toString().equals("java.awt.Color[r=255,g=0,b=0]"),
              "toString format: " + new java.awt.Color(255, 0, 0));

        // decode
        check(java.awt.Color.decode("#FF0000").equals(java.awt.Color.red), "decode #FF0000 -> red");
        check(java.awt.Color.decode("0x00FF00").equals(java.awt.Color.green), "decode 0x00FF00 -> green");
        check(java.awt.Color.decode("255").equals(new java.awt.Color(0, 0, 255)), "decode decimal -> blue");
        boolean nfe = false;
        try { java.awt.Color.decode("not-a-color"); } catch (NumberFormatException e) { nfe = true; }
        check(nfe, "decode invalid throws NumberFormatException");

        // HSB conversions
        check(java.awt.Color.HSBtoRGB(0f, 0f, 0f) == 0xff000000, "HSBtoRGB black");
        check(java.awt.Color.HSBtoRGB(0f, 0f, 1f) == 0xffffffff, "HSBtoRGB white");
        java.awt.Color hsb = java.awt.Color.getHSBColor(0f, 0f, 1f);
        check(hsb.equals(java.awt.Color.white), "getHSBColor(0,0,1) == white");
        float[] hsbv = java.awt.Color.RGBtoHSB(255, 0, 0, null);
        check(hsbv.length == 3, "RGBtoHSB length 3");
        check(approx(hsbv[0], 0f) && approx(hsbv[1], 1f) && approx(hsbv[2], 1f),
              "RGBtoHSB(255,0,0) == [0,1,1]");
        float[] hsbBlack = java.awt.Color.RGBtoHSB(0, 0, 0, null);
        check(approx(hsbBlack[0], 0f) && approx(hsbBlack[1], 0f) && approx(hsbBlack[2], 0f),
              "RGBtoHSB(0,0,0) == [0,0,0]");

        // component arrays
        java.awt.Color oc = new java.awt.Color(255, 128, 0);
        float[] rgb = oc.getRGBComponents(null);
        check(rgb.length == 4 && rgb[0] == 1.0f && approx(rgb[1], 128f / 255f) && rgb[3] == 1.0f,
              "getRGBComponents for int color");
        float[] rgbol = oc.getRGBColorComponents(null);
        check(rgbol.length == 3 && rgbol[0] == 1.0f && approx(rgbol[1], 128f / 255f),
              "getRGBColorComponents length 3");
        float[] comps = oc.getComponents(null);
        check(comps.length == 4 && comps[0] == 1.0f, "getComponents length 4 for sRGB");
        float[] ccomps = oc.getColorComponents(null);
        check(ccomps.length == 3 && ccomps[0] == 1.0f, "getColorComponents length 3 for sRGB");

        // color space + transparency
        check(oc.getColorSpace().getType() == java.awt.color.ColorSpace.TYPE_RGB, "sRGB color space");
        check(new java.awt.Color(1, 2, 3).getTransparency() == java.awt.Transparency.OPAQUE,
              "opaque transparency");
        check(new java.awt.Color(1, 2, 3, 0).getTransparency() == java.awt.Transparency.BITMASK,
              "alpha 0 -> BITMASK");
        check(new java.awt.Color(1, 2, 3, 128).getTransparency() == java.awt.Transparency.TRANSLUCENT,
              "alpha 128 -> TRANSLUCENT");

        // range validation
        boolean iae1 = false;
        try { new java.awt.Color(256, 0, 0); } catch (IllegalArgumentException e) { iae1 = true; }
        check(iae1, "red 256 throws IllegalArgumentException");
        boolean iae2 = false;
        try { new java.awt.Color(-1, 0, 0); } catch (IllegalArgumentException e) { iae2 = true; }
        check(iae2, "negative red throws IllegalArgumentException");
        boolean iae3 = false;
        try { new java.awt.Color(0, 0, 0, 256); } catch (IllegalArgumentException e) { iae3 = true; }
        check(iae3, "alpha 256 throws IllegalArgumentException");
        boolean iae4 = false;
        try { new java.awt.Color(0.5f, 1.5f, 0f); } catch (IllegalArgumentException e) { iae4 = true; }
        check(iae4, "float green 1.5 throws IllegalArgumentException");

        // combination chain
        java.awt.Color chained = java.awt.Color.decode("#336699");
        java.awt.Color expected = new java.awt.Color(0x33, 0x66, 0x99);
        check(chained.equals(expected), "decode -> equals int ctor");
        check(chained.getRed() == 0x33 && chained.getGreen() == 0x66 && chained.getBlue() == 0x99,
              "decoded components");
        check(chained.getRGB() == 0xff336699, "decoded ARGB: " + Integer.toHexString(chained.getRGB()));
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
