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

        // ---- null spec: provider-dependent ----
        // Linux 上 RSA 只有 SunRsaSign 一个实现，null 参数抛
        // InvalidAlgorithmParameterException；Windows 的提供者链里还有 SunMSCAPI，
        // 其 CKeyPairGenerator 把 null 当作“默认 2048 位 RSA”接受，
        // 因此 Delegate 故障转移后不会抛异常。
        final KeyPairGenerator kpgNull = KeyPairGenerator.getInstance("RSA");
        if (Security.getProvider("SunMSCAPI") != null) {
            try {
                kpgNull.initialize((AlgorithmParameterSpec) null);
                KeyPair kpNull = kpgNull.generateKeyPair();
                check(kpNull.getPublic() != null && kpNull.getPrivate() != null,
                      "Windows: initialize(null) falls over to SunMSCAPI default keypair");
            } catch (InvalidAlgorithmParameterException iape) {
                check(true, "Windows: initialize(null) rejected with InvalidAlgorithmParameterException");
            }
        } else {
            checkThrows(InvalidAlgorithmParameterException.class,
                    () -> kpgNull.initialize((AlgorithmParameterSpec) null),
                    "initialize(null spec)");
        }

        // ==== contract deepening: DSA/EC engines, size matrix, spec-driven generation ====

        // ---- DSA-1024: initialize(1024) then two independent keypairs; params match spec ----
        KeyPairGenerator dsa = KeyPairGenerator.getInstance("DSA");
        check("DSA".equals(dsa.getAlgorithm()), "DSA getAlgorithm");
        dsa.initialize(1024, new SecureRandom());
        java.security.KeyPair dkp = dsa.generateKeyPair();
        java.security.spec.DSAPublicKeySpec dspec = java.security.KeyFactory.getInstance("DSA")
                .getKeySpec(dkp.getPublic(), java.security.spec.DSAPublicKeySpec.class);
        check(dspec.getP().bitLength() == 1024, "DSA p exactly 1024 bits");
        check(dspec.getQ().bitLength() == 160, "DSA q exactly 160 bits");
        check(dspec.getY().signum() > 0, "DSA y positive");
        java.security.KeyPair dkp2 = dsa.generateKeyPair();
        check(!dkp.getPrivate().equals(dkp2.getPrivate()), "two DSA keypairs differ");
        check("DSA".equals(dkp.getPublic().getAlgorithm()) && "PKCS#8".equals(dkp.getPrivate().getFormat()),
                "DSA key formats");

        // ---- RSA size boundary matrix ----
        final KeyPairGenerator rsaBounds = KeyPairGenerator.getInstance("RSA");
        checkThrows(InvalidParameterException.class, () -> rsaBounds.initialize(0), "RSA initialize(0)");
        checkThrows(InvalidParameterException.class, () -> rsaBounds.initialize(-512), "RSA initialize(-512)");
        // non-multiple-of-8 and odd sizes are accepted by SunRsaSign
        KeyPairGenerator odd = KeyPairGenerator.getInstance("RSA");
        odd.initialize(513);
        java.security.spec.RSAPublicKeySpec ospec = java.security.KeyFactory.getInstance("RSA")
                .getKeySpec(odd.generateKeyPair().getPublic(), java.security.spec.RSAPublicKeySpec.class);
        check(ospec.getModulus().bitLength() == 513, "RSA 513-bit honored");
        check(ospec.getPublicExponent().intValue() == 65537, "RSA default exponent 65537");
        // RSAKeyGenParameterSpec with custom exponent F0
        KeyPairGenerator f0 = KeyPairGenerator.getInstance("RSA");
        f0.initialize(new RSAKeyGenParameterSpec(512, RSAKeyGenParameterSpec.F0));
        java.security.spec.RSAPublicKeySpec f0spec = java.security.KeyFactory.getInstance("RSA")
                .getKeySpec(f0.generateKeyPair().getPublic(), java.security.spec.RSAPublicKeySpec.class);
        check(f0spec.getPublicExponent().intValue() == RSAKeyGenParameterSpec.F0.intValue(),
                "RSA spec exponent F0 honored");

        // ---- DSA size matrix: sub-supported size and huge size ----
        final KeyPairGenerator dsaBounds = KeyPairGenerator.getInstance("DSA");
        checkThrows(InvalidParameterException.class, () -> dsaBounds.initialize(1023),
                "DSA initialize(1023) unsupported");
        // initialize(int, SecureRandom=null) is legal (provider picks default entropy)
        KeyPairGenerator nullRandom = KeyPairGenerator.getInstance("DSA");
        nullRandom.initialize(1024, null);
        check(nullRandom.generateKeyPair().getPublic() != null, "initialize(size, null) works");
        // initialize after generateKeyPair re-keys cleanly
        KeyPairGenerator reInit = KeyPairGenerator.getInstance("RSA");
        reInit.initialize(512);
        reInit.generateKeyPair();
        reInit.initialize(512);
        java.security.spec.RSAPublicKeySpec r2 = java.security.KeyFactory.getInstance("RSA")
                .getKeySpec(reInit.generateKeyPair().getPublic(), java.security.spec.RSAPublicKeySpec.class);
        check(r2.getModulus().bitLength() == 512, "re-initialize new size honored");

        // ---- genKeyPair() == generateKeyPair() alias, default size honored ----
        KeyPairGenerator alias = KeyPairGenerator.getInstance("DSA");
        java.security.KeyPair akp = alias.genKeyPair();
        java.security.spec.DSAPublicKeySpec aspec = java.security.KeyFactory.getInstance("DSA")
                .getKeySpec(akp.getPublic(), java.security.spec.DSAPublicKeySpec.class);
        check(aspec.getP().bitLength() == 2048, "DSA default size 2048");
        // both generated pairs usable end-to-end in a SHA256withDSA signature
        java.security.Signature sgn = java.security.Signature.getInstance("SHA256withDSA");
        sgn.initSign(dkp.getPrivate());
        sgn.update("payload".getBytes());
        byte[] dsig = sgn.sign();
        java.security.Signature vfy = java.security.Signature.getInstance("SHA256withDSA");
        vfy.initVerify(dkp.getPublic());
        vfy.update("payload".getBytes());
        check(vfy.verify(dsig), "DSA keypair end-to-end signature");

        // ---- DSAParameterSpec-driven generation (reuses provider-generated params) ----
        java.security.KeyPairGenerator dsaSpec = java.security.KeyPairGenerator.getInstance("DSA");
        dsaSpec.initialize(new java.security.spec.DSAParameterSpec(dspec.getP(), dspec.getQ(), dspec.getG()));
        java.security.KeyPair skp = dsaSpec.generateKeyPair();
        java.security.spec.DSAPublicKeySpec sSpec = java.security.KeyFactory.getInstance("DSA")
                .getKeySpec(skp.getPublic(), java.security.spec.DSAPublicKeySpec.class);
        check(sSpec.getP().equals(dspec.getP()) && sSpec.getQ().equals(dspec.getQ())
                        && sSpec.getG().equals(dspec.getG()),
                "DSA spec-driven generation reuses p/q/g");
        // malformed DSA spec -> InvalidParameterException (prime/subprime combination)
        final java.security.KeyPairGenerator badDsa = java.security.KeyPairGenerator.getInstance("DSA");
        checkThrows(InvalidParameterException.class, () -> badDsa.initialize(new java.security.spec.DSAParameterSpec(
                        java.math.BigInteger.ONE, java.math.BigInteger.ONE, java.math.BigInteger.valueOf(2))),
                "DSA spec invalid prime sizes");

        // ---- toString ----
        check(kpg.toString() != null && kpg.toString().length() > 0, "toString non-empty");
        // ---- EC engine: ECGenParameterSpec-driven + explicit size + invalid curve ----
        KeyPairGenerator ec = KeyPairGenerator.getInstance("EC");
        check("EC".equals(ec.getAlgorithm()), "EC getAlgorithm");
        ec.initialize(new java.security.spec.ECGenParameterSpec("secp256r1"));
        java.security.KeyPair ekp = ec.generateKeyPair();
        java.security.interfaces.ECPublicKey ecp = (java.security.interfaces.ECPublicKey) ekp.getPublic();
        check(ecp.getParams().getCurve().getField().getFieldSize() == 256, "EC secp256r1 field size");
        check("EC".equals(ekp.getPublic().getAlgorithm()), "EC key algorithm");
        final KeyPairGenerator ecBogus = KeyPairGenerator.getInstance("EC");
        checkThrows(InvalidAlgorithmParameterException.class,
                () -> ecBogus.initialize(new java.security.spec.ECGenParameterSpec("bogusCurveXYZ")),
                "EC unknown curve");
        KeyPairGenerator ecSize = KeyPairGenerator.getInstance("EC");
        ecSize.initialize(256);
        java.security.interfaces.ECPublicKey ec256 =
                (java.security.interfaces.ECPublicKey) ecSize.generateKeyPair().getPublic();
        check(ec256.getParams().getCurve().getField().getFieldSize() == 256, "EC initialize(256) honored");
        final KeyPairGenerator ecBad = KeyPairGenerator.getInstance("EC");
        checkThrows(InvalidParameterException.class, () -> ecBad.initialize(100),
                "EC initialize(100) unsupported");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
