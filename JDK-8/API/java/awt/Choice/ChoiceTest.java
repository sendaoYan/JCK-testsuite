/*
 * @test
 * @summary java.awt.Choice conformance: class reflection.
 * @run main ChoiceTest
 */

public class ChoiceTest {
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
        Class<?> c = Class.forName("java.awt.Choice");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.Component"), "superclass java.awt.Component");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.ItemSelectable", "javax.accessibility.Accessible"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "addNotify"), "method addNotify");
        check(hasMethod(c, "getItemCount"), "method getItemCount");
        check(hasMethod(c, "countItems"), "method countItems");
        check(hasMethod(c, "getItem"), "method getItem");
        check(hasMethod(c, "add"), "method add");
        check(hasMethod(c, "addItem"), "method addItem");
        check(hasMethod(c, "insert"), "method insert");
        check(hasMethod(c, "remove"), "method remove");
        check(hasMethod(c, "removeAll"), "method removeAll");
        check(hasMethod(c, "getSelectedItem"), "method getSelectedItem");
        check(hasMethod(c, "getSelectedObjects"), "method getSelectedObjects");
        check(hasMethod(c, "getSelectedIndex"), "method getSelectedIndex");
        check(hasMethod(c, "select"), "method select");
        check(hasMethod(c, "addItemListener"), "method addItemListener");
        check(hasMethod(c, "removeItemListener"), "method removeItemListener");
        check(hasMethod(c, "getItemListeners"), "method getItemListeners");
        check(hasMethod(c, "getListeners"), "method getListeners");
        check(hasMethod(c, "getAccessibleContext"), "method getAccessibleContext");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
