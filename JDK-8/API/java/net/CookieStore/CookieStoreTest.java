/*
 * @test
 * @summary java.net.CookieStore conformance: interface hierarchy and members.
 * @run main CookieStoreTest
 */


public class CookieStoreTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.net.CookieStore");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("add"), "has method add");
        check(names.contains("get"), "has method get");
        check(names.contains("getCookies"), "has method getCookies");        System.out.println("PASS (" + passed + " assertions)");
    }
}
