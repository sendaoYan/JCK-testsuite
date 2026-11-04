/*
 * @test
 * @summary java.awt.PopupMenu conformance: class reflection.
 * @run main PopupMenuTest
 */

public class PopupMenuTest {
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
        Class<?> c = Class.forName("java.awt.PopupMenu");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.Menu"), "superclass java.awt.Menu");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getParent"), "method getParent");
        check(hasMethod(c, "addNotify"), "method addNotify");
        check(hasMethod(c, "show"), "method show");
        check(hasMethod(c, "getAccessibleContext"), "method getAccessibleContext");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
