/*
 * @test
 * @summary javax.net.ssl.SSLSessionContext conformance: interface hierarchy and key methods.
 * @run main SSLSessionContextTest
 */

public class SSLSessionContextTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.SSLSessionContext");
        check(c.isInterface(), "is interface");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getIds"), "declares getIds");
        check(ms.contains("getSession"), "declares getSession");
        check(ms.contains("getSessionCacheSize"), "declares getSessionCacheSize");
        check(ms.contains("getSessionTimeout"), "declares getSessionTimeout");
        check(ms.contains("setSessionCacheSize"), "declares setSessionCacheSize");
        check(ms.contains("setSessionTimeout"), "declares setSessionTimeout");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
