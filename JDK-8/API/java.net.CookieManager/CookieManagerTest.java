/*
 * @test
 * @summary java.net.CookieManager conformance: store/policy/put/get.
 * @run main CookieManagerTest
 */


public class CookieManagerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.net.CookieManager cm = new java.net.CookieManager();
        check(cm.getCookieStore() != null, "default cookie store");
        java.net.CookiePolicy policy = new java.net.CookiePolicy() {
            public boolean shouldAccept(java.net.URI u, java.net.HttpCookie c) { return false; }
        };
        cm.setCookiePolicy(policy);
        java.net.URI uri = new java.net.URI("http://example.com/");
        java.net.HttpCookie ck = new java.net.HttpCookie("k", "v");
        cm.getCookieStore().add(uri, ck);
        check(cm.getCookieStore().get(uri).contains(ck), "cookie stored");
        java.util.Map<String, java.util.List<String>> headers = new java.util.HashMap<String, java.util.List<String>>();
        headers.put("Set-Cookie", java.util.Collections.singletonList("a=b; Path=/"));
        cm.put(uri, headers);
        java.util.Map<String, java.util.List<String>> got = cm.get(uri, new java.util.HashMap<String, java.util.List<String>>());
        check(got != null, "get returns cookie map");        System.out.println("PASS (" + passed + " assertions)");
    }
}
