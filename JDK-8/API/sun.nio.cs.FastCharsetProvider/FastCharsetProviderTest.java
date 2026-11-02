/*
 * @test
 * @summary sun.nio.cs.FastCharsetProvider conformance: class and members.
 * @run main FastCharsetProviderTest
 */

public class FastCharsetProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.cs.FastCharsetProvider", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.nio.cs.FastCharsetProvider"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.nio.charset.spi.CharsetProvider"), "superclass java.nio.charset.spi.CharsetProvider");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("canonicalize"), "declares method canonicalize");
        check(mnames.contains("charsetForName"), "declares method charsetForName");
        check(mnames.contains("charsets"), "declares method charsets");
        check(mnames.contains("lookup"), "declares method lookup");
        check(mnames.contains("toLower"), "declares method toLower");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
