/*
 * @test
 * @summary javax.management.remote.JMXConnectionNotification conformance: white-box structure + black-box behavior.
 * @run main JMXConnectionNotificationTest
 */
import javax.management.*;
import javax.management.remote.*;


import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import javax.management.Notification;
import javax.management.remote.JMXConnectionNotification;

public class JMXConnectionNotificationTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.remote.JMXConnectionNotification");
        check(Notification.class.isAssignableFrom(c), "extends Notification");
        Constructor<?> k = c.getConstructor(String.class, Object.class, String.class, long.class, String.class, Object.class);
        check(k != null, "6-arg ctor");

        // Public constant fields are static final Strings.
        check(Modifier.isStatic(c.getField("OPENED").getModifiers()), "OPENED static");
        check(Modifier.isStatic(c.getField("CLOSED").getModifiers()), "CLOSED static");
        check(Modifier.isStatic(c.getField("FAILED").getModifiers()), "FAILED static");
        check(Modifier.isStatic(c.getField("NOTIFS_LOST").getModifiers()), "NOTIFS_LOST static");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(JMXConnectionNotification.OPENED.equals("jmx.remote.connection.opened"), "OPENED value");
        check(JMXConnectionNotification.CLOSED.equals("jmx.remote.connection.closed"), "CLOSED value");
        check(JMXConnectionNotification.FAILED.equals("jmx.remote.connection.failed"), "FAILED value");
        check(JMXConnectionNotification.NOTIFS_LOST.equals("jmx.remote.connection.notifs.lost"), "NOTIFS_LOST value");

        Object source = new Object();
        JMXConnectionNotification n =
            new JMXConnectionNotification(JMXConnectionNotification.OPENED, source, "conn-7", 99L, "opened", "extra");
        check(n.getConnectionId().equals("conn-7"), "connection id");
        check(n.getType().equals(JMXConnectionNotification.OPENED), "type propagated to Notification");
        check(n.getSource() == source, "source propagated");
        check(n.getSequenceNumber() == 99L, "sequence number");
        check(n.getMessage().equals("opened"), "message");
        check(n.getUserData().equals("extra"), "user data");

        // A notification for each of the other standard types.
        JMXConnectionNotification closed =
            new JMXConnectionNotification(JMXConnectionNotification.CLOSED, source, "conn-7", 100L, null, null);
        check(closed.getType().equals(JMXConnectionNotification.CLOSED), "closed type");
        check(closed.getMessage() == null, "null message allowed");
        check(closed.getUserData() == null, "null user data allowed");

        // Null type/source/connectionId each throw NullPointerException with stable message.
        try {
            new JMXConnectionNotification(null, source, "c", 1L, null, null);
            check(false, "expected NPE for null type");
        } catch (NullPointerException e) {
            check(e.getMessage().equals("Illegal null argument"), "null type message: " + e.getMessage());
        }
        try {
            new JMXConnectionNotification(JMXConnectionNotification.OPENED, null, "c", 1L, null, null);
            check(false, "expected NPE for null source");
        } catch (NullPointerException e) {
            check(e.getMessage().equals("Illegal null argument"), "null source message: " + e.getMessage());
        }
        try {
            new JMXConnectionNotification(JMXConnectionNotification.OPENED, source, null, 1L, null, null);
            check(false, "expected NPE for null connectionId");
        } catch (NullPointerException e) {
            check(e.getMessage().equals("Illegal null argument"), "null connectionId message: " + e.getMessage());
        }
        try {
            new JMXConnectionNotification(JMXConnectionNotification.OPENED, source, "c", -1L, null, null);
            check(false, "expected IAE for negative sequence");
        } catch (IllegalArgumentException e) {
            check(e.getMessage().equals("Negative sequence number"), "negative seq message: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepened();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ==== deepened (2026-11, batch m): constants, ctor contract, serialization, real OPENED/CLOSED over RMI loopback ====
    static void deepAwait(int target, java.util.List<?> l) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 5000;
        while (l.size() < target && System.currentTimeMillis() < deadline) Thread.sleep(50);
    }

    static void testDeepened() throws Exception {
        check("jmx.remote.connection.opened".equals(JMXConnectionNotification.OPENED), "d-OPENED constant");
        check("jmx.remote.connection.closed".equals(JMXConnectionNotification.CLOSED), "d-CLOSED constant");
        check("jmx.remote.connection.failed".equals(JMXConnectionNotification.FAILED), "d-FAILED constant");
        check("jmx.remote.connection.notifs.lost".equals(JMXConnectionNotification.NOTIFS_LOST), "d-NOTIFS_LOST constant");

        // Constructor: every getter round-trips, Notification-inherited fields included.
        JMXConnectionNotification cn = new JMXConnectionNotification(
                JMXConnectionNotification.OPENED, "src", "conn-7", 42L, "opened!", "extra");
        check("jmx.remote.connection.opened".equals(cn.getType()), "d-type");
        check("conn-7".equals(cn.getConnectionId()), "d-connectionId");
        check("src".equals(cn.getSource()), "d-source");
        check(cn.getSequenceNumber() == 42L, "d-sequence");
        check("opened!".equals(cn.getMessage()), "d-message");
        check("extra".equals(cn.getUserData()), "d-userData");
        check(cn.getTimeStamp() >= 0, "d-timestamp non-negative");
        // Null type / null connectionId -> NPE with the shared message (both JDKs).
        try {
            new JMXConnectionNotification(null, "s", "c", 1L, "m", null);
            check(false, "d-null type should throw");
        } catch (NullPointerException e) {
            check("Illegal null argument".equals(e.getMessage()), "d-null type message: " + e.getMessage());
        }
        try {
            new JMXConnectionNotification("t", "s", null, 1L, "m", null);
            check(false, "d-null connectionId should throw");
        } catch (NullPointerException e) {
            check("Illegal null argument".equals(e.getMessage()), "d-null connectionId message: " + e.getMessage());
        }
        // Serialization round-trip.
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = null;
        try {
            oos = new java.io.ObjectOutputStream(bos);
            oos.writeObject(cn);
        } finally {
            if (oos != null) oos.close();
        }
        java.io.ObjectInputStream ois = null;
        try {
            ois = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray()));
            JMXConnectionNotification back = (JMXConnectionNotification) ois.readObject();
            check("conn-7".equals(back.getConnectionId()), "d-ser connectionId");
            check("jmx.remote.connection.opened".equals(back.getType()), "d-ser type");
            check(back.getSequenceNumber() == 42L, "d-ser sequence");
        } finally {
            if (ois != null) ois.close();
        }

        // Real closed loop over 127.0.0.1 RMI (stub-embedded URL, no registry, no fixed port):
        // a connector server emits OPENED then CLOSED carrying the client's connection id.
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi://127.0.0.1");
        JMXConnectorServer cs = JMXConnectorServerFactory.newJMXConnectorServer(url, null, server);
        final java.util.List<Notification> seen =
                java.util.Collections.synchronizedList(new java.util.ArrayList<Notification>());
        cs.addNotificationListener(new NotificationListener() {
            public void handleNotification(Notification n, Object h) { seen.add(n); }
        }, null, null);
        cs.start();
        try {
            JMXConnector conn = JMXConnectorFactory.connect(cs.getAddress(), null);
            String id = conn.getConnectionId();
            conn.close();
            deepAwait(2, seen);
            check(seen.size() == 2, "d-exactly OPENED+CLOSED, got " + seen.size());
            if (seen.size() == 2) {
                Notification opened = seen.get(0);
                Notification closed = seen.get(1);
                check(opened instanceof JMXConnectionNotification, "d-OPENED is JMXConnectionNotification");
                check(closed instanceof JMXConnectionNotification, "d-CLOSED is JMXConnectionNotification");
                check(JMXConnectionNotification.OPENED.equals(opened.getType()), "d-first is OPENED");
                check(JMXConnectionNotification.CLOSED.equals(closed.getType()), "d-second is CLOSED");
                check(id.equals(((JMXConnectionNotification) opened).getConnectionId()), "d-OPENED id matches client");
                check(id.equals(((JMXConnectionNotification) closed).getConnectionId()), "d-CLOSED id matches client");
                check(opened.getSource() == cs, "d-source is the connector server");
                check("Connection opened".equals(opened.getMessage()), "d-OPENED message");
                check("Client connection closed".equals(closed.getMessage()), "d-CLOSED message");
                check(closed.getSequenceNumber() > opened.getSequenceNumber(), "d-sequence numbers increase");
            }
        } finally {
            cs.stop();
        }
    }

}
