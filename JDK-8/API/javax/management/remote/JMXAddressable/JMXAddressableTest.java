/*
 * @test
 * @summary javax.management.remote.JMXAddressable conformance: interface reflection.
 * @run main JMXAddressableTest
 */
import javax.management.*;
import javax.management.remote.*;


public class JMXAddressableTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.remote.JMXAddressable");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(hasMethod(c, "getAddress"), "declares getAddress");
        testDeepened();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ==== deepened (2026-11, batch m): getAddress() across the connector server and client lifecycle ====
    static void testDeepened() throws Exception {
        // Interface shape: getAddress() returns JMXServiceURL.
        check(JMXAddressable.class.getMethod("getAddress").getReturnType() == JMXServiceURL.class,
                "d-getAddress returns JMXServiceURL");

        // A connector server is JMXAddressable: address is null before start, a loopback stub URL
        // while active, and null again after stop.
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        JMXConnectorServer cs = JMXConnectorServerFactory.newJMXConnectorServer(
                new JMXServiceURL("service:jmx:rmi://127.0.0.1"), null, server);
        JMXAddressable addressable = cs;
        check(addressable.getAddress() == null, "d-server address null before start");
        cs.start();
        try {
            JMXServiceURL addr = addressable.getAddress();
            check(addr != null, "d-server address non-null while active");
            check("rmi".equals(addr.getProtocol()), "d-server address protocol");
            check("127.0.0.1".equals(addr.getHost()), "d-server address loopback host");
            check(addr.getURLPath().startsWith("/stub/"), "d-server address embeds stub");
            // The client connector is JMXAddressable too and reports the same server address shape.
            JMXConnector conn = JMXConnectorFactory.connect(cs.getAddress(), null);
            try {
                check(conn instanceof JMXAddressable, "d-client is JMXAddressable");
                JMXServiceURL clientAddr = ((JMXAddressable) conn).getAddress();
                check(clientAddr != null, "d-client address non-null when connected");
                check("rmi".equals(clientAddr.getProtocol()), "d-client address protocol");
                check(clientAddr.getURLPath().startsWith("/stub/"), "d-client address embeds stub");
            } finally {
                conn.close();
            }
        } finally {
            cs.stop();
        }
        check(addressable.getAddress() == null, "d-server address null after stop");

        // User implementation round-trip through the interface.
        final JMXServiceURL fixed = new JMXServiceURL("service:jmx:rmi://localhost:1/x");
        JMXAddressable user = new JMXAddressable() {
            public JMXServiceURL getAddress() { return fixed; }
        };
        check(user.getAddress() == fixed, "d-user impl identity round-trip");
    }

}
