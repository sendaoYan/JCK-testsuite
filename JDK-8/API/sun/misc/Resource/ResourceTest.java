/*
 * @test
 * @summary sun.misc.Resource conformance: abstract class and members.
 * @run main ResourceTest
 */

public class ResourceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.Resource", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.Resource"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("cachedInputStream"), "declares method cachedInputStream");
        check(mnames.contains("getByteBuffer"), "declares method getByteBuffer");
        check(mnames.contains("getBytes"), "declares method getBytes");
        check(mnames.contains("getCertificates"), "declares method getCertificates");
        check(mnames.contains("getCodeSigners"), "declares method getCodeSigners");
        check(mnames.contains("getCodeSourceURL"), "declares method getCodeSourceURL");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
