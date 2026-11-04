/*
 * @test
 * @summary javax.management.monitor.StringMonitor conformance: concrete class reflection.
 * @run main StringMonitorTest
 */

public class StringMonitorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.monitor.StringMonitor");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("javax.management.monitor.Monitor".equals(c.getSuperclass().getName()), "superclass javax.management.monitor.Monitor");
        check(Class.forName("javax.management.monitor.StringMonitorMBean").isAssignableFrom(c), "implements/extends javax.management.monitor.StringMonitorMBean");
        check(hasMethod(c, "getDerivedGauge"), "declares getDerivedGauge");
        check(hasMethod(c, "getDerivedGaugeTimeStamp"), "declares getDerivedGaugeTimeStamp");
        check(hasMethod(c, "getNotificationInfo"), "declares getNotificationInfo");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
