/*
 * @test
 * @summary sun.nio.ch.ChannelInputStream conformance: class and members.
 * @run main ChannelInputStreamTest
 */

public class ChannelInputStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.ch.ChannelInputStream", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.nio.ch.ChannelInputStream"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.io.InputStream"), "superclass java.io.InputStream");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("available"), "declares method available");
        check(mnames.contains("close"), "declares method close");
        check(mnames.contains("read"), "declares method read");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
