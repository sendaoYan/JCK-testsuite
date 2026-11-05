/*
 * @test
 * @summary javax.management.remote.rmi.RMIConnectionImpl conformance: concrete class reflection.
 * @run main RMIConnectionImplTest
 */

import java.io.IOException;
import java.rmi.MarshalledObject;
import java.rmi.Remote;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.RuntimeOperationsException;
import javax.security.auth.Subject;

import javax.management.remote.NotificationResult;
import javax.management.remote.rmi.RMIConnection;
import javax.management.remote.rmi.RMIConnectionImpl;
import javax.management.remote.rmi.RMIServerImpl;

public class RMIConnectionImplTest {
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

    // Minimal RMIServerImpl so RMIConnectionImpl can be constructed directly, locally.
    public static class TinyServer extends RMIServerImpl {
        public TinyServer() { super(null); }
        public Remote toStub() { return null; }
        protected void export() { }
        protected RMIConnection makeClient(String id, Subject s) { return null; }
        protected void closeClient(RMIConnection c) { }
        protected String getProtocol() { return "rmi"; }
        protected void closeServer() { }
        protected void clientClosed(RMIConnection c) { } // skip connector-server bookkeeping
    }

    static TinyServer tinyWith(MBeanServer mbs) {
        TinyServer t = new TinyServer();
        t.setMBeanServer(mbs);
        return t;
    }

    // ---- deepened: constructor contract ----
    static void testDeepCtor() throws Exception {
        checkThrowsMsg(NullPointerException.class, "Illegal null argument",
                new ThrowingRunnable() { public void run() {
                    new RMIConnectionImpl(null, "id", null, null, null); } },
                "null rmiServer");
        checkThrowsMsg(NullPointerException.class, "Illegal null argument",
                new ThrowingRunnable() { public void run() {
                    new RMIConnectionImpl(tinyWith(MBeanServerFactory.newMBeanServer()), null, null, null, null); } },
                "null connectionId");
        // null env is tolerated (treated as empty map)
        RMIConnectionImpl ok = new RMIConnectionImpl(
                tinyWith(MBeanServerFactory.newMBeanServer()), "jck-id", null, null, null);
        check(ok.getConnectionId().equals("jck-id"), "null env accepted");
        ok.close();
    }

    // ---- deepened: local MBeanServer routing through the RMIConnection surface ----
    static void testDeepLocalRouting() throws Exception {
        MBeanServer mbs = MBeanServerFactory.newMBeanServer();
        final ObjectName on = new ObjectName("jck.ci:type=Counter");
        mbs.registerMBean(new Counter(), on);
        RMIConnectionImpl conn = new RMIConnectionImpl(tinyWith(mbs), "jck-conn-42", null, null, null);

        check(conn.getConnectionId().equals("jck-conn-42"), "connection id round-trip");
        check(conn.toString().contains("connectionId=jck-conn-42"), "toString carries connectionId");
        check(((Integer) conn.getAttribute(on, "Count", null)) == 0, "getAttribute routes to server");
        check(((Integer) conn.invoke(on, "add",
                new MarshalledObject<Object>(new Object[] { Integer.valueOf(3) }),
                new String[] { "int" }, null)) == 3, "invoke add(3) routes with unmarshalled args");
        conn.setAttribute(on, new MarshalledObject<Object>(new Attribute("Count", 11)), null);
        check(((Integer) conn.getAttribute(on, "Count", null)) == 11, "setAttribute routes + read-back");
        AttributeList attrs = conn.getAttributes(on, new String[] { "Count" }, null);
        check(attrs.size() == 1 && ((Integer) ((Attribute) attrs.get(0)).getValue()) == 11, "getAttributes");
        check(conn.isRegistered(on, null), "isRegistered true");
        check(!conn.isRegistered(new ObjectName("jck.ci:type=Nope"), null), "isRegistered false");
        check(conn.getMBeanCount(null) == 2, "getMBeanCount delegate+counter");
        check(conn.getDefaultDomain(null).equals("DefaultDomain"), "getDefaultDomain");
        boolean found = false;
        for (String d : conn.getDomains(null)) if (d.equals("jck.ci")) found = true;
        check(found, "getDomains contains test domain");
        check(conn.getMBeanInfo(on, null).getClassName().endsWith("Counter"), "getMBeanInfo class");
        check(conn.isInstanceOf(on, CounterMBean.class.getName(), null), "isInstanceOf true");
        check(conn.isInstanceOf(on, Counter.class.getName(), null), "isInstanceOf impl class true");
        ObjectInstance oi = conn.getObjectInstance(on, null);
        check(oi.getObjectName().equals(on) && oi.getClassName().endsWith("Counter"),
                "getObjectInstance round-trip");
        ObjectInstance created = conn.createMBean(Counter.class.getName(),
                new ObjectName("jck.ci:type=Counter2"), null);
        check(created.getObjectName().equals(new ObjectName("jck.ci:type=Counter2")),
                "createMBean(String,ObjectName,Subject) registers");
        check(conn.queryNames(new ObjectName("jck.ci:*"), null, null).size() == 2, "queryNames both");
        conn.unregisterMBean(new ObjectName("jck.ci:type=Counter2"), null);
        check(conn.getMBeanCount(null) == 2, "unregisterMBean back to delegate+counter");

        // null checks wrap IllegalArgumentException in RuntimeOperationsException
        try {
            conn.invoke(null, "add", null, null, null);
            throw new AssertionError("expected RuntimeOperationsException");
        } catch (RuntimeOperationsException e) {
            check(e.getCause() instanceof IllegalArgumentException, "ROE wraps IAE");
            check("ObjectName must not be null".equals(e.getCause().getMessage()), "IAE message");
        }
        try {
            conn.invoke(on, null, null, null, null);
            throw new AssertionError("expected RuntimeOperationsException");
        } catch (RuntimeOperationsException e) {
            check("Operation name must not be null".equals(e.getCause().getMessage()), "op-name IAE message");
        }
        // delegation subject without an authenticated subject on JDK-8: SecurityException
        checkThrowsMsg(SecurityException.class,
                "Subject delegation cannot be enabled unless an authenticated subject is put in place",
                new ThrowingRunnable() { public void run() throws Exception {
                    conn.getAttribute(on, "Count", new Subject()); } },
                "delegation without authenticated subject");

        // fetchNotifications with timeout 0 returns promptly and empty
        long t0 = System.currentTimeMillis();
        NotificationResult nr = conn.fetchNotifications(0, 10, 0);
        check(System.currentTimeMillis() - t0 < 5000, "fetchNotifications prompt");
        check(nr != null && nr.getTargetedNotifications().length == 0, "fetchNotifications empty");

        // close is idempotent; the connection object stays locally usable (quirk)
        conn.close();
        conn.close();
        check(conn.getConnectionId().equals("jck-conn-42"), "id readable after close (no state check)");
        check(((Integer) conn.getAttribute(on, "Count", null)) == 11,
                "local routing still works after close (unexported usage)");
        conn.unreferenced();
        conn.unreferenced();
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.remote.rmi.RMIConnectionImpl");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check(Class.forName("javax.management.remote.rmi.RMIConnection").isAssignableFrom(c), "implements/extends javax.management.remote.rmi.RMIConnection");
        check(Class.forName("java.rmi.server.Unreferenced").isAssignableFrom(c), "implements/extends java.rmi.server.Unreferenced");
        check(hasMethod(c, "addNotificationListener"), "declares addNotificationListener");
        check(hasMethod(c, "addNotificationListeners"), "declares addNotificationListeners");
        check(hasMethod(c, "close"), "declares close");
        check(hasMethod(c, "unreferenced"), "declares unreferenced");
        check(c.getConstructor(RMIServerImpl.class, String.class, ClassLoader.class,
                Subject.class, java.util.Map.class) != null,
                "public (RMIServerImpl,String,ClassLoader,Subject,Map) ctor");
        testDeepCtor();
        testDeepLocalRouting();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
