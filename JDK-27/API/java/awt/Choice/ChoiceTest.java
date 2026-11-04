/*
 * @test
 * @summary java.awt.Choice conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main ChoiceTest
 */

import java.awt.*;
import java.awt.event.ItemListener;
import java.lang.reflect.*;

public class ChoiceTest {
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
        Class<?> c = Class.forName("java.awt.Choice");
        check(c != null, "type loads: java.awt.Choice");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(!Modifier.isAbstract(m), "not abstract");
        check(c.getSuperclass() == Component.class, "extends Component");
        boolean itemSelectable = false;
        for (Class<?> i : c.getInterfaces()) if (i == ItemSelectable.class) itemSelectable = true;
        check(itemSelectable, "implements ItemSelectable");
        for (String name : new String[]{"getItemCount", "getItem", "add", "addItem", "insert",
                "remove", "removeAll", "getSelectedItem", "getSelectedIndex", "select",
                "addItemListener", "removeItemListener", "getItemListeners"}) {
            check(hasMethod(c, name), "has " + name);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Choice c = new Choice();
        check(c.getItemCount() == 0, "initial empty");
        check(c.getSelectedIndex() == -1, "no selection initially");
        check(c.getSelectedItem() == null, "selected item null initially");

        c.add("one");
        c.addItem("two");
        c.insert("zero", 0);
        check(c.getItemCount() == 3, "add/addItem/insert count");
        check("zero".equals(c.getItem(0)), "insert at 0");
        check("one".equals(c.getItem(1)) && "two".equals(c.getItem(2)), "item order");

        c.select(1);
        check(c.getSelectedIndex() == 1, "select(index)");
        check("one".equals(c.getSelectedItem()), "getSelectedItem");
        c.select("two");
        check(c.getSelectedIndex() == 2, "select(string)");
        check(c.getSelectedObjects() != null && c.getSelectedObjects().length == 1
                && "two".equals(c.getSelectedObjects()[0]), "getSelectedObjects");

        c.remove("two");
        check(c.getItemCount() == 2, "remove(string)");
        c.remove(0);
        check(c.getItemCount() == 1 && "one".equals(c.getItem(0)), "remove(int)");
        c.removeAll();
        check(c.getItemCount() == 0, "removeAll");
        check(c.getSelectedIndex() == -1, "selection cleared after removeAll");

        ItemListener l = e -> {};
        c.addItemListener(l);
        check(c.getItemListeners().length == 1 && c.getItemListeners()[0] == l,
              "addItemListener/getItemListeners");
        c.removeItemListener(l);
        check(c.getItemListeners().length == 0, "removeItemListener");

        Frame f = new Frame("ChoiceTest");
        f.setSize(150, 150);
        f.setVisible(true);
        try {
            f.add(c);
            check(c.isDisplayable(), "displayable after add");
        } finally {
            f.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
