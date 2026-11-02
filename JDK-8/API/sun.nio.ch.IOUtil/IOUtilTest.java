/*
 * @test
 * @summary sun.nio.ch.IOUtil conformance: class and members.
 * @run main IOUtilTest
 */

public class IOUtilTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.ch.IOUtil", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.nio.ch.IOUtil"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("configureBlocking"), "declares method configureBlocking");
        check(mnames.contains("drain"), "declares method drain");
        check(mnames.contains("fdLimit"), "declares method fdLimit");
        check(mnames.contains("fdVal"), "declares method fdVal");
        check(mnames.contains("initIDs"), "declares method initIDs");
        check(mnames.contains("iovMax"), "declares method iovMax");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
