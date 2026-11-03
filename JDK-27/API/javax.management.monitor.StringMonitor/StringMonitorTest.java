/*
 * @test
 * @summary javax.management.monitor.StringMonitor conformance: white-box structure + black-box behavior.
 * @run main StringMonitorTest
 */

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.management.MBeanNotificationInfo;
import javax.management.ObjectName;
import javax.management.monitor.MonitorNotification;
import javax.management.monitor.StringMonitor;
import javax.management.monitor.StringMonitorMBean;

@SuppressWarnings("deprecation")
public class StringMonitorTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.monitor.StringMonitor");
        check(!c.isInterface(), "not interface");
        check(javax.management.monitor.Monitor.class.isAssignableFrom(c), "extends Monitor");
        check(StringMonitorMBean.class.isAssignableFrom(c), "implements StringMonitorMBean");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        StringMonitor sm = new StringMonitor();
        check("".equals(sm.getStringToCompare()), "default string to compare empty");
        check(!sm.getNotifyMatch(), "default notify match false");
        check(!sm.getNotifyDiffer(), "default notify differ false");
        check(sm.getDerivedGauge() == null, "no derived gauge without observed object");
        check(sm.getDerivedGaugeTimeStamp() == 0L, "derived gauge timestamp 0");

        sm.setStringToCompare("hello");
        check("hello".equals(sm.getStringToCompare()), "setStringToCompare/getStringToCompare");
        sm.setNotifyMatch(true);
        sm.setNotifyDiffer(true);
        check(sm.getNotifyMatch() && sm.getNotifyDiffer(), "notify flags");

        ObjectName on = new ObjectName("test:type=string");
        sm.setObservedObject(on);
        sm.setObservedAttribute("Message");
        check("Message".equals(sm.getObservedAttribute()), "observed attribute");
        sm.setGranularityPeriod(900L);
        check(sm.getGranularityPeriod() == 900L, "granularity period");

        MBeanNotificationInfo[] infos = sm.getNotificationInfo();
        check(infos.length > 0, "notification info non-empty");
        Set<String> types = new HashSet<>();
        for (MBeanNotificationInfo i : infos)
            types.addAll(Arrays.asList(i.getNotifTypes()));
        check(types.contains(MonitorNotification.STRING_TO_COMPARE_VALUE_MATCHED),
                "includes STRING_TO_COMPARE_VALUE_MATCHED");
        check(types.contains(MonitorNotification.STRING_TO_COMPARE_VALUE_DIFFERED),
                "includes STRING_TO_COMPARE_VALUE_DIFFERED");

        boolean e1 = false;
        try { sm.setStringToCompare(null); } catch (IllegalArgumentException e) { e1 = true; }
        check(e1, "setStringToCompare(null) throws IllegalArgumentException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
