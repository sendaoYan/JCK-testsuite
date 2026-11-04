/*
 * @test
 * @summary sun.net.www.http.KeepAliveStream conformance: class and members.
 * @run main KeepAliveStreamTest
 */

public class KeepAliveStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.http.KeepAliveStream", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.www.http.KeepAliveStream"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.net.www.MeteredStream"), "superclass sun.net.www.MeteredStream");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("sun.net.www.http.Hurryable"), "interface sun.net.www.http.Hurryable");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("close"), "declares method close");
        check(mnames.contains("hurry"), "declares method hurry");
        check(mnames.contains("mark"), "declares method mark");
        check(mnames.contains("markSupported"), "declares method markSupported");
        check(mnames.contains("queueForCleanup"), "declares method queueForCleanup");
        check(mnames.contains("remainingToRead"), "declares method remainingToRead");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
