/*
 * @test
 * @summary com.sun.net.httpserver.HttpPrincipal conformance: behavior of constructors, accessors and value semantics.
 * @run main HttpPrincipalTest
 */

public class HttpPrincipalTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        com.sun.net.httpserver.HttpPrincipal p = new com.sun.net.httpserver.HttpPrincipal("user", "realm");
        check(p instanceof java.security.Principal, "implements java.security.Principal");
        check("realm".equals(p.getRealm()), "realm");
        check("user".equals(p.getUsername()), "username");
        check("user".equals(p.getName()), "name");
        check(p.equals(new com.sun.net.httpserver.HttpPrincipal("user", "realm")), "equals");
        check(!p.equals(new com.sun.net.httpserver.HttpPrincipal("user", "other")), "not equals");
        check(p.hashCode() == new com.sun.net.httpserver.HttpPrincipal("user", "realm").hashCode(), "hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
