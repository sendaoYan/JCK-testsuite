/*
 * @test
 * @summary java.security.KeyStore.SecretKeyEntry conformance: constructor null
 *          matrix, getSecretKey round-trip, default getAttributes, toString
 *          algorithm mention, identity equality, and end-to-end JCEKS/PKCS12
 *          setEntry/getEntry + getKey loops (JKS rejection included).
 * @run main SecretKeyEntryTest
 */

public class SecretKeyEntryTest {
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
        javax.crypto.SecretKey sk = new javax.crypto.spec.SecretKeySpec(new byte[16], "AES");
        java.security.KeyStore.SecretKeyEntry e = new java.security.KeyStore.SecretKeyEntry(sk);
        check(sk.equals(e.getSecretKey()), "getSecretKey");

        testContractDeep();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract-deepening section (appended) ----
    static void testContractDeep() throws Exception {
        javax.crypto.SecretKey aes =
                new javax.crypto.spec.SecretKeySpec(new byte[16], "AES");

        // constructor null matrix
        checkThrows(NullPointerException.class,
                () -> new java.security.KeyStore.SecretKeyEntry(null),
                "null key NPE");

        // getters and interface defaults
        java.security.KeyStore.SecretKeyEntry entry =
                new java.security.KeyStore.SecretKeyEntry(aes);
        check(entry.getSecretKey() == aes, "getSecretKey identity round-trip");
        check(entry instanceof java.security.KeyStore.Entry, "implements Entry");
        check(entry.getAttributes() != null && entry.getAttributes().isEmpty(),
                "default attributes empty");
        check(entry.toString() != null && entry.toString().contains("AES"),
                "toString mentions algorithm");

        // identity equality (no equals/hashCode override)
        java.security.KeyStore.SecretKeyEntry same =
                new java.security.KeyStore.SecretKeyEntry(aes);
        java.security.KeyStore.SecretKeyEntry equal2 =
                new java.security.KeyStore.SecretKeyEntry(
                        new javax.crypto.spec.SecretKeySpec(new byte[16], "AES"));
        check(entry.equals(entry), "reflexive equals");
        check(!entry.equals(same), "same-key distinct instances unequal (identity equality)");
        check(!entry.equals(equal2), "value-equal keys still unequal (identity equality)");
        check(entry.hashCode() == entry.hashCode(), "hashCode stable");
        check(!entry.equals("x"), "not equal to unrelated type");

        // JKS rejects secret keys outright
        java.security.KeyStore jks = java.security.KeyStore.getInstance("JKS");
        jks.load(null, null);
        checkThrows(java.security.KeyStoreException.class,
                () -> jks.setKeyEntry("s", aes, "pw".toCharArray(), null),
                "JKS setKeyEntry SecretKey KeyStoreException");

        // JCEKS full loop: setEntry -> getEntry/getKey with encoded round-trip
        char[] pw = "pw".toCharArray();
        java.security.KeyStore jce = java.security.KeyStore.getInstance("JCEKS");
        jce.load(null, null);
        java.security.KeyStore.PasswordProtection prot =
                new java.security.KeyStore.PasswordProtection(pw);
        jce.setEntry("s", entry, prot);
        check(jce.isKeyEntry("s"), "JCEKS entry is key entry");
        check(!jce.isCertificateEntry("s"), "JCEKS entry not cert entry");
        java.security.KeyStore.Entry back = jce.getEntry("s", prot);
        check(back instanceof java.security.KeyStore.SecretKeyEntry,
                "JCEKS getEntry type round-trip");
        check(java.util.Arrays.equals(
                ((java.security.KeyStore.SecretKeyEntry) back).getSecretKey().getEncoded(),
                aes.getEncoded()), "JCEKS key bytes round-trip");
        java.security.Key viaGetKey = jce.getKey("s", pw);
        check(viaGetKey instanceof javax.crypto.SecretKey, "JCEKS getKey SecretKey");
        check("AES".equals(viaGetKey.getAlgorithm()), "JCEKS getKey algorithm AES");
        check(jce.size() == 1 && jce.containsAlias("s"), "JCEKS size/containsAlias");
        check(jce.getCertificateChain("s") == null, "JCEKS secret entry null chain");

        // wrong password cannot recover
        checkThrows(java.security.UnrecoverableKeyException.class,
                () -> jce.getKey("s", "wrong".toCharArray()),
                "JCEKS wrong password UnrecoverableKeyException");

        // PKCS12 loop (PKCS12 stores secret keys natively since JDK-8)
        java.security.KeyStore p12 = java.security.KeyStore.getInstance("PKCS12");
        p12.load(null, null);
        p12.setEntry("s", entry, prot);
        check(p12.getKey("s", pw) instanceof javax.crypto.SecretKey,
                "PKCS12 secret key loop");
        check(p12.getEntry("s", prot) instanceof java.security.KeyStore.SecretKeyEntry,
                "PKCS12 secret entry loop");

        // deleteEntry removes the secret
        jce.deleteEntry("s");
        check(!jce.containsAlias("s") && jce.size() == 0, "deleteEntry removes secret");
        check(jce.getEntry("s", prot) == null, "getEntry after delete null");
    }
}
