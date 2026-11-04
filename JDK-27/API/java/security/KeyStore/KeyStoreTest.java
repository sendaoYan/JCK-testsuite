/*
 * @test
 * @summary java.security.KeyStore conformance: full contract coverage.
 *          getInstance variants + exact exceptions (KeyStoreException /
 *          NullPointerException / IllegalArgumentException / NoSuchProviderException),
 *          load(null,null) empty keystore semantics, setCertificateEntry/getCertificate/
 *          aliases/containsAlias/isCertificateEntry/isKeyEntry/getCreationDate/deleteEntry
 *          (incl. no-op delete of missing alias), store/load round-trip, store(null)
 *          -> NPE, and getType/getProvider/getDefaultType.
 * @run main KeyStoreTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

public class KeyStoreTest {
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

    // A fixed self-signed X.509 certificate (RSA 2048, SHA256withRSA) for deterministic parsing.
    static final String CERT_B64 =
        "MIIDhzCCAm+gAwIBAgIEDxeuOjANBgkqhkiG9w0BAQsFADB0MQswCQYDVQQGEwJV" +
        "UzELMAkGA1UECBMCSUwxFDASBgNVBAcTC1NwcmluZ2ZpZWxkMRQwEgYDVQQKEwtF" +
        "eGFtcGxlIE9yZzEUMBIGA1UECxMLQ29uZm9ybWFuY2UxFjAUBgNVBAMTDUpDSyBU" +
        "ZXN0IENlcnQwHhcNMjYxMTA0MDkzNzI4WhcNMzYxMTAxMDkzNzI4WjB0MQswCQYD" +
        "VQQGEwJVUzELMAkGA1UECBMCSUwxFDASBgNVBAcTC1NwcmluZ2ZpZWxkMRQwEgYD" +
        "VQQKEwtFeGFtcGxlIE9yZzEUMBIGA1UECxMLQ29uZm9ybWFuY2UxFjAUBgNVBAMT" +
        "DUpDSyBUZXN0IENlcnQwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDA" +
        "ImuPu7xdlUkyW0Q8Y75ek2oybshN0xF3MxmxqiUWJruiM+U2+Ema1DA7lSquiCNG" +
        "bzmHhWpk8mK2rjEFu++avlIG5mnr4XQdiQe5o4q2IIuN3LKdvfauwmT5WoxUTdCa" +
        "FxtBDdAPsfckCr8C/ypWwJUdK4uY4MFgrVWg5NAIIG8xVmGONYIpE25fxQ7O55CM" +
        "23CXsJ3iU5+GpTBFaDr8/LJ2eKQEx35voV3iyWa9bKdsys4kg1sQgTCLv2kvRFU9" +
        "QXmcLwuhP0tEUGf7fann26h5FrL5fsj4UwT+djO42tNHTDN5KGgeoinVKYlUcfEY" +
        "WEyek9a95hFdn8I/30fLAgMBAAGjITAfMB0GA1UdDgQWBBTo72jITqQdGMA1Fxl/" +
        "lTqG24M1VTANBgkqhkiG9w0BAQsFAAOCAQEAnofWJt83h4XkRfD6ejfnYJeMXZ+O" +
        "QY4J4/cHa6jSTgXPrKI8GQtZD8QF0jlb/2rjEKQ33YW8r9lci6p1po5bIMqbToGb" +
        "zoKFK2x16ggsx37pxwIWqdp9aFIWJvQFXrK8vwzEGFc518wxjhOLKE7BbYzeCSiL" +
        "jP92JbEWUw6Ae4eCEpxMyZKWCw/iizv2zRv3iH5uobZwZ+aUiQUgjTXRM0exJXFv" +
        "VfXV9EY8MG9PMbWNjkusz66NIyUKoupRvl9rTnjNv1LlggVSAwW5uXzNCh/sUSMm" +
        "jVmmYMwYpDTG1Uz54HYKL7ZNhkDUj0IzSM5PI0ut8jdnMJOOWXr6tg0GPg==";

    static Certificate parseCert() throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return cf.generateCertificate(new ByteArrayInputStream(Base64.getDecoder().decode(CERT_B64)));
    }

    public static void main(String[] args) throws Exception {
        // ---- factory + exception matrix ----
        checkThrows(KeyStoreException.class,
                () -> KeyStore.getInstance("NoSuchKS_XYZ"), "getInstance(bogus)");
        checkThrows(NullPointerException.class,
                () -> KeyStore.getInstance(null), "getInstance(null)");
        checkThrows(NullPointerException.class,
                () -> KeyStore.getInstance(null, Security.getProvider("SUN")),
                "getInstance(null, Provider)");
        checkThrows(IllegalArgumentException.class,
                () -> KeyStore.getInstance("PKCS12", (Provider) null),
                "getInstance(type, null Provider)");
        checkThrows(IllegalArgumentException.class,
                () -> KeyStore.getInstance("PKCS12", (String) null),
                "getInstance(type, null String)");
        checkThrows(NoSuchProviderException.class,
                () -> KeyStore.getInstance("PKCS12", "NoSuchProvider_XYZ"),
                "getInstance(type, bogus provider)");

        // ---- default type non-empty (JDK-8: jks, JDK-27: pkcs12) ----
        String defType = KeyStore.getDefaultType();
        check(defType != null && defType.length() > 0, "getDefaultType non-empty");

        // ---- empty keystore semantics ----
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(null, null);
        check("PKCS12".equals(ks.getType()), "getType PKCS12");
        check(ks.getProvider() != null, "getProvider non-null");
        check(ks.size() == 0, "empty size 0");
        check(!ks.aliases().hasMoreElements(), "empty aliases");
        check(ks.getKey("missing", "pw".toCharArray()) == null, "getKey missing null");
        check(ks.getCertificate("missing") == null, "getCertificate missing null");
        check(!ks.containsAlias("missing"), "containsAlias missing false");
        check(!ks.isKeyEntry("missing") && !ks.isCertificateEntry("missing"),
                "isKeyEntry/isCertificateEntry missing false");

        // ---- certificate entry lifecycle ----
        Certificate cert = parseCert();
        ks.setCertificateEntry("cert1", cert);
        check(ks.size() == 1, "size 1 after setCertificateEntry");
        check(ks.containsAlias("cert1"), "containsAlias true");
        check(ks.isCertificateEntry("cert1"), "isCertificateEntry true");
        check(!ks.isKeyEntry("cert1"), "isKeyEntry false for cert entry");
        check(cert.equals(ks.getCertificate("cert1")), "getCertificate round-trip");
        check(ks.getCreationDate("cert1") != null, "getCreationDate non-null");

        // alias enumeration
        Set<String> aliases = new HashSet<>();
        Enumeration<String> e = ks.aliases();
        while (e.hasMoreElements()) aliases.add(e.nextElement());
        check(aliases.size() == 1 && aliases.contains("cert1"), "aliases enumeration");

        // ---- deleteEntry: missing alias no-op, existing alias removes ----
        ks.deleteEntry("missing");
        check(ks.size() == 1, "deleteEntry missing alias no-op");
        ks.deleteEntry("cert1");
        check(ks.size() == 0 && !ks.containsAlias("cert1"), "deleteEntry removes");

        // ---- store/load round-trip ----
        KeyStore ks2 = KeyStore.getInstance("PKCS12");
        ks2.load(null, null);
        ks2.setCertificateEntry("c1", cert);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ks2.store(bos, "password".toCharArray());
        check(bos.size() > 0, "store writes bytes");

        KeyStore ks3 = KeyStore.getInstance("PKCS12");
        ks3.load(new ByteArrayInputStream(bos.toByteArray()), "password".toCharArray());
        check(ks3.size() == 1, "reload preserves size");
        check(cert.equals(ks3.getCertificate("c1")), "reload preserves certificate");
        check(ks3.isCertificateEntry("c1"), "reload isCertificateEntry");

        // ---- store(null) -> NPE ----
        final KeyStore ksNull = KeyStore.getInstance("PKCS12");
        ksNull.load(null, null);
        checkThrows(NullPointerException.class,
                () -> ksNull.store((OutputStream) null, "pw".toCharArray()), "store(null stream)");

        testContractDeep(cert);

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract-deepening section (appended; deterministic RSA-512 keypair) ----
    static void testContractDeep(Certificate cert) throws Exception {
        char[] PW = "deep-pw".toCharArray();

        // default type is the keystore.type security property (pkcs12 since JDK-9)
        check("pkcs12".equals(KeyStore.getDefaultType()), "default type pkcs12 (JDK-27)");

        // ---- uninitialized keystore rejects state queries/mutations ----
        final KeyStore uninit = KeyStore.getInstance("JKS");
        checkThrows(KeyStoreException.class, () -> uninit.size(), "uninitialized size");
        checkThrows(KeyStoreException.class,
                () -> uninit.setCertificateEntry("x", cert), "uninitialized setCertificateEntry");
        checkThrows(KeyStoreException.class,
                () -> uninit.getKey("x", PW), "uninitialized getKey");
        checkThrows(KeyStoreException.class,
                () -> uninit.aliases(), "uninitialized aliases");

        // ---- private-key entry closed loop (JKS) ----
        java.security.KeyPairGenerator kpg = java.security.KeyPairGenerator.getInstance("RSA");
        kpg.initialize(512);
        java.security.KeyPair kp = kpg.generateKeyPair();
        java.security.PrivateKey priv = kp.getPrivate();
        KeyStore jks = KeyStore.getInstance("JKS");
        jks.load(null, null);
        jks.setKeyEntry("k", priv, PW, new Certificate[]{cert});
        check(jks.isKeyEntry("k"), "key entry classified");
        check(!jks.isCertificateEntry("k"), "key entry not cert entry");
        java.security.Key back = jks.getKey("k", PW);
        check(back instanceof java.security.PrivateKey, "getKey returns PrivateKey");
        check(back.equals(priv), "getKey equals original (cross-instance)");
        check("RSA".equals(back.getAlgorithm()), "getKey algorithm RSA");
        Certificate[] chain = jks.getCertificateChain("k");
        check(chain != null && chain.length == 1, "chain length 1");
        check(cert.equals(chain[0]), "chain round-trip equals");
        check(cert.equals(jks.getCertificate("k")), "getCertificate is chain[0]");
        check("k".equals(jks.getCertificateAlias(cert)), "getCertificateAlias finds key-entry cert");
        check(jks.size() == 1, "size 1 with key entry");
        check(jks.getCreationDate("k") != null, "key entry creation date non-null");
        check(jks.getCreationDate("missing") == null, "creation date missing alias null");
        check(jks.getCertificateChain("nosuch") == null, "chain missing alias null");

        // JDK-9+: getCreationInstant mirrors getCreationDate
        check(jks.getCreationInstant("k") != null, "getCreationInstant non-null");
        check(jks.getCreationInstant("missing") == null, "getCreationInstant missing null");
        check(!jks.getCreationInstant("k").isAfter(java.time.Instant.now()),
                "creation instant not in future");

        // JDK-9+: getAttributes for stores without attribute support -> empty set
        check(jks.getAttributes("k").isEmpty(), "getAttributes empty for JKS");
        check(jks.getAttributes("missing").isEmpty(), "getAttributes missing alias empty");

        // wrong password -> UnrecoverableKeyException
        checkThrows(java.security.UnrecoverableKeyException.class,
                () -> jks.getKey("k", "wrong".toCharArray()), "getKey wrong password");

        // ---- setKeyEntry negative matrix (JKS) ----
        checkThrows(IllegalArgumentException.class,
                () -> jks.setKeyEntry("k2", priv, PW, null),
                "setKeyEntry private key null chain IAE");
        checkThrows(KeyStoreException.class,
                () -> jks.setKeyEntry("s", new javax.crypto.spec.SecretKeySpec(new byte[16], "AES"),
                        PW, null),
                "JKS rejects SecretKey");
        checkThrows(KeyStoreException.class,
                () -> jks.setKeyEntry("b", new byte[]{1, 2, 3}, new Certificate[]{cert}),
                "setKeyEntry non-EPI byte[] rejected");
        checkThrows(IllegalArgumentException.class,
                () -> jks.store(new ByteArrayOutputStream(), null),
                "JKS store null password IAE");

        // ---- null-alias NPE matrix ----
        checkThrows(NullPointerException.class, () -> jks.containsAlias(null), "containsAlias(null)");
        checkThrows(NullPointerException.class, () -> jks.getCertificate(null), "getCertificate(null)");
        checkThrows(NullPointerException.class, () -> jks.isKeyEntry(null), "isKeyEntry(null)");
        checkThrows(NullPointerException.class, () -> jks.deleteEntry(null), "deleteEntry(null)");

        // ---- getEntry exception paths ----
        checkThrows(UnsupportedOperationException.class,
                () -> jks.getEntry("k", new KeyStore.CallbackHandlerProtection(
                        callbacks -> { })),
                "getEntry key with CallbackHandlerProtection");
        KeyStore certOnly = KeyStore.getInstance("JKS");
        certOnly.load(null, null);
        certOnly.setCertificateEntry("c", cert);
        checkThrows(UnsupportedOperationException.class,
                () -> certOnly.getEntry("c", new KeyStore.PasswordProtection(PW)),
                "getEntry cert entry with password protection");
        check(certOnly.getEntry("missing", null) == null, "getEntry missing alias null");
        check(certOnly.getCertificateChain("c") == null, "cert entry has null chain");
        check(certOnly.getKey("c", PW) == null, "cert entry getKey null");

        // ---- full store/load byte round-trip with private key ----
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        jks.store(bos, PW);
        KeyStore reloaded = KeyStore.getInstance("JKS");
        reloaded.load(new ByteArrayInputStream(bos.toByteArray()), PW);
        check(reloaded.size() == 1, "reload size");
        check(reloaded.isKeyEntry("k"), "reload key entry classification");
        java.security.Key rk = reloaded.getKey("k", PW);
        check(rk != null && rk.equals(priv), "reload key equals original");
        check(cert.equals(reloaded.getCertificate("k")), "reload cert equals original");
        checkThrows(java.security.UnrecoverableKeyException.class,
                () -> reloaded.getKey("k", "nope".toCharArray()), "reload wrong password");

        // JDK-9+: getInstance(File, char[]) probes and loads a keystore file
        java.io.File tmp = java.io.File.createTempFile("jck-ks", ".jks");
        tmp.deleteOnExit();
        try (OutputStream fos = new java.io.FileOutputStream(tmp)) {
            jks.store(fos, PW);
        }
        try {
            KeyStore fromFile = KeyStore.getInstance(tmp, PW);
            check("JKS".equals(fromFile.getType()), "getInstance(File,char[]) probes type");
            check(fromFile.isKeyEntry("k"), "File-loaded key entry");
            check(fromFile.getKey("k", PW).equals(priv), "File-loaded key equals original");
        } finally {
            tmp.delete();
        }

        // empty JKS store round-trips as empty
        KeyStore emptySrc = KeyStore.getInstance("JKS");
        emptySrc.load(null, null);
        ByteArrayOutputStream ebos = new ByteArrayOutputStream();
        emptySrc.store(ebos, PW);
        KeyStore emptyDst = KeyStore.getInstance("JKS");
        emptyDst.load(new ByteArrayInputStream(ebos.toByteArray()), PW);
        check(emptyDst.size() == 0, "empty JKS store round-trip");
        check(ebos.size() > 0, "empty JKS store writes header");

        // ---- garbage stream -> IOException family (EOF for truncated magic) ----
        checkThrows(java.io.EOFException.class,
                () -> KeyStore.getInstance("JKS").load(
                        new ByteArrayInputStream(new byte[]{1, 2, 3}), null),
                "load truncated stream EOFException");

        // ---- alias case-sensitivity: JKS folds, CASEEXACTJKS does not ----
        KeyStore caseStore = KeyStore.getInstance("JKS");
        caseStore.load(null, null);
        caseStore.setCertificateEntry("CaseAlias", cert);
        check(caseStore.containsAlias("CaseAlias"), "JKS exact alias");
        check(caseStore.containsAlias("casealias"), "JKS alias case-insensitive");
        KeyStore ceStore = KeyStore.getInstance("CASEEXACTJKS");
        ceStore.load(null, null);
        ceStore.setCertificateEntry("C1", cert);
        check(ceStore.containsAlias("C1"), "CASEEXACTJKS exact alias");
        check(!ceStore.containsAlias("c1"), "CASEEXACTJKS case-sensitive");
        check("CASEEXACTJKS".equals(ceStore.getType()), "CASEEXACTJKS getType");

        // ---- secret-key loop on JCEKS and PKCS12 ----
        javax.crypto.SecretKey aes = new javax.crypto.spec.SecretKeySpec(new byte[16], "AES");
        KeyStore jce = KeyStore.getInstance("JCEKS");
        jce.load(null, null);
        jce.setEntry("s", new KeyStore.SecretKeyEntry(aes), new KeyStore.PasswordProtection(PW));
        check(jce.isKeyEntry("s"), "JCEKS secret isKeyEntry");
        java.security.Key sk = jce.getKey("s", PW);
        check(sk instanceof javax.crypto.SecretKey, "JCEKS getKey SecretKey");
        check("AES".equals(sk.getAlgorithm()), "JCEKS secret algorithm");
        check(jce.getEntry("s", new KeyStore.PasswordProtection(PW))
                instanceof KeyStore.SecretKeyEntry, "JCEKS getEntry SecretKeyEntry");
        KeyStore p12 = KeyStore.getInstance("PKCS12");
        p12.load(null, null);
        p12.setEntry("s", new KeyStore.SecretKeyEntry(aes), new KeyStore.PasswordProtection(PW));
        check(p12.getKey("s", PW) instanceof javax.crypto.SecretKey, "PKCS12 secret key loop");
        check(p12.getEntry("s", new KeyStore.PasswordProtection(PW))
                instanceof KeyStore.SecretKeyEntry, "PKCS12 secret entry loop");

        // ---- setEntry protection-parameter matrix ----
        checkThrows(KeyStoreException.class,
                () -> p12.setEntry("t", new KeyStore.TrustedCertificateEntry(cert),
                        new KeyStore.PasswordProtection(PW)),
                "setEntry trusted cert with password protection");
        checkThrows(KeyStoreException.class,
                () -> p12.setEntry("p", new KeyStore.PrivateKeyEntry(priv,
                        new Certificate[]{cert}), null),
                "setEntry private key with null protection");

        // ---- provider-pinned factories ----
        KeyStore sunJks = KeyStore.getInstance("JKS", "SUN");
        check("SUN".equals(sunJks.getProvider().getName()), "getInstance(type, providerName)");
        check("JKS".equals(sunJks.getType()), "getType JKS");
        sunJks.load(null, null);
        check(sunJks.size() == 0, "provider-pinned store usable");
        KeyStore sunJks2 = KeyStore.getInstance("JKS", Security.getProvider("SUN"));
        check("SUN".equals(sunJks2.getProvider().getName()), "getInstance(type, Provider)");

        // ---- store(LoadStoreParameter) unsupported for file-based formats ----
        KeyStore.LoadStoreParameter lsp = new KeyStore.LoadStoreParameter() {
            public KeyStore.ProtectionParameter getProtectionParameter() {
                return new KeyStore.PasswordProtection(PW);
            }
        };
        checkThrows(UnsupportedOperationException.class,
                () -> jks.store(lsp), "JKS store(LoadStoreParameter) UOE");

        // JDK-9+: JKS load(LoadStoreParameter) initializes an empty store via the
        // default engineLoad(null, ...) path (JDK-8 rejected it with UOE)
        KeyStore lspLoad = KeyStore.getInstance("JKS");
        lspLoad.load(new KeyStore.LoadStoreParameter() {
            public KeyStore.ProtectionParameter getProtectionParameter() {
                return new KeyStore.PasswordProtection(PW);
            }
        });
        check(lspLoad.size() == 0, "JKS load(LSP) empty store (JDK-9+)");
    }
}
