/*
 * @test
 * @summary java.awt.MenuBar conformance: class reflection.
 * @run main MenuBarTest
 */

public class MenuBarTest {
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
        Class<?> c = Class.forName("java.awt.MenuBar");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.MenuComponent"), "superclass java.awt.MenuComponent");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.MenuContainer", "javax.accessibility.Accessible"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "addNotify"), "method addNotify");
        check(hasMethod(c, "removeNotify"), "method removeNotify");
        check(hasMethod(c, "getHelpMenu"), "method getHelpMenu");
        check(hasMethod(c, "setHelpMenu"), "method setHelpMenu");
        check(hasMethod(c, "add"), "method add");
        check(hasMethod(c, "remove"), "method remove");
        check(hasMethod(c, "getMenuCount"), "method getMenuCount");
        check(hasMethod(c, "countMenus"), "method countMenus");
        check(hasMethod(c, "getMenu"), "method getMenu");
        check(hasMethod(c, "shortcuts"), "method shortcuts");
        check(hasMethod(c, "getShortcutMenuItem"), "method getShortcutMenuItem");
        check(hasMethod(c, "deleteShortcut"), "method deleteShortcut");
        check(hasMethod(c, "getAccessibleContext"), "method getAccessibleContext");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
