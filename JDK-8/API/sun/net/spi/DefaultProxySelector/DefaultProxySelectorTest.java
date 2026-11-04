/*
 * @test
 * @summary sun.net.spi.DefaultProxySelector conformance: class and members.
 * @run main DefaultProxySelectorTest
 */

public class DefaultProxySelectorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.spi.DefaultProxySelector", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.spi.DefaultProxySelector"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.net.ProxySelector"), "superclass java.net.ProxySelector");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("connectFailed"), "declares method connectFailed");
        check(mnames.contains("defaultPort"), "declares method defaultPort");
        check(mnames.contains("disjunctToRegex"), "declares method disjunctToRegex");
        check(mnames.contains("getSystemProxy"), "declares method getSystemProxy");
        check(mnames.contains("init"), "declares method init");
        check(mnames.contains("select"), "declares method select");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
