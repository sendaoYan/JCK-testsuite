/*
 * @test
 * @summary java.awt.font.TextLayout conformance: white-box structure plus black-box behavior.
 * @run main TextLayoutTest
 */

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.*;

public class TextLayoutTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n, Class<?>... p) {
        try { c.getMethod(n, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.font.TextLayout");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) && Modifier.isFinal(m), "public final class");
        check(Cloneable.class.isAssignableFrom(c), "implements Cloneable");
        check(hasMethod(c, "getAdvance"), "getAdvance()");
        check(hasMethod(c, "getAscent"), "getAscent()");
        check(hasMethod(c, "getDescent"), "getDescent()");
        check(hasMethod(c, "getBounds"), "getBounds()");
        check(hasMethod(c, "getCharacterCount"), "getCharacterCount()");
        check(hasMethod(c, "hitTestChar", float.class, float.class), "hitTestChar");
        check(hasMethod(c, "getCaretShape", TextHitInfo.class), "getCaretShape(TextHitInfo)");
        check(hasMethod(c, "getOutline", AffineTransform.class), "getOutline(AffineTransform)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Font f = new Font(Font.DIALOG, Font.PLAIN, 12);
        FontRenderContext frc = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics().getFontRenderContext();
        TextLayout tl = new TextLayout("Hello World", f, frc);

        check(tl.getCharacterCount() == 11, "character count 11");
        check(tl.getAdvance() > 0, "advance positive");
        check(tl.getVisibleAdvance() > 0, "visible advance positive");
        check(tl.getAscent() > 0, "ascent positive");
        check(tl.getDescent() >= 0, "descent non-negative");
        check(tl.getLeading() >= 0, "leading non-negative");
        check(tl.getBounds() != null && tl.getBounds().getWidth() > 0, "bounds width positive");
        check(tl.isLeftToRight(), "LTR text isLeftToRight");
        check(!tl.isVertical(), "horizontal text not vertical");
        check(tl.getBaseline() == Font.ROMAN_BASELINE, "ROMAN baseline");
        float[] bo = tl.getBaselineOffsets();
        check(bo != null && bo.length >= 3, "baseline offsets length >= 3");

        check(tl.getOutline(null) != null, "getOutline non-null");
        check(tl.getPixelBounds(frc, 0, 0) != null, "getPixelBounds non-null");

        // hit testing / caret
        TextHitInfo hit = tl.hitTestChar(tl.getAdvance() / 2, 0);
        check(hit != null, "hitTestChar non-null");
        check(tl.getCaretShape(hit) != null, "getCaretShape non-null");
        float[] caret = tl.getCaretInfo(hit);
        check(caret != null && caret.length >= 2, "getCaretInfo length >= 2");
        TextHitInfo next = tl.getNextRightHit(hit);
        check(next != null, "getNextRightHit non-null");

        // logical highlight / black box bounds
        check(tl.getLogicalHighlightShape(0, 5) != null, "getLogicalHighlightShape non-null");
        check(tl.getBlackBoxBounds(0, 5) != null, "getBlackBoxBounds non-null");

        // justified layout returns distinct TextLayout
        TextLayout justified = tl.getJustifiedLayout(200);
        check(justified != null, "getJustifiedLayout non-null");

        check(tl.equals(tl), "self equals");
        check(!tl.equals(new TextLayout("Hello World", f, frc)), "distinct layouts not equal (identity)");
        check(tl.toString() != null, "toString non-null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
