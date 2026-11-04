/*
 * @test
 * @key headful
 * @summary java.awt.List conformance: class reflection.
 * @run main ListTest
 */

import java.awt.*;

public class ListTest {
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
        Class<?> c = Class.forName("java.awt.List");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.Component"), "superclass java.awt.Component");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.ItemSelectable", "javax.accessibility.Accessible"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "addNotify"), "method addNotify");
        check(hasMethod(c, "removeNotify"), "method removeNotify");
        check(hasMethod(c, "getItemCount"), "method getItemCount");
        check(hasMethod(c, "countItems"), "method countItems");
        check(hasMethod(c, "getItem"), "method getItem");
        check(hasMethod(c, "getItems"), "method getItems");
        check(hasMethod(c, "add"), "method add");
        check(hasMethod(c, "addItem"), "method addItem");
        check(hasMethod(c, "replaceItem"), "method replaceItem");
        check(hasMethod(c, "removeAll"), "method removeAll");
        check(hasMethod(c, "clear"), "method clear");
        check(hasMethod(c, "remove"), "method remove");
        check(hasMethod(c, "delItem"), "method delItem");
        check(hasMethod(c, "getSelectedIndex"), "method getSelectedIndex");
        check(hasMethod(c, "getSelectedIndexes"), "method getSelectedIndexes");
        check(hasMethod(c, "getSelectedItem"), "method getSelectedItem");
        check(hasMethod(c, "getSelectedItems"), "method getSelectedItems");
        check(hasMethod(c, "getSelectedObjects"), "method getSelectedObjects");
        check(hasMethod(c, "select"), "method select");
        check(hasMethod(c, "deselect"), "method deselect");
        check(hasMethod(c, "isIndexSelected"), "method isIndexSelected");
        check(hasMethod(c, "isSelected"), "method isSelected");
        check(hasMethod(c, "getRows"), "method getRows");
        check(hasMethod(c, "isMultipleMode"), "method isMultipleMode");
        check(hasMethod(c, "allowsMultipleSelections"), "method allowsMultipleSelections");
        check(hasMethod(c, "setMultipleMode"), "method setMultipleMode");
        check(hasMethod(c, "setMultipleSelections"), "method setMultipleSelections");
        check(hasMethod(c, "getVisibleIndex"), "method getVisibleIndex");
        check(hasMethod(c, "makeVisible"), "method makeVisible");
        check(hasMethod(c, "getPreferredSize"), "method getPreferredSize");
        check(hasMethod(c, "preferredSize"), "method preferredSize");
        check(hasMethod(c, "getMinimumSize"), "method getMinimumSize");
        check(hasMethod(c, "minimumSize"), "method minimumSize");
        check(hasMethod(c, "addItemListener"), "method addItemListener");
        check(hasMethod(c, "removeItemListener"), "method removeItemListener");
        check(hasMethod(c, "getItemListeners"), "method getItemListeners");
        check(hasMethod(c, "addActionListener"), "method addActionListener");
        check(hasMethod(c, "removeActionListener"), "method removeActionListener");
        check(hasMethod(c, "getActionListeners"), "method getActionListeners");
        check(hasMethod(c, "getListeners"), "method getListeners");
        check(hasMethod(c, "delItems"), "method delItems");
        check(hasMethod(c, "getAccessibleContext"), "method getAccessibleContext");
        testContractList();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
