/*
 * @test
 * @key headful
 * @summary java.awt.Button conformance: class reflection.
 * @run main ButtonTest
 */

import java.awt.*;

public class ButtonTest {
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
    static void testContractButton() throws Exception {
        // label null semantics: the null is retained, it is not normalized to ""
        Button b0 = new Button();
        check("".equals(b0.getLabel()), "Button(): default label is the empty string");
        check(new Button(null).getLabel() == null, "Button(null): null label retained");
        b0.setLabel("x");
        b0.setLabel(null);
        check(b0.getLabel() == null, "setLabel(null) retains null");

        // action command: defaults to label, explicit value wins, null falls back to label
        Button b1 = new Button("Go");
        check("Go".equals(b1.getActionCommand()), "action command defaults to label");
        b1.setActionCommand("run");
        check("run".equals(b1.getActionCommand()), "setActionCommand/getActionCommand roundtrip");
        b1.setLabel("Stop");
        check("run".equals(b1.getActionCommand()), "explicit action command independent of label");
        b1.setActionCommand(null);
        check("Stop".equals(b1.getActionCommand()), "setActionCommand(null) falls back to label");

        // listener registry: duplicates allowed, remove removes one instance, null tolerated
        java.awt.event.ActionListener l1 = e -> { }, l2 = e -> { };
        Button b2 = new Button("L");
        b2.addActionListener(l1); b2.addActionListener(l1); b2.addActionListener(l2);
        check(b2.getActionListeners().length == 3, "duplicate listeners all registered");
        b2.removeActionListener(l1);
        check(b2.getActionListeners().length == 2, "removeActionListener removes a single instance");
        b2.addActionListener(null);
        check(b2.getActionListeners().length == 2, "addActionListener(null) is a no-op");
        b2.removeActionListener(null);
        check(b2.getActionListeners().length == 2, "removeActionListener(null) is a no-op");
        check(b2.getListeners(java.awt.event.ActionListener.class).length == 2,
              "getListeners(ActionListener) reflects registry");
        b2.removeActionListener(l1); b2.removeActionListener(l2);
        check(b2.getActionListeners().length == 0, "registry empties");

        // enable/disable cycle
        check(b2.isEnabled(), "enabled by default");
        b2.setEnabled(false);
        check(!b2.isEnabled(), "setEnabled(false)");
        b2.setEnabled(true);
        check(b2.isEnabled(), "setEnabled(true)");

        // programmatic mutations never dispatch ActionEvent without a peer
        boolean[] fired = { false };
        Button b3 = new Button("NoEvent");
        b3.addActionListener(e -> fired[0] = true);
        b3.setLabel("changed");
        b3.setEnabled(false);
        java.awt.EventQueue.invokeAndWait(() -> { });
        check(!fired[0], "no ActionEvent from programmatic label/enable changes");
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.awt.Button");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.Component"), "superclass java.awt.Component");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"javax.accessibility.Accessible"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "addNotify"), "method addNotify");
        check(hasMethod(c, "getLabel"), "method getLabel");
        check(hasMethod(c, "setLabel"), "method setLabel");
        check(hasMethod(c, "setActionCommand"), "method setActionCommand");
        check(hasMethod(c, "getActionCommand"), "method getActionCommand");
        check(hasMethod(c, "addActionListener"), "method addActionListener");
        check(hasMethod(c, "removeActionListener"), "method removeActionListener");
        check(hasMethod(c, "getActionListeners"), "method getActionListeners");
        check(hasMethod(c, "getListeners"), "method getListeners");
        check(hasMethod(c, "getAccessibleContext"), "method getAccessibleContext");
        testContractButton();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
