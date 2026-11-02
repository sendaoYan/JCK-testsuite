/*
 * @test
 * @summary com.sun.security.auth.NTDomainPrincipal conformance: behavior of constructors, accessors and value semantics.
 * @run main NTDomainPrincipalTest
 */

public class NTDomainPrincipalTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        com.sun.security.auth.NTDomainPrincipal p = new com.sun.security.auth.NTDomainPrincipal("DOMAIN");
        check(p instanceof java.security.Principal, "implements java.security.Principal");
        check("DOMAIN".equals(p.getName()), "getName");
        check(p.toString() != null, "toString");
        check(p.equals(new com.sun.security.auth.NTDomainPrincipal("DOMAIN")), "equals");
        check(p.hashCode() == new com.sun.security.auth.NTDomainPrincipal("DOMAIN").hashCode(), "hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
