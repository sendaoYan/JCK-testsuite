/*
 * @test
 * @summary java.security.cert.X509Certificate conformance: full contract coverage.
 *          parse a fixed self-signed X.509 cert via CertificateFactory, assert version/
 *          serial/sigAlgName/sigAlgOID/public-key algorithm/validity/DNs/basicConstraints,
 *          checkValidity(Date) inside range and NotYetValid/Expired outside range,
 *          verify(self public key), getEncoded round-trip, getType, equals/hashCode,
 *          and toString.
 * @run main X509CertificateTest
 */

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Date;

public class X509CertificateTest {
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

    // Fixed self-signed X.509 cert: RSA 2048, SHA256withRSA, serial 0x0f17ae3a.
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

    static X509Certificate cert() throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return (X509Certificate) cf.generateCertificate(
                new ByteArrayInputStream(Base64.getDecoder().decode(CERT_B64)));
    }

    public static void main(String[] args) throws Exception {
        X509Certificate c = cert();

        // ---- fields ----
        check(c.getVersion() == 3, "getVersion == 3");
        check(new BigInteger("0f17ae3a", 16).equals(c.getSerialNumber()), "getSerialNumber");
        check("SHA256withRSA".equals(c.getSigAlgName()), "getSigAlgName");
        check("1.2.840.113549.1.1.11".equals(c.getSigAlgOID()), "getSigAlgOID");
        check("RSA".equals(c.getPublicKey().getAlgorithm()), "getPublicKey algorithm RSA");

        // ---- validity ----
        Date notBefore = c.getNotBefore();
        Date notAfter = c.getNotAfter();
        check(notBefore != null && notAfter != null, "validity dates non-null");
        check(notBefore.before(notAfter), "notBefore before notAfter");

        // checkValidity(Date) inside range
        c.checkValidity(new Date(notBefore.getTime() + 1000));
        check(true, "checkValidity just after notBefore");
        c.checkValidity(new Date(notAfter.getTime() - 1000));
        check(true, "checkValidity just before notAfter");

        // checkValidity(Date) outside range (deterministic relative to cert)
        checkThrows(CertificateNotYetValidException.class,
                () -> c.checkValidity(new Date(notBefore.getTime() - 1000)), "checkValidity before notBefore");
        checkThrows(CertificateExpiredException.class,
                () -> c.checkValidity(new Date(notAfter.getTime() + 1000)), "checkValidity after notAfter");

        // ---- DNs ----
        check("CN=JCK Test Cert,OU=Conformance,O=Example Org,L=Springfield,ST=IL,C=US"
                .equals(c.getSubjectX500Principal().getName()), "getSubjectX500Principal");
        check("CN=JCK Test Cert,OU=Conformance,O=Example Org,L=Springfield,ST=IL,C=US"
                .equals(c.getIssuerX500Principal().getName()), "getIssuerX500Principal");
        check(c.getSubjectDN() != null && c.getIssuerDN() != null, "getSubjectDN/getIssuerDN non-null");

        // ---- basic constraints ----
        check(c.getBasicConstraints() == -1, "getBasicConstraints -1 (no extension)");

        // ---- verify self-signed ----
        c.verify(c.getPublicKey());
        check(true, "verify(self public key)");

        // ---- type + encoded round-trip ----
        check("X.509".equals(c.getType()), "getType X.509");
        byte[] enc = c.getEncoded();
        check(enc != null && enc.length > 500, "getEncoded non-trivial");
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate reparsed = (X509Certificate) cf.generateCertificate(
                new ByteArrayInputStream(enc));
        check(c.equals(reparsed), "reparse encoded equals");

        // ---- equals/hashCode/toString ----
        X509Certificate c2 = cert();
        check(c.equals(c2), "equals same cert");
        check(c.hashCode() == c2.hashCode(), "hashCode equal");
        check(c.toString() != null && c.toString().length() > 0, "toString non-empty");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
