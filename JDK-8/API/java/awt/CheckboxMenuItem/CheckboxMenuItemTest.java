/*
 * @test
 * @key headful
 * @summary java.awt.CheckboxMenuItem conformance: class reflection.
 * @run main CheckboxMenuItemTest
 */

import java.awt.*;

public class CheckboxMenuItemTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
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
        Class<?> c = Class.forName("java.awt.CheckboxMenuItem");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.MenuItem"), "superclass java.awt.MenuItem");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.ItemSelectable", "javax.accessibility.Accessible"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "addNotify"), "method addNotify");
        check(hasMethod(c, "getState"), "method getState");
        check(hasMethod(c, "setState"), "method setState");
        check(hasMethod(c, "getSelectedObjects"), "method getSelectedObjects");
        check(hasMethod(c, "addItemListener"), "method addItemListener");
        check(hasMethod(c, "removeItemListener"), "method removeItemListener");
        check(hasMethod(c, "getItemListeners"), "method getItemListeners");
        check(hasMethod(c, "getListeners"), "method getListeners");
        check(hasMethod(c, "paramString"), "method paramString");
        check(hasMethod(c, "getAccessibleContext"), "method getAccessibleContext");
        testContractCheckboxMenuItem();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
