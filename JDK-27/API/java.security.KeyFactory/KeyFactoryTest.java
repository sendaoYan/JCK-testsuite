/*
 * @test
 * @summary java.security.KeyFactory conformance: RSA generatePublic/generatePrivate/getKeySpec round trip.
 * @run main KeyFactoryTest
 */

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.PrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;

public class KeyFactoryTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();

        KeyFactory kf = KeyFactory.getInstance("RSA");
        check("RSA".equals(kf.getAlgorithm()), "getAlgorithm");
        check(kf.getProvider() != null, "getProvider");

        PublicKey pub = kf.generatePublic(new X509EncodedKeySpec(kp.getPublic().getEncoded()));
        PrivateKey priv = kf.generatePrivate(new PKCS8EncodedKeySpec(kp.getPrivate().getEncoded()));
        check(pub instanceof RSAPublicKey, "generatePublic RSAPublicKey");
        check(priv != null && priv.getAlgorithm().equals("RSA"), "generatePrivate algorithm");

        RSAPublicKeySpec spec = kf.getKeySpec(pub, RSAPublicKeySpec.class);
        check(spec.getModulus().equals(((RSAPublicKey) kp.getPublic()).getModulus()), "getKeySpec modulus");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
