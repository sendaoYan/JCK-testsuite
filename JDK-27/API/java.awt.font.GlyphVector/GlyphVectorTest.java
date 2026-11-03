/*
 * @test
 * @summary java.awt.font.GlyphVector conformance: white-box structure plus black-box behavior.
 * @run main GlyphVectorTest
 */

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.*;

public class GlyphVectorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n, Class<?>... p) {
        try { c.getMethod(n, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.font.GlyphVector");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) && Modifier.isAbstract(m), "abstract public class");
        check(!c.isInterface() && !c.isEnum(), "class, not interface/enum");
        check(Cloneable.class.isAssignableFrom(c), "implements Cloneable");
        check(hasMethod(c, "getFont"), "getFont()");
        check(hasMethod(c, "getFontRenderContext"), "getFontRenderContext()");
        check(hasMethod(c, "getNumGlyphs"), "getNumGlyphs()");
        check(hasMethod(c, "getGlyphCode", int.class), "getGlyphCode(int)");
        check(hasMethod(c, "getLogicalBounds"), "getLogicalBounds()");
        check(hasMethod(c, "getVisualBounds"), "getVisualBounds()");
        check(hasMethod(c, "getGlyphMetrics", int.class), "getGlyphMetrics(int)");
        check(hasMethod(c, "getGlyphPosition", int.class), "getGlyphPosition(int)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Font f = new Font(Font.DIALOG, Font.PLAIN, 12);
        FontRenderContext frc = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics().getFontRenderContext();
        GlyphVector gv = f.createGlyphVector(frc, "Hello");
        check(gv.getFont().equals(f), "getFont returns font");
        check(gv.getFontRenderContext() != null, "getFontRenderContext non-null");
        check(gv.getNumGlyphs() == 5, "5 glyphs for Hello");

        int[] codes = gv.getGlyphCodes(0, 5, null);
        check(codes != null && codes.length == 5, "getGlyphCodes returns 5 codes");
        check(gv.getGlyphCode(0) == codes[0], "getGlyphCode(0) matches array[0]");

        Rectangle2D logical = gv.getLogicalBounds();
        check(logical.getWidth() > 0, "logical bounds width positive");
        check(gv.getVisualBounds() != null, "visual bounds non-null");
        check(gv.getOutline() != null, "getOutline non-null");
        check(gv.getOutline(1, 2) != null, "getOutline(x,y) non-null");
        check(gv.getGlyphOutline(0) != null, "getGlyphOutline(0) non-null");

        Point2D pos0 = gv.getGlyphPosition(0);
        check(pos0 != null, "getGlyphPosition non-null");
        GlyphMetrics gm = gv.getGlyphMetrics(0);
        check(gm.getAdvance() > 0, "glyph advance positive");

        // getLayoutFlags is a valid int (default 0 for simple text)
        check(gv.getLayoutFlags() >= 0, "getLayoutFlags non-negative");
        check(gv.getGlyphCharIndex(0) == 0, "getGlyphCharIndex(0) == 0");

        // default layout + equality
        GlyphVector gv2 = f.createGlyphVector(frc, "Hello");
        check(gv.equals(gv2), "equal glyph vectors equal");
        check(gv.getPixelBounds(frc, 0, 0) != null, "getPixelBounds non-null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
