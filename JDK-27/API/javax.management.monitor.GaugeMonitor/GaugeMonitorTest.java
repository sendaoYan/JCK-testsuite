/*
 * @test
 * @summary javax.management.monitor.GaugeMonitor conformance: white-box structure + black-box behavior.
 * @run main GaugeMonitorTest
 */

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.management.MBeanNotificationInfo;
import javax.management.ObjectName;
import javax.management.monitor.GaugeMonitor;
import javax.management.monitor.GaugeMonitorMBean;
import javax.management.monitor.MonitorNotification;

@SuppressWarnings("deprecation")
public class GaugeMonitorTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.monitor.GaugeMonitor");
        check(!c.isInterface(), "not interface");
        check(javax.management.monitor.Monitor.class.isAssignableFrom(c), "extends Monitor");
        check(GaugeMonitorMBean.class.isAssignableFrom(c), "implements GaugeMonitorMBean");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        GaugeMonitor gm = new GaugeMonitor();
        check(gm.getHighThreshold().intValue() == 0, "default high threshold 0");
        check(gm.getLowThreshold().intValue() == 0, "default low threshold 0");
        check(!gm.getNotifyHigh(), "default notify high false");
        check(!gm.getNotifyLow(), "default notify low false");
        check(!gm.getDifferenceMode(), "default difference mode false");
        check(gm.getDerivedGauge() == null, "no derived gauge without observed object");
        check(gm.getDerivedGaugeTimeStamp() == 0L, "derived gauge timestamp 0");

        gm.setThresholds(100, 10);
        check(gm.getHighThreshold().intValue() == 100, "setThresholds high");
        check(gm.getLowThreshold().intValue() == 10, "setThresholds low");
        gm.setNotifyHigh(true);
        gm.setNotifyLow(true);
        check(gm.getNotifyHigh() && gm.getNotifyLow(), "notify flags");
        gm.setDifferenceMode(true);
        check(gm.getDifferenceMode(), "difference mode");

        ObjectName on = new ObjectName("test:type=gauge");
        gm.setObservedObject(on);
        gm.setObservedAttribute("Level");
        check("Level".equals(gm.getObservedAttribute()), "observed attribute");
        gm.setGranularityPeriod(700L);
        check(gm.getGranularityPeriod() == 700L, "granularity period");

        MBeanNotificationInfo[] infos = gm.getNotificationInfo();
        check(infos.length > 0, "notification info non-empty");
        Set<String> types = new HashSet<>();
        for (MBeanNotificationInfo i : infos)
            types.addAll(Arrays.asList(i.getNotifTypes()));
        check(types.contains(MonitorNotification.THRESHOLD_HIGH_VALUE_EXCEEDED),
                "includes THRESHOLD_HIGH_VALUE_EXCEEDED");
        check(types.contains(MonitorNotification.THRESHOLD_LOW_VALUE_EXCEEDED),
                "includes THRESHOLD_LOW_VALUE_EXCEEDED");

        // validation
        boolean e1 = false;
        try { gm.setThresholds(null, 0); } catch (IllegalArgumentException e) { e1 = true; }
        check(e1, "setThresholds(null,..) throws IllegalArgumentException");
        boolean e2 = false;
        try { gm.setThresholds(100, 200); } catch (IllegalArgumentException e) { e2 = true; }
        check(e2, "setThresholds(high<low) throws IllegalArgumentException");
        boolean e3 = false;
        try { gm.setThresholds(100L, 0); } catch (IllegalArgumentException e) { e3 = true; }
        check(e3, "setThresholds(different types) throws IllegalArgumentException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
