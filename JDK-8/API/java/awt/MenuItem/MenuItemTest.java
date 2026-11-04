/*
 * @test
 * @key headful
 * @summary java.awt.MenuItem conformance: class reflection.
 * @run main MenuItemTest
 */

import java.awt.*;

public class MenuItemTest {
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
    static void testContractMenuShortcut() {
        java.awt.MenuShortcut plain = new java.awt.MenuShortcut(java.awt.event.KeyEvent.VK_A);
        java.awt.MenuShortcut plain2 = new java.awt.MenuShortcut(java.awt.event.KeyEvent.VK_A);
        java.awt.MenuShortcut shift = new java.awt.MenuShortcut(java.awt.event.KeyEvent.VK_A, true);
        check(plain.getKey() == java.awt.event.KeyEvent.VK_A, "getKey");
        check(!plain.usesShiftModifier() && shift.usesShiftModifier(), "usesShiftModifier");
        check(plain.equals(plain2) && plain2.equals(plain), "equals symmetric for equal shortcuts");
        check(plain.hashCode() == plain2.hashCode(), "hashCode consistent with equals");
        check(!plain.equals(shift) && !shift.equals(plain), "shift modifier distinguishes");
        check(!plain.equals(null) && !plain.equals("x"), "equals foreign and null false");
        check(plain.toString().contains(java.awt.event.KeyEvent.getKeyText(java.awt.event.KeyEvent.VK_A)),
              "toString carries the key");
        check(shift.toString().contains("Shift"), "toString carries the Shift modifier");
    }

    static void testContractMenuItem() throws Exception {
        MenuItem mi = new MenuItem();
        check("".equals(mi.getLabel()), "MenuItem() default empty label");
        check(new MenuItem(null).getLabel() == null, "MenuItem(null) retains null");
        check(new MenuItem("x", null).getShortcut() == null, "MenuItem(String,null) has no shortcut");
        mi.setLabel(null);
        check(mi.getLabel() == null, "setLabel(null) retains null");
        // action command: defaults to label, explicit wins, null falls back
        MenuItem m2 = new MenuItem("Cut");
        check("Cut".equals(m2.getActionCommand()), "action command defaults to label");
        m2.setActionCommand("cut2");
        check("cut2".equals(m2.getActionCommand()), "explicit action command");
        m2.setLabel("Copy");
        check("cut2".equals(m2.getActionCommand()), "command independent of label");
        m2.setActionCommand(null);
        check("Copy".equals(m2.getActionCommand()), "setActionCommand(null) falls back to label");
        // deprecated enable/disable aliases
        m2.disable();
        check(!m2.isEnabled(), "disable()");
        m2.enable();
        check(m2.isEnabled(), "enable()");
        m2.enable(false);
        check(!m2.isEnabled(), "enable(false)");
        m2.enable(true);
        check(m2.isEnabled(), "enable(true)");
        // shortcut lifecycle
        java.awt.MenuShortcut sc = new java.awt.MenuShortcut(java.awt.event.KeyEvent.VK_N);
        m2.setShortcut(sc);
        check(m2.getShortcut() == sc && sc.getKey() == java.awt.event.KeyEvent.VK_N, "setShortcut/getShortcut");
        m2.deleteShortcut();
        check(m2.getShortcut() == null, "deleteShortcut");
        // listener registry with duplicates
        java.awt.event.ActionListener l = e -> { };
        m2.addActionListener(l); m2.addActionListener(l);
        check(m2.getActionListeners().length == 2, "duplicate ActionListeners registered");
        m2.removeActionListener(l);
        check(m2.getActionListeners().length == 1, "removeActionListener removes one instance");
        check(m2.getListeners(java.awt.event.ActionListener.class).length == 1, "getListeners agrees");
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.awt.MenuItem");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.MenuComponent"), "superclass java.awt.MenuComponent");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"javax.accessibility.Accessible"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "addNotify"), "method addNotify");
        check(hasMethod(c, "getLabel"), "method getLabel");
        check(hasMethod(c, "setLabel"), "method setLabel");
        check(hasMethod(c, "isEnabled"), "method isEnabled");
        check(hasMethod(c, "setEnabled"), "method setEnabled");
        check(hasMethod(c, "enable"), "method enable");
        check(hasMethod(c, "disable"), "method disable");
        check(hasMethod(c, "getShortcut"), "method getShortcut");
        check(hasMethod(c, "setShortcut"), "method setShortcut");
        check(hasMethod(c, "deleteShortcut"), "method deleteShortcut");
        check(hasMethod(c, "setActionCommand"), "method setActionCommand");
        check(hasMethod(c, "getActionCommand"), "method getActionCommand");
        check(hasMethod(c, "addActionListener"), "method addActionListener");
        check(hasMethod(c, "removeActionListener"), "method removeActionListener");
        check(hasMethod(c, "getActionListeners"), "method getActionListeners");
        check(hasMethod(c, "getListeners"), "method getListeners");
        check(hasMethod(c, "paramString"), "method paramString");
        check(hasMethod(c, "getAccessibleContext"), "method getAccessibleContext");
        testContractMenuShortcut();
        testContractMenuItem();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
