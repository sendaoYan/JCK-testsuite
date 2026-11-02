/*
 * @test
 * @summary sun.misc.URLClassPath conformance: class and members.
 * @run main URLClassPathTest
 */

public class URLClassPathTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.URLClassPath", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.URLClassPath"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("addURL"), "declares method addURL");
        check(mnames.contains("check"), "declares method check");
        check(mnames.contains("checkURL"), "declares method checkURL");
        check(mnames.contains("closeLoaders"), "declares method closeLoaders");
        check(mnames.contains("disableAllLookupCaches"), "declares method disableAllLookupCaches");
        check(mnames.contains("ensureLoaderOpened"), "declares method ensureLoaderOpened");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
