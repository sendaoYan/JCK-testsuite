/*
 * @test
 * @summary javax.management.remote.MBeanServerForwarder conformance: interface reflection.
 * @run main MBeanServerForwarderTest
 */

public class MBeanServerForwarderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.remote.MBeanServerForwarder");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(Class.forName("javax.management.MBeanServer").isAssignableFrom(c), "implements/extends javax.management.MBeanServer");
        check(hasMethod(c, "getMBeanServer"), "declares getMBeanServer");
        check(hasMethod(c, "setMBeanServer"), "declares setMBeanServer");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
