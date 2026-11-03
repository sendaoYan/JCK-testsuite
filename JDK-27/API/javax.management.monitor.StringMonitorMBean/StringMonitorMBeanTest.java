/*
 * @test
 * @summary javax.management.monitor.StringMonitorMBean conformance: white-box structure + black-box behavior.
 * @run main StringMonitorMBeanTest
 */

import java.lang.reflect.Method;
import javax.management.monitor.MonitorMBean;
import javax.management.monitor.StringMonitor;
import javax.management.monitor.StringMonitorMBean;

public class StringMonitorMBeanTest {
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
        Class<?> c = Class.forName("javax.management.monitor.StringMonitorMBean");
        check(c.isInterface(), "is interface");
        check(MonitorMBean.class.isAssignableFrom(c), "extends MonitorMBean");
        Method[] mm = c.getMethods();
        check(has(mm, "getDerivedGauge"), "has getDerivedGauge");
        check(has(mm, "getDerivedGaugeTimeStamp"), "has getDerivedGaugeTimeStamp");
        check(has(mm, "getStringToCompare"), "has getStringToCompare");
        check(has(mm, "setStringToCompare"), "has setStringToCompare");
        check(has(mm, "getNotifyMatch"), "has getNotifyMatch");
        check(has(mm, "setNotifyMatch"), "has setNotifyMatch");
        check(has(mm, "getNotifyDiffer"), "has getNotifyDiffer");
        check(has(mm, "setNotifyDiffer"), "has setNotifyDiffer");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        StringMonitorMBean m = new StringMonitor();
        m.setStringToCompare("abc");
        check("abc".equals(m.getStringToCompare()), "interface setStringToCompare/getStringToCompare");
        m.setNotifyMatch(true);
        m.setNotifyDiffer(true);
        check(m.getNotifyMatch() && m.getNotifyDiffer(), "interface notify flags");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
