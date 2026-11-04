/*
 * @test
 * @summary javax.security.auth.kerberos.KerberosPrincipal conformance: constructors and getters.
 * @run main KerberosPrincipalTest
 */

public class KerberosPrincipalTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.security.auth.kerberos.KerberosPrincipal p = new javax.security.auth.kerberos.KerberosPrincipal("user@REALM.COM");
        check("user@REALM.COM".equals(p.getName()), "getName");
        check("REALM.COM".equals(p.getRealm()), "getRealm");
        javax.security.auth.kerberos.KerberosPrincipal p2 = new javax.security.auth.kerberos.KerberosPrincipal(
            "user@REALM.COM", javax.security.auth.kerberos.KerberosPrincipal.KRB_NT_PRINCIPAL);
        check(p2.getNameType() == javax.security.auth.kerberos.KerberosPrincipal.KRB_NT_PRINCIPAL, "getNameType");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
