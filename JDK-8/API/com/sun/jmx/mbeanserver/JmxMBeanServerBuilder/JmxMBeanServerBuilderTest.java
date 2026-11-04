/*
 * @test
 * @summary com.sun.jmx.mbeanserver.JmxMBeanServerBuilder conformance: hierarchy and declared members via reflection.
 * @run main JmxMBeanServerBuilderTest
 */

public class JmxMBeanServerBuilderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jmx.mbeanserver.JmxMBeanServerBuilder", false, null);
        check(c.getName().equals("com.sun.jmx.mbeanserver.JmxMBeanServerBuilder"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.management.MBeanServerBuilder"), "superclass javax.management.MBeanServerBuilder");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("newMBeanServer"), "declares newMBeanServer");
        check(ms.contains("newMBeanServerDelegate"), "declares newMBeanServerDelegate");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
