/*
 * @test
 * @summary sun.nio.ch.Secrets conformance: class and members.
 * @run main SecretsTest
 */

public class SecretsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.ch.Secrets", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.nio.ch.Secrets"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("newServerSocketChannel"), "declares method newServerSocketChannel");
        check(mnames.contains("newSocketChannel"), "declares method newSocketChannel");
        check(mnames.contains("provider"), "declares method provider");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
