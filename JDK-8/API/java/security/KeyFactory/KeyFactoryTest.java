/*
 * @test
 * @summary java.security.KeyFactory conformance: getInstance/generatePublic/getKeySpec.
 * @run main KeyFactoryTest
 */

public class KeyFactoryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.KeyPairGenerator kpg = java.security.KeyPairGenerator.getInstance("RSA");
        kpg.initialize(512);
        java.security.KeyPair kp = kpg.generateKeyPair();
        java.security.KeyFactory kf = java.security.KeyFactory.getInstance("RSA");
        check("RSA".equals(kf.getAlgorithm()), "getAlgorithm");
        check(kf.getProvider() != null, "getProvider");
        java.security.spec.X509EncodedKeySpec x509 = kf.getKeySpec(kp.getPublic(), java.security.spec.X509EncodedKeySpec.class);
        java.security.PublicKey pub = kf.generatePublic(x509);
        check(kp.getPublic().equals(pub), "generatePublic/getKeySpec roundtrip");
        check(kf.translateKey(kp.getPublic()) != null, "translateKey");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
