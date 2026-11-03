/*
 * @test
 * @summary javax.management.remote.JMXAddressable conformance: white-box structure + black-box behavior.
 * @run main JMXAddressableTest
 */

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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
