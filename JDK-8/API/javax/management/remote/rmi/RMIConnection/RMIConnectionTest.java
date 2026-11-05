/*
 * @test
 * @summary javax.management.remote.rmi.RMIConnection conformance: interface reflection.
 * @run main RMIConnectionTest
 */

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Modifier;
import java.rmi.MarshalledObject;
import java.rmi.Remote;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.ObjectInstance;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.NotificationResult;
import javax.management.remote.rmi.RMIConnection;
import javax.management.remote.rmi.RMIConnectorServer;
import javax.management.remote.rmi.RMIJRMPServerImpl;
import javax.management.remote.rmi.RMIServer;

public class RMIConnectionTest {
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

    public static interface CounterMBean { int getCount(); void setCount(int c); int add(int x); }
    public static class Counter implements CounterMBean {
        private int count;
        public int getCount() { return count; }
        public void setCount(int c) { count = c; }
        public int add(int x) { count += x; return count; }
    }

    static boolean hasForm(Method[] mm, String name, int params) {
        for (Method m : mm)
            if (m.getName().equals(name) && m.getParameterTypes().length == params) return true;
        return false;
    }

    // ---- deepened: exact method-set contract (name + parameter count) ----
    static void testDeepMethodSet() throws Exception {
        Class<?> c = Class.forName("javax.management.remote.rmi.RMIConnection");
        Method[] mm = c.getDeclaredMethods();
        // 27 methods in total on both JDK-8 and JDK-27 (verified by javap).
        check(mm.length == 27, "27 declared methods, got " + mm.length);
        String[][] expect = {
            {"getConnectionId", "0"}, {"close", "0"},
            {"createMBean", "3"}, {"createMBean", "4"}, {"createMBean", "5"}, {"createMBean", "6"},
            {"unregisterMBean", "2"}, {"getObjectInstance", "2"},
            {"queryMBeans", "3"}, {"queryNames", "3"},
            {"isRegistered", "2"}, {"getMBeanCount", "1"},
            {"getAttribute", "3"}, {"getAttributes", "3"},
            {"setAttribute", "3"}, {"setAttributes", "3"},
            {"invoke", "5"},
            {"getDefaultDomain", "1"}, {"getDomains", "1"},
            {"getMBeanInfo", "2"}, {"isInstanceOf", "3"},
            {"addNotificationListener", "5"},
            {"removeNotificationListener", "3"}, {"removeNotificationListener", "5"},
            {"addNotificationListeners", "3"}, {"removeNotificationListeners", "3"},
            {"fetchNotifications", "3"},
        };
        for (String[] e : expect)
            check(hasForm(mm, e[0], Integer.parseInt(e[1])),
                    "method " + e[0] + "/" + e[1] + " params present");
        // every method declares IOException
        for (Method m : mm) {
            boolean io = false;
            for (Class<?> ex : m.getExceptionTypes()) if (ex == IOException.class) io = true;
            check(io, "declares IOException: " + m.getName());
            check(Modifier.isAbstract(m.getModifiers()), "abstract: " + m.getName());
        }
        // every parameterized payload type uses MarshalledObject for params/filters
        Method invoke = c.getMethod("invoke", ObjectName.class, String.class,
                MarshalledObject.class, String[].class, javax.security.auth.Subject.class);
        check(invoke != null, "invoke exact signature");
        Method setAttr = c.getMethod("setAttribute", ObjectName.class,
                MarshalledObject.class, javax.security.auth.Subject.class);
        check(setAttr != null, "setAttribute exact signature");
        Method fetch = c.getMethod("fetchNotifications", long.class, int.class, long.class);
        check(fetch.getReturnType() == NotificationResult.class, "fetchNotifications returns NotificationResult");
    }

    // ---- deepened: recording proxy + real protocol round-trip ----
    static void testDeepBehavior() throws Exception {
        final List<String> called = new ArrayList<String>();
        final NotificationResult result = new NotificationResult(1L, 2L,
                new javax.management.remote.TargetedNotification[0]);
        RMIConnection recording = (RMIConnection) Proxy.newProxyInstance(
                RMIConnection.class.getClassLoader(), new Class<?>[] { RMIConnection.class },
                new InvocationHandler() {
                    public Object invoke(Object p, Method m, Object[] a) {
                        called.add(m.getName());
                        if (m.getName().equals("getConnectionId")) return "rec-42";
                        if (m.getName().equals("fetchNotifications")) return result;
                        Class<?> rt = m.getReturnType();
                        if (rt == boolean.class) return Boolean.FALSE;
                        if (rt == int.class) return Integer.valueOf(0);
                        if (rt == long.class) return Long.valueOf(0);
                        return null;
                    }
                });
        check(recording.getConnectionId().equals("rec-42"), "proxy getConnectionId");
        check(recording.fetchNotifications(0, 1, 0) == result, "proxy fetchNotifications identity");
        check(recording.isRegistered(new ObjectName("d:k=v"), null) == false, "proxy isRegistered default");
        check(recording.getMBeanCount(null) == null, "proxy getMBeanCount default (boxed Integer)");
        Set<String> distinct = new HashSet<String>(called);
        check(distinct.contains("getConnectionId") && distinct.contains("fetchNotifications")
                && distinct.contains("isRegistered") && distinct.contains("getMBeanCount"),
                "proxy recorded calls");

        // real wire round-trip through an exported RMIJRMPServerImpl stub
        MBeanServer mbs = MBeanServerFactory.newMBeanServer();
        final ObjectName on = new ObjectName("jck.rcon:type=Counter");
        mbs.registerMBean(new Counter(), on);
        RMIJRMPServerImpl impl = new RMIJRMPServerImpl(0, null, null, null);
        JMXConnectorServer cs = new RMIConnectorServer(
                new JMXServiceURL("service:jmx:rmi://127.0.0.1"), null, impl, mbs);
        cs.start();
        final RMIConnection[] rcHolder = new RMIConnection[1];
        try {
            RMIConnection rc = ((RMIServer) impl.toStub()).newClient(null);
            rcHolder[0] = rc;
            check(rc != null, "live RMIConnection");
            check(rc.getConnectionId().startsWith("rmi://127.0.0.1 "), "live connection id format");
            check(((Integer) rc.getAttribute(on, "Count", null)) == 0, "live getAttribute");
            rc.setAttribute(on, new MarshalledObject<Object>(new Attribute("Count", 3)), null);
            check(((Integer) rc.getAttribute(on, "Count", null)) == 3, "live setAttribute+read-back");
            check(((Integer) rc.invoke(on, "add",
                    new MarshalledObject<Object>(new Object[] { Integer.valueOf(2) }),
                    new String[] { "int" }, null)) == 5, "live invoke add(2)==5");
            AttributeList two = rc.getAttributes(on, new String[] { "Count" }, null);
            check(two.size() == 1 && ((Integer) ((Attribute) two.get(0)).getValue()) == 5, "live getAttributes");
            ObjectInstance oi = rc.getObjectInstance(on, null);
            check(oi.getObjectName().equals(on), "live getObjectInstance name");
            ObjectInstance created = rc.createMBean(Counter.class.getName(),
                    new ObjectName("jck.rcon:type=Counter2"), null);
            check(created.getObjectName().equals(new ObjectName("jck.rcon:type=Counter2")),
                    "live createMBean registers remotely");
            check(rc.getMBeanCount(null) == 3, "live getMBeanCount delegate+2 counters");
            rc.unregisterMBean(new ObjectName("jck.rcon:type=Counter2"), null);
            check(rc.getMBeanCount(null) == 2, "live unregisterMBean removes remotely");
            check(rc.queryNames(new ObjectName("jck.rcon:*"), null, null).size() == 1, "live queryNames");
            check(rc.isInstanceOf(on, CounterMBean.class.getName(), null), "live isInstanceOf");
            check(rc.getDefaultDomain(null).equals("DefaultDomain"), "live getDefaultDomain");
            long t0 = System.currentTimeMillis();
            NotificationResult nr = rc.fetchNotifications(0, 10, 0);
            check(System.currentTimeMillis() - t0 < 5000, "fetchNotifications timeout=0 returns promptly");
            check(nr != null && nr.getTargetedNotifications().length == 0, "no buffered notifications");
            rc.close();
            // after close the server unexports the connection object: stub calls fail
            checkThrows(java.io.IOException.class,
                    new ThrowingRunnable() { public void run() throws Exception {
                        rcHolder[0].getConnectionId(); } },
                    "getConnectionId on closed raw connection");
            checkThrows(java.io.IOException.class,
                    new ThrowingRunnable() { public void run() throws Exception {
                        rcHolder[0].getAttribute(on, "Count", null); } },
                    "getAttribute on closed raw connection");
        } finally {
            cs.stop();
        }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.remote.rmi.RMIConnection");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(Class.forName("java.io.Closeable").isAssignableFrom(c), "implements/extends java.io.Closeable");
        check(Class.forName("java.rmi.Remote").isAssignableFrom(c), "implements/extends java.rmi.Remote");
        check(hasMethod(c, "addNotificationListener"), "declares addNotificationListener");
        check(hasMethod(c, "addNotificationListeners"), "declares addNotificationListeners");
        check(hasMethod(c, "close"), "declares close");
        testDeepMethodSet();
        testDeepBehavior();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
