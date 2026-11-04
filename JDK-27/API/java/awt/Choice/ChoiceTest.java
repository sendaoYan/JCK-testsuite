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
    static void testContractChoice() throws Exception {
        Choice c = new Choice();
        check(c.getItemCount() == 0 && c.getSelectedIndex() == -1 && c.getSelectedItem() == null,
              "empty defaults: count 0, index -1, item null");
        // null item rejected at both add entry points
        expectExact(NullPointerException.class, "add(null)", () -> c.add((String) null));
        expectExact(NullPointerException.class, "addItem(null)", () -> c.addItem(null));
        c.add("a"); c.add("b"); c.add("c");
        check(c.getItemCount() == 3, "count after three adds");
        // first add auto-selects index 0
        check(c.getSelectedIndex() == 0 && "a".equals(c.getSelectedItem()), "first item auto-selected");
        check("b".equals(c.getItem(1)), "getItem order");
        expectExact(ArrayIndexOutOfBoundsException.class, "getItem(3)", () -> c.getItem(3));
        expectExact(ArrayIndexOutOfBoundsException.class, "getItem(-1)", () -> c.getItem(-1));
        // insert: negative rejected, at-count appends, beyond count appends
        expectExact(IllegalArgumentException.class, "insert(x,-1)", () -> c.insert("x", -1));
        c.insert("d", 3);
        check(c.getItemCount() == 4 && "d".equals(c.getItem(3)), "insert at count appends");
        c.insert("e", 100);
        check(c.getItemCount() == 5 && "e".equals(c.getItem(4)), "insert beyond count appends");
        c.insert("z", 0);
        check("z".equals(c.getItem(0)) && "a".equals(c.getItem(1)), "insert at 0 shifts tail");
        // remove(int) bounds
        expectExact(ArrayIndexOutOfBoundsException.class, "remove(99)", () -> c.remove(99));
        expectExact(ArrayIndexOutOfBoundsException.class, "remove(-1)", () -> c.remove(-1));
        // remove(String): by equals; missing or null item -> IllegalArgumentException (not NoSuchElementException)
        expectExact(IllegalArgumentException.class, "remove(String missing)", () -> c.remove("nope"));
        expectExact(IllegalArgumentException.class, "remove(String null)", () -> c.remove((String) null));
        c.remove(new String("z"));
        check(c.getItemCount() == 5 && "a".equals(c.getItem(0)), "remove by equals");
        // select(int) bounds, selection preserved on failure
        c.select(2);
        check(c.getSelectedIndex() == 2, "select(int)");
        expectExact(IllegalArgumentException.class, "select(99)", () -> c.select(99));
        expectExact(IllegalArgumentException.class, "select(-1)", () -> c.select(-1));
        check(c.getSelectedIndex() == 2, "failed select keeps selection");
        // select(String): match by equals, miss and null are silent no-ops
        c.select(new String("d"));
        check(c.getSelectedIndex() == 3, "select by equals");
        c.select("missing");
        check(c.getSelectedIndex() == 3, "select(missing) is a silent no-op");
        c.select((String) null);
        check(c.getSelectedIndex() == 3, "select(null) is a silent no-op");
        // ItemListener registry while items exist
        java.awt.event.ItemListener il = e -> { };
        c.addItemListener(il);
        check(c.getItemListeners().length == 1
              && c.getListeners(java.awt.event.ItemListener.class).length == 1, "ItemListener registered");
        c.removeItemListener(il);
        check(c.getItemListeners().length == 0, "ItemListener removed");
        // removeAll resets model and selection
        c.removeAll();
        check(c.getItemCount() == 0, "removeAll empties items");
        check(c.getSelectedIndex() == -1 && c.getSelectedItem() == null, "removeAll clears selection");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractChoice();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
