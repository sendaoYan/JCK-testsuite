/*
 * @test
 * @summary javax.management.MBeanPermission conformance: concrete class reflection.
 * @run main MBeanPermissionTest
 */

public class MBeanPermissionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.MBeanPermission");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("java.security.Permission".equals(c.getSuperclass().getName()), "superclass java.security.Permission");
        check(hasMethod(c, "equals"), "declares equals");
        check(hasMethod(c, "getActions"), "declares getActions");
        check(hasMethod(c, "hashCode"), "declares hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
