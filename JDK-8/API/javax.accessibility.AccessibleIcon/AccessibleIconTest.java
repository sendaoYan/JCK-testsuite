/*
 * @test
 * @summary javax.accessibility.AccessibleIcon conformance: interface reflection.
 * @run main AccessibleIconTest
 */

public class AccessibleIconTest {
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
        Class<?> c = Class.forName("javax.accessibility.AccessibleIcon");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getAccessibleIconDescription"), "method getAccessibleIconDescription");
        check(hasMethod(c, "setAccessibleIconDescription"), "method setAccessibleIconDescription");
        check(hasMethod(c, "getAccessibleIconWidth"), "method getAccessibleIconWidth");
        check(hasMethod(c, "getAccessibleIconHeight"), "method getAccessibleIconHeight");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
