/*
 * @test
 * @summary javax.management.remote.JMXConnectorServerProvider conformance: interface reflection.
 * @run main JMXConnectorServerProviderTest
 */

public class JMXConnectorServerProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.remote.JMXConnectorServerProvider");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(hasMethod(c, "newJMXConnectorServer"), "declares newJMXConnectorServer");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
