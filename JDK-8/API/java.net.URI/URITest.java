/*
 * @test
 * @summary java.net.URI conformance: components/resolve/relativize.
 * @run main URITest
 */


public class URITest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.net.URI u = new java.net.URI("http://user@example.com:8080/a/b?q=1#frag");
        check("http".equals(u.getScheme()), "getScheme");
        check("example.com".equals(u.getHost()), "getHost");
        check(u.getPort() == 8080, "getPort");
        check("/a/b".equals(u.getPath()), "getPath");
        check("q=1".equals(u.getQuery()), "getQuery");
        check("frag".equals(u.getFragment()), "getFragment");
        check("user".equals(u.getUserInfo()), "getUserInfo");
        check("user@example.com:8080".equals(u.getAuthority()), "getAuthority");
        java.net.URI base = new java.net.URI("http://example.com/a/b");
        check(base.resolve("/c").toString().equals("http://example.com/c"), "resolve");
        check(base.resolve("d").toString().equals("http://example.com/a/d"), "resolve relative");
        java.net.URI same = new java.net.URI("http://example.com/a/b");
        check(base.equals(same) && base.compareTo(same) == 0, "equals/compareTo");
        check(!base.equals(new java.net.URI("http://example.com/a/x")), "unequal");        System.out.println("PASS (" + passed + " assertions)");
    }
}
