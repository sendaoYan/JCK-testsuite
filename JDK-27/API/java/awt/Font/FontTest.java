/*
 * @test
 * @summary java.awt.Font conformance: white-box structure + black-box behavior.
 * @run main FontTest
 */

import java.lang.reflect.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;

public class FontTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static FontRenderContext frc() {
        java.awt.image.BufferedImage img =
            new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        return img.createGraphics().getFontRenderContext();
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.Font", false, FontTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.Font");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public class");
        check(!Modifier.isAbstract(m), "concrete class");
        check(!c.isInterface(), "not interface");
        check(c.getEnumConstants() == null, "non-enum");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");

        check(c.getField("DIALOG").getType() == String.class, "static String DIALOG");
        check(c.getField("PLAIN").getType() == int.class, "static int PLAIN");
        check(c.getField("BOLD").getType() == int.class, "static int BOLD");
        check(c.getField("ITALIC").getType() == int.class, "static int ITALIC");

        check(c.getConstructor(String.class, int.class, int.class) != null, "ctor (String,int,int)");
        for (String name : new String[] {"getFamily", "getName", "getFontName", "getPSName",
                "getStyle", "getSize", "getSize2D", "isPlain", "isBold", "isItalic",
                "isTransformed", "deriveFont", "decode", "canDisplay", "canDisplayUpTo",
                "getNumGlyphs", "getMissingGlyphCode", "getBaselineFor", "getAttributes",
                "getAvailableAttributes", "getLineMetrics", "getStringBounds", "getMaxCharBounds",
                "createGlyphVector", "getTransform", "equals", "hashCode", "toString"})
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(mm -> mm.getName().equals(name)), "method present: " + name);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // constants
        check(java.awt.Font.DIALOG.equals("Dialog"), "DIALOG constant");
        check(java.awt.Font.SANS_SERIF.equals("SansSerif"), "SANS_SERIF constant");
        check(java.awt.Font.SERIF.equals("Serif"), "SERIF constant");
        check(java.awt.Font.MONOSPACED.equals("Monospaced"), "MONOSPACED constant");
        check(java.awt.Font.DIALOG_INPUT.equals("DialogInput"), "DIALOG_INPUT constant");
        check(java.awt.Font.PLAIN == 0, "PLAIN == 0");
        check(java.awt.Font.BOLD == 1, "BOLD == 1");
        check(java.awt.Font.ITALIC == 2, "ITALIC == 2");
        check(java.awt.Font.ROMAN_BASELINE == 0, "ROMAN_BASELINE == 0");
        check(java.awt.Font.CENTER_BASELINE == 1, "CENTER_BASELINE == 1");
        check(java.awt.Font.HANGING_BASELINE == 2, "HANGING_BASELINE == 2");
        check(java.awt.Font.TRUETYPE_FONT == 0, "TRUETYPE_FONT == 0");
        check(java.awt.Font.TYPE1_FONT == 1, "TYPE1_FONT == 1");
        check(java.awt.Font.LAYOUT_LEFT_TO_RIGHT == 0, "LAYOUT_LEFT_TO_RIGHT == 0");
        check(java.awt.Font.LAYOUT_RIGHT_TO_LEFT == 1, "LAYOUT_RIGHT_TO_LEFT == 1");
        check(java.awt.Font.LAYOUT_NO_START_CONTEXT == 2, "LAYOUT_NO_START_CONTEXT == 2");
        check(java.awt.Font.LAYOUT_NO_LIMIT_CONTEXT == 4, "LAYOUT_NO_LIMIT_CONTEXT == 4");

        // constructor + basic getters
        java.awt.Font f = new java.awt.Font("Dialog", java.awt.Font.BOLD, 24);
        check(f.getName().equals("Dialog"), "getName returns logical name");
        check(f.getFamily().equals("Dialog"), "getFamily returns logical family");
        check(f.getStyle() == java.awt.Font.BOLD, "getStyle == BOLD");
        check(f.getSize() == 24, "getSize == 24");
        check(f.getSize2D() == 24.0f, "getSize2D == 24.0");
        check(!f.isPlain() && f.isBold() && !f.isItalic(), "isPlain/isBold/isItalic");
        check(!f.isTransformed(), "plain font not transformed");
        check(f.getFontName() != null && !f.getFontName().isEmpty(), "getFontName non-empty");
        check(f.getPSName() != null && !f.getPSName().isEmpty(), "getPSName non-empty");

        java.awt.Font fi = new java.awt.Font("SansSerif", java.awt.Font.ITALIC, 12);
        check(fi.isItalic() && !fi.isBold() && !fi.isPlain(), "italic style flags");
        check(fi.getFamily().equals("SansSerif"), "SansSerif family");

        // deriveFont
        java.awt.Font d1 = f.deriveFont(30f);
        check(d1.getSize() == 30 && d1.getStyle() == java.awt.Font.BOLD, "deriveFont(float) keeps style");
        java.awt.Font d2 = f.deriveFont(java.awt.Font.ITALIC);
        check(d2.getStyle() == java.awt.Font.ITALIC && d2.getSize() == 24, "deriveFont(int) keeps size");
        java.awt.Font d3 = f.deriveFont(java.awt.Font.ITALIC | java.awt.Font.BOLD, 18f);
        check(d3.getSize() == 18 && d3.isItalic() && d3.isBold(), "deriveFont(style,size)");
        java.awt.Font d4 = f.deriveFont(java.awt.Font.PLAIN, 24f);
        check(d4.isPlain() && d4.getSize() == 24, "deriveFont(style,size) to plain");

        // equals / hashCode
        java.awt.Font e1 = new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12);
        java.awt.Font e2 = new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12);
        check(e1.equals(e2), "equal fonts");
        check(e1.hashCode() == e2.hashCode(), "equal hashCodes");
        check(!e1.equals(f), "different style/size not equal");
        check(!e1.equals(null), "not equal null");
        check(!e1.equals("Dialog"), "not equal unrelated type");
        check(f.deriveFont(30f).equals(new java.awt.Font("Dialog", java.awt.Font.BOLD, 30)),
              "derived font equals constructed font");

        // toString
        String ts = f.toString();
        check(ts.startsWith("java.awt.Font["), "toString prefix");
        check(ts.contains("name=Dialog"), "toString contains name");
        check(ts.contains("style=bold"), "toString contains style: " + ts);
        check(ts.contains("size=24"), "toString contains size");

        // decode
        java.awt.Font dec = java.awt.Font.decode("Dialog-BOLD-24");
        check(dec.getFamily().equals("Dialog") && dec.isBold() && dec.getSize() == 24,
              "decode Dialog-BOLD-24");
        java.awt.Font dec2 = java.awt.Font.decode("SansSerif-42");
        check(dec2.getFamily().equals("SansSerif") && dec2.isPlain() && dec2.getSize() == 42,
              "decode SansSerif-42");
        java.awt.Font decNull = java.awt.Font.decode(null);
        check(decNull.getFamily().equals("Dialog") && decNull.isPlain() && decNull.getSize() == 12,
              "decode null -> Dialog plain 12");
        java.awt.Font decItalic = java.awt.Font.decode("Dialog-italic-18");
        check(decItalic.isItalic() && decItalic.getSize() == 18, "decode Dialog-italic-18");

        // canDisplay / canDisplayUpTo
        check(f.canDisplay('A'), "canDisplay('A')");
        check(f.canDisplay('a'), "canDisplay('a')");
        check(f.canDisplayUpTo("Hello") == -1, "canDisplayUpTo ascii == -1");
        check(f.canDisplayUpTo(new char[] {'H', 'i'}, 0, 2) == -1, "canDisplayUpTo char[] == -1");

        // glyph / baseline info
        check(f.getNumGlyphs() > 0, "getNumGlyphs positive");
        check(f.getMissingGlyphCode() >= 0, "getMissingGlyphCode non-negative");
        check(f.getBaselineFor('A') == java.awt.Font.ROMAN_BASELINE, "getBaselineFor('A') == ROMAN");

        // attributes
        java.util.Map<java.awt.font.TextAttribute, ?> attrs = f.getAttributes();
        check(attrs != null, "getAttributes non-null");
        check(attrs.containsKey(java.awt.font.TextAttribute.FAMILY), "attributes contain FAMILY");
        check(((Float) attrs.get(java.awt.font.TextAttribute.SIZE)).floatValue() == 24f,
              "attributes SIZE == 24");
        java.text.AttributedCharacterIterator.Attribute[] avail = f.getAvailableAttributes();
        check(avail.length > 0, "getAvailableAttributes non-empty");
        boolean hasFamily = false;
        for (java.text.AttributedCharacterIterator.Attribute a : avail)
            if (a == java.awt.font.TextAttribute.FAMILY) hasFamily = true;
        check(hasFamily, "available attributes include FAMILY");

        // line metrics + string bounds (relationships only, not exact pixels)
        FontRenderContext frc = frc();
        LineMetrics lm = f.getLineMetrics("Hello", frc);
        check(lm.getAscent() >= 0 && lm.getDescent() >= 0 && lm.getHeight() >= 0,
              "line metrics non-negative");
        check(lm.getHeight() >= lm.getAscent() + lm.getDescent(), "height >= ascent+descent");

        java.awt.Font small = new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12);
        Rectangle2D b24 = f.getStringBounds("MMM", frc);
        Rectangle2D b12 = small.getStringBounds("MMM", frc);
        check(b24.getWidth() > b12.getWidth(), "size 24 'MMM' wider than size 12");
        Rectangle2D bM = f.getStringBounds("M", frc);
        Rectangle2D bMMM = f.getStringBounds("MMM", frc);
        check(bMMM.getWidth() > bM.getWidth(), "longer string wider than shorter string");
        check(f.getMaxCharBounds(frc).getWidth() > 0, "maxCharBounds positive width");

        // glyph vector
        GlyphVector gv = f.createGlyphVector(frc, "ABC");
        check(gv.getNumGlyphs() == 3, "createGlyphVector numGlyphs == 3");

        // combination chain
        java.awt.Font chained = java.awt.Font.decode("Dialog-PLAIN-10");
        java.awt.Font grown = chained.deriveFont(20f).deriveFont(java.awt.Font.BOLD);
        check(grown.getSize() == 20 && grown.isBold(), "decode -> deriveFont(size) -> deriveFont(style)");
        check(grown.equals(new java.awt.Font("Dialog", java.awt.Font.BOLD, 20)),
              "chained font equals directly-constructed");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
