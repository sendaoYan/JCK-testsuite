/*
 * @test
 * @summary java.awt.print.PageFormat conformance: page description (headless-safe):
 *          orientation constants and per-orientation geometry, transform matrices,
 *          paper copy semantics, setOrientation validation, clone, defaults.
 * @run main PageFormatTest
 */

import java.awt.print.PageFormat;
import java.awt.print.Paper;

public class PageFormatTest {
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

    static String matrix(PageFormat f) {
        double[] m = f.getMatrix();
        StringBuilder sb = new StringBuilder("[");
        for (double v : m) sb.append(v).append(",");
        return sb.append("]").toString();
    }

    public static void main(String[] args) {
        // Constants.
        check(PageFormat.LANDSCAPE == 0, "LANDSCAPE == 0");
        check(PageFormat.PORTRAIT == 1, "PORTRAIT == 1");
        check(PageFormat.REVERSE_LANDSCAPE == 2, "REVERSE_LANDSCAPE == 2");

        // Defaults: portrait letter.
        PageFormat f = new PageFormat();
        check(f.getOrientation() == PageFormat.PORTRAIT, "default orientation portrait");
        check(f.getWidth() == 612.0 && f.getHeight() == 792.0, "default extents letter");
        check(f.getImageableX() == 72.0 && f.getImageableY() == 72.0, "default imageable origin");
        check(f.getImageableWidth() == 468.0 && f.getImageableHeight() == 648.0, "default imageable extent");

        // Portrait matrix is the identity.
        check(matrix(f).equals("[1.0,0.0,0.0,1.0,0.0,0.0,]"), "portrait matrix identity: " + matrix(f));

        // Landscape swaps extents and keeps imageable origin in page coordinates.
        f.setOrientation(PageFormat.LANDSCAPE);
        check(f.getOrientation() == PageFormat.LANDSCAPE, "orientation round-trip");
        check(f.getWidth() == 792.0 && f.getHeight() == 612.0, "landscape swaps extents");
        check(f.getImageableX() == 72.0 && f.getImageableY() == 72.0, "landscape imageable origin");
        check(f.getImageableWidth() == 648.0 && f.getImageableHeight() == 468.0, "landscape swaps imageable extents");
        check(matrix(f).equals("[0.0,-1.0,1.0,0.0,0.0,792.0,]"), "landscape matrix: " + matrix(f));

        // Reverse landscape mirrors the other way.
        f.setOrientation(PageFormat.REVERSE_LANDSCAPE);
        check(f.getWidth() == 792.0 && f.getHeight() == 612.0, "reverse landscape extents");
        check(matrix(f).equals("[0.0,1.0,-1.0,0.0,612.0,0.0,]"), "reverse landscape matrix: " + matrix(f));

        // Matrix is a fresh array each time.
        double[] m1 = f.getMatrix();
        double[] m2 = f.getMatrix();
        check(m1 != m2, "getMatrix returns a fresh copy");
        m1[0] = 99;
        check(f.getMatrix()[0] != 99.0, "mutating returned matrix is harmless");

        // Only the three constants are accepted.
        checkThrows(IllegalArgumentException.class, "setOrientation(5)",
                new ThrowingRunnable() { public void run() { new PageFormat().setOrientation(5); } });
        checkThrows(IllegalArgumentException.class, "setOrientation(-1)",
                new ThrowingRunnable() { public void run() { new PageFormat().setOrientation(-1); } });

        // Paper wiring: setPaper clones the argument, getPaper clones the field.
        PageFormat g = new PageFormat();
        Paper shared = new Paper();
        shared.setSize(300, 400);
        shared.setImageableArea(5, 6, 7, 8);
        g.setPaper(shared);
        check(g.getPaper().getWidth() == 300.0, "paper stored");
        shared.setSize(999, 999);
        check(g.getPaper().getWidth() == 300.0, "setPaper clones its argument");
        Paper got = g.getPaper();
        check(got != g.getPaper(), "getPaper returns a fresh copy each call");
        got.setSize(1, 1);
        check(g.getPaper().getWidth() == 300.0, "mutating a returned paper is harmless");
        // Geometry follows the paper through the portrait orientation.
        check(g.getWidth() == 300.0 && g.getHeight() == 400.0, "extents follow paper");
        check(g.getImageableX() == 5.0 && g.getImageableWidth() == 7.0, "imageable follows paper");
        // setPaper(null) rejects null.
        try {
            g.setPaper(null);
            check(false, "setPaper(null) should reject");
        } catch (NullPointerException expected) {
            check(true, "setPaper(null) throws NPE");
        }

        // clone(): same values, independent object (equals is identity-based).
        PageFormat orig = new PageFormat();
        orig.setPaper(shared);
        PageFormat cl = (PageFormat) orig.clone();
        check(cl.getClass() == PageFormat.class, "clone is a PageFormat");
        check(cl.getOrientation() == orig.getOrientation(), "clone orientation");
        check(cl.getWidth() == orig.getWidth() && cl.getHeight() == orig.getHeight(), "clone extents");
        check(!cl.equals(orig), "PageFormat equals is identity-based");
        check(!orig.equals(null), "equals(null) false");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
