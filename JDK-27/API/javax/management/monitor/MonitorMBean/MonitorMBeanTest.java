/*
 * @test
 * @summary javax.management.monitor.MonitorMBean conformance: white-box structure + black-box behavior.
 * @run main MonitorMBeanTest
 */

import java.lang.reflect.Method;
import javax.management.ObjectName;
import javax.management.monitor.CounterMonitor;
import javax.management.monitor.MonitorMBean;

@SuppressWarnings("deprecation")
public class MonitorMBeanTest {
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
        Class<?> c = Class.forName("javax.management.monitor.MonitorMBean");
        check(c.isInterface(), "is interface");
        Method[] mm = c.getMethods();
        check(has(mm, "start"), "has start");
        check(has(mm, "stop"), "has stop");
        check(has(mm, "addObservedObject"), "has addObservedObject");
        check(has(mm, "removeObservedObject"), "has removeObservedObject");
        check(has(mm, "containsObservedObject"), "has containsObservedObject");
        check(has(mm, "getObservedObjects"), "has getObservedObjects");
        check(has(mm, "getObservedObject"), "has getObservedObject");
        check(has(mm, "setObservedObject"), "has setObservedObject");
        check(has(mm, "getObservedAttribute"), "has getObservedAttribute");
        check(has(mm, "setObservedAttribute"), "has setObservedAttribute");
        check(has(mm, "getGranularityPeriod"), "has getGranularityPeriod");
        check(has(mm, "setGranularityPeriod"), "has setGranularityPeriod");
        check(has(mm, "isActive"), "has isActive");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        MonitorMBean m = new CounterMonitor();
        ObjectName on = new ObjectName("test:type=monitormbean");
        m.setObservedObject(on);
        check(m.containsObservedObject(on), "interface containsObservedObject");
        check(on.equals(m.getObservedObject()), "interface getObservedObject");
        m.setObservedAttribute("Value");
        check("Value".equals(m.getObservedAttribute()), "interface getObservedAttribute");
        m.setGranularityPeriod(300L);
        check(m.getGranularityPeriod() == 300L, "interface granularity period");
        check(!m.isActive(), "interface isActive false before start");
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
        Class<?> c = Class.forName("javax.management.monitor.MonitorMBean");
        java.util.Set<String> s = sigs(c);
        String[] expected = {
            "start/0", "stop/0",
            "addObservedObject/1", "removeObservedObject/1", "containsObservedObject/1",
            "getObservedObjects/0", "getObservedObject/0", "setObservedObject/1",
            "getObservedAttribute/0", "setObservedAttribute/1",
            "getGranularityPeriod/0", "setGranularityPeriod/1",
            "isActive/0"
        };
        for (String e : expected) check(s.contains(e), "MonitorMBean declares " + e);
        check(s.size() == 13, "MonitorMBean method set exact, got " + s);
    }

    @SuppressWarnings("deprecation")
    static void testDeepThroughInterfaceClosedLoop() throws Exception {
        javax.management.MBeanServer server = javax.management.MBeanServerFactory.newMBeanServer();
        javax.management.ObjectName observedName = new javax.management.ObjectName("jck.mmb.i:type=obs");
        javax.management.ObjectName monitorName = new javax.management.ObjectName("jck.mmb.i:type=mon");
        server.registerMBean(new Observed(), observedName);
        javax.management.monitor.CounterMonitor cm = new javax.management.monitor.CounterMonitor();
        server.registerMBean(cm, monitorName);
        check(cm instanceof javax.management.monitor.MonitorMBean, "CounterMonitor instanceof MonitorMBean");
        javax.management.monitor.MonitorMBean ref = cm;

        // configuration entirely through the interface reference
        ref.setGranularityPeriod(200L);
        check(ref.getGranularityPeriod() == 200L, "granularity via interface");
        ref.setObservedAttribute("Count");
        check("Count".equals(ref.getObservedAttribute()), "observed attribute via interface");
        ref.setObservedObject(observedName);
        check(observedName.equals(ref.getObservedObject()), "deprecated set/getObservedObject via interface");
        ref.addObservedObject(new javax.management.ObjectName("jck.mmb.i:type=extra"));
        check(ref.getObservedObjects().length == 2, "addObservedObject via interface");
        check(ref.containsObservedObject(observedName), "containsObservedObject via interface");
        ref.removeObservedObject(new javax.management.ObjectName("jck.mmb.i:type=extra"));
        check(ref.getObservedObjects().length == 1, "removeObservedObject via interface");
        check(!ref.isActive(), "inactive before start");

        // monitor-level attributes as seen through the MBeanServer
        check("Count".equals(server.getAttribute(monitorName, "ObservedAttribute")),
                "ObservedAttribute readable through server");
        server.setAttribute(monitorName, new javax.management.Attribute("GranularityPeriod", 300L));
        check(((Long) server.getAttribute(monitorName, "GranularityPeriod")).longValue() == 300L,
                "GranularityPeriod writable through server");
        check(Boolean.FALSE.equals(server.getAttribute(monitorName, "Active")),
                "Active attribute false before start");

        // start through the interface drives a real threshold notification
        cm.setInitThreshold(5);
        cm.setNotify(true);
        Collector col = new Collector();
        server.addNotificationListener(monitorName, col, null, null);
        ref.start();
        try {
            check(ref.isActive(), "isActive via interface after start");
            check(Boolean.TRUE.equals(server.getAttribute(monitorName, "Active")),
                    "Active attribute true after start");
            server.setAttribute(observedName, new javax.management.Attribute("Count", 6));
            javax.management.Notification n = col.waitFor(
                    javax.management.monitor.MonitorNotification.THRESHOLD_VALUE_EXCEEDED, 1, 5000);
            check(n != null, "notification delivered while driven through interface");
        } finally {
            ref.stop();
        }
        check(!ref.isActive(), "inactive after stop via interface");
    }
}
