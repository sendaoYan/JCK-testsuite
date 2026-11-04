/*
 * @test
 * @summary sun.net.httpserver.AuthFilter conformance: class and members.
 * @run main AuthFilterTest
 */

public class AuthFilterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.httpserver.AuthFilter", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.httpserver.AuthFilter"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.net.httpserver.Filter"), "superclass com.sun.net.httpserver.Filter");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("consumeInput"), "declares method consumeInput");
        check(mnames.contains("description"), "declares method description");
        check(mnames.contains("doFilter"), "declares method doFilter");
        check(mnames.contains("setAuthenticator"), "declares method setAuthenticator");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
