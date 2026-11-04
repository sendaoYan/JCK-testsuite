/*
 * @test
 * @summary javax.management.openmbean.OpenMBeanOperationInfoSupport conformance: concrete class reflection.
 * @run main OpenMBeanOperationInfoSupportTest
 */

public class OpenMBeanOperationInfoSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.OpenMBeanOperationInfoSupport");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("javax.management.MBeanOperationInfo".equals(c.getSuperclass().getName()), "superclass javax.management.MBeanOperationInfo");
        check(Class.forName("javax.management.openmbean.OpenMBeanOperationInfo").isAssignableFrom(c), "implements/extends javax.management.openmbean.OpenMBeanOperationInfo");
        check(hasMethod(c, "equals"), "declares equals");
        check(hasMethod(c, "getReturnOpenType"), "declares getReturnOpenType");
        check(hasMethod(c, "hashCode"), "declares hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
