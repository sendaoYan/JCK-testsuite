/*
 * @test
 * @requires os.family == "linux"
 * @summary sun.net.dns.ResolverConfigurationImpl conformance: class and members.
 * @run main ResolverConfigurationImplTest
 */

public class ResolverConfigurationImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.dns.ResolverConfigurationImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.dns.ResolverConfigurationImpl"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.net.dns.ResolverConfiguration"), "superclass sun.net.dns.ResolverConfiguration");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("fallbackDomain0"), "declares method fallbackDomain0");
        check(mnames.contains("getSearchList"), "declares method getSearchList");
        check(mnames.contains("loadConfig"), "declares method loadConfig");
        check(mnames.contains("localDomain0"), "declares method localDomain0");
        check(mnames.contains("nameservers"), "declares method nameservers");
        check(mnames.contains("options"), "declares method options");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
