/*
 * @test
 * @summary java.awt.MenuItem conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main MenuItemTest
 */

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.lang.reflect.*;

public class MenuItemTest {
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
        Class<?> c = Class.forName("java.awt.MenuItem");
        check(c != null, "type loads: java.awt.MenuItem");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(!Modifier.isAbstract(m), "not abstract");
        check(c.getSuperclass() == MenuComponent.class, "extends MenuComponent");
        boolean accessible = false;
        for (Class<?> i : c.getInterfaces()) if (i == javax.accessibility.Accessible.class) accessible = true;
        check(accessible, "implements Accessible");
        for (String name : new String[]{"getLabel", "setLabel", "isEnabled", "setEnabled",
                "enable", "disable", "getShortcut", "setShortcut", "deleteShortcut",
                "getActionCommand", "setActionCommand", "addActionListener",
                "removeActionListener", "getActionListeners"}) {
            check(hasMethod(c, name), "has " + name);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        MenuItem mi = new MenuItem();
        check("".equals(mi.getLabel()), "default empty label");
        check(mi.isEnabled(), "enabled default true");

        MenuItem mi2 = new MenuItem("open");
        check("open".equals(mi2.getLabel()), "constructor label");

        mi2.setLabel("close");
        check("close".equals(mi2.getLabel()), "setLabel/getLabel");
        mi2.setEnabled(false);
        check(!mi2.isEnabled(), "setEnabled(false)");
        mi2.enable();
        check(mi2.isEnabled(), "enable()");
        mi2.disable();
        check(!mi2.isEnabled(), "disable()");

        check("close".equals(mi2.getActionCommand()), "action command defaults to label");
        mi2.setActionCommand("cmd");
        check("cmd".equals(mi2.getActionCommand()), "setActionCommand/getActionCommand");

        // shortcut
        MenuShortcut sc = new MenuShortcut(KeyEvent.VK_O);
        check(sc.getKey() == KeyEvent.VK_O, "MenuShortcut getKey");
        check(!sc.usesShiftModifier(), "MenuShortcut default no shift");
        mi2.setShortcut(sc);
        check(mi2.getShortcut() == sc, "setShortcut/getShortcut");
        mi2.deleteShortcut();
        check(mi2.getShortcut() == null, "deleteShortcut");

        ActionListener l = e -> {};
        mi2.addActionListener(l);
        check(mi2.getActionListeners().length == 1 && mi2.getActionListeners()[0] == l,
              "addActionListener/getActionListeners");
        mi2.removeActionListener(l);
        check(mi2.getActionListeners().length == 0, "removeActionListener");
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
        testWhiteBox();
        testBlackBox();
        testContractMenuShortcut();
        testContractMenuItem();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
