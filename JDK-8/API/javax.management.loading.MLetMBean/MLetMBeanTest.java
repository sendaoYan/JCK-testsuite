/*
 * @test
 * @summary javax.management.loading.MLetMBean conformance: interface reflection.
 * @run main MLetMBeanTest
 */

public class MLetMBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.loading.MLetMBean");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(hasMethod(c, "addURL"), "declares addURL");
        check(hasMethod(c, "getLibraryDirectory"), "declares getLibraryDirectory");
        check(hasMethod(c, "getMBeansFromURL"), "declares getMBeansFromURL");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
