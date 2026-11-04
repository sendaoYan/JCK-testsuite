/*
 * @test
 * @summary java.awt.event.WindowEvent conformance: class reflection.
 * @run main WindowEventTest
 */

public class WindowEventTest {
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
        Class<?> c = Class.forName("java.awt.event.WindowEvent");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.event.ComponentEvent"), "superclass java.awt.event.ComponentEvent");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getWindow"), "method getWindow");
        check(hasMethod(c, "getOppositeWindow"), "method getOppositeWindow");
        check(hasMethod(c, "getOldState"), "method getOldState");
        check(hasMethod(c, "getNewState"), "method getNewState");
        check(hasMethod(c, "paramString"), "method paramString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
