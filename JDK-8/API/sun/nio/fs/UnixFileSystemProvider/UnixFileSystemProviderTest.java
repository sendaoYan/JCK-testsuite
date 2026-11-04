/*
 * @test
 * @summary sun.nio.fs.UnixFileSystemProvider conformance: abstract class and members.
 * @run main UnixFileSystemProviderTest
 */

public class UnixFileSystemProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.fs.UnixFileSystemProvider", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.nio.fs.UnixFileSystemProvider"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.nio.fs.AbstractFileSystemProvider"), "superclass sun.nio.fs.AbstractFileSystemProvider");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("chain"), "declares method chain");
        check(mnames.contains("checkAccess"), "declares method checkAccess");
        check(mnames.contains("checkPath"), "declares method checkPath");
        check(mnames.contains("checkUri"), "declares method checkUri");
        check(mnames.contains("copy"), "declares method copy");
        check(mnames.contains("createDirectory"), "declares method createDirectory");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
