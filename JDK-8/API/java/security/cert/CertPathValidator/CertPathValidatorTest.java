/*
 * @test
 * @summary java.security.cert.CertPathValidator conformance: PKIX validate, factory errors, revocation checker.
 * @run main CertPathValidatorTest
 */

import java.io.ByteArrayInputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorResult;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXCertPathValidatorResult;
import java.security.cert.PKIXParameters;
import java.security.cert.PKIXRevocationChecker;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CertPathValidatorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        String def = CertPathValidator.getDefaultType();
        check(def != null, "getDefaultType non-null");
        CertPathValidator cpv = CertPathValidator.getInstance(def);
        check(cpv.getAlgorithm() != null, "getAlgorithm");
        check(cpv.getProvider() != null, "getProvider");
        check(cpv.getRevocationChecker() != null, "getRevocationChecker");
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
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        List<Certificate> chain = new ArrayList<Certificate>();
        chain.add(root);
        CertPath cp = cf.generateCertPath(chain);

        CertPathValidator v = CertPathValidator.getInstance("PKIX");
        check("PKIX".equals(v.getAlgorithm()), "algorithm PKIX");
        check(v.getProvider() != null, "provider non-null");
        check("PKIX".equals(CertPathValidator.getInstance("PKIX", v.getProvider()).getAlgorithm()),
            "provider overload");
        check("PKIX".equals(CertPathValidator.getInstance("PKIX", v.getProvider().getName()).getAlgorithm()),
            "provider-name overload");
        check("PKIX".equals(CertPathValidator.getDefaultType()), "default type PKIX");

        // error contracts
        try {
            CertPathValidator.getInstance("BOGUS");
            check(false, "unknown algorithm rejected");
        } catch (NoSuchAlgorithmException e) { check(true, "unknown algorithm NoSuchAlgorithmException"); }
        try {
            CertPathValidator.getInstance("PKIX", "NoSuchProvider");
            check(false, "unknown provider rejected");
        } catch (NoSuchProviderException e) { check(true, "unknown provider NoSuchProviderException"); }

        // revocation checker
        check(v.getRevocationChecker() instanceof PKIXRevocationChecker, "revocation checker type");
        PKIXRevocationChecker rev = (PKIXRevocationChecker) v.getRevocationChecker();
        check(rev.getOptions().isEmpty(), "no default revocation options");

        // validate a self-signed root anchored on itself -> success
        Set<TrustAnchor> anchors = new HashSet<TrustAnchor>(Arrays.asList(new TrustAnchor(root, null)));
        PKIXParameters params = new PKIXParameters(anchors);
        params.setRevocationEnabled(false);
        CertPathValidatorResult res = v.validate(cp, params);
        check(res instanceof PKIXCertPathValidatorResult, "PKIX validator result type");
        PKIXCertPathValidatorResult pr = (PKIXCertPathValidatorResult) res;
        check(pr.getTrustAnchor().getTrustedCert().equals(root), "anchor is the root");
        check(pr.getPublicKey().equals(root.getPublicKey()), "public key of validated leaf");

        // a wrong anchor fails validation
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        Set<TrustAnchor> wrongAnchors = new HashSet<TrustAnchor>(Arrays.asList(
            new TrustAnchor(new javax.security.auth.x500.X500Principal("CN=other"),
                kpg.generateKeyPair().getPublic(), null)));
        PKIXParameters wrong = new PKIXParameters(wrongAnchors);
        wrong.setRevocationEnabled(false);
        try {
            v.validate(cp, wrong);
            check(false, "wrong anchor fails validation");
        } catch (java.security.cert.CertPathValidatorException e) {
            check(true, "wrong anchor CertPathValidatorException");
        }

        // null argument error contracts
        try {
            v.validate(null, params);
            check(false, "null path rejected");
        } catch (NullPointerException e) { check(true, "null path NPE"); }
        try {
            v.validate(cp, null);
            check(false, "null params rejected");
        } catch (InvalidAlgorithmParameterException e) { check(true, "null params InvalidAlgorithmParameterException"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
