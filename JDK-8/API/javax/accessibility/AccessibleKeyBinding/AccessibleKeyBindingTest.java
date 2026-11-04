/*
 * @test
 * @summary javax.accessibility.AccessibleKeyBinding conformance: interface reflection.
 * @run main AccessibleKeyBindingTest
 */

public class AccessibleKeyBindingTest {
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
        Class<?> c = Class.forName("javax.accessibility.AccessibleKeyBinding");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getAccessibleKeyBindingCount"), "method getAccessibleKeyBindingCount");
        check(hasMethod(c, "getAccessibleKeyBinding"), "method getAccessibleKeyBinding");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
