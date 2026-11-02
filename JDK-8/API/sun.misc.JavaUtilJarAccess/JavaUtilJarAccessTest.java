/*
 * @test
 * @summary sun.misc.JavaUtilJarAccess conformance: interface and members.
 * @run main JavaUtilJarAccessTest
 */

public class JavaUtilJarAccessTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.JavaUtilJarAccess", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.JavaUtilJarAccess"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("ensureInitialization"), "declares method ensureInitialization");
        check(mnames.contains("entries2"), "declares method entries2");
        check(mnames.contains("entryNames"), "declares method entryNames");
        check(mnames.contains("getCodeSource"), "declares method getCodeSource");
        check(mnames.contains("getCodeSources"), "declares method getCodeSources");
        check(mnames.contains("getManifestDigests"), "declares method getManifestDigests");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
