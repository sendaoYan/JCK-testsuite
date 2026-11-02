/*
 * @test
 * @summary javax.security.auth.kerberos.EncryptionKey conformance: kerberos encryption key accessors and destroy.
 * @run main EncryptionKeyTest
 */

import javax.security.auth.kerberos.*;

public class EncryptionKeyTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        EncryptionKey ek = new EncryptionKey(new byte[16], 17);
        check(ek.getKeyType() == 17, "getKeyType");
        check("RAW".equals(ek.getFormat()), "getFormat RAW");
        check(ek.getEncoded().length == 16, "getEncoded length");
        check(ek.getAlgorithm() != null, "getAlgorithm non-null");
        check(!ek.isDestroyed(), "not destroyed initially");
        ek.destroy();
        check(ek.isDestroyed(), "destroyed");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
