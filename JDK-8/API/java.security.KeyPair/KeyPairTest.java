/*
 * @test
 * @summary java.security.KeyPair conformance: constructor and getters.
 * @run main KeyPairTest
 */

public class KeyPairTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.KeyPairGenerator kpg = java.security.KeyPairGenerator.getInstance("RSA");
        kpg.initialize(512);
        java.security.KeyPair gen = kpg.generateKeyPair();
        java.security.KeyPair kp = new java.security.KeyPair(gen.getPublic(), gen.getPrivate());
        check(gen.getPublic().equals(kp.getPublic()), "getPublic");
        check(gen.getPrivate().equals(kp.getPrivate()), "getPrivate");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
