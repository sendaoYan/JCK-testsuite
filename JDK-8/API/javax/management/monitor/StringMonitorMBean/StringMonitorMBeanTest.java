/*
 * @test
 * @summary javax.management.monitor.StringMonitorMBean conformance: interface reflection.
 * @run main StringMonitorMBeanTest
 */

public class StringMonitorMBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.monitor.StringMonitorMBean");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(Class.forName("javax.management.monitor.MonitorMBean").isAssignableFrom(c), "implements/extends javax.management.monitor.MonitorMBean");
        check(hasMethod(c, "getDerivedGauge"), "declares getDerivedGauge");
        check(hasMethod(c, "getDerivedGaugeTimeStamp"), "declares getDerivedGaugeTimeStamp");
        check(hasMethod(c, "getNotifyDiffer"), "declares getNotifyDiffer");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
