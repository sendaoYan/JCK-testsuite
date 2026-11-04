/*
 * @test
 * @summary javax.management.monitor.GaugeMonitor conformance: concrete class reflection.
 * @run main GaugeMonitorTest
 */

public class GaugeMonitorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.monitor.GaugeMonitor");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("javax.management.monitor.Monitor".equals(c.getSuperclass().getName()), "superclass javax.management.monitor.Monitor");
        check(Class.forName("javax.management.monitor.GaugeMonitorMBean").isAssignableFrom(c), "implements/extends javax.management.monitor.GaugeMonitorMBean");
        check(hasMethod(c, "getDerivedGauge"), "declares getDerivedGauge");
        check(hasMethod(c, "getDerivedGaugeTimeStamp"), "declares getDerivedGaugeTimeStamp");
        check(hasMethod(c, "getDifferenceMode"), "declares getDifferenceMode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
