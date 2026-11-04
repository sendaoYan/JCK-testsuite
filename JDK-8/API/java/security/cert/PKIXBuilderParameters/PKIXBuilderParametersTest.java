/*
 * @test
 * @summary java.security.cert.PKIXBuilderParameters conformance: constructor and getters.
 * @run main PKIXBuilderParametersTest
 */

public class PKIXBuilderParametersTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.KeyPairGenerator kpg = java.security.KeyPairGenerator.getInstance("RSA");
        kpg.initialize(512);
        java.security.PublicKey pk = kpg.generateKeyPair().getPublic();
        java.util.Set<java.security.cert.TrustAnchor> anchors = new java.util.HashSet<java.security.cert.TrustAnchor>();
        anchors.add(new java.security.cert.TrustAnchor("CN=Test", pk, null));
        java.security.cert.X509CertSelector sel = new java.security.cert.X509CertSelector();
        java.security.cert.PKIXBuilderParameters p = new java.security.cert.PKIXBuilderParameters(anchors, sel);
        check(anchors.equals(p.getTrustAnchors()), "getTrustAnchors");
        check(p.getTargetCertConstraints() != null, "getTargetCertConstraints");
        check(p.getMaxPathLength() >= -1, "getMaxPathLength");
        p.setRevocationEnabled(false);
        check(!p.isRevocationEnabled(), "setRevocationEnabled false");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
