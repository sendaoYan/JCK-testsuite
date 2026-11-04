/*
 * @test
 * @summary javax.swing.JTabbedPane conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main JTabbedPaneTest
 */

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.lang.reflect.Modifier;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

public class JTabbedPaneTest {
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

    static class FixedIcon implements Icon {
        private final int w, h;
        FixedIcon(int w, int h) { this.w = w; this.h = h; }
        public void paintIcon(Component c, Graphics g, int x, int y) {}
        public int getIconWidth() { return w; }
        public int getIconHeight() { return h; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.JTabbedPane");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 3, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.io.Serializable"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("javax.accessibility.Accessible"), "interface 1");
        check(c.getInterfaces()[2].getName().equals("javax.swing.SwingConstants"), "interface 2");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Defaults.
        JTabbedPane tp = new JTabbedPane();
        check(tp.getTabCount() == 0, "default tab count 0");
        check(tp.getSelectedIndex() == -1, "default selectedIndex -1");
        check(tp.getSelectedComponent() == null, "default selectedComponent null");
        check(tp.getTabPlacement() == SwingConstants.TOP, "default tabPlacement TOP");
        check(tp.getTabLayoutPolicy() == JTabbedPane.WRAP_TAB_LAYOUT, "default tabLayoutPolicy WRAP");

        // addTab + first tab auto-selected.
        JLabel comp1 = new JLabel("c1");
        JLabel comp2 = new JLabel("c2");
        tp.addTab("Tab1", comp1);
        check(tp.getTabCount() == 1, "addTab count 1");
        check(tp.getSelectedIndex() == 0, "first tab selected");
        check("Tab1".equals(tp.getTitleAt(0)), "getTitleAt(0)");
        check(tp.getComponentAt(0) == comp1, "getComponentAt(0)");
        check(tp.getSelectedComponent() == comp1, "getSelectedComponent first");

        tp.addTab("Tab2", comp2);
        check(tp.getTabCount() == 2, "addTab count 2");
        check(tp.getSelectedIndex() == 0, "addTab keeps selection");
        check("Tab2".equals(tp.getTitleAt(1)), "getTitleAt(1)");
        check(tp.getComponentAt(1) == comp2, "getComponentAt(1)");

        // indexOfTab / indexOfComponent.
        check(tp.indexOfTab("Tab1") == 0, "indexOfTab Tab1");
        check(tp.indexOfTab("Tab2") == 1, "indexOfTab Tab2");
        check(tp.indexOfTab("nope") == -1, "indexOfTab missing");
        check(tp.indexOfComponent(comp2) == 1, "indexOfComponent comp2");
        check(tp.indexOfComponent(new JLabel("z")) == -1, "indexOfComponent missing");

        // Selection roundtrip.
        tp.setSelectedIndex(1);
        check(tp.getSelectedIndex() == 1, "setSelectedIndex(1)");
        check(tp.getSelectedComponent() == comp2, "getSelectedComponent after setSelectedIndex");
        tp.setSelectedComponent(comp1);
        check(tp.getSelectedIndex() == 0, "setSelectedComponent updates index");
        try {
            tp.setSelectedComponent(new JLabel("x"));
            check(false, "setSelectedComponent unknown should throw IAE");
        } catch (IllegalArgumentException expected) {
            check(true, "setSelectedComponent unknown IAE");
        }

        // Title roundtrip.
        tp.setTitleAt(0, "Renamed");
        check("Renamed".equals(tp.getTitleAt(0)), "setTitleAt/getTitleAt");
        check(tp.indexOfTab("Renamed") == 0, "indexOfTab renamed");

        // Enabled-at roundtrip.
        tp.setEnabledAt(1, false);
        check(!tp.isEnabledAt(1), "setEnabledAt(false)");
        check(tp.isEnabledAt(0), "isEnabledAt(0) still true");
        tp.setEnabledAt(1, true);
        check(tp.isEnabledAt(1), "setEnabledAt(true)");

        // Mnemonic roundtrip.
        tp.setMnemonicAt(0, KeyEvent.VK_A);
        check(tp.getMnemonicAt(0) == KeyEvent.VK_A, "setMnemonicAt/getMnemonicAt");
        check(tp.getMnemonicAt(1) == -1, "getMnemonicAt default -1");

        // Tooltip / colors roundtrip.
        tp.setToolTipTextAt(0, "tip");
        check("tip".equals(tp.getToolTipTextAt(0)), "setToolTipTextAt/getToolTipTextAt");
        tp.setBackgroundAt(0, Color.RED);
        check(Color.RED.equals(tp.getBackgroundAt(0)), "setBackgroundAt/getBackgroundAt");
        tp.setForegroundAt(0, Color.BLUE);
        check(Color.BLUE.equals(tp.getForegroundAt(0)), "setForegroundAt/getForegroundAt");

        // Component replacement.
        JLabel comp3 = new JLabel("c3");
        tp.setComponentAt(1, comp3);
        check(tp.getComponentAt(1) == comp3, "setComponentAt/getComponentAt");
        check(tp.indexOfComponent(comp3) == 1, "indexOfComponent replaced");

        // Icon roundtrip.
        check(tp.getIconAt(0) == null, "getIconAt null initially");
        Icon icon1 = new FixedIcon(16, 16);
        tp.setIconAt(0, icon1);
        check(tp.getIconAt(0) == icon1, "setIconAt/getIconAt");
        check(tp.indexOfTab(icon1) == 0, "indexOfTab(Icon)");

        // Placement / layout policy roundtrip + invalid.
        tp.setTabPlacement(SwingConstants.BOTTOM);
        check(tp.getTabPlacement() == SwingConstants.BOTTOM, "setTabPlacement(BOTTOM)");
        tp.setTabPlacement(SwingConstants.TOP);
        check(tp.getTabPlacement() == SwingConstants.TOP, "setTabPlacement(TOP)");
        tp.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        check(tp.getTabLayoutPolicy() == JTabbedPane.SCROLL_TAB_LAYOUT, "setTabLayoutPolicy(SCROLL)");
        try {
            tp.setTabLayoutPolicy(999);
            check(false, "setTabLayoutPolicy(999) should throw IAE");
        } catch (IllegalArgumentException expected) {
            check(true, "setTabLayoutPolicy(999) IAE");
        }

        // removeTabAt shrinks the pane.
        tp.removeTabAt(0);
        check(tp.getTabCount() == 1, "removeTabAt count");
        check("Tab2".equals(tp.getTitleAt(0)), "removeTabAt shifts remaining tab");
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(JTabbedPaneTest::testWhiteBox);
        runOnEDT(JTabbedPaneTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
