/*
 * @test
 * @summary sun.net.www.http.HttpClient conformance: class and members.
 * @run main HttpClientTest
 */

public class HttpClientTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.http.HttpClient", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.www.http.HttpClient"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.net.NetworkClient"), "superclass sun.net.NetworkClient");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("New"), "declares method New");
        check(mnames.contains("afterConnect"), "declares method afterConnect");
        check(mnames.contains("available"), "declares method available");
        check(mnames.contains("checkTunneling"), "declares method checkTunneling");
        check(mnames.contains("closeIdleConnection"), "declares method closeIdleConnection");
        check(mnames.contains("closeServer"), "declares method closeServer");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
