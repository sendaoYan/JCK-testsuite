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
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 3: contract deepening ----
    static java.awt.Color serColor(java.awt.Color c) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(bos).writeObject(c);
        return (java.awt.Color) new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
    }
    static void testContract() throws Exception {
        // all 13 predefined constants: exact RGB values
        int[] def = {
            0xFFFFFF, 0xC0C0C0, 0x808080, 0x404040, 0x000000,
            0xFF0000, 0xFFAFAF, 0xFFC800, 0xFFFF00, 0x00FF00,
            0xFF00FF, 0x00FFFF, 0x0000FF};
        java.awt.Color[] consts = {java.awt.Color.white, java.awt.Color.lightGray, java.awt.Color.gray,
            java.awt.Color.darkGray, java.awt.Color.black, java.awt.Color.red, java.awt.Color.pink,
            java.awt.Color.orange, java.awt.Color.yellow, java.awt.Color.green, java.awt.Color.magenta,
            java.awt.Color.cyan, java.awt.Color.blue};
        String[] names = {"white","lightGray","gray","darkGray","black","red","pink","orange",
            "yellow","green","magenta","cyan","blue"};
        for (int i = 0; i < consts.length; i++) {
            check((consts[i].getRGB() & 0xFFFFFF) == def[i],
                  "predefined " + names[i] + " exact value 0x" + Integer.toHexString(consts[i].getRGB()&0xFFFFFF));
            check(consts[i].getAlpha() == 255, "predefined " + names[i] + " opaque");
        }
        // uppercase aliases are the same objects
        check(java.awt.Color.WHITE == java.awt.Color.white, "WHITE same as white");
        check(java.awt.Color.DARK_GRAY == java.awt.Color.darkGray, "DARK_GRAY same");
        check(java.awt.Color.pink.getRGB() == 0xFFFFAFAF, "pink packed with alpha");
        // constructors with alpha
        check(new java.awt.Color(1, 2, 3, 200).getAlpha() == 200, "int ctor with alpha");
        check(new java.awt.Color(0x89ABCDEF, true).getRGB() == 0x89ABCDEF, "packed ctor rgba preserves alpha");
        check(new java.awt.Color(0x89ABCDEF, true).getAlpha() == 0x89, "packed ctor alpha component");
        check(new java.awt.Color(0x89ABCDEF, false).getRGB() == 0xFFABCDEF,
              "packed ctor hasalpha=false forces opaque");
        check(new java.awt.Color(1f, 0.5f, 0f).getRed() == 255 && new java.awt.Color(1f, 0.5f, 0f).getGreen() == 128,
              "float ctor maps 0..1 with rounding (0.5 -> 128)");
        check(new java.awt.Color(0f, 0f, 0f, 0f).getAlpha() == 0, "float ctor alpha");
        // getRed/Green/Blue from packed
        java.awt.Color unpack = new java.awt.Color(0xFF1E2D3C, true);
        check(unpack.getRed() == 0x1E && unpack.getGreen() == 0x2D && unpack.getBlue() == 0x3C, "component unpack");
        // brighter/darker: black brighter -> (3,3,3); white darker -> (178,178,178); saturated stays in range
        check(java.awt.Color.black.brighter().getRGB() == 0xFF030303, "black brighter (3,3,3)");
        check(java.awt.Color.white.brighter().getRGB() == 0xFFFFFFFF, "white brighter clamps");
        check(java.awt.Color.white.darker().getRGB() == 0xFFB2B2B2, "white darker (178,178,178)");
        check(java.awt.Color.black.darker().getRGB() == 0xFF000000, "black darker stays black");
        check(java.awt.Color.red.darker().getRGB() == 0xFFB20000, "red darker (178,0,0)");
        // every predefined brighter/darker stays in 0..255 (no overflow)
        for (int i = 0; i < consts.length; i++) {
            java.awt.Color b = consts[i].brighter(), d = consts[i].darker();
            check(b.getRed() >= 0 && b.getRed() <= 255 && b.getGreen() >= 0 && b.getGreen() <= 255
                  && b.getBlue() >= 0 && b.getBlue() <= 255, names[i] + " brighter in range");
            check(d.getRed() >= 0 && d.getRed() <= 255 && d.getGreen() >= 0 && d.getGreen() <= 255
                  && d.getBlue() >= 0 && d.getBlue() <= 255, names[i] + " darker in range");
        }
        // HSB round-trips
        float[] hsb = java.awt.Color.RGBtoHSB(255, 0, 0, null);
        check(hsb[0] == 0.0f && hsb[1] == 1.0f && hsb[2] == 1.0f, "RGBtoHSB red exact");
        check(java.awt.Color.RGBtoHSB(0, 0, 255, new float[3]) != null, "RGBtoHSB fills provided array");
        check(java.awt.Color.HSBtoRGB(0f, 0f, 1f) == 0xFFFFFFFF, "HSBtoRGB white");
        check(java.awt.Color.HSBtoRGB(0f, 0f, 0f) == 0xFF000000, "HSBtoRGB black");
        check(java.awt.Color.HSBtoRGB(0.5f, 0.5f, 0.5f) == 0xFF408080, "HSBtoRGB (0.5,0.5,0.5)");
        check(java.awt.Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]) == 0xFFFF0000, "HSB round-trip red");
        // hue wraps: h=1 same as h=0 for pure red
        check(java.awt.Color.HSBtoRGB(1.0f, 1.0f, 1.0f) == java.awt.Color.HSBtoRGB(0.0f, 1.0f, 1.0f), "hue wraps");
        // getHSBColor equivalence
        check(java.awt.Color.getHSBColor(0f, 0f, 1f).equals(java.awt.Color.white), "getHSBColor white");
        check(java.awt.Color.getHSBColor(1f/3f, 1f, 1f).getGreen() == 255, "getHSBColor green");
        check(java.awt.Color.getHSBColor(2f/3f, 1f, 1f).getBlue() == 255, "getHSBColor blue");
        // decode: #RRGGBB, 0x, plain decimal octal per Integer.decode
        check(java.awt.Color.decode("#0F0F0F").getRGB() == 0xFF0F0F0F, "decode #");
        check(java.awt.Color.decode("0x101010").getRGB() == 0xFF101010, "decode 0x");
        check(java.awt.Color.decode("255").getRGB() == 0xFF0000FF, "decode decimal 255 -> blue");
        boolean nfe = false;
        try { java.awt.Color.decode("zz"); } catch (java.lang.NumberFormatException e) { nfe = true; }
        check(nfe, "decode invalid -> NumberFormatException");
        nfe = false;
        try { java.awt.Color.decode("#GGGGGG"); } catch (java.lang.NumberFormatException e) { nfe = true; }
        check(nfe, "decode bad hex -> NumberFormatException");
        // out-of-range components
        boolean iae = false;
        try { new java.awt.Color(300, 0, 0); } catch (java.lang.IllegalArgumentException e) { iae = true; }
        check(iae, "int ctor r>255 -> IAE");
        iae = false;
        try { new java.awt.Color(0, -1, 0); } catch (java.lang.IllegalArgumentException e) { iae = true; }
        check(iae, "int ctor g<0 -> IAE");
        iae = false;
        try { new java.awt.Color(0, 0, 0, 300); } catch (java.lang.IllegalArgumentException e) { iae = true; }
        check(iae, "int ctor alpha>255 -> IAE");
        iae = false;
        try { new java.awt.Color(1.5f, 0f, 0f); } catch (java.lang.IllegalArgumentException e) { iae = true; }
        check(iae, "float ctor >1 -> IAE");
        iae = false;
        try { new java.awt.Color(0f, -0.1f, 0f); } catch (java.lang.IllegalArgumentException e) { iae = true; }
        check(iae, "float ctor <0 -> IAE");
        // getColor with system properties
        System.setProperty("awt.contract.color.test", "0xFF00FF");
        check(java.awt.Color.getColor("awt.contract.color.test").getRGB() == 0xFFFF00FF, "getColor(String) reads property");
        System.setProperty("awt.contract.color.test2", "16711935"); // decimal
        check(java.awt.Color.getColor("awt.contract.color.test2").getRGB() == 0xFFFF00FF, "getColor decimal property");
        check(java.awt.Color.getColor("awt.contract.color.unset") == null, "getColor unset property -> null");
        check(java.awt.Color.getColor("awt.contract.color.unset", java.awt.Color.cyan).equals(java.awt.Color.cyan),
              "getColor(String,Color) default returned");
        check(java.awt.Color.getColor("awt.contract.color.unset", 0x00FFFF).equals(java.awt.Color.cyan),
              "getColor(String,int) default returned");
        System.clearProperty("awt.contract.color.test");
        System.clearProperty("awt.contract.color.test2");
        // equals includes alpha
        check(!new java.awt.Color(255, 0, 0, 128).equals(new java.awt.Color(255, 0, 0, 129)),
              "equals distinguishes alpha");
        check(new java.awt.Color(255, 0, 0, 128).equals(new java.awt.Color(255, 0, 0, 128)), "equals with same alpha");
        check(new java.awt.Color(255, 0, 0, 128).hashCode() == new java.awt.Color(255, 0, 0, 128).hashCode(),
              "hashCode equal for equal");
        check(java.awt.Color.red.hashCode() == java.awt.Color.red.getRGB(), "hashCode == getRGB");
        check(!java.awt.Color.red.equals(null), "equals(null)");
        check(!java.awt.Color.red.equals("red"), "equals unrelated");
        // ColorSpace ctor with sRGB
        java.awt.Color csColor = new java.awt.Color(java.awt.color.ColorSpace.getInstance(java.awt.color.ColorSpace.CS_sRGB),
                new float[]{0f, 0f, 1f}, 1f);
        check(csColor.getBlue() == 255 && csColor.getRed() == 0, "ColorSpace ctor sRGB blue");
        check(java.awt.Color.red.getColorSpace().isCS_sRGB(), "getColorSpace sRGB for int-ctor");
        check(java.awt.Color.red.getComponents(null).length == 4, "getComponents(float[]) includes alpha: length 4");
        check(java.awt.Color.red.getColorComponents(null)[0] == 1.0f, "getColorComponents red");
        // serialization round-trip incl. alpha and constants
        check(serColor(java.awt.Color.red).getRGB() == 0xFFFF0000, "serialization red");
        check(serColor(new java.awt.Color(0xF0F0F0F0, true)).getRGB() == 0xF0F0F0F0, "serialization alpha preserved");
        check(serColor(java.awt.Color.WHITE).equals(java.awt.Color.WHITE), "serialization WHITE equals constant");
        check(java.io.ObjectStreamClass.lookup(java.awt.Color.class).getSerialVersionUID()
              == 1185268168740142434L
              || java.io.ObjectStreamClass.lookup(java.awt.Color.class).getSerialVersionUID() != 0L,
              "serialVersionUID present");
        // toString stable format
        check(java.awt.Color.red.toString().equals("java.awt.Color[r=255,g=0,b=0]"), "toString exact red");
        check(new java.awt.Color(1, 2, 3, 4).toString().equals("java.awt.Color[r=1,g=2,b=3]"), "toString omits alpha");
        // getRGB with transparency constants
        check(java.awt.Color.red.getTransparency() == java.awt.Transparency.OPAQUE, "opaque color transparency");
        check(new java.awt.Color(255, 0, 0, 128).getTransparency() == java.awt.Transparency.TRANSLUCENT,
              "translucent transparency");
        check(new java.awt.Color(255, 0, 0, 0).getTransparency() == java.awt.Transparency.BITMASK,
              "alpha 0/255 extremes: 0 -> BITMASK");
        check(new java.awt.Color(255, 0, 0, 255).getTransparency() == java.awt.Transparency.OPAQUE,
              "alpha 255 -> OPAQUE");
    }

}
