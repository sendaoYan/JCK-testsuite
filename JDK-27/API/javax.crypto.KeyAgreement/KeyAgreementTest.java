/*
 * @test
 * @summary javax.crypto.KeyAgreement conformance: X25519 key agreement shared secret.
 * @run main KeyAgreementTest
 */

import javax.crypto.*;
import java.security.*;

public class KeyAgreementTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("X25519");
        KeyPair kp = kpg.generateKeyPair();
        KeyAgreement ka = KeyAgreement.getInstance("X25519");
        check("X25519".equals(ka.getAlgorithm()), "getAlgorithm");
        check(ka.getProvider() != null, "getProvider non-null");
        ka.init(kp.getPrivate());
        ka.doPhase(kp.getPublic(), true);
        check(ka.generateSecret().length == 32, "generateSecret length 32");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
