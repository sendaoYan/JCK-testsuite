/*
 * @test
 * @summary javax.management.monitor.Monitor conformance: white-box structure + black-box behavior.
 * @run main MonitorTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.management.MBeanRegistration;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;
import javax.management.monitor.Monitor;
import javax.management.monitor.MonitorMBean;

@SuppressWarnings("deprecation")
public class MonitorTest {
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
        Class<?> c = Class.forName("javax.management.monitor.Monitor");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(NotificationBroadcasterSupport.class.isAssignableFrom(c),
                "extends NotificationBroadcasterSupport");
        check(MonitorMBean.class.isAssignableFrom(c), "implements MonitorMBean");
        check(MBeanRegistration.class.isAssignableFrom(c), "implements MBeanRegistration");
        Method[] mm = c.getMethods();
        check(has(mm, "start"), "has start");
        check(has(mm, "stop"), "has stop");
        check(has(mm, "getObservedObject"), "has getObservedObject");
        check(has(mm, "setObservedObject"), "has setObservedObject");
        check(has(mm, "addObservedObject"), "has addObservedObject");
        check(has(mm, "removeObservedObject"), "has removeObservedObject");
        check(has(mm, "containsObservedObject"), "has containsObservedObject");
        check(has(mm, "getObservedObjects"), "has getObservedObjects");
        check(has(mm, "getObservedAttribute"), "has getObservedAttribute");
        check(has(mm, "setObservedAttribute"), "has setObservedAttribute");
        check(has(mm, "getGranularityPeriod"), "has getGranularityPeriod");
        check(has(mm, "setGranularityPeriod"), "has setGranularityPeriod");
        check(has(mm, "isActive"), "has isActive");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Concrete anonymous subclass exercises inherited state without starting a thread.
        Monitor m = new Monitor() {
            public void start() {}
            public void stop() {}
        };

        check(!m.isActive(), "initially inactive");
        check(m.getObservedObject() == null, "no observed object initially");
        check(m.getObservedAttribute() == null, "no observed attribute initially");
        check(m.getGranularityPeriod() == 10000L, "default granularity period 10s");
        check(m.getObservedObjects().length == 0, "no observed objects");

        ObjectName on1 = new ObjectName("test:type=one");
        m.setObservedObject(on1);
        check(on1.equals(m.getObservedObject()), "setObservedObject/getObservedObject");
        check(m.containsObservedObject(on1), "containsObservedObject after set");

        ObjectName on2 = new ObjectName("test:type=two");
        m.addObservedObject(on2);
        check(m.containsObservedObject(on2), "containsObservedObject after add");
        check(m.getObservedObjects().length == 2, "two observed objects");

        m.removeObservedObject(on1);
        check(!m.containsObservedObject(on1), "removed object gone");
        check(m.containsObservedObject(on2), "remaining object present");
        check(m.getObservedObjects().length == 1, "one observed object after remove");

        m.setObservedAttribute("State");
        check("State".equals(m.getObservedAttribute()), "setObservedAttribute/getObservedAttribute");

        m.setGranularityPeriod(250L);
        check(m.getGranularityPeriod() == 250L, "setGranularityPeriod");

        // validation
        boolean e1 = false;
        try { m.setObservedObject(null); } catch (IllegalArgumentException e) { e1 = true; }
        check(e1, "setObservedObject(null) throws IllegalArgumentException");

        boolean e2 = false;
        try { m.addObservedObject(null); } catch (IllegalArgumentException e) { e2 = true; }
        check(e2, "addObservedObject(null) throws IllegalArgumentException");

        boolean e3 = false;
        try { m.setObservedAttribute(null); } catch (IllegalArgumentException e) { e3 = true; }
        check(e3, "setObservedAttribute(null) throws IllegalArgumentException");

        boolean e4 = false;
        try { m.setGranularityPeriod(0L); } catch (IllegalArgumentException e) { e4 = true; }
        check(e4, "setGranularityPeriod(0) throws IllegalArgumentException");

        boolean e5 = false;
        try { m.setGranularityPeriod(-1L); } catch (IllegalArgumentException e) { e5 = true; }
        check(e5, "setGranularityPeriod(-1) throws IllegalArgumentException");

        m.removeObservedObject(null); // no-op, must not throw
        check(true, "removeObservedObject(null) no-op");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepLifecycleRegistration();
        testDeepErrorNotificationMatrix();
        testDeepMultiObservedObjects();
        testDeepStartStopIdempotent();
        testDeepObservedObjectsSnapshot();
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

    // ==== deepened: real closed loop (batch i, 2026-11) ====

    // preRegister injects the MBeanServer reference and returns the name unchanged;
    // preDeregister stops the monitor; full MBeanRegistration ordering is recorded.
    static void testDeepLifecycleRegistration() throws Exception {
        final javax.management.MBeanServer mbs = javax.management.MBeanServerFactory.newMBeanServer();
        final javax.management.ObjectName name = new javax.management.ObjectName("jck.mon.i:type=lifecycle");
        final java.util.List<String> calls = new java.util.concurrent.CopyOnWriteArrayList<String>();
        // NB: the local is deliberately named mbs; inside the anonymous subclass the
        // simple name "server" resolves to the inherited protected field Monitor.server,
        // which would silently shadow the captured local and break the identity check.
        javax.management.monitor.CounterMonitor cm = new javax.management.monitor.CounterMonitor() {
            public javax.management.ObjectName preRegister(javax.management.MBeanServer srv, javax.management.ObjectName n) throws Exception {
                calls.add("preRegister:" + (srv == mbs));
                return super.preRegister(srv, n);
            }
            public void postRegister(Boolean done) { calls.add("postRegister:" + done); super.postRegister(done); }
            public void preDeregister() throws Exception { calls.add("preDeregister"); super.preDeregister(); }
            public void postDeregister() { calls.add("postDeregister"); super.postDeregister(); }
        };
        // holder: the anonymous subclass exposes the inherited protected field back out
        final javax.management.MBeanServer[] injected = new javax.management.MBeanServer[1];
        javax.management.monitor.CounterMonitor probe = new javax.management.monitor.CounterMonitor() {
            public void postRegister(Boolean done) { injected[0] = server; super.postRegister(done); }
        };
        mbs.registerMBean(probe, new javax.management.ObjectName("jck.mon.i:type=injProbe"));
        check(injected[0] == mbs, "preRegister stored the server into the protected Monitor.server field");
        javax.management.ObjectInstance inst = mbs.registerMBean(cm, name);
        check(name.equals(inst.getObjectName()), "registerMBean returns preRegister name");
        cm.start();
        check(cm.isActive(), "active after start");
        mbs.unregisterMBean(name);
        java.util.List<String> expected = java.util.Arrays.asList(
                "preRegister:true", "postRegister:true", "preDeregister", "postDeregister");
        check(calls.equals(expected), "MBeanRegistration callback order, got " + calls);
        check(!cm.isActive(), "preDeregister stops the monitor");
    }

    // Base-class error handling: polling failures surface as monitor error notifications
    // (never as thrown exceptions from start()); one notification per error condition.
    static void testDeepErrorNotificationMatrix() throws Exception {
        javax.management.MBeanServer server = javax.management.MBeanServerFactory.newMBeanServer();
        javax.management.ObjectName observedName = new javax.management.ObjectName("jck.mon.i:type=errObs");
        javax.management.ObjectName monitorName = new javax.management.ObjectName("jck.mon.i:type=errMon");
        server.registerMBean(new Observed(), observedName);
        javax.management.monitor.CounterMonitor cm = new javax.management.monitor.CounterMonitor();
        server.registerMBean(cm, monitorName);
        Collector col = new Collector();
        server.addNotificationListener(monitorName, col, null, null);
        cm.setGranularityPeriod(100L);

        // (a) observed attribute does not exist -> jmx.monitor.error.attribute
        cm.addObservedObject(observedName);
        cm.setObservedAttribute("NoSuchAttribute");
        cm.start();
        javax.management.Notification n = col.waitFor(
                javax.management.monitor.MonitorNotification.OBSERVED_ATTRIBUTE_ERROR, 1, 5000);
        check(n != null, "OBSERVED_ATTRIBUTE_ERROR delivered, collected=" + col.received.size());
        javax.management.monitor.MonitorNotification mn = (javax.management.monitor.MonitorNotification) n;
        check(observedName.equals(mn.getObservedObject()), "error notif observed object");
        check("NoSuchAttribute".equals(mn.getObservedAttribute()), "error notif observed attribute");
        check(mn.getDerivedGauge() == null, "attribute error notif derived gauge null");
        check(mn.getTrigger() == null, "attribute error notif trigger null");
        check("jmx.monitor.error.attribute".equals(n.getType()), "attribute error exact type string");
        check(cm.isActive(), "monitor keeps polling after attribute error");
        cm.stop();
        cm.removeObservedObject(observedName);

        // (b) observed MBean not registered -> jmx.monitor.error.mbean
        javax.management.ObjectName ghost = new javax.management.ObjectName("jck.mon.i:type=ghost");
        cm.addObservedObject(ghost);
        cm.setObservedAttribute("Count");
        cm.start();
        n = col.waitFor(javax.management.monitor.MonitorNotification.OBSERVED_OBJECT_ERROR, 1, 5000);
        check(n != null, "OBSERVED_OBJECT_ERROR delivered");
        mn = (javax.management.monitor.MonitorNotification) n;
        check(ghost.equals(mn.getObservedObject()), "object error notif observed object");
        check("jmx.monitor.error.mbean".equals(n.getType()), "object error exact type string");
        cm.stop();
        cm.removeObservedObject(ghost);

        // (c) attribute type not supported by the monitor -> jmx.monitor.error.type
        cm.addObservedObject(observedName);
        cm.setObservedAttribute("Gauge");
        cm.start();
        n = col.waitFor(javax.management.monitor.MonitorNotification.OBSERVED_ATTRIBUTE_TYPE_ERROR, 1, 5000);
        check(n != null, "OBSERVED_ATTRIBUTE_TYPE_ERROR delivered");
        check("jmx.monitor.error.type".equals(n.getType()), "type error exact type string");
        cm.stop();
        cm.removeObservedObject(observedName);

        // (d) attribute getter throws -> jmx.monitor.error.runtime with the cause text
        cm.addObservedObject(observedName);
        cm.setObservedAttribute("Broken");
        cm.start();
        n = col.waitFor(javax.management.monitor.MonitorNotification.RUNTIME_ERROR, 1, 5000);
        check(n != null, "RUNTIME_ERROR delivered");
        check("jmx.monitor.error.runtime".equals(n.getType()), "runtime error exact type string");
        check(n.getMessage() != null && n.getMessage().contains("boom"),
                "runtime error message carries cause, got: " + n.getMessage());
        cm.stop();
        cm.removeObservedObject(observedName);
    }

    // One monitor, several observed MBeans: independent derived gauges and one
    // threshold notification per observed object.
    static void testDeepMultiObservedObjects() throws Exception {
        javax.management.MBeanServer server = javax.management.MBeanServerFactory.newMBeanServer();
        javax.management.ObjectName on1 = new javax.management.ObjectName("jck.mon.i:type=multi1");
        javax.management.ObjectName on2 = new javax.management.ObjectName("jck.mon.i:type=multi2");
        javax.management.ObjectName monitorName = new javax.management.ObjectName("jck.mon.i:type=multiMon");
        server.registerMBean(new Observed(), on1);
        server.registerMBean(new Observed(), on2);
        javax.management.monitor.CounterMonitor cm = new javax.management.monitor.CounterMonitor();
        server.registerMBean(cm, monitorName);
        cm.addObservedObject(on1);
        cm.addObservedObject(on2);
        check(cm.getObservedObjects().length == 2, "two observed objects");
        check(cm.containsObservedObject(on1) && cm.containsObservedObject(on2), "contains both");
        cm.setObservedAttribute("Count");
        cm.setGranularityPeriod(100L);
        cm.setInitThreshold(5);
        cm.setNotify(true);
        Collector col = new Collector();
        server.addNotificationListener(monitorName, col, null, null);
        cm.start();
        try {
            server.setAttribute(on1, new javax.management.Attribute("Count", 6));
            server.setAttribute(on2, new javax.management.Attribute("Count", 7));
            javax.management.Notification n = col.waitFor(
                    javax.management.monitor.MonitorNotification.THRESHOLD_VALUE_EXCEEDED, 2, 5000);
            check(n != null, "both threshold notifications delivered");
            javax.management.monitor.MonitorNotification first = (javax.management.monitor.MonitorNotification)
                    col.nth(javax.management.monitor.MonitorNotification.THRESHOLD_VALUE_EXCEEDED, 0);
            javax.management.monitor.MonitorNotification second = (javax.management.monitor.MonitorNotification)
                    col.nth(javax.management.monitor.MonitorNotification.THRESHOLD_VALUE_EXCEEDED, 1);
            check(first != null && second != null, "two distinct notifications");
            check(!first.getObservedObject().equals(second.getObservedObject()),
                    "notifications distinguish observed objects");
            check(((Number) cm.getDerivedGauge(on1)).intValue() == 6, "derived gauge object 1");
            check(((Number) cm.getDerivedGauge(on2)).intValue() == 7, "derived gauge object 2");
        } finally {
            cm.stop();
        }
    }

    // start()/stop() are idempotent; a monitor can be restarted after stop.
    static void testDeepStartStopIdempotent() throws Exception {
        javax.management.monitor.CounterMonitor cm = new javax.management.monitor.CounterMonitor();
        cm.start();
        cm.start();
        check(cm.isActive(), "start twice keeps monitor active, no exception");
        cm.stop();
        cm.stop();
        check(!cm.isActive(), "stop twice keeps monitor inactive, no exception");
        cm.start();
        check(cm.isActive(), "restart after stop works");
        cm.stop();
        check(!cm.isActive(), "stopped again");
    }

    // getObservedObjects() returns a snapshot copy: mutating it does not affect the monitor.
    @SuppressWarnings("deprecation")
    static void testDeepObservedObjectsSnapshot() throws Exception {
        javax.management.monitor.CounterMonitor cm = new javax.management.monitor.CounterMonitor();
        javax.management.ObjectName on1 = new javax.management.ObjectName("jck.mon.i:type=snap1");
        javax.management.ObjectName on2 = new javax.management.ObjectName("jck.mon.i:type=snap2");
        cm.setObservedObject(on1);
        cm.addObservedObject(on2);
        javax.management.ObjectName[] arr = cm.getObservedObjects();
        check(arr.length == 2, "snapshot length 2");
        arr[0] = null;
        check(cm.getObservedObjects().length == 2, "snapshot copy: mutation not visible");
        check(on1.equals(cm.getObservedObject()), "deprecated getObservedObject returns first");
        cm.addObservedObject(on1);
        check(cm.getObservedObjects().length == 2, "duplicate addObservedObject no-op");
        javax.management.ObjectName on3 = new javax.management.ObjectName("jck.mon.i:type=snap3");
        cm.setObservedObject(on3);
        check(cm.getObservedObjects().length == 1 && on3.equals(cm.getObservedObject()),
                "setObservedObject replaces the whole set");
        cm.removeObservedObject(null);
        check(cm.getObservedObjects().length == 1, "removeObservedObject(null) is a no-op");
    }
}
