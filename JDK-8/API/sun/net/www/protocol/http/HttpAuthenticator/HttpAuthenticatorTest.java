/*
 * @test
 * @summary sun.net.www.protocol.http.HttpAuthenticator conformance: interface and members.
 * @run main HttpAuthenticatorTest
 */

public class HttpAuthenticatorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.protocol.http.HttpAuthenticator", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.www.protocol.http.HttpAuthenticator"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("authString"), "declares method authString");
        check(mnames.contains("schemeSupported"), "declares method schemeSupported");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
