/*
 * @test
 * @summary sun.net.www.protocol.jar.URLJarFile conformance: class and members.
 * @run main URLJarFileTest
 */

public class URLJarFileTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.protocol.jar.URLJarFile", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.www.protocol.jar.URLJarFile"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.jar.JarFile"), "superclass java.util.jar.JarFile");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("close"), "declares method close");
        check(mnames.contains("finalize"), "declares method finalize");
        check(mnames.contains("getEntry"), "declares method getEntry");
        check(mnames.contains("getJarFile"), "declares method getJarFile");
        check(mnames.contains("getManifest"), "declares method getManifest");
        check(mnames.contains("isFileURL"), "declares method isFileURL");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
