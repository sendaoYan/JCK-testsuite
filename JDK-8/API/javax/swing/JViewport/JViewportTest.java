/*
 * @test
 * @summary javax.swing.JViewport conformance: type shape via reflection + viewport
 *          contract (defaults, view wiring/size/position, scroll modes, border
 *          rejection; headless-safe, Metal LaF, EDT).
 * @run main JViewportTest
 */

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JLabel;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class JViewportTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void checkThrows(Class<? extends Throwable> ex, Runnable r, String m) {
        try { r.run(); }
        catch (Throwable t) {
            if (!ex.isInstance(t)) throw new AssertionError(m + ": expected " + ex.getSimpleName() + " got " + t);
            passed++; return; }
        throw new AssertionError(m + ": expected " + ex.getSimpleName() + " not thrown");
    }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    // ---- Part 1: structural shape (kept from the original skeleton) ----
    static void testShape() throws Exception {
        Class<?> c = Class.forName("javax.swing.JViewport", false, JViewportTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent"), "superclass");
        check(hasInterface(c, "javax.accessibility.Accessible"), "implements javax.accessibility.Accessible");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getUI"), "method getUI");
        check(names.contains("setUI"), "method setUI");
        check(names.contains("updateUI"), "method updateUI");
        check(names.contains("getUIClassID"), "method getUIClassID");
        check(names.contains("remove"), "method remove");
        check(names.contains("scrollRectToVisible"), "method scrollRectToVisible");
        check(names.contains("setBorder"), "method setBorder");
    }

    // ---- Part 2: defaults and scroll modes ----
    static void testDefaults() {
        JViewport vp = new JViewport();
        check(vp.getView() == null, "default view null");
        check(new Dimension(0, 0).equals(vp.getViewSize()), "default viewSize 0,0");
        check(new Point(0, 0).equals(vp.getViewPosition()), "default viewPosition 0,0");
        check(new Dimension(0, 0).equals(vp.getExtentSize()), "default extentSize 0,0");
        check(new Rectangle(0, 0, 0, 0).equals(vp.getViewRect()), "default viewRect 0,0,0,0");
        check(vp.getScrollMode() == JViewport.BLIT_SCROLL_MODE, "default scroll mode BLIT");
        check(JViewport.SIMPLE_SCROLL_MODE == 0, "SIMPLE_SCROLL_MODE 0");
        check(JViewport.BLIT_SCROLL_MODE == 1, "BLIT_SCROLL_MODE 1");
        check(JViewport.BACKINGSTORE_SCROLL_MODE == 2, "BACKINGSTORE_SCROLL_MODE 2");
        check(!vp.isBackingStoreEnabled(), "default backing store disabled");
        check(!vp.isOptimizedDrawingEnabled(), "isOptimizedDrawingEnabled false");
        check("ViewportUI".equals(vp.getUIClassID()), "getUIClassID ViewportUI");
        check("BasicViewportUI".equals(vp.getUI().getClass().getSimpleName()), "Metal installs BasicViewportUI");
        check(vp.getBorder() == null, "default border null");
        check(new Insets(0, 0, 0, 0).equals(vp.getInsets()), "insets always zero");

        vp.setExtentSize(new Dimension(200, 100));
        check(new Dimension(200, 100).equals(vp.getExtentSize()), "extent size round-trip");
        vp.setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        check(vp.getScrollMode() == JViewport.SIMPLE_SCROLL_MODE, "setScrollMode(SIMPLE)");
        vp.setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
        check(vp.getScrollMode() == JViewport.BACKINGSTORE_SCROLL_MODE, "setScrollMode(BACKINGSTORE)");
        check(vp.isBackingStoreEnabled(), "isBackingStoreEnabled after BACKINGSTORE");
        vp.setBackingStoreEnabled(false);
        check(vp.getScrollMode() == JViewport.BLIT_SCROLL_MODE, "setBackingStoreEnabled(false) -> BLIT");
    }

    // ---- Part 3: view wiring, sizes, positions ----
    static void testView() {
        JViewport vp = new JViewport();
        JLabel view = new JLabel("v");
        vp.setView(view);
        check(vp.getView() == view, "setView/getView");
        check(view.getParent() == vp, "view parented by viewport");
        vp.setViewSize(new Dimension(300, 150));
        check(new Dimension(300, 150).equals(vp.getViewSize()), "view size round-trip");
        vp.setViewPosition(new Point(10, 20));
        check(new Point(10, 20).equals(vp.getViewPosition()), "view position round-trip");
        // Position is NOT clamped to the view size while undisplayed.
        vp.setViewPosition(new Point(500, 500));
        check(new Point(500, 500).equals(vp.getViewPosition()), "view position not clamped");
        // View rect derives from position + extent.
        vp.setExtentSize(new Dimension(120, 80));
        check(new Rectangle(500, 500, 120, 80).equals(vp.getViewRect()), "getViewRect position+extent");
        // Coordinate conversion is identity mapping for a plain viewport.
        check(new Point(5, 6).equals(vp.toViewCoordinates(new Point(5, 6))), "toViewCoordinates(Point)");
        check(new Dimension(7, 8).equals(vp.toViewCoordinates(new Dimension(7, 8))), "toViewCoordinates(Dimension)");
        // scrollRectToVisible is callable regardless of a view / visibility.
        new JViewport().scrollRectToVisible(new Rectangle(0, 0, 10, 10));
        check(true, "scrollRectToVisible empty viewport no-throw");
        vp.scrollRectToVisible(new Rectangle(0, 0, 50, 50));
        check(true, "scrollRectToVisible with view no-throw");
        vp.setView(null);
        check(vp.getView() == null, "setView(null) clears view");
    }

    // ---- Part 4: border rejection ----
    static void testBorder() {
        final JViewport vp = new JViewport();
        vp.setBorder(null);
        check(vp.getBorder() == null, "setBorder(null) allowed");
        checkThrows(IllegalArgumentException.class,
                new Runnable() { public void run() { vp.setBorder(new LineBorder(java.awt.Color.BLACK, 1)); } },
                "setBorder(non-null) IAE");
        check(vp.getBorder() == null, "border still null after rejection");
    }

    public static void main(String[] args) throws Exception {
        try {
            UIManager.setLookAndFeel(new MetalLookAndFeel());
        } catch (Exception e) {
            throw new AssertionError("cannot install Metal LaF", e);
        }
        testShape();
        SwingUtilities.invokeAndWait(new Runnable() { public void run() { testDefaults(); } });
        SwingUtilities.invokeAndWait(new Runnable() { public void run() { testView(); } });
        SwingUtilities.invokeAndWait(new Runnable() { public void run() { testBorder(); } });
        System.out.println("PASS (" + passed + " assertions)");
    }
}
