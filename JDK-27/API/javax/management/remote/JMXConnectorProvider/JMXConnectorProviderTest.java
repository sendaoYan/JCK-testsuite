/*
 * @test
 * @summary javax.management.remote.JMXConnectorProvider conformance: white-box structure + black-box behavior.
 * @run main JMXConnectorProviderTest
 */
import javax.management.*;
import javax.management.remote.*;


import java.lang.reflect.Method;
import java.util.Map;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorProvider;
import javax.management.remote.JMXServiceURL;

public class JMXConnectorProviderTest {
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
        Class<?> c = Class.forName("javax.management.remote.JMXConnectorProvider");
        check(c.isInterface(), "is interface");
        Method m = c.getMethod("newJMXConnector", JMXServiceURL.class, Map.class);
        check(m.getReturnType() == JMXConnector.class, "newJMXConnector returns JMXConnector");
    }

    // ---- Part 2: black-box (light behavior) ----
    static void testBlackBox() throws Exception {
        JMXConnectorProvider provider = new JMXConnectorProvider() {
            public JMXConnector newJMXConnector(JMXServiceURL serviceURL, Map<String, ?> environment) {
                return null;
            }
        };
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi://localhost:1/x");
        check(provider.newJMXConnector(url, null) == null, "anonymous newJMXConnector callable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepened();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ==== deepened (2026-11, batch m): SPI contract with a recording implementation ====
    static void testDeepened() throws Exception {
        // Interface shape: one factory method returning JMXConnector, declaring IOException.
        java.lang.reflect.Method m = JMXConnectorProvider.class.getMethod(
                "newJMXConnector", JMXServiceURL.class, java.util.Map.class);
        check(m.getReturnType() == JMXConnector.class, "d-newJMXConnector returns JMXConnector");
        boolean declaresIO = false;
        for (Class<?> ex : m.getExceptionTypes()) {
            if (ex == java.io.IOException.class) declaresIO = true;
        }
        check(declaresIO, "d-newJMXConnector declares IOException");

        // Recording provider: both arguments arrive as passed (null env allowed).
        final java.util.List<Object> log = java.util.Collections.synchronizedList(new java.util.ArrayList<Object>());
        final JMXConnector sentinel = new JMXConnector() {
            public void connect() { }
            public void connect(java.util.Map<String, ?> env) { }
            public MBeanServerConnection getMBeanServerConnection() { return null; }
            public MBeanServerConnection getMBeanServerConnection(javax.security.auth.Subject s) { return null; }
            public void close() { }
            public void addConnectionNotificationListener(NotificationListener l, NotificationFilter f, Object h) { }
            public void removeConnectionNotificationListener(NotificationListener l) { }
            public void removeConnectionNotificationListener(NotificationListener l, NotificationFilter f, Object h) { }
            public String getConnectionId() { return "sentinel"; }
        };
        JMXConnectorProvider provider = new JMXConnectorProvider() {
            public JMXConnector newJMXConnector(JMXServiceURL serviceURL, java.util.Map<String, ?> environment) {
                log.add(serviceURL);
                log.add(environment);
                return sentinel;
            }
        };
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi://127.0.0.1");
        java.util.Map<String, Object> env = new java.util.HashMap<String, Object>();
        env.put("jck.key", "v");
        check(provider.newJMXConnector(url, env) == sentinel, "d-provider returns its connector");
        check(log.get(0) == url, "d-url passed by identity");
        check(log.get(1) == env, "d-env passed by identity");
        check(provider.newJMXConnector(url, null) == sentinel, "d-null env accepted");
        // A provider that refuses a URL signals via IOException, propagated by a direct call.
        JMXConnectorProvider refusing = new JMXConnectorProvider() {
            public JMXConnector newJMXConnector(JMXServiceURL serviceURL, java.util.Map<String, ?> environment)
                    throws java.io.IOException {
                throw new java.io.IOException("no connector");
            }
        };
        try {
            refusing.newJMXConnector(url, null);
            check(false, "d-refusing provider should throw");
        } catch (java.io.IOException e) {
            check("no connector".equals(e.getMessage()), "d-refusal message: " + e.getMessage());
        }
    }

}
