/*
 * @test
 * @summary java.security.KeyStore.PasswordProtection conformance: getPassword/destroy and algorithm variant.
 * @run main PasswordProtectionTest
 */

public class PasswordProtectionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        char[] pw = "secret".toCharArray();
        java.security.KeyStore.PasswordProtection p = new java.security.KeyStore.PasswordProtection(pw);
        check(java.util.Arrays.equals(pw, p.getPassword()), "getPassword");
        check(!p.isDestroyed(), "not destroyed");
        p.destroy();
        check(p.isDestroyed(), "destroyed");
        java.security.KeyStore.PasswordProtection p2 = new java.security.KeyStore.PasswordProtection(
            pw, "PBE", new javax.crypto.spec.PBEParameterSpec(new byte[8], 100));
        check("PBE".equals(p2.getProtectionAlgorithm()), "getProtectionAlgorithm");
        check(p2.getProtectionParameters() != null, "getProtectionParameters");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
