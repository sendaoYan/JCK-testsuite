/*
 * @test
 * @summary javax.crypto.KeyAgreement conformance: getInstance/init/doPhase/generateSecret.
 * @run main KeyAgreementTest
 */

public class KeyAgreementTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.KeyPairGenerator kpg = java.security.KeyPairGenerator.getInstance("DH");
        kpg.initialize(512);
        java.security.KeyPair a = kpg.generateKeyPair();
        java.security.KeyPair b = kpg.generateKeyPair();
        javax.crypto.KeyAgreement kaA = javax.crypto.KeyAgreement.getInstance("DiffieHellman");
        kaA.init(a.getPrivate());
        kaA.doPhase(b.getPublic(), true);
        byte[] secretA = kaA.generateSecret();
        javax.crypto.KeyAgreement kaB = javax.crypto.KeyAgreement.getInstance("DiffieHellman");
        kaB.init(b.getPrivate());
        kaB.doPhase(a.getPublic(), true);
        byte[] secretB = kaB.generateSecret();
        check(java.util.Arrays.equals(secretA, secretB), "shared secret");
        check("DiffieHellman".equals(kaA.getAlgorithm()), "getAlgorithm");
        check(kaA.getProvider() != null, "getProvider");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
