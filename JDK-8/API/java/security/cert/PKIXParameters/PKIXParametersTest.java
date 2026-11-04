/*
 * @test
 * @summary java.security.cert.PKIXParameters conformance: constructors, accessors, unmodifiable views, clone.
 * @run main PKIXParametersTest
 */

import java.io.ByteArrayInputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.cert.CertStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PKIXParametersTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Set<TrustAnchor> anchors = new HashSet<TrustAnchor>();
        anchors.add(new TrustAnchor("CN=Test", java.security.KeyPairGenerator.getInstance("RSA")
            .generateKeyPair().getPublic(), null));
        PKIXParameters params = new PKIXParameters(anchors);
        check(params.getTrustAnchors().size() == 1, "getTrustAnchors");
        check(params.isRevocationEnabled(), "revocation enabled default true");
        params.setRevocationEnabled(false);
        check(!params.isRevocationEnabled(), "setRevocationEnabled");
        check(params.getDate() == null, "getDate null default");
        Date d = new Date();
        params.setDate(d);
        check(d.equals(params.getDate()), "setDate/getDate value");
        check(params.getDate() != d, "setDate clones the Date");
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

    static class NoopChecker extends PKIXCertPathChecker {
        public void init(boolean forward) { }
        public boolean isForwardCheckingSupported() { return false; }
        public Set<String> getSupportedExtensions() { return new HashSet<String>(); }
        public void check(Certificate cert, Collection<String> unresolvedCritExts)
            throws java.security.cert.CertPathValidatorException { }
    }

    static void testBlackBox() throws Exception {
        X509Certificate root = fixtureRoot();
        TrustAnchor anchor = new TrustAnchor(root, null);

        // Set-of-anchors construction
        PKIXParameters p = new PKIXParameters(new HashSet<TrustAnchor>(Arrays.asList(anchor)));
        check(p.getTrustAnchors().size() == 1
            && p.getTrustAnchors().iterator().next().equals(anchor), "anchors read back");

        // default flag values
        check(p.isRevocationEnabled(), "revocation default true");
        check(!p.isExplicitPolicyRequired(), "explicit policy default false");
        check(!p.isPolicyMappingInhibited(), "policy mapping default false");
        check(!p.isAnyPolicyInhibited(), "any policy default false");
        check(p.getPolicyQualifiersRejected(), "qualifiers rejected default true");

        // flag toggles
        p.setRevocationEnabled(false);
        check(!p.isRevocationEnabled(), "revocation disabled");
        p.setRevocationEnabled(true);
        check(p.isRevocationEnabled(), "revocation re-enabled");
        p.setExplicitPolicyRequired(true);
        check(p.isExplicitPolicyRequired(), "explicit policy set");
        p.setPolicyMappingInhibited(true);
        check(p.isPolicyMappingInhibited(), "policy mapping set");
        p.setAnyPolicyInhibited(true);
        check(p.isAnyPolicyInhibited(), "any policy set");
        p.setPolicyQualifiersRejected(false);
        check(!p.getPolicyQualifiersRejected(), "qualifiers rejected toggled");

        // date
        check(p.getDate() == null, "date default null (means current time)");
        Date fixed = Date.from(java.time.Instant.parse("2030-01-01T00:00:00Z"));
        p.setDate(fixed);
        check(p.getDate().equals(fixed), "date read back");
        check(p.getDate() != fixed, "date cloned");

        // initial policies
        p.setInitialPolicies(new HashSet<String>(Arrays.asList("2.5.29.32.0")));
        check(p.getInitialPolicies().equals(new HashSet<String>(Arrays.asList("2.5.29.32.0"))),
            "initial policies round-trip");
        p.setInitialPolicies(null);
        check(p.getInitialPolicies().isEmpty(), "setInitialPolicies(null) empties");

        // cert stores
        CertStore store = CertStore.getInstance("Collection",
            new CollectionCertStoreParameters(new ArrayList<Object>(Arrays.asList(root))));
        p.addCertStore(store);
        check(p.getCertStores().size() == 1, "store added");
        p.setCertStores(new ArrayList<CertStore>(Arrays.asList(store, store)));
        check(p.getCertStores().size() == 2, "stores set");

        // signature provider
        check(p.getSigProvider() == null, "sig provider default null");
        p.setSigProvider("SUN");
        check("SUN".equals(p.getSigProvider()), "sig provider round-trip");

        // target constraints
        check(p.getTargetCertConstraints() == null, "target constraints default null");
        X509CertSelector target = new X509CertSelector();
        target.setCertificate(root);
        p.setTargetCertConstraints(target);
        check(p.getTargetCertConstraints() instanceof X509CertSelector, "target constraints set");

        // cert path checkers
        NoopChecker nc = new NoopChecker();
        p.setCertPathCheckers(new ArrayList<PKIXCertPathChecker>(Arrays.asList(nc)));
        check(p.getCertPathCheckers().size() == 1, "checkers set");
        p.addCertPathChecker(nc);
        check(p.getCertPathCheckers().size() == 2, "checker added");

        // unmodifiable view contracts (javadoc: immutable collections)
        try {
            p.getTrustAnchors().add(anchor);
            check(false, "trust anchors view is unmodifiable");
        } catch (UnsupportedOperationException e) { check(true, "trust anchors add UOE"); }
        try {
            p.getTrustAnchors().clear();
            check(false, "trust anchors view clear unmodifiable");
        } catch (UnsupportedOperationException e) { check(true, "trust anchors clear UOE"); }
        try {
            p.getInitialPolicies().add("1.2.3.4");
            check(false, "initial policies view is unmodifiable");
        } catch (UnsupportedOperationException e) { check(true, "initial policies add UOE"); }
        try {
            p.getCertStores().add(store);
            check(false, "cert stores view is unmodifiable");
        } catch (UnsupportedOperationException e) { check(true, "cert stores add UOE"); }
        try {
            p.getCertPathCheckers().add(nc);
            check(false, "checkers view is unmodifiable");
        } catch (UnsupportedOperationException e) { check(true, "checkers add UOE"); }

        // constructor error contracts
        try {
            new PKIXParameters(new HashSet<TrustAnchor>());
            check(false, "empty anchors rejected");
        } catch (InvalidAlgorithmParameterException e) { check(true, "empty anchors InvalidAlgorithmParameterException"); }
        try {
            new PKIXParameters((Set<TrustAnchor>) null);
            check(false, "null anchors rejected");
        } catch (NullPointerException e) { check(true, "null anchors NPE"); }
        try {
            p.setTrustAnchors(null);
            check(false, "setTrustAnchors(null) rejected");
        } catch (NullPointerException e) { check(true, "setTrustAnchors(null) NPE"); }
        try {
            p.setTrustAnchors(new HashSet<TrustAnchor>());
            check(false, "setTrustAnchors(empty) rejected");
        } catch (InvalidAlgorithmParameterException e) { check(true, "setTrustAnchors(empty) InvalidAlgorithmParameterException"); }

        // clone is independent
        PKIXParameters c = (PKIXParameters) p.clone();
        check(c != p, "clone distinct");
        check(c.getTrustAnchors().size() == 1, "clone anchors preserved");
        check(c.isExplicitPolicyRequired(), "clone flags preserved");
        c.setRevocationEnabled(false);
        c.setExplicitPolicyRequired(false);
        check(p.isRevocationEnabled(), "clone revocation independent");
        check(p.isExplicitPolicyRequired(), "clone explicit-policy independent");
        check(p.toString().length() > 0, "toString non-empty");

        // KeyStore-driven construction: trusted certificate entries become trust anchors
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(null, null);
        ks.setCertificateEntry("root", root);
        PKIXParameters fromKs = new PKIXParameters(ks);
        check(fromKs.getTrustAnchors().size() == 1, "keystore cert entry becomes anchor");

        // a keystore with no trusted certificate entries is invalid
        KeyStore emptyKs = KeyStore.getInstance("PKCS12");
        emptyKs.load(null, null);
        try {
            new PKIXParameters(emptyKs);
            check(false, "empty keystore rejected");
        } catch (InvalidAlgorithmParameterException e) { check(true, "empty keystore InvalidAlgorithmParameterException"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
