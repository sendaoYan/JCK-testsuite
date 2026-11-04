/*
 * @test
 * @summary javax.management.NotificationEmitter conformance: interface reflection.
 * @run main NotificationEmitterTest
 */

public class NotificationEmitterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.NotificationEmitter");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(Class.forName("javax.management.NotificationBroadcaster").isAssignableFrom(c), "implements/extends javax.management.NotificationBroadcaster");
        check(hasMethod(c, "removeNotificationListener"), "declares removeNotificationListener");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
