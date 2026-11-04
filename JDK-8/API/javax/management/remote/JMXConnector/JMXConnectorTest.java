/*
 * @test
 * @summary javax.management.remote.JMXConnector conformance: interface reflection.
 * @run main JMXConnectorTest
 */

public class JMXConnectorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.remote.JMXConnector");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(Class.forName("java.io.Closeable").isAssignableFrom(c), "implements/extends java.io.Closeable");
        check(hasMethod(c, "addConnectionNotificationListener"), "declares addConnectionNotificationListener");
        check(hasMethod(c, "close"), "declares close");
        check(hasMethod(c, "connect"), "declares connect");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
