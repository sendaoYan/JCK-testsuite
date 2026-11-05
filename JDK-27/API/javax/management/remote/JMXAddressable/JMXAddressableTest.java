/*
 * @test
 * @summary javax.management.remote.JMXAddressable conformance: white-box structure + black-box behavior.
 * @run main JMXAddressableTest
 */
import javax.management.*;
import javax.management.remote.*;


import java.lang.reflect.Method;

import javax.management.remote.JMXAddressable;
import javax.management.remote.JMXServiceURL;

public class JMXAddressableTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean has(Method[] mm, String n) {
        for (Method m : mm) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.remote.JMXAddressable");
        check(c.isInterface(), "is interface");
        Method m = c.getMethod("getAddress");
        check(m.getReturnType() == JMXServiceURL.class, "getAddress returns JMXServiceURL");
    }

    // ---- Part 2: black-box (light behavior) ----
    static void testBlackBox() throws Exception {
        // Anonymous implementation proves the single method is callable by user code.
        JMXAddressable a = new JMXAddressable() {
            public JMXServiceURL getAddress() { return null; }
        };
        check(a.getAddress() == null, "anonymous getAddress callable");

        final JMXServiceURL url = new JMXServiceURL("service:jmx:rmi://localhost:1/x");
        JMXAddressable b = new JMXAddressable() {
            public JMXServiceURL getAddress() { return url; }
        };
        check(b.getAddress() == url, "anonymous getAddress returns value");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
