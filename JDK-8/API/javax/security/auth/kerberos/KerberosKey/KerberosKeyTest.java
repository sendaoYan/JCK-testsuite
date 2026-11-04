/*
 * @test
 * @summary javax.security.auth.kerberos.KerberosKey conformance: constructor, getters, destroy.
 * @run main KerberosKeyTest
 */

public class KerberosKeyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.security.auth.kerberos.KerberosPrincipal princ = new javax.security.auth.kerberos.KerberosPrincipal("u@REALM");
        javax.security.auth.kerberos.KerberosKey k = new javax.security.auth.kerberos.KerberosKey(princ, new byte[16], 18, 1);
        check(princ.equals(k.getPrincipal()), "getPrincipal");
        check(k.getKeyType() == 18, "getKeyType");
        check(k.getVersionNumber() == 1, "getVersionNumber");
        check(k.getEncoded() != null, "getEncoded");
        check(k instanceof javax.crypto.SecretKey, "is SecretKey");
        check(!k.isDestroyed(), "not destroyed");
        k.destroy();
        check(k.isDestroyed(), "destroyed");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
