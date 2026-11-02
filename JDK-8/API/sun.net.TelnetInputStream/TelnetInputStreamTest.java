/*
 * @test
 * @summary sun.net.TelnetInputStream conformance: class and members.
 * @run main TelnetInputStreamTest
 */

public class TelnetInputStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.TelnetInputStream", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.TelnetInputStream"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.io.FilterInputStream"), "superclass java.io.FilterInputStream");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("read"), "declares method read");
        check(mnames.contains("setStickyCRLF"), "declares method setStickyCRLF");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
