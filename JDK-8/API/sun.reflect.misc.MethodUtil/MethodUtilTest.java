/*
 * @test
 * @summary sun.reflect.misc.MethodUtil conformance: class and members.
 * @run main MethodUtilTest
 */

public class MethodUtilTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.reflect.misc.MethodUtil", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.reflect.misc.MethodUtil"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.security.SecureClassLoader"), "superclass java.security.SecureClassLoader");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("addMethod"), "declares method addMethod");
        check(mnames.contains("defineClass"), "declares method defineClass");
        check(mnames.contains("findClass"), "declares method findClass");
        check(mnames.contains("getBytes"), "declares method getBytes");
        check(mnames.contains("getInterfaceMethods"), "declares method getInterfaceMethods");
        check(mnames.contains("getInternalPublicMethods"), "declares method getInternalPublicMethods");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
