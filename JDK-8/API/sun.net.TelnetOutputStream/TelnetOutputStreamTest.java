/*
 * @test
 * @summary sun.net.TelnetOutputStream conformance: class and members.
 * @run main TelnetOutputStreamTest
 */

public class TelnetOutputStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.TelnetOutputStream", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.TelnetOutputStream"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.io.BufferedOutputStream"), "superclass java.io.BufferedOutputStream");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("setStickyCRLF"), "declares method setStickyCRLF");
        check(mnames.contains("write"), "declares method write");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
