/*
 * @test
 * @summary sun.misc.JavaUtilZipFileAccess conformance: interface and members.
 * @run main JavaUtilZipFileAccessTest
 */

public class JavaUtilZipFileAccessTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.JavaUtilZipFileAccess", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.JavaUtilZipFileAccess"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getManifestNum"), "declares method getManifestNum");
        check(mnames.contains("startsWithLocHeader"), "declares method startsWithLocHeader");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
