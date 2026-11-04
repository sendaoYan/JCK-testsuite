/*
 * @test
 * @summary com.sun.security.auth.SolarisNumericGroupPrincipal conformance: behavior of constructors, accessors and value semantics.
 * @run main SolarisNumericGroupPrincipalTest
 */

public class SolarisNumericGroupPrincipalTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        com.sun.security.auth.SolarisNumericGroupPrincipal p = new com.sun.security.auth.SolarisNumericGroupPrincipal(100L, true);
        check(p instanceof java.security.Principal, "implements java.security.Principal");
        check(p.longValue() == 100L, "longValue");
        check(p.isPrimaryGroup(), "isPrimaryGroup");
        check(p.getName() != null, "getName");
        check(p.equals(new com.sun.security.auth.SolarisNumericGroupPrincipal(100L, true)), "equals");
        check(p.hashCode() == new com.sun.security.auth.SolarisNumericGroupPrincipal(100L, true).hashCode(), "hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
