/*
 * @test
 * @summary java.security.CodeSource conformance: white-box structure + black-box behavior.
 * @run main CodeSourceTest
 */

import java.security.CodeSource;
import java.net.URL;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.io.ByteArrayInputStream;
import java.util.*;
import java.security.cert.CertPath;

public class CodeSourceTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        URL loc = new URL("http://example.com/app.jar");
        CodeSource cs = new CodeSource(loc, (java.security.cert.Certificate[]) null);
        check(loc.equals(cs.getLocation()), "getLocation");
        check(cs.getCertificates() == null, "getCertificates null");
        check(cs.getCodeSigners() == null, "getCodeSigners null");
        check(cs.implies(cs), "implies self");
        check(cs.equals(cs), "equals self");
        CodeSource cs2 = new CodeSource(loc, (java.security.cert.Certificate[]) null);
        check(cs.equals(cs2), "equals same location");
        check(cs.hashCode() == cs2.hashCode(), "hashCode equal");
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

        // URL-only CodeSource
        java.net.URL url = new java.net.URL("file:/tmp/jck-code.jar");
        CodeSource cs = new CodeSource(url, (CodeSigner[]) null);
        check(cs.getLocation().equals(url), "location");
        check(cs.getCertificates() == null, "null certs");
        check(cs.getCodeSigners() == null, "null signers");
        check(cs.equals(new CodeSource(new java.net.URL("file:/tmp/jck-code.jar"), (CodeSigner[]) null)), "equals");
        check(cs.hashCode() == new CodeSource(new java.net.URL("file:/tmp/jck-code.jar"), (CodeSigner[]) null).hashCode(),
            "hashCode");
        check(cs.implies(new CodeSource(url, (CodeSigner[]) null)), "implies same");
        check(!cs.implies(new CodeSource(null, (CodeSigner[]) null)), "null location not implied");
        check(!cs.implies(new CodeSource(new java.net.URL("file:/tmp/other.jar"), (CodeSigner[]) null)),
            "different location");
        check(cs.toString().contains("file:/tmp/jck-code.jar"), "toString");
        // Certificate-array constructor clones and returns the certificates
        X509Certificate cert = fixtureCert(ROOT_CERT_B64);
        CodeSource ccs = new CodeSource(url, new java.security.cert.Certificate[]{cert});
        check(ccs.getCertificates() != null && ccs.getCertificates()[0].equals(cert), "cert retained");
        // a cert-carrying CodeSource does not imply a cert-less one with the same URL
        check(!ccs.implies(new CodeSource(url, (CodeSigner[]) null)), "cert not implies bare");
        check(new CodeSource(url, (CodeSigner[]) null).implies(ccs), "bare implies cert");
        // plain directory URL: exact file match only (no recursive implication)
        CodeSource dir = new CodeSource(new java.net.URL("file:/tmp/jckdir/"), (CodeSigner[]) null);
        check(!dir.implies(new CodeSource(new java.net.URL("file:/tmp/jckdir/a.jar"), (CodeSigner[]) null)),
            "plain dir not implies child");
        check(dir.implies(new CodeSource(new java.net.URL("file:/tmp/jckdir/"), (CodeSigner[]) null)),
            "exact match implies");
        // "/-" suffix implies the directory and everything beneath it
        CodeSource deep = new CodeSource(new java.net.URL("file:/tmp/jckdir/-"), (CodeSigner[]) null);
        check(deep.implies(new CodeSource(new java.net.URL("file:/tmp/jckdir/a.jar"), (CodeSigner[]) null)),
            "'/-' implies child");
        check(deep.implies(new CodeSource(new java.net.URL("file:/tmp/jckdir/sub/b.jar"), (CodeSigner[]) null)),
            "'/-' implies nested child");
        check(!deep.implies(new CodeSource(new java.net.URL("file:/tmp/"), (CodeSigner[]) null)),
            "'/-' not implies parent");
        // "/*" suffix implies direct children only
        CodeSource star = new CodeSource(new java.net.URL("file:/tmp/jckdir/*"), (CodeSigner[]) null);
        check(star.implies(new CodeSource(new java.net.URL("file:/tmp/jckdir/a.jar"), (CodeSigner[]) null)),
            "'/*' implies child");
        check(!star.implies(new CodeSource(new java.net.URL("file:/tmp/jckdir/sub/b.jar"), (CodeSigner[]) null)),
            "'/*' not implies nested");
        // http URL implies same URL but not different host
        CodeSource http = new CodeSource(new java.net.URL("http://jck.example/a.jar"), (CodeSigner[]) null);
        check(http.implies(new CodeSource(new java.net.URL("http://jck.example/a.jar"), (CodeSigner[]) null)), "http implies");
        check(!http.implies(new CodeSource(new java.net.URL("http://other.example/a.jar"), (CodeSigner[]) null)),
            "http host");
        // getCodeSigners from the CodeSigner-array constructor
        CertPath cp = CertificateFactory.getInstance("X.509").generateCertPath(java.util.Arrays.asList((java.security.cert.Certificate) cert));
        CodeSigner signer = new CodeSigner(cp, null);
        CodeSource scs = new CodeSource(url, new CodeSigner[]{signer});
        check(scs.getCodeSigners() != null && scs.getCodeSigners()[0].equals(signer), "signer retained");
        check(scs.getCertificates() != null && scs.getCertificates()[0].equals(cert), "certs from signers");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
