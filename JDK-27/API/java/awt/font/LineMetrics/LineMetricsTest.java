/*
 * @test
 * @summary java.awt.font.LineMetrics conformance: white-box structure plus black-box behavior.
 * @run main LineMetricsTest
 */

import java.awt.*;
import java.awt.font.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.*;

public class LineMetricsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n, Class<?>... p) {
        try { c.getMethod(n, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.font.LineMetrics");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) && Modifier.isAbstract(m), "abstract public class");
        check(!c.isInterface() && !c.isEnum(), "class, not interface/enum");
        check(hasMethod(c, "getNumChars"), "getNumChars()");
        check(hasMethod(c, "getAscent"), "getAscent()");
        check(hasMethod(c, "getDescent"), "getDescent()");
        check(hasMethod(c, "getLeading"), "getLeading()");
        check(hasMethod(c, "getHeight"), "getHeight()");
        check(hasMethod(c, "getBaselineIndex"), "getBaselineIndex()");
        check(hasMethod(c, "getUnderlineOffset"), "getUnderlineOffset()");
        check(hasMethod(c, "getUnderlineThickness"), "getUnderlineThickness()");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Font f = new Font(Font.DIALOG, Font.PLAIN, 12);
        FontRenderContext frc = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics().getFontRenderContext();
        LineMetrics lm = f.getLineMetrics("Hello", frc);
        check(lm.getNumChars() == 5, "numChars 5");
        check(lm.getAscent() > 0, "ascent positive");
        check(lm.getDescent() >= 0, "descent non-negative");
        check(lm.getLeading() >= 0, "leading non-negative");
        check(lm.getHeight() >= lm.getAscent() + lm.getDescent(), "height >= ascent+descent");
        check(lm.getBaselineIndex() == Font.ROMAN_BASELINE, "baseline index ROMAN");
        float[] offsets = lm.getBaselineOffsets();
        check(offsets != null && offsets.length >= 3, "baseline offsets length >= 3");
        check(lm.getUnderlineThickness() > 0, "underline thickness positive");
        check(lm.getStrikethroughThickness() > 0, "strikethrough thickness positive");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
