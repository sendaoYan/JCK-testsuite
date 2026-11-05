/*
 * @test
 * @summary javax.management.monitor.StringMonitorMBean conformance: white-box structure + black-box behavior.
 * @run main StringMonitorMBeanTest
 */

import java.lang.reflect.Method;
import javax.management.monitor.MonitorMBean;
import javax.management.monitor.StringMonitor;
import javax.management.monitor.StringMonitorMBean;

public class StringMonitorMBeanTest {
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
        Class<?> c = Class.forName("javax.management.monitor.StringMonitorMBean");
        check(c.isInterface(), "is interface");
        check(MonitorMBean.class.isAssignableFrom(c), "extends MonitorMBean");
        Method[] mm = c.getMethods();
        check(has(mm, "getDerivedGauge"), "has getDerivedGauge");
        check(has(mm, "getDerivedGaugeTimeStamp"), "has getDerivedGaugeTimeStamp");
        check(has(mm, "getStringToCompare"), "has getStringToCompare");
        check(has(mm, "setStringToCompare"), "has setStringToCompare");
        check(has(mm, "getNotifyMatch"), "has getNotifyMatch");
        check(has(mm, "setNotifyMatch"), "has setNotifyMatch");
        check(has(mm, "getNotifyDiffer"), "has getNotifyDiffer");
        check(has(mm, "setNotifyDiffer"), "has setNotifyDiffer");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        StringMonitorMBean m = new StringMonitor();
        m.setStringToCompare("abc");
        check("abc".equals(m.getStringToCompare()), "interface setStringToCompare/getStringToCompare");
        m.setNotifyMatch(true);
        m.setNotifyDiffer(true);
        check(m.getNotifyMatch() && m.getNotifyDiffer(), "interface notify flags");
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
        Class<?> c = Class.forName("javax.management.monitor.StringMonitorMBean");
        java.util.Set<String> s = sigs(c);
        String[] expected = {
            "start/0", "stop/0", "addObservedObject/1", "removeObservedObject/1",
            "containsObservedObject/1", "getObservedObjects/0", "getObservedObject/0",
            "setObservedObject/1", "getObservedAttribute/0", "setObservedAttribute/1",
            "getGranularityPeriod/0", "setGranularityPeriod/1", "isActive/0",
            "getDerivedGauge/0", "getDerivedGaugeTimeStamp/0",
            "getDerivedGauge/1", "getDerivedGaugeTimeStamp/1",
            "getStringToCompare/0", "setStringToCompare/1",
            "getNotifyMatch/0", "setNotifyMatch/1", "getNotifyDiffer/0", "setNotifyDiffer/1"
        };
        for (String e : expected) check(s.contains(e), "StringMonitorMBean declares " + e);
        check(s.size() == 23, "StringMonitorMBean method set exact, got " + s);
    }

    static void testDeepThroughInterfaceClosedLoop() throws Exception {
        javax.management.MBeanServer server = javax.management.MBeanServerFactory.newMBeanServer();
        javax.management.ObjectName observedName = new javax.management.ObjectName("jck.smm.i:type=obs");
        javax.management.ObjectName monitorName = new javax.management.ObjectName("jck.smm.i:type=mon");
        server.registerMBean(new Observed(), observedName);
        javax.management.monitor.StringMonitor sm = new javax.management.monitor.StringMonitor();
        server.registerMBean(sm, monitorName);
        check(sm instanceof javax.management.monitor.StringMonitorMBean, "instanceof StringMonitorMBean");
        javax.management.monitor.StringMonitorMBean ref = sm;

        ref.setStringToCompare("GO");
        check("GO".equals(ref.getStringToCompare()), "string to compare via interface");
        ref.setNotifyMatch(true);
        check(ref.getNotifyMatch(), "notifyMatch via interface");
        ref.setNotifyDiffer(false);
        check(!ref.getNotifyDiffer(), "notifyDiffer via interface");
        ref.addObservedObject(observedName);
        ref.setObservedAttribute("Str");
        ref.setGranularityPeriod(100L);
        Collector col = new Collector();
        server.addNotificationListener(monitorName, col, null, null);
        ref.start();
        try {
            check(ref.isActive(), "started via interface");
            server.setAttribute(observedName, new javax.management.Attribute("Str", "GO"));
            javax.management.Notification n = col.waitFor(
                    javax.management.monitor.MonitorNotification.STRING_TO_COMPARE_VALUE_MATCHED, 1, 5000);
            check(n != null, "matches notification through interface-driven monitor");
            check("GO".equals(ref.getDerivedGauge(observedName)), "per-object derived gauge via interface");
            check(ref.getDerivedGaugeTimeStamp(observedName) > 0L, "per-object time stamp via interface");
        } finally {
            ref.stop();
        }
        check(!ref.isActive(), "stopped via interface");
    }
}
