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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
