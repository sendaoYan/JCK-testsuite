/*
 * @test
 * @summary java.security.cert.PKIXParameters conformance: white-box structure + black-box behavior.
 * @run main PKIXParametersTest
 */

import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.KeyPairGenerator;
import java.util.HashSet;
import java.util.Date;
import java.io.*;
import java.security.cert.*;
import java.util.*;
import java.util.Base64;


public class PKIXParametersTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        HashSet<TrustAnchor> anchors = new HashSet<>();
        anchors.add(new TrustAnchor("CN=Test", KeyPairGenerator.getInstance("RSA").generateKeyPair().getPublic(), null));
        PKIXParameters params = new PKIXParameters(anchors);
        check(params.getTrustAnchors().size() == 1, "getTrustAnchors");
        check(params.isRevocationEnabled(), "revocation enabled default");
        params.setRevocationEnabled(false);
        check(!params.isRevocationEnabled(), "setRevocationEnabled");
        check(params.getDate() == null, "getDate null default");
        Date d = new Date();
        params.setDate(d);
        check(d.equals(params.getDate()), "setDate/getDate");
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
    static final String LEAF_CERT_B64 =
        "MIIDHjCCAgagAwIBAgIJALwJRLT9RfAjMA0GCSqGSIb3DQEBDAUAMD0xCzAJBgNVBAYT" + "\n" +
        "AlVTMQ0wCwYDVQQKEwRUZXN0MQwwCgYDVQQLEwNKQ0sxETAPBgNVBAMTCGpjay1sZWFm" + "\n" +
        "MB4XDTI2MTEwMzA0MDk1M1oXDTM2MTAzMTA0MDk1M1owPTELMAkGA1UEBhMCVVMxDTAL" + "\n" +
        "BgNVBAoTBFRlc3QxDDAKBgNVBAsTA0pDSzERMA8GA1UEAxMIamNrLWxlYWYwggEiMA0G" + "\n" +
        "CSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDisnQcLGfp7REpIgOeehP9d5UOBnrncfNL" + "\n" +
        "m7LcJ+fRiK03KU//GKdls18S3ypr2eV13f28vdpPhVWpZtYEpcfEDWyvgX19nQj9+6zl" + "\n" +
        "serGqnnrPqMt6M4SNW028Dr7z+xG2TmanDUbSXzsvEWfb7sScH5KWrSKOIl++SOBa9s0" + "\n" +
        "nTlqadjGcn2PLBXlJUiLgk7+uj7o5tsf1nTmRd7cKpLW4BnIQVSFyOvrO94/SmzE/SQU" + "\n" +
        "a7J/trD30b1ma901A/MtaeYKAWRTlevmckGCZMn6UF8WXr9lxJW0T3UBWYqvBlsiym6p" + "\n" +
        "80/YefCqe+byY55i7WUVN5YTKSk3Um/yCnpFAgMBAAGjITAfMB0GA1UdDgQWBBQJj8/0" + "\n" +
        "KpspnGDPoSQb7ZCn72zWZjANBgkqhkiG9w0BAQwFAAOCAQEApZSVF4/jEZ2LmUCNryZ3" + "\n" +
        "AXrkDNSrWptBixVWu9vT0RGjXW7rK1OONxcrplssjGLVAnIqW+FuLMFzVy59yKQskGe8" + "\n" +
        "MdHNZdSpKhPO/kkSx6I4uQMCOl4wbPnzkjLZrDa2r4Qr8YQw2SZ9Bb+vaijYIk6gEpJ4" + "\n" +
        "cjBOOCyQFO+Dao6j71L4gxbl8zty5eA0jm2KpDULMXS5rRpKS2FOUHYR40gYpqyonHH5" + "\n" +
        "nzwJ4R1y0o/oct42BLim38UMruvf9I97/tShAvGtwfSofaGKWBwtTRmwwhc16NcL6VKk" + "\n" +
        "Ce9m+wx5FUgwcEGNjv+HJ4b+RgCiKtZWziHX4qrkXKGsKiKrwg==";
    static final String ROOT_CRL_B64 =
        "MIIBmTCBgjANBgkqhkiG9w0BAQwFADA9MQswCQYDVQQGEwJVUzENMAsGA1UEChMEVGVz" + "\n" +
        "dDEMMAoGA1UECxMDSkNLMREwDwYDVQQDEwhqY2stcm9vdBcNMjYxMTAzMDQwOTU0WhcN" + "\n" +
        "MjcwMjAxMDQwOTU0WjAUMBICAQAXDTI2MTEwMzA0MDk1NFowDQYJKoZIhvcNAQEMBQAD" + "\n" +
        "ggEBAFuf6Ir8rfNZ9yAHZLvA03bOZYbNsSC2JTVkO4ZJtm5hquLYU2YA4X/v/gBOuvqm" + "\n" +
        "NXbroPJG2ZSxObRiS+N8Q3XBJGJK4ME8VmRUNxFw6sSPRGBSFvD7jwJOH+YRkHPfJ/2H" + "\n" +
        "5w7fT+4zgvkviCJ/skB2trCrKm1EwRrT7mSKTY9NR3NTau5T2jQ9eJF7km72Bz0Y1LHo" + "\n" +
        "OKXAOF3d2ajb9fbLuDJV16MTl+j+Ar5X2jalGVg1M00omc5Re2eZQJpH9IlWPnoGHmC1" + "\n" +
        "MvNbcH5d1YcI0sX9AOUzcJItQkv+IipcErE5O81X12CKImLCsjD6Wv1lSz7zhgHr7fgA" + "\n" +
        "/z+EmMM=";
    static byte[] fromB64(String s) { return Base64.getMimeDecoder().decode(s); }
    static java.security.cert.X509Certificate fixtureCert(String b64) throws Exception {
        return (java.security.cert.X509Certificate) java.security.cert.CertificateFactory.getInstance("X.509")
            .generateCertificate(new java.io.ByteArrayInputStream(fromB64(b64)));
    }
    static java.security.cert.X509CRL fixtureCrl() throws Exception {
        return (java.security.cert.X509CRL) java.security.cert.CertificateFactory.getInstance("X.509")
            .generateCRL(new java.io.ByteArrayInputStream(fromB64(ROOT_CRL_B64)));
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        // Anchor-driven construction and every flag accessor/mutator pair
        X509Certificate root = fixtureCert(ROOT_CERT_B64);
        TrustAnchor anchor = new TrustAnchor(root, null);
        PKIXParameters p = new PKIXParameters(Set.of(anchor));
        check(p.getTrustAnchors().size() == 1
            && p.getTrustAnchors().iterator().next().equals(anchor), "anchors read back");
        // flags default false; toggle and read
        check(p.isRevocationEnabled(), "revocation default enabled");
        p.setRevocationEnabled(false);
        check(!p.isRevocationEnabled(), "revocation disabled");
        p.setRevocationEnabled(true);
        check(p.isRevocationEnabled(), "revocation re-enabled");
        check(p.isExplicitPolicyRequired() == false, "explicit policy default false");
        p.setExplicitPolicyRequired(true);
        check(p.isExplicitPolicyRequired(), "explicit policy set");
        check(!p.isPolicyMappingInhibited(), "policy mapping default");
        p.setPolicyMappingInhibited(true);
        check(p.isPolicyMappingInhibited(), "policy mapping set");
        check(!p.isAnyPolicyInhibited(), "any policy default");
        p.setAnyPolicyInhibited(true);
        check(p.isAnyPolicyInhibited(), "any policy set");
        check(p.getPolicyQualifiersRejected(), "qualifiers rejected default true");
        p.setPolicyQualifiersRejected(false);
        check(!p.getPolicyQualifiersRejected(), "qualifiers accepted");
        // date defaults to now when unset; fixed date read back exactly
        java.util.Date fixed = java.util.Date.from(java.time.Instant.parse("2030-01-01T00:00:00Z"));
        check(p.getDate() == null, "date default null (current time)");
        p.setDate(fixed);
        check(p.getDate().equals(fixed), "date read back");
        // initial policies
        p.setInitialPolicies(Set.of("2.5.29.32.0"));
        check(p.getInitialPolicies().equals(Set.of("2.5.29.32.0")), "initial policies");
        // cert stores
        CertStore store = CertStore.getInstance("Collection", new CollectionCertStoreParameters(List.of(root)));
        p.addCertStore(store);
        check(p.getCertStores().size() == 1, "store added");
        p.setCertStores(List.of(store, store));
        check(p.getCertStores().size() == 2, "stores set");
        // sig provider
        check(p.getSigProvider() == null, "no sig provider");
        p.setSigProvider("SUN");
        check(p.getSigProvider().equals("SUN"), "sig provider");
        // target constraints
        X509CertSelector target = new X509CertSelector();
        target.setCertificate(root);
        p.setTargetCertConstraints(target);
        check(p.getTargetCertConstraints() instanceof X509CertSelector, "target set");
        // clone keeps content
        PKIXParameters c = (PKIXParameters) p.clone();
        check(c != p, "clone distinct");
        check(c.getTrustAnchors().size() == 1, "clone anchors");
        check(c.isExplicitPolicyRequired(), "clone flags");
        // checker list
        class NoopChecker extends PKIXCertPathChecker {
            public void init(boolean forward) { }
            public boolean isForwardCheckingSupported() { return false; }
            public Set<String> getSupportedExtensions() { return Set.of(); }
            public void check(Certificate cert, Collection<String> unresolvedCritExts) { }
        }
        NoopChecker nc = new NoopChecker();
        p.setCertPathCheckers(List.of(nc));
        check(p.getCertPathCheckers().size() == 1, "checkers set");
        p.addCertPathChecker(nc);
        check(p.getCertPathCheckers().size() == 2, "checker added");
        check(p.toString().length() > 0, "toString");
        // empty anchors rejected
        try { new PKIXParameters(Set.of()); check(false, "empty anchors rejected"); }
        catch (java.security.InvalidAlgorithmParameterException iape) { check(true, "empty anchors IAE"); }
        try { p.setTrustAnchors(null); check(false, "null anchors rejected"); }
        catch (NullPointerException npe) { check(true, "null anchors NPE"); }
        // KeyStore-driven construction
        java.security.KeyStore ks = java.security.KeyStore.getInstance("PKCS12");
        ks.load(null, null);
        ks.setCertificateEntry("root", root);
        PKIXParameters fromKs = new PKIXParameters(ks);
        check(fromKs.getTrustAnchors().size() == 1, "keystore anchors");

    }

    // Unmodifiable view contracts, null-clearing setters and date clone semantics.
    static void testViewsAndEdgeCases() throws Exception {
        X509Certificate root = fixtureCert(ROOT_CERT_B64);
        TrustAnchor anchor = new TrustAnchor(root, null);
        PKIXParameters p = new PKIXParameters(Set.of(anchor));

        // unmodifiable view contracts
        try {
            p.getTrustAnchors().add(anchor);
            check(false, "trust anchors view unmodifiable");
        } catch (UnsupportedOperationException e) { check(true, "trust anchors add UOE"); }
        try {
            p.getTrustAnchors().clear();
            check(false, "trust anchors view clear unmodifiable");
        } catch (UnsupportedOperationException e) { check(true, "trust anchors clear UOE"); }
        try {
            p.getInitialPolicies().add("1.2.3.4");
            check(false, "initial policies view unmodifiable");
        } catch (UnsupportedOperationException e) { check(true, "initial policies add UOE"); }
        CertStore store = CertStore.getInstance("Collection", new CollectionCertStoreParameters(List.of(root)));
        p.addCertStore(store);
        try {
            p.getCertStores().add(store);
            check(false, "cert stores view unmodifiable");
        } catch (UnsupportedOperationException e) { check(true, "cert stores add UOE"); }
        try {
            p.getCertPathCheckers().add(null);
            check(false, "checkers view unmodifiable");
        } catch (UnsupportedOperationException e) { check(true, "checkers add UOE"); }

        // null clears for the list/set setters
        p.setInitialPolicies(null);
        check(p.getInitialPolicies().isEmpty(), "setInitialPolicies(null) empties");
        p.setCertStores(null);
        check(p.getCertStores().isEmpty(), "setCertStores(null) empties");
        p.setCertPathCheckers(null);
        check(p.getCertPathCheckers().isEmpty(), "setCertPathCheckers(null) empties");

        // date clone semantics
        java.util.Date d = new java.util.Date();
        p.setDate(d);
        check(p.getDate().equals(d), "setDate value");
        check(p.getDate() != d, "setDate clones");

        // empty keystore rejected
        java.security.KeyStore emptyKs = java.security.KeyStore.getInstance("PKCS12");
        emptyKs.load(null, null);
        try {
            new PKIXParameters(emptyKs);
            check(false, "empty keystore rejected");
        } catch (java.security.InvalidAlgorithmParameterException e) {
            check(true, "empty keystore InvalidAlgorithmParameterException");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testViewsAndEdgeCases();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
