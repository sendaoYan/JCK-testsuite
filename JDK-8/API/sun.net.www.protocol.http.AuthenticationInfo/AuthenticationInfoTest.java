/*
 * @test
 * @summary sun.net.www.protocol.http.AuthenticationInfo conformance: abstract class and members.
 * @run main AuthenticationInfoTest
 */

public class AuthenticationInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.protocol.http.AuthenticationInfo", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.www.protocol.http.AuthenticationInfo"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.net.www.protocol.http.AuthCacheValue"), "superclass sun.net.www.protocol.http.AuthCacheValue");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("java.lang.Cloneable"), "interface java.lang.Cloneable");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("addToCache"), "declares method addToCache");
        check(mnames.contains("cacheKey"), "declares method cacheKey");
        check(mnames.contains("clone"), "declares method clone");
        check(mnames.contains("credentials"), "declares method credentials");
        check(mnames.contains("endAuthRequest"), "declares method endAuthRequest");
        check(mnames.contains("getAuth"), "declares method getAuth");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
