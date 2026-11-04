/*
 * @test
 * @summary java.awt.CheckboxMenuItem conformance: class reflection.
 * @run main CheckboxMenuItemTest
 */

public class CheckboxMenuItemTest {
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
        Class<?> c = Class.forName("java.awt.CheckboxMenuItem");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.MenuItem"), "superclass java.awt.MenuItem");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.ItemSelectable", "javax.accessibility.Accessible"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "addNotify"), "method addNotify");
        check(hasMethod(c, "getState"), "method getState");
        check(hasMethod(c, "setState"), "method setState");
        check(hasMethod(c, "getSelectedObjects"), "method getSelectedObjects");
        check(hasMethod(c, "addItemListener"), "method addItemListener");
        check(hasMethod(c, "removeItemListener"), "method removeItemListener");
        check(hasMethod(c, "getItemListeners"), "method getItemListeners");
        check(hasMethod(c, "getListeners"), "method getListeners");
        check(hasMethod(c, "paramString"), "method paramString");
        check(hasMethod(c, "getAccessibleContext"), "method getAccessibleContext");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
