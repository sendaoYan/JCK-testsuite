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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
