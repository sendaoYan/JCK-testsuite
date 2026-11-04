/*
 * @test
 * @summary javax.management.modelmbean.ModelMBean conformance: interface reflection.
 * @run main ModelMBeanTest
 */

public class ModelMBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.modelmbean.ModelMBean");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(Class.forName("javax.management.DynamicMBean").isAssignableFrom(c), "implements/extends javax.management.DynamicMBean");
        check(Class.forName("javax.management.PersistentMBean").isAssignableFrom(c), "implements/extends javax.management.PersistentMBean");
        check(Class.forName("javax.management.modelmbean.ModelMBeanNotificationBroadcaster").isAssignableFrom(c), "implements/extends javax.management.modelmbean.ModelMBeanNotificationBroadcaster");
        check(hasMethod(c, "setManagedResource"), "declares setManagedResource");
        check(hasMethod(c, "setModelMBeanInfo"), "declares setModelMBeanInfo");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
