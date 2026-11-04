/*
 * @test
 * @summary java.awt.MenuItem conformance: class reflection.
 * @run main MenuItemTest
 */

public class MenuItemTest {
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
        Class<?> c = Class.forName("java.awt.MenuItem");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.MenuComponent"), "superclass java.awt.MenuComponent");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"javax.accessibility.Accessible"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "addNotify"), "method addNotify");
        check(hasMethod(c, "getLabel"), "method getLabel");
        check(hasMethod(c, "setLabel"), "method setLabel");
        check(hasMethod(c, "isEnabled"), "method isEnabled");
        check(hasMethod(c, "setEnabled"), "method setEnabled");
        check(hasMethod(c, "enable"), "method enable");
        check(hasMethod(c, "disable"), "method disable");
        check(hasMethod(c, "getShortcut"), "method getShortcut");
        check(hasMethod(c, "setShortcut"), "method setShortcut");
        check(hasMethod(c, "deleteShortcut"), "method deleteShortcut");
        check(hasMethod(c, "setActionCommand"), "method setActionCommand");
        check(hasMethod(c, "getActionCommand"), "method getActionCommand");
        check(hasMethod(c, "addActionListener"), "method addActionListener");
        check(hasMethod(c, "removeActionListener"), "method removeActionListener");
        check(hasMethod(c, "getActionListeners"), "method getActionListeners");
        check(hasMethod(c, "getListeners"), "method getListeners");
        check(hasMethod(c, "paramString"), "method paramString");
        check(hasMethod(c, "getAccessibleContext"), "method getAccessibleContext");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
