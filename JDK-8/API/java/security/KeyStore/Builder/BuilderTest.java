/*
 * @test
 * @summary java.security.KeyStore.Builder conformance: abstract structure,
 *          newInstance(KeyStore, ProtectionParameter) caching and call-order
 *          contract (getProtectionParameter before getKeyStore ->
 *          IllegalStateException), uninitialized-store IAE, null-file NPE,
 *          type-only and file-backed builders (temp file, cleaned up), and
 *          a custom subclass honoring the abstract contract.
 * @run main BuilderTest
 */

public class BuilderTest {
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
        Class<?> c = Class.forName("java.security.KeyStore$Builder");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getKeyStore"), "declares getKeyStore");
        check(ms.contains("getProtectionParameter"), "declares getProtectionParameter");
        check(ms.contains("newInstance"), "declares newInstance");

        testContractDeep();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract-deepening section (appended) ----
    static java.security.KeyStore.PasswordProtection PP =
            new java.security.KeyStore.PasswordProtection("pw".toCharArray());

    static void testContractDeep() throws Exception {
        // ---- newInstance(KeyStore, ProtectionParameter) ----
        java.security.KeyStore ks = java.security.KeyStore.getInstance("JKS");
        ks.load(null, null);
        ks.setCertificateEntry("c", parseCert());
        java.security.KeyStore.Builder fixed =
                java.security.KeyStore.Builder.newInstance(ks, PP);

        // call-order contract: getProtectionParameter before getKeyStore -> ISE
        checkThrows(IllegalStateException.class,
                () -> fixed.getProtectionParameter("c"),
                "getProtectionParameter before getKeyStore ISE");
        check(fixed.getKeyStore() == ks, "getKeyStore returns the wrapped store");
        check(fixed.getKeyStore() == ks, "getKeyStore cached (same instance)");
        check(fixed.getProtectionParameter("c") == PP,
                "protection parameter identity round-trip");
        check(fixed.getProtectionParameter("any") == PP,
                "protection parameter alias-independent");
        checkThrows(NullPointerException.class,
                () -> fixed.getProtectionParameter(null),
                "getProtectionParameter(null) NPE");

        // uninitialized store rejected
        checkThrows(IllegalArgumentException.class,
                () -> java.security.KeyStore.Builder.newInstance(
                        java.security.KeyStore.getInstance("JKS"), PP),
                "uninitialized store IAE");

        // null protection rejected
        checkThrows(NullPointerException.class,
                () -> java.security.KeyStore.Builder.newInstance(ks, null),
                "null protection NPE");

        // ---- newInstance(type, provider, file, protection): null file NPE ----
        checkThrows(NullPointerException.class,
                () -> java.security.KeyStore.Builder.newInstance("JKS", null, null, PP),
                "null file NPE at construction");

        // ---- file-backed builder: loads once and caches ----
        java.io.File tmp = java.io.File.createTempFile("jck-builder", ".jks");
        tmp.deleteOnExit();
        try {
            java.io.FileOutputStream fos = new java.io.FileOutputStream(tmp);
            try { ks.store(fos, "pw".toCharArray()); } finally { fos.close(); }
            java.security.KeyStore.Builder fb =
                    java.security.KeyStore.Builder.newInstance("JKS", null, tmp, PP);
            java.security.KeyStore loaded = fb.getKeyStore();
            check(loaded != null && "JKS".equals(loaded.getType()), "file builder type");
            check(loaded.containsAlias("c"), "file builder loads contents");
            check(fb.getKeyStore() == loaded, "file builder caches the store");
            check(fb.getProtectionParameter("c") == PP, "file builder protection");
            // builder store is live: mutations visible in the same instance
            loaded.setCertificateEntry("added", parseCert());
            check(fb.getKeyStore().containsAlias("added"), "builder store is live");
        } finally {
            tmp.delete();
        }

        // ---- type-only builder: fresh empty typed store ----
        java.security.KeyStore.Builder tb =
                java.security.KeyStore.Builder.newInstance("PKCS12", null, PP);
        java.security.KeyStore fresh = tb.getKeyStore();
        check(fresh != null && "PKCS12".equals(fresh.getType()), "type builder type");
        check(fresh.size() == 0, "type builder fresh empty store");
        check(tb.getProtectionParameter("x") == PP, "type builder protection");
        fresh.setCertificateEntry("c", parseCert());
        check(fresh.containsAlias("c"), "type builder store mutable");

        // bogus type fails lazily at getKeyStore with KeyStoreException
        java.security.KeyStore.Builder bogus =
                java.security.KeyStore.Builder.newInstance("NoSuchKS_XYZ", null, PP);
        checkThrows(java.security.KeyStoreException.class,
                () -> bogus.getKeyStore(), "bogus type getKeyStore KeyStoreException");

        // ---- custom subclass honors the abstract contract ----
        java.security.KeyStore subStore = java.security.KeyStore.getInstance("JKS");
        subStore.load(null, null);
        final int[] calls = {0};
        java.security.KeyStore.Builder custom = new java.security.KeyStore.Builder() {
            public java.security.KeyStore getKeyStore() {
                calls[0]++;
                return subStore;
            }
            public java.security.KeyStore.ProtectionParameter getProtectionParameter(String alias) {
                return PP;
            }
        };
        check(custom.getKeyStore() == subStore, "custom builder store");
        check(calls[0] == 1, "custom getKeyStore invoked");
        check(custom.getProtectionParameter("a") == PP, "custom builder protection");
    }

    // fixed self-signed X.509 certificate (RSA 2048) for deterministic parsing
    static final String CERT_B64 =
        "MIIDhzCCAm+gAwIBAgIEDxeuOjANBgkqhkiG9w0BAQsFADB0MQswCQYDVQQGEw" +
        "JVUzELMAkGA1UECBMCSUwxFDASBgNVBAcTC1NwcmluZ2ZpZWxkMRQwEgYDVQQK" +
        "EwtFeGFtcGxlIE9yZzEUMBIGA1UECxMLQ29uZm9ybWFuY2UxFjAUBgNVBAMTDU" +
        "pDSyBUZXN0IENlcnQwHhcNMjYxMTA0MDkzNzI4WhcNMzYxMTAxMDkzNzI4WjB0" +
        "MQswCQYDVQQGEwJVUzELMAkGA1UECBMCSUwxFDASBgNVBAcTC1NwcmluZ2ZpZW" +
        "xkMRQwEgYDVQQKEwtFeGFtcGxlIE9yZzEUMBIGA1UECxMLQ29uZm9ybWFuY2Ux" +
        "FjAUBgNVBAMTDUpDSyBUZXN0IENlcnQwggEiMA0GCSqGSIb3DQEBAQUAA4IBDw" +
        "AwggEKAoIBAQDAImuPu7xdlUkyW0Q8Y75ek2oybshN0xF3MxmxqiUWJruiM+U2" +
        "+Ema1DA7lSquiCNGbzmHhWpk8mK2rjEFu++avlIG5mnr4XQdiQe5o4q2IIuN3L" +
        "KdvfauwmT5WoxUTdCaFxtBDdAPsfckCr8C/ypWwJUdK4uY4MFgrVWg5NAIIG8x" +
        "VmGONYIpE25fxQ7O55CM23CXsJ3iU5+GpTBFaDr8/LJ2eKQEx35voV3iyWa9bK" +
        "dsys4kg1sQgTCLv2kvRFU9QXmcLwuhP0tEUGf7fann26h5FrL5fsj4UwT+djO4" +
        "2tNHTDN5KGgeoinVKYlUcfEYWEyek9a95hFdn8I/30fLAgMBAAGjITAfMB0GA1" +
        "UdDgQWBBTo72jITqQdGMA1Fxl/lTqG24M1VTANBgkqhkiG9w0BAQsFAAOCAQEA" +
        "nofWJt83h4XkRfD6ejfnYJeMXZ+OQY4J4/cHa6jSTgXPrKI8GQtZD8QF0jlb/2" +
        "rjEKQ33YW8r9lci6p1po5bIMqbToGbzoKFK2x16ggsx37pxwIWqdp9aFIWJvQF" +
        "XrK8vwzEGFc518wxjhOLKE7BbYzeCSiLjP92JbEWUw6Ae4eCEpxMyZKWCw/iiz" +
        "v2zRv3iH5uobZwZ+aUiQUgjTXRM0exJXFvVfXV9EY8MG9PMbWNjkusz66NIyUK" +
        "oupRvl9rTnjNv1LlggVSAwW5uXzNCh/sUSMmjVmmYMwYpDTG1Uz54HYKL7ZNhk" +
        "DUj0IzSM5PI0ut8jdnMJOOWXr6tg0GPg==";

    static java.security.cert.Certificate parseCert() throws Exception {
        java.security.cert.CertificateFactory cf =
                java.security.cert.CertificateFactory.getInstance("X.509");
        return cf.generateCertificate(new java.io.ByteArrayInputStream(
                java.util.Base64.getDecoder().decode(CERT_B64)));
    }
}
