/*
 * @test
 * @summary java.security.KeyStore.Builder conformance: newInstance and getKeyStore.
 * @run main BuilderTest
 */

import java.security.KeyStore;

public class BuilderTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);
        KeyStore.ProtectionParameter pp = new KeyStore.PasswordProtection("pw".toCharArray());
        KeyStore.Builder b = KeyStore.Builder.newInstance(ks, pp);
        check(ks == b.getKeyStore(), "getKeyStore");
        check(b.getProtectionParameter("alias") == pp, "getProtectionParameter");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
