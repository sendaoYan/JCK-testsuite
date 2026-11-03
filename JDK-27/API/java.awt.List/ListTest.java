/*
 * @test
 * @summary java.awt.List conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main ListTest
 */

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.lang.reflect.*;

public class ListTest {
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
        Class<?> c = Class.forName("java.awt.List");
        check(c != null, "type loads: java.awt.List");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(!Modifier.isAbstract(m), "not abstract");
        check(c.getSuperclass() == Component.class, "extends Component");
        boolean itemSelectable = false;
        for (Class<?> i : c.getInterfaces()) if (i == ItemSelectable.class) itemSelectable = true;
        check(itemSelectable, "implements ItemSelectable");
        for (String name : new String[]{"getItemCount", "getItems", "add", "addItem",
                "replaceItem", "remove", "removeAll", "select", "deselect", "getSelectedIndex",
                "getSelectedIndexes", "getSelectedItem", "getSelectedItems", "setMultipleMode",
                "isMultipleMode", "getRows", "makeVisible", "getVisibleIndex"}) {
            check(hasMethod(c, name), "has " + name);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        List l = new List();
        check(l.getRows() == 4, "default rows 4");
        check(!l.isMultipleMode(), "single select default");
        check(l.getItemCount() == 0, "initial empty");
        check(l.getSelectedIndex() == -1, "no selection initially");

        List l2 = new List(3);
        check(l2.getRows() == 3, "constructor rows");
        List l3 = new List(2, true);
        check(l3.getRows() == 2 && l3.isMultipleMode(), "constructor rows+multiple");

        l.add("a");
        l.addItem("b");
        l.add("c", 1); // insert at 1
        check(l.getItemCount() == 3, "add/addItem/insert count");
        check("a".equals(l.getItem(0)) && "c".equals(l.getItem(1)) && "b".equals(l.getItem(2)),
              "item order");
        String[] items = l.getItems();
        check(items.length == 3 && "c".equals(items[1]), "getItems order");

        l.replaceItem("C", 1);
        check("C".equals(l.getItem(1)), "replaceItem");

        l.select(0);
        check(l.getSelectedIndex() == 0, "select(index)");
        check("a".equals(l.getSelectedItem()), "getSelectedItem");
        l.deselect(0);
        check(l.getSelectedIndex() == -1, "deselect clears");

        l.remove("b");
        check(l.getItemCount() == 2, "remove(string)");
        l.remove(0);
        check(l.getItemCount() == 1 && "C".equals(l.getItem(0)), "remove(int)");
        l.removeAll();
        check(l.getItemCount() == 0, "removeAll");

        // multiple mode
        l.setMultipleMode(true);
        check(l.isMultipleMode(), "setMultipleMode(true)");
        l.add("x"); l.add("y"); l.add("z");
        l.select(0); l.select(2);
        int[] sel = l.getSelectedIndexes();
        check(sel.length == 2 && sel[0] == 0 && sel[1] == 2, "multiple selection indexes");
        String[] selItems = l.getSelectedItems();
        check(selItems.length == 2 && "x".equals(selItems[0]) && "z".equals(selItems[1]),
              "multiple selection items");
        l.setMultipleMode(false);
        check(!l.isMultipleMode(), "setMultipleMode(false)");

        // visibility / listeners
        l.makeVisible(2);
        check(l.getVisibleIndex() == 2, "makeVisible/getVisibleIndex");
        ItemListener il = e -> {};
        l.addItemListener(il);
        check(l.getItemListeners().length == 1, "addItemListener");
        l.removeItemListener(il);
        ActionListener al = e -> {};
        l.addActionListener(al);
        check(l.getActionListeners().length == 1, "addActionListener");
        l.removeActionListener(al);

        Frame f = new Frame("ListTest");
        f.setSize(150, 150);
        f.setVisible(true);
        try {
            f.add(l);
            check(l.isDisplayable(), "displayable after add");
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
