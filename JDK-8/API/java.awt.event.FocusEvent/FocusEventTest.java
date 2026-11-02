/*
 * @test
 * @summary java.awt.event.FocusEvent conformance: class reflection.
 * @run main FocusEventTest
 */

public class FocusEventTest {
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
        Class<?> c = Class.forName("java.awt.event.FocusEvent");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.event.ComponentEvent"), "superclass java.awt.event.ComponentEvent");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "isTemporary"), "method isTemporary");
        check(hasMethod(c, "getOppositeComponent"), "method getOppositeComponent");
        check(hasMethod(c, "paramString"), "method paramString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
