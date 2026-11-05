/*
 * @test
 * @summary javax.management.remote.JMXConnectorServer conformance: white-box structure + black-box behavior.
 * @run main JMXConnectorServerTest
 */
import javax.management.*;
import javax.management.remote.*;


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
        testDeepened();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ==== deepened (2026-11, batch m): lifecycle state machine, notifications, toJMXConnector, forwarder chain ====
    static void deepAwait(int target, java.util.List<?> l) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 5000;
        while (l.size() < target && System.currentTimeMillis() < deadline) Thread.sleep(50);
    }

    static void testDeepened() throws Exception {
        check("jmx.remote.authenticator".equals(JMXConnectorServer.AUTHENTICATOR), "d-AUTHENTICATOR constant");

        MBeanServer server = MBeanServerFactory.newMBeanServer();
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi://127.0.0.1");
        JMXConnectorServer cs = JMXConnectorServerFactory.newJMXConnectorServer(url, null, server);

        // Pre-start state: inactive, address null, no connection ids, empty attributes map.
        check(!cs.isActive(), "d-pre-start inactive");
        check(cs.getAddress() == null, "d-pre-start address null");
        check(cs.getConnectionIds().length == 0, "d-pre-start no ids");
        check(cs.getAttributes().isEmpty(), "d-getAttributes empty");
        check(cs.getMBeanServer() == server, "d-getMBeanServer identity");
        // getNotificationInfo advertises the three connection notification types.
        MBeanNotificationInfo[] ni = cs.getNotificationInfo();
        check(ni.length == 1, "d-one notification info");
        java.util.List<String> types = java.util.Arrays.asList(ni[0].getNotifTypes());
        check(types.contains("jmx.remote.connection.opened"), "d-info opened");
        check(types.contains("jmx.remote.connection.closed"), "d-info closed");
        check(types.contains("jmx.remote.connection.failed"), "d-info failed");
        check(!types.contains("jmx.remote.connection.notifs.lost"), "d-info lacks notifs.lost");
        check("javax.management.remote.JMXConnectionNotification".equals(ni[0].getName()), "d-info class name");

        // Null forwarder rejected by the base class with a precise message.
        try {
            cs.setMBeanServerForwarder(null);
            check(false, "d-null forwarder should throw");
        } catch (IllegalArgumentException e) {
            check("Invalid null argument: mbsf".equals(e.getMessage()), "d-null forwarder message: " + e.getMessage());
        }

        final java.util.List<Notification> seen =
                java.util.Collections.synchronizedList(new java.util.ArrayList<Notification>());
        cs.addNotificationListener(new NotificationListener() {
            public void handleNotification(Notification n, Object h) { seen.add(n); }
        }, null, null);

        cs.start();
        try {
            check(cs.isActive(), "d-post-start active");
            check(cs.getAddress() != null, "d-post-start address non-null");
            check("rmi".equals(cs.getAddress().getProtocol()), "d-address protocol rmi");
            check("127.0.0.1".equals(cs.getAddress().getHost()), "d-address host loopback");
            // Second start is a no-op (recorded idempotence, both JDKs).
            cs.start();
            check(cs.isActive(), "d-start idempotent");

            JMXConnector conn = JMXConnectorFactory.connect(cs.getAddress(), null);
            String id = conn.getConnectionId();
            check(java.util.Arrays.asList(cs.getConnectionIds()).contains(id), "d-connectionIds contains client id");
            conn.close();
            deepAwait(2, seen);
            check(cs.getConnectionIds().length == 0, "d-connectionIds empty after close");
            check(seen.size() == 2, "d-two connection notifications");
            if (seen.size() == 2) {
                check(JMXConnectionNotification.OPENED.equals(seen.get(0).getType()), "d-OPENED emitted");
                check(JMXConnectionNotification.CLOSED.equals(seen.get(1).getType()), "d-CLOSED emitted");
                check(id.equals(((JMXConnectionNotification) seen.get(0)).getConnectionId()), "d-OPENED id");
            }
            // toJMXConnector on an active server yields a usable client connector.
            JMXConnector viaCs = cs.toJMXConnector(null);
            check(viaCs != null && viaCs != conn, "d-toJMXConnector returns fresh connector");
            try {
                viaCs.connect(null);
                check(viaCs.getConnectionId() != null, "d-toJMXConnector connectable");
            } finally {
                viaCs.close();
            }
        } finally {
            cs.stop();
        }
        check(!cs.isActive(), "d-post-stop inactive");
        check(cs.getAddress() == null, "d-post-stop address null");
        cs.stop();
        check(!cs.isActive(), "d-stop idempotent");
        // Restarting a stopped server is forbidden with a stable message (both JDKs).
        try {
            cs.start();
            check(false, "d-restart should throw");
        } catch (java.io.IOException e) {
            check("The server has been stopped.".equals(e.getMessage()), "d-restart message: " + e.getMessage());
        }
        // toJMXConnector on an inactive server.
        try {
            cs.toJMXConnector(null);
            check(false, "d-toJMXConnector inactive should throw");
        } catch (IllegalStateException e) {
            check("Connector is not active".equals(e.getMessage()), "d-toJMXConnector inactive message: " + e.getMessage());
        }

        // stop() before any start() is a silent no-op; the server can never be started afterwards.
        JMXConnectorServer neverStarted = JMXConnectorServerFactory.newJMXConnectorServer(
                new JMXServiceURL("service:jmx:rmi://127.0.0.1"), null, server);
        neverStarted.stop();
        check(!neverStarted.isActive(), "d-stop-before-start keeps inactive");
        try {
            neverStarted.start();
            check(false, "d-start after premature stop should throw");
        } catch (java.io.IOException e) {
            check("The server has been stopped.".equals(e.getMessage()), "d-premature stop message");
        }

        // A server created without an MBeanServer cannot be started.
        JMXConnectorServer unattached = JMXConnectorServerFactory.newJMXConnectorServer(
                new JMXServiceURL("service:jmx:rmi://127.0.0.1"), null, null);
        check(unattached.getMBeanServer() == null, "d-unattached null server");
        try {
            unattached.start();
            check(false, "d-unattached start should throw");
        } catch (IllegalStateException e) {
            check("This connector server is not attached to an MBean server".equals(e.getMessage()),
                    "d-unattached message: " + e.getMessage());
        }

        // The protected JMXConnectorServer(MBeanServer) constructor wires getMBeanServer; the base
        // class supplies getConnectionIds and getNotificationInfo for free.
        JMXConnectorServer bare = new JMXConnectorServer(server) {
            public void start() { }
            public void stop() { }
            public boolean isActive() { return false; }
            public JMXServiceURL getAddress() { return null; }
            public java.util.Map<String, ?> getAttributes() { return new java.util.HashMap<String, Object>(); }
        };
        check(bare.getMBeanServer() == server, "d-protected ctor wires server");
        check(!bare.isActive(), "d-bare inactive");
        check(bare.getConnectionIds().length == 0, "d-bare no ids");
        check(bare.getNotificationInfo().length == 1, "d-bare notification info");
    }

}
