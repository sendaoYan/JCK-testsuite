/*
 * @test
 * @summary javax.crypto.KEM.Decapsulator conformance: ML-KEM decapsulator behavior.
 * @run main DecapsulatorTest
 */

import javax.crypto.*;
import java.security.*;

public class DecapsulatorTest {
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
        KEM.Encapsulated e = enc.encapsulate();
        KEM.Decapsulator dec = kem.newDecapsulator(kp.getPrivate());
        check(dec.providerName() != null, "providerName non-null");
        check(dec.secretSize() == 32, "secretSize 32");
        check(dec.encapsulationSize() > 0, "encapsulationSize positive");
        check(dec.decapsulate(e.encapsulation()) != null, "decapsulate non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
