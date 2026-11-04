/*
 * @test
 * @summary javax.management.remote.JMXConnectorServerProvider conformance: white-box structure + black-box behavior.
 * @run main JMXConnectorServerProviderTest
 */

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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
