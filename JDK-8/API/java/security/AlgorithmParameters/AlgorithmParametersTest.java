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

        // ==== contract deepening: uninitialized state, DSA cycle, defensive copies ====

        // ---- uninitialized state machine: every data accessor fails until init ----
        AlgorithmParameters fresh = AlgorithmParameters.getInstance("AES");
        check(fresh.getAlgorithm().equals("AES"), "uninitialized getAlgorithm works");
        checkThrows(InvalidParameterSpecException.class,
                () -> fresh.getParameterSpec(IvParameterSpec.class), "uninitialized getParameterSpec");
        checkThrows(IOException.class, () -> fresh.getEncoded(), "uninitialized getEncoded");
        // uninitialized toString returns null (SPI state not yet initialized) - stable on both JDKs
        check(fresh.toString() == null, "uninitialized toString null");

        // ---- AES IV byte round-trip: init(spec) -> encode -> init(bytes) -> spec equals ----
        byte[] ivBytes = new byte[16];
        for (int i = 0; i < 16; i++) ivBytes[i] = (byte) (i * 7);
        AlgorithmParameters encAp = AlgorithmParameters.getInstance("AES");
        encAp.init(new IvParameterSpec(ivBytes));
        byte[] der = encAp.getEncoded();
        check(der.length == 18, "AES IV ASN.1 encoding is 18 bytes (tag+len+16)");
        AlgorithmParameters decAp = AlgorithmParameters.getInstance("AES");
        decAp.init(der);
        check(java.util.Arrays.equals(
                decAp.getParameterSpec(IvParameterSpec.class).getIV(), ivBytes), "AES IV byte round-trip");
        // getEncoded returns a fresh array each call (defensive copy)
        check(encAp.getEncoded() != encAp.getEncoded(), "getEncoded defensive copy");
        // unknown format name: SunJCE returns the primary ASN.1 encoding (recorded behavior)
        check(java.util.Arrays.equals(encAp.getEncoded("BogusFormat"), der),
                "getEncoded(unknown format) falls back to primary encoding");
        // init is once-only: a second init throws InvalidParameterSpecException "already initialized"
        final AlgorithmParameters twice = encAp;
        checkThrows(InvalidParameterSpecException.class,
                () -> twice.init(new IvParameterSpec(new byte[16])), "second init already initialized");

        // ---- DSA parameters: spec -> encode -> decode -> spec value equality ----
        java.security.KeyPairGenerator dsaGen = java.security.KeyPairGenerator.getInstance("DSA");
        dsaGen.initialize(1024);
        java.security.spec.DSAPublicKeySpec dps = java.security.KeyFactory.getInstance("DSA")
                .getKeySpec(dsaGen.generateKeyPair().getPublic(), java.security.spec.DSAPublicKeySpec.class);
        AlgorithmParameters dsaAp = AlgorithmParameters.getInstance("DSA");
        dsaAp.init(new DSAParameterSpec(dps.getP(), dps.getQ(), dps.getG()));
        byte[] dsaDer = dsaAp.getEncoded();
        check(dsaDer != null && dsaDer.length > 100, "DSA params encoding non-trivial");
        AlgorithmParameters dsaAp2 = AlgorithmParameters.getInstance("DSA");
        dsaAp2.init(dsaDer);
        DSAParameterSpec back = dsaAp2.getParameterSpec(DSAParameterSpec.class);
        check(back.getP().equals(dps.getP()) && back.getQ().equals(dps.getQ())
                && back.getG().equals(dps.getG()), "DSA params p/q/g value round-trip");
        // encoded output is deterministic for same state
        check(java.util.Arrays.equals(dsaAp.getEncoded(), dsaDer), "DSA encoding deterministic");
        check(dsaAp.toString() != null && dsaAp.toString().contains("p")
                || dsaAp.toString().length() > 0, "DSA toString non-empty");
        // truncated/garbled DSA DER -> IOException
        final AlgorithmParameters dsaBad = AlgorithmParameters.getInstance("DSA");
        checkThrows(IOException.class, () -> dsaBad.init(new byte[]{0x30, 0x00}),
                "DSA empty ASN.1 sequence -> IOException");
        checkThrows(IOException.class, () -> dsaBad.init(new byte[]{1, 2, 3}),
                "DSA garbage bytes -> IOException");

        // ---- init(byte[], format): "ASN.1" accepted, primary encoding name "ASN.1" ----
        AlgorithmParameters fmtAp = AlgorithmParameters.getInstance("DSA");
        fmtAp.init(dsaDer, "ASN.1");
        check(fmtAp.getParameterSpec(DSAParameterSpec.class) != null, "init(bytes, ASN.1)");

        // ---- getInstance with real provider forms ----
        check("SUN".equals(AlgorithmParameters.getInstance("DSA", "SUN").getProvider().getName()),
                "getInstance(alg, providerName) DSA");
        check("SUN".equals(AlgorithmParameters.getInstance("DSA", Security.getProvider("SUN")).getProvider().getName()),
                "getInstance(alg, Provider) DSA");
        checkThrows(NoSuchAlgorithmException.class,
                () -> AlgorithmParameters.getInstance("NoSuchAP_XYZ", "SUN"), "getInstance(bogus, provider)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
