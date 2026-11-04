/*
 * @test
 * @summary java.net.CacheResponse conformance: abstract class hierarchy and members.
 * @run main CacheResponseTest
 */


public class CacheResponseTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.net.CacheResponse");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(!c.isInterface(), "not interface");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getBody"), "has method getBody");
        check(names.contains("getHeaders"), "has method getHeaders");        System.out.println("PASS (" + passed + " assertions)");
    }
}
