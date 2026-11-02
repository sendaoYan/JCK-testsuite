/*
 * @test
 * @summary sun.invoke.util.VerifyAccess conformance: class and members.
 * @run main VerifyAccessTest
 */

public class VerifyAccessTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.invoke.util.VerifyAccess", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.invoke.util.VerifyAccess"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("classLoaderIsAncestor"), "declares method classLoaderIsAncestor");
        check(mnames.contains("getClassModifiers"), "declares method getClassModifiers");
        check(mnames.contains("getOutermostEnclosingClass"), "declares method getOutermostEnclosingClass");
        check(mnames.contains("getPackageName"), "declares method getPackageName");
        check(mnames.contains("isClassAccessible"), "declares method isClassAccessible");
        check(mnames.contains("isMemberAccessible"), "declares method isMemberAccessible");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
