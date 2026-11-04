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
    static void testContractList() throws Exception {
        // constructor matrix; rows<=0 falls back to the default row count 4
        check(new List(0).getRows() == 4, "List(0) rows falls back to 4");
        check(new List(6).getRows() == 6 && !new List(6).isMultipleMode(), "List(6) rows, single mode");
        check(new List(2, true).isMultipleMode() && new List(2, true).getRows() == 2, "List(2,true)");
        List z = new List();
        check(z.getItemCount() == 0 && z.getSelectedIndex() == -1 && z.getVisibleIndex() == -1,
              "empty defaults");
        // null items are tolerated (unlike Choice); out-of-range indexes append
        z.add((String) null);
        check(z.getItemCount() == 1, "add(null) accepted");
        z.add("a"); z.add("b");
        z.add("neg", -1);
        check("neg".equals(z.getItem(3)), "add(item,-1) appends");
        z.add("big", 99);
        check("big".equals(z.getItem(4)), "add(item,beyond count) appends");
        z.add("mid", 1);
        check("mid".equals(z.getItem(1)) && "a".equals(z.getItem(2)), "add(item,1) inserts");
        check(z.getItemCount() == 6, "item count tracked");
        // replaceItem
        z.replaceItem(null, 0);
        check("".equals(z.getItem(0)), "replaceItem(null,0) accepted");
        z.replaceItem("R", 1);
        check("R".equals(z.getItem(1)), "replaceItem roundtrip");
        expectExact(ArrayIndexOutOfBoundsException.class, "replaceItem(99)", () -> z.replaceItem("x", 99));
        expectExact(ArrayIndexOutOfBoundsException.class, "getItem(99)", () -> z.getItem(99));
        // remove bounds and missing item
        expectExact(ArrayIndexOutOfBoundsException.class, "remove(99)", () -> z.remove(99));
        expectExact(ArrayIndexOutOfBoundsException.class, "remove(-2)", () -> z.remove(-2));
        expectExact(IllegalArgumentException.class, "remove(String missing)", () -> z.remove("nope"));

        // single-selection semantics
        List s = new List();
        s.add("a"); s.add("b"); s.add("c");
        s.select(1);
        check(s.getSelectedIndex() == 1 && "b".equals(s.getSelectedItem()), "select(index)");
        s.select(2);
        check(s.getSelectedIndex() == 2, "reselect moves selection in single mode");
        s.deselect(2);
        check(s.getSelectedIndex() == -1 && s.getSelectedItems().length == 0, "deselect clears selection");
        // out-of-range select() is accepted without throwing and stores the raw
        // index; the model is then inconsistent and getSelectedItems() throws
        List neg = new List();
        neg.add("a"); neg.add("b");
        neg.select(-1);
        check(neg.getSelectedIndex() == -1, "select(-1) keeps reported index -1");
        expectExact(ArrayIndexOutOfBoundsException.class, "getSelectedItems after select(-1)",
              () -> { int n = neg.getSelectedItems().length; });
        List big = new List();
        big.add("a"); big.add("b");
        big.select(99);
        check(big.getSelectedIndex() == 99, "select(99) stores the raw out-of-range index");
        expectExact(ArrayIndexOutOfBoundsException.class, "getSelectedItems after select(99)",
              () -> { int n = big.getSelectedItems().length; });
        // deselect out of range does not throw
        s.deselect(-1);
        s.deselect(99);
        check(s.getSelectedIndex() == -1, "deselect out of range keeps index");
        s.makeVisible(-1);
        check(s.getVisibleIndex() == -1, "makeVisible(-1) is a no-op");
        s.makeVisible(99); // accepted silently; only valid indexes update the visible index
        s.makeVisible(2);
        check(s.getVisibleIndex() == 2, "makeVisible/getVisibleIndex roundtrip");

        // multiple-selection semantics
        List m = new List(4, true);
        m.add("p"); m.add("q"); m.add("r");
        m.select(0); m.select(2);
        int[] idxs = m.getSelectedIndexes();
        check(idxs.length == 2 && ((idxs[0] == 0 && idxs[1] == 2) || (idxs[0] == 2 && idxs[1] == 0)),
              "multi selection indexes {0,2}");
        java.util.Set<String> items = new java.util.HashSet<>(java.util.Arrays.asList(m.getSelectedItems()));
        check(items.equals(new java.util.HashSet<>(java.util.Arrays.asList("p", "r"))),
              "multi selection items {p,r}");
        m.deselect(0);
        check(m.getSelectedItems().length == 1 && "r".equals(m.getSelectedItems()[0]),
              "deselect removes exactly one");
        // mode switching keeps the selected set and reports an ambiguous single index as -1
        m.select(1);
        m.setMultipleMode(false);
        check(!m.isMultipleMode(), "setMultipleMode(false)");
        check(m.getSelectedItems().length == 2, "selection retained across switch to single mode");
        check(m.getSelectedIndex() == -1, "ambiguous single index reported as -1");
        m.setMultipleMode(true);
        check(m.isMultipleMode() && m.getSelectedItems().length == 2, "switch back to multiple mode");

        // removeAll resets model and selection; a later add does not resurrect selection
        s.removeAll();
        check(s.getItemCount() == 0 && s.getSelectedIndex() == -1, "removeAll resets model and selection");
        s.add("fresh");
        check(s.getSelectedIndex() == -1, "selection stays cleared after re-adding items");

        // listener registries
        java.awt.event.ItemListener il = e -> { };
        java.awt.event.ActionListener al = e -> { };
        s.addItemListener(il);
        s.addActionListener(al);
        check(s.getItemListeners().length == 1 && s.getActionListeners().length == 1, "listeners registered");
        s.removeItemListener(il);
        s.removeActionListener(al);
        check(s.getItemListeners().length == 0 && s.getActionListeners().length == 0, "listeners removed");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractList();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
