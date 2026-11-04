/*
 * @test
 * @summary java.security.AlgorithmParameterGenerator conformance: full contract coverage.
 *          getInstance (algorithm/provider/null/empty/unknown), getAlgorithm/getProvider,
 *          init(int)/init(AlgorithmParameterSpec)/init(int,SecureRandom),
 *          generateParameters returning parseable AlgorithmParameters, and exact exceptions.
 * @run main AlgorithmParameterGeneratorTest
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

        System.out.println("PASS (" + passed + " assertions)");
    }
}
