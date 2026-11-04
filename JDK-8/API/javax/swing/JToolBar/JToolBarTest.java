/*
 * @test
 * @summary javax.swing.JToolBar conformance: type shape via reflection + component-layer
 *          black-box contracts (defaults, ctor matrix, add(Action) sync, separators,
 *          indexed child access, orientation negative, floatable/rollover/margin).
 * @run main JToolBarTest
 */

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class JToolBarTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void checkEquals(Object expect, Object actual, String m) {
        check(expect == null ? actual == null : expect.equals(actual),
              m + ": expected " + expect + " but got " + actual);
    }

    static void checkThrows(final Class<? extends Throwable> type, final String what, final Runnable code) {
        try {
            code.run();
        } catch (Throwable t) {
            check(type.isInstance(t),
                  what + ": expected " + type.getSimpleName() + " but threw " + t.getClass().getSimpleName());
            return;
        }
        throw new AssertionError(what + ": expected " + type.getSimpleName() + " but no exception thrown");
    }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    static Action action(final String name, final boolean[] fired) {
        return new AbstractAction(name) {
            public void actionPerformed(ActionEvent e) { if (fired != null) fired[0] = true; }
        };
    }

    public static void main(String[] args) throws Exception {
        final Class<?> c = Class.forName("javax.swing.JToolBar", false, JToolBarTest.class.getClassLoader());
        EventQueue.invokeAndWait(new Runnable() { public void run() { testShape(c); }});
        UIManager.setLookAndFeel(new MetalLookAndFeel());
        EventQueue.invokeAndWait(new Runnable() { public void run() {
            testDefaultsAndCtors();
            testAddActionSync();
            testChildrenAndSeparators();
            testPropertiesAndOrientation();
        }});
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- type shape (reflection, no construction) ----
    static void testShape(Class<?> c) {
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent"), "superclass");
        check(hasInterface(c, "javax.swing.SwingConstants"), "implements javax.swing.SwingConstants");
        check(hasInterface(c, "javax.accessibility.Accessible"), "implements javax.accessibility.Accessible");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getUI"), "method getUI");
        check(names.contains("setUI"), "method setUI");
        check(names.contains("updateUI"), "method updateUI");
        check(names.contains("getUIClassID"), "method getUIClassID");
    }

    // ---- defaults + ctor matrix ----
    static void testDefaultsAndCtors() {
        JToolBar tb = new JToolBar();
        checkEquals(SwingConstants.HORIZONTAL, tb.getOrientation(), "default orientation HORIZONTAL");
        check(tb.isFloatable(), "default floatable true");
        check(!tb.isRollover(), "default rollover false (Metal)");
        checkEquals(new Insets(0, 0, 0, 0), tb.getMargin(), "default margin zero (Metal)");
        checkEquals(0, tb.getComponentCount(), "default child count 0");
        check(tb.getUI() != null && tb.getUI().getClass().getName().endsWith("MetalToolBarUI"),
              "Metal binding installs MetalToolBarUI");
        checkEquals("ToolBarUI", tb.getUIClassID(), "getUIClassID ToolBarUI");
        check(tb.getAccessibleContext() != null, "getAccessibleContext non-null");

        checkEquals(SwingConstants.VERTICAL, new JToolBar(SwingConstants.VERTICAL).getOrientation(),
              "VERTICAL ctor");
        JToolBar named = new JToolBar("tbname");
        checkEquals("tbname", named.getName(), "name ctor sets name");
        JToolBar vname = new JToolBar("vname", SwingConstants.VERTICAL);
        checkEquals(SwingConstants.VERTICAL, vname.getOrientation(), "name+orientation ctor orientation");
        checkEquals("vname", vname.getName(), "name+orientation ctor name");
    }

    // ---- add(Action) wiring: text sync and enabled propagation ----
    static void testAddActionSync() {
        JToolBar tb = new JToolBar();
        final boolean[] fired = new boolean[1];
        Action act = action("TB", fired);
        JButton added = tb.add(act);
        check(added != null, "add(Action) returns button");
        check(added instanceof JButton, "returned component is a JButton");
        checkEquals(1, tb.getComponentCount(), "toolbar gained one child");
        check(tb.getComponentAtIndex(0) == added, "added button at index 0");
        checkEquals("TB", added.getText(), "button text synced from action name");
        check(added.isEnabled(), "button enabled while action enabled");
        check(act == added.getAction(), "button action is the given action");

        act.setEnabled(false);
        check(!added.isEnabled(), "action disable propagates to button");
        act.setEnabled(true);
        check(added.isEnabled(), "action re-enable propagates to button");

        added.doClick();
        check(fired[0], "button click dispatches through the action");
    }

    // ---- indexed child access + separators ----
    static void testChildrenAndSeparators() {
        JToolBar tb = new JToolBar();
        JButton b1 = new JButton("1");
        JButton b2 = new JButton("2");
        tb.add(b1);
        tb.add(b2);
        checkEquals(2, tb.getComponentCount(), "two buttons added");
        checkEquals(0, tb.getComponentIndex(b1), "index of b1");
        checkEquals(1, tb.getComponentIndex(b2), "index of b2");
        check(tb.getComponentAtIndex(0) == b1, "getComponentAtIndex(0)");
        check(tb.getComponentAtIndex(1) == b2, "getComponentAtIndex(1)");
        check(tb.getComponentAtIndex(2) == null, "getComponentAtIndex(count) null");
        check(tb.getComponentAtIndex(99) == null, "getComponentAtIndex(99) null");
        check(tb.getComponentAtIndex(-1) == null, "getComponentAtIndex(-1) null");
        JButton alien = new JButton("x");
        checkEquals(-1, tb.getComponentIndex(alien), "index of foreign button -1");

        int before = tb.getComponentCount();
        tb.addSeparator();
        checkEquals(before + 1, tb.getComponentCount(), "addSeparator() adds one child");
        java.awt.Component sep = tb.getComponent(tb.getComponentCount() - 1);
        checkEquals("javax.swing.JToolBar$Separator", sep.getClass().getName(),
              "separator component class");
        tb.addSeparator(new Dimension(11, 22));
        checkEquals(before + 2, tb.getComponentCount(), "addSeparator(dim) adds one child");

        tb.remove(b1);
        checkEquals(before + 1, tb.getComponentCount(), "remove drops one child");
        checkEquals(0, tb.getComponentIndex(b2), "remaining button reindexed to 0");
        tb.removeAll();
        checkEquals(0, tb.getComponentCount(), "removeAll empties toolbar");
    }

    // ---- floatable/rollover/margin + orientation negative ----
    static void testPropertiesAndOrientation() {
        JToolBar tb = new JToolBar();
        tb.setFloatable(false);
        check(!tb.isFloatable(), "setFloatable(false)");
        tb.setFloatable(true);
        check(tb.isFloatable(), "setFloatable(true) restore");

        tb.setRollover(true);
        check(tb.isRollover(), "setRollover(true)");
        tb.setRollover(false);
        check(!tb.isRollover(), "setRollover(false) restore");

        Insets m = new Insets(1, 2, 3, 4);
        tb.setMargin(m);
        checkEquals(m, tb.getMargin(), "setMargin roundtrip");

        tb.setOrientation(SwingConstants.VERTICAL);
        checkEquals(SwingConstants.VERTICAL, tb.getOrientation(), "setOrientation(VERTICAL)");
        checkEquals(1, SwingConstants.VERTICAL, "VERTICAL is 1");
        checkEquals(0, SwingConstants.HORIZONTAL, "HORIZONTAL is 0");
        checkThrows(IllegalArgumentException.class, "setOrientation(99)",
            new Runnable() { public void run() { tb.setOrientation(99); }});
        checkEquals(SwingConstants.VERTICAL, tb.getOrientation(), "orientation kept after rejection");
        checkThrows(IllegalArgumentException.class, "setOrientation(-5)",
            new Runnable() { public void run() { tb.setOrientation(-5); }});
        checkEquals(SwingConstants.VERTICAL, tb.getOrientation(), "orientation kept after second rejection");
    }
}
