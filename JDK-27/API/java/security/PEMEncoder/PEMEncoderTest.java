/*
 * @test
 * @summary java.security.PEMEncoder conformance: white-box structure + black-box behavior.
 *          PEMEncoder/PEM/PEMDecoder are JDK-27 preview APIs: they cannot be imported
 *          with plain javac (no --enable-preview in this suite), so access is reflective.
 * @run main PEMEncoderTest
 */

import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Base64;

public class PEMEncoderTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- reflective shims over the preview API ----
    static Class<?> PEM_CLASS, BE_CLASS, ENC_CLASS, DEC_CLASS;
    static void initClasses() throws ClassNotFoundException {
        PEM_CLASS = Class.forName("java.security.PEM");
        BE_CLASS = Class.forName("java.security.BinaryEncodable");
        ENC_CLASS = Class.forName("java.security.PEMEncoder");
        DEC_CLASS = Class.forName("java.security.PEMDecoder");
    }
    static Object newPem(String type, String b64) throws Exception {
        return PEM_CLASS.getConstructor(String.class, String.class).newInstance(type, b64);
    }
    static String pemType(Object p) throws Exception {
        return (String) PEM_CLASS.getMethod("type").invoke(p);
    }
    static byte[] pemDecode(Object p) throws Exception {
        return (byte[]) PEM_CLASS.getMethod("decode").invoke(p);
    }
    static Object encOf() throws Exception {
        return ENC_CLASS.getMethod("of").invoke(null);
    }
    static String encToString(Object enc, Object target) throws Exception {
        return (String) ENC_CLASS.getMethod("encodeToString", BE_CLASS).invoke(enc, target);
    }
    static byte[] encBytes(Object enc, Object target) throws Exception {
        return (byte[]) ENC_CLASS.getMethod("encode", BE_CLASS).invoke(enc, target);
    }
    static Object withEncryption(Object enc, char[] pw) throws Exception {
        return ENC_CLASS.getMethod("withEncryption", char[].class).invoke(enc, pw);
    }
    static Object decOf() throws Exception {
        return DEC_CLASS.getMethod("of").invoke(null);
    }
    static Object decDecode(String text) throws Exception {
        return DEC_CLASS.getMethod("decode", String.class).invoke(decOf(), text);
    }
    static Object decTyped(String text, Class<?> target) throws Exception {
        return DEC_CLASS.getMethod("decode", String.class, Class.class).invoke(decOf(), text, target);
    }
    static void checkThrows(Class<? extends Throwable> expected, ReflectCall r, String msg) {
        try {
            r.run();
        } catch (InvocationTargetException ite) {
            check(expected.isInstance(ite.getCause()),
                    msg + " (got " + ite.getCause().getClass().getSimpleName() + ")");
            return;
        } catch (Throwable t) {
            throw new AssertionError(msg + ": unexpected " + t, t);
        }
        throw new AssertionError(msg + ": nothing was thrown");
    }
    interface ReflectCall { void run() throws Exception; }

    static void testWhiteBox() throws Exception {
        Object p = newPem("TEST", "aGVsbG8=");
        String s = encToString(encOf(), p);
        check(s != null, "encodeToString non-null");
        check(s.contains("-----BEGIN TEST-----"), "has BEGIN");
        check(s.contains("-----END TEST-----"), "has END");
        byte[] b = encBytes(encOf(), p);
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
    static X509Certificate fixtureCert(String b64) throws Exception {
        return (X509Certificate) java.security.cert.CertificateFactory.getInstance("X.509")
            .generateCertificate(new ByteArrayInputStream(fromB64(b64)));
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        X509Certificate cert = fixtureCert(ROOT_CERT_B64);
        Object enc = encOf();
        check(enc != null, "of");
        check(encOf() == enc, "shared instance");
        // encode a certificate directly (BinaryEncodable)
        String text = encToString(enc, cert);
        check(text.startsWith("-----BEGIN CERTIFICATE-----"), "cert begin");
        check(text.trim().endsWith("-----END CERTIFICATE-----"), "cert end");
        // round trip through PEMDecoder reproduces the certificate
        check(decDecode(text).equals(cert), "round trip equal");
        // byte[] and String variants agree
        check(Arrays.equals(encBytes(enc, cert), text.getBytes(StandardCharsets.US_ASCII)), "encode bytes");
        // encoding a manually built PEM renders the declared type
        Object custom = newPem("JCK DATA", "aGVsbG8=");
        String customText = encToString(enc, custom);
        check(customText.startsWith("-----BEGIN JCK DATA-----"), "custom begin");
        check(customText.trim().endsWith("-----END JCK DATA-----"), "custom end");
        // body decodes to the content
        String body = customText.replaceAll("-----[A-Z0-9 ]+-----", "").replace("\n", "").replace("\r", "");
        check(body.equals("aGVsbG8="), "body");
        // PEM.toString matches encoder output for the same object
        check(encToString(enc, custom).equals(custom.toString()), "toString == encode");
        // withEncryption returns a new encoder
        Object enc2 = withEncryption(enc, "jckpass".toCharArray());
        check(enc2 != null && enc2 != enc, "withEncryption instance");
        // encoding a PrivateKey produces PRIVATE KEY framing
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        PrivateKey priv = kpg.generateKeyPair().getPrivate();
        String keyText = encToString(enc, priv);
        check(keyText.startsWith("-----BEGIN PRIVATE KEY-----"), "key begin");
        check(((PrivateKey) decDecode(keyText)).getAlgorithm().equals("RSA"), "key round trip");
        // PublicKey produces PUBLIC KEY framing
        PublicKey pub = cert.getPublicKey();
        String pubText = encToString(enc, pub);
        check(pubText.startsWith("-----BEGIN PUBLIC KEY-----"), "pub begin");
        check(decDecode(pubText).equals(pub), "pub round trip");

    }

    public static void main(String[] args) throws Exception {
        initClasses();
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: null matrix, deterministic rendering, CRLF, encryption config ----
    static void testContractDeep() throws Exception {
        Object enc = encOf();

        // null targets
        checkThrows(NullPointerException.class, () -> encToString(enc, null), "encodeToString(null)");
        checkThrows(NullPointerException.class, () -> encBytes(enc, null), "encode(null)");
        // null password rejected, empty password accepted
        checkThrows(NullPointerException.class, () -> withEncryption(enc, null), "withEncryption(null)");
        check(withEncryption(enc, new char[0]) != null, "withEncryption(empty) legal");

        // canonical rendering: CRLF endings, trailing CRLF, body verbatim
        Object p = newPem("TEST", "aGVsbG8=");
        String text = encToString(enc, p);
        check(text.contains("\r\n"), "CRLF line endings");
        check(text.endsWith("\r\n"), "trailing CRLF");
        check(text.equals("-----BEGIN TEST-----\r\naGVsbG8=\r\n-----END TEST-----\r\n"),
                "exact canonical form");
        // byte[] and String forms agree byte-for-byte
        check(Arrays.equals(encBytes(enc, p), text.getBytes(StandardCharsets.US_ASCII)),
                "encode byte[] == encodeToString bytes");

        // deterministic: same object encodes to identical text twice
        check(encToString(enc, p).equals(encToString(enc, p)), "encoding deterministic");
        // encodeToString(x) equals x.toString() for PEM objects
        check(encToString(enc, p).equals(p.toString()), "encoder output == toString");

        // line folding: bodies are emitted verbatim, header/footer on their own CRLF lines
        String c64 = "AAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh4gISIjJCUmJygpKissLS4v";
        String longText = encToString(enc, newPem("TEST", c64 + c64));
        check(longText.startsWith("-----BEGIN TEST-----\r\n"), "long body header line");
        check(longText.contains(c64 + c64), "long body emitted on a single line (no re-wrap)");

        // round trip through the decoder reproduces the exact PEM
        Object back = decTyped(text, PEM_CLASS);
        check("TEST".equals(pemType(back)), "round-trip type preserved");
        check(Arrays.equals(pemDecode(back), pemDecode(p)), "round-trip decoded bytes preserved");
        check(encToString(enc, back).equals(text), "re-encoding is stable");

        // encoding a certificate twice is stable
        X509Certificate cert = fixtureCert(ROOT_CERT_B64);
        check(encToString(enc, cert).equals(encToString(enc, cert)), "cert encoding deterministic");
    }
}
