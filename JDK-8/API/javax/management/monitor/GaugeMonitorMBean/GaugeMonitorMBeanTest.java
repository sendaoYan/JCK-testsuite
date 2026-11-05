/*
 * @test
 * @summary javax.management.monitor.GaugeMonitorMBean conformance: interface reflection.
 * @run main GaugeMonitorMBeanTest
 */

public class GaugeMonitorMBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.monitor.GaugeMonitorMBean");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(Class.forName("javax.management.monitor.MonitorMBean").isAssignableFrom(c), "implements/extends javax.management.monitor.MonitorMBean");
        check(hasMethod(c, "getDerivedGauge"), "declares getDerivedGauge");
        check(hasMethod(c, "getDerivedGaugeTimeStamp"), "declares getDerivedGaugeTimeStamp");
        check(hasMethod(c, "getDifferenceMode"), "declares getDifferenceMode");
        testDeepMethodSetExact();
        testDeepThroughInterfaceClosedLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ==== deepened: shared helpers (batch i, 2026-11) ====

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

    static final long STEP_MS = 25L;

    static class Collector implements javax.management.NotificationListener {
        final java.util.List<javax.management.Notification> received =
                new java.util.concurrent.CopyOnWriteArrayList<javax.management.Notification>();
        public void handleNotification(javax.management.Notification n, Object h) { received.add(n); }
        int count(String type) {
            int c = 0;
            for (javax.management.Notification n : received) if (type.equals(n.getType())) c++;
            return c;
        }
        javax.management.Notification nth(String type, int idx) {
            int i = 0;
            for (javax.management.Notification n : received) {
                if (type.equals(n.getType())) { if (i == idx) return n; i++; }
            }
            return null;
        }
        javax.management.Notification waitFor(String type, int need, long deadlineMs) throws InterruptedException {
            long deadline = System.currentTimeMillis() + deadlineMs;
            while (System.currentTimeMillis() < deadline) {
                if (count(type) >= need) return nth(type, 0);
                Thread.sleep(STEP_MS);
            }
            return nth(type, 0);
        }
        boolean quietFor(String type, long quietMs) throws InterruptedException {
            long deadline = System.currentTimeMillis() + quietMs;
            while (System.currentTimeMillis() < deadline) Thread.sleep(STEP_MS);
            return count(type) == 0;
        }
    }

    static java.util.Set<String> sigs(Class<?> iface) {
        java.util.Set<String> s = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : iface.getMethods())
            s.add(m.getName() + "/" + m.getParameterTypes().length);
        return s;
    }

    public static interface ObservedMBean {
        int getCount(); void setCount(int c);
        double getGauge(); void setGauge(double g);
        String getStr(); void setStr(String s);
    }

    public static class Observed implements ObservedMBean {
        private int count;
        private double gauge;
        private String str = "INIT";
        public int getCount() { return count; }
        public void setCount(int c) { count = c; }
        public double getGauge() { return gauge; }
        public void setGauge(double g) { gauge = g; }
        public String getStr() { return str; }
        public void setStr(String s) { str = s; }
    }

    // ==== deepened: exact method set + closed loop through the interface (batch i, 2026-11) ====

    static void testDeepMethodSetExact() throws Exception {
        Class<?> c = Class.forName("javax.management.monitor.GaugeMonitorMBean");
        java.util.Set<String> s = sigs(c);
        String[] expected = {
            "start/0", "stop/0", "addObservedObject/1", "removeObservedObject/1",
            "containsObservedObject/1", "getObservedObjects/0", "getObservedObject/0",
            "setObservedObject/1", "getObservedAttribute/0", "setObservedAttribute/1",
            "getGranularityPeriod/0", "setGranularityPeriod/1", "isActive/0",
            "getDerivedGauge/0", "getDerivedGaugeTimeStamp/0",
            "getDerivedGauge/1", "getDerivedGaugeTimeStamp/1",
            "getHighThreshold/0", "getLowThreshold/0", "setThresholds/2",
            "getNotifyHigh/0", "setNotifyHigh/1", "getNotifyLow/0", "setNotifyLow/1",
            "getDifferenceMode/0", "setDifferenceMode/1"
        };
        for (String e : expected) check(s.contains(e), "GaugeMonitorMBean declares " + e);
        check(s.size() == 26, "GaugeMonitorMBean method set exact, got " + s);
    }

    static void testDeepThroughInterfaceClosedLoop() throws Exception {
        javax.management.MBeanServer server = javax.management.MBeanServerFactory.newMBeanServer();
        Observed obs = new Observed();
        obs.setGauge(5.0);
        javax.management.ObjectName observedName = new javax.management.ObjectName("jck.gmm.i:type=obs");
        javax.management.ObjectName monitorName = new javax.management.ObjectName("jck.gmm.i:type=mon");
        server.registerMBean(obs, observedName);
        javax.management.monitor.GaugeMonitor gm = new javax.management.monitor.GaugeMonitor();
        server.registerMBean(gm, monitorName);
        check(gm instanceof javax.management.monitor.GaugeMonitorMBean, "instanceof GaugeMonitorMBean");
        javax.management.monitor.GaugeMonitorMBean ref = gm;

        ref.setThresholds(8.0, 3.0);
        check(ref.getHighThreshold().doubleValue() == 8.0, "high threshold via interface");
        check(ref.getLowThreshold().doubleValue() == 3.0, "low threshold via interface");
        ref.setNotifyHigh(true);
        check(ref.getNotifyHigh(), "notifyHigh via interface");
        ref.setNotifyLow(false);
        check(!ref.getNotifyLow(), "notifyLow via interface");
        ref.setDifferenceMode(false);
        check(!ref.getDifferenceMode(), "difference mode via interface");
        ref.addObservedObject(observedName);
        ref.setObservedAttribute("Gauge");
        ref.setGranularityPeriod(100L);
        Collector col = new Collector();
        server.addNotificationListener(monitorName, col, null, null);
        ref.start();
        try {
            check(ref.isActive(), "started via interface");
            server.setAttribute(observedName, new javax.management.Attribute("Gauge", 9.0));
            javax.management.Notification n = col.waitFor(
                    javax.management.monitor.MonitorNotification.THRESHOLD_HIGH_VALUE_EXCEEDED, 1, 5000);
            check(n != null, "high notification through interface-driven monitor");
            check(ref.getDerivedGauge(observedName) != null
                    && ((Number) ref.getDerivedGauge(observedName)).doubleValue() == 9.0,
                    "per-object derived gauge via interface");
            check(ref.getDerivedGaugeTimeStamp(observedName) > 0L, "per-object time stamp via interface");
        } finally {
            ref.stop();
        }
        check(!ref.isActive(), "stopped via interface");
    }
}
