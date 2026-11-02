/*
 * @test
 * @summary sun.rmi.log.LogInputStream conformance: class and members.
 * @run main LogInputStreamTest
 */

public class LogInputStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.rmi.log.LogInputStream", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.rmi.log.LogInputStream"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.io.InputStream"), "superclass java.io.InputStream");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("available"), "declares method available");
        check(mnames.contains("close"), "declares method close");
        check(mnames.contains("finalize"), "declares method finalize");
        check(mnames.contains("read"), "declares method read");
        check(mnames.contains("skip"), "declares method skip");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
