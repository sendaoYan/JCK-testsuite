/*
 * @test
 * @summary java.security.KeyStore conformance: PKCS12 load, SecretKeyEntry round trip, getDefaultType.
 * @run main KeyStoreTest
 */

import java.security.KeyStore;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class KeyStoreTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        KeyStore ks = KeyStore.getInstance("PKCS12");
        check("PKCS12".equals(ks.getType()), "getType");
        check(ks.getProvider() != null, "getProvider");
        ks.load(null, null);
        check(ks.size() == 0, "size 0 after load(null,null)");
        check(!ks.aliases().hasMoreElements(), "no aliases after load");
        check(KeyStore.getDefaultType() != null, "getDefaultType non-null");

        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(128);
        SecretKey sk = kg.generateKey();
        KeyStore.SecretKeyEntry ske = new KeyStore.SecretKeyEntry(sk);
        KeyStore.PasswordProtection pp = new KeyStore.PasswordProtection("pw".toCharArray());
        ks.setEntry("secret", ske, pp);
        check(ks.containsAlias("secret"), "containsAlias");
        check(ks.isKeyEntry("secret"), "isKeyEntry");
        KeyStore.Entry got = ks.getEntry("secret", pp);
        check(got instanceof KeyStore.SecretKeyEntry, "getEntry SecretKeyEntry");
        check(ks.getCreationDate("secret") != null, "getCreationDate non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
