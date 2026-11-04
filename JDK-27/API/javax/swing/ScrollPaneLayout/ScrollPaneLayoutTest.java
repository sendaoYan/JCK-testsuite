/*
 * @test
 * @summary javax.swing.ScrollPaneLayout conformance: white-box structure + black-box
 *          behavior + key-based add IAE, sync identity wiring, empty-pane layout
 *          (headless-safe, Metal LaF, EDT).
 * @run main ScrollPaneLayoutTest
 */

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Rectangle;
import java.lang.reflect.Modifier;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.ScrollPaneLayout;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class ScrollPaneLayoutTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void checkThrows(Class<? extends Throwable> ex, Runnable r, String m) {
        try { r.run(); }
        catch (Throwable t) {
            if (!ex.isInstance(t)) throw new AssertionError(m + ": expected " + ex.getSimpleName() + " got " + t);
            passed++; return; }
        throw new AssertionError(m + ": expected " + ex.getSimpleName() + " not thrown");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.ScrollPaneLayout");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 3, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.awt.LayoutManager"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("javax.swing.ScrollPaneConstants"), "interface 1");
        check(c.getInterfaces()[2].getName().equals("java.io.Serializable"), "interface 2");
    }

    // ---- Part 2: black-box (behavioral, kept) ----
    static void testBlackBox() {
        ScrollPaneLayout layout = new ScrollPaneLayout();
        layout.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        check(layout.getVerticalScrollBarPolicy() == ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, "vsb policy");
        layout.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        check(layout.getHorizontalScrollBarPolicy() == ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER, "hsb policy");

        JScrollPane sp = new JScrollPane();
        layout.syncWithScrollPane(sp);
        check(layout.getViewport() != null, "getViewport");
        check(layout.getHorizontalScrollBar() != null, "getHorizontalScrollBar");
        check(layout.getVerticalScrollBar() != null, "getVerticalScrollBar");
        check(layout.getRowHeader() == null, "row header null");
        check(layout.getColumnHeader() == null, "column header null");
        check(layout.getCorner(ScrollPaneConstants.LOWER_LEFT_CORNER) == null, "corner null");

        JScrollPane sp2 = new JScrollPane();
        layout.layoutContainer(sp2);
        check(layout.preferredLayoutSize(sp2) != null, "preferredLayoutSize");
    }

    // ---- Part 3: policy IAE, key add/remove, sync identity, empty layout ----
    static void testContractExtras() {
        final ScrollPaneLayout layout = new ScrollPaneLayout();
        checkThrows(IllegalArgumentException.class, () -> layout.setVerticalScrollBarPolicy(99),
                "setVerticalScrollBarPolicy(99) IAE");
        checkThrows(IllegalArgumentException.class, () -> layout.setHorizontalScrollBarPolicy(99),
                "setHorizontalScrollBarPolicy(99) IAE");
        checkThrows(IllegalArgumentException.class, () -> layout.addLayoutComponent("x", new JLabel()),
                "addLayoutComponent(unknown key) IAE");
        JLabel corner = new JLabel("c");
        layout.addLayoutComponent(ScrollPaneConstants.LOWER_LEFT_CORNER, corner);
        check(layout.getCorner(ScrollPaneConstants.LOWER_LEFT_CORNER) == corner,
                "addLayoutComponent(corner key) wires corner");
        layout.removeLayoutComponent(corner);
        check(true, "removeLayoutComponent no-throw");

        ScrollPaneLayout lm = new ScrollPaneLayout();
        JScrollPane sp = new JScrollPane();
        lm.syncWithScrollPane(sp);
        check(lm.getViewport() == sp.getViewport(), "sync wires viewport identity");
        check(lm.getVerticalScrollBar() == sp.getVerticalScrollBar(), "sync wires vsb identity");
        check(lm.getHorizontalScrollBar() == sp.getHorizontalScrollBar(), "sync wires hsb identity");

        JScrollPane sp3 = new JScrollPane();
        sp3.setColumnHeaderView(new JLabel("h"));
        ScrollPaneLayout lm3 = new ScrollPaneLayout();
        lm3.syncWithScrollPane(sp3);
        check(lm3.getColumnHeader() == sp3.getColumnHeader(), "sync picks up column header");

        check(lm.preferredLayoutSize(sp) != null, "preferredLayoutSize non-null");
        check(new Dimension(22, 22).equals(lm.minimumLayoutSize(sp)), "minimumLayoutSize 22x22");
        sp.setSize(100, 100);
        lm.layoutContainer(sp);
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
        EventQueue.invokeAndWait(() -> {
            try {
                testWhiteBox();
                testBlackBox();
                testContractExtras();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println("PASS (" + passed + " assertions)");
    }
}
