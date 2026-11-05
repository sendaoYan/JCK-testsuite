/*
 * @test
 * @summary java.security.AlgorithmParameterGenerator conformance: full contract coverage.
 *          getInstance (algorithm/provider/null/empty/unknown), getAlgorithm/getProvider,
 *          init(int)/init(AlgorithmParameterSpec)/init(int,SecureRandom),
 *          generateParameters returning parseable AlgorithmParameters, and exact exceptions.
 * @run main/timeout=600 AlgorithmParameterGeneratorTest
 */

import java.security.AlgorithmParameterGenerator;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.DSAParameterSpec;

import javax.crypto.spec.DHGenParameterSpec;

public class AlgorithmParameterGeneratorTest {
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
        // ---- getInstance(String) for the two standard algorithms ----
        AlgorithmParameterGenerator dh = AlgorithmParameterGenerator.getInstance("DiffieHellman");
        check("DiffieHellman".equals(dh.getAlgorithm()), "DH getAlgorithm");
        check(dh.getProvider() != null, "DH getProvider non-null");

        AlgorithmParameterGenerator dsa = AlgorithmParameterGenerator.getInstance("DSA");
        check("DSA".equals(dsa.getAlgorithm()), "DSA getAlgorithm");
        check(dsa.getProvider() != null, "DSA getProvider non-null");

        // ---- getInstance exception matrix ----
        checkThrows(NullPointerException.class,
                () -> AlgorithmParameterGenerator.getInstance((String) null),
                "getInstance(null algorithm)");
        checkThrows(NoSuchAlgorithmException.class,
                () -> AlgorithmParameterGenerator.getInstance(""),
                "getInstance(empty algorithm)");
        checkThrows(NoSuchAlgorithmException.class,
                () -> AlgorithmParameterGenerator.getInstance("NoSuchAlgo"),
                "getInstance(unknown algorithm)");
        checkThrows(IllegalArgumentException.class,
                () -> AlgorithmParameterGenerator.getInstance("DSA", (Provider) null),
                "getInstance(algorithm, null provider)");
        checkThrows(NullPointerException.class,
                () -> AlgorithmParameterGenerator.getInstance(null, "SUN"),
                "getInstance(null algorithm, provider)");

        // ---- init(int) + generateParameters (DiffieHellman) ----
        dh.init(512);
        AlgorithmParameters p = dh.generateParameters();
        check(p != null, "DH generateParameters non-null");
        check("DH".equals(p.getAlgorithm()), "DH generated params algorithm");

        // ---- init(AlgorithmParameterSpec) (DiffieHellman) ----
        AlgorithmParameterGenerator dh2 = AlgorithmParameterGenerator.getInstance("DiffieHellman");
        dh2.init(new DHGenParameterSpec(512, 160));
        AlgorithmParameters p2 = dh2.generateParameters();
        check(p2 != null, "DH spec init generateParameters non-null");
        check("DH".equals(p2.getAlgorithm()), "DH spec init params algorithm");

        // ---- init(int, SecureRandom) (DSA) + parseable spec ----
        AlgorithmParameterGenerator dsa2 = AlgorithmParameterGenerator.getInstance("DSA");
        dsa2.init(1024, new SecureRandom());
        AlgorithmParameters p3 = dsa2.generateParameters();
        check(p3 != null, "DSA generateParameters non-null");
        check("DSA".equals(p3.getAlgorithm()), "DSA generated params algorithm");
        DSAParameterSpec spec = p3.getParameterSpec(DSAParameterSpec.class);
        check(spec.getP() != null && spec.getQ() != null && spec.getG() != null,
                "DSA p/q/g non-null");
        check(spec.getP().bitLength() == 1024, "DSA p bitLength 1024");

        // ---- init(null spec) -> InvalidAlgorithmParameterException ----
        AlgorithmParameterGenerator dsa3 = AlgorithmParameterGenerator.getInstance("DSA");
        checkThrows(InvalidAlgorithmParameterException.class,
                () -> dsa3.init((AlgorithmParameterSpec) null),
                "init(null AlgorithmParameterSpec)");

        // ---- init(negative) -> InvalidParameterException ----
        AlgorithmParameterGenerator dh3 = AlgorithmParameterGenerator.getInstance("DiffieHellman");
        checkThrows(InvalidParameterException.class,
                () -> dh3.init(-1),
                "init(negative size)");

        // ==== contract deepening: engine reuse, DSA spec round-trip, DSA generation end-to-end ====

        // ---- generated DSA params are directly usable by KeyPairGenerator (real closed loop) ----
        java.security.KeyPairGenerator useGen = java.security.KeyPairGenerator.getInstance("DSA");
        useGen.initialize(spec);
        java.security.KeyPair kp = useGen.generateKeyPair();
        java.security.Signature sgn = java.security.Signature.getInstance("SHA256withDSA");
        sgn.initSign(kp.getPrivate());
        sgn.update("apg".getBytes());
        byte[] dsig = sgn.sign();
        java.security.Signature vfy = java.security.Signature.getInstance("SHA256withDSA");
        vfy.initVerify(kp.getPublic());
        vfy.update("apg".getBytes());
        check(vfy.verify(dsig), "generated DSA params support full signature loop");
        // q/g bit sizes for 1024-bit DSA
        check(spec.getQ().bitLength() == 160, "DSA q 160 bits");
        check(spec.getG().signum() > 0 && spec.getG().bitLength() <= 1024, "DSA g range");

        // ---- encoding round-trip of generated params ----
        byte[] der = p3.getEncoded();
        check(der != null && der.length > 100, "DSA params DER non-trivial");
        AlgorithmParameters parsed = AlgorithmParameters.getInstance("DSA");
        parsed.init(der);
        DSAParameterSpec back = parsed.getParameterSpec(DSAParameterSpec.class);
        check(back.getP().equals(spec.getP()) && back.getQ().equals(spec.getQ())
                && back.getG().equals(spec.getG()), "generated params encode/decode value-equal");

        // ---- two generateParameters calls yield independent parameter sets ----
        AlgorithmParameters p4 = dsa2.generateParameters();
        DSAParameterSpec spec2 = p4.getParameterSpec(DSAParameterSpec.class);
        check(!spec2.getP().equals(spec.getP()), "second generateParameters independent");

        // ---- init(size, null SecureRandom) legal ----
        AlgorithmParameterGenerator nr = AlgorithmParameterGenerator.getInstance("DSA");
        nr.init(1024, null);
        check(nr.generateParameters() != null, "init(size, null random) works");

        // ---- engine metadata ----
        AlgorithmParameterGenerator meta = AlgorithmParameterGenerator.getInstance("DSA");
        check("DSA".equals(meta.getAlgorithm()), "getAlgorithm DSA");
        check(meta.getProvider() != null && "SUN".equals(meta.getProvider().getName()),
                "getProvider SUN for DSA");
        check(meta.toString() != null, "toString non-null");

        // ---- getInstance provider forms ----
        check("SUN".equals(AlgorithmParameterGenerator.getInstance("DSA", "SUN").getProvider().getName()),
                "getInstance(alg, providerName)");
        check("SUN".equals(AlgorithmParameterGenerator.getInstance("DSA", java.security.Security.getProvider("SUN"))
                .getProvider().getName()), "getInstance(alg, Provider)");
        checkThrows(java.security.NoSuchProviderException.class,
                () -> AlgorithmParameterGenerator.getInstance("DSA", "NoSuchProvider_XYZ"),
                "getInstance(alg, bogus providerName)");

        // ---- 2048-bit DSA supported on both JDKs ----
        AlgorithmParameterGenerator big = AlgorithmParameterGenerator.getInstance("DSA");
        big.init(2048);
        DSAParameterSpec bigSpec = big.generateParameters().getParameterSpec(DSAParameterSpec.class);
        check(bigSpec.getP().bitLength() == 2048, "DSA 2048 p size");
        check(bigSpec.getQ().bitLength() == 224, "DSA 2048 q size 224 (SUN default)");
        AlgorithmParameterGenerator huge = AlgorithmParameterGenerator.getInstance("DSA");
        huge.init(3072);
        DSAParameterSpec hugeSpec = huge.generateParameters().getParameterSpec(DSAParameterSpec.class);
        check(hugeSpec.getP().bitLength() == 3072, "DSA 3072 p size");
        check(hugeSpec.getQ().bitLength() == 256, "DSA 3072 q size 256");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
