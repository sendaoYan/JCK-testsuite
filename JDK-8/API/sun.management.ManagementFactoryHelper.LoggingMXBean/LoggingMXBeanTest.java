/*
 * @test
 * @summary sun.management.ManagementFactoryHelper.LoggingMXBean conformance: interface and members.
 * @run main LoggingMXBeanTest
 */

public class LoggingMXBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.management.ManagementFactoryHelper$LoggingMXBean", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.management.ManagementFactoryHelper$LoggingMXBean"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("java.lang.management.PlatformLoggingMXBean"), "interface java.lang.management.PlatformLoggingMXBean");
        check(ifaces.contains("java.util.logging.LoggingMXBean"), "interface java.util.logging.LoggingMXBean");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
