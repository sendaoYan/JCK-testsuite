/*
 * @test
 * @summary com.sun.jmx.remote.security.NotificationAccessController conformance: hierarchy and declared members via reflection.
 * @run main NotificationAccessControllerTest
 */

public class NotificationAccessControllerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jmx.remote.security.NotificationAccessController", false, null);
        check(c.getName().equals("com.sun.jmx.remote.security.NotificationAccessController"), "binary name");
        check(c.isInterface() == true, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "is abstract");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("addNotificationListener"), "declares addNotificationListener");
        check(ms.contains("fetchNotification"), "declares fetchNotification");
        check(ms.contains("removeNotificationListener"), "declares removeNotificationListener");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
