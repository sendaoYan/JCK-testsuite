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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
