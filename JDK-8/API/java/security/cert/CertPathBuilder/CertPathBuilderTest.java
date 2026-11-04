/*
 * @test
 * @summary java.security.cert.CertPathBuilder conformance: PKIX build, factory errors, revocation checker.
 * @run main CertPathBuilderTest
 */

import java.io.ByteArrayInputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathBuilderResult;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.PKIXRevocationChecker;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

public class CertPathBuilderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        String def = CertPathBuilder.getDefaultType();
        check(def != null, "getDefaultType non-null");
        CertPathBuilder cpb = CertPathBuilder.getInstance(def);
        check(cpb.getAlgorithm() != null, "getAlgorithm");
        check(cpb.getProvider() != null, "getProvider");
        check(cpb.getRevocationChecker() != null, "getRevocationChecker");
    }

    // Fixed fixtures generated once with keytool (RSA-2048 self-signed cert CN=jck-root).
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

    static X509Certificate fixtureRoot() throws Exception {
        return (X509Certificate) CertificateFactory.getInstance("X.509")
            .generateCertificate(new ByteArrayInputStream(fromB64(ROOT_CERT_B64)));
    }

    static void testBlackBox() throws Exception {
        X509Certificate root = fixtureRoot();
        CertPathBuilder b = CertPathBuilder.getInstance("PKIX");
        check("PKIX".equals(b.getAlgorithm()), "algorithm PKIX");
        check(b.getProvider() != null, "provider non-null");
        check("PKIX".equals(CertPathBuilder.getInstance("PKIX", b.getProvider()).getAlgorithm()),
            "provider overload");
        check("PKIX".equals(CertPathBuilder.getInstance("PKIX", b.getProvider().getName()).getAlgorithm()),
            "provider-name overload");
        check("PKIX".equals(CertPathBuilder.getDefaultType()), "default type PKIX");

        // error contracts
        try {
            CertPathBuilder.getInstance("BOGUS");
            check(false, "unknown algorithm rejected");
        } catch (NoSuchAlgorithmException e) { check(true, "unknown algorithm NoSuchAlgorithmException"); }
        try {
            CertPathBuilder.getInstance("PKIX", "NoSuchProvider");
            check(false, "unknown provider rejected");
        } catch (NoSuchProviderException e) { check(true, "unknown provider NoSuchProviderException"); }

        // revocation checker is a PKIXRevocationChecker with no default options
        check(b.getRevocationChecker() instanceof PKIXRevocationChecker, "revocation checker type");
        PKIXRevocationChecker rev = (PKIXRevocationChecker) b.getRevocationChecker();
        check(rev.getOptions().isEmpty(), "no default revocation options");

        // build a path for a self-signed trust anchor that is itself the target
        X509CertSelector target = new X509CertSelector();
        target.setCertificate(root);
        Set<TrustAnchor> anchors = new HashSet<TrustAnchor>(Arrays.asList(new TrustAnchor(root, null)));
        PKIXBuilderParameters params = new PKIXBuilderParameters(anchors, target);
        params.setRevocationEnabled(false);
        CertPathBuilderResult res = b.build(params);
        check(res instanceof PKIXCertPathBuilderResult, "PKIX builder result type");
        PKIXCertPathBuilderResult br = (PKIXCertPathBuilderResult) res;
        check(br.getCertPath() != null, "built path non-null");
        // when the target equals the trust anchor, PKIX returns an empty certification path
        check(br.getCertPath().getCertificates().isEmpty(), "empty path when target is the anchor");
        check(br.getTrustAnchor().getTrustedCert().equals(root), "trust anchor reported");
        check(br.getPublicKey().equals(root.getPublicKey()), "target public key reported");
        check(br.getPolicyTree() != null, "policy tree present");

        // an unreachable target fails to build
        X509CertSelector noTarget = new X509CertSelector();
        noTarget.setSubject(new javax.security.auth.x500.X500Principal("CN=no-such-cert"));
        PKIXBuilderParameters bad = new PKIXBuilderParameters(anchors, noTarget);
        bad.setRevocationEnabled(false);
        try {
            b.build(bad);
            check(false, "unreachable target throws");
        } catch (java.security.cert.CertPathBuilderException e) {
            check(true, "unreachable target CertPathBuilderException");
        }

        // null parameters are rejected with a precise exception
        try {
            b.build(null);
            check(false, "null params rejected");
        } catch (InvalidAlgorithmParameterException e) { check(true, "null params InvalidAlgorithmParameterException"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
