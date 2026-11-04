/*
 * @test
 * @summary sun.net.ApplicationProxy conformance: class and members.
 * @run main ApplicationProxyTest
 */

public class ApplicationProxyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.ApplicationProxy", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.ApplicationProxy"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.net.Proxy"), "superclass java.net.Proxy");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("create"), "declares method create");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
