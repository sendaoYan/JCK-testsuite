/*
 * @test
 * @summary java.net.CookiePolicy conformance: interface hierarchy and members.
 * @run main CookiePolicyTest
 */


public class CookiePolicyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.net.CookiePolicy");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("shouldAccept"), "has method shouldAccept");        System.out.println("PASS (" + passed + " assertions)");
    }
}
