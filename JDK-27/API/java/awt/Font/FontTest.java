/*
 * @test
 * @summary java.awt.Font conformance: font data attributes (headless-safe):
 *          names (name/family/fontName/PSName), style/size model incl. float sizes,
 *          every deriveFont variant, decode() parsing incl. null and unknown fonts,
 *          getFont(map), value equality, attribute maps, displayability,
 *          serialization round-trip, style and logical-family constants, and the
 *          JDK-8-only getPeer reflection (textRequiresLayout is JDK-9+).
 * @run main FontTest
 */

import java.awt.Font;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

public class FontTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        // Names and style/size model.
        Font f = new Font("Dialog", Font.BOLD | Font.ITALIC, 14);
        check(f.getName().equals("Dialog"), "family name");
        check(f.getSize() == 14, "size");
        check(f.isBold() && f.isItalic(), "bold+italic");
        check(!f.isPlain(), "not plain");
        check(f.getStyle() == (Font.BOLD | Font.ITALIC), "style mask");
        check(Font.SERIF.equals("Serif"), "SERIF constant");
        check(f.getSize2D() == 14.0f, "float size matches int size");
        check(f.getFontName() != null, "font face name present");
        check(f.getFontName().startsWith("Dialog"), "face name derived from family");
        check(f.getPSName() != null, "postscript name present");
        check(f.getFamily() != null, "family present");
        check(f.toString().contains("Dialog"), "toString mentions family");
        check(f.getNumGlyphs() > 0, "glyph count positive");
        check(!f.hasLayoutAttributes(), "simple font has no layout attributes");

        // Style constants.
        check(Font.PLAIN == 0 && Font.BOLD == 1 && Font.ITALIC == 2, "style constants 0/1/2");
        check(Font.DIALOG.equals("Dialog") && Font.SANS_SERIF.equals("SansSerif")
                && Font.MONOSPACED.equals("Monospaced") && Font.SERIF.equals("Serif")
                && Font.DIALOG_INPUT.equals("DialogInput"), "logical family constants");
        check(Font.LAYOUT_LEFT_TO_RIGHT == 0 && Font.LAYOUT_RIGHT_TO_LEFT == 1, "layout direction constants");

        // deriveFont variants.
        Font plain = f.deriveFont(Font.PLAIN);
        check(plain.isPlain() && plain.getSize() == 14, "deriveFont style");
        Font bigger = f.deriveFont(20f);
        check(bigger.getSize() == 20, "deriveFont size");
        check(bigger.isBold() && bigger.isItalic(), "deriveFont size keeps style");
        Font both = f.deriveFont(Font.ITALIC, 9f);
        check(both.isItalic() && !both.isBold() && both.getSize2D() == 9f, "deriveFont style+size");
        Font scaled = plain.deriveFont(java.awt.geom.AffineTransform.getScaleInstance(2, 2));
        check(scaled.getSize2D() == 14f, "deriveFont(AffineTransform) keeps the point size: " + scaled.getSize2D());
        check(scaled.getTransform().getScaleX() == 2.0, "transform stored on derived font");
        check(plain.getTransform().isIdentity(), "untransformed font has identity transform");
        Map<TextAttribute, Object> attrs = new HashMap<TextAttribute, Object>();
        attrs.put(TextAttribute.SIZE, 30f);
        attrs.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
        Font fromAttrs = plain.deriveFont(attrs);
        check(fromAttrs.getSize() == 30, "deriveFont attributes size");
        check(TextAttribute.WEIGHT_BOLD.equals(fromAttrs.getAttributes().get(TextAttribute.WEIGHT)),
                "deriveFont attributes weight");
        // Deriving keeps the original untouched.
        check(f.getSize() == 14 && f.isBold(), "deriveFont does not mutate receiver");

        // decode(): "FAMILY-STYLE-SIZE" parsing.
        Font dec = Font.decode("Dialog-BOLD-18");
        check(dec.getName().equals("Dialog"), "decode family");
        check(dec.getStyle() == Font.BOLD, "decode style BOLD");
        check(dec.getSize() == 18, "decode size");
        check(Font.decode("Dialog-italic-7").getStyle() == Font.ITALIC, "decode style italic");
        check(Font.decode("Dialog-bolditalic-7").getStyle() == (Font.BOLD | Font.ITALIC), "decode style bolditalic");
        check(Font.decode("Dialog-plain-10").getStyle() == Font.PLAIN, "decode lowercase plain");
        check(Font.decode("Dialog-PLAIN-10").getStyle() == Font.PLAIN, "decode PLAIN");
        Font decNull = Font.decode(null);
        check(decNull.getName().equals("Dialog") && decNull.getStyle() == Font.PLAIN && decNull.getSize() == 12,
                "decode(null) is Dialog plain 12");
        Font decMissing = Font.decode("NoSuchFontAtAll-ITALIC-22");
        check(decMissing.getStyle() == Font.ITALIC && decMissing.getSize() == 22,
                "decode unknown family falls back but keeps style/size");
        check(Font.decode("Dialog-BOLD-18").equals(dec), "decode is deterministic");

        // getFont(map).
        Map<TextAttribute, Object> spec = new HashMap<TextAttribute, Object>();
        spec.put(TextAttribute.FAMILY, "Dialog");
        spec.put(TextAttribute.SIZE, 21f);
        check(Font.getFont(spec).getSize() == 21, "getFont(map)");
        try {
            Font.getFont((Map<TextAttribute, ?>) null);
            check(false, "getFont(null) should throw NPE");
        } catch (NullPointerException expected) {
            check(true, "getFont(null) throws NPE");
        }

        // Value equality.
        Font a = new Font("Dialog", Font.PLAIN, 12);
        Font b = new Font("Dialog", Font.PLAIN, 12);
        check(a.equals(b), "same spec equals");
        check(a.hashCode() == b.hashCode(), "hashCode follows equals");
        check(!a.equals(new Font("Dialog", Font.PLAIN, 13)), "different size not equal");
        check(!a.equals(new Font("Dialog", Font.BOLD, 12)), "different style not equal");
        check(!a.equals(null) && !a.equals("x"), "equals null/foreign false");

        // Attribute map and availability.
        Map<TextAttribute, ?> at = a.getAttributes();
        check(at != null && !at.isEmpty(), "attribute map non-empty");
        check("Dialog".equals(at.get(TextAttribute.FAMILY)), "attribute family");
        check(a.getAvailableAttributes().length > 0, "available attributes advertised");

        // Displayability.
        check(a.canDisplay('a'), "canDisplay for ASCII");
        check(a.canDisplayUpTo("abc") == -1, "canDisplayUpTo -1 when all displayable");

        // JDK-9+ specifics: deprecated getPeer removed; textRequiresLayout added.
        boolean hasGetPeer = true;
        try {
            Font.class.getMethod("getPeer");
        } catch (NoSuchMethodException e) {
            hasGetPeer = false;
        }
        check(!hasGetPeer, "JDK-27: deprecated getPeer removed");
        java.lang.reflect.Method trl;
        try {
            trl = Font.class.getMethod("textRequiresLayout", char[].class, int.class, int.class);
        } catch (NoSuchMethodException e) {
            throw new AssertionError("JDK-27 must expose textRequiresLayout", e);
        }
        check(trl.getReturnType() == boolean.class, "textRequiresLayout returns boolean");
        check((Boolean) trl.invoke(null, new Object[]{"ab".toCharArray(), 0, 2}) == Boolean.FALSE,
                "simple latin text needs no layout");
        check((Boolean) trl.invoke(null, new Object[]{new char[0], 0, 0}) == Boolean.FALSE,
                "empty range needs no layout");
        String arabic = "أبج";
        check((Boolean) trl.invoke(null, new Object[]{arabic.toCharArray(), 0, arabic.length()}) == Boolean.TRUE,
                "arabic text requires layout");

        // Serialization round-trip.
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(a);
        oos.close();
        Font back = (Font) new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back.equals(a), "round-trip equals");
        check(back.getFamily().equals("Dialog"), "round-trip family");
        check(back.getSize() == 12 && back.isPlain(), "round-trip size/style");
        check(back.canDisplay('a'), "round-trip displayability");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
