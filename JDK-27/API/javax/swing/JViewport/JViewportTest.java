/*
 * @test
 * @summary javax.swing.JViewport conformance: white-box structure + black-box behavior
 *          + view wiring, no-clamp positions, UI delegate (headless-safe, Metal LaF).
 * @run main JViewportTest
 */

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.lang.reflect.Modifier;
import javax.swing.JLabel;
import javax.swing.JViewport;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class JViewportTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Exception; }

    static void runOnEDT(ThrowingRunnable r) throws Exception {
        try {
            EventQueue.invokeAndWait(() -> {
                try { r.run(); }
                catch (Exception e) { throw new RuntimeException(e); }
            });
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable t = e.getCause();
            if (t instanceof RuntimeException) throw (RuntimeException) t;
            if (t instanceof Error) throw (Error) t;
            throw new RuntimeException(t);
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.JViewport");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.accessibility.Accessible"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Empty viewport defaults.
        JViewport vp = new JViewport();
        check(vp.getView() == null, "default view null");
        check(new Dimension(0, 0).equals(vp.getViewSize()), "default viewSize 0,0");
        check(new Point(0, 0).equals(vp.getViewPosition()), "default viewPosition 0,0");
        check(new Dimension(0, 0).equals(vp.getExtentSize()), "default extentSize 0,0");
        check(vp.getScrollMode() == JViewport.BLIT_SCROLL_MODE, "default scroll mode BLIT");
        check(!vp.isBackingStoreEnabled(), "default backing store disabled");
        check(!vp.isOptimizedDrawingEnabled(), "isOptimizedDrawingEnabled false");
        check("ViewportUI".equals(vp.getUIClassID()), "getUIClassID ViewportUI");

        // Extent size roundtrip.
        vp.setExtentSize(new Dimension(200, 100));
        check(new Dimension(200, 100).equals(vp.getExtentSize()), "setExtentSize/getExtentSize");

        // Scroll mode roundtrip.
        vp.setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        check(vp.getScrollMode() == JViewport.SIMPLE_SCROLL_MODE, "setScrollMode(SIMPLE)");
        vp.setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
        check(vp.getScrollMode() == JViewport.BACKINGSTORE_SCROLL_MODE, "setScrollMode(BACKINGSTORE)");
        check(vp.isBackingStoreEnabled(), "isBackingStoreEnabled after BACKINGSTORE");
        vp.setScrollMode(JViewport.BLIT_SCROLL_MODE);
        check(vp.getScrollMode() == JViewport.BLIT_SCROLL_MODE, "setScrollMode(BLIT)");

        // setBackingStoreEnabled maps to scroll modes.
        vp.setBackingStoreEnabled(true);
        check(vp.getScrollMode() == JViewport.BACKINGSTORE_SCROLL_MODE, "setBackingStoreEnabled(true) -> BACKINGSTORE");
        check(vp.isBackingStoreEnabled(), "isBackingStoreEnabled true");
        vp.setBackingStoreEnabled(false);
        check(vp.getScrollMode() == JViewport.BLIT_SCROLL_MODE, "setBackingStoreEnabled(false) -> BLIT");

        // View roundtrip.
        JLabel view = new JLabel("v");
        vp.setView(view);
        check(vp.getView() == view, "setView/getView");

        // View size roundtrip.
        vp.setViewSize(new Dimension(300, 150));
        check(new Dimension(300, 150).equals(vp.getViewSize()), "setViewSize/getViewSize");

        // View position roundtrip.
        vp.setViewPosition(new Point(10, 20));
        check(new Point(10, 20).equals(vp.getViewPosition()), "setViewPosition/getViewPosition");

        // View rect derives from view position + extent.
        Rectangle vr = vp.getViewRect();
        check(new Rectangle(10, 20, 200, 100).equals(vr), "getViewRect");

        // Coordinate conversion (identity for basic viewport).
        Point p = vp.toViewCoordinates(new Point(5, 6));
        check(new Point(5, 6).equals(p), "toViewCoordinates(Point)");
        check(p != null, "toViewCoordinates non-null");
        Dimension d = vp.toViewCoordinates(new Dimension(7, 8));
        check(new Dimension(7, 8).equals(d), "toViewCoordinates(Dimension)");

        // Border is not supported: null ok, non-null throws IAE.
        vp.setBorder(null);
        check(true, "setBorder(null) allowed");
        try {
            vp.setBorder(new LineBorder(java.awt.Color.BLACK, 1));
            check(false, "setBorder(non-null) should throw IAE");
        } catch (IllegalArgumentException expected) {
            check(true, "setBorder(non-null) IAE");
        }

        // Insets always zero.
        check(new Insets(0, 0, 0, 0).equals(vp.getInsets()), "getInsets zeros");

        // scrollRectToVisible is callable (no throw) with and without a view.
        JViewport empty = new JViewport();
        empty.scrollRectToVisible(new Rectangle(0, 0, 10, 10));
        check(true, "scrollRectToVisible empty viewport no-throw");
        vp.scrollRectToVisible(new Rectangle(0, 0, 50, 50));
        check(true, "scrollRectToVisible with view no-throw");

        // Removing the view.
        vp.setView(null);
        check(vp.getView() == null, "setView(null) clears view");
    }

    // ---- Part 3: view wiring, no-clamp positions, UI delegate ----
    static void testContractExtras() throws Exception {
        JViewport vp = new JViewport();
        check("BasicViewportUI".equals(vp.getUI().getClass().getSimpleName()), "Metal installs BasicViewportUI");
        check(vp.getBorder() == null, "default border null");
        check(new Rectangle(0, 0, 0, 0).equals(vp.getViewRect()), "default viewRect 0,0,0,0");

        JLabel view = new JLabel("v");
        vp.setView(view);
        check(view.getParent() == vp, "view parented by viewport");
        // Position is NOT clamped to the view size while undisplayed.
        vp.setViewPosition(new Point(500, 500));
        check(new Point(500, 500).equals(vp.getViewPosition()), "view position not clamped");
        // View rect derives from position + extent.
        vp.setExtentSize(new Dimension(120, 80));
        check(new Rectangle(500, 500, 120, 80).equals(vp.getViewRect()), "getViewRect position+extent");
    }

    public static void main(String[] args) throws Exception {
        try {
            UIManager.setLookAndFeel(new MetalLookAndFeel());
        } catch (Exception e) {
            throw new AssertionError("cannot install Metal LaF", e);
        }
        runOnEDT(JViewportTest::testWhiteBox);
        runOnEDT(JViewportTest::testBlackBox);
        runOnEDT(JViewportTest::testContractExtras);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
