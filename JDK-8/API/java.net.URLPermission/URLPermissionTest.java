/*
 * @test
 * @summary java.net.URLPermission conformance: name/actions.
 * @run main URLPermissionTest
 */


public class URLPermissionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.net.URLPermission p = new java.net.URLPermission("http://example.com/*");
        check("http://example.com/*".equals(p.getName()), "getName");
        check("*:*".equals(p.getActions()), "getActions default");
        java.net.URLPermission g = new java.net.URLPermission("http://example.com/*", "GET");
        check("GET:".equals(g.getActions()), "getActions GET");        System.out.println("PASS (" + passed + " assertions)");
    }
}
