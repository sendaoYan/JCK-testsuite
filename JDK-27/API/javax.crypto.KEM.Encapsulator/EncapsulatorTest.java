/*
 * @test
 * @summary javax.crypto.KEM.Encapsulator conformance: ML-KEM encapsulator behavior.
 * @run main EncapsulatorTest
 */

import javax.crypto.*;
import java.security.*;

public class EncapsulatorTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("ML-KEM");
        KeyPair kp = kpg.generateKeyPair();
        KEM kem = KEM.getInstance("ML-KEM");
        KEM.Encapsulator enc = kem.newEncapsulator(kp.getPublic());
        check(enc.providerName() != null, "providerName non-null");
        check(enc.secretSize() == 32, "ML-KEM secretSize 32");
        check(enc.encapsulationSize() > 0, "encapsulationSize positive");
        KEM.Encapsulated e = enc.encapsulate();
        check(e.key() != null && e.encapsulation() != null, "encapsulate non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
