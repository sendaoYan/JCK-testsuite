/*
 * @test
 * @summary java.security.URIParameter conformance: constructor and getURI.
 * @run main URIParameterTest
 */

public class URIParameterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.net.URI u = new java.net.URI("http://example.com/");
        java.security.URIParameter p = new java.security.URIParameter(u);
        check(u.equals(p.getURI()), "getURI");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
