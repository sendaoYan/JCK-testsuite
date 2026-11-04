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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
