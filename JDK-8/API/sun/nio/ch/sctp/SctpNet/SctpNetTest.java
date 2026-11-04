/*
 * @test
 * @summary sun.nio.ch.sctp.SctpNet conformance: class and members.
 * @run main SctpNetTest
 */

public class SctpNetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.ch.sctp.SctpNet", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.nio.ch.sctp.SctpNet"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("IPv4MappedAddresses"), "declares method IPv4MappedAddresses");
        check(mnames.contains("bindx"), "declares method bindx");
        check(mnames.contains("branch"), "declares method branch");
        check(mnames.contains("branch0"), "declares method branch0");
        check(mnames.contains("close"), "declares method close");
        check(mnames.contains("close0"), "declares method close0");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
