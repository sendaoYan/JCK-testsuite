/*
 * @test
 * @summary java.security.KeyStore.SecretKeyEntry conformance: constructor and accessors.
 * @run main SecretKeyEntryTest
 */

import java.security.KeyStore;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class SecretKeyEntryTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        SecretKey sk = KeyGenerator.getInstance("AES").generateKey();
        KeyStore.SecretKeyEntry e = new KeyStore.SecretKeyEntry(sk);
        check(sk.equals(e.getSecretKey()), "getSecretKey");
        check(e.getAttributes().isEmpty(), "getAttributes empty");
        check(e instanceof KeyStore.Entry, "implements Entry");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
