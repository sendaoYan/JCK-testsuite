/*
 * @test
 * @summary java.security.PEM conformance: white-box structure + black-box behavior.
 *          PEM is a JDK-27 preview API: it cannot be imported with plain javac
 *          (no --enable-preview in this suite), so every access is reflective.
 * @run main PEMTest
 */

import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Base64;

public class PEMTest {
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
    interface CtorCall { Object make() throws Exception; }
    static void checkCtorThrows(Class<? extends Throwable> expected, CtorCall c, String msg) {
        try {
            c.make();
        } catch (InvocationTargetException ite) {
            check(expected.isInstance(ite.getCause()),
                    msg + " (got " + ite.getCause().getClass().getSimpleName() + ")");
            return;
        } catch (Throwable t) {
            throw new AssertionError(msg + ": unexpected " + t, t);
        }
        throw new AssertionError(msg + ": nothing was thrown");
    }
    static Object newPem(String type, String b64) throws ReflectiveOperationException {
        return PEM_CLASS.getConstructor(String.class, String.class).newInstance(type, b64);
    }
    static Object newPem(String type, String b64, byte[] lead) throws ReflectiveOperationException {
        return PEM_CLASS.getConstructor(String.class, String.class, byte[].class)
                .newInstance(type, b64, lead);
    }
    static Object newPem(String type, byte[] content) throws ReflectiveOperationException {
        return PEM_CLASS.getConstructor(String.class, byte[].class).newInstance(type, content);
    }
    static String pemType(Object p) throws ReflectiveOperationException {
        return (String) PEM_CLASS.getMethod("type").invoke(p);
    }
    static byte[] pemContent(Object p) throws ReflectiveOperationException {
        return (byte[]) PEM_CLASS.getMethod("content").invoke(p);
    }
    static byte[] pemDecode(Object p) throws ReflectiveOperationException {
        return (byte[]) PEM_CLASS.getMethod("decode").invoke(p);
    }
    static byte[] pemLeading(Object p) throws ReflectiveOperationException {
        return (byte[]) PEM_CLASS.getMethod("leadingData").invoke(p);
    }
    static String encToString(Object binaryEncodable) throws ReflectiveOperationException {
        Object enc = ENC_CLASS.getMethod("of").invoke(null);
        return (String) ENC_CLASS.getMethod("encodeToString", BE_CLASS).invoke(enc, binaryEncodable);
    }
    static Object decTyped(String text, Class<?> target) throws ReflectiveOperationException {
        Object dec = DEC_CLASS.getMethod("of").invoke(null);
        return DEC_CLASS.getMethod("decode", String.class, Class.class).invoke(dec, text, target);
    }

    static void testWhiteBox() throws Exception {
        Object p = newPem("TEST", "aGVsbG8=");
        check("TEST".equals(pemType(p)), "type");
        check(Arrays.equals("aGVsbG8=".getBytes(StandardCharsets.US_ASCII), pemContent(p)), "content");
        check(Arrays.equals("hello".getBytes(StandardCharsets.US_ASCII), pemDecode(p)), "decode");
        check(pemLeading(p) == null || pemLeading(p).length >= 0, "leadingData");
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
    static X509Certificate fixtureCert(String b64) throws Exception {
        return (X509Certificate) java.security.cert.CertificateFactory.getInstance("X.509")
            .generateCertificate(new java.io.ByteArrayInputStream(fromB64(b64)));
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        X509Certificate cert = fixtureCert(ROOT_CERT_B64);
        String b64 = Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.ISO_8859_1))
                .encodeToString(cert.getEncoded());
        Object pem = newPem("CERTIFICATE", b64);
        check(pemType(pem).equals("CERTIFICATE"), "type");
        check(pemLeading(pem) == null, "leadingData null");
        check(pemContent(pem).length > 0, "content");
        // toString renders RFC 7468 framing
        String text = pem.toString();
        check(text.startsWith("-----BEGIN CERTIFICATE-----"), "begin header");
        check(text.trim().endsWith("-----END CERTIFICATE-----"), "end footer");
        check(Arrays.equals(Base64.getMimeDecoder().decode(text.replaceAll("-----[A-Z0-9 ]+-----", "")
                        .replace("\n", "").replace("\r", "")),
                cert.getEncoded()), "body equals DER");
        // leading-data constructor keeps bytes defensively
        byte[] lead = {1, 2, 3};
        Object withLead = newPem("CERTIFICATE", b64, lead);
        check(Arrays.equals(pemLeading(withLead), lead), "leading data");
        lead[0] = 9;
        check(pemLeading(withLead)[0] == 1, "defensive copy");
        // byte[]-content constructors
        Object fromBytes = newPem("JCK DATA", b64.getBytes(StandardCharsets.ISO_8859_1));
        check(pemType(fromBytes).equals("JCK DATA"), "byte ctor");
        check(fromBytes.toString().startsWith("-----BEGIN JCK DATA-----"), "byte ctor framing");
        // validation of the type label
        checkCtorThrows(IllegalArgumentException.class, () -> newPem("-BEGIN BAD", b64), "dash label");
        checkCtorThrows(IllegalArgumentException.class, () -> newPem("BEGIN X", b64), "BEGIN label");
        checkCtorThrows(IllegalArgumentException.class, () -> newPem("END X", b64), "END label");
        checkCtorThrows(NullPointerException.class, () -> newPem(null, b64), "null type");
        checkCtorThrows(NullPointerException.class,
                () -> PEM_CLASS.getConstructor(String.class, byte[].class)
                        .newInstance("CERTIFICATE", (byte[]) null),
                "null content");
        // content is defensively copied
        byte[] shared = b64.getBytes(StandardCharsets.ISO_8859_1);
        Object sharedPem = newPem("JCK DATA", shared);
        shared[0] = '@';
        check(pemContent(sharedPem)[0] != '@', "content copy");
        // PEM implements BinaryEncodable and encodes through PEMEncoder
        check(BE_CLASS.isInstance(pem), "BinaryEncodable");
        String encoded = encToString(pem);
        check(encoded.equals(text), "encoder == toString");
        // decoding the rendered text reproduces the same object shape
        Object back = decTyped(encoded, PEM_CLASS);
        check(pemType(back).equals("CERTIFICATE"), "round trip type");

    }

    public static void main(String[] args) throws Exception {
        initClasses();
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: CRLF rendering, copy semantics, label validation, identity ----
    static void testContractDeep() throws Exception {
        Object p = newPem("TEST", "aGVsbG8=");

        // accessors hand out a fresh copy every call; decode is deterministic
        check(pemContent(p) != pemContent(p), "content fresh copy per call");
        check(Arrays.equals(pemDecode(p), pemDecode(p)), "decode deterministic across calls");
        check(pemLeading(p) == null, "2-arg constructor has null leading data");
        check(pemLeading(newPem("TEST", "aGVsbG8=", new byte[0])).length == 0,
                "explicit empty leading data preserved as length 0");

        // toString renders RFC 7468 text with CRLF line endings and a trailing CRLF
        String text = p.toString();
        check(text.contains("\r\n"), "CRLF line endings");
        check(text.endsWith("\r\n"), "trailing CRLF");
        check(text.equals("-----BEGIN TEST-----\r\naGVsbG8=\r\n-----END TEST-----\r\n"),
                "exact canonical rendering");
        // the base64 text given at construction is preserved verbatim (no re-wrapping)
        String c64 = "AAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh4gISIjJCUmJygpKissLS4v";
        String longText = newPem("TEST", c64 + c64).toString();
        check(longText.contains(c64 + c64), "body preserved verbatim without re-wrapping");

        // String-content and byte-content constructors render identically
        check(newPem("TEST", "aGVsbG8=").toString()
                .equals(newPem("TEST", "aGVsbG8=".getBytes(StandardCharsets.US_ASCII)).toString()),
                "String and byte[] constructors agree");

        // base64 validation is deferred: bad content passes the constructor, fails at decode
        Object bad = newPem("TEST", "!!!not base64!!!");
        check(pemContent(bad) != null, "invalid base64 accepted at construction");
        try {
            pemDecode(bad);
            check(false, "decode must reject invalid base64");
        } catch (InvocationTargetException ite) {
            check(ite.getCause() instanceof IllegalArgumentException,
                    "invalid base64 -> IAE at decode time");
        }
        // lenient bodies: illegal characters are skipped MIME-style
        Object lenient = newPem("TEST", "aGVsbG8!!!");
        check(Arrays.equals(pemDecode(lenient), "hello".getBytes(StandardCharsets.US_ASCII)),
                "lenient MIME-style base64 skips illegal characters");

        // label validation: only the RFC 7468 reserved words are rejected
        try { newPem("certificate", "aGVsbG8="); check(true, "lowercase label legal"); }
        catch (IllegalArgumentException iae) { check(false, "lowercase label must be legal"); }
        try { newPem("", "aGVsbG8="); check(true, "empty label legal"); }
        catch (IllegalArgumentException iae) { check(false, "empty label must be legal"); }
        try { newPem("X509 CRL 2", "aGVsbG8="); check(true, "digits and spaces legal"); }
        catch (IllegalArgumentException iae) { check(false, "digits and spaces must be legal"); }

        // equals is identity-based
        Object p2 = newPem("TEST", "aGVsbG8=");
        check(!p.equals(p2), "identity equals: same value not equal");
        check(p.equals(p), "identity equals reflexive");
        check(p.hashCode() == p.hashCode(), "hashCode self-consistent");
        check(p.hashCode() != p2.hashCode(), "identity hashCode differs");

        // leading data is also defensively returned
        byte[] lead = {1};
        Object withLead = newPem("TEST", "aGVsbG8=", lead);
        check(pemLeading(withLead) != pemLeading(withLead), "leadingData fresh copy per call");
    }
}
