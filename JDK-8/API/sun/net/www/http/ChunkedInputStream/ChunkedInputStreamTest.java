/*
 * @test
 * @summary sun.net.www.http.ChunkedInputStream conformance: class and members.
 * @run main ChunkedInputStreamTest
 */

public class ChunkedInputStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.http.ChunkedInputStream", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.www.http.ChunkedInputStream"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.io.InputStream"), "superclass java.io.InputStream");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("sun.net.www.http.Hurryable"), "interface sun.net.www.http.Hurryable");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("available"), "declares method available");
        check(mnames.contains("close"), "declares method close");
        check(mnames.contains("closeUnderlying"), "declares method closeUnderlying");
        check(mnames.contains("ensureOpen"), "declares method ensureOpen");
        check(mnames.contains("ensureRawAvailable"), "declares method ensureRawAvailable");
        check(mnames.contains("fastRead"), "declares method fastRead");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
