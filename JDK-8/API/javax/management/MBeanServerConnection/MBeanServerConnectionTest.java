/*
 * @test
 * @summary javax.management.MBeanServerConnection conformance: interface reflection.
 * @run main MBeanServerConnectionTest
 */

public class MBeanServerConnectionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.MBeanServerConnection");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(hasMethod(c, "addNotificationListener"), "declares addNotificationListener");
        check(hasMethod(c, "createMBean"), "declares createMBean");
        check(hasMethod(c, "getAttribute"), "declares getAttribute");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
