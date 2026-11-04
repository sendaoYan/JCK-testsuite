/*
 * @test
 * @summary java.awt.Button conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main ButtonTest
 */

import java.awt.*;
import java.awt.event.ActionListener;
import java.lang.reflect.*;

public class ButtonTest {
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
        Class<?> c = Class.forName("java.awt.Button");
        check(c != null, "type loads: java.awt.Button");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(!Modifier.isAbstract(m), "not abstract");
        check(c.getSuperclass() == Component.class, "extends Component");
        boolean accessible = false;
        for (Class<?> i : c.getInterfaces()) if (i == javax.accessibility.Accessible.class) accessible = true;
        check(accessible, "implements Accessible");
        for (String name : new String[]{"getLabel", "setLabel", "setActionCommand",
                "getActionCommand", "addActionListener", "removeActionListener", "getActionListeners"}) {
            check(hasMethod(c, name), "has " + name);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Button b = new Button("press");
        check("press".equals(b.getLabel()), "constructor label");
        b.setLabel("pressed");
        check("pressed".equals(b.getLabel()), "setLabel/getLabel");

        check("pressed".equals(b.getActionCommand()), "action command defaults to label");
        b.setActionCommand("cmd");
        check("cmd".equals(b.getActionCommand()), "setActionCommand/getActionCommand");

        ActionListener l = e -> {};
        b.addActionListener(l);
        check(b.getActionListeners().length == 1 && b.getActionListeners()[0] == l,
              "addActionListener/getActionListeners");
        b.removeActionListener(l);
        check(b.getActionListeners().length == 0, "removeActionListener");

        Frame f = new Frame("ButtonTest");
        f.setLayout(null);
        f.setSize(200, 200);
        f.setVisible(true);
        try {
            f.add(b);
            check(b.isDisplayable(), "displayable after add");
            check(b.isShowing(), "showing after add");
            b.setEnabled(false);
            check(!b.isEnabled(), "setEnabled(false)");
            b.setEnabled(true);
            check(b.isEnabled(), "setEnabled(true)");
            b.setBackground(Color.ORANGE);
            check(Color.ORANGE.equals(b.getBackground()), "setBackground/getBackground");
            b.setBounds(10, 20, 80, 30);
            check(b.getBounds().equals(new Rectangle(10, 20, 80, 30)), "setBounds/getBounds");
        } finally {
            f.dispose();
        }
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
        testWhiteBox();
        testBlackBox();
        testContractButton();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
