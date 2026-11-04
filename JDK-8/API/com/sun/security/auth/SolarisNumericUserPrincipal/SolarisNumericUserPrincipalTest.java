/*
 * @test
 * @summary com.sun.security.auth.SolarisNumericUserPrincipal conformance: behavior of constructors, accessors and value semantics.
 * @run main SolarisNumericUserPrincipalTest
 */

public class SolarisNumericUserPrincipalTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        com.sun.security.auth.SolarisNumericUserPrincipal p = new com.sun.security.auth.SolarisNumericUserPrincipal(501L);
        check(p instanceof java.security.Principal, "implements java.security.Principal");
        check(p.longValue() == 501L, "longValue");
        check(p.getName() != null, "getName");
        check(p.equals(new com.sun.security.auth.SolarisNumericUserPrincipal(501L)), "equals");
        check(p.hashCode() == new com.sun.security.auth.SolarisNumericUserPrincipal(501L).hashCode(), "hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
