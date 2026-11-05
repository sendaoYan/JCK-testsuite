/*
 * @test
 * @summary javax.management.remote.rmi.RMIConnection conformance: white-box structure + black-box behavior.
 * @run main RMIConnectionTest
 */

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.rmi.MarshalledObject;
import java.rmi.Remote;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.NotificationResult;
import javax.management.remote.rmi.RMIConnection;
import javax.management.remote.rmi.RMIConnectorServer;
import javax.management.remote.rmi.RMIJRMPServerImpl;
import javax.management.remote.rmi.RMIServer;

public class RMIConnectionTest {
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
        Class<?> c = Class.forName("javax.management.remote.rmi.RMIConnection");
        check(c.isInterface(), "is interface");
        check(Closeable.class.isAssignableFrom(c), "extends Closeable");
        check(Remote.class.isAssignableFrom(c), "extends Remote");
        Method[] mm = c.getMethods();
        check(has(mm, "getConnectionId"), "getConnectionId");
        check(has(mm, "close"), "close");
        check(has(mm, "createMBean"), "createMBean");
        check(has(mm, "unregisterMBean"), "unregisterMBean");
        check(has(mm, "getAttribute"), "getAttribute");
        check(has(mm, "setAttribute"), "setAttribute");
        check(has(mm, "invoke"), "invoke");
        check(has(mm, "getMBeanInfo"), "getMBeanInfo");
        check(has(mm, "fetchNotifications"), "fetchNotifications");
        check(has(mm, "addNotificationListener"), "addNotificationListener");
        check(has(mm, "removeNotificationListener"), "removeNotificationListener");
    }

    // ---- Part 2: black-box (light behavior) ----
    static void testBlackBox() throws Exception {
        final boolean[] closed = new boolean[1];
        final NotificationResult result = new NotificationResult(1L, 2L, new javax.management.remote.TargetedNotification[0]);
        InvocationHandler handler = new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) {
                String name = method.getName();
                if (name.equals("getConnectionId")) return "conn-42";
                if (name.equals("fetchNotifications")) return result;
                if (name.equals("close")) { closed[0] = true; return null; }
                Class<?> rt = method.getReturnType();
                if (!rt.isPrimitive()) return null;
                if (rt == boolean.class) return false;
                if (rt == char.class) return (char) 0;
                if (rt == byte.class) return (byte) 0;
                if (rt == short.class) return (short) 0;
                if (rt == int.class) return 0;
                if (rt == long.class) return 0L;
                if (rt == float.class) return 0f;
                if (rt == double.class) return 0d;
                return null;
            }
        };
        RMIConnection conn = (RMIConnection) Proxy.newProxyInstance(
            RMIConnection.class.getClassLoader(),
            new Class<?>[]{ RMIConnection.class },
            handler);

        check(conn.getConnectionId().equals("conn-42"), "getConnectionId callable");
        check(conn.fetchNotifications(0, 10, 0) == result, "fetchNotifications callable");
        conn.close();
        check(closed[0], "close callable");
        check(conn.getMBeanCount(null) == null, "other methods return default");
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

    public interface CounterMBean { int getCount(); void setCount(int c); int add(int x); }
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
        for (Method m : mm) {
            boolean io = false;
            for (Class<?> ex : m.getExceptionTypes()) if (ex == IOException.class) io = true;
            check(io, "declares IOException: " + m.getName());
            check(Modifier.isAbstract(m.getModifiers()), "abstract: " + m.getName());
        }
        Method invoke = c.getMethod("invoke", ObjectName.class, String.class,
                MarshalledObject.class, String[].class, javax.security.auth.Subject.class);
        check(invoke != null, "invoke exact signature");
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
        check(recording.isRegistered(new ObjectName("d:k=v"), null) == false, "proxy isRegistered default");
        check(recording.getMBeanCount(null) == null, "proxy getMBeanCount default (boxed Integer)");
        Set<String> distinct = new HashSet<String>(called);
        check(distinct.contains("isRegistered") && distinct.contains("getMBeanCount"),
                "proxy recorded calls");

        // real wire round-trip through an exported RMIJRMPServerImpl stub
        MBeanServer mbs = MBeanServerFactory.newMBeanServer();
        final ObjectName on = new ObjectName("jck.rcon27:type=Counter");
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
            AttributeList one = rc.getAttributes(on, new String[] { "Count" }, null);
            check(one.size() == 1 && ((Integer) ((Attribute) one.get(0)).getValue()) == 5, "live getAttributes");
            ObjectInstance oi = rc.getObjectInstance(on, null);
            check(oi.getObjectName().equals(on), "live getObjectInstance name");
            ObjectInstance created = rc.createMBean(Counter.class.getName(),
                    new ObjectName("jck.rcon27:type=Counter2"), null);
            check(created.getObjectName().equals(new ObjectName("jck.rcon27:type=Counter2")),
                    "live createMBean registers remotely");
            check(rc.getMBeanCount(null) == 3, "live getMBeanCount delegate+2 counters");
            rc.unregisterMBean(new ObjectName("jck.rcon27:type=Counter2"), null);
            check(rc.getMBeanCount(null) == 2, "live unregisterMBean removes remotely");
            check(rc.queryNames(new ObjectName("jck.rcon27:*"), null, null).size() == 1, "live queryNames");
            check(rc.isInstanceOf(on, CounterMBean.class.getName(), null), "live isInstanceOf");
            check(rc.getDefaultDomain(null).equals("DefaultDomain"), "live getDefaultDomain");
            long t0 = System.currentTimeMillis();
            NotificationResult nr = rc.fetchNotifications(0, 10, 0);
            check(System.currentTimeMillis() - t0 < 5000, "fetchNotifications timeout=0 returns promptly");
            check(nr != null && nr.getTargetedNotifications().length == 0, "no buffered notifications");
            rc.close();
            // after close the server unexports the connection object: stub calls fail
            checkThrows(IOException.class,
                    new ThrowingRunnable() { public void run() throws Exception {
                        rcHolder[0].getConnectionId(); } },
                    "getConnectionId on closed raw connection");
            checkThrows(IOException.class,
                    new ThrowingRunnable() { public void run() throws Exception {
                        rcHolder[0].getAttribute(on, "Count", null); } },
                    "getAttribute on closed raw connection");
        } finally {
            cs.stop();
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepMethodSet();
        testDeepBehavior();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
