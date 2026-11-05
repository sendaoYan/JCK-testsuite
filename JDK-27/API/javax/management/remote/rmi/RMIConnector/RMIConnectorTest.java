/*
 * @test
 * @summary javax.management.remote.rmi.RMIConnector conformance: white-box structure + black-box behavior.
 * @run main RMIConnectorTest
 */

import java.io.IOException;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
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

    public interface CounterMBean { int getCount(); void setCount(int c); int add(int x); }
    public static class Counter implements CounterMBean {
        private int count;
        public int getCount() { return count; }
        public void setCount(int c) { count = c; }
        public int add(int x) { count += x; return count; }
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

    // ---- deepened: structure / serialVersionUID ----
    static void testDeepStructure() throws Exception {
        Class<?> c = Class.forName("javax.management.remote.rmi.RMIConnector");
        check(!Modifier.isFinal(c.getModifiers()), "not final (subclassable)");
        check(c.getConstructor(JMXServiceURL.class, java.util.Map.class) != null,
                "(JMXServiceURL,Map) public ctor");
        check(c.getConstructor(RMIServer.class, java.util.Map.class) != null,
                "(RMIServer,Map) public ctor");
        check(ObjectStreamClass.lookup(c).getSerialVersionUID() == 817323035842634473L,
                "serialVersionUID 817323035842634473");
        check(has(c.getMethods(), "removeConnectionNotificationListener"),
                "removeConnectionNotificationListener public");
    }

    // ---- deepened: unconnected/negative state contract ----
    static void testDeepNegatives() throws Exception {
        checkThrowsMsg(IllegalArgumentException.class, "rmiServer and jmxServiceURL both null",
                new ThrowingRunnable() { public void run() { new RMIConnector((JMXServiceURL) null, null); } },
                "null url ctor");
        checkThrowsMsg(IllegalArgumentException.class, "rmiServer and jmxServiceURL both null",
                new ThrowingRunnable() { public void run() { new RMIConnector((RMIServer) null, null); } },
                "null stub ctor");

        final RMIConnector rc = new RMIConnector(new JMXServiceURL("service:jmx:rmi://127.0.0.1"), null);
        check(rc.getAddress().toString().equals("service:jmx:rmi://127.0.0.1"), "URL ctor preserves address");
        checkThrowsMsg(IOException.class, "Not connected",
                new ThrowingRunnable() { public void run() throws IOException { rc.getConnectionId(); } },
                "unconnected getConnectionId");
        checkThrowsMsg(IOException.class, "Not connected",
                new ThrowingRunnable() { public void run() throws IOException { rc.getMBeanServerConnection(); } },
                "unconnected getMBeanServerConnection");
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
        final ObjectName on = new ObjectName("jck.rc27:type=Counter");
        mbs.registerMBean(new Counter(), on);
        JMXConnectorServer cs = JMXConnectorServerFactory.newJMXConnectorServer(
                new JMXServiceURL("service:jmx:rmi://127.0.0.1"), null, mbs);
        cs.start();
        try {
            JMXConnector c = JMXConnectorFactory.connect(cs.getAddress(), null);
            check(c instanceof RMIConnector, "factory client is RMIConnector");
            String id = c.getConnectionId();
            check(id.startsWith("rmi://127.0.0.1 "), "connection id client-host prefix: " + id);
            MBeanServerConnection msc = c.getMBeanServerConnection();
            check(((Integer) msc.getAttribute(on, "Count")) == 0, "remote getAttribute initial 0");
            check(((Integer) msc.invoke(on, "add", new Object[] { Integer.valueOf(5) },
                    new String[] { "int" })) == 5, "remote invoke add(5) == 5");
            check(msc.getMBeanCount() == 2, "remote getMBeanCount == delegate+counter");
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
                msc.getAttribute(new ObjectName("jck.rc27:type=Absent"), "Count");
                throw new AssertionError("expected InstanceNotFoundException");
            } catch (InstanceNotFoundException e) {
                check("jck.rc27:type=Absent".equals(e.getMessage()), "remote INFE carries object name");
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
            check(id2.startsWith("rmi://127.0.0.1 "), "stub-ctor connect id prefix: " + id2);
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
        testWhiteBox();
        testBlackBox();
        testDeepStructure();
        testDeepNegatives();
        testDeepLoopback();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
