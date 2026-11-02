/*
 * @test
 * @summary sun.net.util.URLUtil conformance: class and members.
 * @run main URLUtilTest
 */

public class URLUtilTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.util.URLUtil", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.util.URLUtil"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getConnectPermission"), "declares method getConnectPermission");
        check(mnames.contains("getURLConnectPermission"), "declares method getURLConnectPermission");
        check(mnames.contains("urlNoFragString"), "declares method urlNoFragString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
