/*
 * @test
 * @summary javax.management.monitor.CounterMonitor conformance: white-box structure + black-box behavior.
 * @run main CounterMonitorTest
 */

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.management.MBeanNotificationInfo;
import javax.management.ObjectName;
import javax.management.monitor.CounterMonitor;
import javax.management.monitor.CounterMonitorMBean;
import javax.management.monitor.MonitorNotification;

@SuppressWarnings("deprecation")
public class CounterMonitorTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.monitor.CounterMonitor");
        check(!c.isInterface(), "not interface");
        check(javax.management.monitor.Monitor.class.isAssignableFrom(c), "extends Monitor");
        check(CounterMonitorMBean.class.isAssignableFrom(c), "implements CounterMonitorMBean");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        CounterMonitor cm = new CounterMonitor();
        check(!cm.isActive(), "initially inactive");
        check(cm.getInitThreshold().intValue() == 0, "default init threshold 0");
        check(cm.getOffset().intValue() == 0, "default offset 0");
        check(cm.getModulus().intValue() == 0, "default modulus 0");
        check(!cm.getNotify(), "default notify false");
        check(!cm.getDifferenceMode(), "default difference mode false");
        check(cm.getDerivedGauge() == null, "no derived gauge without observed object");
        check(cm.getDerivedGaugeTimeStamp() == 0L, "derived gauge timestamp 0");

        cm.setInitThreshold(5);
        check(cm.getInitThreshold().intValue() == 5, "setInitThreshold/getInitThreshold");
        cm.setThreshold(8); // deprecated alias of setInitThreshold
        check(cm.getInitThreshold().intValue() == 8, "setThreshold aliases setInitThreshold");
        cm.setOffset(2);
        check(cm.getOffset().intValue() == 2, "setOffset/getOffset");
        cm.setModulus(100);
        check(cm.getModulus().intValue() == 100, "setModulus/getModulus");
        cm.setNotify(true);
        check(cm.getNotify(), "setNotify/getNotify");
        cm.setDifferenceMode(true);
        check(cm.getDifferenceMode(), "setDifferenceMode/getDifferenceMode");

        // inherited Monitor behavior
        ObjectName on = new ObjectName("test:type=counter");
        cm.setObservedObject(on);
        check(cm.containsObservedObject(on), "containsObservedObject");
        cm.setObservedAttribute("Count");
        check("Count".equals(cm.getObservedAttribute()), "observed attribute");
        cm.setGranularityPeriod(500L);
        check(cm.getGranularityPeriod() == 500L, "granularity period");

        // notification info
        MBeanNotificationInfo[] infos = cm.getNotificationInfo();
        check(infos.length > 0, "notification info non-empty");
        Set<String> types = new HashSet<>();
        for (MBeanNotificationInfo i : infos)
            types.addAll(Arrays.asList(i.getNotifTypes()));
        check(types.contains(MonitorNotification.THRESHOLD_VALUE_EXCEEDED),
                "notification info includes THRESHOLD_VALUE_EXCEEDED");

        // validation
        boolean e1 = false;
        try { cm.setInitThreshold(null); } catch (IllegalArgumentException e) { e1 = true; }
        check(e1, "setInitThreshold(null) throws IllegalArgumentException");
        boolean e2 = false;
        try { cm.setInitThreshold(-1); } catch (IllegalArgumentException e) { e2 = true; }
        check(e2, "setInitThreshold(negative) throws IllegalArgumentException");
        boolean e3 = false;
        try { cm.setOffset(null); } catch (IllegalArgumentException e) { e3 = true; }
        check(e3, "setOffset(null) throws IllegalArgumentException");
        boolean e4 = false;
        try { cm.setModulus(-1); } catch (IllegalArgumentException e) { e4 = true; }
        check(e4, "setModulus(negative) throws IllegalArgumentException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
