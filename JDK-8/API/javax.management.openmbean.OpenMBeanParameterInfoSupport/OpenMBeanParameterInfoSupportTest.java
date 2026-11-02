/*
 * @test
 * @summary javax.management.openmbean.OpenMBeanParameterInfoSupport conformance: concrete class reflection.
 * @run main OpenMBeanParameterInfoSupportTest
 */

public class OpenMBeanParameterInfoSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.OpenMBeanParameterInfoSupport");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("javax.management.MBeanParameterInfo".equals(c.getSuperclass().getName()), "superclass javax.management.MBeanParameterInfo");
        check(Class.forName("javax.management.openmbean.OpenMBeanParameterInfo").isAssignableFrom(c), "implements/extends javax.management.openmbean.OpenMBeanParameterInfo");
        check(hasMethod(c, "equals"), "declares equals");
        check(hasMethod(c, "getDefaultValue"), "declares getDefaultValue");
        check(hasMethod(c, "getLegalValues"), "declares getLegalValues");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
