/*
 * @test
 * @summary com.sun.security.auth.SolarisPrincipal conformance: behavior of constructors, accessors and value semantics.
 * @run main SolarisPrincipalTest
 */

public class SolarisPrincipalTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        com.sun.security.auth.SolarisPrincipal p = new com.sun.security.auth.SolarisPrincipal("soluser");
        check(p instanceof java.security.Principal, "implements java.security.Principal");
        check("soluser".equals(p.getName()), "getName");
        check(p.toString() != null, "toString");
        check(p.equals(new com.sun.security.auth.SolarisPrincipal("soluser")), "equals");
        check(p.hashCode() == new com.sun.security.auth.SolarisPrincipal("soluser").hashCode(), "hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
