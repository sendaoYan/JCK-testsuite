/*
 * @test
 * @summary java.security.cert.CertPath conformance: full contract coverage.
 *          abstract class hierarchy, build via CertificateFactory.generateCertPath(List),
 *          getType/getCertificates/getEncoded()/getEncoded(String) incl. PkiPath/PKCS7,
 *          getEncoded(null) -> NPE, getEncoded(bogus) -> CertificateEncodingException,
 *          and PkiPath stream round-trip.
 * @run main CertPathTest
 */

import java.io.ByteArrayInputStream;
import java.security.cert.CertPath;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class CertPathTest {
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

    public static void main(String[] args) throws Exception {
        // ---- class hierarchy ----
        Class<?> c = Class.forName("java.security.cert.CertPath");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(),
                "CertPath abstract class");

        // ---- build via factory ----
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) cf.generateCertificate(
                new ByteArrayInputStream(Base64.getDecoder().decode(CERT_B64)));
        List<X509Certificate> list = new ArrayList<>();
        list.add(cert);
        CertPath cp = cf.generateCertPath(list);

        check("X.509".equals(cp.getType()), "getType X.509");
        check(cp.getCertificates().size() == 1, "getCertificates size 1");
        check(cert.equals(cp.getCertificates().get(0)), "getCertificates contains cert");

        // ---- encodings ----
        byte[] def = cp.getEncoded();
        check(def != null && def.length > 0, "getEncoded() non-empty");
        byte[] pki = cp.getEncoded("PkiPath");
        check(pki != null && pki.length > 0, "getEncoded(PkiPath) non-empty");
        byte[] pkcs7 = cp.getEncoded("PKCS7");
        check(pkcs7 != null && pkcs7.length > 0, "getEncoded(PKCS7) non-empty");

        // ---- PkiPath stream round-trip ----
        CertPath reparsed = cf.generateCertPath(new ByteArrayInputStream(pki));
        check("X.509".equals(reparsed.getType()), "reparsed getType");
        check(reparsed.getCertificates().size() == 1, "reparsed certificates size");
        check(cert.equals(reparsed.getCertificates().get(0)), "reparsed certificate equal");

        // ---- exceptions ----
        final CertPath cpN = cp;
        checkThrows(NullPointerException.class, () -> cpN.getEncoded((String) null),
                "getEncoded(null)");
        checkThrows(CertificateEncodingException.class, () -> cpN.getEncoded("bogusFormat"),
                "getEncoded(bogus format)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
