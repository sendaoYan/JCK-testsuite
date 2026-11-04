/*
 * @test
 * @summary sun.reflect.misc.ReflectUtil conformance: class and members.
 * @run main ReflectUtilTest
 */

public class ReflectUtilTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.reflect.misc.ReflectUtil", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.reflect.misc.ReflectUtil"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("checkPackageAccess"), "declares method checkPackageAccess");
        check(mnames.contains("checkProxyMethod"), "declares method checkProxyMethod");
        check(mnames.contains("checkProxyPackageAccess"), "declares method checkProxyPackageAccess");
        check(mnames.contains("conservativeCheckMemberAccess"), "declares method conservativeCheckMemberAccess");
        check(mnames.contains("ensureMemberAccess"), "declares method ensureMemberAccess");
        check(mnames.contains("forName"), "declares method forName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
