/*
 * @test
 * @summary javax.management.openmbean.OpenMBeanAttributeInfoSupport conformance: concrete class reflection.
 * @run main OpenMBeanAttributeInfoSupportTest
 */

public class OpenMBeanAttributeInfoSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.OpenMBeanAttributeInfoSupport");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("javax.management.MBeanAttributeInfo".equals(c.getSuperclass().getName()), "superclass javax.management.MBeanAttributeInfo");
        check(Class.forName("javax.management.openmbean.OpenMBeanAttributeInfo").isAssignableFrom(c), "implements/extends javax.management.openmbean.OpenMBeanAttributeInfo");
        check(hasMethod(c, "equals"), "declares equals");
        check(hasMethod(c, "getDefaultValue"), "declares getDefaultValue");
        check(hasMethod(c, "getLegalValues"), "declares getLegalValues");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
