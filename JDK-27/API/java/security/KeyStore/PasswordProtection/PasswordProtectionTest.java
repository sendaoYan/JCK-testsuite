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
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract-deepening section (appended) ----
    static void testContractDeep() throws Exception {
        // 3-arg constructor null matrix: null algorithm rejected, null spec retained
        boolean npe = false;
        try { new KeyStore.PasswordProtection("pw".toCharArray(), null, null); }
        catch (NullPointerException e) { npe = true; }
        check(npe, "3-arg null protection algorithm NPE");
        KeyStore.PasswordProtection specLess =
                new KeyStore.PasswordProtection("pw".toCharArray(), "PBE", null);
        check("PBE".equals(specLess.getProtectionAlgorithm())
                && specLess.getProtectionParameters() == null,
                "PBE algorithm recorded, null spec retained");

        // destroy is idempotent and the destroyed state is stable
        KeyStore.PasswordProtection p = new KeyStore.PasswordProtection("x".toCharArray());
        p.destroy(); p.destroy();
        check(p.isDestroyed(), "double destroy stays destroyed");

        // subtyping: ProtectionParameter + Destroyable
        check(p instanceof KeyStore.ProtectionParameter, "implements ProtectionParameter");
        check(p instanceof javax.security.auth.Destroyable, "implements Destroyable");

        // identity equality (no equals override)
        KeyStore.PasswordProtection a = new KeyStore.PasswordProtection("dup".toCharArray());
        KeyStore.PasswordProtection b = new KeyStore.PasswordProtection("dup".toCharArray());
        check(a.equals(a) && !a.equals(b), "identity equality semantics");

        // empty password is a legitimate value
        KeyStore.PasswordProtection e = new KeyStore.PasswordProtection(new char[0]);
        check(e.getPassword() != null && e.getPassword().length == 0, "empty password length 0");

        // end-to-end: the password actually protects and recovers a keystore key
        char[] realPw = "real-pw".toCharArray();
        KeyStore.PasswordProtection prot = new KeyStore.PasswordProtection(realPw);
        KeyStore ks = KeyStore.getInstance("JCEKS");
        ks.load(null, null);
        javax.crypto.SecretKey aes = new javax.crypto.spec.SecretKeySpec(new byte[16], "AES");
        ks.setEntry("s", new KeyStore.SecretKeyEntry(aes), prot);
        check(ks.getEntry("s", prot) instanceof KeyStore.SecretKeyEntry,
                "protection password recovers entry");
        boolean unrecoverable = false;
        try { ks.getKey("s", "not-the-password".toCharArray()); }
        catch (java.security.UnrecoverableKeyException uke) { unrecoverable = true; }
        check(unrecoverable, "different password cannot recover key");
    }
}
