/*
 * @test
 * @summary sun.net.NetworkClient conformance: class and members.
 * @run main NetworkClientTest
 */

public class NetworkClientTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.NetworkClient", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.NetworkClient"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("closeServer"), "declares method closeServer");
        check(mnames.contains("createSocket"), "declares method createSocket");
        check(mnames.contains("doConnect"), "declares method doConnect");
        check(mnames.contains("getConnectTimeout"), "declares method getConnectTimeout");
        check(mnames.contains("getLocalAddress"), "declares method getLocalAddress");
        check(mnames.contains("getReadTimeout"), "declares method getReadTimeout");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
