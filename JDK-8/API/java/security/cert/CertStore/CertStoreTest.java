/*
 * @test
 * @summary java.security.cert.CertStore conformance: getInstance variants, selectors, error contracts.
 * @run main CertStoreTest
 */

import java.io.ByteArrayInputStream;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.InvalidAlgorithmParameterException;
import java.security.cert.CertStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLSelector;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Base64;

public class CertStoreTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        CertStore cs = CertStore.getInstance("Collection", new CollectionCertStoreParameters(new ArrayList<Object>()));
        check("Collection".equals(cs.getType()), "getType");
        check(cs.getProvider() != null, "getProvider non-null");
        check(cs.getCertStoreParameters() != null, "getCertStoreParameters non-null");
        check(cs.getCertificates(null).isEmpty(), "getCertificates empty for empty store");
        check(cs.getCRLs(null).isEmpty(), "getCRLs empty for empty store");
        check(CertStore.getDefaultType() != null, "getDefaultType non-null");
    }

    // Fixed fixtures generated once with keytool (RSA-2048 self-signed certs CN=jck-root / CN=jck-leaf,
    // validity 2026-11-03..2036-10-31, SHA384withRSA) and a v1 CRL issued by jck-root revoking serial 0.
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

    static X509Certificate fixtureCert(String b64) throws Exception {
        return (X509Certificate) CertificateFactory.getInstance("X.509")
            .generateCertificate(new ByteArrayInputStream(fromB64(b64)));
    }

    static X509CRL fixtureCrl() throws Exception {
        return (X509CRL) CertificateFactory.getInstance("X.509")
            .generateCRL(new ByteArrayInputStream(fromB64(ROOT_CRL_B64)));
    }

    static void testBlackBox() throws Exception {
        X509Certificate root = fixtureCert(ROOT_CERT_B64);
        X509Certificate leaf = fixtureCert(LEAF_CERT_B64);
        X509CRL crl = fixtureCrl();

        List<Object> content = new ArrayList<Object>();
        content.add(root);
        content.add(leaf);
        content.add(crl);
        CertStore store = CertStore.getInstance("Collection", new CollectionCertStoreParameters(content));
        check("Collection".equals(store.getType()), "type");
        check(store.getCertStoreParameters() instanceof CollectionCertStoreParameters, "params class");
        check(store.getProvider() != null, "provider");

        // null selector matches everything
        Collection<? extends Certificate> allCerts = store.getCertificates(null);
        check(allCerts.size() == 2, "null selector returns both certs");
        check(allCerts.contains(root) && allCerts.contains(leaf), "null selector contains both");
        check(store.getCRLs(null).size() == 1, "null selector returns the CRL");

        // a null selector result is an independent copy, not a live view of the store
        allCerts.clear();
        check(store.getCertificates(null).size() == 2, "returned collection is a copy");

        // target by subject
        X509CertSelector sel = new X509CertSelector();
        sel.setSubject(root.getSubjectX500Principal());
        Collection<? extends Certificate> found = store.getCertificates(sel);
        check(found.size() == 1 && found.iterator().next().equals(root), "subject selector matches root");
        sel.setSubject(leaf.getSubjectX500Principal());
        check(store.getCertificates(sel).iterator().next().equals(leaf), "subject selector matches leaf");

        // CRL by issuer
        X509CRLSelector csel = new X509CRLSelector();
        csel.addIssuer(root.getIssuerX500Principal());
        check(store.getCRLs(csel).size() == 1, "issuer CRL selector matches");
        csel.setIssuers(java.util.Collections.<javax.security.auth.x500.X500Principal>singleton(leaf.getIssuerX500Principal()));
        check(store.getCRLs(csel).isEmpty(), "no CRL for other issuer");

        // factory variants and error contracts
        CertStore byProvider = CertStore.getInstance("Collection",
            new CollectionCertStoreParameters(content), store.getProvider());
        check("Collection".equals(byProvider.getType()), "provider overload");
        check("Collection".equals(CertStore.getInstance("Collection",
            new CollectionCertStoreParameters(content), store.getProvider().getName()).getType()), "provider-name overload");

        try {
            CertStore.getInstance("Collection", new CollectionCertStoreParameters(content), "NoSuchProvider");
            check(false, "unknown provider rejected");
        } catch (NoSuchProviderException e) {
            check(true, "unknown provider NoSuchProviderException");
        }
        try {
            CertStore.getInstance("BOGUS", new CollectionCertStoreParameters());
            check(false, "unknown type rejected");
        } catch (NoSuchAlgorithmException e) {
            check(true, "unknown type NoSuchAlgorithmException");
        }
        try {
            CertStore.getInstance("LDAP", new CollectionCertStoreParameters());
            check(false, "LDAP with Collection params rejected");
        } catch (InvalidAlgorithmParameterException e) {
            check(true, "wrong params InvalidAlgorithmParameterException");
        }

        check(CertStore.getDefaultType() != null && CertStore.getDefaultType().length() > 0,
            "getDefaultType non-empty");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
