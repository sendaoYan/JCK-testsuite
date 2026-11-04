/*
 * @test
 * @summary java.security.cert.CRL conformance: white-box structure + black-box behavior on a real X509CRL.
 * @run main CRLTest
 */

import java.io.ByteArrayInputStream;
import java.security.cert.CRL;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Base64;

public class CRLTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.cert.CRL");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(c.getSuperclass() == Object.class, "extends Object");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getType"), "declares getType");
        check(ms.contains("isRevoked"), "declares isRevoked");
        check(ms.contains("toString"), "declares toString");
        check(java.lang.reflect.Modifier.isFinal(c.getDeclaredMethod("getType").getModifiers()), "getType final");
    }

    // Fixed fixtures generated once with keytool (RSA-2048 self-signed cert CN=jck-root,
    // validity 2026-11-03..2036-10-31, SHA384withRSA, serial 0x70bdab2c498601da) and a
    // v1 CRL issued by jck-root (thisUpdate 2026-11-03, nextUpdate 2027-02-01, revoked serial 0).
    // Tests decode at runtime; no external files or network.

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
        CRL crl = fixtureCrl();

        // the concrete X509CRL surfaces the base-class contract
        check("X.509".equals(crl.getType()), "real X509CRL type is X.509");
        check(!crl.isRevoked(root), "unrevoked certificate returns false");
        check(crl instanceof X509CRL, "fixture is a concrete X509CRL");

        // a custom subclass drives the protected-type constructor and abstract methods
        class FixedCRL extends CRL {
            FixedCRL() { super("JCK"); }
            public String toString() { return "JCK CRL"; }
            public boolean isRevoked(Certificate c) { return c == null; }
        }
        FixedCRL fc = new FixedCRL();
        check("JCK".equals(fc.getType()), "custom type preserved");
        check("JCK CRL".equals(fc.toString()), "custom toString");
        check(fc.isRevoked(null), "custom verdict for null");
        check(!fc.isRevoked(root), "custom verdict for certificate");

        check(crl.toString().length() > 0, "real toString non-empty");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
