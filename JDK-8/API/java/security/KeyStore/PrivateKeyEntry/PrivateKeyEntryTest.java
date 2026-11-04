/*
 * @test
 * @summary java.security.KeyStore.PrivateKeyEntry conformance: constructor
 *          matrix (null key/chain, zero-length chain, mixed cert types IAE,
 *          key-vs-cert algorithm mismatch IAE), getter round-trips, defensive
 *          copy of the chain, identity equality, default attributes, and an
 *          end-to-end JKS setEntry/getEntry loop with a real RSA key+cert.
 * @run main PrivateKeyEntryTest
 */

public class PrivateKeyEntryTest {
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
        Class<?> c = Class.forName("java.security.KeyStore$PrivateKeyEntry");
        check(!c.isInterface(), "is class");
        check(java.security.KeyStore.Entry.class.isAssignableFrom(c), "implements KeyStore.Entry");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getPrivateKey"), "declares getPrivateKey");
        check(ms.contains("getCertificateChain"), "declares getCertificateChain");

        testContractDeep();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract-deepening section (appended) ----
    static void testContractDeep() throws Exception {
        // real RSA-512 key pair + fixed X.509 certificate for the store loop
        java.security.KeyPairGenerator kpg = java.security.KeyPairGenerator.getInstance("RSA");
        kpg.initialize(512);
        java.security.KeyPair kp = kpg.generateKeyPair();
        java.security.PrivateKey priv = kp.getPrivate();
        java.security.cert.Certificate cert = parseCert();
        java.security.cert.Certificate cert2 = parseCert();

        // constructor null/empty matrix
        checkThrows(NullPointerException.class,
                () -> new java.security.KeyStore.PrivateKeyEntry(null,
                        new java.security.cert.Certificate[]{cert}),
                "null key NPE");
        checkThrows(NullPointerException.class,
                () -> new java.security.KeyStore.PrivateKeyEntry(priv, null),
                "null chain NPE");
        checkThrows(IllegalArgumentException.class,
                () -> new java.security.KeyStore.PrivateKeyEntry(priv,
                        new java.security.cert.Certificate[0]),
                "zero-length chain IAE");
        checkThrows(NullPointerException.class,
                () -> new java.security.KeyStore.PrivateKeyEntry(priv,
                        new java.security.cert.Certificate[]{null}),
                "null chain element NPE");
        // mixed certificate types in one chain -> IAE (cert.getType() mismatch)
        checkThrows(IllegalArgumentException.class,
                () -> new java.security.KeyStore.PrivateKeyEntry(new FauxPrivateKey("RSA"),
                        new java.security.cert.Certificate[]{cert, new FauxCert("OTHER", "RSA")}),
                "mixed cert types IAE");
        // private key algorithm vs end-entity cert public key mismatch -> IAE
        checkThrows(IllegalArgumentException.class,
                () -> new java.security.KeyStore.PrivateKeyEntry(new FauxPrivateKey("DSA"),
                        new java.security.cert.Certificate[]{cert}),
                "key/cert algorithm mismatch IAE");

        // getter round-trips
        java.security.KeyStore.PrivateKeyEntry entry =
                new java.security.KeyStore.PrivateKeyEntry(priv,
                        new java.security.cert.Certificate[]{cert, cert2});
        check(entry.getPrivateKey().equals(priv), "getPrivateKey equals");
        check(entry.getCertificateChain().length == 2, "chain length 2");
        check(cert.equals(entry.getCertificateChain()[0])
                && cert2.equals(entry.getCertificateChain()[1]), "chain elements round-trip");
        check(cert.equals(entry.getCertificate()), "getCertificate is chain[0]");

        // defensive copy: mutating the returned chain does not corrupt the entry
        java.security.cert.Certificate[] leaked = entry.getCertificateChain();
        leaked[0] = null;
        leaked[1] = null;
        check(entry.getCertificateChain()[0] != null
                && entry.getCertificateChain()[1] != null, "chain defensive copy");
        // ... and mutating the constructor-supplied array afterwards is invisible
        java.security.cert.Certificate[] src =
                new java.security.cert.Certificate[]{cert, cert2};
        java.security.KeyStore.PrivateKeyEntry entry2 =
                new java.security.KeyStore.PrivateKeyEntry(priv, src);
        src[0] = null;
        check(entry2.getCertificateChain()[0] != null, "construct-time chain copy");

        // interface defaults + toString
        check(entry instanceof java.security.KeyStore.Entry, "implements Entry");
        check(entry.getAttributes() != null && entry.getAttributes().isEmpty(),
                "default attributes empty");
        check(entry.toString() != null, "toString non-null");

        // identity equality (no equals/hashCode override)
        java.security.KeyStore.PrivateKeyEntry same =
                new java.security.KeyStore.PrivateKeyEntry(priv,
                        new java.security.cert.Certificate[]{cert, cert2});
        check(entry.equals(entry), "reflexive equals");
        check(!entry.equals(same), "same-content distinct instances unequal");
        check(!entry.equals("x"), "not equal to unrelated type");

        // end-to-end JKS loop: setEntry -> getEntry/getKey with cross-instance equals
        char[] pw = "pw".toCharArray();
        java.security.KeyStore jks = java.security.KeyStore.getInstance("JKS");
        jks.load(null, null);
        java.security.KeyStore.PasswordProtection prot =
                new java.security.KeyStore.PasswordProtection(pw);
        jks.setEntry("k", entry, prot);
        check(jks.isKeyEntry("k"), "JKS entry is key entry");
        java.security.KeyStore.Entry back = jks.getEntry("k", prot);
        check(back instanceof java.security.KeyStore.PrivateKeyEntry,
                "JKS getEntry type round-trip");
        java.security.KeyStore.PrivateKeyEntry pke =
                (java.security.KeyStore.PrivateKeyEntry) back;
        check(pke.getPrivateKey().equals(priv), "round-trip key cross-instance equals");
        check(pke.getCertificateChain().length == 2, "round-trip chain length");
        check(cert.equals(pke.getCertificateChain()[0])
                && cert2.equals(pke.getCertificateChain()[1]),
                "round-trip chain elements equal");
        check(jks.getKey("k", pw).equals(priv), "getKey equals original");
        checkThrows(java.security.UnrecoverableKeyException.class,
                () -> jks.getKey("k", "wrong".toCharArray()),
                "JKS wrong password UnrecoverableKeyException");
    }

    // fixed self-signed X.509 certificate (RSA 2048) for deterministic parsing
    static final String CERT_B64 =
        "MIIDhzCCAm+gAwIBAgIEDxeuOjANBgkqhkiG9w0BAQsFADB0MQswCQYDVQQGEwJVUzELMAkG" +
        "A1UECBMCSUwxFDASBgNVBAcTC1NwcmluZ2ZpZWxkMRQwEgYDVQQKEwtFeGFtcGxlIE9yZzEU" +
        "MBIGA1UECxMLQ29uZm9ybWFuY2UxFjAUBgNVBAMTDUpDSyBUZXN0IENlcnQwHhcNMjYxMTA0" +
        "MDkzNzI4WhcNMzYxMTAxMDkzNzI4WjB0MQswCQYDVQQGEwJVUzELMAkGA1UECBMCSUwxFDAS" +
        "BgNVBAcTC1NwcmluZ2ZpZWxkMRQwEgYDVQQKEwtFeGFtcGxlIE9yZzEUMBIGA1UECxMLQ29u" +
        "Zm9ybWFuY2UxFjAUBgNVBAMTDUpDSyBUZXN0IENlcnQwggEiMA0GCSqGSIb3DQEBAQUAA4IB" +
        "DwAwggEKAoIBAQDAImuPu7xdlUkyW0Q8Y75ek2oybshN0xF3MxmxqiUWJruiM+U2+Ema1DA7" +
        "lSquiCNGbzmHhWpk8mK2rjEFu++avlIG5mnr4XQdiQe5o4q2IIuN3LKdvfauwmT5WoxUTdCa" +
        "FxtBDdAPsfckCr8C/ypWwJUdK4uY4MFgrVWg5NAIIG8xVmGONYIpE25fxQ7O55CM23CXsJ3i" +
        "U5+GpTBFaDr8/LJ2eKQEx35voV3iyWa9bKdsys4kg1sQgTCLv2kvRFU9QXmcLwuhP0tEUGf7" +
        "fann26h5FrL5fsj4UwT+djO42tNHTDN5KGgeoinVKYlUcfEYWEyek9a95hFdn8I/30fLAgMB" +
        "AAGjITAfMB0GA1UdDgQWBBTo72jITqQdGMA1Fxl/lTqG24M1VTANBgkqhkiG9w0BAQsFAAOC" +
        "AQEAnofWJt83h4XkRfD6ejfnYJeMXZ+OQY4J4/cHa6jSTgXPrKI8GQtZD8QF0jlb/2rjEKQ3" +
        "3YW8r9lci6p1po5bIMqbToGbzoKFK2x16ggsx37pxwIWqdp9aFIWJvQFXrK8vwzEGFc518wx" +
        "jhOLKE7BbYzeCSiLjP92JbEWUw6Ae4eCEpxMyZKWCw/iizv2zRv3iH5uobZwZ+aUiQUgjTXR" +
        "M0exJXFvVfXV9EY8MG9PMbWNjkusz66NIyUKoupRvl9rTnjNv1LlggVSAwW5uXzNCh/sUSMm" +
        "jVmmYMwYpDTG1Uz54HYKL7ZNhkDUj0IzSM5PI0ut8jdnMJOOWXr6tg0GPg==";

    static java.security.cert.Certificate parseCert() throws Exception {
        java.security.cert.CertificateFactory cf =
                java.security.cert.CertificateFactory.getInstance("X.509");
        return cf.generateCertificate(new java.io.ByteArrayInputStream(
                java.util.Base64.getDecoder().decode(CERT_B64)));
    }

    // fixtures for constructor validation paths
    static class FauxPrivateKey implements java.security.PrivateKey {
        private final String alg;
        FauxPrivateKey(String alg) { this.alg = alg; }
        public String getAlgorithm() { return alg; }
        public String getFormat() { return "PKCS#8"; }
        public byte[] getEncoded() { return new byte[]{1, 2, 3}; }
    }

    static class FauxCert extends java.security.cert.Certificate {
        private final String keyAlg;
        FauxCert(String type, String keyAlg) { super(type); this.keyAlg = keyAlg; }
        public byte[] getEncoded() { return new byte[]{1, 2, 3}; }
        public void verify(java.security.PublicKey key) { }
        public void verify(java.security.PublicKey key, String sigProvider) { }
        public String toString() { return "FauxCert"; }
        public java.security.PublicKey getPublicKey() { return new FauxKey(keyAlg); }
    }

    static class FauxKey implements java.security.PublicKey {
        private final String alg;
        FauxKey(String alg) { this.alg = alg; }
        public String getAlgorithm() { return alg; }
        public String getFormat() { return "X.509"; }
        public byte[] getEncoded() { return new byte[]{4, 5, 6}; }
    }
}
