/*
 * @test
 * @summary java.security.KeyStore.PasswordProtection conformance: white-box structure + black-box behavior.
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
    static void testWhiteBox() throws Exception {
        char[] pw = "secret".toCharArray();
        KeyStore.PasswordProtection p = new KeyStore.PasswordProtection(pw);
        check(Arrays.equals(pw, p.getPassword()), "getPassword");
        check(p.getProtectionAlgorithm() == null, "getProtectionAlgorithm null default");
        check(!p.isDestroyed(), "isDestroyed false");
        p.destroy();
        check(p.isDestroyed(), "isDestroyed true after destroy");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        // password protection: array is cloned in and out, destroyable
        char[] pw = "jck-secret".toCharArray();
        KeyStore.PasswordProtection pp = new KeyStore.PasswordProtection(pw);
        check(new String(pp.getPassword()).equals("jck-secret"), "getPassword");
        pw[0] = 'X';
        check(new String(pp.getPassword()).equals("jck-secret"), "defensive copy on construct");
        // getPassword returns the internal array: caller mutation is visible (no clone on read)
        char[] leaked = pp.getPassword();
        char saved = leaked[0];
        leaked[0] = 'Y';
        check(new String(pp.getPassword()).startsWith("Y"), "no clone on read");
        leaked[0] = saved;
        check(pp instanceof KeyStore.ProtectionParameter, "is ProtectionParameter");
        // protection algorithm accessors (no algorithm by default)
        check(pp.getProtectionAlgorithm() == null, "no protection algorithm");
        check(pp.getProtectionParameters() == null, "no protection params");
        // 3-arg constructor records the algorithm
        KeyStore.PasswordProtection pp3 = new KeyStore.PasswordProtection(
            "pw".toCharArray(), "PBEWithHmacSHA256AndAES_128", null);
        check(pp3.getProtectionAlgorithm().equals("PBEWithHmacSHA256AndAES_128"), "algorithm recorded");
        check(pp3.getProtectionParameters() == null, "null params retained");
        // destroy
        check(!pp.isDestroyed(), "not destroyed initially");
        pp.destroy();
        check(pp.isDestroyed(), "destroyed");
        try {
            pp.getPassword();
            check(false, "destroyed read throws");
        } catch (IllegalStateException ise) {
            check(true, "destroyed read IllegalStateException");
        }
        // null password accepted
        KeyStore.PasswordProtection np = new KeyStore.PasswordProtection(null);
        check(np.getPassword() == null, "null password retained");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
