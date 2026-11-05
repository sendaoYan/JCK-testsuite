/*
 * @test
 * @summary javax.management.monitor.CounterMonitor conformance: concrete class reflection.
 * @run main CounterMonitorTest
 */

public class CounterMonitorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.monitor.CounterMonitor");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("javax.management.monitor.Monitor".equals(c.getSuperclass().getName()), "superclass javax.management.monitor.Monitor");
        check(Class.forName("javax.management.monitor.CounterMonitorMBean").isAssignableFrom(c), "implements/extends javax.management.monitor.CounterMonitorMBean");
        check(hasMethod(c, "getDerivedGauge"), "declares getDerivedGauge");
        check(hasMethod(c, "getDerivedGaugeTimeStamp"), "declares getDerivedGaugeTimeStamp");
        check(hasMethod(c, "getDifferenceMode"), "declares getDifferenceMode");
        testDeepThresholdClosedLoop();
        testDeepOffsetChain();
        testDeepModulusWrap();
        testDeepDifferenceModeFalseMatchesObserved();
        testDeepStopHaltsNotifications();
        testDeepNotifySwitchOff();
        testDeepThresholdTypeError();
        testDeepUnknownObjectGetters();
        testDeepValidationMatrix();
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

    // ==== deepened: real closed loop (batch i, 2026-11) ====

    // Crossing the initial threshold emits jmx.monitor.counter.threshold with the
    // observed value as derived gauge and the threshold as trigger.
    @SuppressWarnings("deprecation")
    static void testDeepThresholdClosedLoop() throws Exception {
        javax.management.MBeanServer server = javax.management.MBeanServerFactory.newMBeanServer();
        javax.management.ObjectName observedName = new javax.management.ObjectName("jck.cm.i:type=obs");
        javax.management.ObjectName monitorName = new javax.management.ObjectName("jck.cm.i:type=mon");
        Observed obs = new Observed();
        server.registerMBean(obs, observedName);
        javax.management.monitor.CounterMonitor cm = new javax.management.monitor.CounterMonitor();
        server.registerMBean(cm, monitorName);
        cm.addObservedObject(observedName);
        cm.setObservedAttribute("Count");
        cm.setGranularityPeriod(100L);
        cm.setInitThreshold(5);
        cm.setNotify(true);
        Collector col = new Collector();
        server.addNotificationListener(monitorName, col, null, null);
        cm.start();
        try {
            server.setAttribute(observedName, new javax.management.Attribute("Count", 6));
            javax.management.Notification n = col.waitFor(
                    javax.management.monitor.MonitorNotification.THRESHOLD_VALUE_EXCEEDED, 1, 5000);
            check(n != null, "threshold notification delivered");
            check("jmx.monitor.counter.threshold".equals(n.getType()), "exact type string");
            javax.management.monitor.MonitorNotification mn = (javax.management.monitor.MonitorNotification) n;
            check(observedName.equals(mn.getObservedObject()), "observed object");
            check("Count".equals(mn.getObservedAttribute()), "observed attribute");
            check(mn.getDerivedGauge() instanceof Integer && ((Integer) mn.getDerivedGauge()).intValue() == 6,
                    "derived gauge is observed value 6");
            check(mn.getTrigger() instanceof Integer && ((Integer) mn.getTrigger()).intValue() == 5,
                    "trigger is threshold 5");
            check(n.getSequenceNumber() == 0L, "first notification sequence number 0");
            check(n.getTimeStamp() > 0L, "notification time stamp positive");
            check(monitorName.equals(n.getSource()), "source is monitor ObjectName");
            check(((Number) cm.getDerivedGauge(observedName)).intValue() == 6, "getDerivedGauge(ObjectName)");
            check(cm.getDerivedGauge().intValue() == 6, "getDerivedGauge() deprecated form agrees");
            check(cm.getThreshold(observedName).intValue() == 5, "threshold unchanged without offset");
            check(cm.getDerivedGaugeTimeStamp(observedName) > 0L, "derived gauge time stamp positive");
        } finally {
            cm.stop();
        }
        check(!cm.isActive(), "stopped in finally");
    }

    // offset semantics: after each crossing the threshold advances by offset,
    // producing a chain of notifications with triggers 5, 7, 9.
    static void testDeepOffsetChain() throws Exception {
        javax.management.MBeanServer server = javax.management.MBeanServerFactory.newMBeanServer();
        javax.management.ObjectName observedName = new javax.management.ObjectName("jck.cm.i:type=offObs");
        javax.management.ObjectName monitorName = new javax.management.ObjectName("jck.cm.i:type=offMon");
        server.registerMBean(new Observed(), observedName);
        javax.management.monitor.CounterMonitor cm = new javax.management.monitor.CounterMonitor();
        server.registerMBean(cm, monitorName);
        cm.addObservedObject(observedName);
        cm.setObservedAttribute("Count");
        cm.setGranularityPeriod(100L);
        cm.setInitThreshold(5);
        cm.setOffset(2);
        cm.setNotify(true);
        Collector col = new Collector();
        server.addNotificationListener(monitorName, col, null, null);
        String t = javax.management.monitor.MonitorNotification.THRESHOLD_VALUE_EXCEEDED;
        cm.start();
        try {
            server.setAttribute(observedName, new javax.management.Attribute("Count", 6));
            javax.management.Notification n1 = col.waitFor(t, 1, 5000);
            check(n1 != null, "first crossing notified");
            server.setAttribute(observedName, new javax.management.Attribute("Count", 8));
            javax.management.Notification n2 = col.waitFor(t, 2, 5000);
            check(n2 != null, "second crossing notified");
            server.setAttribute(observedName, new javax.management.Attribute("Count", 10));
            javax.management.Notification n3 = col.waitFor(t, 3, 5000);
            check(n3 != null, "third crossing notified");
            int[] triggers = new int[3];
            for (int i = 0; i < 3; i++) {
                javax.management.monitor.MonitorNotification mn = (javax.management.monitor.MonitorNotification) col.nth(t, i);
                triggers[i] = ((Number) mn.getTrigger()).intValue();
            }
            check(triggers[0] == 5 && triggers[1] == 7 && triggers[2] == 9,
                    "trigger chain 5,7,9, got " + java.util.Arrays.toString(triggers));
            check(cm.getThreshold(observedName).intValue() == 11, "threshold advanced to 11 after third crossing");
            check(cm.getInitThreshold().intValue() == 5, "init threshold unchanged by offset chain");
        } finally {
            cm.stop();
        }
    }

    // modulus wrap in difference mode: 9 -> 0 with modulus 10 derives 1, never -9.
    static void testDeepModulusWrap() throws Exception {
        javax.management.MBeanServer server = javax.management.MBeanServerFactory.newMBeanServer();
        javax.management.ObjectName observedName = new javax.management.ObjectName("jck.cm.i:type=wrapObs");
        javax.management.ObjectName monitorName = new javax.management.ObjectName("jck.cm.i:type=wrapMon");
        Observed obs = new Observed();
        obs.setCount(9); // baseline captured at first poll
        server.registerMBean(obs, observedName);
        javax.management.monitor.CounterMonitor cm = new javax.management.monitor.CounterMonitor();
        server.registerMBean(cm, monitorName);
        cm.addObservedObject(observedName);
        cm.setObservedAttribute("Count");
        cm.setGranularityPeriod(200L);
        cm.setModulus(10);
        cm.setDifferenceMode(true);
        cm.start();
        try {
            // wait for the first poll to record the baseline 9 before wrapping:
            // addObservedObject() already stamps the time (Monitor.java), so the
            // usable signal is the stamp CHANGING, which happens once the scheduler's
            // first sample has run. Wrapping earlier would make 0 itself the baseline.
            final long stampAtAdd = cm.getDerivedGaugeTimeStamp(observedName);
            long settleDeadline = System.currentTimeMillis() + 3000;
            while (System.currentTimeMillis() < settleDeadline
                    && cm.getDerivedGaugeTimeStamp(observedName) == stampAtAdd) {
                Thread.sleep(STEP_MS);
            }
            check(cm.getDerivedGaugeTimeStamp(observedName) > stampAtAdd,
                    "first poll recorded baseline (time stamp advanced)");
            server.setAttribute(observedName, new javax.management.Attribute("Count", 0)); // wrap
            boolean sawOne = false;
            boolean sawNegative = false;
            long deadline = System.currentTimeMillis() + 3000;
            while (System.currentTimeMillis() < deadline && !sawOne) {
                Number dg = cm.getDerivedGauge(observedName);
                if (dg != null) {
                    if (dg.intValue() == 1) sawOne = true;
                    if (dg.intValue() == -9) sawNegative = true;
                }
                Thread.sleep(STEP_MS);
            }
            check(sawOne, "derived gauge wraps to 1 (0-9+10), never negative");
            check(!sawNegative, "no raw -9 sample observed");
        } finally {
            cm.stop();
        }
    }

    // Without difference mode the derived gauge equals the observed value and is stable.
    static void testDeepDifferenceModeFalseMatchesObserved() throws Exception {
        javax.management.MBeanServer server = javax.management.MBeanServerFactory.newMBeanServer();
        javax.management.ObjectName observedName = new javax.management.ObjectName("jck.cm.i:type=plainObs");
        javax.management.ObjectName monitorName = new javax.management.ObjectName("jck.cm.i:type=plainMon");
        server.registerMBean(new Observed(), observedName);
        javax.management.monitor.CounterMonitor cm = new javax.management.monitor.CounterMonitor();
        server.registerMBean(cm, monitorName);
        cm.addObservedObject(observedName);
        cm.setObservedAttribute("Count");
        cm.setGranularityPeriod(100L);
        cm.setDifferenceMode(false);
        cm.start();
        try {
            server.setAttribute(observedName, new javax.management.Attribute("Count", 7));
            long deadline = System.currentTimeMillis() + 3000;
            while (System.currentTimeMillis() < deadline
                    && (cm.getDerivedGauge(observedName) == null
                        || cm.getDerivedGauge(observedName).intValue() != 7)) {
                Thread.sleep(STEP_MS);
            }
            check(cm.getDerivedGauge(observedName).intValue() == 7, "derived gauge equals observed value");
            Thread.sleep(400); // value stable: gauge must stay at 7 (non-transient)
            check(cm.getDerivedGauge(observedName).intValue() == 7, "derived gauge stable without difference mode");
        } finally {
            cm.stop();
        }
    }

    // After stop() no further notifications are emitted even when crossing again.
    static void testDeepStopHaltsNotifications() throws Exception {
        javax.management.MBeanServer server = javax.management.MBeanServerFactory.newMBeanServer();
        javax.management.ObjectName observedName = new javax.management.ObjectName("jck.cm.i:type=haltObs");
        javax.management.ObjectName monitorName = new javax.management.ObjectName("jck.cm.i:type=haltMon");
        server.registerMBean(new Observed(), observedName);
        javax.management.monitor.CounterMonitor cm = new javax.management.monitor.CounterMonitor();
        server.registerMBean(cm, monitorName);
        cm.addObservedObject(observedName);
        cm.setObservedAttribute("Count");
        cm.setGranularityPeriod(100L);
        cm.setInitThreshold(5);
        cm.setNotify(true);
        Collector col = new Collector();
        server.addNotificationListener(monitorName, col, null, null);
        String t = javax.management.monitor.MonitorNotification.THRESHOLD_VALUE_EXCEEDED;
        cm.start();
        server.setAttribute(observedName, new javax.management.Attribute("Count", 6));
        javax.management.Notification n = col.waitFor(t, 1, 5000);
        check(n != null, "notification before stop");
        cm.stop();
        server.setAttribute(observedName, new javax.management.Attribute("Count", 12));
        int delivered = col.count(t);
        long quiet = System.currentTimeMillis() + 800;
        while (System.currentTimeMillis() < quiet) Thread.sleep(STEP_MS);
        check(col.count(t) == delivered, "no notifications after stop");
        check(col.count(t) == 1, "exactly one notification in total");
    }

    // notify=false: crossings update the derived gauge but stay silent.
    static void testDeepNotifySwitchOff() throws Exception {
        javax.management.MBeanServer server = javax.management.MBeanServerFactory.newMBeanServer();
        javax.management.ObjectName observedName = new javax.management.ObjectName("jck.cm.i:type=silentObs");
        javax.management.ObjectName monitorName = new javax.management.ObjectName("jck.cm.i:type=silentMon");
        server.registerMBean(new Observed(), observedName);
        javax.management.monitor.CounterMonitor cm = new javax.management.monitor.CounterMonitor();
        server.registerMBean(cm, monitorName);
        cm.addObservedObject(observedName);
        cm.setObservedAttribute("Count");
        cm.setGranularityPeriod(100L);
        cm.setInitThreshold(5);
        cm.setNotify(false);
        Collector col = new Collector();
        server.addNotificationListener(monitorName, col, null, null);
        cm.start();
        try {
            server.setAttribute(observedName, new javax.management.Attribute("Count", 9));
            check(col.quietFor(javax.management.monitor.MonitorNotification.THRESHOLD_VALUE_EXCEEDED, 800),
                    "no notification while notify=false");
            long deadline = System.currentTimeMillis() + 3000;
            while (System.currentTimeMillis() < deadline && cm.getDerivedGauge(observedName).intValue() != 9) {
                Thread.sleep(STEP_MS);
            }
            check(cm.getDerivedGauge(observedName).intValue() == 9, "derived gauge still tracked with notify=false");
        } finally {
            cm.stop();
        }
    }

    // Threshold type must match the observed type: Integer threshold over a Long
    // attribute yields jmx.monitor.error.threshold and no threshold notification.
    static void testDeepThresholdTypeError() throws Exception {
        javax.management.MBeanServer server = javax.management.MBeanServerFactory.newMBeanServer();
        javax.management.ObjectName observedName = new javax.management.ObjectName("jck.cm.i:type=tterrObs");
        javax.management.ObjectName monitorName = new javax.management.ObjectName("jck.cm.i:type=tterrMon");
        server.registerMBean(new Observed(), observedName);
        javax.management.monitor.CounterMonitor cm = new javax.management.monitor.CounterMonitor();
        server.registerMBean(cm, monitorName);
        cm.addObservedObject(observedName);
        cm.setObservedAttribute("Big"); // long attribute
        cm.setGranularityPeriod(100L);
        cm.setInitThreshold(5);         // Integer threshold
        cm.setNotify(true);
        Collector col = new Collector();
        server.addNotificationListener(monitorName, col, null, null);
        cm.start();
        try {
            server.setAttribute(observedName, new javax.management.Attribute("Big", 7L));
            javax.management.Notification n = col.waitFor(
                    javax.management.monitor.MonitorNotification.THRESHOLD_ERROR, 1, 5000);
            check(n != null, "THRESHOLD_ERROR delivered");
            check("jmx.monitor.error.threshold".equals(n.getType()), "threshold error exact type string");
            check(col.quietFor(javax.management.monitor.MonitorNotification.THRESHOLD_VALUE_EXCEEDED, 600),
                    "no threshold notification despite value above threshold");
        } finally {
            cm.stop();
        }
    }

    // Per-object getters for unknown names return null (no exception).
    static void testDeepUnknownObjectGetters() throws Exception {
        javax.management.monitor.CounterMonitor cm = new javax.management.monitor.CounterMonitor();
        javax.management.ObjectName unknown = new javax.management.ObjectName("jck.cm.i:type=unknown");
        check(cm.getThreshold(unknown) == null, "getThreshold(unknown) null");
        check(cm.getDerivedGauge(unknown) == null, "getDerivedGauge(unknown) null");
        check(cm.getDerivedGaugeTimeStamp(unknown) == 0L, "getDerivedGaugeTimeStamp(unknown) 0");
        check(cm.getDerivedGauge() == null, "getDerivedGauge() null without observed object");
        check(cm.getDerivedGaugeTimeStamp() == 0L, "getDerivedGaugeTimeStamp() 0 without observed object");
    }

    // Validation matrix with exact messages.
    @SuppressWarnings("deprecation")
    static void testDeepValidationMatrix() throws Exception {
        final javax.management.monitor.CounterMonitor cm = new javax.management.monitor.CounterMonitor();
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { cm.setInitThreshold(null); } }, "setInitThreshold(null)");
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { cm.setInitThreshold(-1); } }, "setInitThreshold(-1)");
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { cm.setOffset(null); } }, "setOffset(null)");
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { cm.setOffset(-1); } }, "setOffset(-1)");
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { cm.setModulus(null); } }, "setModulus(null)");
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { cm.setModulus(-1); } }, "setModulus(-1)");
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { cm.setThreshold(null); } }, "setThreshold(null)");
        try {
            cm.setOffset(-1);
            throw new AssertionError("setOffset(-1) must throw");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("Negative offset"),
                    "offset message, got: " + e.getMessage());
        }
        try {
            cm.setInitThreshold(-1);
            throw new AssertionError("setInitThreshold(-1) must throw");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("Negative threshold"),
                    "threshold message, got: " + e.getMessage());
        }
        try {
            cm.setModulus(-1);
            throw new AssertionError("setModulus(-1) must throw");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("Negative modulus"),
                    "modulus message, got: " + e.getMessage());
        }
        // deprecated setThreshold is an alias of setInitThreshold
        cm.setThreshold(9);
        check(cm.getInitThreshold().intValue() == 9, "setThreshold aliases setInitThreshold");
        check(cm.getThreshold(new javax.management.ObjectName("jck.cm.i:type=none2")) == null,
                "per-object threshold null before observation starts");
    }
}
