/*
 * @test
 * @summary java.security.cert.TrustAnchor conformance: constructor and getters.
 * @run main TrustAnchorTest
 */

public class TrustAnchorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.KeyPairGenerator kpg = java.security.KeyPairGenerator.getInstance("RSA");
        kpg.initialize(512);
        java.security.PublicKey pk = kpg.generateKeyPair().getPublic();
        java.security.cert.TrustAnchor t = new java.security.cert.TrustAnchor("CN=Test", pk, null);
        check("CN=Test".equals(t.getCAName()), "getCAName");
        check(pk.equals(t.getCAPublicKey()), "getCAPublicKey");
        check(t.getTrustedCert() == null, "getTrustedCert null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
