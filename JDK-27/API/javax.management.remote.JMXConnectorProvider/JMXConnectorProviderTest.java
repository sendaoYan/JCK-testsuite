/*
 * @test
 * @summary javax.management.remote.JMXConnectorProvider conformance: white-box structure + black-box behavior.
 * @run main JMXConnectorProviderTest
 */

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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
