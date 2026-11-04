/*
 * @test
 * @summary javax.management.modelmbean.ModelMBeanInfoSupport conformance: concrete class reflection.
 * @run main ModelMBeanInfoSupportTest
 */

public class ModelMBeanInfoSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.modelmbean.ModelMBeanInfoSupport");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("javax.management.MBeanInfo".equals(c.getSuperclass().getName()), "superclass javax.management.MBeanInfo");
        check(Class.forName("javax.management.modelmbean.ModelMBeanInfo").isAssignableFrom(c), "implements/extends javax.management.modelmbean.ModelMBeanInfo");
        check(hasMethod(c, "clone"), "declares clone");
        check(hasMethod(c, "getAttribute"), "declares getAttribute");
        check(hasMethod(c, "getConstructor"), "declares getConstructor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
