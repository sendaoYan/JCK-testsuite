/*
 * @test
 * @summary java.awt.FontMetrics conformance: white-box structure + black-box behavior.
 * @run main FontMetricsTest
 */

import java.lang.reflect.*;

public class FontMetricsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.FontMetrics", false, FontMetricsTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.FontMetrics");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public class");
        check(Modifier.isAbstract(m), "abstract class");
        check(!c.isInterface(), "not interface");
        check(c.getEnumConstants() == null, "non-enum");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");

        check(c.getMethod("getFont").getReturnType() == java.awt.Font.class, "getFont returns Font");
        check(c.getMethod("getFontRenderContext").getReturnType() == java.awt.font.FontRenderContext.class,
              "getFontRenderContext returns FontRenderContext");
        for (String name : new String[] {"getLeading", "getAscent", "getDescent", "getHeight",
                "getMaxAscent", "getMaxDescent", "getMaxDecent", "getMaxAdvance",
                "charWidth", "stringWidth", "charsWidth", "bytesWidth", "getWidths",
                "hasUniformLineMetrics", "getLineMetrics", "getStringBounds", "getMaxCharBounds",
                "toString"})
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(mm -> mm.getName().equals(name)), "method present: " + name);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        java.awt.image.BufferedImage img =
            new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics g = img.createGraphics();

        java.awt.Font f24 = new java.awt.Font("Dialog", java.awt.Font.PLAIN, 24);
        java.awt.Font f12 = new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12);
        java.awt.FontMetrics fm = g.getFontMetrics(f24);
        java.awt.FontMetrics fm12 = g.getFontMetrics(f12);

        // getFont / font render context
        check(fm.getFont().equals(f24), "getFont returns the font");
        check(fm.getFontRenderContext() != null, "getFontRenderContext non-null");

        // metrics relationships
        check(fm.getAscent() >= 0, "ascent non-negative");
        check(fm.getDescent() >= 0, "descent non-negative");
        check(fm.getLeading() >= 0, "leading non-negative");
        check(fm.getHeight() == fm.getLeading() + fm.getAscent() + fm.getDescent(),
              "height == leading+ascent+descent");
        check(fm.getHeight() >= fm.getAscent() + fm.getDescent(), "height >= ascent+descent");
        check(fm.getMaxAscent() >= fm.getAscent(), "maxAscent >= ascent");
        check(fm.getMaxDescent() >= fm.getDescent(), "maxDescent >= descent");
        check(fm.getMaxAdvance() > 0, "maxAdvance positive");

        // string / char widths (relationships, not exact pixels)
        check(fm.stringWidth("MMM") > fm.stringWidth("M"), "stringWidth MMM > M");
        check(fm.stringWidth("MMM") > fm.stringWidth("I"), "stringWidth MMM > I");
        check(fm.stringWidth("MMM") > fm12.stringWidth("MMM"), "size 24 width > size 12 width");
        check(fm.charWidth('M') == fm.stringWidth("M"), "charWidth('M') == stringWidth('M')");
        check(fm.charWidth('A') == fm.charWidth((int) 'A'), "charWidth(char) == charWidth(int)");

        // charsWidth / bytesWidth consistency
        check(fm.charsWidth(new char[] {'A', 'B', 'C'}, 0, 3) == fm.stringWidth("ABC"),
              "charsWidth equals stringWidth");
        check(fm.bytesWidth(new byte[] {'A', 'B'}, 0, 2) == fm.stringWidth("AB"),
              "bytesWidth latin-1 equals stringWidth");

        // getWidths
        int[] widths = fm.getWidths();
        check(widths.length == 256, "getWidths length 256");
        check(widths[0] >= 0, "getWidths entries non-negative");

        // line metrics via Graphics
        java.awt.font.LineMetrics lm = fm.getLineMetrics("Hello", g);
        check(lm.getAscent() >= 0 && lm.getDescent() >= 0, "getLineMetrics non-negative");
        check(lm.getHeight() >= lm.getAscent() + lm.getDescent(), "lineMetrics height >= ascent+descent");

        // string bounds via Graphics
        java.awt.geom.Rectangle2D b = fm.getStringBounds("Hello", g);
        check(b.getWidth() > 0, "getStringBounds positive width");
        java.awt.geom.Rectangle2D bm = fm.getMaxCharBounds(g);
        check(bm.getWidth() > 0, "getMaxCharBounds positive width");

        // hasUniformLineMetrics callable
        fm.hasUniformLineMetrics();
        check(true, "hasUniformLineMetrics callable");

        // toString
        String ts = fm.toString();
        check(ts != null && ts.contains("ascent=") && ts.contains("height="), "toString format");

        g.dispose();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
