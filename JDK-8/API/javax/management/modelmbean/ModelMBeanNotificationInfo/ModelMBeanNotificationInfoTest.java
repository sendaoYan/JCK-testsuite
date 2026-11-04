/*
 * @test
 * @summary javax.management.modelmbean.ModelMBeanNotificationInfo conformance: concrete class reflection.
 * @run main ModelMBeanNotificationInfoTest
 */

public class ModelMBeanNotificationInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.modelmbean.ModelMBeanNotificationInfo");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("javax.management.MBeanNotificationInfo".equals(c.getSuperclass().getName()), "superclass javax.management.MBeanNotificationInfo");
        check(Class.forName("javax.management.DescriptorAccess").isAssignableFrom(c), "implements/extends javax.management.DescriptorAccess");
        check(hasMethod(c, "clone"), "declares clone");
        check(hasMethod(c, "getDescriptor"), "declares getDescriptor");
        check(hasMethod(c, "setDescriptor"), "declares setDescriptor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
