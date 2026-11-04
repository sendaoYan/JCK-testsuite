/*
 * @test
 * @summary sun.misc.JarFilter conformance: class and members.
 * @run main JarFilterTest
 */

public class JarFilterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.JarFilter", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.JarFilter"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("java.io.FilenameFilter"), "interface java.io.FilenameFilter");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("accept"), "declares method accept");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
