/*
 * @test
 * @summary sun.net.www.http.HttpCaptureOutputStream conformance: class and members.
 * @run main HttpCaptureOutputStreamTest
 */

public class HttpCaptureOutputStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.http.HttpCaptureOutputStream", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.www.http.HttpCaptureOutputStream"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.io.FilterOutputStream"), "superclass java.io.FilterOutputStream");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("flush"), "declares method flush");
        check(mnames.contains("write"), "declares method write");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
