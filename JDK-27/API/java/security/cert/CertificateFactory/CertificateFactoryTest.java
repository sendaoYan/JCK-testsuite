/*
 * @test
 * @summary java.security.cert.CertificateFactory conformance: full contract coverage.
 *          getInstance variants + exact exceptions (CertificateException /
 *          NullPointerException / IllegalArgumentException / NoSuchProviderException),
 *          generateCertificate(InputStream) from DER, generateCertificate(null) ->
 *          CertificateException, generateCertificates(Collection), generateCRL from
 *          cert DER -> CRLException, generateCertPath(List) -> CertPath, and
 *          getType/getProvider.
 * @run main CertificateFactoryTest
 */

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.security.cert.CRLException;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;

public class CertificateFactoryTest {
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

    static byte[] der() { return Base64.getDecoder().decode(CERT_B64); }

    public static void main(String[] args) throws Exception {
        // ---- factory + exception matrix ----
        checkThrows(CertificateException.class,
                () -> CertificateFactory.getInstance("NoSuchCF_XYZ"), "getInstance(bogus)");
        checkThrows(NullPointerException.class,
                () -> CertificateFactory.getInstance(null), "getInstance(null)");
        checkThrows(NullPointerException.class,
                () -> CertificateFactory.getInstance(null, Security.getProvider("SUN")),
                "getInstance(null, Provider)");
        checkThrows(IllegalArgumentException.class,
                () -> CertificateFactory.getInstance("X.509", (Provider) null),
                "getInstance(type, null Provider)");
        checkThrows(IllegalArgumentException.class,
                () -> CertificateFactory.getInstance("X.509", (String) null),
                "getInstance(type, null String)");
        checkThrows(NoSuchProviderException.class,
                () -> CertificateFactory.getInstance("X.509", "NoSuchProvider_XYZ"),
                "getInstance(type, bogus provider)");

        // ---- metadata ----
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        check("X.509".equals(cf.getType()), "getType X.509");
        check(cf.getProvider() != null, "getProvider non-null");

        // ---- generateCertificate ----
        X509Certificate cert = (X509Certificate) cf.generateCertificate(
                new ByteArrayInputStream(der()));
        check(cert != null && cert.getVersion() == 3, "generateCertificate parses cert");

        // ---- generateCertificate(null) -> CertificateException ----
        final CertificateFactory cfNull = CertificateFactory.getInstance("X.509");
        checkThrows(CertificateException.class,
                () -> cfNull.generateCertificate((InputStream) null), "generateCertificate(null)");

        // ---- generateCertificates ----
        Collection<? extends Certificate> certs = cf.generateCertificates(
                new ByteArrayInputStream(der()));
        check(certs != null && certs.size() == 1, "generateCertificates parses one cert");
        final CertificateFactory cfNull2 = CertificateFactory.getInstance("X.509");
        checkThrows(CertificateException.class,
                () -> cfNull2.generateCertificates((InputStream) null), "generateCertificates(null)");

        // ---- generateCRL from cert DER -> CRLException ----
        final CertificateFactory cfCrl = CertificateFactory.getInstance("X.509");
        checkThrows(CRLException.class,
                () -> cfCrl.generateCRL(new ByteArrayInputStream(der())), "generateCRL(cert DER)");

        // ---- generateCertPath(List) ----
        List<X509Certificate> list = new ArrayList<>();
        list.add(cert);
        CertPath cp = cf.generateCertPath(list);
        check(cp != null && "X.509".equals(cp.getType()), "generateCertPath(List) type");
        check(cp.getCertificates().size() == 1, "generateCertPath(List) one cert");

        // ---- generateCertPath(null List) -> NPE ----
        @SuppressWarnings("unchecked")
        List<X509Certificate> nullList = (List<X509Certificate>) (List<?>) null;
        final CertificateFactory cfCp = CertificateFactory.getInstance("X.509");
        checkThrows(NullPointerException.class,
                () -> cfCp.generateCertPath(nullList), "generateCertPath(null List)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
