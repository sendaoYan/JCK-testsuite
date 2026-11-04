/*
 * @test
 * @summary javax.swing.JPopupMenu conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main JPopupMenuTest
 */

import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Insets;
import java.lang.reflect.Modifier;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuListener;

public class JPopupMenuTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.JPopupMenu");
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
        check(c.getInterfaces()[0].getName().equals("javax.accessibility.Accessible"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("javax.swing.MenuElement"), "interface 1");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // constructors + label
        JPopupMenu p = new JPopupMenu();
        check(p.getLabel() == null, "default label null");
        JPopupMenu titled = new JPopupMenu("Title");
        check("Title".equals(titled.getLabel()), "JPopupMenu(String) stores label");
        titled.setLabel("NewTitle");
        check("NewTitle".equals(titled.getLabel()), "setLabel reflected");
        titled.setLabel(null);
        check(titled.getLabel() == null, "setLabel(null) clears label");
        check("PopupMenuUI".equals(p.getUIClassID()), "UIClassID is PopupMenuUI");

        // add / insert / remove / component count
        check(p.getComponentCount() == 0, "new popup has 0 components");
        JMenuItem a = new JMenuItem("A");
        check(p.add(a) == a, "add(JMenuItem) returns the item");
        check(p.getComponentCount() == 1, "count 1 after add(JMenuItem)");
        JMenuItem b = p.add("B");
        check("B".equals(b.getText()), "add(String) returns JMenuItem with text");
        check(p.getComponentCount() == 2, "count 2 after add(String)");
        p.addSeparator();
        check(p.getComponentCount() == 3, "addSeparator increments count");
        JMenuItem c = new JMenuItem("C");
        p.insert(c, 0);
        check(p.getComponentCount() == 4, "insert increments count");
        check(p.getComponent(0) == c, "insert(0) places component at head");
        check(p.getComponentAtIndex(0) == c, "getComponentAtIndex(0) is head item");
        check(p.getComponentIndex(a) == 1, "getComponentIndex(a) is 1 after head insert");
        check(p.getComponentIndex(new JMenuItem("ghost")) == -1, "getComponentIndex unknown is -1");

        // insert beyond size appends
        JMenuItem d = new JMenuItem("D");
        p.insert(d, 99);
        check(p.getComponentCount() == 5, "insert beyond size increments count");
        check(p.getComponentIndex(d) == 4, "insert beyond size appends at end");

        // remove + index-out-of-range exceptions
        p.remove(0);
        check(p.getComponentCount() == 4, "remove(0) decrements count");
        boolean negRemoveThrew = false;
        try { p.remove(-1); } catch (IllegalArgumentException e) { negRemoveThrew = true; }
        check(negRemoveThrew, "remove(-1) throws IllegalArgumentException");
        boolean bigRemoveThrew = false;
        try { p.remove(999); } catch (IllegalArgumentException e) { bigRemoveThrew = true; }
        check(bigRemoveThrew, "remove beyond size throws IllegalArgumentException");

        // lightweight popup enabled (instance + static default)
        check(p.isLightWeightPopupEnabled(), "lightWeightPopupEnabled defaults true");
        p.setLightWeightPopupEnabled(false);
        check(!p.isLightWeightPopupEnabled(), "setLightWeightPopupEnabled(false) reflected");
        p.setLightWeightPopupEnabled(true);
        check(p.isLightWeightPopupEnabled(), "setLightWeightPopupEnabled(true) reflected");
        check(JPopupMenu.getDefaultLightWeightPopupEnabled(), "static default lightweight popup is true");

        // selection model + setSelected
        check(p.getSelectionModel() != null, "selection model non-null");
        p.setSelected(a);
        check(p.getSelectionModel().getSelectedIndex() == p.getComponentIndex(a), "setSelected selects the item index");
        p.getSelectionModel().clearSelection();
        check(p.getSelectionModel().getSelectedIndex() == -1, "clearSelection resets index");

        // borderPainted / margin round-trips
        check(p.isBorderPainted(), "borderPainted defaults true");
        p.setBorderPainted(false);
        check(!p.isBorderPainted(), "setBorderPainted(false) reflected");
        Insets zero = new Insets(0, 0, 0, 0);
        check(zero.equals(p.getMargin()), "default margin is zero insets");

        // popup size round-trip through preferred size
        p.setPopupSize(100, 200);
        check(p.getPreferredSize().width == 100 && p.getPreferredSize().height == 200, "setPopupSize reflected in preferred size");

        // MenuElement plumbing
        check(p.getComponent() == p, "MenuElement.getComponent returns popup");
        JPopupMenu me = new JPopupMenu();
        JMenuItem m1 = new JMenuItem("M1");
        JMenuItem m2 = new JMenuItem("M2");
        me.add(m1);
        me.add(m2);
        check(me.getSubElements().length == 2, "getSubElements lists MenuElement children");
        check(me.getSubElements()[0] == m1, "getSubElements[0] is first item");
        check(me.getSubElements()[1] == m2, "getSubElements[1] is second item");
        me.addSeparator();
        check(me.getSubElements().length == 2, "separator (JSeparator) is not a MenuElement and is excluded");

        // invoker round-trip
        JPopupMenu q = new JPopupMenu();
        check(q.getInvoker() == null, "invoker null by default");
        JPanel invoker = new JPanel();
        q.setInvoker(invoker);
        check(q.getInvoker() == invoker, "setInvoker reflected");

        // popup menu listener round-trip (baseline-relative; UI installs one)
        int popupListenerBase = q.getPopupMenuListeners().length;
        PopupMenuListener pl = new PopupMenuListener() {
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {}
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) {}
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {}
        };
        q.addPopupMenuListener(pl);
        check(q.getPopupMenuListeners().length == popupListenerBase + 1, "addPopupMenuListener reflected");
        q.removePopupMenuListener(pl);
        check(q.getPopupMenuListeners().length == popupListenerBase, "removePopupMenuListener reflected");

        // pack is a no-op when not shown
        q.add("One");
        q.pack();
        check(true, "pack() completes when not shown");

        // show / isVisible / hide with a real invoker (headful)
        JFrame frame = new JFrame("Popup");
        JPopupMenu popup = new JPopupMenu("Context");
        try {
            frame.add(invoker);
            frame.setSize(300, 200);
            frame.setVisible(true);
            popup.add("One");
            popup.add("Two");
            popup.show(invoker, 5, 5);
            check(popup.isVisible(), "popup visible after show(invoker,x,y)");
            check(popup.getInvoker() == invoker, "show sets invoker");
            popup.setVisible(false);
            check(!popup.isVisible(), "popup hidden after setVisible(false)");
            popup.pack();
            check(true, "pack() completes after hide");
        } finally {
            frame.dispose();
        }
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

    public static void main(String[] args) throws Exception {
        runOnEDT(JPopupMenuTest::testWhiteBox);
        runOnEDT(JPopupMenuTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
