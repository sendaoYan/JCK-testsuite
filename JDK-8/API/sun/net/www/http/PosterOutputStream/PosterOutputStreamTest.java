/*
 * @test
 * @summary sun.net.www.http.PosterOutputStream conformance: class and members.
 * @run main PosterOutputStreamTest
 */

public class PosterOutputStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.http.PosterOutputStream", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.www.http.PosterOutputStream"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.io.ByteArrayOutputStream"), "superclass java.io.ByteArrayOutputStream");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("close"), "declares method close");
        check(mnames.contains("reset"), "declares method reset");
        check(mnames.contains("write"), "declares method write");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
