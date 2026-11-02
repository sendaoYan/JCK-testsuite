/*
 * @test
 * @summary java.security.KeyStore.PasswordProtection conformance: constructor and accessors.
 * @run main PasswordProtectionTest
 */

import java.security.KeyStore;
import java.util.Arrays;

public class PasswordProtectionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        char[] pw = "secret".toCharArray();
        KeyStore.PasswordProtection p = new KeyStore.PasswordProtection(pw);
        check(Arrays.equals(pw, p.getPassword()), "getPassword");
        check(p.getProtectionAlgorithm() == null, "getProtectionAlgorithm null default");
        check(!p.isDestroyed(), "isDestroyed false");
        p.destroy();
        check(p.isDestroyed(), "isDestroyed true after destroy");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
