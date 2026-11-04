/*
 * @test
 * @summary javax.swing.JScrollPane conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main JScrollPaneTest
 */

import java.awt.Component;
import java.awt.EventQueue;
import java.lang.reflect.Modifier;
import javax.swing.JLabel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

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

    public static void main(String[] args) throws Exception {
        runOnEDT(JScrollPaneTest::testWhiteBox);
        runOnEDT(JScrollPaneTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
