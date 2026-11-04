/*
 * @test
 * @summary sun.nio.fs.Globs conformance: class and members.
 * @run main GlobsTest
 */

public class GlobsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.fs.Globs", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.nio.fs.Globs"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("isGlobMeta"), "declares method isGlobMeta");
        check(mnames.contains("isRegexMeta"), "declares method isRegexMeta");
        check(mnames.contains("next"), "declares method next");
        check(mnames.contains("toRegexPattern"), "declares method toRegexPattern");
        check(mnames.contains("toUnixRegexPattern"), "declares method toUnixRegexPattern");
        check(mnames.contains("toWindowsRegexPattern"), "declares method toWindowsRegexPattern");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
