/*
 * @test
 * @summary javax.management.remote.JMXConnectorServer conformance: abstract class reflection.
 * @run main JMXConnectorServerTest
 */

public class JMXConnectorServerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.remote.JMXConnectorServer");
        check(!c.isInterface(), "not an interface");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check("javax.management.NotificationBroadcasterSupport".equals(c.getSuperclass().getName()), "superclass javax.management.NotificationBroadcasterSupport");
        check(Class.forName("javax.management.remote.JMXConnectorServerMBean").isAssignableFrom(c), "implements/extends javax.management.remote.JMXConnectorServerMBean");
        check(Class.forName("javax.management.MBeanRegistration").isAssignableFrom(c), "implements/extends javax.management.MBeanRegistration");
        check(Class.forName("javax.management.remote.JMXAddressable").isAssignableFrom(c), "implements/extends javax.management.remote.JMXAddressable");
        check(hasMethod(c, "getConnectionIds"), "declares getConnectionIds");
        check(hasMethod(c, "getMBeanServer"), "declares getMBeanServer");
        check(hasMethod(c, "getNotificationInfo"), "declares getNotificationInfo");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
