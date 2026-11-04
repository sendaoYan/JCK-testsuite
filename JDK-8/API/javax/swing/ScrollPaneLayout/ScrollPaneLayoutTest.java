/*
 * @test
 * @summary javax.swing.ScrollPaneLayout conformance: type shape via reflection + layout
 *          contract (sync wiring, key-based add/remove, policy IAE, empty-pane layout;
 *          headless-safe, Metal LaF, EDT).
 * @run main ScrollPaneLayoutTest
 */

import java.awt.Dimension;
import java.awt.Rectangle;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.ScrollPaneLayout;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class ScrollPaneLayoutTest {
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
        Class<?> c = Class.forName("javax.swing.ScrollPaneLayout", false, ScrollPaneLayoutTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(hasInterface(c, "java.awt.LayoutManager"), "implements java.awt.LayoutManager");
        check(hasInterface(c, "javax.swing.ScrollPaneConstants"), "implements javax.swing.ScrollPaneConstants");
        check(hasInterface(c, "java.io.Serializable"), "implements java.io.Serializable");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("syncWithScrollPane"), "method syncWithScrollPane");
        check(names.contains("addLayoutComponent"), "method addLayoutComponent");
        check(names.contains("removeLayoutComponent"), "method removeLayoutComponent");
        check(names.contains("getVerticalScrollBarPolicy"), "method getVerticalScrollBarPolicy");
        check(names.contains("setVerticalScrollBarPolicy"), "method setVerticalScrollBarPolicy");
        check(names.contains("getHorizontalScrollBarPolicy"), "method getHorizontalScrollBarPolicy");
        check(names.contains("setHorizontalScrollBarPolicy"), "method setHorizontalScrollBarPolicy");
    }

    // ---- Part 2: policy round-trips and reverse cases ----
    static void testPolicies() {
        final ScrollPaneLayout layout = new ScrollPaneLayout();
        layout.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        check(layout.getVerticalScrollBarPolicy() == ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                "vsb policy round-trip");
        layout.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        check(layout.getHorizontalScrollBarPolicy() == ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER,
                "hsb policy round-trip");
        checkThrows(IllegalArgumentException.class,
                new Runnable() { public void run() { layout.setVerticalScrollBarPolicy(99); } },
                "setVerticalScrollBarPolicy(99) IAE");
        checkThrows(IllegalArgumentException.class,
                new Runnable() { public void run() { layout.setHorizontalScrollBarPolicy(99); } },
                "setHorizontalScrollBarPolicy(99) IAE");
    }

    // ---- Part 3: key-based add/remove ----
    static void testKeys() {
        final ScrollPaneLayout layout = new ScrollPaneLayout();
        checkThrows(IllegalArgumentException.class,
                new Runnable() { public void run() { layout.addLayoutComponent("x", new JLabel()); } },
                "addLayoutComponent(unknown key) IAE");
        JLabel corner = new JLabel("c");
        layout.addLayoutComponent(ScrollPaneConstants.LOWER_LEFT_CORNER, corner);
        check(layout.getCorner(ScrollPaneConstants.LOWER_LEFT_CORNER) == corner,
                "addLayoutComponent(corner key) wires corner");
        layout.removeLayoutComponent(corner);
        check(true, "removeLayoutComponent no-throw");
    }

    // ---- Part 4: sync wiring and empty-pane layout ----
    static void testSyncAndLayout() {
        ScrollPaneLayout layout = new ScrollPaneLayout();
        JScrollPane sp = new JScrollPane();
        layout.syncWithScrollPane(sp);
        check(layout.getViewport() == sp.getViewport(), "sync wires viewport identity");
        check(layout.getVerticalScrollBar() == sp.getVerticalScrollBar(), "sync wires vsb identity");
        check(layout.getHorizontalScrollBar() == sp.getHorizontalScrollBar(), "sync wires hsb identity");
        check(layout.getRowHeader() == null, "row header null on plain pane");
        check(layout.getColumnHeader() == null, "column header null on plain pane");
        check(layout.getCorner(ScrollPaneConstants.LOWER_LEFT_CORNER) == null, "corner null on plain pane");

        // A fresh sync picks up headers added to the pane in the meantime.
        JScrollPane sp3 = new JScrollPane();
        sp3.setColumnHeaderView(new JLabel("h"));
        ScrollPaneLayout lm3 = new ScrollPaneLayout();
        lm3.syncWithScrollPane(sp3);
        check(lm3.getColumnHeader() == sp3.getColumnHeader(), "sync picks up column header");

        check(layout.preferredLayoutSize(sp) != null, "preferredLayoutSize non-null");
        check(new Dimension(22, 22).equals(layout.minimumLayoutSize(sp)), "minimumLayoutSize 22x22");
        sp.setSize(100, 100);
        layout.layoutContainer(sp);
        // Empty view + AS_NEEDED policies: viewport keeps the full inner area,
        // neither scrollbar is shown.
        check(new Rectangle(1, 1, 97, 97).equals(sp.getViewport().getBounds()),
                "empty AS_NEEDED layout viewport (1,1,97,97)");
        check(!sp.getVerticalScrollBar().isVisible(), "vsb hidden with empty view");
        check(!sp.getHorizontalScrollBar().isVisible(), "hsb hidden with empty view");
    }

    public static void main(String[] args) throws Exception {
        try {
            UIManager.setLookAndFeel(new MetalLookAndFeel());
        } catch (Exception e) {
            throw new AssertionError("cannot install Metal LaF", e);
        }
        testShape();
        SwingUtilities.invokeAndWait(new Runnable() { public void run() { testPolicies(); } });
        SwingUtilities.invokeAndWait(new Runnable() { public void run() { testKeys(); } });
        SwingUtilities.invokeAndWait(new Runnable() { public void run() { testSyncAndLayout(); } });
        System.out.println("PASS (" + passed + " assertions)");
    }
}
