/*
 * @test
 * @summary javax.management.monitor.MonitorMBean conformance: white-box structure + black-box behavior.
 * @run main MonitorMBeanTest
 */

import java.lang.reflect.Method;
import javax.management.ObjectName;
import javax.management.monitor.CounterMonitor;
import javax.management.monitor.MonitorMBean;

@SuppressWarnings("deprecation")
public class MonitorMBeanTest {
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
        Class<?> c = Class.forName("javax.management.monitor.MonitorMBean");
        check(c.isInterface(), "is interface");
        Method[] mm = c.getMethods();
        check(has(mm, "start"), "has start");
        check(has(mm, "stop"), "has stop");
        check(has(mm, "addObservedObject"), "has addObservedObject");
        check(has(mm, "removeObservedObject"), "has removeObservedObject");
        check(has(mm, "containsObservedObject"), "has containsObservedObject");
        check(has(mm, "getObservedObjects"), "has getObservedObjects");
        check(has(mm, "getObservedObject"), "has getObservedObject");
        check(has(mm, "setObservedObject"), "has setObservedObject");
        check(has(mm, "getObservedAttribute"), "has getObservedAttribute");
        check(has(mm, "setObservedAttribute"), "has setObservedAttribute");
        check(has(mm, "getGranularityPeriod"), "has getGranularityPeriod");
        check(has(mm, "setGranularityPeriod"), "has setGranularityPeriod");
        check(has(mm, "isActive"), "has isActive");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        MonitorMBean m = new CounterMonitor();
        ObjectName on = new ObjectName("test:type=monitormbean");
        m.setObservedObject(on);
        check(m.containsObservedObject(on), "interface containsObservedObject");
        check(on.equals(m.getObservedObject()), "interface getObservedObject");
        m.setObservedAttribute("Value");
        check("Value".equals(m.getObservedAttribute()), "interface getObservedAttribute");
        m.setGranularityPeriod(300L);
        check(m.getGranularityPeriod() == 300L, "interface granularity period");
        check(!m.isActive(), "interface isActive false before start");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
