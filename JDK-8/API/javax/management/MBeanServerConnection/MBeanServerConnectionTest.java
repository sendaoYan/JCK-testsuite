/*
 * @test
 * @summary [deepened 2026-11: local server closed loop, exception matrices] javax.management.MBeanServerConnection conformance: interface reflection.
 * @run main MBeanServerConnectionTest
 */

public class MBeanServerConnectionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.MBeanServerConnection");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(hasMethod(c, "addNotificationListener"), "declares addNotificationListener");
        check(hasMethod(c, "createMBean"), "declares createMBean");
        check(hasMethod(c, "getAttribute"), "declares getAttribute");
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

