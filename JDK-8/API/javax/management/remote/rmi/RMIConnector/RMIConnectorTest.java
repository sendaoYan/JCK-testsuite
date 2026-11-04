/*
 * @test
 * @summary javax.management.remote.rmi.RMIConnector conformance: concrete class reflection.
 * @run main RMIConnectorTest
 */

public class RMIConnectorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.remote.rmi.RMIConnector");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check(Class.forName("javax.management.remote.JMXConnector").isAssignableFrom(c), "implements/extends javax.management.remote.JMXConnector");
        check(Class.forName("java.io.Serializable").isAssignableFrom(c), "implements/extends java.io.Serializable");
        check(Class.forName("javax.management.remote.JMXAddressable").isAssignableFrom(c), "implements/extends javax.management.remote.JMXAddressable");
        check(hasMethod(c, "addConnectionNotificationListener"), "declares addConnectionNotificationListener");
        check(hasMethod(c, "close"), "declares close");
        check(hasMethod(c, "connect"), "declares connect");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
