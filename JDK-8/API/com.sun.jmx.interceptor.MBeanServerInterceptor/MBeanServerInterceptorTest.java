/*
 * @test
 * @summary com.sun.jmx.interceptor.MBeanServerInterceptor conformance: hierarchy and declared members via reflection.
 * @run main MBeanServerInterceptorTest
 */

public class MBeanServerInterceptorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jmx.interceptor.MBeanServerInterceptor", false, null);
        check(c.getName().equals("com.sun.jmx.interceptor.MBeanServerInterceptor"), "binary name");
        check(c.isInterface() == true, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "is abstract");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("javax.management.MBeanServer"), "implements javax.management.MBeanServer");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("deserialize"), "declares deserialize");
        check(ms.contains("getClassLoaderRepository"), "declares getClassLoaderRepository");
        check(ms.contains("instantiate"), "declares instantiate");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
