/*
 * @test
 * @summary javax.management.remote.rmi.RMIConnector conformance: concrete class reflection.
 * @run main RMIConnectorTest
 */

import java.io.IOException;
import java.io.ObjectStreamClass;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerFactory;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXAddressable;
import javax.management.remote.JMXConnectionNotification;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnector;
import javax.management.remote.rmi.RMIServer;

public class RMIConnectorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static void checkThrowsMsg(Class<? extends Throwable> expected, String message,
                               ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t) && message.equals(t.getMessage())) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + "(" + message + ") but got " + t.getClass().getName() + "(" + t.getMessage() + ")", t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    public static interface CounterMBean { int getCount(); void setCount(int c); int add(int x); }
    public static class Counter implements CounterMBean {
        private int count;
        public int getCount() { return count; }
        public void setCount(int c) { count = c; }
        public int add(int x) { count += x; return count; }
    }

    // ---- deepened: structure / serialVersionUID ----
    static void testDeepStructure() throws Exception {
        Class<?> c = Class.forName("javax.management.remote.rmi.RMIConnector");
        check(!java.lang.reflect.Modifier.isFinal(c.getModifiers()), "not final (subclassable)");
        check(c.getConstructor(JMXServiceURL.class, java.util.Map.class) != null,
                "(JMXServiceURL,Map) public ctor");
        check(c.getConstructor(RMIServer.class, java.util.Map.class) != null,
                "(RMIServer,Map) public ctor");
        check(hasMethod(c, "removeConnectionNotificationListener"),
                "declares removeConnectionNotificationListener");
        check(ObjectStreamClass.lookup(c).getSerialVersionUID() == 817323035842634473L,
                "serialVersionUID 817323035842634473");
        check(JMXAddressable.class.isAssignableFrom(c), "JMXAddressable for getAddress()");
    }

    // ---- deepened: unconnected/negative state contract ----
    static void testDeepNegatives() throws Exception {
        checkThrowsMsg(IllegalArgumentException.class, "rmiServer and jmxServiceURL both null",
                new ThrowingRunnable() { public void run() { new RMIConnector((JMXServiceURL) null, null); } },
                "null url ctor");
        checkThrowsMsg(IllegalArgumentException.class, "rmiServer and jmxServiceURL both null",
                new ThrowingRunnable() { public void run() { new RMIConnector((RMIServer) null, null); } },
                "null stub ctor");

        RMIConnector rc = new RMIConnector(new JMXServiceURL("service:jmx:rmi://127.0.0.1"), null);
        check(rc.getAddress().toString().equals("service:jmx:rmi://127.0.0.1"), "URL ctor preserves address");
        check(rc.toString().startsWith("javax.management.remote.rmi.RMIConnector: jmxServiceURL="),
                "toString carries prefix+url: " + rc.toString());
        checkThrowsMsg(IOException.class, "Not connected",
                new ThrowingRunnable() { public void run() throws IOException { rc.getConnectionId(); } },
                "unconnected getConnectionId");
        checkThrowsMsg(IOException.class, "Not connected",
                new ThrowingRunnable() { public void run() throws IOException { rc.getMBeanServerConnection(); } },
                "unconnected getMBeanServerConnection");
        checkThrowsMsg(NullPointerException.class, "listener",
                new ThrowingRunnable() { public void run() { rc.addConnectionNotificationListener(null, null, null); } },
                "null listener add");
        checkThrowsMsg(NullPointerException.class, "listener",
                new ThrowingRunnable() { public void run() throws Exception { rc.removeConnectionNotificationListener(null); } },
                "null listener remove");
        checkThrows(javax.management.ListenerNotFoundException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    rc.removeConnectionNotificationListener(new NotificationListener() {
                        public void handleNotification(Notification n, Object h) { } });
                } }, "remove unregistered listener");
        rc.close(); // unconnected close is safe and idempotent
        rc.close();
        checkThrowsMsg(IOException.class, "Connector closed",
                new ThrowingRunnable() { public void run() throws IOException { rc.connect(null); } },
                "connect after close");

        // connect() to a dead RMI registry port fails with a descriptive IOException.
        final int deadPort;
        ServerSocket ss = new ServerSocket(0);
        deadPort = ss.getLocalPort();
        ss.close();
        final RMIConnector dead = new RMIConnector(
                new JMXServiceURL("service:jmx:rmi:///jndi/rmi://127.0.0.1:" + deadPort + "/jckrmi"), null);
        try {
            dead.connect(null);
            throw new AssertionError("dead-port connect: expected IOException but nothing was thrown");
        } catch (IOException e) {
            check(e.getMessage() != null && e.getMessage().startsWith("Failed to retrieve RMIServer stub"),
                    "dead-port IOException message prefix: " + e.getMessage());
            check(e.getCause() != null, "dead-port IOException has cause chain");
        }
        dead.close();
    }

    // ---- deepened: RMI loopback closed-loop (stub-encoded URL, no registry) ----
    static void testDeepLoopback() throws Exception {
        MBeanServer mbs = MBeanServerFactory.newMBeanServer();
        final ObjectName on = new ObjectName("jck.rc:type=Counter");
        mbs.registerMBean(new Counter(), on);
        JMXConnectorServer cs = JMXConnectorServerFactory.newJMXConnectorServer(
                new JMXServiceURL("service:jmx:rmi://127.0.0.1"), null, mbs);
        cs.start();
        try {
            JMXConnector c = JMXConnectorFactory.connect(cs.getAddress(), null);
            check(c instanceof RMIConnector, "factory client is RMIConnector");
            check(c.getClass().getName().equals("javax.management.remote.rmi.RMIConnector"),
                    "client class name: " + c.getClass().getName());
            String id = c.getConnectionId();
            check(id.startsWith("rmi://"), "connection id client-host prefix: " + id);
            MBeanServerConnection msc = c.getMBeanServerConnection();
            check(((Integer) msc.getAttribute(on, "Count")) == 0, "remote getAttribute initial 0");
            check(((Integer) msc.invoke(on, "add", new Object[] { Integer.valueOf(5) },
                    new String[] { "int" })) == 5, "remote invoke add(5) == 5");
            check(((Integer) msc.getAttribute(on, "Count")) == 5, "remote getAttribute after add == 5");
            check(msc.getMBeanCount() == 2, "remote getMBeanCount == delegate+counter");
            check(msc.isRegistered(on), "remote isRegistered true");
            c.connect(null); // second connect is a documented no-op
            check(c.getConnectionId().equals(id), "idempotent connect keeps same id");
            try {
                msc.getAttribute(on, "Nope");
                throw new AssertionError("expected AttributeNotFoundException");
            } catch (AttributeNotFoundException e) {
                check("No such attribute: Nope".equals(e.getMessage()), "remote ANFE message");
            }
            try {
                msc.invoke(on, "nope", new Object[0], new String[0]);
                throw new AssertionError("expected ReflectionException");
            } catch (ReflectionException e) {
                check("No such operation: nope".equals(e.getMessage()), "remote RE message");
            }
            try {
                msc.getAttribute(new ObjectName("jck.rc:type=Absent"), "Count");
                throw new AssertionError("expected InstanceNotFoundException");
            } catch (InstanceNotFoundException e) {
                check("jck.rc:type=Absent".equals(e.getMessage()), "remote INFE carries object name");
            }

            // toJMXConnector() yields an RMIConnector built from the live RMIServer stub.
            final RMIConnector viaStub = (RMIConnector) cs.toJMXConnector(null);
            check(viaStub != null, "toJMXConnector returns RMIConnector");
            check(viaStub.getAddress() == null, "stub-built RMIConnector has no address");
            final List<Notification> notifs = new ArrayList<Notification>();
            viaStub.addConnectionNotificationListener(new NotificationListener() {
                public void handleNotification(Notification n, Object h) { notifs.add(n); }
            }, null, null);
            viaStub.connect(null);
            String id2 = viaStub.getConnectionId();
            check(id2.startsWith("rmi://"), "stub-ctor connect id prefix: " + id2);
            check(((Integer) viaStub.getMBeanServerConnection().getAttribute(on, "Count")) == 5,
                    "stub-ctor remote read matches server state");
            viaStub.close();
            check(notifs.size() == 2, "two client-side notifications");
            if (notifs.size() == 2) {
                JMXConnectionNotification opened = (JMXConnectionNotification) notifs.get(0);
                JMXConnectionNotification closed = (JMXConnectionNotification) notifs.get(1);
                check(opened.getType().equals("jmx.remote.connection.opened"), "OPENED type");
                check(closed.getType().equals("jmx.remote.connection.closed"), "CLOSED type");
                check(opened.getConnectionId().equals(id2), "OPENED id matches");
                check(closed.getConnectionId().equals(id2), "CLOSED id matches");
                check(opened.getSource() == viaStub, "notification source is the connector");
            }
            checkThrowsMsg(IOException.class, "Not connected",
                    new ThrowingRunnable() { public void run() throws IOException { viaStub.getConnectionId(); } },
                    "getConnectionId after close");
            checkThrowsMsg(IOException.class, "Connection closed",
                    new ThrowingRunnable() { public void run() throws IOException { viaStub.getMBeanServerConnection(); } },
                    "getMBeanServerConnection after close");
            viaStub.close(); // close is idempotent
            c.close();
        } finally {
            cs.stop();
        }
        check(!cs.isActive(), "server stopped after loopback");
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.remote.rmi.RMIConnector");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check(Class.forName("javax.management.remote.JMXConnector").isAssignableFrom(c), "implements/extends javax.management.remote.JMXConnector");
        check(Class.forName("java.io.Serializable").isAssignableFrom(c), "implements/extends java.io.Serializable");
        check(Class.forName("javax.management.remote.JMXAddressable").isAssignableFrom(c), "implements/extends javax.management.remote.JMXAddressable");
        check(hasMethod(c, "addConnectionNotificationListener"), "declares addConnectionNotificationListener");
        check(hasMethod(c, "close"), "declares close");
        check(hasMethod(c, "connect"), "declares connect");
        testDeepStructure();
        testDeepNegatives();
        testDeepLoopback();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
