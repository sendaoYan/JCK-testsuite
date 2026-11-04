/*
 * @test
 * @summary sun.net.www.protocol.jar.JarURLConnection conformance: class and members.
 * @run main JarURLConnectionTest
 */

public class JarURLConnectionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.protocol.jar.JarURLConnection", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.www.protocol.jar.JarURLConnection"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.net.JarURLConnection"), "superclass java.net.JarURLConnection");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("addRequestProperty"), "declares method addRequestProperty");
        check(mnames.contains("connect"), "declares method connect");
        check(mnames.contains("getAllowUserInteraction"), "declares method getAllowUserInteraction");
        check(mnames.contains("getContent"), "declares method getContent");
        check(mnames.contains("getContentLength"), "declares method getContentLength");
        check(mnames.contains("getContentLengthLong"), "declares method getContentLengthLong");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
