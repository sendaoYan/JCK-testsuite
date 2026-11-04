/*
 * @test
 * @summary javax.management.timer.Timer conformance: concrete class reflection.
 * @run main TimerTest
 */

public class TimerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.timer.Timer");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("javax.management.NotificationBroadcasterSupport".equals(c.getSuperclass().getName()), "superclass javax.management.NotificationBroadcasterSupport");
        check(Class.forName("javax.management.timer.TimerMBean").isAssignableFrom(c), "implements/extends javax.management.timer.TimerMBean");
        check(Class.forName("javax.management.MBeanRegistration").isAssignableFrom(c), "implements/extends javax.management.MBeanRegistration");
        check(hasMethod(c, "addNotification"), "declares addNotification");
        check(hasMethod(c, "getAllNotificationIDs"), "declares getAllNotificationIDs");
        check(hasMethod(c, "getDate"), "declares getDate");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
