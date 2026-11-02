/*
 * @test
 * @summary java.security.KeyPairGenerator conformance: RSA keypair generation, getAlgorithm/getProvider.
 * @run main KeyPairGeneratorTest
 */

import java.security.KeyPairGenerator;
import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import java.security.interfaces.RSAPrivateKey;

public class KeyPairGeneratorTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        check("RSA".equals(kpg.getAlgorithm()), "getAlgorithm");
        check(kpg.getProvider() != null, "getProvider");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        check(kp.getPublic() instanceof RSAPublicKey, "public is RSAPublicKey");
        check(kp.getPrivate() instanceof RSAPrivateKey, "private is RSAPrivateKey");
        check(kp.getPublic().getEncoded() != null, "public encoded");
        check(kp.getPrivate().getEncoded() != null, "private encoded");
        check(((RSAPublicKey) kp.getPublic()).getModulus().bitLength() >= 2048, "modulus size");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
