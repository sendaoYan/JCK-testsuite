/*
 * @test
 * @summary com.sun.security.auth.LdapPrincipal conformance: behavior of constructors, accessors and value semantics.
 * @run main LdapPrincipalTest
 */

public class LdapPrincipalTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        com.sun.security.auth.LdapPrincipal p = new com.sun.security.auth.LdapPrincipal("cn=alice");
        check(p instanceof java.security.Principal, "implements java.security.Principal");
        check(p.getName() != null, "getName");
        check(p.toString() != null, "toString");
        check(p.equals(new com.sun.security.auth.LdapPrincipal("cn=alice")), "equals");
        check(p.hashCode() == new com.sun.security.auth.LdapPrincipal("cn=alice").hashCode(), "hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
