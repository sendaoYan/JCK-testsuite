/*
 * @test
 * @summary java.security.PEMEncoder conformance: white-box structure + black-box behavior.
 * @enablePreview
 * @enablePreview
 * @run main PEMEncoderTest
 */

import java.security.PEM;
import java.security.PEMEncoder;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.io.ByteArrayInputStream;
import java.util.*;
import java.nio.charset.StandardCharsets;

public class PEMEncoderTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        PEM p = new PEM("TEST", "aGVsbG8=");
        String s = PEMEncoder.of().encodeToString(p);
        check(s != null, "encodeToString non-null");
        check(s.contains("-----BEGIN TEST-----"), "has BEGIN");
        check(s.contains("-----END TEST-----"), "has END");
        byte[] b = PEMEncoder.of().encode(p);
        check(b != null && b.length > 0, "encode non-empty");
    }

    // Fixed fixtures, generated once with JDK-27 keytool (RSA-2048 self-signed certs CN=jck-root / CN=jck-leaf,
    // validity 2026-11-03T04:09:52Z..2036-10-31T04:09:52Z, sig SHA384withRSA, root serial 0x70bdab2c498601da,
    // leaf serial 0xbc0944b4fd45f023; CRL v1 issued by jck-root, thisUpdate 2026-11-03, nextUpdate 2027-02-01,
    // revoked serial 0; PKCS#8 DER of the root private key). Tests decode at runtime; no external files.

    static final String ROOT_CERT_B64 =
        "MIIDLjCCAhagAwIBAgIIcL2rLEmGAdowDQYJKoZIhvcNAQEMBQAwPTELMAkGA1UEBhMC" + "\n" +
        "VVMxDTALBgNVBAoTBFRlc3QxDDAKBgNVBAsTA0pDSzERMA8GA1UEAxMIamNrLXJvb3Qw" + "\n" +
        "HhcNMjYxMTAzMDQwOTUyWhcNMzYxMDMxMDQwOTUyWjA9MQswCQYDVQQGEwJVUzENMAsG" + "\n" +
        "A1UEChMEVGVzdDEMMAoGA1UECxMDSkNLMREwDwYDVQQDEwhqY2stcm9vdDCCASIwDQYJ" + "\n" +
        "KoZIhvcNAQEBBQADggEPADCCAQoCggEBALrM4ukZtyCiH9zsjkRMgOt6EYkhMv8IXY7t" + "\n" +
        "wYzWwLJnR9ED7BnleYFAaeSH17SWUAPdUBTH+ig57+2DI2Te3IJo974rgHjq94UXk5ez" + "\n" +
        "y1aUlrpe4SV+1NCnA/zg2E3lIOIaObZrTbBTT16jveIuX3Rqjbz9/z6Xic3Wvn2CqBNp" + "\n" +
        "1rnF1/GqhN6/6O0I+8el3ulMXzqmc21YPQuvpJDYuR1026ht3vqZhuY3elLEdvTQfOQa" + "\n" +
        "5w4xNGg6BlXycQsOXXP7Uqk8s5fTxBEKhR9gfABehJTuHwqEkeaQgT44xtgeaEOnbqH2" + "\n" +
        "thm0cucWQnSsGOJdR2IZ6RYVP+ix4hOZ3vsCAwEAAaMyMDAwHQYDVR0OBBYEFI92/CSa" + "\n" +
        "lBAhQnaSMXtuaF3G4f9TMA8GA1UdEwEB/wQFMAMBAf8wDQYJKoZIhvcNAQEMBQADggEB" + "\n" +
        "AJy35loMlTYxvzsaqqlXMBOuaMULP/qZXyiZhX0H3sKtYxZVBdHLS/CG2k4mXtOQHNqq" + "\n" +
        "Gz+JQo0LS4VoxNCPrAky9tvubH4eSoF3xGJIdThVDBhcn0T2rYjb9z52xA1p+GIoc9Gi" + "\n" +
        "Fv6vh1l4OkhLq9ymIwnM+CjBUNOW6fwojo7c7o/qxk0+qPijanzhxh9ffIrRqQmQom9W" + "\n" +
        "pelY03y0XZKuAGxb0cuRBOCkH4Jg0RJhbJoR08wn3jEJdQ7t5I65VcOjJ0KzyRzV/g3V" + "\n" +
        "C0Wwnv9j7RSSuxdcwrNYVm3vgDQiZWlXDcYJob2fn2H+q0hohK0mdyw43fyDkxSsWiFq" + "\n" +
        "UF8=";
    static byte[] fromB64(String s) { return Base64.getMimeDecoder().decode(s); }
    static java.security.cert.X509Certificate fixtureCert(String b64) throws Exception {
        return (java.security.cert.X509Certificate) java.security.cert.CertificateFactory.getInstance("X.509")
            .generateCertificate(new java.io.ByteArrayInputStream(fromB64(b64)));
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        X509Certificate cert = fixtureCert(ROOT_CERT_B64);
        PEMEncoder enc = PEMEncoder.of();
        check(enc != null, "of");
        check(PEMEncoder.of() == enc, "shared instance");
        // encode a certificate directly (BinaryEncodable)
        String text = enc.encodeToString(cert);
        check(text.startsWith("-----BEGIN CERTIFICATE-----"), "cert begin");
        check(text.trim().endsWith("-----END CERTIFICATE-----"), "cert end");
        // round trip through PEMDecoder reproduces the certificate
        check(PEMDecoder.of().decode(text).equals(cert), "round trip equal");
        // byte[] and String variants agree
        check(java.util.Arrays.equals(enc.encode(cert), text.getBytes(StandardCharsets.US_ASCII)), "encode bytes");
        // encoding a manually built PEM renders the declared type
        PEM custom = new PEM("JCK DATA", "aGVsbG8=");
        String customText = enc.encodeToString(custom);
        check(customText.startsWith("-----BEGIN JCK DATA-----"), "custom begin");
        check(customText.trim().endsWith("-----END JCK DATA-----"), "custom end");
        // body decodes to the content
        String body = customText.replaceAll("-----[A-Z0-9 ]+-----", "").replace("\n", "").replace("\r", "");
        check(body.equals("aGVsbG8="), "body");
        // PEM.toString matches encoder output for the same object
        check(enc.encodeToString(custom).equals(custom.toString()), "toString == encode");
        // withEncryption returns a new encoder
        PEMEncoder enc2 = enc.withEncryption("jckpass".toCharArray());
        check(enc2 != null && enc2 != enc, "withEncryption instance");
        // encoding a PrivateKey produces PRIVATE KEY framing
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        PrivateKey priv = kpg.generateKeyPair().getPrivate();
        String keyText = enc.encodeToString(priv);
        check(keyText.startsWith("-----BEGIN PRIVATE KEY-----"), "key begin");
        check(((PrivateKey) PEMDecoder.of().decode(keyText)).getAlgorithm().equals("RSA"), "key round trip");
        // PublicKey produces PUBLIC KEY framing
        PublicKey pub = cert.getPublicKey();
        String pubText = enc.encodeToString(pub);
        check(pubText.startsWith("-----BEGIN PUBLIC KEY-----"), "pub begin");
        check(PEMDecoder.of().decode(pubText).equals(pub), "pub round trip");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
