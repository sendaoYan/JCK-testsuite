/*
 * @test
 * @summary javax.management.MBeanServerPermission conformance: concrete class reflection.
 * @run main MBeanServerPermissionTest
 */

public class MBeanServerPermissionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.MBeanServerPermission");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("java.security.BasicPermission".equals(c.getSuperclass().getName()), "superclass java.security.BasicPermission");
        check(hasMethod(c, "equals"), "declares equals");
        check(hasMethod(c, "hashCode"), "declares hashCode");
        check(hasMethod(c, "implies"), "declares implies");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
