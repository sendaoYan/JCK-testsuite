/*
 * @test
 * @summary javax.management.MBeanServerNotification conformance: concrete class reflection.
 * @run main MBeanServerNotificationTest
 */

public class MBeanServerNotificationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.MBeanServerNotification");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("javax.management.Notification".equals(c.getSuperclass().getName()), "superclass javax.management.Notification");
        check(hasMethod(c, "getMBeanName"), "declares getMBeanName");
        check(hasMethod(c, "toString"), "declares toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
