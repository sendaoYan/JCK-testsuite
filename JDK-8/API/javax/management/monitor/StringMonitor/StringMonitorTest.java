/*
 * @test
 * @summary javax.management.monitor.StringMonitor conformance: concrete class reflection.
 * @run main StringMonitorTest
 */

public class StringMonitorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.monitor.StringMonitor");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("javax.management.monitor.Monitor".equals(c.getSuperclass().getName()), "superclass javax.management.monitor.Monitor");
        check(Class.forName("javax.management.monitor.StringMonitorMBean").isAssignableFrom(c), "implements/extends javax.management.monitor.StringMonitorMBean");
        check(hasMethod(c, "getDerivedGauge"), "declares getDerivedGauge");
        check(hasMethod(c, "getDerivedGaugeTimeStamp"), "declares getDerivedGaugeTimeStamp");
        check(hasMethod(c, "getNotificationInfo"), "declares getNotificationInfo");
        testDeepDiffersMatchesClosedLoop();
        testDeepSwitchMatrix();
        testDeepValidation();
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

    static void testDeepDiffersMatchesClosedLoop() throws Exception {
        javax.management.MBeanServer server = javax.management.MBeanServerFactory.newMBeanServer();
        javax.management.ObjectName observedName = new javax.management.ObjectName("jck.sm.i:type=obs");
        javax.management.ObjectName monitorName = new javax.management.ObjectName("jck.sm.i:type=mon");
        server.registerMBean(new Observed(), observedName);
        javax.management.monitor.StringMonitor sm = new javax.management.monitor.StringMonitor();
        server.registerMBean(sm, monitorName);
        sm.addObservedObject(observedName);
        sm.setObservedAttribute("Str");
        sm.setGranularityPeriod(100L);
        sm.setStringToCompare("GO");
        sm.setNotifyDiffer(true);
        sm.setNotifyMatch(true);
        Collector col = new Collector();
        server.addNotificationListener(monitorName, col, null, null);
        String differs = javax.management.monitor.MonitorNotification.STRING_TO_COMPARE_VALUE_DIFFERED;
        String matches = javax.management.monitor.MonitorNotification.STRING_TO_COMPARE_VALUE_MATCHED;
        sm.start();
        try {
            // first poll: "INIT" differs from "GO"
            javax.management.Notification n = col.waitFor(differs, 1, 5000);
            check(n != null, "initial differs notification delivered");
            check("jmx.monitor.string.differs".equals(n.getType()), "differs exact type string");
            javax.management.monitor.MonitorNotification mn = (javax.management.monitor.MonitorNotification) n;
            check("INIT".equals(mn.getDerivedGauge()), "differs derived gauge is current string");
            check(n.getSequenceNumber() == 0L, "first string notification sequence 0");
            check(monitorName.equals(n.getSource()), "source is monitor ObjectName");

            // switch to the compared string
            server.setAttribute(observedName, new javax.management.Attribute("Str", "GO"));
            javax.management.Notification n2 = col.waitFor(matches, 1, 5000);
            check(n2 != null, "matches notification delivered");
            check("jmx.monitor.string.matches".equals(n2.getType()), "matches exact type string");
            javax.management.monitor.MonitorNotification mn2 = (javax.management.monitor.MonitorNotification) n2;
            check("GO".equals(mn2.getTrigger()), "matches trigger is string to compare");
            check("GO".equals(mn2.getDerivedGauge()), "matches derived gauge is current string");

            // and away again
            server.setAttribute(observedName, new javax.management.Attribute("Str", "STOP"));
            javax.management.Notification n3 = col.waitFor(differs, 2, 5000);
            check(n3 != null && col.count(differs) == 2, "second differs notification delivered");

            long deadline = System.currentTimeMillis() + 3000;
            while (System.currentTimeMillis() < deadline && !"STOP".equals(sm.getDerivedGauge())) {
                Thread.sleep(STEP_MS);
            }
            check("STOP".equals(sm.getDerivedGauge()), "getDerivedGauge() tracks current string");
            check("STOP".equals(sm.getDerivedGauge(observedName)), "per-object derived gauge tracks string");
            check(sm.getDerivedGaugeTimeStamp(observedName) > 0L, "derived gauge time stamp positive");
        } finally {
            sm.stop();
        }
    }

    // Switch matrix: notifyDiffer=false keeps the initial difference silent;
    // notifyMatch=false keeps the match silent.
    static void testDeepSwitchMatrix() throws Exception {
        javax.management.MBeanServer server = javax.management.MBeanServerFactory.newMBeanServer();
        String differs = javax.management.monitor.MonitorNotification.STRING_TO_COMPARE_VALUE_DIFFERED;
        String matches = javax.management.monitor.MonitorNotification.STRING_TO_COMPARE_VALUE_MATCHED;

        // (a) notifyDiffer=false, notifyMatch=true
        javax.management.ObjectName onA = new javax.management.ObjectName("jck.sm.i:type=swAObs");
        javax.management.ObjectName mnA = new javax.management.ObjectName("jck.sm.i:type=swAMon");
        server.registerMBean(new Observed(), onA);
        javax.management.monitor.StringMonitor s1 = new javax.management.monitor.StringMonitor();
        server.registerMBean(s1, mnA);
        s1.addObservedObject(onA);
        s1.setObservedAttribute("Str");
        s1.setGranularityPeriod(100L);
        s1.setStringToCompare("GO");
        s1.setNotifyDiffer(false);
        s1.setNotifyMatch(true);
        Collector colA = new Collector();
        server.addNotificationListener(mnA, colA, null, null);
        s1.start();
        try {
            check(colA.quietFor(differs, 700), "notifyDiffer=false: no initial differs notification");
            server.setAttribute(onA, new javax.management.Attribute("Str", "GO"));
            javax.management.Notification n = colA.waitFor(matches, 1, 5000);
            check(n != null, "notifyMatch=true: matches still delivered");
        } finally {
            s1.stop();
        }

        // (b) notifyDiffer=true, notifyMatch=false
        javax.management.ObjectName onB = new javax.management.ObjectName("jck.sm.i:type=swBObs");
        javax.management.ObjectName mnB = new javax.management.ObjectName("jck.sm.i:type=swBMon");
        server.registerMBean(new Observed(), onB);
        javax.management.monitor.StringMonitor s2 = new javax.management.monitor.StringMonitor();
        server.registerMBean(s2, mnB);
        s2.addObservedObject(onB);
        s2.setObservedAttribute("Str");
        s2.setGranularityPeriod(100L);
        s2.setStringToCompare("GO");
        s2.setNotifyDiffer(true);
        s2.setNotifyMatch(false);
        Collector colB = new Collector();
        server.addNotificationListener(mnB, colB, null, null);
        s2.start();
        try {
            javax.management.Notification n = colB.waitFor(differs, 1, 5000);
            check(n != null, "notifyDiffer=true: differs delivered");
            server.setAttribute(onB, new javax.management.Attribute("Str", "GO"));
            check(colB.quietFor(matches, 700), "notifyMatch=false: no matches notification");
        } finally {
            s2.stop();
        }
    }

    // null is rejected with a message; the empty string is accepted by this implementation.
    static void testDeepValidation() throws Exception {
        final javax.management.monitor.StringMonitor sm = new javax.management.monitor.StringMonitor();
        check(!sm.getNotifyMatch() && !sm.getNotifyDiffer(), "switches default false");
        check(sm.getDerivedGauge() == null, "derived gauge null before observation");
        javax.management.ObjectName unknown = new javax.management.ObjectName("jck.sm.i:type=unknown");
        check(sm.getDerivedGauge(unknown) == null, "derived gauge unknown object null");
        check(sm.getDerivedGaugeTimeStamp(unknown) == 0L, "time stamp unknown object 0");
        try {
            sm.setStringToCompare(null);
            throw new AssertionError("null string to compare must throw");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("Null string to compare"),
                    "null compare message, got: " + e.getMessage());
        }
        sm.setStringToCompare("");
        check("".equals(sm.getStringToCompare()), "empty string to compare is accepted (recorded behaviour)");
    }
}
