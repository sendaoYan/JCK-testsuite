/*
 * @test
 * @summary java.security.KeyStore.Entry conformance: interface structure,
 *          default getAttributes (immutable empty set), the three concrete
 *          entry subtypes, and record-style fixtures through the interface.
 * @run main EntryTest
 */

public class EntryTest {
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
        Class<?> c = Class.forName("java.security.KeyStore$Entry");
        check(c.isInterface(), "is interface");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getAttributes"), "declares getAttributes");

        testContractDeep();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract-deepening section (appended) ----
    static void testContractDeep() throws Exception {
        Class<?> c = Class.forName("java.security.KeyStore$Entry");

        // interface declares exactly one method: the default getAttributes
        java.lang.reflect.Method[] own = c.getDeclaredMethods();
        check(own.length == 1, "exactly one declared method");
        check("getAttributes".equals(own[0].getName()), "sole method getAttributes");
        check(java.lang.reflect.Modifier.isPublic(own[0].getModifiers()), "getAttributes public");
        check(!java.lang.reflect.Modifier.isAbstract(own[0].getModifiers()),
                "getAttributes is a default method");

        // default method on a bare anonymous implementation returns an immutable empty set
        java.security.KeyStore.Entry bare = new java.security.KeyStore.Entry() { };
        java.util.Set<java.security.KeyStore.Entry.Attribute> attrs = bare.getAttributes();
        check(attrs != null, "default getAttributes non-null");
        check(attrs.isEmpty(), "default getAttributes empty");
        check(attrs.size() == 0, "default getAttributes size 0");
        checkThrows(UnsupportedOperationException.class,
                () -> attrs.add(null), "default attribute set immutable");

        // the three concrete entry subtypes all flow through the interface and
        // inherit the empty default attributes
        javax.crypto.SecretKey sk =
                new javax.crypto.spec.SecretKeySpec(new byte[16], "AES");
        java.security.KeyStore.Entry ske = new java.security.KeyStore.SecretKeyEntry(sk);
        java.security.KeyStore.Entry tce =
                new java.security.KeyStore.TrustedCertificateEntry(new FauxCertificate());
        java.security.KeyStore.Entry pke = new java.security.KeyStore.PrivateKeyEntry(
                new FauxPrivateKey(), new java.security.cert.Certificate[]{new FauxCertificate()});
        java.security.KeyStore.Entry[] entries = {ske, tce, pke};
        for (java.security.KeyStore.Entry e : entries) {
            check(c.isInstance(e), "concrete entry instanceof Entry");
            check(e.getAttributes() != null && e.getAttributes().isEmpty(),
                    "concrete entry default attributes empty: " + e.getClass().getSimpleName());
            check(e.toString() != null, "toString non-null: " + e.getClass().getSimpleName());
        }

        // PKCS12 in-memory entry loop through the interface type
        java.security.KeyStore ks = java.security.KeyStore.getInstance("PKCS12");
        ks.load(null, null);
        char[] pw = "pw".toCharArray();
        ks.setEntry("s", ske, new java.security.KeyStore.PasswordProtection(pw));
        java.security.KeyStore.Entry got = ks.getEntry("s",
                new java.security.KeyStore.PasswordProtection(pw));
        check(got instanceof java.security.KeyStore.SecretKeyEntry, "PKCS12 entry round-trip type");
        // PKCS12 adds its own attributes on round-trip: friendlyName (= alias) and localKeyId
        java.security.KeyStore.Entry.Attribute friendly = null;
        boolean hasLocalKeyId = false;
        for (java.security.KeyStore.Entry.Attribute at : got.getAttributes()) {
            if ("1.2.840.113549.1.9.20".equals(at.getName())) friendly = at;
            if ("1.2.840.113549.1.9.21".equals(at.getName())) hasLocalKeyId = true;
        }
        check(friendly != null && "s".equals(friendly.getValue()),
                "PKCS12 round-trip friendlyName equals alias");
        check(hasLocalKeyId, "PKCS12 round-trip localKeyId present");
        check(got.getAttributes().size() == 2, "PKCS12 round-trip attribute count");
        check(ks.getEntry("missing", null) == null, "missing alias yields null Entry");
    }

    // minimal fixtures (record-style implementations)
    static class FauxCertificate extends java.security.cert.Certificate {
        private final String keyAlg;
        FauxCertificate() { this("RSA"); }
        FauxCertificate(String keyAlg) { super("X.509"); this.keyAlg = keyAlg; }
        public byte[] getEncoded() { return new byte[]{1, 2, 3}; }
        public void verify(java.security.PublicKey key) { }
        public void verify(java.security.PublicKey key, String sigProvider) { }
        public String toString() { return "FauxCertificate"; }
        public java.security.PublicKey getPublicKey() { return new FauxPublicKey(keyAlg); }
    }

    static class FauxPublicKey implements java.security.PublicKey {
        private final String alg;
        FauxPublicKey(String alg) { this.alg = alg; }
        public String getAlgorithm() { return alg; }
        public String getFormat() { return "X.509"; }
        public byte[] getEncoded() { return new byte[]{4, 5, 6}; }
    }

    static class FauxPrivateKey implements java.security.PrivateKey {
        public String getAlgorithm() { return "RSA"; }
        public String getFormat() { return "PKCS#8"; }
        public byte[] getEncoded() { return new byte[]{1, 2, 3}; }
    }
}
