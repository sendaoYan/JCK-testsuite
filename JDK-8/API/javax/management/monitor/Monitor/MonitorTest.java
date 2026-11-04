/*
 * @test
 * @summary javax.management.monitor.Monitor conformance: abstract class reflection.
 * @run main MonitorTest
 */

public class MonitorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.monitor.Monitor");
        check(!c.isInterface(), "not an interface");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check("javax.management.NotificationBroadcasterSupport".equals(c.getSuperclass().getName()), "superclass javax.management.NotificationBroadcasterSupport");
        check(Class.forName("javax.management.monitor.MonitorMBean").isAssignableFrom(c), "implements/extends javax.management.monitor.MonitorMBean");
        check(Class.forName("javax.management.MBeanRegistration").isAssignableFrom(c), "implements/extends javax.management.MBeanRegistration");
        check(hasMethod(c, "addObservedObject"), "declares addObservedObject");
        check(hasMethod(c, "containsObservedObject"), "declares containsObservedObject");
        check(hasMethod(c, "getGranularityPeriod"), "declares getGranularityPeriod");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
