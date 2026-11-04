/*
 * @test
 * @summary sun.net.www.URLConnection conformance: abstract class and members.
 * @run main URLConnectionTest
 */

public class URLConnectionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.URLConnection", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.www.URLConnection"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.net.URLConnection"), "superclass java.net.URLConnection");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("addRequestProperty"), "declares method addRequestProperty");
        check(mnames.contains("canCache"), "declares method canCache");
        check(mnames.contains("close"), "declares method close");
        check(mnames.contains("getContentLength"), "declares method getContentLength");
        check(mnames.contains("getContentType"), "declares method getContentType");
        check(mnames.contains("getHeaderField"), "declares method getHeaderField");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
