/*
 * @test
 * @summary javax.management.remote.JMXConnectorServer conformance: white-box structure + black-box behavior.
 * @run main JMXConnectorServerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.management.ManagementFactory;
import java.util.Map;

import javax.management.MBeanNotificationInfo;
import javax.management.MBeanServer;
import javax.management.NotificationBroadcasterSupport;
import javax.management.remote.JMXAddressable;
import javax.management.remote.JMXConnectionNotification;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerMBean;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.MBeanServerForwarder;

public class JMXConnectorServerTest {
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
        Class<?> c = Class.forName("javax.management.remote.JMXConnectorServer");
        check(Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(NotificationBroadcasterSupport.class.isAssignableFrom(c), "extends NotificationBroadcasterSupport");
        check(JMXConnectorServerMBean.class.isAssignableFrom(c), "implements JMXConnectorServerMBean");
        check(JMXAddressable.class.isAssignableFrom(c), "implements JMXAddressable");
        Method[] mm = c.getMethods();
        check(has(mm, "getMBeanServer"), "getMBeanServer");
        check(has(mm, "setMBeanServerForwarder"), "setMBeanServerForwarder");
        check(has(mm, "getConnectionIds"), "getConnectionIds");
        check(has(mm, "toJMXConnector"), "toJMXConnector");
        check(has(mm, "getNotificationInfo"), "getNotificationInfo");
        check(JMXConnectorServer.AUTHENTICATOR.equals("jmx.remote.authenticator"), "AUTHENTICATOR constant");
    }

    // ---- Part 2: black-box (light behavior) ----
    static void testBlackBox() throws Exception {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        final boolean[] started = new boolean[1];
        JMXConnectorServer server = new JMXConnectorServer(mbs) {
            public void start() { started[0] = true; }
            public void stop() { started[0] = false; }
            public boolean isActive() { return started[0]; }
            public JMXServiceURL getAddress() { return null; }
            public Map<String, ?> getAttributes() { return java.util.Collections.emptyMap(); }
        };

        check(server.getMBeanServer() == mbs, "getMBeanServer preserved");
        check(server.getConnectionIds().length == 0, "no connections initially");
        check(!server.isActive(), "not active initially");

        MBeanNotificationInfo[] infos = server.getNotificationInfo();
        check(infos.length == 1, "single notification info");
        check(infos[0].getNotifTypes().length == 3, "three notification types");
        check(infos[0].getNotifTypes()[0].equals(JMXConnectionNotification.OPENED), "OPENED first");
        check(infos[0].getNotifTypes()[1].equals(JMXConnectionNotification.CLOSED), "CLOSED second");
        check(infos[0].getNotifTypes()[2].equals(JMXConnectionNotification.FAILED), "FAILED third");

        server.start();
        check(server.isActive(), "start -> active");
        server.stop();
        check(!server.isActive(), "stop -> inactive");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
