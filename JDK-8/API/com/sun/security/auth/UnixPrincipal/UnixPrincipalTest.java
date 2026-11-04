/*
 * @test
 * @summary com.sun.security.auth.UnixPrincipal conformance: behavior of constructors, accessors and value semantics.
 * @run main UnixPrincipalTest
 */

public class UnixPrincipalTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        com.sun.security.auth.UnixPrincipal p = new com.sun.security.auth.UnixPrincipal("unixuser");
        check(p instanceof java.security.Principal, "implements java.security.Principal");
        check("unixuser".equals(p.getName()), "getName");
        check(p.toString() != null, "toString");
        check(p.equals(new com.sun.security.auth.UnixPrincipal("unixuser")), "equals");
        check(p.hashCode() == new com.sun.security.auth.UnixPrincipal("unixuser").hashCode(), "hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
