/*
 * @test
 * @summary java.awt.HeadlessException conformance: class reflection.
 * @run main HeadlessExceptionTest
 */

public class HeadlessExceptionTest {
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
        Class<?> c = Class.forName("java.awt.HeadlessException");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.UnsupportedOperationException"), "superclass java.lang.UnsupportedOperationException");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getMessage"), "method getMessage");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
