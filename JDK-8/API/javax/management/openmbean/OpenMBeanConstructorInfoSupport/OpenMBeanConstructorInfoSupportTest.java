/*
 * @test
 * @summary javax.management.openmbean.OpenMBeanConstructorInfoSupport conformance: concrete class reflection.
 * @run main OpenMBeanConstructorInfoSupportTest
 */

public class OpenMBeanConstructorInfoSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.OpenMBeanConstructorInfoSupport");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("javax.management.MBeanConstructorInfo".equals(c.getSuperclass().getName()), "superclass javax.management.MBeanConstructorInfo");
        check(Class.forName("javax.management.openmbean.OpenMBeanConstructorInfo").isAssignableFrom(c), "implements/extends javax.management.openmbean.OpenMBeanConstructorInfo");
        check(hasMethod(c, "equals"), "declares equals");
        check(hasMethod(c, "hashCode"), "declares hashCode");
        check(hasMethod(c, "toString"), "declares toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
