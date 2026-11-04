/*
 * @test
 * @summary javax.management.MBeanServerBuilder conformance: concrete class reflection.
 * @run main MBeanServerBuilderTest
 */

public class MBeanServerBuilderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.MBeanServerBuilder");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check(hasMethod(c, "newMBeanServer"), "declares newMBeanServer");
        check(hasMethod(c, "newMBeanServerDelegate"), "declares newMBeanServerDelegate");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
