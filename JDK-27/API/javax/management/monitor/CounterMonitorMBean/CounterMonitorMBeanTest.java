/*
 * @test
 * @summary javax.management.monitor.CounterMonitorMBean conformance: white-box structure + black-box behavior.
 * @run main CounterMonitorMBeanTest
 */

import java.lang.reflect.Method;
import javax.management.monitor.CounterMonitor;
import javax.management.monitor.CounterMonitorMBean;
import javax.management.monitor.MonitorMBean;

public class CounterMonitorMBeanTest {
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
        Class<?> c = Class.forName("javax.management.monitor.CounterMonitorMBean");
        check(c.isInterface(), "is interface");
        check(MonitorMBean.class.isAssignableFrom(c), "extends MonitorMBean");
        Method[] mm = c.getMethods();
        check(has(mm, "getDerivedGauge"), "has getDerivedGauge");
        check(has(mm, "getDerivedGaugeTimeStamp"), "has getDerivedGaugeTimeStamp");
        check(has(mm, "getThreshold"), "has getThreshold");
        check(has(mm, "setThreshold"), "has setThreshold");
        check(has(mm, "getInitThreshold"), "has getInitThreshold");
        check(has(mm, "setInitThreshold"), "has setInitThreshold");
        check(has(mm, "getOffset"), "has getOffset");
        check(has(mm, "setOffset"), "has setOffset");
        check(has(mm, "getModulus"), "has getModulus");
        check(has(mm, "setModulus"), "has setModulus");
        check(has(mm, "getNotify"), "has getNotify");
        check(has(mm, "setNotify"), "has setNotify");
        check(has(mm, "getDifferenceMode"), "has getDifferenceMode");
        check(has(mm, "setDifferenceMode"), "has setDifferenceMode");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        CounterMonitorMBean m = new CounterMonitor();
        m.setInitThreshold(7);
        check(m.getInitThreshold().intValue() == 7, "interface setInitThreshold/getInitThreshold");
        m.setOffset(3);
        check(m.getOffset().intValue() == 3, "interface setOffset/getOffset");
        m.setNotify(true);
        check(m.getNotify(), "interface setNotify/getNotify");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
