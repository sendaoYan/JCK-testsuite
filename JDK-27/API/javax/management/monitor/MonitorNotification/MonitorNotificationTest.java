/*
 * @test
 * @summary javax.management.monitor.MonitorNotification conformance: white-box structure + black-box behavior.
 * @run main MonitorNotificationTest
 */

import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.monitor.CounterMonitor;
import javax.management.monitor.MonitorNotification;

@SuppressWarnings("deprecation")
public class MonitorNotificationTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean has(Method[] mm, String n) {
        for (Method m : mm) if (m.getName().equals(n)) return true;
        return false;
    }

    // Minimal DynamicMBean exposing a single read-only Integer attribute.
    static class CounterBean implements DynamicMBean {
        public Object getAttribute(String attribute) throws AttributeNotFoundException {
            if ("Count".equals(attribute)) return Integer.valueOf(10);
            throw new AttributeNotFoundException(attribute);
        }
        public AttributeList getAttributes(String[] attributes) {
            AttributeList list = new AttributeList();
            for (String a : attributes) {
                try { list.add(new Attribute(a, getAttribute(a))); } catch (Exception ignored) {}
            }
            return list;
        }
        public MBeanInfo getMBeanInfo() {
            MBeanAttributeInfo ai = new MBeanAttributeInfo(
                    "Count", "java.lang.Integer", "count", true, false, false);
            return new MBeanInfo(CounterBean.class.getName(), "counter",
                    new MBeanAttributeInfo[]{ai}, null, null, null);
        }
        public Object invoke(String action, Object[] params, String[] signature)
                throws javax.management.ReflectionException {
            throw new javax.management.ReflectionException(new UnsupportedOperationException());
        }
        public void setAttribute(Attribute attribute) throws AttributeNotFoundException {
            throw new AttributeNotFoundException(attribute.getName());
        }
        public AttributeList setAttributes(AttributeList attributes) {
            return new AttributeList();
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.monitor.MonitorNotification");
        check(Notification.class.isAssignableFrom(c), "extends Notification");
        check("jmx.monitor.error.mbean".equals(MonitorNotification.OBSERVED_OBJECT_ERROR), "OBSERVED_OBJECT_ERROR");
        check("jmx.monitor.error.attribute".equals(MonitorNotification.OBSERVED_ATTRIBUTE_ERROR), "OBSERVED_ATTRIBUTE_ERROR");
        check("jmx.monitor.error.type".equals(MonitorNotification.OBSERVED_ATTRIBUTE_TYPE_ERROR), "OBSERVED_ATTRIBUTE_TYPE_ERROR");
        check("jmx.monitor.error.threshold".equals(MonitorNotification.THRESHOLD_ERROR), "THRESHOLD_ERROR");
        check("jmx.monitor.error.runtime".equals(MonitorNotification.RUNTIME_ERROR), "RUNTIME_ERROR");
        check("jmx.monitor.counter.threshold".equals(MonitorNotification.THRESHOLD_VALUE_EXCEEDED), "THRESHOLD_VALUE_EXCEEDED");
        check("jmx.monitor.gauge.high".equals(MonitorNotification.THRESHOLD_HIGH_VALUE_EXCEEDED), "THRESHOLD_HIGH_VALUE_EXCEEDED");
        check("jmx.monitor.gauge.low".equals(MonitorNotification.THRESHOLD_LOW_VALUE_EXCEEDED), "THRESHOLD_LOW_VALUE_EXCEEDED");
        check("jmx.monitor.string.matches".equals(MonitorNotification.STRING_TO_COMPARE_VALUE_MATCHED), "STRING_TO_COMPARE_VALUE_MATCHED");
        check("jmx.monitor.string.differs".equals(MonitorNotification.STRING_TO_COMPARE_VALUE_DIFFERED), "STRING_TO_COMPARE_VALUE_DIFFERED");
        Method[] mm = c.getMethods();
        check(has(mm, "getObservedObject"), "has getObservedObject");
        check(has(mm, "getObservedAttribute"), "has getObservedAttribute");
        check(has(mm, "getDerivedGauge"), "has getDerivedGauge");
        check(has(mm, "getTrigger"), "has getTrigger");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // The MonitorNotification constructor is package-private; obtain a real
        // instance through a short-lived CounterMonitor (daemon scheduler thread).
        MBeanServer mbs = MBeanServerFactory.createMBeanServer();
        CounterMonitor cm = new CounterMonitor();
        ObjectName observedName = new ObjectName("test:type=CounterBean");
        ObjectName monitorName = new ObjectName("test:type=CounterMonitor");

        final CountDownLatch latch = new CountDownLatch(1);
        final MonitorNotification[] captured = new MonitorNotification[1];

        try {
            mbs.registerMBean(new CounterBean(), observedName);
            mbs.registerMBean(cm, monitorName);

            cm.addNotificationListener(new NotificationListener() {
                public void handleNotification(Notification n, Object handback) {
                    if (n instanceof MonitorNotification) {
                        captured[0] = (MonitorNotification) n;
                        latch.countDown();
                    }
                }
            }, null, null);

            cm.setObservedObject(observedName);
            cm.setObservedAttribute("Count");
            cm.setInitThreshold(5);
            cm.setNotify(true);
            cm.setDifferenceMode(false);
            cm.setGranularityPeriod(10);

            cm.start();
            boolean got = latch.await(30, TimeUnit.SECONDS);
            check(got, "received a MonitorNotification within timeout");
            check(captured[0] != null, "captured notification non-null");

            MonitorNotification mn = captured[0];
            check("jmx.monitor.counter.threshold".equals(mn.getType()), "getType");
            check(observedName.equals(mn.getObservedObject()), "getObservedObject");
            check("Count".equals(mn.getObservedAttribute()), "getObservedAttribute");
            check(mn.getDerivedGauge() instanceof Number &&
                    ((Number) mn.getDerivedGauge()).intValue() == 10, "getDerivedGauge");
            check(mn.getTrigger() instanceof Number &&
                    ((Number) mn.getTrigger()).intValue() == 5, "getTrigger");
            check(mn.toString().contains("jmx.monitor.counter.threshold"), "toString contains type");
        } finally {
            try { cm.stop(); } catch (Exception ignored) {}
            try { mbs.unregisterMBean(monitorName); } catch (Exception ignored) {}
            try { mbs.unregisterMBean(observedName); } catch (Exception ignored) {}
            MBeanServerFactory.releaseMBeanServer(mbs);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepConstantValues();
        testDeepCounterNotificationGetters();
        testDeepErrorNotificationGetters();
        testDeepWhiteBoxContract();
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

    // ==== deepened: constant matrix + real notification getters + white box (batch i, 2026-11) ====

    static void testDeepConstantValues() throws Exception {
        check("jmx.monitor.error.mbean".equals(javax.management.monitor.MonitorNotification.OBSERVED_OBJECT_ERROR),
                "OBSERVED_OBJECT_ERROR value");
        check("jmx.monitor.error.attribute".equals(javax.management.monitor.MonitorNotification.OBSERVED_ATTRIBUTE_ERROR),
                "OBSERVED_ATTRIBUTE_ERROR value");
        check("jmx.monitor.error.type".equals(javax.management.monitor.MonitorNotification.OBSERVED_ATTRIBUTE_TYPE_ERROR),
                "OBSERVED_ATTRIBUTE_TYPE_ERROR value");
        check("jmx.monitor.error.threshold".equals(javax.management.monitor.MonitorNotification.THRESHOLD_ERROR),
                "THRESHOLD_ERROR value");
        check("jmx.monitor.error.runtime".equals(javax.management.monitor.MonitorNotification.RUNTIME_ERROR),
                "RUNTIME_ERROR value");
        check("jmx.monitor.counter.threshold".equals(javax.management.monitor.MonitorNotification.THRESHOLD_VALUE_EXCEEDED),
                "THRESHOLD_VALUE_EXCEEDED value");
        check("jmx.monitor.gauge.high".equals(javax.management.monitor.MonitorNotification.THRESHOLD_HIGH_VALUE_EXCEEDED),
                "THRESHOLD_HIGH_VALUE_EXCEEDED value");
        check("jmx.monitor.gauge.low".equals(javax.management.monitor.MonitorNotification.THRESHOLD_LOW_VALUE_EXCEEDED),
                "THRESHOLD_LOW_VALUE_EXCEEDED value");
        check("jmx.monitor.string.matches".equals(javax.management.monitor.MonitorNotification.STRING_TO_COMPARE_VALUE_MATCHED),
                "STRING_TO_COMPARE_VALUE_MATCHED value");
        check("jmx.monitor.string.differs".equals(javax.management.monitor.MonitorNotification.STRING_TO_COMPARE_VALUE_DIFFERED),
                "STRING_TO_COMPARE_VALUE_DIFFERED value");
    }

    // Real threshold notification carries the full observable contract of the getters.
    static void testDeepCounterNotificationGetters() throws Exception {
        javax.management.MBeanServer server = javax.management.MBeanServerFactory.newMBeanServer();
        javax.management.ObjectName observedName = new javax.management.ObjectName("jck.mn.i:type=obs");
        javax.management.ObjectName monitorName = new javax.management.ObjectName("jck.mn.i:type=mon");
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
            javax.management.monitor.MonitorNotification mn = (javax.management.monitor.MonitorNotification) n;
            check(n instanceof javax.management.monitor.MonitorNotification, "instanceof MonitorNotification");
            check(observedName.equals(mn.getObservedObject()), "getObservedObject");
            check("Count".equals(mn.getObservedAttribute()), "getObservedAttribute");
            check(mn.getDerivedGauge() instanceof Integer && ((Integer) mn.getDerivedGauge()).intValue() == 6,
                    "getDerivedGauge is the observed value at crossing");
            check(mn.getTrigger() instanceof Integer && ((Integer) mn.getTrigger()).intValue() == 5,
                    "getTrigger is the exceeded threshold");
            check(n.getSequenceNumber() == 0L, "first monitor notification has sequence number 0");
            check(n.getTimeStamp() > 0L, "notification time stamp positive");
            check(monitorName.equals(n.getSource()), "source replaced by the monitor ObjectName");
            check(n.toString().contains("jmx.monitor.counter.threshold"), "toString contains type");
        } finally {
            cm.stop();
        }
    }

    // Error notifications expose the same getters with null gauge/trigger payload.
    static void testDeepErrorNotificationGetters() throws Exception {
        javax.management.MBeanServer server = javax.management.MBeanServerFactory.newMBeanServer();
        javax.management.ObjectName observedName = new javax.management.ObjectName("jck.mn.i:type=errObs");
        javax.management.ObjectName monitorName = new javax.management.ObjectName("jck.mn.i:type=errMon");
        server.registerMBean(new Observed(), observedName);
        javax.management.monitor.CounterMonitor cm = new javax.management.monitor.CounterMonitor();
        server.registerMBean(cm, monitorName);
        Collector col = new Collector();
        server.addNotificationListener(monitorName, col, null, null);
        cm.addObservedObject(observedName);
        cm.setObservedAttribute("NoSuchAttribute");
        cm.setGranularityPeriod(100L);
        cm.start();
        try {
            javax.management.Notification n = col.waitFor(
                    javax.management.monitor.MonitorNotification.OBSERVED_ATTRIBUTE_ERROR, 1, 5000);
            check(n != null, "error notification delivered");
            javax.management.monitor.MonitorNotification mn = (javax.management.monitor.MonitorNotification) n;
            check(observedName.equals(mn.getObservedObject()), "error notif getObservedObject");
            check("NoSuchAttribute".equals(mn.getObservedAttribute()), "error notif getObservedAttribute");
            check(mn.getDerivedGauge() == null, "error notif getDerivedGauge null");
            check(mn.getTrigger() == null, "error notif getTrigger null");
        } finally {
            cm.stop();
        }
    }

    // White box: package-private single constructor with 9 parameters; stable serialVersionUID.
    static void testDeepWhiteBoxContract() throws Exception {
        Class<?> c = Class.forName("javax.management.monitor.MonitorNotification");
        check(javax.management.Notification.class.isAssignableFrom(c), "extends Notification");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
        java.lang.reflect.Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 1, "single declared constructor");
        check(!java.lang.reflect.Modifier.isPublic(ctors[0].getModifiers())
                && !java.lang.reflect.Modifier.isProtected(ctors[0].getModifiers())
                && !java.lang.reflect.Modifier.isPrivate(ctors[0].getModifiers()),
                "constructor is package-private (not public/protected/private)");
        check(ctors[0].getParameterTypes().length == 9, "constructor takes 9 parameters");
        check(java.io.ObjectStreamClass.lookup(c) != null
                && java.io.ObjectStreamClass.lookup(c).getSerialVersionUID() == -4608189663661929204L,
                "serialVersionUID locked");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getDeclaringClass() == c) names.add(m.getName());
        check(names.contains("getObservedObject") && names.contains("getObservedAttribute")
                && names.contains("getDerivedGauge") && names.contains("getTrigger"),
                "declares exactly the four domain getters, got " + names);
    }
}
