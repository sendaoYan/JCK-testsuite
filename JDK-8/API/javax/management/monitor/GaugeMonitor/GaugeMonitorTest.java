/*
 * @test
 * @summary javax.management.monitor.GaugeMonitor conformance: concrete class reflection.
 * @run main GaugeMonitorTest
 */

public class GaugeMonitorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.monitor.GaugeMonitor");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("javax.management.monitor.Monitor".equals(c.getSuperclass().getName()), "superclass javax.management.monitor.Monitor");
        check(Class.forName("javax.management.monitor.GaugeMonitorMBean").isAssignableFrom(c), "implements/extends javax.management.monitor.GaugeMonitorMBean");
        check(hasMethod(c, "getDerivedGauge"), "declares getDerivedGauge");
        check(hasMethod(c, "getDerivedGaugeTimeStamp"), "declares getDerivedGaugeTimeStamp");
        check(hasMethod(c, "getDifferenceMode"), "declares getDifferenceMode");
        testDeepHighLowHysteresisClosedLoop();
        testDeepNotifySwitchMatrix();
        testDeepValidationMessages();
        testDeepDifferenceModeTransient();
        testDeepTypeErrorNotification();
        testDeepDefaultsAndUnknown();
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

    // ==== deepened: real closed loop (batch i, 2026-11) ====

    // High/low thresholds with hysteresis: one notification per crossing direction.
    static void testDeepHighLowHysteresisClosedLoop() throws Exception {
        javax.management.MBeanServer server = javax.management.MBeanServerFactory.newMBeanServer();
        Observed obs = new Observed();
        obs.setGauge(5.0);
        javax.management.ObjectName observedName = new javax.management.ObjectName("jck.gm.i:type=hystObs");
        javax.management.ObjectName monitorName = new javax.management.ObjectName("jck.gm.i:type=hystMon");
        server.registerMBean(obs, observedName);
        javax.management.monitor.GaugeMonitor gm = new javax.management.monitor.GaugeMonitor();
        server.registerMBean(gm, monitorName);
        gm.addObservedObject(observedName);
        gm.setObservedAttribute("Gauge");
        gm.setGranularityPeriod(100L);
        gm.setThresholds(8.0, 3.0);
        gm.setNotifyHigh(true);
        gm.setNotifyLow(true);
        Collector col = new Collector();
        server.addNotificationListener(monitorName, col, null, null);
        String high = javax.management.monitor.MonitorNotification.THRESHOLD_HIGH_VALUE_EXCEEDED;
        String low = javax.management.monitor.MonitorNotification.THRESHOLD_LOW_VALUE_EXCEEDED;
        gm.start();
        try {
            // settle on 5.0 (between thresholds)
            long deadline = System.currentTimeMillis() + 3000;
            while (System.currentTimeMillis() < deadline
                    && !(gm.getDerivedGauge(observedName) instanceof Double
                         && ((Number) gm.getDerivedGauge(observedName)).doubleValue() == 5.0)) {
                Thread.sleep(STEP_MS);
            }
            check(gm.getDerivedGauge(observedName) instanceof Double
                    && ((Number) gm.getDerivedGauge(observedName)).doubleValue() == 5.0,
                    "derived gauge settles at observed value 5.0");
            check(col.received.isEmpty(), "no notification while inside the band");

            // cross high
            server.setAttribute(observedName, new javax.management.Attribute("Gauge", 9.0));
            javax.management.Notification n = col.waitFor(high, 1, 5000);
            check(n != null, "high notification delivered");
            check("jmx.monitor.gauge.high".equals(n.getType()), "high exact type string");
            javax.management.monitor.MonitorNotification mn = (javax.management.monitor.MonitorNotification) n;
            check(((Number) mn.getTrigger()).doubleValue() == 8.0, "high trigger is high threshold");
            check(((Number) mn.getDerivedGauge()).doubleValue() == 9.0, "high derived gauge 9.0");
            check(n.getSequenceNumber() == 0L, "first gauge notification sequence 0");
            check(monitorName.equals(n.getSource()), "source is monitor ObjectName");

            // stay above: hysteresis suppresses duplicates
            int highSoFar = col.count(high);
            server.setAttribute(observedName, new javax.management.Attribute("Gauge", 9.5));
            long quiet = System.currentTimeMillis() + 700;
            while (System.currentTimeMillis() < quiet) Thread.sleep(STEP_MS);
            check(col.count(high) == highSoFar, "no duplicate high notification while staying high");

            // cross low
            server.setAttribute(observedName, new javax.management.Attribute("Gauge", 2.0));
            javax.management.Notification n2 = col.waitFor(low, 1, 5000);
            check(n2 != null, "low notification delivered");
            check("jmx.monitor.gauge.low".equals(n2.getType()), "low exact type string");
            javax.management.monitor.MonitorNotification mn2 = (javax.management.monitor.MonitorNotification) n2;
            check(((Number) mn2.getTrigger()).doubleValue() == 3.0, "low trigger is low threshold");
            check(((Number) mn2.getDerivedGauge()).doubleValue() == 2.0, "low derived gauge 2.0");

            // rearm: back into band, then high again
            server.setAttribute(observedName, new javax.management.Attribute("Gauge", 5.0));
            deadline = System.currentTimeMillis() + 3000;
            while (System.currentTimeMillis() < deadline
                    && ((Number) gm.getDerivedGauge(observedName)).doubleValue() != 5.0) {
                Thread.sleep(STEP_MS);
            }
            server.setAttribute(observedName, new javax.management.Attribute("Gauge", 10.0));
            javax.management.Notification n3 = col.waitFor(high, 2, 5000);
            check(n3 != null && col.count(high) == 2, "high notification rearmed after returning into band");
            check(gm.getHighThreshold().doubleValue() == 8.0 && gm.getLowThreshold().doubleValue() == 3.0,
                    "threshold getters unchanged");
            check(gm.getDerivedGaugeTimeStamp(observedName) > 0L, "derived gauge time stamp positive");
        } finally {
            gm.stop();
        }
    }

    // Switch matrix: notifyHigh=false suppresses high but not low (and mirrored).
    static void testDeepNotifySwitchMatrix() throws Exception {
        javax.management.MBeanServer server = javax.management.MBeanServerFactory.newMBeanServer();
        String high = javax.management.monitor.MonitorNotification.THRESHOLD_HIGH_VALUE_EXCEEDED;
        String low = javax.management.monitor.MonitorNotification.THRESHOLD_LOW_VALUE_EXCEEDED;

        // (a) notifyHigh=false, notifyLow=true
        Observed obsA2 = new Observed();
        obsA2.setGauge(5.0);
        javax.management.ObjectName onA = new javax.management.ObjectName("jck.gm.i:type=matrixAObs");
        javax.management.ObjectName mnA = new javax.management.ObjectName("jck.gm.i:type=matrixAMon");
        server.registerMBean(obsA2, onA);
        javax.management.monitor.GaugeMonitor g1 = new javax.management.monitor.GaugeMonitor();
        server.registerMBean(g1, mnA);
        g1.addObservedObject(onA);
        g1.setObservedAttribute("Gauge");
        g1.setGranularityPeriod(100L);
        g1.setThresholds(8.0, 3.0);
        g1.setNotifyHigh(false);
        g1.setNotifyLow(true);
        Collector colA = new Collector();
        server.addNotificationListener(mnA, colA, null, null);
        g1.start();
        try {
            server.setAttribute(onA, new javax.management.Attribute("Gauge", 9.0));
            check(colA.quietFor(high, 700), "notifyHigh=false: no high notification");
            server.setAttribute(onA, new javax.management.Attribute("Gauge", 2.0));
            javax.management.Notification n = colA.waitFor(low, 1, 5000);
            check(n != null, "notifyLow=true: low notification still delivered");
        } finally {
            g1.stop();
        }

        // (b) notifyHigh=true, notifyLow=false
        Observed obsB = new Observed();
        obsB.setGauge(5.0);
        javax.management.ObjectName onB = new javax.management.ObjectName("jck.gm.i:type=matrixBObs");
        javax.management.ObjectName mnB = new javax.management.ObjectName("jck.gm.i:type=matrixBMon");
        server.registerMBean(obsB, onB);
        javax.management.monitor.GaugeMonitor g2 = new javax.management.monitor.GaugeMonitor();
        server.registerMBean(g2, mnB);
        g2.addObservedObject(onB);
        g2.setObservedAttribute("Gauge");
        g2.setGranularityPeriod(100L);
        g2.setThresholds(8.0, 3.0);
        g2.setNotifyHigh(true);
        g2.setNotifyLow(false);
        Collector colB = new Collector();
        server.addNotificationListener(mnB, colB, null, null);
        g2.start();
        try {
            server.setAttribute(onB, new javax.management.Attribute("Gauge", 9.0));
            javax.management.Notification n = colB.waitFor(high, 1, 5000);
            check(n != null, "notifyHigh=true: high notification delivered");
            server.setAttribute(onB, new javax.management.Attribute("Gauge", 2.0));
            check(colB.quietFor(low, 700), "notifyLow=false: no low notification");
        } finally {
            g2.stop();
        }
    }

    // Validation with exact messages.
    static void testDeepValidationMessages() throws Exception {
        final javax.management.monitor.GaugeMonitor gm = new javax.management.monitor.GaugeMonitor();
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { gm.setThresholds(null, 3.0); } }, "setThresholds(null high)");
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { gm.setThresholds(8.0, null); } }, "setThresholds(null low)");
        try {
            gm.setThresholds(3.0, 8.0);
            throw new AssertionError("high<low must throw");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("High threshold less than low threshold"),
                    "high<low message, got: " + e.getMessage());
        }
        try {
            gm.setThresholds(null, 8.0);
            throw new AssertionError("null high must throw");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("Null threshold value"),
                    "null high message, got: " + e.getMessage());
        }
        check(gm.getHighThreshold() != null && gm.getHighThreshold().intValue() == 0
                && gm.getLowThreshold() != null && gm.getLowThreshold().intValue() == 0,
                "failed setThresholds leaves both thresholds at the Integer 0 default");
    }

    // Difference mode derives the transient delta between consecutive observed values.
    static void testDeepDifferenceModeTransient() throws Exception {
        javax.management.MBeanServer server = javax.management.MBeanServerFactory.newMBeanServer();
        Observed obs = new Observed();
        obs.setGauge(5.0);
        javax.management.ObjectName observedName = new javax.management.ObjectName("jck.gm.i:type=diffObs");
        javax.management.ObjectName monitorName = new javax.management.ObjectName("jck.gm.i:type=diffMon");
        server.registerMBean(obs, observedName);
        javax.management.monitor.GaugeMonitor gm = new javax.management.monitor.GaugeMonitor();
        server.registerMBean(gm, monitorName);
        gm.addObservedObject(observedName);
        gm.setObservedAttribute("Gauge");
        gm.setGranularityPeriod(200L);
        gm.setDifferenceMode(true);
        gm.start();
        try {
            // wait for baseline (derived gauge becomes Double 0.0 after the second poll)
            long deadline = System.currentTimeMillis() + 3000;
            while (System.currentTimeMillis() < deadline
                    && !(gm.getDerivedGauge(observedName) instanceof Double
                         && ((Number) gm.getDerivedGauge(observedName)).doubleValue() == 0.0)) {
                Thread.sleep(STEP_MS);
            }
            check(gm.getDerivedGauge(observedName) instanceof Double
                    && ((Number) gm.getDerivedGauge(observedName)).doubleValue() == 0.0,
                    "difference mode baseline settles at 0.0");
            server.setAttribute(observedName, new javax.management.Attribute("Gauge", 7.0));
            boolean sawDelta = false;
            deadline = System.currentTimeMillis() + 3000;
            while (System.currentTimeMillis() < deadline && !sawDelta) {
                Number dg = (Number) gm.getDerivedGauge(observedName);
                if (dg != null && dg.doubleValue() == 2.0) sawDelta = true;
                else Thread.sleep(STEP_MS);
            }
            check(sawDelta, "difference mode derives transient delta 2.0 (7.0-5.0)");
        } finally {
            gm.stop();
        }
    }

    // Gauge monitor cannot observe a String attribute: jmx.monitor.error.type.
    static void testDeepTypeErrorNotification() throws Exception {
        javax.management.MBeanServer server = javax.management.MBeanServerFactory.newMBeanServer();
        javax.management.ObjectName observedName = new javax.management.ObjectName("jck.gm.i:type=terrObs");
        javax.management.ObjectName monitorName = new javax.management.ObjectName("jck.gm.i:type=terrMon");
        server.registerMBean(new Observed(), observedName);
        javax.management.monitor.GaugeMonitor gm = new javax.management.monitor.GaugeMonitor();
        server.registerMBean(gm, monitorName);
        gm.addObservedObject(observedName);
        gm.setObservedAttribute("Str");
        gm.setGranularityPeriod(100L);
        gm.setThresholds(8.0, 3.0);
        gm.setNotifyHigh(true);
        gm.setNotifyLow(true);
        Collector col = new Collector();
        server.addNotificationListener(monitorName, col, null, null);
        gm.start();
        try {
            javax.management.Notification n = col.waitFor(
                    javax.management.monitor.MonitorNotification.OBSERVED_ATTRIBUTE_TYPE_ERROR, 1, 5000);
            check(n != null, "OBSERVED_ATTRIBUTE_TYPE_ERROR for String attribute");
            check("jmx.monitor.error.type".equals(n.getType()), "exact type error string");
        } finally {
            gm.stop();
        }
    }

    // Default state and unknown-object getters.
    static void testDeepDefaultsAndUnknown() throws Exception {
        javax.management.monitor.GaugeMonitor gm = new javax.management.monitor.GaugeMonitor();
        check(!gm.getNotifyHigh() && !gm.getNotifyLow(), "notify switches default false");
        check(!gm.getDifferenceMode(), "difference mode default false");
        check(gm.getHighThreshold() != null && gm.getHighThreshold().intValue() == 0
                && gm.getLowThreshold() != null && gm.getLowThreshold().intValue() == 0,
                "thresholds default to Integer 0, not null");
        javax.management.ObjectName observedName = new javax.management.ObjectName("jck.gm.i:type=dfltObs");
        javax.management.ObjectName unknown = new javax.management.ObjectName("jck.gm.i:type=unknown");
        gm.addObservedObject(observedName);
        check(gm.getDerivedGauge(observedName) instanceof Integer
                && ((Number) gm.getDerivedGauge(observedName)).intValue() == 0,
                "derived gauge initialised to Integer 0 before first poll");
        check(gm.getDerivedGauge(unknown) == null, "getDerivedGauge(unknown) null");
        check(gm.getDerivedGaugeTimeStamp(unknown) == 0L, "getDerivedGaugeTimeStamp(unknown) 0");
    }
}
