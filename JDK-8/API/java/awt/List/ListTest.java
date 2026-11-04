/*
 * @test
 * @summary java.awt.List conformance: class reflection.
 * @run main ListTest
 */

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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
