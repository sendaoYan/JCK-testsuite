/*
 * @test
 * @summary javax.swing.JScrollPane conformance: white-box structure + black-box behavior
 *          + policy constants, lazy headers, layout wiring, fixed geometry
 *          (headless-safe, Metal LaF).
 * @run main JScrollPaneTest
 */

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Rectangle;
import java.lang.reflect.Modifier;
import javax.swing.JLabel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.JPanel;
import javax.swing.ScrollPaneConstants;
import javax.swing.ScrollPaneLayout;
import javax.swing.UIManager;
import javax.swing.ViewportLayout;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class JScrollPaneTest {
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
        Class<?> c = Class.forName("javax.swing.JScrollPane");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.ScrollPaneConstants"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("javax.accessibility.Accessible"), "interface 1");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Default construction.
        JScrollPane sp = new JScrollPane();
        check(sp.getVerticalScrollBarPolicy() == ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, "default vsb policy AS_NEEDED");
        check(sp.getHorizontalScrollBarPolicy() == ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED, "default hsb policy AS_NEEDED");
        check(sp.getViewport() != null, "default viewport non-null");
        check(sp.getViewport().getView() == null, "default viewport view null");
        check(sp.getVerticalScrollBar() != null, "default vertical scrollbar non-null");
        check(sp.getHorizontalScrollBar() != null, "default horizontal scrollbar non-null");
        check(sp.isWheelScrollingEnabled(), "default wheel scrolling enabled");
        check("ScrollPaneUI".equals(sp.getUIClassID()), "getUIClassID ScrollPaneUI");

        // View constructor roundtrip.
        JLabel view = new JLabel("view");
        JScrollPane spv = new JScrollPane(view);
        check(spv.getViewport().getView() == view, "constructor view installed");
        check(spv.getVerticalScrollBarPolicy() == ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, "view ctor vsb AS_NEEDED");

        // Policy roundtrip + invalid value.
        spv.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        check(spv.getVerticalScrollBarPolicy() == ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, "setVerticalScrollBarPolicy(ALWAYS)");
        spv.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        check(spv.getVerticalScrollBarPolicy() == ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, "setVerticalScrollBarPolicy(NEVER)");
        try {
            spv.setVerticalScrollBarPolicy(-1);
            check(false, "setVerticalScrollBarPolicy(-1) should throw IAE");
        } catch (IllegalArgumentException expected) {
            check(true, "setVerticalScrollBarPolicy(-1) IAE");
        }
        spv.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        check(spv.getHorizontalScrollBarPolicy() == ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS, "setHorizontalScrollBarPolicy(ALWAYS)");
        spv.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        check(spv.getHorizontalScrollBarPolicy() == ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER, "setHorizontalScrollBarPolicy(NEVER)");

        // setViewportView replaces the view.
        JLabel view2 = new JLabel("view2");
        sp.setViewportView(view2);
        check(sp.getViewport().getView() == view2, "setViewportView/getViewport().getView()");

        // Column header view roundtrip.
        JLabel colHeader = new JLabel("col");
        sp.setColumnHeaderView(colHeader);
        check(sp.getColumnHeader() != null, "column header viewport non-null");
        check(sp.getColumnHeader().getView() == colHeader, "setColumnHeaderView/getColumnHeader().getView()");

        // Row header view roundtrip.
        JLabel rowHeader = new JLabel("row");
        sp.setRowHeaderView(rowHeader);
        check(sp.getRowHeader() != null, "row header viewport non-null");
        check(sp.getRowHeader().getView() == rowHeader, "setRowHeaderView/getRowHeader().getView()");

        // Corner roundtrip + invalid key.
        JLabel corner = new JLabel("corner");
        sp.setCorner(JScrollPane.UPPER_LEFT_CORNER, corner);
        check(sp.getCorner(JScrollPane.UPPER_LEFT_CORNER) == corner, "setCorner/getCorner");
        check(sp.getCorner("bogus-corner") == null, "getCorner invalid key null");
        try {
            sp.setCorner("bogus-corner", corner);
            check(false, "setCorner invalid key should throw IAE");
        } catch (IllegalArgumentException expected) {
            check(true, "setCorner invalid key IAE");
        }

        // Viewport border roundtrip.
        Border vb = new LineBorder(java.awt.Color.BLACK, 1);
        sp.setViewportBorder(vb);
        check(sp.getViewportBorder() == vb, "setViewportBorder/getViewportBorder");

        // Viewport replacement roundtrip.
        JViewport vp = new JViewport();
        JLabel v3 = new JLabel("v3");
        vp.setView(v3);
        sp.setViewport(vp);
        check(sp.getViewport() == vp, "setViewport/getViewport");
        check(sp.getViewport().getView() == v3, "custom viewport view preserved");

        // Scrollbar replacement roundtrip.
        JScrollBar vsb = new JScrollBar();
        sp.setVerticalScrollBar(vsb);
        check(sp.getVerticalScrollBar() == vsb, "setVerticalScrollBar/getVerticalScrollBar");
        JScrollBar hsb = new JScrollBar();
        sp.setHorizontalScrollBar(hsb);
        check(sp.getHorizontalScrollBar() == hsb, "setHorizontalScrollBar/getHorizontalScrollBar");

        // Wheel scrolling roundtrip.
        sp.setWheelScrollingEnabled(false);
        check(!sp.isWheelScrollingEnabled(), "setWheelScrollingEnabled(false)");
        sp.setWheelScrollingEnabled(true);
        check(sp.isWheelScrollingEnabled(), "setWheelScrollingEnabled(true)");

        // Nested scrolling: a scroll pane can be another scroll pane's view.
        JScrollPane inner = new JScrollPane(new JLabel("inner"));
        JScrollPane outer = new JScrollPane();
        outer.setViewportView(inner);
        check(outer.getViewport().getView() == inner, "nested scroll pane view");
    }

    // ---- Part 3: constants, lazy headers, layout wiring, fixed geometry ----
    static void testContractExtras() throws Exception {
        check(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED == 20, "VSB AS_NEEDED 20");
        check(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER == 21, "VSB NEVER 21");
        check(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS == 22, "VSB ALWAYS 22");
        check(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED == 30, "HSB AS_NEEDED 30");
        check(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER == 31, "HSB NEVER 31");
        check(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS == 32, "HSB ALWAYS 32");

        JScrollPane sp = new JScrollPane();
        check("MetalScrollPaneUI".equals(sp.getUI().getClass().getSimpleName()), "Metal installs MetalScrollPaneUI");
        // Headers are lazy: not created until the corresponding view is set.
        check(sp.getColumnHeader() == null, "column header lazy null");
        check(sp.getRowHeader() == null, "row header lazy null");
        // Layout wiring.
        check(sp.getLayout() instanceof ScrollPaneLayout, "pane layout is ScrollPaneLayout");
        check(sp.getLayout() instanceof javax.swing.plaf.UIResource, "installed layout is UIResource");
        check(sp.getViewport().getLayout() instanceof ViewportLayout, "viewport layout is ViewportLayout");
        try {
            sp.setHorizontalScrollBarPolicy(99);
            check(false, "setHorizontalScrollBarPolicy(99) should throw IAE");
        } catch (IllegalArgumentException expected) {
            check(true, "setHorizontalScrollBarPolicy(99) IAE");
        }
        JLabel corner = new JLabel("corner");
        sp.setCorner(ScrollPaneConstants.LOWER_LEFT_CORNER, corner);
        check(sp.getCorner(ScrollPaneConstants.LOWER_LEFT_CORNER) == corner, "LOWER_LEFT corner round-trip");
        sp.setCorner(ScrollPaneConstants.LOWER_LEFT_CORNER, null);
        check(sp.getCorner(ScrollPaneConstants.LOWER_LEFT_CORNER) == null, "setCorner(key,null) clears");

        // Deterministic fixed geometry (Metal metrics; no headers involved).
        JPanel big = new JPanel();
        big.setPreferredSize(new java.awt.Dimension(400, 400));
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
        runOnEDT(JScrollPaneTest::testWhiteBox);
        runOnEDT(JScrollPaneTest::testBlackBox);
        runOnEDT(JScrollPaneTest::testContractExtras);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
