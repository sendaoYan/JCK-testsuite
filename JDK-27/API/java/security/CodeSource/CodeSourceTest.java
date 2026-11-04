/*
 * @test
 * @summary java.security.CodeSource conformance: full contract coverage.
 *          constructors (URL + Certificate[]/CodeSigner[]), getLocation/getCertificates/
 *          getCodeSigners, cert<->signer conversion, null semantics, implies asymmetry,
 *          equals/hashCode, defensive copy, and serialization round-trip.
 * @run main CodeSourceTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.security.cert.CertPath;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Collections;

public class CodeSourceTest {
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

    static X509Certificate cert() throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return (X509Certificate) cf.generateCertificate(
                new ByteArrayInputStream(Base64.getDecoder().decode(CERT_B64)));
    }

    static CertPath certPath() throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return cf.generateCertPath(Collections.<java.security.cert.Certificate>singletonList(cert()));
    }

    public static void main(String[] args) throws Exception {
        URL u = new URL("file:/tmp/x");
        X509Certificate cert = cert();

        // ---- URL + Certificate[] ----
        CodeSource withCert = new CodeSource(u, new java.security.cert.Certificate[]{cert});
        check(u.equals(withCert.getLocation()), "getLocation");
        check(withCert.getCertificates() != null && withCert.getCertificates().length == 1,
                "getCertificates length 1");
        check(withCert.getCertificates()[0].equals(cert), "getCertificates contains cert");
        check(withCert.getCodeSigners() != null && withCert.getCodeSigners().length == 1,
                "cert-derived getCodeSigners length 1");
        check(withCert.getCodeSigners()[0].getSignerCertPath().getCertificates().get(0).equals(cert),
                "cert-derived signer certPath contains cert");
        check(withCert.getCodeSigners()[0].getTimestamp() == null,
                "cert-derived signer timestamp null");

        // ---- URL + CodeSigner[] -> certificates ----
        CodeSigner s = new CodeSigner(certPath(), null);
        CodeSource withSigner = new CodeSource(u, new CodeSigner[]{s});
        check(withSigner.getCertificates() != null && withSigner.getCertificates().length == 1,
                "signer-derived getCertificates length 1");
        check(withSigner.getCertificates()[0].equals(cert), "signer-derived certificate equals");

        // ---- null semantics ----
        CodeSource noCert = new CodeSource(u, (java.security.cert.Certificate[]) null);
        check(noCert.getCertificates() == null, "null certs -> getCertificates null");
        check(noCert.getCodeSigners() == null, "null certs -> getCodeSigners null");
        CodeSource emptySigners = new CodeSource(u, new CodeSigner[0]);
        check(emptySigners.getCodeSigners() != null && emptySigners.getCodeSigners().length == 0,
                "empty signers -> empty getCodeSigners");

        // ---- null URL allowed ----
        CodeSource nullUrl = new CodeSource(null, (java.security.cert.Certificate[]) null);
        check(nullUrl.getLocation() == null, "null URL -> getLocation null");
        check(nullUrl.getCertificates() == null, "null URL -> getCertificates null");

        // ---- implies asymmetry ----
        check(noCert.implies(noCert), "implies self (no cert)");
        check(withCert.implies(withCert), "implies self (cert)");
        check(noCert.implies(withCert), "no-cert implies cert true");
        check(!withCert.implies(noCert), "cert implies no-cert false");
        check(!noCert.implies(new CodeSource(new URL("file:/tmp/y"), (java.security.cert.Certificate[]) null)),
                "implies different URL false");

        // ---- equals/hashCode ----
        check(withCert.equals(new CodeSource(u, new java.security.cert.Certificate[]{cert})),
                "equals same URL + cert");
        check(withCert.hashCode() == new CodeSource(u, new java.security.cert.Certificate[]{cert}).hashCode(),
                "hashCode same URL + cert");
        check(!withCert.equals(noCert), "equals cert vs no-cert false");
        check(!withCert.equals(null), "equals null false");
        check(!withCert.equals("not a codesource"), "equals unrelated type false");

        // ---- defensive copy ----
        java.security.cert.Certificate[] input = {cert};
        CodeSource copy = new CodeSource(u, input);
        input[0] = null;
        check(copy.getCertificates()[0].equals(cert), "defensive copy of certificates");

        // ---- serialization round-trip ----
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(withCert); }
        CodeSource back;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            back = (CodeSource) ois.readObject();
        }
        check(withCert.equals(back), "serialization equals");
        check(u.equals(back.getLocation()), "serialization preserves location");
        check(back.getCertificates() != null && back.getCertificates().length == 1,
                "serialization preserves certificates");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
