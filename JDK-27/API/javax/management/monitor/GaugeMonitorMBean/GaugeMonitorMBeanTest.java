/*
 * @test
 * @summary javax.management.monitor.GaugeMonitorMBean conformance: white-box structure + black-box behavior.
 * @run main GaugeMonitorMBeanTest
 */

import java.lang.reflect.Method;
import javax.management.monitor.GaugeMonitor;
import javax.management.monitor.GaugeMonitorMBean;
import javax.management.monitor.MonitorMBean;

public class GaugeMonitorMBeanTest {
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
        Class<?> c = Class.forName("javax.management.monitor.GaugeMonitorMBean");
        check(c.isInterface(), "is interface");
        check(MonitorMBean.class.isAssignableFrom(c), "extends MonitorMBean");
        Method[] mm = c.getMethods();
        check(has(mm, "getDerivedGauge"), "has getDerivedGauge");
        check(has(mm, "getDerivedGaugeTimeStamp"), "has getDerivedGaugeTimeStamp");
        check(has(mm, "getHighThreshold"), "has getHighThreshold");
        check(has(mm, "getLowThreshold"), "has getLowThreshold");
        check(has(mm, "setThresholds"), "has setThresholds");
        check(has(mm, "getNotifyHigh"), "has getNotifyHigh");
        check(has(mm, "setNotifyHigh"), "has setNotifyHigh");
        check(has(mm, "getNotifyLow"), "has getNotifyLow");
        check(has(mm, "setNotifyLow"), "has setNotifyLow");
        check(has(mm, "getDifferenceMode"), "has getDifferenceMode");
        check(has(mm, "setDifferenceMode"), "has setDifferenceMode");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        GaugeMonitorMBean m = new GaugeMonitor();
        m.setThresholds(50, 20);
        check(m.getHighThreshold().intValue() == 50, "interface getHighThreshold");
        check(m.getLowThreshold().intValue() == 20, "interface getLowThreshold");
        m.setNotifyHigh(true);
        check(m.getNotifyHigh(), "interface setNotifyHigh/getNotifyHigh");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
