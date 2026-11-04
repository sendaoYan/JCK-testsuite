/*
 * @test
 * @summary sun.net.www.protocol.http.AuthCacheValue conformance: abstract class and members.
 * @run main AuthCacheValueTest
 */

public class AuthCacheValueTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.protocol.http.AuthCacheValue", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.www.protocol.http.AuthCacheValue"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("java.io.Serializable"), "interface java.io.Serializable");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("credentials"), "declares method credentials");
        check(mnames.contains("getAuthScheme"), "declares method getAuthScheme");
        check(mnames.contains("getAuthType"), "declares method getAuthType");
        check(mnames.contains("getHost"), "declares method getHost");
        check(mnames.contains("getPath"), "declares method getPath");
        check(mnames.contains("getPort"), "declares method getPort");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
