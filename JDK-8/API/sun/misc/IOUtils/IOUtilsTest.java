/*
 * @test
 * @summary sun.misc.IOUtils conformance: class and members.
 * @run main IOUtilsTest
 */

public class IOUtilsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.IOUtils", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.IOUtils"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("readAllBytes"), "declares method readAllBytes");
        check(mnames.contains("readExactlyNBytes"), "declares method readExactlyNBytes");
        check(mnames.contains("readFully"), "declares method readFully");
        check(mnames.contains("readNBytes"), "declares method readNBytes");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
