/*
 * @test
 * @summary java.net.URL conformance: protocol/host/port/path/query/ref.
 * @run main URLTest
 */


public class URLTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.net.URL u = new java.net.URL("http://example.com:8080/a/b?q=1#f");
        check("http".equals(u.getProtocol()), "getProtocol");
        check("example.com".equals(u.getHost()), "getHost");
        check(u.getPort() == 8080, "getPort");
        check("/a/b".equals(u.getPath()), "getPath");
        check("q=1".equals(u.getQuery()), "getQuery");
        check("f".equals(u.getRef()), "getRef");
        check("http://example.com:8080/a/b?q=1#f".equals(u.toExternalForm()), "toExternalForm");
        check(u.toURI().getScheme().equals("http"), "toURI");
        java.net.URL same = new java.net.URL("http://example.com:8080/a/b?q=1#f");
        check(u.equals(same), "equals");        System.out.println("PASS (" + passed + " assertions)");
    }
}
