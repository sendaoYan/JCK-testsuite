/*
 * @test
 * @summary java.security.cert.CertPathValidator conformance: white-box structure + black-box behavior.
 * @run main CertPathValidatorTest
 */

import java.security.cert.CertPathValidator;
import java.io.*;
import java.security.cert.*;
import java.util.*;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;


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
    static final String ROOT_KEY_B64 =
        "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC6zOLpGbcgoh/c7I5E" + "\n" +
        "TIDrehGJITL/CF2O7cGM1sCyZ0fRA+wZ5XmBQGnkh9e0llAD3VAUx/ooOe/tgyNk3tyC" + "\n" +
        "aPe+K4B46veFF5OXs8tWlJa6XuElftTQpwP84NhN5SDiGjm2a02wU09eo73iLl90ao28" + "\n" +
        "/f8+l4nN1r59gqgTada5xdfxqoTev+jtCPvHpd7pTF86pnNtWD0Lr6SQ2LkddNuobd76" + "\n" +
        "mYbmN3pSxHb00HzkGucOMTRoOgZV8nELDl1z+1KpPLOX08QRCoUfYHwAXoSU7h8KhJHm" + "\n" +
        "kIE+OMbYHmhDp26h9rYZtHLnFkJ0rBjiXUdiGekWFT/oseITmd77AgMBAAECggEAA8Px" + "\n" +
        "26o/3nqWJNag/ROR7tDFYZvCUq2hNn/aTKkXxBKj5K6JgMcFV31d9ttIlh1dxsN/ywcA" + "\n" +
        "Q0rDr4TpJcqMepWYwUsXv3vJ7A/TxMAEOOVJi2oWFogDdzEJVnyNkzadV3bDwg3CczL0" + "\n" +
        "fVgp9RhG+/i2G30bc/59OOi0h0u1G4PtbLdrO6vx6lFxJVX56fQiYm1rEGXEByD8diOV" + "\n" +
        "iYnmpxU5w+CAi0cfFzWmqrnd2nFhvbB7/+iX/ITFXO+ffPrMkbLEpiIOcIEWR5yclF6+" + "\n" +
        "cAdPWIqfr2dQz1lBK8rg3EI1HXOZcyxu3MLjgXUFWZQ7DwulCnUbn6JBenvWcIZ8vQKB" + "\n" +
        "gQDLmx5lP+Uqhi5SUJvo5EYgaD9lXv1FhToD/Kb2hXgb78v6Qw29OFr0GG+1wICoNeXZ" + "\n" +
        "8UAMUpyIU0jhpFDcPMQuWXlQX7i549Y+TJYKTMyst2y1gcG2yhxECnHSb2b++0X6KSlD" + "\n" +
        "p2oRuu2KpnBezbLh9YN6B7dPvo/H1JBlVh5mXQKBgQDq3qvDnTpAruJfAXNCP/CWGU58" + "\n" +
        "0RFoIIc0SWhzWNaqvxNO7c1CMhnw/ojuJrJ/OTEo8pMKzR8mykj7oBMqFgf/RYb4IfRY" + "\n" +
        "dnKnO2fmibdih6PCdvIvVph41l6CbBDhulaxwdwIUFJZXAlxJeaNw8ypFgibHF3pMuXg" + "\n" +
        "aBt+rBlVNwKBgDqH5T0GGUMYP8i6E5fnx6EZVuA1ZmnN2te6keTciaK+VM0k6ZYz1THG" + "\n" +
        "tJZzOIkitWjpyZfBR+6IwrLbIcOuVFdesVlBq0EFFlniel+LpGsib8Xd/2okv+tvdTkh" + "\n" +
        "fWJfqcY4fvxL7Focep0ZK8SVLY93nMw/25q5FWxE1x3KnAKhAoGBAK/aKsbauNvnVSyH" + "\n" +
        "HHI1B4CK0B5aNus8t56pe+ApWwka6W0GVdHDS3MKUSHmrWzeOHWNMhU7QaC8gmqMDu1K" + "\n" +
        "84/o0caFPbribLPTodaVtE28extGDcztzFllP/vsx7MK3pBXMtSytwWbCKLEGtx4Botv" + "\n" +
        "KOBiG9pXtpAPws5UMt9/AoGAYjejGOvORTlBCw8e5za2yOP6FfVV6xRkCMbpemv0+vM6" + "\n" +
        "t0C4GQ8WLSqyUh2US0Um8RYYeuym0LcKaRGhEoEURZ8R2evk0Jkdw7EM8JZc5m7w+i8n" + "\n" +
        "3wje9tols+cDrNd4nZd8zjNC7U+t57kCp84zYFdUkGUjpIxOrSSWW1BMC6U=";
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

        // PKIX validation of a self-signed root path anchored on itself
        X509Certificate root = fixtureCert(ROOT_CERT_B64);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        CertPath cp = cf.generateCertPath(List.of(root));
        CertPathValidator v = CertPathValidator.getInstance("PKIX");
        check(v.getAlgorithm().equals("PKIX"), "algorithm");
        check(v.getProvider() != null, "provider");
        check(CertPathValidator.getInstance("PKIX", v.getProvider()).getAlgorithm().equals("PKIX"), "provider factory");
        try { CertPathValidator.getInstance("PKIX", "NoSuchProvider"); check(false, "unknown provider"); }
        catch (NoSuchProviderException e) { check(true, "unknown provider NoSuchProviderException"); }
        try { CertPathValidator.getInstance("BOGUS"); check(false, "unknown algorithm"); }
        catch (NoSuchAlgorithmException e) { check(true, "unknown algorithm NoSuchAlgorithmException"); }
        PKIXParameters params = new PKIXParameters(Set.of(new TrustAnchor(root, null)));
        params.setRevocationEnabled(false);
        CertPathValidatorResult res = v.validate(cp, params);
        check(res instanceof PKIXCertPathValidatorResult, "PKIX result type");
        PKIXCertPathValidatorResult pr = (PKIXCertPathValidatorResult) res;
        check(pr.getTrustAnchor().getTrustedCert().equals(root), "anchor is the root");
        check(pr.getPublicKey().equals(root.getPublicKey()), "public key of leaf");
        // wrong anchor -> validation failure
        java.security.KeyPairGenerator kpg = java.security.KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        try {
            PKIXParameters wrong = new PKIXParameters(Set.of(
                new TrustAnchor(new javax.security.auth.x500.X500Principal("CN=other"), kpg.generateKeyPair().getPublic(), null)));
            wrong.setRevocationEnabled(false);
            v.validate(cp, wrong);
            check(false, "wrong anchor fails");
        } catch (CertPathValidatorException cpve) {
            check(true, "wrong anchor CertPathValidatorException");
        }
        // null path rejected
        try { v.validate(null, params); check(false, "null path rejected"); }
        catch (NullPointerException npe) { check(true, "null path NPE"); }
        // getRevocationChecker
        PKIXRevocationChecker rev = (PKIXRevocationChecker) v.getRevocationChecker();
        check(rev != null, "revocation checker");
        check(rev.getOptions().isEmpty(), "no default options");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
