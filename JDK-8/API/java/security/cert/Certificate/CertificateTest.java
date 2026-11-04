/*
 * @test
 * @summary java.security.cert.Certificate conformance: full contract coverage.
 *          abstract class implements Serializable, concrete X.509 getType/getPublicKey/
 *          getEncoded/verify(PublicKey)/hashCode/equals/toString, serialization
 *          round-trip, and class hierarchy.
 * @run main CertificateTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

public class CertificateTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
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

    public static void main(String[] args) throws Exception {
        // ---- class hierarchy ----
        Class<?> c = Class.forName("java.security.cert.Certificate");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(),
                "Certificate abstract class");
        check(Serializable.class.isAssignableFrom(c), "Certificate implements Serializable");

        X509Certificate cert = cert();

        // ---- abstract methods on concrete impl ----
        check("X.509".equals(cert.getType()), "getType");
        check(cert.getPublicKey() != null && "RSA".equals(cert.getPublicKey().getAlgorithm()),
                "getPublicKey");
        byte[] enc = cert.getEncoded();
        check(enc != null && enc.length > 500, "getEncoded non-trivial");
        cert.verify(cert.getPublicKey());
        check(true, "verify(publicKey)");
        cert.verify(cert.getPublicKey(), "SunRsaSign");
        check(true, "verify(publicKey, provider)");
        check(cert.hashCode() != 0, "hashCode non-zero");
        check(cert.equals(cert()), "equals same cert");
        check(cert.toString() != null && cert.toString().length() > 0, "toString non-empty");

        // ---- serialization round-trip ----
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(cert);
        }
        Certificate back;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            back = (Certificate) ois.readObject();
        }
        check(cert.equals(back), "serialization round-trip equals");
        check(java.util.Arrays.equals(cert.getEncoded(), back.getEncoded()), "serialization encoded equal");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
