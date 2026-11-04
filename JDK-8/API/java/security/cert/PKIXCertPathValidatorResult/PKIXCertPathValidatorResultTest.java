/*
 * @test
 * @summary java.security.cert.PKIXCertPathValidatorResult conformance: constructor and getters.
 * @run main PKIXCertPathValidatorResultTest
 */

public class PKIXCertPathValidatorResultTest {
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
        java.security.cert.PKIXCertPathValidatorResult r = new java.security.cert.PKIXCertPathValidatorResult(ta, null, pk);
        check(ta.equals(r.getTrustAnchor()), "getTrustAnchor");
        check(r.getPolicyTree() == null, "getPolicyTree");
        check(pk.equals(r.getPublicKey()), "getPublicKey");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
