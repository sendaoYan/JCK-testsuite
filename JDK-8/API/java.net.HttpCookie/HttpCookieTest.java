/*
 * @test
 * @summary java.net.HttpCookie conformance: parse/name/value/domain/path.
 * @run main HttpCookieTest
 */


public class HttpCookieTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.net.HttpCookie c = new java.net.HttpCookie("sid", "abc123");
        check("sid".equals(c.getName()), "getName");
        check("abc123".equals(c.getValue()), "getValue");
        c.setDomain("example.com"); c.setPath("/"); c.setMaxAge(3600); c.setSecure(true);
        check("example.com".equals(c.getDomain()), "getDomain");
        check("/".equals(c.getPath()), "getPath");
        check(c.getMaxAge() == 3600, "getMaxAge");
        check(c.getSecure(), "getSecure");
        java.util.List<java.net.HttpCookie> list = java.net.HttpCookie.parse("Set-Cookie: a=b; Path=/");
        check(list.size() == 1 && "a".equals(list.get(0).getName()), "parse");
        check(!c.hasExpired(), "hasExpired");        System.out.println("PASS (" + passed + " assertions)");
    }
}
