/*
 * @test
 * @summary com.sun.security.auth.NTUserPrincipal conformance: behavior of constructors, accessors and value semantics.
 * @run main NTUserPrincipalTest
 */

public class NTUserPrincipalTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        com.sun.security.auth.NTUserPrincipal p = new com.sun.security.auth.NTUserPrincipal("alice");
        check(p instanceof java.security.Principal, "implements java.security.Principal");
        check("alice".equals(p.getName()), "getName");
        check(p.toString() != null, "toString");
        check(p.equals(new com.sun.security.auth.NTUserPrincipal("alice")), "equals");
        check(p.hashCode() == new com.sun.security.auth.NTUserPrincipal("alice").hashCode(), "hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
