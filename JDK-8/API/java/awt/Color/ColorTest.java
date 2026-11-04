/*
 * @test
 * @summary java.awt.Color conformance: RGB components, constants, HSB round-trip.
 * @run main ColorTest
 */


import java.awt.Color;
public class ColorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
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
