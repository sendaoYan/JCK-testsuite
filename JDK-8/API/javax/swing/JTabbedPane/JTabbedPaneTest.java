/*
 * @test
 * @summary javax.swing.JTabbedPane conformance: type shape via reflection + tab model
 *          contract (add/insert/remove matrix, selection migration, mnemonics, reverse
 *          index/argument negatives; headless-safe, Metal LaF, EDT).
 * @run main JTabbedPaneTest
 */

import java.awt.Component;
import java.awt.Graphics;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class JTabbedPaneTest {
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
        Class<?> c = Class.forName("javax.swing.JTabbedPane", false, JTabbedPaneTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent"), "superclass");
        check(hasInterface(c, "java.io.Serializable"), "implements java.io.Serializable");
        check(hasInterface(c, "javax.accessibility.Accessible"), "implements javax.accessibility.Accessible");
        check(hasInterface(c, "javax.swing.SwingConstants"), "implements javax.swing.SwingConstants");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getUI"), "method getUI");
        check(names.contains("setUI"), "method setUI");
        check(names.contains("updateUI"), "method updateUI");
        check(names.contains("getUIClassID"), "method getUIClassID");
        check(names.contains("addChangeListener"), "method addChangeListener");
    }

    static final Icon ICON = new Icon() {
        public void paintIcon(Component c, Graphics g, int x, int y) {}
        public int getIconWidth() { return 3; }
        public int getIconHeight() { return 3; }
    };

    // ---- Part 2: defaults and constructors ----
    static void testDefaults() {
        JTabbedPane tp = new JTabbedPane();
        check(tp.getTabCount() == 0, "default tabCount 0");
        check(tp.getSelectedIndex() == -1, "default selectedIndex -1");
        check(tp.getTabPlacement() == SwingConstants.TOP, "default placement TOP");
        check(tp.getTabLayoutPolicy() == JTabbedPane.WRAP_TAB_LAYOUT, "default policy WRAP");
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
    }

    // ---- Part 3: add / query / mutate / remove ----
    static void testAddQueryRemove() {
        JTabbedPane tp = new JTabbedPane();
        JLabel cA = new JLabel("a");
        JLabel cB = new JLabel("b");
        tp.addTab("A", ICON, cA, "tipA");
        tp.addTab("B", null, cB, null);
        check(tp.getTabCount() == 2, "addTab count");
        check("A".equals(tp.getTitleAt(0)), "getTitleAt(0)");
        check(tp.getIconAt(0) == ICON, "getIconAt identity");
        check("tipA".equals(tp.getToolTipTextAt(0)), "getToolTipTextAt");
        check(tp.getComponentAt(0) == cA, "getComponentAt identity");
        check(tp.getComponentAt(1).getParent() == tp, "tab component parent is pane");
        check(tp.indexOfTab("B") == 1, "indexOfTab(String)");
        check(tp.indexOfTab(ICON) == 0, "indexOfTab(Icon)");
        check(tp.indexOfComponent(cB) == 1, "indexOfComponent");
        tp.add("C", new JLabel("c"));
        check(tp.getTabCount() == 3, "add(String,Component) adds tab");
        check("C".equals(tp.getTitleAt(2)), "add(String,Component) title");
        tp.setTitleAt(0, "A2");
        check("A2".equals(tp.getTitleAt(0)), "setTitleAt round-trip");
        tp.setToolTipTextAt(0, "tip2");
        check("tip2".equals(tp.getToolTipTextAt(0)), "setToolTipTextAt round-trip");
        Icon ic2 = new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {}
            public int getIconWidth() { return 1; }
            public int getIconHeight() { return 1; }
        };
        tp.setIconAt(0, ic2);
        check(tp.getIconAt(0) == ic2, "setIconAt round-trip");
        tp.setDisabledIconAt(0, ic2);
        check(tp.getDisabledIconAt(0) == ic2, "setDisabledIconAt round-trip");
        tp.setEnabledAt(0, false);
        check(tp.isEnabledAt(0) == false, "setEnabledAt(false)");
        tp.setEnabledAt(0, true);
        check(tp.isEnabledAt(1), "other tabs keep enabled state");
        tp.insertTab("ins", ICON, new JLabel("i"), "tipi", 0);
        check(tp.getTabCount() == 4, "insertTab count");
        check("ins".equals(tp.getTitleAt(0)), "insertTab at 0 title");
        check("A2".equals(tp.getTitleAt(1)), "insertTab shifts previous 0 to 1");
        tp.removeTabAt(0);
        check(tp.getTabCount() == 3, "removeTabAt count");
        check("A2".equals(tp.getTitleAt(0)), "removeTabAt shifts back");
        tp.removeAll();
        check(tp.getTabCount() == 0, "removeAll count 0");
        check(tp.getSelectedIndex() == -1, "selectedIndex -1 after removeAll");
        tp.addTab("again", new JLabel("g"));
        check(tp.getSelectedIndex() == 0, "first added tab becomes selected");
        // Mnemonics: valid title/mnemonic pair round-trips.
        JTabbedPane mp = new JTabbedPane();
        mp.addTab("Tab", new JLabel());
        check(mp.getMnemonicAt(0) == -1, "default mnemonic -1");
        mp.setMnemonicAt(0, 'T');
        check(mp.getMnemonicAt(0) == 'T', "setMnemonicAt round-trip");
        mp.setDisplayedMnemonicIndexAt(0, 0);
        check(mp.getDisplayedMnemonicIndexAt(0) == 0, "setDisplayedMnemonicIndexAt matching char");
        javax.swing.SingleSelectionModel m = new javax.swing.DefaultSingleSelectionModel();
        tp.setModel(m);
        check(tp.getModel() == m, "setModel round-trip");
    }

    // ---- Part 4: selection semantics and change events ----
    static void testSelectionAndChangeEvents() {
        final JTabbedPane tp = new JTabbedPane();
        tp.addTab("one", new JLabel("1"));
        tp.addTab("two", new JLabel("2"));
        tp.addTab("three", new JLabel("3"));
        check(tp.getSelectedIndex() == 0, "first tab auto-selected");
        final AtomicInteger fired = new AtomicInteger();
        tp.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) { fired.incrementAndGet(); }
        });
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
    }

    // ---- Part 5: reverse / negative matrix ----
    static void testNegatives() {
        final JTabbedPane tp = new JTabbedPane();
        tp.addTab("only", new JLabel("o"));
        checkThrows(IndexOutOfBoundsException.class, new Runnable() { public void run() { tp.getTitleAt(5); } },
                "getTitleAt(5)");
        checkThrows(IndexOutOfBoundsException.class, new Runnable() { public void run() { tp.getTitleAt(-1); } },
                "getTitleAt(-1)");
        checkThrows(IndexOutOfBoundsException.class, new Runnable() { public void run() { tp.setTitleAt(5, "x"); } },
                "setTitleAt(5)");
        checkThrows(IndexOutOfBoundsException.class, new Runnable() { public void run() { tp.getComponentAt(5); } },
                "getComponentAt(5)");
        checkThrows(IndexOutOfBoundsException.class, new Runnable() { public void run() { tp.setEnabledAt(5, false); } },
                "setEnabledAt(5)");
        checkThrows(IndexOutOfBoundsException.class, new Runnable() { public void run() { tp.setSelectedIndex(5); } },
                "setSelectedIndex(5)");
        checkThrows(IndexOutOfBoundsException.class, new Runnable() { public void run() { tp.setSelectedIndex(-2); } },
                "setSelectedIndex(-2)");
        checkThrows(IndexOutOfBoundsException.class, new Runnable() { public void run() { tp.removeTabAt(5); } },
                "removeTabAt(5)");
        checkThrows(IndexOutOfBoundsException.class, new Runnable() { public void run() { tp.setMnemonicAt(5, 'X'); } },
                "setMnemonicAt(5)");
        checkThrows(IndexOutOfBoundsException.class,
                new Runnable() { public void run() { tp.setDisplayedMnemonicIndexAt(5, 0); } },
                "setDisplayedMnemonicIndexAt(5,0)");
        checkThrows(IllegalArgumentException.class, new Runnable() { public void run() { tp.setTabPlacement(99); } },
                "setTabPlacement(99)");
        checkThrows(IllegalArgumentException.class, new Runnable() { public void run() { tp.setTabLayoutPolicy(99); } },
                "setTabLayoutPolicy(99)");
        checkThrows(IllegalArgumentException.class,
                new Runnable() { public void run() { tp.setDisplayedMnemonicIndexAt(0, 99); } },
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
        testShape();
        SwingUtilities.invokeAndWait(new Runnable() { public void run() { testDefaults(); } });
        SwingUtilities.invokeAndWait(new Runnable() { public void run() { testAddQueryRemove(); } });
        SwingUtilities.invokeAndWait(new Runnable() { public void run() { testSelectionAndChangeEvents(); } });
        SwingUtilities.invokeAndWait(new Runnable() { public void run() { testNegatives(); } });
        System.out.println("PASS (" + passed + " assertions)");
    }
}
