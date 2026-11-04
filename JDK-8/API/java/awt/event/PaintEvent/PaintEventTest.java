/*
 * @test
 * @summary java.awt.event.PaintEvent conformance: class reflection.
 * @run main PaintEventTest
 */

public class PaintEventTest {
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
        Class<?> c = Class.forName("java.awt.event.PaintEvent");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.event.ComponentEvent"), "superclass java.awt.event.ComponentEvent");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getUpdateRect"), "method getUpdateRect");
        check(hasMethod(c, "setUpdateRect"), "method setUpdateRect");
        check(hasMethod(c, "paramString"), "method paramString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
