/*
 * @test
 * @summary java.awt.FontMetrics conformance: abstract class reflection.
 * @run main FontMetricsTest
 */

public class FontMetricsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.awt.FontMetrics");
        check(!c.isInterface() && !c.isEnum(), "not interface/enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.io.Serializable"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getFont"), "method getFont");
        check(hasMethod(c, "getFontRenderContext"), "method getFontRenderContext");
        check(hasMethod(c, "getLeading"), "method getLeading");
        check(hasMethod(c, "getAscent"), "method getAscent");
        check(hasMethod(c, "getDescent"), "method getDescent");
        check(hasMethod(c, "getHeight"), "method getHeight");
        check(hasMethod(c, "getMaxAscent"), "method getMaxAscent");
        check(hasMethod(c, "getMaxDescent"), "method getMaxDescent");
        check(hasMethod(c, "getMaxDecent"), "method getMaxDecent");
        check(hasMethod(c, "getMaxAdvance"), "method getMaxAdvance");
        check(hasMethod(c, "charWidth"), "method charWidth");
        check(hasMethod(c, "stringWidth"), "method stringWidth");
        check(hasMethod(c, "charsWidth"), "method charsWidth");
        check(hasMethod(c, "bytesWidth"), "method bytesWidth");
        check(hasMethod(c, "getWidths"), "method getWidths");
        check(hasMethod(c, "hasUniformLineMetrics"), "method hasUniformLineMetrics");
        check(hasMethod(c, "getLineMetrics"), "method getLineMetrics");
        check(hasMethod(c, "getStringBounds"), "method getStringBounds");
        check(hasMethod(c, "getMaxCharBounds"), "method getMaxCharBounds");
        check(hasMethod(c, "toString"), "method toString");
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 3: contract deepening (headless via BufferedImage font metrics) ----
    static void testContract() {
        java.awt.image.BufferedImage bi = new java.awt.image.BufferedImage(10, 10, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = bi.createGraphics();
        java.awt.Font f = new java.awt.Font(java.awt.Font.DIALOG, java.awt.Font.PLAIN, 12);
        java.awt.FontMetrics fm = g.getFontMetrics(f);
        check(fm != null, "getFontMetrics(Font) headless");
        check(fm.getFont().equals(f), "getFont returns the font");
        check(fm.getAscent() > 0, "ascent positive");
        check(fm.getDescent() >= 0, "descent non-negative");
        check(fm.getLeading() >= 0, "leading non-negative");
        check(fm.getHeight() == fm.getAscent() + fm.getDescent() + fm.getLeading(),
              "height == ascent + descent + leading");
        check(fm.getMaxAscent() >= fm.getAscent(), "maxAscent >= ascent");
        check(fm.getMaxDescent() >= fm.getDescent(), "maxDescent >= descent");
        check(fm.getHeight() > 0, "height positive");
        // charWidth consistency
        check(fm.charWidth('W') >= fm.charWidth('i'), "W wider than i");
        check(fm.charWidth(' ') > 0, "space has width");
        check(fm.charWidth('中') >= 0, "cjk char width callable");
        // stringWidth == sum of charWidth for every char
        String probe = "Hello, World! 0123 .ab|~";
        int sum = 0;
        for (int i = 0; i < probe.length(); i++) sum += fm.charWidth(probe.charAt(i));
        check(fm.stringWidth(probe) == sum, "stringWidth == sum(charWidth)");
        check(fm.stringWidth("") == 0, "empty string zero width");
        check(fm.stringWidth(probe) == fm.charsWidth(probe.toCharArray(), 0, probe.length()),
              "charsWidth == stringWidth");
        check(fm.stringWidth(probe.substring(0, 5)) ==
              fm.charsWidth(probe.toCharArray(), 0, 5), "charsWidth partial");
        // bytesWidth uses platform charset bytes: consistency with stringWidth for ASCII
        String ascii = "ascii only";
        check(fm.bytesWidth(ascii.getBytes(), 0, ascii.getBytes().length) == fm.stringWidth(ascii),
              "bytesWidth == stringWidth for ASCII");
        // substring widths are additive
        check(fm.stringWidth("ab") + fm.stringWidth("cd") == fm.stringWidth("abcd"),
              "widths additive for split strings");
        // max advance: -1 allowed if unknown; with real font expected positive
        check(fm.getMaxAdvance() > 0 || fm.getMaxAdvance() == -1, "getMaxAdvance valid");
        // different font sizes scale metrics
        java.awt.Font big = f.deriveFont(24f);
        java.awt.FontMetrics fmb = g.getFontMetrics(big);
        check(fmb.getHeight() > fm.getHeight(), "larger font larger height");
        // null probes throw NPE
        boolean npe = false;
        try { fm.stringWidth(null); } catch (java.lang.NullPointerException e) { npe = true; }
        check(npe, "stringWidth(null) -> NPE");
        npe = false;
        try { fm.charsWidth(null, 0, 1); } catch (java.lang.NullPointerException e) { npe = true; }
        check(npe, "charsWidth(null,...) -> NPE");
        npe = false;
        try { fm.bytesWidth(null, 0, 1); } catch (java.lang.NullPointerException e) { npe = true; }
        check(npe, "bytesWidth(null,...) -> NPE");
        // out-of-range offsets: IndexOutOfBoundsException
        boolean oob = false;
        try { fm.charsWidth(new char[]{'a'}, 0, 5); } catch (java.lang.IndexOutOfBoundsException e) { oob = true; }
        check(oob, "charsWidth offset+len beyond array -> IndexOutOfBounds");
        // charWidth(int) accepts code point
        check(fm.charWidth((int) 'A') == fm.charWidth('A'), "charWidth(int) == charWidth(char)");
        g.dispose();
    }

}
