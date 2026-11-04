/*
 * @test
 * @summary java.security.KeyPairGenerator conformance: full contract coverage.
 *          getInstance variants + exact exceptions (NoSuchAlgorithmException /
 *          NullPointerException / IllegalArgumentException / NoSuchProviderException),
 *          initialize(int)/initialize(int,SecureRandom)/initialize(AlgorithmParameterSpec)
 *          incl. negative key size (InvalidParameterException) and wrong/null spec
 *          (InvalidAlgorithmParameterException), generateKeyPair returns non-null
 *          public/private keys with X.509 / PKCS#8 encodings and RSA algorithm,
 *          and getAlgorithm/getProvider.
 * @run main KeyPairGeneratorTest
 */

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.RSAKeyGenParameterSpec;

public class KeyPairGeneratorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    public static void main(String[] args) throws Exception {
        // ---- factory + exception matrix ----
        checkThrows(NoSuchAlgorithmException.class,
                () -> KeyPairGenerator.getInstance("NoSuchKPG_XYZ"), "getInstance(bogus)");
        checkThrows(NullPointerException.class,
                () -> KeyPairGenerator.getInstance(null), "getInstance(null)");
        checkThrows(NullPointerException.class,
                () -> KeyPairGenerator.getInstance(null, Security.getProvider("SunRsaSign")),
                "getInstance(null, Provider)");
        checkThrows(IllegalArgumentException.class,
                () -> KeyPairGenerator.getInstance("RSA", (Provider) null),
                "getInstance(alg, null Provider)");
        checkThrows(IllegalArgumentException.class,
                () -> KeyPairGenerator.getInstance("RSA", (String) null),
                "getInstance(alg, null String)");
        checkThrows(NoSuchProviderException.class,
                () -> KeyPairGenerator.getInstance("RSA", "NoSuchProvider_XYZ"),
                "getInstance(alg, bogus provider)");

        // ---- metadata ----
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        check("RSA".equals(kpg.getAlgorithm()), "getAlgorithm");
        check(kpg.getProvider() != null, "getProvider non-null");

        // ---- initialize(int) + generateKeyPair ----
        kpg.initialize(1024);
        KeyPair kp = kpg.generateKeyPair();
        check(kp.getPublic() != null && kp.getPrivate() != null, "generateKeyPair non-null keys");
        check("RSA".equals(kp.getPublic().getAlgorithm()), "public key algorithm RSA");
        check("RSA".equals(kp.getPrivate().getAlgorithm()), "private key algorithm RSA");
        check("X.509".equals(kp.getPublic().getFormat()), "public key format X.509");
        check("PKCS#8".equals(kp.getPrivate().getFormat()), "private key format PKCS#8");
        check(kp.getPublic().getEncoded() != null && kp.getPublic().getEncoded().length > 100,
                "public key encoded non-trivial");
        check(kp.getPrivate().getEncoded() != null && kp.getPrivate().getEncoded().length > 100,
                "private key encoded non-trivial");

        // ---- initialize(int, SecureRandom) ----
        KeyPairGenerator kpg2 = KeyPairGenerator.getInstance("RSA");
        kpg2.initialize(1024, new SecureRandom());
        KeyPair kp2 = kpg2.generateKeyPair();
        check(kp2.getPublic() != null && kp2.getPrivate() != null, "initialize(int,SecureRandom) keypair");

        // ---- initialize(AlgorithmParameterSpec) with RSAKeyGenParameterSpec ----
        KeyPairGenerator kpg3 = KeyPairGenerator.getInstance("RSA");
        kpg3.initialize(new RSAKeyGenParameterSpec(1024, RSAKeyGenParameterSpec.F4));
        KeyPair kp3 = kpg3.generateKeyPair();
        check(kp3.getPublic() != null && kp3.getPrivate() != null, "initialize(RSAKeyGenParameterSpec) keypair");

        // ---- negative key size -> InvalidParameterException ----
        final KeyPairGenerator kpgNeg = KeyPairGenerator.getInstance("RSA");
        checkThrows(InvalidParameterException.class, () -> kpgNeg.initialize(-1),
                "initialize(-1)");

        // ---- wrong spec type -> InvalidAlgorithmParameterException ----
        final KeyPairGenerator kpgWrong = KeyPairGenerator.getInstance("RSA");
        checkThrows(InvalidAlgorithmParameterException.class,
                () -> kpgWrong.initialize((AlgorithmParameterSpec) MGF1ParameterSpec.SHA256),
                "initialize(wrong spec)");

        // ---- null spec -> InvalidAlgorithmParameterException ----
        final KeyPairGenerator kpgNull = KeyPairGenerator.getInstance("RSA");
        checkThrows(InvalidAlgorithmParameterException.class,
                () -> kpgNull.initialize((AlgorithmParameterSpec) null),
                "initialize(null spec)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
