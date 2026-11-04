/*
 * @test
 * @summary javax.management.relation.MBeanServerNotificationFilter conformance: concrete class reflection.
 * @run main MBeanServerNotificationFilterTest
 */

public class MBeanServerNotificationFilterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.relation.MBeanServerNotificationFilter");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("javax.management.NotificationFilterSupport".equals(c.getSuperclass().getName()), "superclass javax.management.NotificationFilterSupport");
        check(hasMethod(c, "disableAllObjectNames"), "declares disableAllObjectNames");
        check(hasMethod(c, "disableObjectName"), "declares disableObjectName");
        check(hasMethod(c, "enableAllObjectNames"), "declares enableAllObjectNames");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
