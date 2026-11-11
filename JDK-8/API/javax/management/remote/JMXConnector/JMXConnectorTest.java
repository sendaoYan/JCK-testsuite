/*
 * @test
 * @summary javax.management.remote.JMXConnector conformance: interface reflection.
 * @run main JMXConnectorTest
 */
import javax.management.*;
import javax.management.remote.*;


public class JMXConnectorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.remote.JMXConnector");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(Class.forName("java.io.Closeable").isAssignableFrom(c), "implements/extends java.io.Closeable");
        check(hasMethod(c, "addConnectionNotificationListener"), "declares addConnectionNotificationListener");
        check(hasMethod(c, "close"), "declares close");
        check(hasMethod(c, "connect"), "declares connect");
        testDeepened();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ==== deepened (2026-11, batch m): full remote op matrix, lifecycle, client-side notifications ====
    public static interface RemCounterMBean {
        int getCount();
        void setCount(int c);
        int add(int x);
    }
    public static class RemCounter implements RemCounterMBean {
        private int count;
        public int getCount() { return count; }
        public void setCount(int c) { count = c; }
        public int add(int x) { count += x; return count; }
    }
    static void deepAwait(int target, java.util.List<?> l) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 5000;
        while (l.size() < target && System.currentTimeMillis() < deadline) Thread.sleep(50);
    }

    static void testDeepened() throws Exception {
        check("jmx.remote.credentials".equals(JMXConnector.CREDENTIALS), "d-CREDENTIALS constant");

        MBeanServer server = MBeanServerFactory.newMBeanServer();
        ObjectName on = new ObjectName("jck.deep:type=Counter");
        server.registerMBean(new RemCounter(), on);
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi://127.0.0.1");
        JMXConnectorServer cs = JMXConnectorServerFactory.newJMXConnectorServer(url, null, server);
        cs.start();
        try {
            JMXConnector conn = JMXConnectorFactory.connect(cs.getAddress(), null);
            String id = conn.getConnectionId();
            check(id != null && id.startsWith("rmi://"), "d-connectionId prefix, got " + id);
            check(java.util.Arrays.asList(cs.getConnectionIds()).contains(id), "d-server knows client id");
            // A second connect on the same connector is a documented no-op.
            conn.connect(null);
            check(conn.getConnectionId().equals(id), "d-connect idempotent keeps id");

            MBeanServerConnection mbsc = conn.getMBeanServerConnection();
            check(conn.getMBeanServerConnection() == mbsc, "d-two getMBeanServerConnection same instance");
            // Subject delegation (JDK 8): a delegated connection object is handed out even without an
            // authenticated connection, but the first remote op is refused until delegation is authorized.
            javax.security.auth.Subject deleg = new javax.security.auth.Subject(true,
                    new java.util.HashSet(java.util.Arrays.asList(new JMXPrincipal("delegate"))),
                    new java.util.HashSet(), new java.util.HashSet());
            MBeanServerConnection delegated = conn.getMBeanServerConnection(deleg);
            check(delegated != null && delegated != mbsc, "d-delegated connection distinct");
            try {
                delegated.getAttribute(on, "Count");
                check(false, "d-unauthorized delegation op should throw");
            } catch (java.lang.SecurityException e) {
                check(String.valueOf(e.getMessage())
                        .contains("Subject delegation cannot be enabled unless an authenticated subject"),
                        "d-delegation refusal message: " + e.getMessage());
            }
            // ---- full remote operation matrix vs the local server ----
            check(((Integer) mbsc.getAttribute(on, "Count")).intValue() == 0, "d-remote getAttribute initial");
            check(((Integer) mbsc.invoke(on, "add", new Object[]{ Integer.valueOf(7) },
                    new String[]{ "int" })).intValue() == 7, "d-remote invoke add returns");
            mbsc.setAttribute(on, new Attribute("Count", Integer.valueOf(3)));
            check(((Integer) server.getAttribute(on, "Count")).intValue() == 3, "d-remote setAttribute visible locally");
            check(((Integer) mbsc.getAttribute(on, "Count")).intValue() == 3, "d-remote readback");
            java.util.Set<ObjectName> names = mbsc.queryNames(new ObjectName("jck.deep:*"), null);
            check(names.contains(on) && names.size() == 1, "d-remote queryNames");
            MBeanInfo info = mbsc.getMBeanInfo(on);
            check(info.getAttributes().length == 1, "d-remote MBeanInfo attrs (Count folds get+set)");
            check(info.getOperations().length == 1, "d-remote MBeanInfo ops (add)");
            check("DefaultDomain".equals(mbsc.getDefaultDomain()), "d-remote getDefaultDomain");
            int countBefore = server.getMBeanCount().intValue();
            check(mbsc.getMBeanCount().intValue() == countBefore, "d-remote getMBeanCount");
            ObjectName created = new ObjectName("jck.deep:type=Created");
            ObjectInstance oi = mbsc.createMBean(RemCounter.class.getName(), created);
            check(created.equals(oi.getObjectName()), "d-remote createMBean name");
            check(mbsc.isRegistered(created) && server.isRegistered(created), "d-remote createMBean registered both sides");
            mbsc.unregisterMBean(created);
            check(!server.isRegistered(created), "d-remote unregister visible locally");
            try {
                mbsc.getAttribute(new ObjectName("jck.deep:type=Missing"), "Count");
                check(false, "d-unknown name getAttribute should throw");
            } catch (javax.management.InstanceNotFoundException e) {
                passed++;
            }

            // ---- client-side connection notification on close ----
            final java.util.List<Notification> seen =
                    java.util.Collections.synchronizedList(new java.util.ArrayList<Notification>());
            conn.addConnectionNotificationListener(new NotificationListener() {
                public void handleNotification(Notification n, Object h) { seen.add(n); }
            }, null, "handback");
            // removeConnectionNotificationListener with an unregistered listener.
            try {
                conn.removeConnectionNotificationListener(new NotificationListener() {
                    public void handleNotification(Notification n, Object h) { }
                });
                check(false, "d-remove unknown listener should throw");
            } catch (javax.management.ListenerNotFoundException e) {
                check("Listener not registered".equals(e.getMessage()), "d-remove unknown listener message: " + e.getMessage());
            }
            conn.close();
            deepAwait(1, seen);
            check(seen.size() == 1, "d-one client-side notification, got " + seen.size());
            if (seen.size() == 1) {
                Notification n = seen.get(0);
                check(JMXConnectionNotification.CLOSED.equals(n.getType()), "d-client sees CLOSED");
                check(n instanceof JMXConnectionNotification
                        && id.equals(((JMXConnectionNotification) n).getConnectionId()), "d-client CLOSED id matches");
                check(n.getSource() == conn, "d-client notification source is the connector");
            }
            // close is idempotent.
            conn.close();
            // After close, every operational path raises IOException (exact messages recorded per JDK).
            try {
                mbsc.getDefaultDomain();
                check(false, "d-op after close should throw");
            } catch (java.io.IOException e) {
                check("The client has been closed.".equals(e.getMessage()), "d-op after close message: " + e.getMessage());
            }
            try {
                conn.getMBeanServerConnection();
                check(false, "d-getMBeanServerConnection after close should throw");
            } catch (java.io.IOException e) {
                passed++;
            }
            try {
                conn.getConnectionId();
                check(false, "d-getConnectionId after close should throw");
            } catch (java.io.IOException e) {
                check("Not connected".equals(e.getMessage()), "d-getConnectionId after close message: " + e.getMessage());
            }
        } finally {
            cs.stop();
        }

        // A connector built but never connected: getConnectionId reports Not connected (both JDKs).
        JMXConnector unconnected = JMXConnectorFactory.newJMXConnector(
                new JMXServiceURL("service:jmx:rmi:///jndi/rmi://127.0.0.1:1/x"), null);
        try {
            unconnected.getConnectionId();
            check(false, "d-unconnected getConnectionId should throw");
        } catch (java.io.IOException e) {
            check("Not connected".equals(e.getMessage()), "d-unconnected message: " + e.getMessage());
        } finally {
            unconnected.close();
        }
    }

}
