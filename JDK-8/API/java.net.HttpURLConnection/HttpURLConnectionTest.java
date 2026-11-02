/*
 * @test
 * @summary java.net.HttpURLConnection conformance: abstract class hierarchy and members.
 * @run main HttpURLConnectionTest
 */


public class HttpURLConnectionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.net.HttpURLConnection");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(!c.isInterface(), "not interface");
        check("java.net.URLConnection".equals(c.getSuperclass().getName()), "superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("addRequestProperty"), "has method addRequestProperty");
        check(names.contains("connect"), "has method connect");
        check(names.contains("disconnect"), "has method disconnect");        System.out.println("PASS (" + passed + " assertions)");
    }
}
