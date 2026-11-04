/*
 * @test
 * @summary javax.swing.JTabbedPane conformance: white-box structure + black-box behavior
 *          + tab model contract (insert/removeAll/selection migration, change events,
 *          reverse index/argument negatives; headless-safe, Metal LaF).
 * @run main JTabbedPaneTest
 */

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.SingleSelectionModel;
import javax.swing.DefaultSingleSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.metal.MetalLookAndFeel;

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

    static void checkThrows(Class<? extends Throwable> ex, Runnable r, String m) {
        try { r.run(); }
        catch (Throwable t) {
            if (!ex.isInstance(t)) throw new AssertionError(m + ": expected " + ex.getSimpleName() + " got " + t);
            passed++; return; }
        throw new AssertionError(m + ": expected " + ex.getSimpleName() + " not thrown");
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

    // ---- Part 3: defaults, constructors, insert/removeAll bookkeeping ----
    static void testContractDefaults() throws Exception {
        JTabbedPane tp = new JTabbedPane();
        check(tp.getTabRunCount() == 0, "default tabRunCount 0");
        check(tp.getModel() != null && tp.getModel().getSelectedIndex() == -1, "default model selection -1");
        check("TabbedPaneUI".equals(tp.getUIClassID()), "getUIClassID");
        check("MetalTabbedPaneUI".equals(tp.getUI().getClass().getSimpleName()), "Metal installs MetalTabbedPaneUI");
        check(tp.indexOfComponent(new JLabel("x")) == -1, "indexOfComponent on empty -1");
        check(tp.indexOfTab("none") == -1, "indexOfTab(String) on empty -1");
        check(tp.indexOfTab((String) null) == -1, "indexOfTab((String)null) -1");
        check(tp.indexOfTab((Icon) null) == -1, "indexOfTab((Icon)null) -1");

        JTabbedPane tp2 = new JTabbedPane(SwingConstants.BOTTOM, JTabbedPane.SCROLL_TAB_LAYOUT);
        check(tp2.getTabPlacement() == SwingConstants.BOTTOM, "ctor(BOTTOM, SCROLL) placement");
        check(tp2.getTabLayoutPolicy() == JTabbedPane.SCROLL_TAB_LAYOUT, "ctor(BOTTOM, SCROLL) policy");

        // Container.add(String, Component) routes through addImpl as a titled tab.
        tp.add("C", new JLabel("c"));
        check(tp.getTabCount() == 1, "add(String,Component) adds tab");
        check("C".equals(tp.getTitleAt(0)), "add(String,Component) title");

        // insertTab in the middle shifts everything.
        tp.addTab("A", new JLabel("a"));
        tp.insertTab("ins", new FixedIcon(3, 3), new JLabel("i"), "tipi", 0);
        check(tp.getTabCount() == 3, "insertTab count");
        check("ins".equals(tp.getTitleAt(0)), "insertTab at 0 title");
        check(tp.getToolTipTextAt(0) == null || "tipi".equals(tp.getToolTipTextAt(0)), "insertTab tip");

        // removeAll clears selection; next added tab becomes selected again.
        tp.removeAll();
        check(tp.getTabCount() == 0, "removeAll count 0");
        check(tp.getSelectedIndex() == -1, "selectedIndex -1 after removeAll");
        tp.addTab("again", new JLabel("g"));
        check(tp.getSelectedIndex() == 0, "first added tab becomes selected");

        // Disabled icon round-trip.
        Icon ic = new FixedIcon(2, 2);
        tp.setDisabledIconAt(0, ic);
        check(tp.getDisabledIconAt(0) == ic, "setDisabledIconAt round-trip");

        // Model swap round-trip.
        SingleSelectionModel m = new DefaultSingleSelectionModel();
        tp.setModel(m);
        check(tp.getModel() == m, "setModel round-trip");
    }

    // ---- Part 4: selection semantics, change events, removal migration ----
    static void testContractSelection() throws Exception {
        JTabbedPane tp = new JTabbedPane();
        tp.addTab("one", new JLabel("1"));
        tp.addTab("two", new JLabel("2"));
        tp.addTab("three", new JLabel("3"));
        check(tp.getSelectedIndex() == 0, "first tab auto-selected");
        AtomicInteger fired = new AtomicInteger();
        tp.addChangeListener((ChangeEvent e) -> fired.incrementAndGet());
        tp.setSelectedIndex(2);
        check(tp.getSelectedIndex() == 2, "setSelectedIndex");
        check(fired.get() == 1, "ChangeEvent fired once");
        tp.setSelectedIndex(2);
        check(fired.get() == 1, "re-select same index fires nothing");
        check(tp.getModel().getSelectedIndex() == 2, "model tracks pane selection");
        tp.getModel().setSelectedIndex(1);
        check(tp.getSelectedIndex() == 1, "pane tracks model selection");
        check(fired.get() == 2, "model-driven change fires listener");
        // Removing the selected (non-last) tab keeps the index: neighbour content shows.
        tp.removeTabAt(1);
        check(tp.getTabCount() == 2, "count after removing selected tab");
        check(tp.getSelectedIndex() == 1, "selected index held at neighbour after removal");
        // Removing the last selected tab steps back to the previous index.
        tp.setSelectedIndex(1);
        tp.removeTabAt(1);
        check(tp.getSelectedIndex() == 0, "removing last selected tab selects previous");
        // Mnemonics: valid title/mnemonic pair round-trips.
        JTabbedPane mp = new JTabbedPane();
        mp.addTab("Tab", new JLabel());
        check(mp.getMnemonicAt(0) == -1, "default mnemonic -1");
        mp.setMnemonicAt(0, 'T');
        check(mp.getMnemonicAt(0) == 'T', "setMnemonicAt round-trip");
        mp.setDisplayedMnemonicIndexAt(0, 0);
        check(mp.getDisplayedMnemonicIndexAt(0) == 0, "setDisplayedMnemonicIndexAt matching char");
    }

    // ---- Part 5: reverse / negative matrix ----
    static void testContractNegatives() throws Exception {
        JTabbedPane tp = new JTabbedPane();
        tp.addTab("only", new JLabel("o"));
        checkThrows(IndexOutOfBoundsException.class, () -> tp.getTitleAt(5), "getTitleAt(5)");
        checkThrows(IndexOutOfBoundsException.class, () -> tp.getTitleAt(-1), "getTitleAt(-1)");
        checkThrows(IndexOutOfBoundsException.class, () -> tp.setTitleAt(5, "x"), "setTitleAt(5)");
        checkThrows(IndexOutOfBoundsException.class, () -> tp.getComponentAt(5), "getComponentAt(5)");
        checkThrows(IndexOutOfBoundsException.class, () -> tp.setEnabledAt(5, false), "setEnabledAt(5)");
        checkThrows(IndexOutOfBoundsException.class, () -> tp.setSelectedIndex(5), "setSelectedIndex(5)");
        checkThrows(IndexOutOfBoundsException.class, () -> tp.setSelectedIndex(-2), "setSelectedIndex(-2)");
        checkThrows(IndexOutOfBoundsException.class, () -> tp.removeTabAt(5), "removeTabAt(5)");
        checkThrows(IndexOutOfBoundsException.class, () -> tp.setMnemonicAt(5, 'X'), "setMnemonicAt(5)");
        checkThrows(IndexOutOfBoundsException.class, () -> tp.setDisplayedMnemonicIndexAt(5, 0),
                "setDisplayedMnemonicIndexAt(5,0)");
        checkThrows(IllegalArgumentException.class, () -> tp.setTabPlacement(99), "setTabPlacement(99)");
        checkThrows(IllegalArgumentException.class, () -> tp.setTabLayoutPolicy(99), "setTabLayoutPolicy(99)");
        checkThrows(IllegalArgumentException.class, () -> tp.setDisplayedMnemonicIndexAt(0, 99),
                "setDisplayedMnemonicIndexAt(0,99)");
        tp.setSelectedIndex(-1);
        check(tp.getSelectedIndex() == -1, "setSelectedIndex(-1) clears selection");
    }

    public static void main(String[] args) throws Exception {
        try {
            UIManager.setLookAndFeel(new MetalLookAndFeel());
        } catch (Exception e) {
            throw new AssertionError("cannot install Metal LaF", e);
        }
        runOnEDT(JTabbedPaneTest::testWhiteBox);
        runOnEDT(JTabbedPaneTest::testBlackBox);
        runOnEDT(JTabbedPaneTest::testContractDefaults);
        runOnEDT(JTabbedPaneTest::testContractSelection);
        runOnEDT(JTabbedPaneTest::testContractNegatives);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
