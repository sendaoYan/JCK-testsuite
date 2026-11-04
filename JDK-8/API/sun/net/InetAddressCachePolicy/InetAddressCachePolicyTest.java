/*
 * @test
 * @summary sun.net.InetAddressCachePolicy conformance: class and members.
 * @run main InetAddressCachePolicyTest
 */

public class InetAddressCachePolicyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.InetAddressCachePolicy", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.InetAddressCachePolicy"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("checkValue"), "declares method checkValue");
        check(mnames.contains("get"), "declares method get");
        check(mnames.contains("getNegative"), "declares method getNegative");
        check(mnames.contains("setIfNotSet"), "declares method setIfNotSet");
        check(mnames.contains("setNegativeIfNotSet"), "declares method setNegativeIfNotSet");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
