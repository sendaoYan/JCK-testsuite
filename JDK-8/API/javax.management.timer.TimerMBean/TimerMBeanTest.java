/*
 * @test
 * @summary javax.management.timer.TimerMBean conformance: interface reflection.
 * @run main TimerMBeanTest
 */

public class TimerMBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.timer.TimerMBean");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(hasMethod(c, "addNotification"), "declares addNotification");
        check(hasMethod(c, "getAllNotificationIDs"), "declares getAllNotificationIDs");
        check(hasMethod(c, "getDate"), "declares getDate");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
