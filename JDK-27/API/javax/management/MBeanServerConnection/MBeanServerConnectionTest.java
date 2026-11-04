/*
 * @test
 * @summary [deepened 2026-11: local server closed loop, exception matrices] javax.management.MBeanServerConnection conformance: white-box structure + black-box behavior.
 * @run main MBeanServerConnectionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.lang.management.*;
import javax.management.*;

public class MBeanServerConnectionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.MBeanServerConnection");
        check(c.isInterface(), "is interface");
        check(hasMethod(c, "getAttribute"), "has getAttribute");
        check(hasMethod(c, "getMBeanInfo"), "has getMBeanInfo");
        check(hasMethod(c, "queryNames"), "has queryNames");
        check(hasMethod(c, "getMBeanCount"), "has getMBeanCount");
        check(hasMethod(c, "isRegistered"), "has isRegistered");
        check(hasMethod(c, "getDomains"), "has getDomains");
        check(hasMethod(c, "getDefaultDomain"), "has getDefaultDomain");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // The platform MBeanServer is a real MBeanServerConnection.
        MBeanServerConnection mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName runtime = new ObjectName("java.lang:type=Runtime");

        Object vmName = mbs.getAttribute(runtime, "VmName");
        check(vmName != null && vmName.toString().length() > 0, "VmName attribute non-empty");

        Object specVersion = mbs.getAttribute(runtime, "SpecVersion");
        check(specVersion != null && specVersion.toString().length() > 0, "SpecVersion attribute non-empty");

        // batch getAttributes
        AttributeList attrs = mbs.getAttributes(runtime, new String[]{"VmName", "SpecVersion"});
        check(attrs.size() == 2, "getAttributes returns two");
        check(attrs.get(0) instanceof Attribute, "getAttributes elements typed");

        // getMBeanInfo
        MBeanInfo info = mbs.getMBeanInfo(runtime);
        check(info != null && info.getAttributes().length > 0, "getMBeanInfo non-empty");

        // queryNames with a wildcard and null query returns the platform MBeans
        Set<ObjectName> names = mbs.queryNames(new ObjectName("java.lang:*"), null);
        check(!names.isEmpty(), "queryNames java.lang:* non-empty");
        check(names.contains(runtime), "queryNames includes java.lang:type=Runtime");

        // queryMBeans returns ObjectInstances with the same names
        Set<ObjectInstance> mbeans = mbs.queryMBeans(new ObjectName("java.lang:type=Runtime"), null);
        check(mbeans.size() == 1, "queryMBeans one result");
        check(mbeans.iterator().next().getObjectName().equals(runtime), "queryMBeans object name");

        check(mbs.isRegistered(runtime), "isRegistered");
        check(mbs.getMBeanCount() > 0, "getMBeanCount positive");
        check(mbs.isInstanceOf(runtime, "java.lang.management.RuntimeMXBean"), "isInstanceOf RuntimeMXBean");

        String[] domains = mbs.getDomains();
        check(domains.length > 0, "getDomains non-empty");
        boolean hasJavaLang = false;
        for (String d : domains) if ("java.lang".equals(d)) hasJavaLang = true;
        check(hasJavaLang, "domains include java.lang");
        check(mbs.getDefaultDomain() != null, "getDefaultDomain non-null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepInterfaceShape();
        deepConnectionThroughLocalServer();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ==== appended deepening (2026-11): shared helpers ====

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

    // ==== appended deepening (2026-11): local closed-loop type ====

    public static interface DialMBean {
        int getLevel();
        void setLevel(int v);
        void reset();
        int bump(int x);
    }

    public static class Dial implements DialMBean {
        private int level = 6;
        public int getLevel() { return level; }
        public void setLevel(int v) { level = v; }
        public void reset() { level = 0; }
        public int bump(int x) { return level + x; }
    }

    // ==== appended deepening (2026-11): interface shape contract ====

    static void deepInterfaceShape() throws Exception {
        Class<?> c = Class.forName("javax.management.MBeanServerConnection");
        check(c.isInterface(), "is an interface");
        java.util.Set<String> names = new java.util.TreeSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        java.util.Set<String> expected = new java.util.TreeSet<String>(java.util.Arrays.asList(
                "addNotificationListener", "createMBean", "getAttribute", "getAttributes",
                "getDefaultDomain", "getDomains", "getMBeanCount", "getMBeanInfo", "getObjectInstance",
                "invoke", "isInstanceOf", "isRegistered", "queryMBeans", "queryNames",
                "removeNotificationListener", "setAttribute", "setAttributes", "unregisterMBean"));
        check(names.equals(expected), "public method-name set exact (18 names), got " + names);
        boolean allThrowIO = true;
        for (java.lang.reflect.Method m : c.getMethods()) {
            boolean io = false;
            for (Class<?> t : m.getExceptionTypes()) if (t == java.io.IOException.class) io = true;
            if (!io) allThrowIO = false;
        }
        check(allThrowIO, "every interface method declares IOException (remote contract)");
        check(c.isAssignableFrom(javax.management.MBeanServer.class), "MBeanServer is-a MBeanServerConnection");
    }

    // ==== appended deepening (2026-11): every method reachable through a local server ====

    static void deepConnectionThroughLocalServer() throws Exception {
        final javax.management.MBeanServer server = javax.management.MBeanServerFactory.newMBeanServer();
        final javax.management.MBeanServerConnection conn = server;
        final javax.management.ObjectName on = new javax.management.ObjectName("jck.conn:type=Dial");
        javax.management.ObjectInstance oi = conn.createMBean(Dial.class.getName(), on);
        check(on.equals(oi.getObjectName()), "createMBean via connection type");
        check(conn.isRegistered(on), "isRegistered");
        check(Integer.valueOf(6).equals(conn.getAttribute(on, "Level")), "getAttribute");
        conn.setAttribute(on, new javax.management.Attribute("Level", Integer.valueOf(8)));
        check(Integer.valueOf(8).equals(conn.getAttribute(on, "Level")), "setAttribute round-trip");
        check(conn.getAttributes(on, new String[]{"Level"}).size() == 1, "getAttributes");
        javax.management.AttributeList in = new javax.management.AttributeList();
        in.add(new javax.management.Attribute("Level", Integer.valueOf(10)));
        check(conn.setAttributes(on, in).size() == 1, "setAttributes");
        Object r = conn.invoke(on, "bump", new Object[]{Integer.valueOf(3)}, new String[]{"int"});
        check(r instanceof Integer && ((Integer) r).intValue() == 13, "invoke with signature");
        check(conn.queryNames(new javax.management.ObjectName("jck.conn:*"), null).contains(on), "queryNames");
        check(conn.queryMBeans(on, null).size() == 1, "queryMBeans");
        check("DefaultDomain".equals(conn.getDefaultDomain()), "getDefaultDomain");
        boolean dom = false;
        for (String d : conn.getDomains()) if ("jck.conn".equals(d)) dom = true;
        check(dom, "getDomains contains the registered domain");
        check(conn.getMBeanCount().intValue() == 2, "getMBeanCount = delegate + 1");
        check(Dial.class.getName().equals(conn.getMBeanInfo(on).getClassName()), "getMBeanInfo");
        check(Dial.class.getName().equals(conn.getObjectInstance(on).getClassName()), "getObjectInstance");
        check(conn.isInstanceOf(on, Dial.class.getName()), "isInstanceOf");
        // notification add/remove through the connection type
        final java.util.List<javax.management.Notification> got =
                new java.util.ArrayList<javax.management.Notification>();
        final javax.management.NotificationListener li = new javax.management.NotificationListener() {
            public void handleNotification(javax.management.Notification n, Object h) { got.add(n); }
        };
        conn.addNotificationListener(javax.management.MBeanServerDelegate.DELEGATE_NAME, li, null, null);
        final javax.management.ObjectName tmp = new javax.management.ObjectName("jck.conn:type=Tmp");
        conn.createMBean(Dial.class.getName(), tmp);
        long deadline = System.currentTimeMillis() + 5000L;
        while (got.isEmpty() && System.currentTimeMillis() < deadline) Thread.sleep(50L);
        check(got.size() == 1, "exactly one registration notification received");
        check(javax.management.MBeanServerNotification.REGISTRATION_NOTIFICATION.equals(got.get(0).getType()),
                "delegate registration notification type");
        conn.removeNotificationListener(javax.management.MBeanServerDelegate.DELEGATE_NAME, li);
        conn.unregisterMBean(tmp);
        // negatives through the connection type (on is still registered here)
        final javax.management.ObjectName miss = new javax.management.ObjectName("jck.conn:type=Missing");
        checkThrows(javax.management.InstanceNotFoundException.class, new ThrowingRunnable() {
            public void run() throws Exception { conn.getAttribute(miss, "Level"); }
        }, "getAttribute missing MBean via connection type");
        checkThrows(javax.management.AttributeNotFoundException.class, new ThrowingRunnable() {
            public void run() throws Exception { conn.getAttribute(on, "Nope"); }
        }, "getAttribute unknown attribute via connection type");
        conn.unregisterMBean(on);
        check(!conn.isRegistered(on), "unregisterMBean via connection type");
    }
}

