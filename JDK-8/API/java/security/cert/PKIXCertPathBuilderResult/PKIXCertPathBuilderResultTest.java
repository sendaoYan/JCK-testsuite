/*
 * @test
 * @summary java.security.cert.PKIXCertPathBuilderResult conformance: constructor and getters.
 * @run main PKIXCertPathBuilderResultTest
 */

public class PKIXCertPathBuilderResultTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.KeyPairGenerator kpg = java.security.KeyPairGenerator.getInstance("RSA");
        kpg.initialize(512);
        java.security.PublicKey pk = kpg.generateKeyPair().getPublic();
        java.security.cert.TrustAnchor ta = new java.security.cert.TrustAnchor("CN=Test", pk, null);
        java.security.cert.CertificateFactory cf = java.security.cert.CertificateFactory.getInstance("X.509");
        java.security.cert.CertPath cp = cf.generateCertPath(new java.util.ArrayList<java.security.cert.Certificate>());
        java.security.cert.PKIXCertPathBuilderResult r = new java.security.cert.PKIXCertPathBuilderResult(cp, ta, null, pk);
        check(cp.equals(r.getCertPath()), "getCertPath");
        check(ta.equals(r.getTrustAnchor()), "getTrustAnchor");
        check(r.getPolicyTree() == null, "getPolicyTree");
        check(pk.equals(r.getPublicKey()), "getPublicKey");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
