/*
 * @test
 * @summary java.security.cert.PKIXParameters conformance: constructor and getters.
 * @run main PKIXParametersTest
 */

public class PKIXParametersTest {
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
        java.security.cert.PKIXParameters p = new java.security.cert.PKIXParameters(anchors);
        check(anchors.equals(p.getTrustAnchors()), "getTrustAnchors");
        check(p.getInitialPolicies() != null, "getInitialPolicies");
        p.setRevocationEnabled(false);
        check(!p.isRevocationEnabled(), "setRevocationEnabled false");
        check(p.getDate() == null, "getDate null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
