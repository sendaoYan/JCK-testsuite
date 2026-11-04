/*
 * @test
 * @summary javax.management.monitor.MonitorMBean conformance: interface reflection.
 * @run main MonitorMBeanTest
 */

public class MonitorMBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.monitor.MonitorMBean");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(hasMethod(c, "addObservedObject"), "declares addObservedObject");
        check(hasMethod(c, "containsObservedObject"), "declares containsObservedObject");
        check(hasMethod(c, "getGranularityPeriod"), "declares getGranularityPeriod");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
