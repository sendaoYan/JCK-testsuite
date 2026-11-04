/*
 * @test
 * @summary [deepened 2026-11: local server closed loop, exception matrices] javax.management.JMX conformance: isMXBeanInterface and field constants.
 * @run main JMXTest
 */

import javax.management.*;

public class JMXTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public interface FooMXBean {}

    public static void main(String[] args) throws Exception {
        check(JMX.isMXBeanInterface(FooMXBean.class), "MXBean interface");
        check(!JMX.isMXBeanInterface(java.lang.Object.class), "non-MXBean");
        check(JMX.DEFAULT_VALUE_FIELD != null && JMX.IMMUTABLE_INFO_FIELD != null, "field constants");
        deepFieldConstants();
        deepIsMXBeanInterface();
        deepMBeanProxyClosure();
        deepMXBeanProxyClosure();

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

    // ==== appended deepening (2026-11): closed-loop types ====

    @MXBean
    public static interface AnnotatedMX { int getX(); }

    public static interface NamedMXBean { int getY(); }

    public static interface PlainMBeanIface { int getZ(); }

    public static interface PingerMBean {
        int getCount();
        void setCount(int v);
        int add(int x);
    }

    public static class Pinger implements PingerMBean {
        private int count = 3;
        public int getCount() { return count; }
        public void setCount(int v) { count = v; }
        public int add(int x) { return count + x; }
    }

    public static interface EmitterMBean {
        int getLevel();
        void emit(String msg);
    }

    public static class Emitter extends javax.management.NotificationBroadcasterSupport implements EmitterMBean {
        private long seq;
        public int getLevel() { return 4; }
        public void emit(String msg) { sendNotification(new javax.management.Notification("jck.emit", this, seq++, msg)); }
    }

    public static interface SpotMXBean {
        int getX();
        void setX(int v);
        java.util.List<String> getTags();
    }

    public static class Spot implements SpotMXBean {
        private int x = 4;
        public int getX() { return x; }
        public void setX(int v) { x = v; }
        public java.util.List<String> getTags() { return java.util.Arrays.asList("a", "b"); }
    }

    // ==== appended deepening (2026-11): descriptor field constants ====

    static void deepFieldConstants() {
        check("legalValues".equals(JMX.LEGAL_VALUES_FIELD), "LEGAL_VALUES_FIELD");
        check("maxValue".equals(JMX.MAX_VALUE_FIELD), "MAX_VALUE_FIELD");
        check("minValue".equals(JMX.MIN_VALUE_FIELD), "MIN_VALUE_FIELD");
        check("originalType".equals(JMX.ORIGINAL_TYPE_FIELD), "ORIGINAL_TYPE_FIELD");
    }

    // ==== appended deepening (2026-11): isMXBeanInterface rules ====

    static void deepIsMXBeanInterface() {
        check(JMX.isMXBeanInterface(AnnotatedMX.class), "@MXBean annotated interface is an MXBean interface");
        check(JMX.isMXBeanInterface(NamedMXBean.class), "*MXBean naming convention is an MXBean interface");
        check(!JMX.isMXBeanInterface(PlainMBeanIface.class), "*MBean suffix is not an MXBean interface");
        check(!JMX.isMXBeanInterface(java.util.List.class), "plain interface without convention is not");
        check(!JMX.isMXBeanInterface(String.class), "non-interface class is not");
    }

    // ==== appended deepening (2026-11): newMBeanProxy closed loop on a local server ====

    static void deepMBeanProxyClosure() throws Exception {
        final javax.management.MBeanServer server = javax.management.MBeanServerFactory.newMBeanServer();
        final javax.management.ObjectName on = new javax.management.ObjectName("jck.jmx:type=Pinger");
        server.registerMBean(new Pinger(), on);
        PingerMBean proxy = JMX.newMBeanProxy(server, on, PingerMBean.class);
        check(proxy.getCount() == 3, "proxy getter reads the server");
        proxy.setCount(9);
        check(Integer.valueOf(9).equals(server.getAttribute(on, "Count")), "proxy setter reaches the server");
        check(proxy.add(4) == 13, "proxy operation routes to server.invoke");
        PingerMBean proxy3 = JMX.newMBeanProxy(server, on, PingerMBean.class, false);
        check(proxy3.getCount() == 9, "explicit notificationBroadcast=false behaves the same");
        // emitter variant implements NotificationEmitter and subscriptions really work
        final javax.management.ObjectName en = new javax.management.ObjectName("jck.jmx:type=Emitter");
        server.registerMBean(new Emitter(), en);
        EmitterMBean ep = JMX.newMBeanProxy(server, en, EmitterMBean.class, true);
        check(ep instanceof javax.management.NotificationEmitter, "emitter proxy implements NotificationEmitter");
        check(ep.getLevel() == 4, "emitter proxy getter");
        final java.util.List<javax.management.Notification> got =
                new java.util.ArrayList<javax.management.Notification>();
        final java.util.List<Object> handbacks = new java.util.ArrayList<Object>();
        javax.management.NotificationListener li = new javax.management.NotificationListener() {
            public void handleNotification(javax.management.Notification n, Object h) {
                got.add(n);
                handbacks.add(h);
            }
        };
        ((javax.management.NotificationEmitter) ep).addNotificationListener(li, null, "hb");
        ep.emit("hello");
        long deadline = System.currentTimeMillis() + 5000L;
        while (got.isEmpty() && System.currentTimeMillis() < deadline) Thread.sleep(50L);
        check(got.size() == 1, "notification delivered through the proxy subscription");
        check("jck.emit".equals(got.get(0).getType()), "delivered notification type");
        check("hello".equals(got.get(0).getMessage()), "delivered notification message");
        check("hb".equals(handbacks.get(0)), "handback delivered");
        ((javax.management.NotificationEmitter) ep).removeNotificationListener(li);
        server.unregisterMBean(en);
    }

    // ==== appended deepening (2026-11): newMXBeanProxy + open-type conversion ====

    static void deepMXBeanProxyClosure() throws Exception {
        final javax.management.MBeanServer server = javax.management.MBeanServerFactory.newMBeanServer();
        final javax.management.ObjectName on = new javax.management.ObjectName("jck.jmx:type=Spot");
        server.registerMBean(new Spot(), on);
        check("true".equals(String.valueOf(server.getMBeanInfo(on).getDescriptor().getFieldValue("mxbean"))),
                "registered MXBean descriptor mxbean=true");
        Object rawTags = server.getAttribute(on, "Tags");
        check(rawTags instanceof String[], "List<String> attribute surfaces as String[] through the server");
        SpotMXBean px = JMX.newMXBeanProxy(server, on, SpotMXBean.class);
        check(px.getX() == 4, "MXBean proxy getter");
        check(java.util.Arrays.asList("a", "b").equals(px.getTags()),
                "MXBean proxy reconstitutes the original List type");
        px.setX(11);
        check(Integer.valueOf(11).equals(server.getAttribute(on, "X")), "MXBean proxy setter reaches the server");
        SpotMXBean px2 = JMX.newMXBeanProxy(server, on, SpotMXBean.class, false);
        check(px2.getX() == 11, "4-arg newMXBeanProxy works");
    }
}

