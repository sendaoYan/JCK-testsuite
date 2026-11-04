/*
 * @test
 * @summary javax.management.remote.JMXConnectorServerMBean conformance: interface reflection.
 * @run main JMXConnectorServerMBeanTest
 */

public class JMXConnectorServerMBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.remote.JMXConnectorServerMBean");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(hasMethod(c, "getAddress"), "declares getAddress");
        check(hasMethod(c, "getAttributes"), "declares getAttributes");
        check(hasMethod(c, "getConnectionIds"), "declares getConnectionIds");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
