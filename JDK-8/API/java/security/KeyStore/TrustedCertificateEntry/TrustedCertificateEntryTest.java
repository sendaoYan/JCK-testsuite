/*
 * @test
 * @summary java.security.KeyStore.TrustedCertificateEntry conformance:
 *          constructor null matrix, getter round-trip with reparsed equal
 *          certificates, default attributes, toString, identity equality,
 *          and an end-to-end JKS setCertificateEntry/getEntry loop with
 *          null protection (password protection rejected).
 * @run main TrustedCertificateEntryTest
 */

public class TrustedCertificateEntryTest {
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
        Class<?> c = Class.forName("java.security.KeyStore$TrustedCertificateEntry");
        check(!c.isInterface(), "is class");
        check(java.security.KeyStore.Entry.class.isAssignableFrom(c), "implements KeyStore.Entry");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getTrustedCertificate"), "declares getTrustedCertificate");

        testContractDeep();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract-deepening section (appended) ----
    static void testContractDeep() throws Exception {
        java.security.cert.Certificate cert = parseCert();

        // constructor null matrix
        checkThrows(NullPointerException.class,
                () -> new java.security.KeyStore.TrustedCertificateEntry(null),
                "null cert NPE");

        // getter round-trip: value-equal reparsed certificate
        java.security.KeyStore.TrustedCertificateEntry entry =
                new java.security.KeyStore.TrustedCertificateEntry(cert);
        check(entry.getTrustedCertificate().equals(cert), "cert identity round-trip");
        java.security.KeyStore.TrustedCertificateEntry reparsed =
                new java.security.KeyStore.TrustedCertificateEntry(parseCert());
        check(reparsed.getTrustedCertificate().equals(cert),
                "reparsed cert value-equal");

        // interface defaults + toString
        check(entry instanceof java.security.KeyStore.Entry, "implements Entry");
        check(entry.getAttributes() != null && entry.getAttributes().isEmpty(),
                "default attributes empty");
        check(entry.toString() != null, "toString non-null");

        // identity equality (no equals/hashCode override)
        check(entry.equals(entry), "reflexive equals");
        check(!entry.equals(reparsed), "same-cert distinct instances unequal");
        check(!entry.equals("x"), "not equal to unrelated type");
        check(entry.hashCode() == entry.hashCode(), "hashCode stable");

        // JKS loop: setEntry with null protection / getEntry with null protection
        java.security.KeyStore jks = java.security.KeyStore.getInstance("JKS");
        jks.load(null, null);
        jks.setEntry("t", entry, null);
        check(jks.isCertificateEntry("t"), "cert entry classification");
        check(!jks.isKeyEntry("t"), "cert entry is not key entry");
        check(jks.containsAlias("t") && jks.size() == 1, "size/containsAlias");
        java.security.KeyStore.Entry back = jks.getEntry("t", null);
        check(back instanceof java.security.KeyStore.TrustedCertificateEntry,
                "getEntry type round-trip");
        check(((java.security.KeyStore.TrustedCertificateEntry) back)
                .getTrustedCertificate().equals(cert), "round-trip cert equals original");
        check(jks.getCertificate("t").equals(cert), "getCertificate equals original");
        check(jks.getCertificateChain("t") == null, "cert entry null chain");
        check(jks.getKey("t", "pw".toCharArray()) == null, "cert entry getKey null");
        check("t".equals(jks.getCertificateAlias(cert)), "getCertificateAlias");

        // password protection is rejected for trusted-cert entries
        java.security.KeyStore.PasswordProtection prot =
                new java.security.KeyStore.PasswordProtection("pw".toCharArray());
        checkThrows(java.security.KeyStoreException.class,
                () -> jks.setEntry("t2", entry, prot),
                "setEntry with password protection KeyStoreException");
        checkThrows(UnsupportedOperationException.class,
                () -> jks.getEntry("t", prot),
                "getEntry with password protection UOE");

        // replace + delete lifecycle
        java.security.cert.Certificate certB = parseCert();
        jks.setCertificateEntry("t", certB);
        check(jks.size() == 1, "replace keeps size");
        jks.deleteEntry("t");
        check(jks.size() == 0 && !jks.containsAlias("t"), "deleteEntry removes");
        check(jks.getEntry("t", null) == null, "getEntry after delete null");
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
}
