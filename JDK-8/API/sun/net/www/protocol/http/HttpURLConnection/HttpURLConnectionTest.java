/*
 * @test
 * @summary sun.net.www.protocol.http.HttpURLConnection conformance: class and members.
 * @run main HttpURLConnectionTest
 */

public class HttpURLConnectionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.protocol.http.HttpURLConnection", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.www.protocol.http.HttpURLConnection"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.net.HttpURLConnection"), "superclass java.net.HttpURLConnection");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("URLtoSocketPermission"), "declares method URLtoSocketPermission");
        check(mnames.contains("addRequestProperty"), "declares method addRequestProperty");
        check(mnames.contains("authObj"), "declares method authObj");
        check(mnames.contains("checkHost"), "declares method checkHost");
        check(mnames.contains("checkMessageHeader"), "declares method checkMessageHeader");
        check(mnames.contains("checkResponseCredentials"), "declares method checkResponseCredentials");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
