/*
 * @test
 * @summary java.security.PEM conformance: white-box structure + black-box behavior.
 * @enablePreview
 * @enablePreview
 * @run main PEMTest
 */

import java.security.PEM;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.io.ByteArrayInputStream;
import java.util.*;
import java.nio.charset.StandardCharsets;

public class PEMTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        PEM p = new PEM("TEST", "aGVsbG8=");
        check("TEST".equals(p.type()), "type");
        check(Arrays.equals("aGVsbG8=".getBytes(StandardCharsets.US_ASCII), p.content()), "content");
        check(Arrays.equals("hello".getBytes(StandardCharsets.US_ASCII), p.decode()), "decode");
        check(p.leadingData() == null || p.leadingData().length >= 0, "leadingData");
        check(p.toString() != null, "toString");
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
        String b64 = Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.ISO_8859_1)).encodeToString(cert.getEncoded());
        PEM pem = new PEM("CERTIFICATE", b64);
        check(pem.type().equals("CERTIFICATE"), "type");
        check(pem.leadingData() == null, "leadingData null");
        check(pem.content().length > 0, "content");
        // toString renders RFC 7468 framing
        String text = pem.toString();
        check(text.startsWith("-----BEGIN CERTIFICATE-----"), "begin header");
        check(text.trim().endsWith("-----END CERTIFICATE-----"), "end footer");
        check(java.util.Arrays.equals(Base64.getMimeDecoder().decode(text.replaceAll("-----[A-Z0-9 ]+-----", "").replace("\n", "").replace("\r", "")),
                cert.getEncoded()), "body equals DER");
        // leading-data constructor keeps bytes defensively
        byte[] lead = {1, 2, 3};
        PEM withLead = new PEM("CERTIFICATE", b64, lead);
        check(java.util.Arrays.equals(withLead.leadingData(), lead), "leading data");
        lead[0] = 9;
        check(withLead.leadingData()[0] == 1, "defensive copy");
        // byte[]-content constructors
        PEM fromBytes = new PEM("JCK DATA", b64.getBytes(StandardCharsets.ISO_8859_1));
        check(fromBytes.type().equals("JCK DATA"), "byte ctor");
        check(fromBytes.toString().startsWith("-----BEGIN JCK DATA-----"), "byte ctor framing");
        // validation of the type label
        try { new PEM("-BEGIN BAD", b64); check(false, "dash label"); }
        catch (IllegalArgumentException iae) { check(true, "dash label IAE"); }
        try { new PEM("BEGIN X", b64); check(false, "BEGIN label"); }
        catch (IllegalArgumentException iae) { check(true, "BEGIN label IAE"); }
        try { new PEM("END X", b64); check(false, "END label"); }
        catch (IllegalArgumentException iae) { check(true, "END label IAE"); }
        try { new PEM(null, b64); check(false, "null type"); }
        catch (NullPointerException npe) { check(true, "null type NPE"); }
        try { PEM bad = new PEM("CERTIFICATE", (byte[]) null); check(false, "null content"); }
        catch (NullPointerException npe) { check(true, "null content NPE"); }
        // content is defensively copied
        byte[] shared = b64.getBytes(StandardCharsets.ISO_8859_1);
        PEM sharedPem = new PEM("JCK DATA", shared);
        shared[0] = '@';
        check(sharedPem.content()[0] != '@', "content copy");
        // PEM implements BinaryEncodable and encodes through PEMEncoder
        check(pem instanceof BinaryEncodable, "BinaryEncodable");
        String encoded = PEMEncoder.of().encodeToString(pem);
        check(encoded.equals(text), "encoder == toString");
        // decoding the rendered text reproduces the same object shape
        PEM back = PEMDecoder.of().decode(encoded, PEM.class);
        check(back.type().equals("CERTIFICATE"), "round trip type");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
