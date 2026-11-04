/*
 * @test
 * @summary javax.accessibility.AccessibleValue conformance: interface reflection.
 * @run main AccessibleValueTest
 */

public class AccessibleValueTest {
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
        Class<?> c = Class.forName("javax.accessibility.AccessibleValue");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getCurrentAccessibleValue"), "method getCurrentAccessibleValue");
        check(hasMethod(c, "setCurrentAccessibleValue"), "method setCurrentAccessibleValue");
        check(hasMethod(c, "getMinimumAccessibleValue"), "method getMinimumAccessibleValue");
        check(hasMethod(c, "getMaximumAccessibleValue"), "method getMaximumAccessibleValue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
