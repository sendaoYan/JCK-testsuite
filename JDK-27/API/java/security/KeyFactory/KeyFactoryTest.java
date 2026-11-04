/*
 * @test
 * @summary java.security.KeyFactory conformance: full contract coverage.
 *          getInstance variants + exact exceptions (NoSuchAlgorithmException /
 *          NullPointerException / IllegalArgumentException / NoSuchProviderException),
 *          generatePublic/generatePrivate/getKeySpec/translateKey round-trips with
 *          X.509 and PKCS#8 encodings, wrong/null KeySpec -> InvalidKeySpecException,
 *          null key -> InvalidKeySpecException/InvalidKeyException, and
 *          getAlgorithm/getProvider.
 * @run main KeyFactoryTest
 */

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class KeyFactoryTest {
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
                () -> KeyFactory.getInstance("NoSuchKF_XYZ"), "getInstance(bogus)");
        checkThrows(NullPointerException.class,
                () -> KeyFactory.getInstance(null), "getInstance(null)");
        checkThrows(NullPointerException.class,
                () -> KeyFactory.getInstance(null, Security.getProvider("SunRsaSign")),
                "getInstance(null, Provider)");
        checkThrows(IllegalArgumentException.class,
                () -> KeyFactory.getInstance("RSA", (Provider) null),
                "getInstance(alg, null Provider)");
        checkThrows(IllegalArgumentException.class,
                () -> KeyFactory.getInstance("RSA", (String) null),
                "getInstance(alg, null String)");
        checkThrows(NoSuchProviderException.class,
                () -> KeyFactory.getInstance("RSA", "NoSuchProvider_XYZ"),
                "getInstance(alg, bogus provider)");

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(1024);
        KeyPair kp = kpg.generateKeyPair();

        KeyFactory kf = KeyFactory.getInstance("RSA");
        check("RSA".equals(kf.getAlgorithm()), "getAlgorithm");
        check(kf.getProvider() != null, "getProvider non-null");

        // ---- getKeySpec round-trips ----
        X509EncodedKeySpec pubSpec = kf.getKeySpec(kp.getPublic(), X509EncodedKeySpec.class);
        check(pubSpec != null && pubSpec.getEncoded().length > 100, "getKeySpec public X509 non-trivial");
        PKCS8EncodedKeySpec privSpec = kf.getKeySpec(kp.getPrivate(), PKCS8EncodedKeySpec.class);
        check(privSpec != null && privSpec.getEncoded().length > 100, "getKeySpec private PKCS8 non-trivial");

        // ---- generatePublic/generatePrivate round-trips ----
        check(kp.getPublic().equals(kf.generatePublic(pubSpec)), "generatePublic(X509) equals");
        check(kp.getPrivate().equals(kf.generatePrivate(privSpec)), "generatePrivate(PKCS8) equals");

        // ---- regenerate from freshly-encoded spec ----
        check(kp.getPublic().equals(kf.generatePublic(
                new X509EncodedKeySpec(kp.getPublic().getEncoded()))), "generatePublic(getEncoded) equals");
        check(kp.getPrivate().equals(kf.generatePrivate(
                new PKCS8EncodedKeySpec(kp.getPrivate().getEncoded()))), "generatePrivate(getEncoded) equals");

        // ---- translateKey ----
        Key translated = kf.translateKey(kp.getPublic());
        check(translated != null && "RSA".equals(translated.getAlgorithm()), "translateKey non-null RSA");
        check(kp.getPublic().equals(translated), "translateKey equals input");

        // ---- wrong spec class -> InvalidKeySpecException ----
        final KeyFactory kfWrong = KeyFactory.getInstance("RSA");
        checkThrows(InvalidKeySpecException.class,
                () -> kfWrong.getKeySpec(kp.getPublic(), PKCS8EncodedKeySpec.class),
                "getKeySpec(public, PKCS8 spec) wrong class");

        // ---- null spec class -> NPE ----
        @SuppressWarnings("unchecked")
        Class<X509EncodedKeySpec> nullSpecClass = (Class<X509EncodedKeySpec>) (Class<?>) null;
        checkThrows(NullPointerException.class,
                () -> kfWrong.getKeySpec(kp.getPublic(), nullSpecClass),
                "getKeySpec(public, null class)");

        // ---- null key -> InvalidKeySpecException ----
        checkThrows(InvalidKeySpecException.class,
                () -> kfWrong.getKeySpec((Key) null, X509EncodedKeySpec.class),
                "getKeySpec(null key)");

        // ---- generatePublic null/garbage -> InvalidKeySpecException ----
        final KeyFactory kfGen = KeyFactory.getInstance("RSA");
        checkThrows(InvalidKeySpecException.class,
                () -> kfGen.generatePublic((KeySpec) null), "generatePublic(null)");
        checkThrows(InvalidKeySpecException.class,
                () -> kfGen.generatePublic(new PKCS8EncodedKeySpec(new byte[]{1, 2, 3})),
                "generatePublic(wrong spec)");
        checkThrows(InvalidKeySpecException.class,
                () -> kfGen.generatePrivate((KeySpec) null), "generatePrivate(null)");
        checkThrows(InvalidKeySpecException.class,
                () -> kfGen.generatePrivate(new X509EncodedKeySpec(new byte[]{1, 2, 3})),
                "generatePrivate(wrong spec)");

        // ---- translateKey(null) -> InvalidKeyException ----
        checkThrows(InvalidKeyException.class,
                () -> kfGen.translateKey(null), "translateKey(null)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
