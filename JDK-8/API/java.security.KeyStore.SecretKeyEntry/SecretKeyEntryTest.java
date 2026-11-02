/*
 * @test
 * @summary java.security.KeyStore.SecretKeyEntry conformance: constructor and getSecretKey.
 * @run main SecretKeyEntryTest
 */

public class SecretKeyEntryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.crypto.SecretKey sk = new javax.crypto.spec.SecretKeySpec(new byte[16], "AES");
        java.security.KeyStore.SecretKeyEntry e = new java.security.KeyStore.SecretKeyEntry(sk);
        check(sk.equals(e.getSecretKey()), "getSecretKey");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
