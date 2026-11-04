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
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
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

        // ==== contract deepening: DSA factory, spec-class round-trips, cross-checks ====

        // ---- DSA KeyFactory: full encode/spec/rebuild cycle at 1024 bits ----
        java.security.KeyPairGenerator dsaGen = java.security.KeyPairGenerator.getInstance("DSA");
        dsaGen.initialize(1024);
        java.security.KeyPair dkp = dsaGen.generateKeyPair();
        KeyFactory dkf = KeyFactory.getInstance("DSA");
        check("DSA".equals(dkf.getAlgorithm()), "DSA KeyFactory getAlgorithm");
        check(dkf.getProvider() != null, "DSA KeyFactory provider non-null");
        java.security.spec.DSAPublicKeySpec dpub = dkf.getKeySpec(dkp.getPublic(), java.security.spec.DSAPublicKeySpec.class);
        java.security.spec.DSAPrivateKeySpec dpriv = dkf.getKeySpec(dkp.getPrivate(), java.security.spec.DSAPrivateKeySpec.class);
        check(dpub.getP().bitLength() == 1024 && dpub.getQ().bitLength() == 160,
                "DSA public spec p/q sizes");
        check(dpriv.getX().signum() > 0, "DSA private spec x positive");
        java.security.PublicKey rebuiltPub = dkf.generatePublic(dpub);
        java.security.PrivateKey rebuiltPriv = dkf.generatePrivate(dpriv);
        check(dkp.getPublic().equals(rebuiltPub), "DSA generatePublic(spec) equals");
        check(dkp.getPrivate().equals(rebuiltPriv), "DSA generatePrivate(spec) equals");
        // encoding round-trip across instances (format names precise)
        java.security.PublicKey encPub = dkf.generatePublic(
                new X509EncodedKeySpec(dkp.getPublic().getEncoded()));
        java.security.PrivateKey encPriv = dkf.generatePrivate(
                new PKCS8EncodedKeySpec(dkp.getPrivate().getEncoded()));
        check(dkp.getPublic().equals(encPub) && "X.509".equals(encPub.getFormat()),
                "DSA encode/rebuild public");
        check(dkp.getPrivate().equals(encPriv) && "PKCS#8".equals(encPriv.getFormat()),
                "DSA encode/rebuild private");
        // cross algorithm: DSA key into RSA factory -> InvalidKeySpecException / InvalidKeyException
        checkThrows(java.security.spec.InvalidKeySpecException.class,
                () -> kf.getKeySpec(dkp.getPublic(), RSAPublicKeySpec.class),
                "RSA factory getKeySpec(DSA public)");
        checkThrows(java.security.InvalidKeyException.class,
                () -> kf.translateKey(dkp.getPublic()),
                "RSA factory translateKey(DSA public)");

        // ---- RSA spec-class round-trips (modulus/exponent values survive) ----
        RSAPublicKeySpec rpub = kf.getKeySpec(kp.getPublic(), RSAPublicKeySpec.class);
        RSAPrivateKeySpec rpriv = kf.getKeySpec(kp.getPrivate(), RSAPrivateKeySpec.class);
        check(rpub.getModulus().bitLength() == 1024, "RSA public modulus 1024 bits");
        check(rpub.getPublicExponent().intValue() == 65537, "RSA exponent 65537");
        check(rpriv.getPrivateExponent().signum() > 0, "RSA private exponent positive");
        check(kp.getPublic().equals(kf.generatePublic(rpub)), "RSA generatePublic(RSAPublicKeySpec) equals");
        // minimal (n,d) spec rebuilds a working but non-CRT key: signatures still verify
        java.security.PrivateKey minPriv = kf.generatePrivate(
                new RSAPrivateKeySpec(rpriv.getModulus(), rpriv.getPrivateExponent()));
        java.security.Signature sg = java.security.Signature.getInstance("SHA256withRSA");
        sg.initSign(minPriv);
        sg.update("x".getBytes());
        byte[] s = sg.sign();
        java.security.Signature vf = java.security.Signature.getInstance("SHA256withRSA");
        vf.initVerify(kp.getPublic());
        vf.update("x".getBytes());
        check(vf.verify(s), "non-CRT private key signs verifiably");
        check(!minPriv.equals(kp.getPrivate()), "non-CRT key differs from original CRT key");

        // ---- getInstance with explicit provider forms ----
        KeyFactory byName = KeyFactory.getInstance("RSA", "SunRsaSign");
        check("SunRsaSign".equals(byName.getProvider().getName()), "getInstance(alg, providerName)");
        KeyFactory byProv = KeyFactory.getInstance("RSA", Security.getProvider("SunRsaSign"));
        check(byProv.getProvider().getName().equals("SunRsaSign"), "getInstance(alg, Provider)");
        checkThrows(NoSuchAlgorithmException.class, () -> KeyFactory.getInstance("BogusKF_XYZ", "SunRsaSign"),
                "getInstance(bogus, providerName)");
        checkThrows(NullPointerException.class, () -> KeyFactory.getInstance(null, "SunRsaSign"),
                "getInstance(null, providerName)");

        // ---- keys from distinct factories are value-equal (inter-instance equals) ----
        KeyFactory kf2 = KeyFactory.getInstance("RSA");
        check(kf.generatePublic(pubSpec).equals(kf2.generatePublic(pubSpec)),
                "cross-instance generatePublic equals");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
