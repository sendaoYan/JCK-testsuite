/*
 * @test
 * @summary javax.management.monitor.MonitorSettingException conformance: exception hierarchy and construction.
 * @run main MonitorSettingExceptionTest
 */

public class MonitorSettingExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.management.monitor.MonitorSettingException x = new javax.management.monitor.MonitorSettingException("msg");
        Class<?> c = x.getClass();
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(Class.forName("javax.management.JMRuntimeException").isAssignableFrom(c), "extends javax.management.JMRuntimeException");
        check(x.toString() != null, "toString non-null");
        x.getMessage(); x.getCause(); x.getStackTrace();
        testDeepConstructorChain();
        testDeepSerialization();
        testDeepNotThrownByMonitorErrorPaths();
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
        Object getBroken();
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
        public Object getBroken() { throw new IllegalStateException("boom"); }
    }

    // ==== deepened: constructor chain, serialization, error-path non-throw (batch i, 2026-11) ====

    static void testDeepConstructorChain() throws Exception {
        javax.management.monitor.MonitorSettingException e1 = new javax.management.monitor.MonitorSettingException();
        check(e1.getMessage() == null, "no-arg message null");
        check(e1.getCause() == null, "no-arg cause null");
        javax.management.monitor.MonitorSettingException e2 = new javax.management.monitor.MonitorSettingException("bad setting");
        check("bad setting".equals(e2.getMessage()), "String message");
        check(e2.getCause() == null, "String ctor cause null");
        check(e1 instanceof javax.management.JMRuntimeException, "instanceof JMRuntimeException");
        check(e1 instanceof RuntimeException, "instanceof RuntimeException");
        check(e1.toString().contains("MonitorSettingException"), "toString contains class name");
        check(java.io.Serializable.class.isAssignableFrom(e1.getClass()), "Serializable");
    }

    static void testDeepSerialization() throws Exception {
        javax.management.monitor.MonitorSettingException orig = new javax.management.monitor.MonitorSettingException("keep me");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(orig);
        oos.close();
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        javax.management.monitor.MonitorSettingException copy = (javax.management.monitor.MonitorSettingException) ois.readObject();
        check("keep me".equals(copy.getMessage()), "message survives serialization");
        check(java.io.ObjectStreamClass.lookup(javax.management.monitor.MonitorSettingException.class)
                .getSerialVersionUID() == -8807913418190202007L, "serialVersionUID locked");
    }

    // Monitors report configuration problems through error notifications; the monitor
    // keeps polling and MonitorSettingException is never raised on these paths
    // (no throw site exists in either JDK - recorded in DESIGN.md).
    static void testDeepNotThrownByMonitorErrorPaths() throws Exception {
        javax.management.MBeanServer server = javax.management.MBeanServerFactory.newMBeanServer();
        javax.management.ObjectName observedName = new javax.management.ObjectName("jck.mse.i:type=obs");
        javax.management.ObjectName monitorName = new javax.management.ObjectName("jck.mse.i:type=mon");
        server.registerMBean(new Observed(), observedName);
        javax.management.monitor.CounterMonitor cm = new javax.management.monitor.CounterMonitor();
        server.registerMBean(cm, monitorName);
        Collector col = new Collector();
        server.addNotificationListener(monitorName, col, null, null);
        cm.addObservedObject(observedName);
        cm.setObservedAttribute("Broken"); // getter throws IllegalStateException
        cm.setGranularityPeriod(100L);
        cm.start();
        try {
            javax.management.Notification n = col.waitFor(
                    javax.management.monitor.MonitorNotification.RUNTIME_ERROR, 1, 5000);
            check(n != null, "runtime error notification delivered");
            check(cm.isActive(), "monitor survives getter failures without MonitorSettingException");
        } finally {
            cm.stop();
        }
    }
}
