/*
 * @test
 * @summary java.security.KeyPair conformance: full contract coverage.
 *          constructor + getPublic/getPrivate, null-key constructor allowed,
 *          generated RSA keypair non-null with X.509/PKCS#8 formats and
 *          non-trivial encodings.
 * @run main KeyPairTest
 */

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

public class KeyPairTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        // ---- generated keypair ----
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(1024);
        KeyPair gen = kpg.generateKeyPair();
        check(gen.getPublic() != null && gen.getPrivate() != null, "generated keys non-null");
        check("RSA".equals(gen.getPublic().getAlgorithm()), "public getAlgorithm");
        check("RSA".equals(gen.getPrivate().getAlgorithm()), "private getAlgorithm");
        check("X.509".equals(gen.getPublic().getFormat()), "public format X.509");
        check("PKCS#8".equals(gen.getPrivate().getFormat()), "private format PKCS#8");
        check(gen.getPublic().getEncoded() != null && gen.getPublic().getEncoded().length > 64,
                "public encoded non-trivial");
        check(gen.getPrivate().getEncoded() != null && gen.getPrivate().getEncoded().length > 64,
                "private encoded non-trivial");

        // ---- constructor round-trip ----
        KeyPair kp = new KeyPair(gen.getPublic(), gen.getPrivate());
        check(gen.getPublic().equals(kp.getPublic()), "constructor getPublic equals");
        check(gen.getPrivate().equals(kp.getPrivate()), "constructor getPrivate equals");

        // ---- null-key constructor allowed ----
        KeyPair empty = new KeyPair(null, null);
        check(empty.getPublic() == null && empty.getPrivate() == null, "null-key constructor stores nulls");

        // ---- mixed ----
        PublicKey pub = gen.getPublic();
        PrivateKey priv = gen.getPrivate();
        KeyPair mixed = new KeyPair(pub, null);
        check(mixed.getPublic() == pub && mixed.getPrivate() == null, "public-only keypair");
        KeyPair mixed2 = new KeyPair(null, priv);
        check(mixed2.getPublic() == null && mixed2.getPrivate() == priv, "private-only keypair");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
