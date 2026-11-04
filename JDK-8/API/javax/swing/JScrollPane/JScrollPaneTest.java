/*
 * @test
 * @summary javax.swing.JScrollPane conformance: type shape via reflection + scroll
 *          pane contract (policy constants/matrix, corner keys, lazy headers, layout
 *          wiring, fixed geometry; headless-safe, Metal LaF, EDT).
 * @run main JScrollPaneTest
 */

import java.awt.Dimension;
import java.awt.Rectangle;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.ScrollPaneLayout;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.ViewportLayout;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class JScrollPaneTest {
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
        Class<?> c = Class.forName("javax.swing.JScrollPane", false, JScrollPaneTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent"), "superclass");
        check(hasInterface(c, "javax.swing.ScrollPaneConstants"), "implements javax.swing.ScrollPaneConstants");
        check(hasInterface(c, "javax.accessibility.Accessible"), "implements javax.accessibility.Accessible");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getUI"), "method getUI");
        check(names.contains("setUI"), "method setUI");
        check(names.contains("updateUI"), "method updateUI");
        check(names.contains("getUIClassID"), "method getUIClassID");
    }

    // ---- Part 2: policy constants, defaults, lazy headers ----
    static void testConstantsAndDefaults() {
        check(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED == 20, "VSB AS_NEEDED 20");
        check(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER == 21, "VSB NEVER 21");
        check(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS == 22, "VSB ALWAYS 22");
        check(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED == 30, "HSB AS_NEEDED 30");
        check(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER == 31, "HSB NEVER 31");
        check(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS == 32, "HSB ALWAYS 32");

        final JScrollPane sp = new JScrollPane();
        check(sp.getVerticalScrollBarPolicy() == ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                "default vsb policy AS_NEEDED");
        check(sp.getHorizontalScrollBarPolicy() == ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED,
                "default hsb policy AS_NEEDED");
        check(sp.getViewport() != null, "default viewport non-null");
        check(sp.getViewport().getView() == null, "default viewport view null");
        check(sp.getVerticalScrollBar() != null, "default vertical scrollbar non-null");
        check(sp.getHorizontalScrollBar() != null, "default horizontal scrollbar non-null");
        check(sp.isWheelScrollingEnabled(), "default wheel scrolling enabled");
        check("ScrollPaneUI".equals(sp.getUIClassID()), "getUIClassID ScrollPaneUI");
        check("MetalScrollPaneUI".equals(sp.getUI().getClass().getSimpleName()), "Metal installs MetalScrollPaneUI");
        // Headers are lazy: not created until the corresponding view is set.
        check(sp.getColumnHeader() == null, "column header lazy null");
        check(sp.getRowHeader() == null, "row header lazy null");
        // Layout wiring.
        check(sp.getLayout() instanceof ScrollPaneLayout, "pane layout is ScrollPaneLayout");
        check(sp.getLayout() instanceof javax.swing.plaf.UIResource, "installed layout is UIResource");
        check(sp.getViewport().getLayout() instanceof ViewportLayout, "viewport layout is ViewportLayout");
    }

    // ---- Part 3: policy matrix and reverse cases ----
    static void testPolicies() {
        final JScrollPane sp = new JScrollPane(new JLabel("view"));
        check(sp.getViewport().getView() != null, "view ctor installs view");
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        check(sp.getVerticalScrollBarPolicy() == ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, "vsb ALWAYS round-trip");
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        check(sp.getVerticalScrollBarPolicy() == ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, "vsb NEVER round-trip");
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        check(sp.getHorizontalScrollBarPolicy() == ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS, "hsb ALWAYS round-trip");
        checkThrows(IllegalArgumentException.class,
                new Runnable() { public void run() { sp.setVerticalScrollBarPolicy(99); } },
                "setVerticalScrollBarPolicy(99) IAE");
        checkThrows(IllegalArgumentException.class,
                new Runnable() { public void run() { sp.setHorizontalScrollBarPolicy(99); } },
                "setHorizontalScrollBarPolicy(99) IAE");
        sp.setWheelScrollingEnabled(false);
        check(!sp.isWheelScrollingEnabled(), "wheel scrolling off");
        sp.setWheelScrollingEnabled(true);
        check(sp.isWheelScrollingEnabled(), "wheel scrolling on");
    }

    // ---- Part 4: headers, corners, replacement round-trips ----
    static void testParts() {
        JScrollPane sp = new JScrollPane();
        JLabel view2 = new JLabel("view2");
        sp.setViewportView(view2);
        check(sp.getViewport().getView() == view2, "setViewportView");

        JLabel colHeader = new JLabel("col");
        sp.setColumnHeaderView(colHeader);
        check(sp.getColumnHeader() != null, "column header viewport after set");
        check(sp.getColumnHeader().getView() == colHeader, "column header view round-trip");
        JLabel rowHeader = new JLabel("row");
        sp.setRowHeaderView(rowHeader);
        check(sp.getRowHeader() != null, "row header viewport after set");
        check(sp.getRowHeader().getView() == rowHeader, "row header view round-trip");

        JLabel corner = new JLabel("corner");
        sp.setCorner(ScrollPaneConstants.LOWER_LEFT_CORNER, corner);
        check(sp.getCorner(ScrollPaneConstants.LOWER_LEFT_CORNER) == corner, "setCorner/getCorner");
        sp.setCorner(ScrollPaneConstants.LOWER_LEFT_CORNER, null);
        check(sp.getCorner(ScrollPaneConstants.LOWER_LEFT_CORNER) == null, "setCorner(key,null) clears");
        check(sp.getCorner("bogus-corner") == null, "getCorner invalid key null");
        checkThrows(IllegalArgumentException.class,
                new Runnable() { public void run() { sp.setCorner("bogus-corner", corner); } },
                "setCorner invalid key IAE");

        Border vb = new LineBorder(java.awt.Color.BLACK, 1);
        sp.setViewportBorder(vb);
        check(sp.getViewportBorder() == vb, "viewport border round-trip");

        JViewport vp = new JViewport();
        JLabel v3 = new JLabel("v3");
        vp.setView(v3);
        sp.setViewport(vp);
        check(sp.getViewport() == vp, "setViewport round-trip");
        check(sp.getViewport().getView() == v3, "custom viewport view preserved");
        JScrollBar vsb = new JScrollBar();
        sp.setVerticalScrollBar(vsb);
        check(sp.getVerticalScrollBar() == vsb, "setVerticalScrollBar round-trip");
        JScrollBar hsb = new JScrollBar();
        sp.setHorizontalScrollBar(hsb);
        check(sp.getHorizontalScrollBar() == hsb, "setHorizontalScrollBar round-trip");

        // Nested scrolling: a scroll pane can be another scroll pane's view.
        JScrollPane inner = new JScrollPane(new JLabel("inner"));
        JScrollPane outer = new JScrollPane();
        outer.setViewportView(inner);
        check(outer.getViewport().getView() == inner, "nested scroll pane view");
    }

    // ---- Part 5: deterministic fixed geometry (Metal metrics) ----
    static void testGeometry() {
        JScrollPane sp = new JScrollPane();
        JPanel big = new JPanel();
        big.setPreferredSize(new Dimension(400, 400));
        sp.setViewportView(big);
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        sp.setSize(100, 100);
        sp.doLayout();
        check(new Rectangle(1, 1, 82, 82).equals(sp.getViewport().getBounds()),
                "viewport [1,1,82,82] with ALWAYS vsb in 100x100 pane");
        check(sp.getVerticalScrollBar().isVisible(), "ALWAYS policy shows the scrollbar");
        check(new Rectangle(83, 1, 15, 82).equals(sp.getVerticalScrollBar().getBounds()),
                "vertical scrollbar column [83,1,15,82]");
    }

    public static void main(String[] args) throws Exception {
        try {
            UIManager.setLookAndFeel(new MetalLookAndFeel());
        } catch (Exception e) {
            throw new AssertionError("cannot install Metal LaF", e);
        }
        testShape();
        SwingUtilities.invokeAndWait(new Runnable() { public void run() { testConstantsAndDefaults(); } });
        SwingUtilities.invokeAndWait(new Runnable() { public void run() { testPolicies(); } });
        SwingUtilities.invokeAndWait(new Runnable() { public void run() { testParts(); } });
        SwingUtilities.invokeAndWait(new Runnable() { public void run() { testGeometry(); } });
        System.out.println("PASS (" + passed + " assertions)");
    }
}
