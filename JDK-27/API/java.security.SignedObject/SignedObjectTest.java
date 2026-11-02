/*
 * @test
 * @summary java.security.SignedObject conformance: sign and verify round trip.
 * @run main SignedObjectTest
 */

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.SignedObject;

public class SignedObjectTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();

        String payload = "payload";
        Signature signingEngine = Signature.getInstance("SHA256withRSA");
        SignedObject so = new SignedObject(payload, kp.getPrivate(), signingEngine);
        check("SHA256withRSA".equals(so.getAlgorithm()), "getAlgorithm");
        check(so.getSignature() != null && so.getSignature().length > 0, "getSignature");
        check(payload.equals(so.getObject()), "getObject round trip");

        Signature verifyEngine = Signature.getInstance("SHA256withRSA");
        check(so.verify(kp.getPublic(), verifyEngine), "verify true");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
