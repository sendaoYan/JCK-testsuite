/*
 * @test
 * @summary javax.security.auth.kerberos.KerberosKey conformance: kerberos key accessors.
 * @run main KerberosKeyTest
 */

import javax.security.auth.kerberos.*;

public class KerberosKeyTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        KerberosPrincipal p = new KerberosPrincipal("alice@EXAMPLE.COM");
        KerberosKey kk = new KerberosKey(p, new byte[16], 17, 3);
        check("alice@EXAMPLE.COM".equals(kk.getPrincipal().getName()), "getPrincipal");
        check(kk.getVersionNumber() == 3, "getVersionNumber");
        check(kk.getKeyType() == 17, "getKeyType");
        check("RAW".equals(kk.getFormat()), "getFormat RAW");
        check(kk.getEncoded().length == 16, "getEncoded length");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
