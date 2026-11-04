/*
 * @test
 * @summary java.security.KeyStore.PasswordProtection conformance: defensive
 *          copy on construct, no clone on read, destroy lifecycle (incl.
 *          IllegalStateException on read and idempotent destroy), null
 *          password, 3-arg constructor null matrix, ProtectionParameter and
 *          Destroyable subtyping, and end-to-end keystore password service.
 * @run main PasswordProtectionTest
 */

public class PasswordProtectionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
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

        testContractDeep();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract-deepening section (appended) ----
    static void testContractDeep() throws Exception {
        // defensive copy on construct: mutating the source array is not visible
        char[] src = "deep-secret".toCharArray();
        java.security.KeyStore.PasswordProtection pp =
                new java.security.KeyStore.PasswordProtection(src);
        src[0] = 'X';
        check("deep-secret".equals(new String(pp.getPassword())),
                "defensive copy on construct");

        // no clone on read: repeated getPassword returns the internal array
        char[] read1 = pp.getPassword();
        check(read1 == pp.getPassword(), "getPassword returns internal array (no clone on read)");

        // destroy lifecycle
        check(!pp.isDestroyed(), "not destroyed initially");
        pp.destroy();
        check(pp.isDestroyed(), "destroyed");
        checkThrows(IllegalStateException.class, () -> pp.getPassword(),
                "getPassword after destroy IllegalStateException");
        pp.destroy();
        check(pp.isDestroyed(), "double destroy stays destroyed");

        // null password accepted and observable
        java.security.KeyStore.PasswordProtection nullPp =
                new java.security.KeyStore.PasswordProtection(null);
        check(nullPp.getPassword() == null, "null password retained");
        check(!nullPp.isDestroyed(), "null password not destroyed");
        checkThrows(IllegalStateException.class, () -> {
            nullPp.destroy();
            nullPp.getPassword();
        }, "null password destroy then read ISE");

        // empty password is a legitimate value
        java.security.KeyStore.PasswordProtection emptyPp =
                new java.security.KeyStore.PasswordProtection(new char[0]);
        check(emptyPp.getPassword() != null && emptyPp.getPassword().length == 0,
                "empty password length 0");

        // 3-arg constructor: null algorithm rejected, null spec retained
        checkThrows(NullPointerException.class,
                () -> new java.security.KeyStore.PasswordProtection(
                        "pw".toCharArray(), null, null),
                "3-arg null protection algorithm NPE");
        java.security.KeyStore.PasswordProtection specLess =
                new java.security.KeyStore.PasswordProtection("pw".toCharArray(), "PBE", null);
        check("PBE".equals(specLess.getProtectionAlgorithm()), "PBE algorithm recorded");
        check(specLess.getProtectionParameters() == null, "null protection spec retained");
        javax.crypto.spec.PBEParameterSpec pbeSpec =
                new javax.crypto.spec.PBEParameterSpec(new byte[8], 100);
        java.security.KeyStore.PasswordProtection specFull =
                new java.security.KeyStore.PasswordProtection("pw".toCharArray(), "PBE", pbeSpec);
        check(specFull.getProtectionParameters() == pbeSpec,
                "protection spec identity round-trip");
        check(specFull.getProtectionAlgorithm() == null ? false : true,
                "algorithm non-null when specified");
        check(!specFull.isDestroyed(), "3-arg not destroyed");

        // subtyping: ProtectionParameter + Destroyable
        check(pp instanceof java.security.KeyStore.ProtectionParameter,
                "implements ProtectionParameter");
        check(pp instanceof javax.security.auth.Destroyable,
                "implements Destroyable");

        // identity equality (no equals override)
        java.security.KeyStore.PasswordProtection other =
                new java.security.KeyStore.PasswordProtection("deep-secret".toCharArray());
        check(pp.equals(pp), "reflexive equals");
        check(!pp.equals(other), "same-content distinct instances unequal");

        // end-to-end: the password actually protects and recovers a keystore key
        char[] realPw = "real-pw".toCharArray();
        java.security.KeyStore.PasswordProtection prot =
                new java.security.KeyStore.PasswordProtection(realPw);
        java.security.KeyStore ks = java.security.KeyStore.getInstance("JCEKS");
        ks.load(null, null);
        javax.crypto.SecretKey aes =
                new javax.crypto.spec.SecretKeySpec(new byte[16], "AES");
        ks.setEntry("s", new java.security.KeyStore.SecretKeyEntry(aes), prot);
        java.security.KeyStore.Entry back = ks.getEntry("s", prot);
        check(back instanceof java.security.KeyStore.SecretKeyEntry,
                "protection password recovers entry");
        java.security.Key wrong = null;
        boolean unrecoverable = false;
        try {
            wrong = ks.getKey("s", "not-the-password".toCharArray());
        } catch (java.security.UnrecoverableKeyException uke) {
            unrecoverable = true;
        }
        check(unrecoverable, "different password cannot recover key");
    }
}
