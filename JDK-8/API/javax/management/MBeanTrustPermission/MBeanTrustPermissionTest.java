/*
 * @test
 * @summary javax.management.MBeanTrustPermission conformance: concrete class reflection.
 * @run main MBeanTrustPermissionTest
 */

public class MBeanTrustPermissionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.MBeanTrustPermission");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("java.security.BasicPermission".equals(c.getSuperclass().getName()), "superclass java.security.BasicPermission");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
