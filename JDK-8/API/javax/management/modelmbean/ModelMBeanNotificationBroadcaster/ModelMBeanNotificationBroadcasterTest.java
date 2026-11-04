/*
 * @test
 * @summary javax.management.modelmbean.ModelMBeanNotificationBroadcaster conformance: interface reflection.
 * @run main ModelMBeanNotificationBroadcasterTest
 */

public class ModelMBeanNotificationBroadcasterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.modelmbean.ModelMBeanNotificationBroadcaster");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(Class.forName("javax.management.NotificationBroadcaster").isAssignableFrom(c), "implements/extends javax.management.NotificationBroadcaster");
        check(hasMethod(c, "addAttributeChangeNotificationListener"), "declares addAttributeChangeNotificationListener");
        check(hasMethod(c, "removeAttributeChangeNotificationListener"), "declares removeAttributeChangeNotificationListener");
        check(hasMethod(c, "sendAttributeChangeNotification"), "declares sendAttributeChangeNotification");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
