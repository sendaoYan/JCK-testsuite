/*
 * @test
 * @summary javax.management.MBeanServerDelegate conformance: concrete class reflection.
 * @run main MBeanServerDelegateTest
 */

public class MBeanServerDelegateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.MBeanServerDelegate");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check(Class.forName("javax.management.MBeanServerDelegateMBean").isAssignableFrom(c), "implements/extends javax.management.MBeanServerDelegateMBean");
        check(Class.forName("javax.management.NotificationEmitter").isAssignableFrom(c), "implements/extends javax.management.NotificationEmitter");
        check(hasMethod(c, "addNotificationListener"), "declares addNotificationListener");
        check(hasMethod(c, "getImplementationName"), "declares getImplementationName");
        check(hasMethod(c, "getImplementationVendor"), "declares getImplementationVendor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
