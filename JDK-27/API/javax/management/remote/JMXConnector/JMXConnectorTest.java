/*
 * @test
 * @summary javax.management.remote.JMXConnector conformance: white-box structure + black-box behavior.
 * @run main JMXConnectorTest
 */

import java.io.Closeable;
import java.lang.reflect.Method;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.remote.JMXConnector;

public class JMXConnectorTest {
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
        Class<?> c = Class.forName("javax.management.remote.JMXConnector");
        check(c.isInterface(), "is interface");
        check(Closeable.class.isAssignableFrom(c), "extends Closeable");
        Method[] mm = c.getMethods();
        check(has(mm, "connect"), "connect");
        check(has(mm, "getMBeanServerConnection"), "getMBeanServerConnection");
        check(has(mm, "close"), "close");
        check(has(mm, "addConnectionNotificationListener"), "addConnectionNotificationListener");
        check(has(mm, "removeConnectionNotificationListener"), "removeConnectionNotificationListener");
        check(has(mm, "getConnectionId"), "getConnectionId");
        check(JMXConnector.CREDENTIALS.equals("jmx.remote.credentials"), "CREDENTIALS constant");
    }

    // ---- Part 2: black-box (light behavior) ----
    static void testBlackBox() throws Exception {
        final boolean[] closed = new boolean[1];
        JMXConnector connector = new JMXConnector() {
            boolean connected;
            public void connect() { connected = true; }
            public void connect(Map<String, ?> env) { connected = true; }
            public MBeanServerConnection getMBeanServerConnection() { return null; }
            public void close() { closed[0] = true; }
            public void addConnectionNotificationListener(NotificationListener l, NotificationFilter f, Object h) { }
            public void removeConnectionNotificationListener(NotificationListener l) { }
            public void removeConnectionNotificationListener(NotificationListener l, NotificationFilter f, Object h) { }
            public String getConnectionId() { return "conn-1"; }
        };
        connector.connect();
        check(connector.getConnectionId().equals("conn-1"), "anonymous getConnectionId");
        check(connector.getMBeanServerConnection() == null, "anonymous getMBeanServerConnection");
        connector.close();
        check(closed[0], "close called");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
