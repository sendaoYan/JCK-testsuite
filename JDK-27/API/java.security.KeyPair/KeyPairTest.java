/*
 * @test
 * @summary java.security.KeyPair conformance: constructor and accessors.
 * @run main KeyPairTest
 */

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.PrivateKey;

public class KeyPairTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair gen = kpg.generateKeyPair();
        PublicKey pub = gen.getPublic();
        PrivateKey priv = gen.getPrivate();
        KeyPair kp = new KeyPair(pub, priv);
        check(kp.getPublic() == pub, "getPublic same instance");
        check(kp.getPrivate() == priv, "getPrivate same instance");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
