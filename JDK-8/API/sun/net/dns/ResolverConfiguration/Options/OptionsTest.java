/*
 * @test
 * @summary sun.net.dns.ResolverConfiguration.Options conformance: abstract class and members.
 * @run main OptionsTest
 */

public class OptionsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.dns.ResolverConfiguration$Options", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.dns.ResolverConfiguration$Options"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("attempts"), "declares method attempts");
        check(mnames.contains("retrans"), "declares method retrans");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
