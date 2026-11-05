/*
 * @test
 * @summary javax.management.monitor.CounterMonitorMBean conformance: white-box structure + black-box behavior.
 * @run main CounterMonitorMBeanTest
 */

import java.lang.reflect.Method;
import javax.management.monitor.CounterMonitor;
import javax.management.monitor.CounterMonitorMBean;
import javax.management.monitor.MonitorMBean;

public class CounterMonitorMBeanTest {
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
        Class<?> c = Class.forName("javax.management.monitor.CounterMonitorMBean");
        check(c.isInterface(), "is interface");
        check(MonitorMBean.class.isAssignableFrom(c), "extends MonitorMBean");
        Method[] mm = c.getMethods();
        check(has(mm, "getDerivedGauge"), "has getDerivedGauge");
        check(has(mm, "getDerivedGaugeTimeStamp"), "has getDerivedGaugeTimeStamp");
        check(has(mm, "getThreshold"), "has getThreshold");
        check(has(mm, "setThreshold"), "has setThreshold");
        check(has(mm, "getInitThreshold"), "has getInitThreshold");
        check(has(mm, "setInitThreshold"), "has setInitThreshold");
        check(has(mm, "getOffset"), "has getOffset");
        check(has(mm, "setOffset"), "has setOffset");
        check(has(mm, "getModulus"), "has getModulus");
        check(has(mm, "setModulus"), "has setModulus");
        check(has(mm, "getNotify"), "has getNotify");
        check(has(mm, "setNotify"), "has setNotify");
        check(has(mm, "getDifferenceMode"), "has getDifferenceMode");
        check(has(mm, "setDifferenceMode"), "has setDifferenceMode");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        CounterMonitorMBean m = new CounterMonitor();
        m.setInitThreshold(7);
        check(m.getInitThreshold().intValue() == 7, "interface setInitThreshold/getInitThreshold");
        m.setOffset(3);
        check(m.getOffset().intValue() == 3, "interface setOffset/getOffset");
        m.setNotify(true);
        check(m.getNotify(), "interface setNotify/getNotify");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
        long getBig(); void setBig(long v);
    }

    public static class Observed implements ObservedMBean {
        private int count;
        private double gauge;
        private String str = "INIT";
        private long big;
        public int getCount() { return count; }
        public void setCount(int c) { count = c; }
        public double getGauge() { return gauge; }
        public void setGauge(double g) { gauge = g; }
        public String getStr() { return str; }
        public void setStr(String s) { str = s; }
        public long getBig() { return big; }
        public void setBig(long v) { big = v; }
    }

    // ==== deepened: exact method set + closed loop through the interface (batch i, 2026-11) ====

    static void testDeepMethodSetExact() throws Exception {
        Class<?> c = Class.forName("javax.management.monitor.CounterMonitorMBean");
        java.util.Set<String> s = sigs(c);
        String[] expected = {
            // inherited from MonitorMBean
            "start/0", "stop/0", "addObservedObject/1", "removeObservedObject/1",
            "containsObservedObject/1", "getObservedObjects/0", "getObservedObject/0",
            "setObservedObject/1", "getObservedAttribute/0", "setObservedAttribute/1",
            "getGranularityPeriod/0", "setGranularityPeriod/1", "isActive/0",
            // own
            "getDerivedGauge/0", "getDerivedGaugeTimeStamp/0", "getThreshold/0", "setThreshold/1",
            "getDerivedGauge/1", "getDerivedGaugeTimeStamp/1", "getThreshold/1",
            "getInitThreshold/0", "setInitThreshold/1", "getOffset/0", "setOffset/1",
            "getModulus/0", "setModulus/1", "getNotify/0", "setNotify/1",
            "getDifferenceMode/0", "setDifferenceMode/1"
        };
        for (String e : expected) check(s.contains(e), "CounterMonitorMBean declares " + e);
        check(s.size() == 30, "CounterMonitorMBean method set exact, got " + s);
    }

    @SuppressWarnings("deprecation")
    static void testDeepThroughInterfaceClosedLoop() throws Exception {
        javax.management.MBeanServer server = javax.management.MBeanServerFactory.newMBeanServer();
        javax.management.ObjectName observedName = new javax.management.ObjectName("jck.cmm.i:type=obs");
        javax.management.ObjectName monitorName = new javax.management.ObjectName("jck.cmm.i:type=mon");
        server.registerMBean(new Observed(), observedName);
        javax.management.monitor.CounterMonitor cm = new javax.management.monitor.CounterMonitor();
        server.registerMBean(cm, monitorName);
        check(cm instanceof javax.management.monitor.CounterMonitorMBean, "instanceof CounterMonitorMBean");
        javax.management.monitor.CounterMonitorMBean ref = cm;

        // full configuration surface through the interface reference
        ref.setInitThreshold(3);
        check(ref.getInitThreshold().intValue() == 3, "init threshold via interface");
        ref.setThreshold(9); // deprecated alias
        check(ref.getInitThreshold().intValue() == 9, "alias keeps init threshold in sync");
        check(ref.getThreshold() == null,
                "deprecated getThreshold() reports the first observed object, null when none added");
        ref.setOffset(4);
        check(ref.getOffset().intValue() == 4, "offset via interface");
        ref.setModulus(100);
        check(ref.getModulus().intValue() == 100, "modulus via interface");
        ref.setNotify(true);
        check(ref.getNotify(), "notify via interface");
        ref.setDifferenceMode(true);
        check(ref.getDifferenceMode(), "difference mode via interface");
        check(ref.getDerivedGauge() == null, "derived gauge null before observation");

        // closed loop: drive a crossing entirely configured via the interface
        ref.setInitThreshold(5);
        ref.setOffset(0);
        ref.setModulus(0);
        ref.setDifferenceMode(false);
        ref.addObservedObject(observedName);
        ref.setObservedAttribute("Count");
        ref.setGranularityPeriod(100L);
        Collector col = new Collector();
        server.addNotificationListener(monitorName, col, null, null);
        ref.start();
        try {
            check(ref.isActive(), "started via interface");
            server.setAttribute(observedName, new javax.management.Attribute("Count", 6));
            javax.management.Notification n = col.waitFor(
                    javax.management.monitor.MonitorNotification.THRESHOLD_VALUE_EXCEEDED, 1, 5000);
            check(n != null, "threshold notification through interface-driven monitor");
            check(ref.getDerivedGauge(observedName).intValue() == 6, "per-object derived gauge via interface");
            check(ref.getDerivedGaugeTimeStamp(observedName) > 0L, "per-object time stamp via interface");
            check(ref.getThreshold(observedName).intValue() == 5, "per-object threshold via interface");
        } finally {
            ref.stop();
        }
        check(!ref.isActive(), "stopped via interface");
    }
}
