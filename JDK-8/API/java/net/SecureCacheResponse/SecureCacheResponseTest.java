/*
 * @test
 * @summary java.net.SecureCacheResponse conformance: abstract class hierarchy and members.
 * @run main SecureCacheResponseTest
 */


public class SecureCacheResponseTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.net.SecureCacheResponse");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(!c.isInterface(), "not interface");
        check("java.net.CacheResponse".equals(c.getSuperclass().getName()), "superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getBody"), "has method getBody");
        check(names.contains("getCipherSuite"), "has method getCipherSuite");
        check(names.contains("getHeaders"), "has method getHeaders");        System.out.println("PASS (" + passed + " assertions)");
    }
}
