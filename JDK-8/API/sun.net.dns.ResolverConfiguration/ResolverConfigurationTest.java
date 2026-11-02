/*
 * @test
 * @summary sun.net.dns.ResolverConfiguration conformance: abstract class and members.
 * @run main ResolverConfigurationTest
 */

public class ResolverConfigurationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.dns.ResolverConfiguration", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.dns.ResolverConfiguration"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("nameservers"), "declares method nameservers");
        check(mnames.contains("open"), "declares method open");
        check(mnames.contains("options"), "declares method options");
        check(mnames.contains("searchlist"), "declares method searchlist");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
