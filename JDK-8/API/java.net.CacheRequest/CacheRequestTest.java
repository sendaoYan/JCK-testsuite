/*
 * @test
 * @summary java.net.CacheRequest conformance: abstract class hierarchy and members.
 * @run main CacheRequestTest
 */


public class CacheRequestTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.net.CacheRequest");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(!c.isInterface(), "not interface");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("abort"), "has method abort");
        check(names.contains("getBody"), "has method getBody");        System.out.println("PASS (" + passed + " assertions)");
    }
}
