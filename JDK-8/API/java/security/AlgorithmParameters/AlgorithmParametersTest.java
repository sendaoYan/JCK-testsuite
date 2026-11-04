/*
 * @test
 * @summary java.security.AlgorithmParameters conformance: full contract coverage.
 *          getInstance variants + exact exceptions (NoSuchAlgorithmException /
 *          NullPointerException / IllegalArgumentException / NoSuchProviderException),
 *          init(AlgorithmParameterSpec) with AES IV, getEncoded()/getEncoded(format),
 *          getParameterSpec(Class) round-trip, wrong spec class -> InvalidParameterSpecException,
 *          null spec class -> NPE, init(null spec) -> InvalidParameterSpecException,
 *          init(bad bytes) -> IOException, and getAlgorithm/getProvider.
 * @run main AlgorithmParametersTest
 */

import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.DSAParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import javax.crypto.spec.IvParameterSpec;

public class AlgorithmParametersTest {
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
                () -> AlgorithmParameters.getInstance("NoSuchAP_XYZ"), "getInstance(bogus)");
        checkThrows(NullPointerException.class,
                () -> AlgorithmParameters.getInstance(null), "getInstance(null)");
        checkThrows(NullPointerException.class,
                () -> AlgorithmParameters.getInstance(null, Security.getProvider("SunJCE")),
                "getInstance(null, Provider)");
        checkThrows(IllegalArgumentException.class,
                () -> AlgorithmParameters.getInstance("AES", (Provider) null),
                "getInstance(alg, null Provider)");
        checkThrows(IllegalArgumentException.class,
                () -> AlgorithmParameters.getInstance("AES", (String) null),
                "getInstance(alg, null String)");
        checkThrows(NoSuchProviderException.class,
                () -> AlgorithmParameters.getInstance("AES", "NoSuchProvider_XYZ"),
                "getInstance(alg, bogus provider)");

        // ---- init + metadata ----
        AlgorithmParameters ap = AlgorithmParameters.getInstance("AES");
        ap.init(new IvParameterSpec(new byte[16]));
        check("AES".equals(ap.getAlgorithm()), "getAlgorithm");
        check(ap.getProvider() != null, "getProvider non-null");

        // ---- getEncoded ----
        byte[] enc = ap.getEncoded();
        check(enc != null && enc.length > 0, "getEncoded non-empty");
        byte[] encFmt = ap.getEncoded("ASN.1");
        check(encFmt != null && encFmt.length == enc.length, "getEncoded(format) matches length");

        // ---- getParameterSpec round-trip ----
        IvParameterSpec iv = ap.getParameterSpec(IvParameterSpec.class);
        check(iv != null && iv.getIV().length == 16, "getParameterSpec(IvParameterSpec)");

        // ---- wrong spec class -> InvalidParameterSpecException ----
        final AlgorithmParameters apWrong = AlgorithmParameters.getInstance("AES");
        apWrong.init(new IvParameterSpec(new byte[16]));
        checkThrows(InvalidParameterSpecException.class,
                () -> apWrong.getParameterSpec(DSAParameterSpec.class),
                "getParameterSpec(wrong class)");

        // ---- null spec class -> NPE ----
        @SuppressWarnings("unchecked")
        Class<IvParameterSpec> nullCls = (Class<IvParameterSpec>) (Class<?>) null;
        checkThrows(NullPointerException.class,
                () -> apWrong.getParameterSpec(nullCls), "getParameterSpec(null)");

        // ---- init(null spec) -> InvalidParameterSpecException ----
        final AlgorithmParameters apNull = AlgorithmParameters.getInstance("AES");
        checkThrows(InvalidParameterSpecException.class,
                () -> apNull.init((AlgorithmParameterSpec) null), "init(null spec)");

        // ---- init(bad bytes) -> IOException ----
        final AlgorithmParameters apBad = AlgorithmParameters.getInstance("AES");
        checkThrows(IOException.class, () -> apBad.init(new byte[]{1, 2, 3}), "init(bad bytes)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
