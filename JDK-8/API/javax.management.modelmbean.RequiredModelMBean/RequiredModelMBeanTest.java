/*
 * @test
 * @summary javax.management.modelmbean.RequiredModelMBean conformance: concrete class reflection.
 * @run main RequiredModelMBeanTest
 */

public class RequiredModelMBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.modelmbean.RequiredModelMBean");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check(Class.forName("javax.management.modelmbean.ModelMBean").isAssignableFrom(c), "implements/extends javax.management.modelmbean.ModelMBean");
        check(Class.forName("javax.management.MBeanRegistration").isAssignableFrom(c), "implements/extends javax.management.MBeanRegistration");
        check(Class.forName("javax.management.NotificationEmitter").isAssignableFrom(c), "implements/extends javax.management.NotificationEmitter");
        check(hasMethod(c, "addAttributeChangeNotificationListener"), "declares addAttributeChangeNotificationListener");
        check(hasMethod(c, "addNotificationListener"), "declares addNotificationListener");
        check(hasMethod(c, "getAttribute"), "declares getAttribute");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
