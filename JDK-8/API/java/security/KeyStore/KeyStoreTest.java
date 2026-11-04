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

        System.out.println("PASS (" + passed + " assertions)");
    }
}
