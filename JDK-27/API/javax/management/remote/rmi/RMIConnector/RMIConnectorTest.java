/*
 * @test
 * @summary javax.management.remote.rmi.RMIConnector conformance: white-box structure + black-box behavior.
 * @run main RMIConnectorTest
 */

import java.io.Serializable;
import java.lang.reflect.Method;

import javax.management.remote.JMXAddressable;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnection;
import javax.management.remote.rmi.RMIConnector;
import javax.management.remote.rmi.RMIServer;

public class RMIConnectorTest {
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
        Class<?> c = Class.forName("javax.management.remote.rmi.RMIConnector");
        check(JMXConnector.class.isAssignableFrom(c), "implements JMXConnector");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(JMXAddressable.class.isAssignableFrom(c), "implements JMXAddressable");
        Method[] mm = c.getMethods();
        check(has(mm, "getAddress"), "getAddress");
        check(has(mm, "connect"), "connect");
        check(has(mm, "getMBeanServerConnection"), "getMBeanServerConnection");
        check(has(mm, "close"), "close");
        check(has(mm, "getConnectionId"), "getConnectionId");
    }

    // ---- Part 2: black-box (light behavior) ----
    static void testBlackBox() throws Exception {
        // URL constructor: address is preserved; connector is not connected.
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:9999/server");
        RMIConnector rc = new RMIConnector(url, null);
        check(rc.getAddress().equals(url), "URL constructor preserves address");
        check(rc.toString().contains("jmxServiceURL=" + url), "toString carries url");
        try {
            rc.getConnectionId();
            check(false, "expected IOException for unconnected getConnectionId");
        } catch (java.io.IOException e) {
            check(e.getMessage().equals("Not connected"), "unconnected message: " + e.getMessage());
        }
        rc.close();

        // RMI-stub constructor: no address, so getAddress() is null.
        RMIServer stub = new RMIServer() {
            public String getVersion() { return "1.0"; }
            public RMIConnection newClient(Object credentials) { return null; }
        };
        RMIConnector rc2 = new RMIConnector(stub, null);
        check(rc2.getAddress() == null, "stub constructor has null address");
        rc2.close();

        // Both null arguments are rejected with a stable message.
        try {
            new RMIConnector((JMXServiceURL) null, null);
            check(false, "expected IAE for null url");
        } catch (IllegalArgumentException e) {
            check(e.getMessage().equals("rmiServer and jmxServiceURL both null"), "null url message: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
