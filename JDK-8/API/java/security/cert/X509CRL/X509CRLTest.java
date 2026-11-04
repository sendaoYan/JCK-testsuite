/*
 * @test
 * @summary java.security.cert.X509CRL conformance: white-box structure + black-box behavior on a real CRL.
 * @run main X509CRLTest
 */

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.KeyPairGenerator;
import java.security.cert.CRLException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Set;

public class X509CRLTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.cert.X509CRL");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(c.getSuperclass() == Class.forName("java.security.cert.CRL"), "extends CRL");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("java.security.cert.X509Extension"), "implements X509Extension");
        // JDK-8 X509CRL does NOT implement java.security.BinaryEncodable (that arrived later).
        check(!in.contains("java.security.BinaryEncodable"), "no BinaryEncodable on JDK-8");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        String[] methods = {"equals", "getEncoded", "getIssuerDN", "getIssuerX500Principal",
            "getNextUpdate", "getRevokedCertificate", "getRevokedCertificates", "getSigAlgName",
            "getSigAlgOID", "getSigAlgParams", "getSignature", "getTBSCertList", "getThisUpdate",
            "getVersion", "hashCode", "verify"};
        for (String n : methods) check(ms.contains(n), "declares " + n);
    }

    // Fixed fixtures generated once with keytool (RSA-2048 self-signed cert CN=jck-root,
    // validity 2026-11-03..2036-10-31, SHA384withRSA, serial 0x70bdab2c498601da) and a
    // v1 CRL issued by jck-root (thisUpdate 2026-11-03, nextUpdate 2027-02-01, revoked serial 0).
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

    static X509Certificate fixtureRoot() throws Exception {
        return (X509Certificate) CertificateFactory.getInstance("X.509")
            .generateCertificate(new ByteArrayInputStream(fromB64(ROOT_CERT_B64)));
    }

    static X509CRL fixtureCrl() throws Exception {
        return (X509CRL) CertificateFactory.getInstance("X.509")
            .generateCRL(new ByteArrayInputStream(fromB64(ROOT_CRL_B64)));
    }

    static void testBlackBox() throws Exception {
        X509Certificate root = fixtureRoot();
        X509CRL crl = fixtureCrl();

        check("X.509".equals(crl.getType()), "type");
        check(crl.getVersion() == 1, "v1 CRL version");
        check(crl.getIssuerX500Principal().getName().contains("jck-root"), "issuer X500Principal");
        check(crl.getIssuerDN() != null && crl.getIssuerDN().getName().contains("jck-root"), "deprecated issuerDN");
        check("SHA384withRSA".equals(crl.getSigAlgName()), "sig alg name");
        check("1.2.840.113549.1.1.12".equals(crl.getSigAlgOID()), "sig alg OID");
        check(crl.getSigAlgParams() == null, "no sig params");
        check(crl.getThisUpdate() != null, "thisUpdate non-null");
        check(crl.getNextUpdate() != null, "nextUpdate non-null");
        check(crl.getNextUpdate().after(crl.getThisUpdate()), "nextUpdate after thisUpdate");
        check(!crl.isRevoked(root), "root serial not revoked");

        // revoked entries by serial
        Set<? extends X509CRLEntry> entries = crl.getRevokedCertificates();
        check(entries != null && entries.size() == 1, "one revoked entry");
        X509CRLEntry e = crl.getRevokedCertificate(BigInteger.ZERO);
        check(e != null && e.getSerialNumber().signum() == 0, "revoked serial 0");
        check(e.getRevocationDate() != null, "revocation date non-null");
        check(crl.getRevokedCertificate(BigInteger.ONE) == null, "serial 1 not revoked");
        check(e.getCertificateIssuer() == null, "v1 entry has no certificateIssuer");
        check(crl.getRevokedCertificate(root) == null, "getRevokedCertificate(cert) misses non-revoked root");
        check(e.getRevocationReason() == null || e.getRevocationReason() instanceof java.security.cert.CRLReason,
            "revocation reason parse");

        // raw ASN.1 surfaces
        byte[] tbs = crl.getTBSCertList();
        check(tbs != null && tbs.length > 0, "TBSCertList non-empty");
        byte[] sig = crl.getSignature();
        check(sig != null && sig.length > 0, "signature non-empty");

        // encoded round-trip and value semantics
        byte[] der = crl.getEncoded();
        check(der != null && der.length > 0, "encoded non-empty");
        X509CRL again = (X509CRL) CertificateFactory.getInstance("X.509")
            .generateCRL(new ByteArrayInputStream(der));
        check(again.equals(crl), "encoded round-trip equals");
        check(again.hashCode() == crl.hashCode(), "hashCode consistent");
        check(!crl.equals(root), "not equal to unrelated object");

        // signature verification with the issuing key
        crl.verify(root.getPublicKey());
        check(true, "verify with issuing key succeeds");
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            crl.verify(kpg.generateKeyPair().getPublic());
            check(false, "wrong key rejected");
        } catch (Exception ex) {
            check(ex instanceof java.security.SignatureException || ex instanceof java.security.InvalidKeyException
                || ex instanceof CRLException, "wrong key rejected with checked exception");
        }

        // X509Extension contract: null (not empty) when no extensions are present
        check(!crl.hasUnsupportedCriticalExtension(), "no unsupported critical extension");
        check(crl.getCriticalExtensionOIDs() == null, "no critical extensions");
        check(crl.getNonCriticalExtensionOIDs() == null, "no non-critical extensions");
        check(crl.getExtensionValue("2.5.29.99") == null, "unknown extension null");
        check(crl.toString().length() > 0, "toString non-empty");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
