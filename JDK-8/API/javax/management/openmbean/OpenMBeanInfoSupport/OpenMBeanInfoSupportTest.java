/*
 * @test
 * @summary javax.management.openmbean.OpenMBeanInfoSupport conformance: concrete class reflection.
 * @run main OpenMBeanInfoSupportTest
 */

public class OpenMBeanInfoSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.OpenMBeanInfoSupport");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("javax.management.MBeanInfo".equals(c.getSuperclass().getName()), "superclass javax.management.MBeanInfo");
        check(Class.forName("javax.management.openmbean.OpenMBeanInfo").isAssignableFrom(c), "implements/extends javax.management.openmbean.OpenMBeanInfo");
        check(hasMethod(c, "equals"), "declares equals");
        check(hasMethod(c, "hashCode"), "declares hashCode");
        check(hasMethod(c, "toString"), "declares toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
