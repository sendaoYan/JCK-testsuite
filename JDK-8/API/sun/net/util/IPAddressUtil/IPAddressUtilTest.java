/*
 * @test
 * @summary sun.net.util.IPAddressUtil conformance: class and members.
 * @run main IPAddressUtilTest
 */

public class IPAddressUtilTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.util.IPAddressUtil", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.util.IPAddressUtil"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("checkAuth"), "declares method checkAuth");
        check(mnames.contains("checkAuthority"), "declares method checkAuthority");
        check(mnames.contains("checkExternalForm"), "declares method checkExternalForm");
        check(mnames.contains("checkHost"), "declares method checkHost");
        check(mnames.contains("checkHostString"), "declares method checkHostString");
        check(mnames.contains("checkPrefix"), "declares method checkPrefix");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
