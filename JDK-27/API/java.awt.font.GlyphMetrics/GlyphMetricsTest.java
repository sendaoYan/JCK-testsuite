/*
 * @test
 * @summary java.awt.font.GlyphMetrics conformance: white-box structure plus black-box behavior.
 * @run main GlyphMetricsTest
 */

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.*;

public class GlyphMetricsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n, Class<?>... p) {
        try { c.getMethod(n, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.font.GlyphMetrics");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) && Modifier.isFinal(m), "public final class");
        for (String s : new String[]{"STANDARD","LIGATURE","COMBINING","COMPONENT","WHITESPACE"}) {
            Field f = c.getField(s);
            check(Modifier.isPublic(f.getModifiers()) && Modifier.isStatic(f.getModifiers())
                  && f.getType() == byte.class, "constant " + s + " byte");
        }
        check(hasMethod(c, "getAdvance"), "getAdvance()");
        check(hasMethod(c, "getAdvanceX"), "getAdvanceX()");
        check(hasMethod(c, "getAdvanceY"), "getAdvanceY()");
        check(hasMethod(c, "getBounds2D"), "getBounds2D()");
        check(hasMethod(c, "getType"), "getType()");
        check(hasMethod(c, "isStandard"), "isStandard()");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(GlyphMetrics.STANDARD == 0 && GlyphMetrics.LIGATURE == 1 && GlyphMetrics.COMBINING == 2
              && GlyphMetrics.COMPONENT == 3 && GlyphMetrics.WHITESPACE == 4, "type constant values");

        Font f = new Font(Font.DIALOG, Font.PLAIN, 12);
        FontRenderContext frc = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics().getFontRenderContext();
        GlyphVector gv = f.createGlyphVector(frc, "H");
        GlyphMetrics gm = gv.getGlyphMetrics(0);
        check(gm.getAdvance() > 0, "advance positive");
        check(gm.getAdvanceX() > 0 && gm.getAdvanceY() == 0f, "advanceX positive, advanceY 0");
        check(gm.getBounds2D() != null, "bounds2D non-null");
        check(gm.getType() == GlyphMetrics.STANDARD, "H is STANDARD");
        check(gm.isStandard(), "isStandard true");
        check(!gm.isLigature() && !gm.isCombining() && !gm.isComponent() && !gm.isWhitespace(),
              "other type predicates false");

        // space glyph type is font-dependent; assert it is a valid type constant
        GlyphVector spaceGv = f.createGlyphVector(frc, " ");
        GlyphMetrics spaceGm = spaceGv.getGlyphMetrics(0);
        check(spaceGm.getAdvance() >= 0, "space glyph advance non-negative");
        check(spaceGm.getType() >= GlyphMetrics.STANDARD && spaceGm.getType() <= GlyphMetrics.WHITESPACE,
              "space glyph valid type");

        // whitespace type via manual constructor
        GlyphMetrics ws = new GlyphMetrics(8.0f, new Rectangle2D.Float(0, 0, 8, 10), GlyphMetrics.WHITESPACE);
        check(ws.isWhitespace() && ws.getType() == GlyphMetrics.WHITESPACE, "manual WHITESPACE metric");

        // manual constructor
        Rectangle2D bounds = new Rectangle2D.Float(0, 0, 8, 10);
        GlyphMetrics m = new GlyphMetrics(8.0f, bounds, GlyphMetrics.STANDARD);
        check(m.getAdvance() == 8.0f && m.getBounds2D().equals(bounds) && m.isStandard(),
              "manual constructor round-trip");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
