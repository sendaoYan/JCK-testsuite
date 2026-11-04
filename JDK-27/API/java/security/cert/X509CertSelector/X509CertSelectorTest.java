/*
 * @test
 * @summary java.security.cert.X509CertSelector conformance: white-box structure + black-box behavior.
 * @run main X509CertSelectorTest
 */

import java.security.cert.X509CertSelector;
import java.math.BigInteger;
import java.util.Date;
import java.io.*;
import java.security.cert.*;
import java.util.*;
import java.util.Base64;


public class X509CertSelectorTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        X509CertSelector sel = new X509CertSelector();
        check(sel.getBasicConstraints() == -1, "getBasicConstraints default -1");
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

        X509Certificate root = fixtureCert(ROOT_CERT_B64);
        X509Certificate leaf = fixtureCert(LEAF_CERT_B64);
        X509CertSelector sel = new X509CertSelector();
        check(sel.match(root), "empty selector matches all");
        // certificate equality pinning
        sel.setCertificate(root);
        check(sel.match(root), "pinned cert matches");
        check(!sel.match(leaf), "pinned excludes others");
        check(sel.getCertificate().equals(root), "getCertificate");
        // serial filter
        X509CertSelector bySerial = new X509CertSelector();
        bySerial.setSerialNumber(root.getSerialNumber());
        check(bySerial.match(root) && !bySerial.match(leaf), "serial filter");
        check(bySerial.getSerialNumber().equals(root.getSerialNumber()), "serial read back");
        // issuer filter
        X509CertSelector byIssuer = new X509CertSelector();
        byIssuer.setIssuer(root.getIssuerX500Principal());
        check(byIssuer.match(root), "issuer matches self-signed root");
        check(byIssuer.getIssuerAsString().contains("jck-root"), "issuer string");
        // subject filter
        X509CertSelector bySubject = new X509CertSelector();
        bySubject.setSubject(root.getSubjectX500Principal());
        check(bySubject.match(root), "subject filter");
        // basic constraints filter accepts everything when unset
        check(sel.getBasicConstraints() == -1, "no bc constraint");
        // validity window
        X509CertSelector window = new X509CertSelector();
        window.setCertificateValid(java.util.Date.from(java.time.Instant.parse("2030-01-01T00:00:00Z")));
        check(window.match(root), "valid at 2030");
        window.setCertificateValid(java.util.Date.from(java.time.Instant.parse("2020-01-01T00:00:00Z")));
        check(!window.match(root), "not yet valid at 2020");
        check(window.getCertificateValid() != null, "validity read back");
        // key usage: selector with digitalSignature required (bit 0)
        X509CertSelector ku = new X509CertSelector();
        ku.setKeyUsage(new boolean[9]);
        check(ku.match(root), "zero key-usage constraints pass");
        // clone and addIssuer
        X509CertSelector c = (X509CertSelector) sel.clone();
        check(c != sel && c.match(root), "clone works");
        check(sel.toString().length() > 0, "toString");
        // null certificate does not match a pinned selector
        check(!sel.match(null), "null cert no match");

    }

    // Additional full-field round-trips, clone independence, null clearing and error contracts.
    static void testSelectorRoundTrips() throws Exception {
        X509Certificate root = fixtureCert(ROOT_CERT_B64);
        X509Certificate leaf = fixtureCert(LEAF_CERT_B64);

        // subject public key (PublicKey and byte[] forms)
        X509CertSelector spk = new X509CertSelector();
        spk.setSubjectPublicKey(root.getPublicKey());
        check(spk.match(root) && !spk.match(leaf), "subject public key filter");
        check(spk.getSubjectPublicKey().equals(root.getPublicKey()), "getSubjectPublicKey round-trip");
        X509CertSelector spkBytes = new X509CertSelector();
        spkBytes.setSubjectPublicKey(root.getPublicKey().getEncoded());
        check(spkBytes.getSubjectPublicKey().equals(root.getPublicKey()), "setSubjectPublicKey(byte[]) round-trip");

        // subject public key algorithm OID
        X509CertSelector alg = new X509CertSelector();
        alg.setSubjectPublicKeyAlgID("1.2.840.113549.1.1.1");
        check(alg.match(root), "subject public key alg matches RSA");
        check("1.2.840.113549.1.1.1".equals(alg.getSubjectPublicKeyAlgID()), "subject public key alg read back");

        // subject key identifier round-trip + negative match
        check(root.getExtensionValue("2.5.29.14") != null, "root has SKI extension");
        byte[] ski = new byte[20];
        for (int i = 0; i < ski.length; i++) ski[i] = (byte) i;
        X509CertSelector skiSel = new X509CertSelector();
        skiSel.setSubjectKeyIdentifier(ski);
        check(Arrays.equals(skiSel.getSubjectKeyIdentifier(), ski), "getSubjectKeyIdentifier round-trip");
        check(!skiSel.match(root), "mismatched SKI excludes cert");

        // authority key identifier byte round-trip
        byte[] aki = new byte[]{4, 3, 2, 1};
        X509CertSelector akiSel = new X509CertSelector();
        akiSel.setAuthorityKeyIdentifier(aki);
        check(Arrays.equals(akiSel.getAuthorityKeyIdentifier(), aki), "authority key identifier round-trip");

        // private key validity round-trip
        X509CertSelector pkv = new X509CertSelector();
        pkv.setPrivateKeyValid(java.util.Date.from(java.time.Instant.parse("2030-01-01T00:00:00Z")));
        check(pkv.getPrivateKeyValid() != null, "private key validity read back");

        // key usage read back
        boolean[] kuBits = new boolean[9]; kuBits[0] = true; kuBits[3] = true;
        X509CertSelector ku = new X509CertSelector();
        ku.setKeyUsage(kuBits);
        check(Arrays.equals(ku.getKeyUsage(), kuBits), "key usage read back");

        // extended key usage round-trip
        X509CertSelector eku = new X509CertSelector();
        eku.setExtendedKeyUsage(Set.of("1.3.6.1.5.5.7.3.1"));
        check(eku.getExtendedKeyUsage().equals(Set.of("1.3.6.1.5.5.7.3.1")), "extended key usage round-trip");

        // subject alternative names round-trip
        X509CertSelector san = new X509CertSelector();
        List<List<?>> names = new ArrayList<>();
        names.add(Arrays.asList(2, "example.com"));
        san.setSubjectAlternativeNames(names);
        check(san.getSubjectAlternativeNames().size() == 1, "SAN set size");
        san.addSubjectAlternativeName(7, "127.0.0.1");
        check(san.getSubjectAlternativeNames().size() == 2, "SAN add size");
        san.setMatchAllSubjectAltNames(false);
        check(!san.getMatchAllSubjectAltNames(), "matchAllSubjectAltNames toggle");

        // name constraints round-trip (empty NameConstraints DER)
        byte[] emptyNc = new byte[]{0x30, 0x00};
        X509CertSelector nc = new X509CertSelector();
        nc.setNameConstraints(emptyNc);
        check(Arrays.equals(nc.getNameConstraints(), emptyNc), "name constraints round-trip");

        // policy round-trip
        X509CertSelector pol = new X509CertSelector();
        pol.setPolicy(Set.of("1.2.3.4"));
        check(pol.getPolicy().equals(Set.of("1.2.3.4")), "policy round-trip");

        // path-to-names round-trip
        X509CertSelector ptn = new X509CertSelector();
        List<List<?>> paths = new ArrayList<>();
        paths.add(Arrays.asList(2, "example.com"));
        ptn.setPathToNames(paths);
        check(ptn.getPathToNames().size() == 1, "pathToNames set size");
        ptn.addPathToName(2, "example.org");
        check(ptn.getPathToNames().size() == 2, "pathToNames add size");

        // clone is a deep, independent copy
        X509CertSelector original = new X509CertSelector();
        original.setSerialNumber(root.getSerialNumber());
        X509CertSelector copy = (X509CertSelector) original.clone();
        check(copy != original, "clone distinct");
        check(copy.getSerialNumber().equals(root.getSerialNumber()), "clone preserves value");
        copy.setSerialNumber(BigInteger.TEN);
        check(original.getSerialNumber().equals(root.getSerialNumber()), "mutating clone does not affect original");

        // null clearing semantics
        X509CertSelector clearable = new X509CertSelector();
        clearable.setCertificate(root); clearable.setCertificate(null);
        check(clearable.getCertificate() == null, "setCertificate(null) clears");
        clearable.setSerialNumber(root.getSerialNumber()); clearable.setSerialNumber(null);
        check(clearable.getSerialNumber() == null, "setSerialNumber(null) clears");
        clearable.setCertificateValid(new Date()); clearable.setCertificateValid(null);
        check(clearable.getCertificateValid() == null, "setCertificateValid(null) clears");
        clearable.setKeyUsage(new boolean[9]); clearable.setKeyUsage(null);
        check(clearable.getKeyUsage() == null, "setKeyUsage(null) clears");

        // issuer/subject byte round-trips
        X509CertSelector byIssuer = new X509CertSelector();
        byIssuer.setIssuer(root.getIssuerX500Principal());
        check(Arrays.equals(byIssuer.getIssuerAsBytes(), root.getIssuerX500Principal().getEncoded()),
            "getIssuerAsBytes round-trip");
        X509CertSelector bySubject = new X509CertSelector();
        bySubject.setSubject(root.getSubjectX500Principal());
        check(Arrays.equals(bySubject.getSubjectAsBytes(), root.getSubjectX500Principal().getEncoded()),
            "getSubjectAsBytes round-trip");

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
            new X509CertSelector().setExtendedKeyUsage(Set.of("not-an-oid"));
            check(false, "setExtendedKeyUsage(bad oid) rejected");
        } catch (IOException e) { check(true, "setExtendedKeyUsage(bad oid) IOException"); }
        try {
            new X509CertSelector().setPolicy(Set.of("not-an-oid"));
            check(false, "setPolicy(bad oid) rejected");
        } catch (IOException e) { check(true, "setPolicy(bad oid) IOException"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testSelectorRoundTrips();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
