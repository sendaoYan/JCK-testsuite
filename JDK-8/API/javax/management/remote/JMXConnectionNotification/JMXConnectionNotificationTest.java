/*
 * @test
 * @summary javax.management.remote.JMXConnectionNotification conformance: concrete class reflection.
 * @run main JMXConnectionNotificationTest
 */
import javax.management.*;
import javax.management.remote.*;


public class JMXConnectionNotificationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.remote.JMXConnectionNotification");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("javax.management.Notification".equals(c.getSuperclass().getName()), "superclass javax.management.Notification");
        check(hasMethod(c, "getConnectionId"), "declares getConnectionId");
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
