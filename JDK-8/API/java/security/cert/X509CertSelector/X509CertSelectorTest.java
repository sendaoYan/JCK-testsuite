/*
 * @test
 * @summary java.security.cert.X509CertSelector conformance: full field round-trips, match(), clone(), errors.
 * @run main X509CertSelectorTest
 */

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class X509CertSelectorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        X509CertSelector sel = new X509CertSelector();
        check(sel.getBasicConstraints() == -1, "getBasicConstraints default -1");
        check(sel.getMatchAllSubjectAltNames(), "getMatchAllSubjectAltNames default true");
        check(sel.getSubjectPublicKeyAlgID() == null, "getSubjectPublicKeyAlgID default null");
        check(sel.getNameConstraints() == null, "getNameConstraints default null");
        sel.setBasicConstraints(0);
        check(sel.getBasicConstraints() == 0, "setBasicConstraints/getBasicConstraints");
        BigInteger sn = new BigInteger("1234567890");
        sel.setSerialNumber(sn);
        check(sn.equals(sel.getSerialNumber()), "setSerialNumber/getSerialNumber");
        Date d = new Date();
        sel.setCertificateValid(d);
        check(d.equals(sel.getCertificateValid()), "setCertificateValid/getCertificateValid");
        Object clone = sel.clone();
        check(clone instanceof X509CertSelector, "clone type");
        check(sn.equals(((X509CertSelector) clone).getSerialNumber()), "clone serialNumber preserved");
    }

    // Fixed fixtures generated once with keytool (RSA-2048 self-signed certs CN=jck-root / CN=jck-leaf,
    // validity 2026-11-03..2036-10-31, SHA384withRSA, root serial 0x70bdab2c498601da).
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

    static byte[] fromB64(String s) { return Base64.getMimeDecoder().decode(s); }

    static X509Certificate fixtureCert(String b64) throws Exception {
        return (X509Certificate) CertificateFactory.getInstance("X.509")
            .generateCertificate(new ByteArrayInputStream(fromB64(b64)));
    }

    static void testBlackBox() throws Exception {
        X509Certificate root = fixtureCert(ROOT_CERT_B64);
        X509Certificate leaf = fixtureCert(LEAF_CERT_B64);

        // empty selector matches everything (including a real cert), but never null
        X509CertSelector empty = new X509CertSelector();
        check(empty.match(root), "empty selector matches a certificate");
        check(empty.match(leaf), "empty selector matches another certificate");
        check(!empty.match(null), "empty selector does not match null");

        // setCertificate pins an exact certificate
        X509CertSelector pinned = new X509CertSelector();
        pinned.setCertificate(root);
        check(pinned.match(root), "pinned cert matches itself");
        check(!pinned.match(leaf), "pinned cert excludes others");
        check(root.equals(pinned.getCertificate()), "getCertificate round-trip");
        check(!pinned.match(null), "pinned selector does not match null");

        // serial number filter
        X509CertSelector bySerial = new X509CertSelector();
        bySerial.setSerialNumber(root.getSerialNumber());
        check(bySerial.match(root) && !bySerial.match(leaf), "serial filter");
        check(root.getSerialNumber().equals(bySerial.getSerialNumber()), "serial read back");

        // issuer / subject principal filters
        X509CertSelector byIssuer = new X509CertSelector();
        byIssuer.setIssuer(root.getIssuerX500Principal());
        check(byIssuer.match(root), "issuer matches self-signed root");
        check(byIssuer.getIssuer().equals(root.getIssuerX500Principal()), "getIssuer round-trip");
        check(byIssuer.getIssuerAsString().contains("jck-root"), "getIssuerAsString");
        check(Arrays.equals(byIssuer.getIssuerAsBytes(), root.getIssuerX500Principal().getEncoded()),
            "getIssuerAsBytes round-trip");

        X509CertSelector bySubject = new X509CertSelector();
        bySubject.setSubject(root.getSubjectX500Principal());
        check(bySubject.match(root), "subject filter");
        check(bySubject.getSubject().equals(root.getSubjectX500Principal()), "getSubject round-trip");
        check(bySubject.getSubjectAsString().contains("jck-root"), "getSubjectAsString");
        check(Arrays.equals(bySubject.getSubjectAsBytes(), root.getSubjectX500Principal().getEncoded()),
            "getSubjectAsBytes round-trip");

        // subject public key filter
        X509CertSelector bySpk = new X509CertSelector();
        bySpk.setSubjectPublicKey(root.getPublicKey());
        check(bySpk.match(root) && !bySpk.match(leaf), "subject public key filter");
        check(bySpk.getSubjectPublicKey().equals(root.getPublicKey()), "getSubjectPublicKey round-trip");

        X509CertSelector bySpkBytes = new X509CertSelector();
        bySpkBytes.setSubjectPublicKey(root.getPublicKey().getEncoded());
        check(bySpkBytes.getSubjectPublicKey().equals(root.getPublicKey()), "setSubjectPublicKey(byte[]) round-trip");

        // subject public key algorithm OID filter
        X509CertSelector bySpkAlg = new X509CertSelector();
        bySpkAlg.setSubjectPublicKeyAlgID("1.2.840.113549.1.1.1");
        check(bySpkAlg.match(root), "subject public key alg matches RSA");
        check("1.2.840.113549.1.1.1".equals(bySpkAlg.getSubjectPublicKeyAlgID()), "subject public key alg read back");

        // subject key identifier round-trip and a negative match (root carries an SKI extension)
        check(root.getExtensionValue("2.5.29.14") != null, "root has subject key identifier extension");
        byte[] ski = new byte[20];
        for (int i = 0; i < ski.length; i++) ski[i] = (byte) i;
        X509CertSelector bySki = new X509CertSelector();
        bySki.setSubjectKeyIdentifier(ski);
        check(Arrays.equals(bySki.getSubjectKeyIdentifier(), ski), "getSubjectKeyIdentifier round-trip");
        check(!bySki.match(root), "mismatched subject key identifier excludes cert");

        // authority key identifier byte round-trip
        byte[] aki = new byte[]{4, 3, 2, 1};
        X509CertSelector akiSel = new X509CertSelector();
        akiSel.setAuthorityKeyIdentifier(aki);
        check(Arrays.equals(akiSel.getAuthorityKeyIdentifier(), aki), "authority key identifier round-trip");

        // validity window
        X509CertSelector window = new X509CertSelector();
        window.setCertificateValid(Date.from(java.time.Instant.parse("2030-01-01T00:00:00Z")));
        check(window.match(root), "valid at 2030");
        window.setCertificateValid(Date.from(java.time.Instant.parse("2020-01-01T00:00:00Z")));
        check(!window.match(root), "not yet valid at 2020");
        check(window.getCertificateValid() != null, "validity read back");

        // private key validity round-trip
        X509CertSelector pkv = new X509CertSelector();
        pkv.setPrivateKeyValid(Date.from(java.time.Instant.parse("2030-01-01T00:00:00Z")));
        check(pkv.getPrivateKeyValid() != null, "private key validity read back");

        // basic constraints filter
        check(root.getBasicConstraints() >= 0, "root is a CA (fixture sanity)");
        check(leaf.getBasicConstraints() == -1, "leaf is not a CA (fixture sanity)");
        X509CertSelector caOnly = new X509CertSelector();
        caOnly.setBasicConstraints(0);
        check(caOnly.match(root) && !caOnly.match(leaf), "basicConstraints 0 matches CA root only");
        // -2 selects end-entity certificates; -1 means "no check" (the default)
        X509CertSelector leafOnly = new X509CertSelector();
        leafOnly.setBasicConstraints(-2);
        check(leafOnly.match(leaf) && !leafOnly.match(root), "basicConstraints -2 matches non-CA only");

        // key usage: an all-false array means no constraint
        X509CertSelector ku = new X509CertSelector();
        ku.setKeyUsage(new boolean[9]);
        check(ku.match(root), "all-false key usage imposes no constraint");
        boolean[] kuBits = new boolean[9]; kuBits[0] = true; kuBits[3] = true;
        ku.setKeyUsage(kuBits);
        check(Arrays.equals(ku.getKeyUsage(), kuBits), "key usage read back");

        // extended key usage round-trip
        X509CertSelector eku = new X509CertSelector();
        eku.setExtendedKeyUsage(new HashSet<String>(Arrays.asList("1.3.6.1.5.5.7.3.1")));
        check(eku.getExtendedKeyUsage().equals(new HashSet<String>(Arrays.asList("1.3.6.1.5.5.7.3.1"))),
            "extended key usage round-trip");

        // subject alternative names round-trip
        X509CertSelector san = new X509CertSelector();
        List<List<?>> names = new ArrayList<List<?>>();
        names.add(Arrays.asList(new Integer(2), "example.com"));
        san.setSubjectAlternativeNames(names);
        check(san.getSubjectAlternativeNames().size() == 1, "SAN set size");
        san.addSubjectAlternativeName(7, "127.0.0.1");
        check(san.getSubjectAlternativeNames().size() == 2, "SAN add size");
        san.setMatchAllSubjectAltNames(false);
        check(!san.getMatchAllSubjectAltNames(), "matchAllSubjectAltNames toggle");

        // name constraints round-trip (empty NameConstraints DER)
        X509CertSelector nc = new X509CertSelector();
        byte[] emptyNc = new byte[]{0x30, 0x00};
        nc.setNameConstraints(emptyNc);
        check(Arrays.equals(nc.getNameConstraints(), emptyNc), "name constraints round-trip");

        // policy round-trip
        X509CertSelector pol = new X509CertSelector();
        pol.setPolicy(new HashSet<String>(Arrays.asList("1.2.3.4")));
        check(pol.getPolicy().equals(new HashSet<String>(Arrays.asList("1.2.3.4"))), "policy round-trip");

        // path-to-names round-trip
        X509CertSelector ptn = new X509CertSelector();
        List<List<?>> paths = new ArrayList<List<?>>();
        paths.add(Arrays.asList(new Integer(2), "example.com"));
        ptn.setPathToNames(paths);
        check(ptn.getPathToNames().size() == 1, "pathToNames set size");
        ptn.addPathToName(2, "example.org");
        check(ptn.getPathToNames().size() == 2, "pathToNames add size");

        // clone is a deep, independent copy
        X509CertSelector original = new X509CertSelector();
        original.setSerialNumber(root.getSerialNumber());
        X509CertSelector copy = (X509CertSelector) original.clone();
        check(copy != original, "clone is a distinct object");
        check(copy.getSerialNumber().equals(root.getSerialNumber()), "clone preserves value");
        copy.setSerialNumber(BigInteger.TEN);
        check(original.getSerialNumber().equals(root.getSerialNumber()), "mutating clone does not affect original");

        check(empty.toString().length() > 0, "toString non-empty");

        // error contracts for malformed DER/OID inputs
        try {
            new X509CertSelector().setIssuer(new byte[]{1, 2, 3});
            check(false, "setIssuer(bad bytes) rejected");
        } catch (IOException e) { check(true, "setIssuer(bad bytes) IOException"); }
        try {
            new X509CertSelector().setSubject(new byte[]{1, 2, 3});
            check(false, "setSubject(bad bytes) rejected");
        } catch (IOException e) { check(true, "setSubject(bad bytes) IOException"); }
        try {
            new X509CertSelector().setSubjectPublicKey(new byte[]{1, 2, 3});
            check(false, "setSubjectPublicKey(bad bytes) rejected");
        } catch (IOException e) { check(true, "setSubjectPublicKey(bad bytes) IOException"); }
        try {
            new X509CertSelector().setExtendedKeyUsage(new HashSet<String>(Arrays.asList("not-an-oid")));
            check(false, "setExtendedKeyUsage(bad oid) rejected");
        } catch (IOException e) { check(true, "setExtendedKeyUsage(bad oid) IOException"); }
        try {
            new X509CertSelector().setPolicy(new HashSet<String>(Arrays.asList("not-an-oid")));
            check(false, "setPolicy(bad oid) rejected");
        } catch (IOException e) { check(true, "setPolicy(bad oid) IOException"); }

        // null clearing / no-constraint semantics
        X509CertSelector clearable = new X509CertSelector();
        clearable.setCertificate(root);
        clearable.setCertificate(null);
        check(clearable.getCertificate() == null, "setCertificate(null) clears");
        clearable.setSerialNumber(root.getSerialNumber());
        clearable.setSerialNumber(null);
        check(clearable.getSerialNumber() == null, "setSerialNumber(null) clears");
        clearable.setCertificateValid(new Date());
        clearable.setCertificateValid(null);
        check(clearable.getCertificateValid() == null, "setCertificateValid(null) clears");
        clearable.setKeyUsage(new boolean[9]);
        clearable.setKeyUsage(null);
        check(clearable.getKeyUsage() == null, "setKeyUsage(null) clears");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
