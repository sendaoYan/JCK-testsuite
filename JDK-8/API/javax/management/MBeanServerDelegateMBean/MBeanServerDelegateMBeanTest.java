/*
 * @test
 * @summary javax.management.MBeanServerDelegateMBean conformance: interface reflection.
 * @run main MBeanServerDelegateMBeanTest
 */

public class MBeanServerDelegateMBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.MBeanServerDelegateMBean");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(hasMethod(c, "getImplementationName"), "declares getImplementationName");
        check(hasMethod(c, "getImplementationVendor"), "declares getImplementationVendor");
        check(hasMethod(c, "getImplementationVersion"), "declares getImplementationVersion");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
