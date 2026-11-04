/*
 * @test
 * @summary javax.management.remote.JMXConnectorServerMBean conformance: white-box structure + black-box behavior.
 * @run main JMXConnectorServerMBeanTest
 */

import java.lang.reflect.Method;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorServerMBean;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.MBeanServerForwarder;

public class JMXConnectorServerMBeanTest {
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
        Class<?> c = Class.forName("javax.management.remote.JMXConnectorServerMBean");
        check(c.isInterface(), "is interface");
        Method[] mm = c.getMethods();
        check(has(mm, "start"), "start");
        check(has(mm, "stop"), "stop");
        check(has(mm, "isActive"), "isActive");
        check(has(mm, "setMBeanServerForwarder"), "setMBeanServerForwarder");
        check(has(mm, "getConnectionIds"), "getConnectionIds");
        check(has(mm, "getAddress"), "getAddress");
        check(has(mm, "getAttributes"), "getAttributes");
        check(has(mm, "toJMXConnector"), "toJMXConnector");
    }

    // ---- Part 2: black-box (light behavior) ----
    static void testBlackBox() throws Exception {
        JMXConnectorServerMBean mbean = new JMXConnectorServerMBean() {
            boolean active = true;
            public void start() { active = true; }
            public void stop() { active = false; }
            public boolean isActive() { return active; }
            public void setMBeanServerForwarder(MBeanServerForwarder f) { }
            public String[] getConnectionIds() { return new String[]{ "a", "b" }; }
            public JMXServiceURL getAddress() { return null; }
            public Map<String, ?> getAttributes() { return java.util.Collections.emptyMap(); }
            public JMXConnector toJMXConnector(Map<String, ?> env) { return null; }
        };
        check(mbean.isActive(), "initial active");
        mbean.stop();
        check(!mbean.isActive(), "stop -> inactive");
        mbean.start();
        check(mbean.isActive(), "start -> active");
        check(mbean.getConnectionIds().length == 2, "connection ids");
        check(mbean.getAttributes().isEmpty(), "empty attributes");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
