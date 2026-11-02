/*
 * @test
 * @summary sun.net.www.http.ChunkedOutputStream conformance: class and members.
 * @run main ChunkedOutputStreamTest
 */

public class ChunkedOutputStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.http.ChunkedOutputStream", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.www.http.ChunkedOutputStream"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.io.PrintStream"), "superclass java.io.PrintStream");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("checkError"), "declares method checkError");
        check(mnames.contains("close"), "declares method close");
        check(mnames.contains("ensureOpen"), "declares method ensureOpen");
        check(mnames.contains("flush"), "declares method flush");
        check(mnames.contains("getHeader"), "declares method getHeader");
        check(mnames.contains("getHeaderSize"), "declares method getHeaderSize");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
