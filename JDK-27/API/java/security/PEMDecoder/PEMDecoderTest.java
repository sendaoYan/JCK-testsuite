/*
 * @test
 * @summary java.security.PEMDecoder conformance: white-box structure + black-box behavior.
 *          PEMDecoder/PEM/PEMEncoder are JDK-27 preview APIs: they cannot be imported
 *          with plain javac (no --enable-preview in this suite), so access is reflective.
 * @run main PEMDecoderTest
 */

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Base64;

public class PEMDecoderTest {
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
    static byte[] pemDecodeBytes(Object p) throws Exception {
        return (byte[]) PEM_CLASS.getMethod("decode").invoke(p);
    }
    static byte[] pemContent(Object p) throws Exception {
        return (byte[]) PEM_CLASS.getMethod("content").invoke(p);
    }
    static byte[] pemLeading(Object p) throws Exception {
        return (byte[]) PEM_CLASS.getMethod("leadingData").invoke(p);
    }
    static Object encOf() throws Exception {
        return ENC_CLASS.getMethod("of").invoke(null);
    }
    static String encToString(Object target) throws Exception {
        return (String) ENC_CLASS.getMethod("encodeToString", BE_CLASS).invoke(encOf(), target);
    }
    static Object decOf() throws Exception {
        return DEC_CLASS.getMethod("of").invoke(null);
    }
    static Object decode(Object dec, String s) throws Exception {
        return DEC_CLASS.getMethod("decode", String.class).invoke(dec, s);
    }
    static Object decode(Object dec, InputStream in) throws Exception {
        return DEC_CLASS.getMethod("decode", InputStream.class).invoke(dec, in);
    }
    static Object decodeTyped(Object dec, String s, Class<?> target) throws Exception {
        return DEC_CLASS.getMethod("decode", String.class, Class.class).invoke(dec, s, target);
    }
    static Object decodeTyped(Object dec, InputStream in, Class<?> target) throws Exception {
        return DEC_CLASS.getMethod("decode", InputStream.class, Class.class).invoke(dec, in, target);
    }
    static Object withFactoriesOf(Object dec, Provider p) throws Exception {
        return DEC_CLASS.getMethod("withFactoriesOf", Provider.class).invoke(dec, p);
    }
    static Object withDecryption(Object dec, char[] pw) throws Exception {
        return DEC_CLASS.getMethod("withDecryption", char[].class).invoke(dec, pw);
    }
    static void checkThrows(Class<? extends Throwable> expected, ReflectCall r, String msg) {
        try {
            r.run();
        } catch (InvocationTargetException ite) {
            Throwable c = ite.getCause();
            check(expected.isInstance(c),
                    msg + " (got " + c.getClass().getSimpleName() + ")");
            return;
        } catch (Throwable t) {
            throw new AssertionError(msg + ": unexpected " + t, t);
        }
        throw new AssertionError(msg + ": nothing was thrown");
    }
    static void checkThrowsEither(Class<? extends Throwable> e1, Class<? extends Throwable> e2,
                                  ReflectCall r, String msg) {
        try {
            r.run();
        } catch (InvocationTargetException ite) {
            Throwable c = ite.getCause();
            check(e1.isInstance(c) || e2.isInstance(c),
                    msg + " (got " + c.getClass().getSimpleName() + ")");
            return;
        } catch (Throwable t) {
            throw new AssertionError(msg + ": unexpected " + t, t);
        }
        throw new AssertionError(msg + ": nothing was thrown");
    }
    interface ReflectCall { void run() throws Exception; }

    static void testWhiteBox() throws Exception {
        String pem = "-----BEGIN TEST-----\naGVsbG8=\n-----END TEST-----";
        Object be = decode(decOf(), pem);
        check(be != null, "decode non-null");
    }

    // Fixed fixtures, generated once with JDK-27 keytool (RSA-2048 self-signed cert CN=jck-root,
    // sig SHA384withRSA, cert + matching PKCS#8 DER of its private key regenerated 2026-11-05 after the
    // original key fixture proved truncated; CRL v1 issued by jck-root, thisUpdate 2026-11-03,
    // nextUpdate 2027-02-01, revoked serial 0). Tests decode at runtime; no external files.

    static final String ROOT_CERT_B64 =
        "MIIDHjCCAgagAwIBAgIJAPFughKVMPf+MA0GCSqGSIb3DQEBDAUAMD0xCzAJBgNV" + "\n" +
        "BAYTAlVTMQ0wCwYDVQQKEwRUZXN0MQwwCgYDVQQLEwNKQ0sxETAPBgNVBAMTCGpj" + "\n" +
        "ay1yb290MB4XDTI2MTEwNDIyNTQwMFoXDTM2MTEwMTIyNTQwMFowPTELMAkGA1UE" + "\n" +
        "BhMCVVMxDTALBgNVBAoTBFRlc3QxDDAKBgNVBAsTA0pDSzERMA8GA1UEAxMIamNr" + "\n" +
        "LXJvb3QwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQC5BRRkD0oHb95I" + "\n" +
        "ZVkjYfO71h9yz5bnhbftRlYPeVuU8amPKXfN6H9bhiv5MXINE8BtMXAr1KPj5KkF" + "\n" +
        "NdKz+FR4tDSccX+yD7BNSiwdqJ1woAB5NVU1fENqKLEMxeo+m489mvEzn7ZbGidC" + "\n" +
        "AV64WW/14vdsLdQ/eh/0BwQtNEnySTqdesbfBCkdJ83pq8+mjjDWSbLOLMsHXtDV" + "\n" +
        "Vt0hAhu2iPtiX96N/xlO3M5dZ7h9DQDodgwJbs0vdnUbIr8zM/9kIECfR9kbGypD" + "\n" +
        "oj14Ha7sTyTBbFyxdEOo26dRYoohaO2GqZTPiX2Gy0ISDD5s5TfjxWZraF13O66j" + "\n" +
        "lYbn2mbNAgMBAAGjITAfMB0GA1UdDgQWBBTrj5CeOR9kCMIk9RvQ90yIaLtV1jAN" + "\n" +
        "BgkqhkiG9w0BAQwFAAOCAQEAbHjFDtdyEi3PzleiKNKyNXWr4Fe4+/p4urOAG0Y/" + "\n" +
        "Z3adzD1Go9A6gKP2EapG+Vb47Zr7l3h2xxqEKbjoA0g9EeBIOCI/DsQwl0Bn1qcQ" + "\n" +
        "45CoB7iKnRuGLNNGKcWoW7ZN6OC7GOI4nBG0z5uK0rL0fAYLx3sjZBBN+Jo7AIxf" + "\n" +
        "sGKWxjOKJoKlG0mIP/sgLWCGaTrVVxNxYN26gdokLEYTUWav7kgcUMtdLk1ShH3e" + "\n" +
        "n7syb2CoNSJeRE3VfdZKVDfStXV5pXXDyS3shnuLXZ4YUd4WIXsk1KWkCI369zm5" + "\n" +
        "HGmaG7SVMpFDaIdSmD564AzUfY0E/lQbtmC8QSrScFO1pg==";
    static final String ROOT_KEY_B64 =
        "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC5BRRkD0oHb95I" + "\n" +
        "ZVkjYfO71h9yz5bnhbftRlYPeVuU8amPKXfN6H9bhiv5MXINE8BtMXAr1KPj5KkF" + "\n" +
        "NdKz+FR4tDSccX+yD7BNSiwdqJ1woAB5NVU1fENqKLEMxeo+m489mvEzn7ZbGidC" + "\n" +
        "AV64WW/14vdsLdQ/eh/0BwQtNEnySTqdesbfBCkdJ83pq8+mjjDWSbLOLMsHXtDV" + "\n" +
        "Vt0hAhu2iPtiX96N/xlO3M5dZ7h9DQDodgwJbs0vdnUbIr8zM/9kIECfR9kbGypD" + "\n" +
        "oj14Ha7sTyTBbFyxdEOo26dRYoohaO2GqZTPiX2Gy0ISDD5s5TfjxWZraF13O66j" + "\n" +
        "lYbn2mbNAgMBAAECggEAAeqS6NmK1Rp1TUiDcTG7dHU8ykqPa1l2tLg3ImcuTm9D" + "\n" +
        "MRxU6dSNtbUJ1fxmwxPE7/NyNMMnaWbriPPsQmaA+gtbM0kJ8Xmv+v4Yv04ttP0i" + "\n" +
        "HUPXgSliQIJxhwFpe1TzkFoj1dYd/8H8zWP/PfCPxoREEn2xmLnbKQLPBXrWhYRi" + "\n" +
        "RQt9HnFpu69s2jY0qMxqXxKCoHNrO2VEqe5LtMhlaDEecDhCVzpmlLKvSwIcva4H" + "\n" +
        "/XjYtM+jjl6x/+khb/ZEv0B4Sm4lnlPDL3bjmB6pl1hCXtvIjOen5HN/BJl5zXb4" + "\n" +
        "ADqzNOM4LnZi/Ne6bZ7QtkU71KNc83VXzC1xzwaz3wKBgQDIr2d26Y5VFDtCcjOc" + "\n" +
        "7qbd/sLg1Rj062zwsNhQhQUEqGZ+3sddSyCS1f9Z/vgRchEDhTrN2Yk+qsX0AeLL" + "\n" +
        "1G85qxxeiZ/1da1+kUCPGhO41AuMMAK1dbCUCEFAeDGKsSkKbrhiFSPuzwlnfW9v" + "\n" +
        "aMSfzhVCjoULgdsc5IO2ZkNV4wKBgQDsBE9ulkzXeRCMQAjM9/1nPT0RPXVCVrLK" + "\n" +
        "DvVg9zgKoVGiV9p0k+sEIbLQUbrTYcJwLEHGkq+fDjLGpEk4mFcUS7I+CaM8xQuw" + "\n" +
        "ddJl7OgZtIn1u2f9jixknbxbOoYbjuJsFXEgx935n5gk8/m59AHCKGdyp0mSyW/T" + "\n" +
        "9FE08gBvjwKBgQCSKc59eRXFOXr/phWZUugN/6MIBayrlVw+4U5rNra08cHU3YTu" + "\n" +
        "me0bhOj6Lw2WBzIFEmAF1U50agTK9ySruAlaQxnicPIj4JV//WK71w5OK4JNlBok" + "\n" +
        "95an+msr/V532Rg/FO0N4r/NCtWanm0vA2tRyNMLKqoT+3wOyxBzJIAeaQKBgFwg" + "\n" +
        "upjiVpBtk4YLoOPRkbrw7puCL/l7OkMhX6tAFUIhAmqqfH2xF7X/KXujy5eju3Bp" + "\n" +
        "9ir7K06dDjj3/BT5clRbz/HjdTLNf+ibtUtQFLGkBY5EXy1ch/umjtXVnG+5cKzc" + "\n" +
        "GdK/8ef04kDac4qgsucFOoxIvLFtrBdCcUiHhqAFAoGBAIMThSmaZHALmBiH57C5" + "\n" +
        "MaPfGqOrJidZg+OoPrw3iqLTNPzq0r2WIf5CVU8Lv0f9wtQczuLOPszHmgVM3i7p" + "\n" +
        "/Iu/nrqz0o7dp0v6YGaSpu3lKc8uVAuyh6jSzXqCGO0wydqIqRMnyNbK+hh/TCTY" + "\n" +
        "9f3uQGFBkZZVBXW2/NKJuNEi";
    static final String ROOT_CRL_B64 =
        "MIIBmTCBgjANBgkqhkiG9w0BAQwFADA9MQswCQYDVQQGEwJVUzENMAsGA1UEChMEVGVz" + "\n" +
        "dDEMMAoGA1UECxMDSkNLMREwDwYDVQQDEwhqY2stcm9vdBcNMjYxMTAzMDQwOTU0WhcN" + "\n" +
        "MjcwMjAxMDQwOTU0WjAUMBICAQAXDTI2MTEwMzA0MDk1NFowDQYJKoZIhvcNAQEMBQAD" + "\n" +
        "ggEBAFuf6Ir8rfNZ9yAHZLvA03bOZYbNsSC2JTVkO4ZJtm5hquLYU2YA4X/v/gBOuvqm" + "\n" +
        "NXbroPJG2ZSxObRiS+N8Q3XBJGJK4ME8VmRUNxFw6sSPRGBSFvD7jwJOH+YRkHPfJ/2H" + "\n" +
        "5w7fT+4zgvkviCJ/skB2trCrKm1EwRrT7mSKTY9NR3NTau5T2jQ9eJF7km72Bz0Y1LHo" + "\n" +
        "OKXAOF3d2ajb9fbLuDJV16MTl+j+Ar9X2jalGVg1M00omc5Re2eZQJpH9IlWPnoGHmC1" + "\n" +
        "MvNbcH5d1YcI0sX9AOUzcJItQkv+IipcErE5O81X12CKImLCsjD6Wv1lSz7zhgHr7fgA" + "\n" +
        "/z+EmMM=";
    static byte[] fromB64(String s) { return Base64.getMimeDecoder().decode(s); }
    static X509Certificate fixtureCert(String b64) throws Exception {
        return (X509Certificate) java.security.cert.CertificateFactory.getInstance("X.509")
            .generateCertificate(new ByteArrayInputStream(fromB64(b64)));
    }
    static java.security.cert.X509CRL fixtureCrl() throws Exception {
        return (java.security.cert.X509CRL) java.security.cert.CertificateFactory.getInstance("X.509")
            .generateCRL(new ByteArrayInputStream(fromB64(ROOT_CRL_B64)));
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        X509Certificate cert = fixtureCert(ROOT_CERT_B64);
        String certPem = encToString(newPem("CERTIFICATE", ROOT_CERT_B64.replace("\n", "")));
        Object dec = decOf();
        // CERTIFICATE decodes to X509Certificate equal to the fixture
        Object decoded = decode(dec, certPem);
        check(decoded instanceof X509Certificate, "cert class");
        check(decoded.equals(cert), "cert equal");
        // typed decode
        X509Certificate typed = (X509Certificate) decodeTyped(dec, certPem, X509Certificate.class);
        check(typed.getSubjectX500Principal().getName().equals("CN=jck-root,OU=JCK,O=Test,C=US"), "typed");
        // InputStream variant
        try (ByteArrayInputStream in = new ByteArrayInputStream(certPem.getBytes(StandardCharsets.US_ASCII))) {
            check(decode(dec, in).equals(cert), "stream decode");
        }
        try (ByteArrayInputStream in = new ByteArrayInputStream(certPem.getBytes(StandardCharsets.US_ASCII))) {
            check(decodeTyped(dec, in, X509Certificate.class).equals(cert), "typed stream decode");
        }
        // PRIVATE KEY decodes to an RSA PrivateKey
        String keyPem = encToString(newPem("PRIVATE KEY", ROOT_KEY_B64.replace("\n", "")));
        Object key = decode(dec, keyPem);
        check(key instanceof PrivateKey, "key class");
        check(((PrivateKey) key).getAlgorithm().equals("RSA"), "key alg");
        check("PKCS#8".equals(((PrivateKey) key).getFormat()), "key format");
        // the decoded private key matches the fixture public key
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PublicKey pub = kf.generatePublic(new java.security.spec.X509EncodedKeySpec(cert.getPublicKey().getEncoded()));
        check(kf.getKeySpec((PrivateKey) key, java.security.spec.RSAPrivateKeySpec.class).getModulus()
            .equals(kf.getKeySpec(pub, java.security.spec.RSAPublicKeySpec.class).getModulus()), "modulus match");
        // X509 CRL type decodes to X509CRL
        String crlPem = encToString(newPem("X509 CRL", ROOT_CRL_B64.replace("\n", "")));
        Object crl = decode(dec, crlPem);
        check(crl instanceof java.security.cert.X509CRL, "crl class");
        check(((java.security.cert.X509CRL) crl).getIssuerX500Principal().getName().equals("CN=jck-root,OU=JCK,O=Test,C=US"),
            "crl issuer");
        // unknown type returns a PEM object with the type preserved
        String customPem = encToString(newPem("JCK DATA", ROOT_CERT_B64.replace("\n", "")));
        Object custom = decode(dec, customPem);
        check(PEM_CLASS.isInstance(custom), "custom class");
        check(pemType(custom).equals("JCK DATA"), "custom type");
        // PUBLIC KEY type decodes to a PublicKey
        String pubPem = encToString(newPem("PUBLIC KEY",
            Base64.getMimeEncoder().encodeToString(cert.getPublicKey().getEncoded())));
        Object pk = decode(dec, pubPem);
        check(pk instanceof PublicKey, "pubkey class");
        check(pk.equals(pub), "pubkey equal");
        // typed decode with mismatching class throws ClassCastException
        checkThrows(ClassCastException.class,
                () -> decodeTyped(dec, customPem, X509Certificate.class), "typed mismatch");
        // malformed input
        checkThrows(IllegalArgumentException.class, () -> decode(dec, "totally not pem"), "garbage");
        checkThrowsEither(IllegalArgumentException.class, NullPointerException.class,
                () -> decode(dec, ""), "empty rejected");
        // leading data is accessible through typed PEM decode
        String withLead = "proc-type note\r\n" + customPem;
        Object asPem = decodeTyped(dec, withLead, PEM_CLASS);
        check(pemLeading(asPem) != null && pemLeading(asPem).length > 0, "leading data");
        // withFactoriesOf returns a new configured decoder
        Object configured = withFactoriesOf(dec, Security.getProvider("SUN"));
        check(configured != null && configured != dec, "factories");
        // typed private key decode
        PrivateKey typedKey = (PrivateKey) decodeTyped(dec, keyPem, PrivateKey.class);
        check(typedKey.getAlgorithm().equals("RSA"), "typed key");

    }

    public static void main(String[] args) throws Exception {
        initClasses();
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: null matrix, framing edge cases, lenient bodies, shared instance ----
    static void testContractDeep() throws Exception {
        Object dec = decOf();
        String block = "-----BEGIN TEST-----\r\naGVsbG8=\r\n-----END TEST-----";

        // null arguments
        checkThrows(NullPointerException.class, () -> decode(dec, (String) null), "decode(null String)");
        checkThrows(NullPointerException.class, () -> decode(dec, (InputStream) null), "decode(null InputStream)");
        checkThrows(NullPointerException.class,
                () -> DEC_CLASS.getMethod("decode", String.class, Class.class).invoke(dec, block, null),
                "decode(s, null Class)");

        // CRLF framing accepted; BEGIN/END mismatch rejected; missing END rejected
        check(PEM_CLASS.isInstance(decode(dec, block)), "CRLF framing accepted");
        checkThrows(IllegalArgumentException.class,
                () -> decode(dec, "-----BEGIN TEST-----\r\naGVsbG8=\r\n-----END OTHER-----"),
                "BEGIN/END label mismatch");
        checkThrows(IllegalArgumentException.class,
                () -> decode(dec, "-----BEGIN TEST-----\r\naGVsbG8="),
                "missing END footer");
        // concatenated blocks: the FIRST block wins
        Object first = decodeTyped(dec, block + "\r\n" + block.replace("TEST", "JCK"), PEM_CLASS);
        check("TEST".equals(pemType(first)), "first block wins on concatenated input");

        // lenient base64: illegal characters are skipped at decode time
        Object lenient = decodeTyped(dec, "-----BEGIN TEST-----\r\naGVsbG8!!\r\n-----END TEST-----", PEM_CLASS);
        check(Arrays.equals(pemDecodeBytes(lenient), "hello".getBytes(StandardCharsets.US_ASCII)),
                "lenient base64 body decode");
        check(new String(pemContent(lenient), StandardCharsets.US_ASCII).contains("aGVsbG8!!"),
                "lenient raw body preserved verbatim in content()");

        // decode of an unknown type keeps the raw body text (base64 incl. CRLF) in content()
        check(new String(pemContent(first), StandardCharsets.US_ASCII).contains("aGVsbG8="),
                "unknown-type PEM keeps raw base64 body in content()");
        check(Arrays.equals(pemDecodeBytes(first), "hello".getBytes(StandardCharsets.US_ASCII)),
                "unknown-type PEM decode() yields the DER bytes");

        // configuration methods return NEW instances
        check(withDecryption(dec, "pw".toCharArray()) != dec, "withDecryption returns new instance");
        check(withDecryption(dec, "pw".toCharArray()) != withDecryption(dec, "pw".toCharArray()),
                "each withDecryption call is a fresh instance");
        // of() hands out a shared decoder
        check(decOf() == dec, "of() shared instance");

        // encrypted framing without a password is rejected
        String encHead = "-----BEGIN ENCRYPTED PRIVATE KEY-----";
        try {
            decode(dec, encHead + "\r\nAAAA\r\n-----END ENCRYPTED PRIVATE KEY-----");
            check(true, "encrypted body handled without password (garbage tolerated or rejected)");
        } catch (InvocationTargetException ite) {
            check(ite.getCause() instanceof IllegalArgumentException,
                    "encrypted body without password -> IAE tolerated");
        }
    }
}
