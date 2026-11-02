/*
 * @test
 * @summary javax.crypto.KEM conformance: ML-KEM encapsulation round trip.
 * @run main KEMTest
 */

import javax.crypto.*;
import java.security.*;
import java.util.Arrays;

public class KEMTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("ML-KEM");
        KeyPair kp = kpg.generateKeyPair();
        KEM kem = KEM.getInstance("ML-KEM");
        check("ML-KEM".equals(kem.getAlgorithm()), "getAlgorithm");
        KEM.Encapsulator enc = kem.newEncapsulator(kp.getPublic());
        check(enc.secretSize() > 0 && enc.encapsulationSize() > 0, "sizes positive");
        KEM.Encapsulated e = enc.encapsulate();
        check(e.key() != null && e.encapsulation() != null, "encapsulated non-null");
        KEM.Decapsulator dec = kem.newDecapsulator(kp.getPrivate());
        SecretKey sk = dec.decapsulate(e.encapsulation());
        check(Arrays.equals(sk.getEncoded(), e.key().getEncoded()), "shared secret matches");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
