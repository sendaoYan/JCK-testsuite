/*
 * @test
 * @summary javax.security.auth.kerberos.KerberosPrincipal conformance: kerberos principal name and realm.
 * @run main KerberosPrincipalTest
 */

import javax.security.auth.kerberos.*;

public class KerberosPrincipalTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        check(KerberosPrincipal.KRB_NT_PRINCIPAL == 1, "KRB_NT_PRINCIPAL 1");
        KerberosPrincipal p = new KerberosPrincipal("alice@EXAMPLE.COM");
        check("alice@EXAMPLE.COM".equals(p.getName()), "getName");
        check("EXAMPLE.COM".equals(p.getRealm()), "getRealm");
        check(p.getNameType() == KerberosPrincipal.KRB_NT_PRINCIPAL, "getNameType default");
        check(p.equals(new KerberosPrincipal("alice@EXAMPLE.COM")), "equals");
        check(p.hashCode() == new KerberosPrincipal("alice@EXAMPLE.COM").hashCode(), "hashCode");
        check(p.toString().contains("alice@EXAMPLE.COM"), "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
