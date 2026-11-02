/*
 * @test
 * @summary java.security.Signature conformance: SHA256withRSA sign/verify round trip.
 * @run main SignatureTest
 */

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.nio.charset.StandardCharsets;

public class SignatureTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();

        Signature s = Signature.getInstance("SHA256withRSA");
        check("SHA256withRSA".equals(s.getAlgorithm()), "getAlgorithm");
        check(s.getProvider() != null, "getProvider");
        s.initSign(kp.getPrivate());
        byte[] data = "data".getBytes(StandardCharsets.UTF_8);
        s.update(data);
        byte[] sig = s.sign();
        check(sig != null && sig.length > 0, "sign() non-empty");

        s.initVerify(kp.getPublic());
        s.update(data);
        check(s.verify(sig), "verify() true");

        s.initVerify(kp.getPublic());
        s.update("tampered".getBytes(StandardCharsets.UTF_8));
        check(!s.verify(sig), "verify() false on tampered data");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
