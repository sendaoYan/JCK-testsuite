/*
 * @test
 * @summary javax.management.remote.JMXConnectorServerProvider conformance: white-box structure + black-box behavior.
 * @run main JMXConnectorServerProviderTest
 */
import javax.management.*;
import javax.management.remote.*;


import java.lang.reflect.Method;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerProvider;
import javax.management.remote.JMXServiceURL;

public class JMXConnectorServerProviderTest {
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
        Class<?> c = Class.forName("javax.management.remote.JMXConnectorServerProvider");
        check(c.isInterface(), "is interface");
        Method m = c.getMethod("newJMXConnectorServer", JMXServiceURL.class, Map.class, MBeanServer.class);
        check(m.getReturnType() == JMXConnectorServer.class, "returns JMXConnectorServer");
    }

    // ---- Part 2: black-box (light behavior) ----
    static void testBlackBox() throws Exception {
        JMXConnectorServerProvider provider = new JMXConnectorServerProvider() {
            public JMXConnectorServer newJMXConnectorServer(JMXServiceURL url, Map<String, ?> env, MBeanServer mbs) {
                return null;
            }
        };
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi://localhost:1/x");
        check(provider.newJMXConnectorServer(url, null, null) == null, "anonymous newJMXConnectorServer callable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepened();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ==== deepened (2026-11, batch m): SPI contract with a recording implementation ====
    static void testDeepened() throws Exception {
        // Interface shape: one factory method returning JMXConnectorServer, declaring IOException.
        java.lang.reflect.Method m = JMXConnectorServerProvider.class.getMethod(
                "newJMXConnectorServer", JMXServiceURL.class, java.util.Map.class, MBeanServer.class);
        check(m.getReturnType() == JMXConnectorServer.class, "d-newJMXConnectorServer returns JMXConnectorServer");
        boolean declaresIO = false;
        for (Class<?> ex : m.getExceptionTypes()) {
            if (ex == java.io.IOException.class) declaresIO = true;
        }
        check(declaresIO, "d-newJMXConnectorServer declares IOException");

        // Recording provider: all three arguments arrive as passed (null env allowed).
        final java.util.List<Object> log = java.util.Collections.synchronizedList(new java.util.ArrayList<Object>());
        JMXConnectorServerProvider provider = new JMXConnectorServerProvider() {
            public JMXConnectorServer newJMXConnectorServer(JMXServiceURL serviceURL,
                    java.util.Map<String, ?> environment, MBeanServer mbeanServer) {
                log.add(serviceURL);
                log.add(environment);
                log.add(mbeanServer);
                return null;
            }
        };
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi://127.0.0.1");
        MBeanServer mbs = MBeanServerFactory.newMBeanServer();
        java.util.Map<String, Object> env = new java.util.HashMap<String, Object>();
        env.put("jck.key", "v");
        check(provider.newJMXConnectorServer(url, env, mbs) == null, "d-provider returns its value");
        check(log.get(0) == url, "d-url passed by identity");
        check(log.get(1) == env, "d-env passed by identity");
        check(log.get(2) == mbs, "d-mbeanServer passed by identity");
        check(provider.newJMXConnectorServer(url, null, mbs) == null, "d-null env accepted");
        check(provider.newJMXConnectorServer(url, null, null) == null, "d-null mbeanServer accepted by SPI");
        // A provider that refuses a URL signals via IOException, propagated by a direct call.
        JMXConnectorServerProvider refusing = new JMXConnectorServerProvider() {
            public JMXConnectorServer newJMXConnectorServer(JMXServiceURL serviceURL,
                    java.util.Map<String, ?> environment, MBeanServer mbeanServer) throws java.io.IOException {
                throw new java.io.IOException("no server");
            }
        };
        try {
            refusing.newJMXConnectorServer(url, null, mbs);
            check(false, "d-refusing provider should throw");
        } catch (java.io.IOException e) {
            check("no server".equals(e.getMessage()), "d-refusal message: " + e.getMessage());
        }
    }

}
