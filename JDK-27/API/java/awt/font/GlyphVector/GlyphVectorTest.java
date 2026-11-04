/*
 * @test
 * @summary java.awt.font.GlyphVector conformance: data attributes of a real vector
 *          (headless-safe, no rasterization): identity wiring (font/FRC), glyph count
 *          and codes, char-index mapping, positional model (2n+2 floats, boundary
 *          positions), bounds/outline objects, metrics and justification null-ness,
 *          mutability (setGlyphPosition/Transform), performDefaultLayout, equals,
 *          out-of-range exception matrix, constructor visibility (public on JDK-8).
 * @run main GlyphVectorTest
 */

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

public class GlyphVectorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Exception; }

    static void checkThrows(Class<? extends Throwable> ex, String m, ThrowingRunnable r) {
        try { r.run(); } catch (Throwable t) {
            if (ex.isInstance(t)) { passed++; return; }
            throw new AssertionError(m + ": expected " + ex.getName() + " but got " + t.getClass().getName() + ": " + t.getMessage());
        }
        throw new AssertionError(m + ": expected " + ex.getName() + " but no exception");
    }

    public static void main(String[] args) throws Exception {
        Font f = new Font("Dialog", Font.PLAIN, 12);
        FontRenderContext frc = new FontRenderContext(new AffineTransform(), true, true);
        GlyphVector gv = f.createGlyphVector(frc, "abc");
        final int n = gv.getNumGlyphs();

        check(n == 3, "numGlyphs matches text length");
        check(gv.getFont() == f, "getFont is the creating font");
        check(gv.getFontRenderContext() == frc, "getFontRenderContext is the creating context");

        // Glyph codes: one per character, stable within a run.
        int[] codes = gv.getGlyphCodes(0, n, new int[n]);
        check(codes.length == n, "glyphCodes length");
        check(codes[1] == codes[0] + 1 && codes[2] == codes[1] + 1,
                "sequential text maps to consecutive glyph codes: " + java.util.Arrays.toString(codes));
        check(gv.getGlyphCode(0) == codes[0], "getGlyphCode(i) matches batch array");
        check(java.util.Arrays.toString(gv.getGlyphCodes(0, 0, new int[0])).equals("[]"),
                "empty glyph-code range gives empty array");

        // Character mapping is the identity for simple LTR text.
        int[] cis = gv.getGlyphCharIndices(0, n, new int[n]);
        check(java.util.Arrays.toString(cis).equals("[0, 1, 2]"), "glyphCharIndices identity: "
                + java.util.Arrays.toString(cis));
        check(gv.getGlyphCharIndex(0) == 0 && gv.getGlyphCharIndex(n - 1) == 2, "getGlyphCharIndex");
        check(gv.getGlyphCharIndex(gv.getGlyphCode(1) == codes[1] ? 1 : 1) == 1, "index lookup consistent");

        // Positional model: n+1 positions, x monotone, y zero on a baseline.
        float[] pos = gv.getGlyphPositions(0, n + 1, new float[2 * (n + 1)]);
        check(pos.length == 2 * (n + 1), "positions array holds 2*(n+1) floats");
        check(pos[0] == 0f && pos[1] == 0f, "first glyph at origin");
        check(pos[3] == 0f && pos[5] == 0f && pos[7] == 0f, "baseline has zero y");
        check(pos[2] > 0f && pos[4] > pos[2] && pos[6] > pos[4], "advances accumulate monotonically");
        Point2D p0 = gv.getGlyphPosition(0);
        Point2D pEnd = gv.getGlyphPosition(n);
        check(p0.getX() == 0.0 && p0.getY() == 0.0, "getGlyphPosition(0)");
        check(Math.abs(pEnd.getX() - pos[6]) < 1e-6 && pEnd.getY() == 0.0,
                "terminal position equals total advance");

        // Bounds and outlines are concrete geometry objects (no pixels involved).
        check(gv.getLogicalBounds() != null && !gv.getLogicalBounds().isEmpty(), "logical bounds non-empty");
        check(gv.getVisualBounds() != null, "visual bounds non-null");
        check(gv.getOutline() != null, "outline non-null");
        check(gv.getGlyphOutline(0) != null, "glyph outline non-null");
        check(gv.getGlyphLogicalBounds(0) != null, "glyph logical bounds non-null");
        check(gv.getGlyphVisualBounds(0) != null, "glyph visual bounds non-null");
        check(gv.getGlyphMetrics(0) != null, "glyph metrics non-null");
        check(gv.getPixelBounds(frc, 0f, 0f) != null, "pixel bounds non-null");

        // Simple LTR text: no justification info, no per-glyph transform, no flags.
        check(gv.getGlyphJustificationInfo(0) == null, "justification info null for simple text");
        check(gv.getGlyphTransform(0) == null, "glyph transform null by default");
        check(gv.getLayoutFlags() == 0, "layoutFlags 0 for plain LTR text");

        // Equality: same construction yields equal vectors.
        check(gv.equals(gv), "equals self");
        check(!gv.equals(null), "equals null false");
        check(gv.equals(f.createGlyphVector(frc, "abc")), "equal vectors from equal input");
        check(!gv.equals(f.createGlyphVector(frc, "abd")), "different text not equal");
        check(gv.hashCode() == gv.hashCode(), "hashCode stable");

        // Mutability.
        gv.setGlyphPosition(0, new Point2D.Float(7f, 8f));
        check(gv.getGlyphPosition(0).getX() == 7.0 && gv.getGlyphPosition(0).getY() == 8.0,
                "setGlyphPosition read-back");
        gv.setGlyphTransform(0, new AffineTransform(1, 0, 0, 1, 2, 3));
        check(gv.getGlyphTransform(0) != null, "setGlyphTransform read-back");
        gv.setGlyphTransform(0, null);
        check(gv.getGlyphTransform(0) == null, "null transform clears it");
        int before = gv.getNumGlyphs();
        gv.performDefaultLayout();
        check(gv.getNumGlyphs() == before, "performDefaultLayout keeps glyph count");
        check(gv.getGlyphPosition(0).getX() == 0.0, "performDefaultLayout restores default positions");

        // Empty text: zero glyphs, degenerate bounds.
        GlyphVector empty = f.createGlyphVector(frc, "");
        check(empty.getNumGlyphs() == 0, "empty text has zero glyphs");
        check(empty.getGlyphPosition(0).equals(new Point2D.Float(0f, 0f)), "empty vector still has position 0");

        // Out-of-range matrix (indices doubled for positions).
        checkThrows(ArrayIndexOutOfBoundsException.class, "getGlyphCode(n)",
                new ThrowingRunnable() { public void run() { gv.getGlyphCode(n); } });
        checkThrows(ArrayIndexOutOfBoundsException.class, "getGlyphCode(-1)",
                new ThrowingRunnable() { public void run() { gv.getGlyphCode(-1); } });
        checkThrows(ArrayIndexOutOfBoundsException.class, "getGlyphPosition(99)",
                new ThrowingRunnable() { public void run() { gv.getGlyphPosition(99); } });
        // On JDK-9+ this surfaces as a plain IndexOutOfBoundsException (JDK-8 threw AIOOBE).
        checkThrows(IndexOutOfBoundsException.class, "setGlyphPosition(99, p)",
                new ThrowingRunnable() { public void run() { gv.setGlyphPosition(99, new Point2D.Float(1, 2)); } });
        // getGlyphCharIndex is an unchecked identity mapping: out-of-range arguments
        // are returned verbatim instead of throwing.
        check(gv.getGlyphCharIndex(9) == 9, "getGlyphCharIndex oob is identity");
        check(gv.getGlyphCharIndex(-1) == -1, "getGlyphCharIndex negative is identity");
        // Batch/per-glyph accessors do validate their ranges.
        checkThrows(IndexOutOfBoundsException.class, "getGlyphCharIndices range too big",
                new ThrowingRunnable() { public void run() { gv.getGlyphCharIndices(0, 9, new int[9]); } });
        checkThrows(IndexOutOfBoundsException.class, "getGlyphCodes range too big",
                new ThrowingRunnable() { public void run() { gv.getGlyphCodes(0, 9, new int[9]); } });
        checkThrows(IndexOutOfBoundsException.class, "getGlyphPositions range too big",
                new ThrowingRunnable() { public void run() { gv.getGlyphPositions(0, 9, new float[18]); } });
        checkThrows(IndexOutOfBoundsException.class, "getGlyphMetrics(n)",
                new ThrowingRunnable() { public void run() { gv.getGlyphMetrics(n); } });
        checkThrows(IndexOutOfBoundsException.class, "getGlyphJustificationInfo(n)",
                new ThrowingRunnable() { public void run() { gv.getGlyphJustificationInfo(n); } });
        checkThrows(IndexOutOfBoundsException.class, "getGlyphLogicalBounds(n)",
                new ThrowingRunnable() { public void run() { gv.getGlyphLogicalBounds(n); } });
        checkThrows(IndexOutOfBoundsException.class, "getGlyphTransform(n)",
                new ThrowingRunnable() { public void run() { gv.getGlyphTransform(n); } });
        checkThrows(IndexOutOfBoundsException.class, "setGlyphTransform(n)",
                new ThrowingRunnable() { public void run() { gv.setGlyphTransform(n, new AffineTransform()); } });
        checkThrows(ArrayIndexOutOfBoundsException.class, "getGlyphOutline(n)",
                new ThrowingRunnable() { public void run() { gv.getGlyphOutline(n); } });

        // GlyphVector is Cloneable; its no-arg constructor is protected since JDK-9
        // (JDK-8 still had it public).
        Constructor<?> ctor = GlyphVector.class.getDeclaredConstructor();
        check(Modifier.isProtected(ctor.getModifiers()), "JDK-27: protected no-arg constructor");
        check(GlyphVector.class.getInterfaces()[0] == Cloneable.class, "implements Cloneable");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
