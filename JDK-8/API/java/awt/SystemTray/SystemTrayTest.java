/*
 * @test
 * @summary java.awt.SystemTray conformance: class reflection.
 * @run main SystemTrayTest
 */

public class SystemTrayTest {
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
        Class<?> c = Class.forName("java.awt.SystemTray");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getSystemTray"), "method getSystemTray");
        check(hasMethod(c, "isSupported"), "method isSupported");
        check(hasMethod(c, "add"), "method add");
        check(hasMethod(c, "remove"), "method remove");
        check(hasMethod(c, "getTrayIcons"), "method getTrayIcons");
        check(hasMethod(c, "getTrayIconSize"), "method getTrayIconSize");
        check(hasMethod(c, "addPropertyChangeListener"), "method addPropertyChangeListener");
        check(hasMethod(c, "removePropertyChangeListener"), "method removePropertyChangeListener");
        check(hasMethod(c, "getPropertyChangeListeners"), "method getPropertyChangeListeners");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
