/*
 * @test
 * @summary java.awt.CheckboxMenuItem conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main CheckboxMenuItemTest
 */

import java.awt.*;
import java.awt.event.ItemListener;
import java.lang.reflect.*;

public class CheckboxMenuItemTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.CheckboxMenuItem");
        check(c != null, "type loads: java.awt.CheckboxMenuItem");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(!Modifier.isAbstract(m), "not abstract");
        check(c.getSuperclass() == MenuItem.class, "extends MenuItem");
        boolean itemSelectable = false;
        for (Class<?> i : c.getInterfaces()) if (i == ItemSelectable.class) itemSelectable = true;
        check(itemSelectable, "implements ItemSelectable");
        for (String name : new String[]{"getState", "setState", "getSelectedObjects",
                "addItemListener", "removeItemListener", "getItemListeners"}) {
            check(hasMethod(c, name), "has " + name);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        CheckboxMenuItem c = new CheckboxMenuItem();
        check("".equals(c.getLabel()), "default empty label");
        check(!c.getState(), "default state false");

        CheckboxMenuItem c2 = new CheckboxMenuItem("wrap", true);
        check("wrap".equals(c2.getLabel()), "constructor label");
        check(c2.getState(), "constructor state true");

        c2.setLabel("wrapped");
        check("wrapped".equals(c2.getLabel()), "inherited setLabel");
        c2.setEnabled(false);
        check(!c2.isEnabled(), "inherited setEnabled");
        c2.setEnabled(true);

        c2.setState(false);
        check(!c2.getState(), "setState(false)");
        check(c2.getSelectedObjects() == null, "getSelectedObjects null when off");
        c2.setState(true);
        check(c2.getState(), "setState(true)");
        check(c2.getSelectedObjects() != null && c2.getSelectedObjects().length == 1
                && "wrapped".equals(c2.getSelectedObjects()[0]), "getSelectedObjects when on");

        ItemListener l = e -> {};
        c2.addItemListener(l);
        check(c2.getItemListeners().length == 1 && c2.getItemListeners()[0] == l,
              "addItemListener/getItemListeners");
        c2.removeItemListener(l);
        check(c2.getItemListeners().length == 0, "removeItemListener");
    }

    // ---- shared helper for exact-exception assertions (deepening batch) ----
    interface ThrowingAction { void run() throws Exception; }

    static void expectExact(Class<? extends Throwable> expected, String tag, ThrowingAction action) {
        try {
            action.run();
            check(false, tag + ": expected " + expected.getSimpleName() + " but nothing was thrown");
        } catch (Throwable thrown) {
            check(thrown.getClass() == expected,
                  tag + ": expected " + expected.getName() + " but got " + thrown.getClass().getName());
        }
    }

    // ---- Part 3: contract deepening ----
    static void testContractCheckboxMenuItem() throws Exception {
        CheckboxMenuItem c0 = new CheckboxMenuItem(null);
        check(c0.getLabel() == null && !c0.getState(), "CheckboxMenuItem(null) retains null, off");
        CheckboxMenuItem c1 = new CheckboxMenuItem("w", true);
        check(c1.getState(), "CheckboxMenuItem(String,true)");
        c1.setState(false);
        check(!c1.getState(), "setState(false)");
        c1.setState(false);
        check(!c1.getState(), "setState idempotent");
        // programmatic setState never dispatches ItemEvent without a peer
        boolean[] ev = { false };
        c1.addItemListener(e -> ev[0] = true);
        c1.setState(true);
        java.awt.EventQueue.invokeAndWait(() -> { });
        check(!ev[0], "no ItemEvent from programmatic setState");
        check(c1.getSelectedObjects() != null && c1.getSelectedObjects().length == 1
              && "w".equals(c1.getSelectedObjects()[0]), "getSelectedObjects when on");
        c1.setState(false);
        check(c1.getSelectedObjects() == null, "getSelectedObjects null when off");
        // listener registry (on a fresh component: the no-event probe above
        // deliberately left its own listener registered)
        java.awt.event.ItemListener il = e -> { };
        CheckboxMenuItem reg = new CheckboxMenuItem("reg");
        reg.addItemListener(il); reg.addItemListener(il);
        check(reg.getItemListeners().length == 2, "duplicate ItemListeners registered");
        reg.removeItemListener(il);
        check(reg.getItemListeners().length == 1, "removeItemListener removes one instance");
        // inherited shortcut plumbing
        java.awt.MenuShortcut sc = new java.awt.MenuShortcut(java.awt.event.KeyEvent.VK_A, true);
        c1.setShortcut(sc);
        check(c1.getShortcut() == sc && sc.usesShiftModifier(), "setShortcut/getShortcut with shift");
        c1.deleteShortcut();
        check(c1.getShortcut() == null, "deleteShortcut");
        // toString carries the state
        c1.setState(true);
        check(c1.toString().contains("state=true"), "toString carries state=true");
        c1.setState(false);
        check(c1.toString().contains("state=false"), "toString carries state=false");
        // inherited action command
        check("w".equals(c1.getActionCommand()), "action command defaults to label");
        c1.setActionCommand("cmd");
        check("cmd".equals(c1.getActionCommand()), "setActionCommand/getActionCommand");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractCheckboxMenuItem();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
