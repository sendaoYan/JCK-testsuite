/*
 * @test
 * @summary javax.accessibility.AccessibleResourceBundle conformance: class reflection.
 * @run main AccessibleResourceBundleTest
 */

public class AccessibleResourceBundleTest {
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
        Class<?> c = Class.forName("javax.accessibility.AccessibleResourceBundle");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.util.ListResourceBundle"), "superclass java.util.ListResourceBundle");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getContents"), "method getContents");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
