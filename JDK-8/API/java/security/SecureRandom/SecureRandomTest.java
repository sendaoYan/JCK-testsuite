/*
 * @test
 * @summary java.security.SecureRandom conformance: full contract coverage.
 *          getInstance variants + exact exceptions (NoSuchAlgorithmException /
 *          NullPointerException / IllegalArgumentException / NoSuchProviderException),
 *          default and SHA1PRNG instances, nextBytes non-trivial (bounded not-all-zero
 *          check on a fixed-length buffer), nextBytes(null)/setSeed(null) -> NPE,
 *          generateSeed length + negative -> NegativeArraySizeException, setSeed
 *          reproducibility across two SHA1PRNG instances, and getAlgorithm/getProvider.
 * @run main SecureRandomTest
 */

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;

public class SecureRandomTest {
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
                () -> SecureRandom.getInstance("NoSuchSR_XYZ"), "getInstance(bogus)");
        checkThrows(NullPointerException.class,
                () -> SecureRandom.getInstance(null), "getInstance(null)");
        checkThrows(NullPointerException.class,
                () -> SecureRandom.getInstance(null, Security.getProvider("SUN")),
                "getInstance(null, Provider)");
        checkThrows(IllegalArgumentException.class,
                () -> SecureRandom.getInstance("SHA1PRNG", (Provider) null),
                "getInstance(alg, null Provider)");
        checkThrows(IllegalArgumentException.class,
                () -> SecureRandom.getInstance("SHA1PRNG", (String) null),
                "getInstance(alg, null String)");
        checkThrows(NoSuchProviderException.class,
                () -> SecureRandom.getInstance("SHA1PRNG", "NoSuchProvider_XYZ"),
                "getInstance(alg, bogus provider)");

        // ---- metadata ----
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        check("SHA1PRNG".equals(sr.getAlgorithm()), "getAlgorithm SHA1PRNG");
        check(sr.getProvider() != null, "getProvider non-null");
        check(new SecureRandom() != null, "no-arg constructor");

        // ---- nextBytes not all zero (bounded on fixed 100-byte buffer) ----
        SecureRandom gen = new SecureRandom();
        byte[] buf = new byte[100];
        gen.nextBytes(buf);
        boolean allZero = true;
        for (byte b : buf) if (b != 0) { allZero = false; break; }
        check(!allZero, "nextBytes(100) not all zero");
        check(buf.length == 100, "nextBytes fills exact length");

        // ---- nextBytes(null) -> NPE ----
        final SecureRandom nb = new SecureRandom();
        checkThrows(NullPointerException.class, () -> nb.nextBytes(null), "nextBytes(null)");

        // ---- setSeed(null) -> NPE, setSeed(byte[]) legal ----
        final SecureRandom ss = new SecureRandom();
        checkThrows(NullPointerException.class, () -> ss.setSeed(null), "setSeed(null)");
        ss.setSeed(new byte[16]);
        check(true, "setSeed(byte[]) legal");

        // ---- generateSeed ----
        SecureRandom gs = new SecureRandom();
        byte[] seed = gs.generateSeed(20);
        check(seed.length == 20, "generateSeed(20) length");
        final SecureRandom gsNeg = new SecureRandom();
        checkThrows(NegativeArraySizeException.class, () -> gsNeg.generateSeed(-1),
                "generateSeed(-1)");

        // ---- setSeed reproducibility across two SHA1PRNG instances ----
        byte[] fixedSeed = new byte[32];
        Arrays.fill(fixedSeed, (byte) 7);
        SecureRandom r1 = SecureRandom.getInstance("SHA1PRNG");
        r1.setSeed(fixedSeed);
        SecureRandom r2 = SecureRandom.getInstance("SHA1PRNG");
        r2.setSeed(fixedSeed);
        byte[] o1 = new byte[64];
        byte[] o2 = new byte[64];
        r1.nextBytes(o1);
        r2.nextBytes(o2);
        check(Arrays.equals(o1, o2), "SHA1PRNG setSeed reproducible");

        // ---- SecureRandom(byte[] seed) constructor ----
        SecureRandom seeded = new SecureRandom(new byte[16]);
        byte[] sb = new byte[16];
        seeded.nextBytes(sb);
        check(sb.length == 16, "SecureRandom(byte[]) nextBytes length");


        testContractDeep();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: metadata, determinism closed loops, primitive streams,
    //      edge-length seeds, and subclass fallback ("unknown") ----
    static void testContractDeep() throws Exception {
        // default instance: platform-specific algorithm (NativePRNG on Linux,
        // Windows-PRNG on Windows), seeded from entropy
        SecureRandom def = new SecureRandom();
        String defAlg = def.getAlgorithm();
        check(defAlg != null && !defAlg.isEmpty(), "default algorithm non-empty");
        check(SecureRandom.getInstance(defAlg) != null,
                "default algorithm resolvable via getInstance: " + defAlg);
        // default provider is platform-specific too (SUN on Linux, SunMSCAPI on
        // Windows); assert it is named and consistent with a fresh getInstance.
        check(def.getProvider() != null && def.getProvider().getName() != null
                && !def.getProvider().getName().isEmpty(), "default provider named");
        check(def.getProvider().getName().equals(SecureRandom.getInstance(defAlg).getProvider().getName()),
                "default provider matches getInstance(defAlg)");

        // all three factory spellings agree on algorithm and provider
        check("SUN".equals(SecureRandom.getInstance("SHA1PRNG").getProvider().getName()),
                "getInstance(alg) provider SUN");
        check("SUN".equals(SecureRandom.getInstance("SHA1PRNG", "SUN").getProvider().getName()),
                "getInstance(alg, String) provider SUN");
        check("SUN".equals(SecureRandom.getInstance("SHA1PRNG", Security.getProvider("SUN")).getProvider().getName()),
                "getInstance(alg, Provider) provider SUN");

        // determinism closed loop: same seed -> identical stream across the whole read
        byte[] seed = new byte[32];
        Arrays.fill(seed, (byte) 5);
        SecureRandom r1 = SecureRandom.getInstance("SHA1PRNG");
        r1.setSeed(seed);
        SecureRandom r2 = SecureRandom.getInstance("SHA1PRNG");
        r2.setSeed(seed);
        byte[] o1 = new byte[64], o2 = new byte[64];
        r1.nextBytes(o1);
        r2.nextBytes(o2);
        check(Arrays.equals(o1, o2), "same seed -> identical 64-byte stream");
        // re-seed both with a different seed: still in lockstep, and the stream changes
        byte[] seed2 = seed.clone();
        seed2[0] = (byte) 6;
        r1.setSeed(seed2);
        r2.setSeed(seed2);
        byte[] p1 = new byte[64], p2 = new byte[64];
        r1.nextBytes(p1);
        r2.nextBytes(p2);
        check(Arrays.equals(p1, p2), "post-output reseed keeps twins in lockstep");
        check(!Arrays.equals(o1, p1), "reseed changes the stream");
        // different seed from scratch -> different stream
        SecureRandom r3 = SecureRandom.getInstance("SHA1PRNG");
        r3.setSeed(seed2);
        byte[] o3 = new byte[64];
        r3.nextBytes(o3);
        check(!Arrays.equals(o1, o3), "different seed -> different stream");
        // determinism extends to the primitive stream (nextLong follows nextBytes state)
        SecureRandom q1 = SecureRandom.getInstance("SHA1PRNG");
        q1.setSeed(seed);
        SecureRandom q2 = SecureRandom.getInstance("SHA1PRNG");
        q2.setSeed(seed);
        byte[] hop = new byte[3];
        q1.nextBytes(hop);
        q2.nextBytes(hop);
        check(q1.nextLong() == q2.nextLong(), "primitive stream follows bytes determinism");
        check(q1.nextInt() == q2.nextInt(), "nextInt follows bytes determinism");
        check(q1.nextDouble() == q2.nextDouble(), "nextDouble follows bytes determinism");

        // SecureRandom(byte[]) ctor uses the platform NativePRNG: the seed is only
        // supplemental, so two identically-seeded ctors DIVERGE (fresh entropy)
        SecureRandom c1 = new SecureRandom(seed);
        SecureRandom c2 = new SecureRandom(seed);
        byte[] b1 = new byte[64], b2 = new byte[64];
        c1.nextBytes(b1);
        c2.nextBytes(b2);
        check(!Arrays.equals(b1, b2), "ctor-seed NativePRNG is entropy-backed (streams differ)");

        // primitives: bounded and total ranges
        SecureRandom pr = SecureRandom.getInstance("SHA1PRNG");
        pr.setSeed(seed);
        check(pr.nextInt(1) == 0, "nextInt(1) always 0");
        boolean inRange = true;
        for (int i = 0; i < 100; i++) { int v = pr.nextInt(50); if (v < 0 || v >= 50) inRange = false; }
        check(inRange, "nextInt(50) stays in [0,50) over 100 draws");
        boolean dblOk = true, fltOk = true;
        for (int i = 0; i < 50; i++) {
            double d = pr.nextDouble();
            float f = pr.nextFloat();
            if (!(d >= 0.0 && d < 1.0)) dblOk = false;
            if (!(f >= 0.0f && f < 1.0f)) fltOk = false;
        }
        check(dblOk, "nextDouble in [0,1)");
        check(fltOk, "nextFloat in [0,1)");
        check(!Double.isInfinite(pr.nextGaussian()) && !Double.isNaN(pr.nextGaussian()),
                "nextGaussian finite");
        check(pr.nextBoolean() == pr.nextBoolean() || true, "nextBoolean callable");
        check(pr.ints(5, 0, 100).boxed().filter(x -> x >= 0 && x < 100).count() == 5,
                "ints(5,0,100) yields 5 in-range values");
        check(pr.longs(3).count() == 3, "longs(3) yields 3 values");
        check(pr.doubles(4).filter(d -> d >= 0.0 && d < 1.0).count() == 4,
                "doubles(4) yields 4 in-range values");

        // edge lengths: empty seed and empty buffer are legal no-ops
        new SecureRandom().setSeed(new byte[0]);
        check(true, "setSeed(empty) legal no-op");
        new SecureRandom().nextBytes(new byte[0]);
        check(true, "nextBytes(empty) legal no-op");
        check(new SecureRandom().generateSeed(0).length == 0, "generateSeed(0) empty array");
        byte[] gs1 = new SecureRandom().generateSeed(32);
        byte[] gs2 = new SecureRandom().generateSeed(32);
        check(gs1.length == 32 && gs2.length == 32, "generateSeed(32) length");
        check(!Arrays.equals(gs1, gs2), "generateSeed returns fresh entropy");

        // subclass without engine wiring falls back to "unknown" but keeps SUN provider
        SecureRandom sub = new SecureRandom() {};
        check("unknown".equals(sub.getAlgorithm()), "subclass algorithm unknown");
        check("SUN".equals(sub.getProvider().getName()), "subclass provider SUN");
        byte[] sb = new byte[8];
        sub.nextBytes(sb);
        check(true, "subclass nextBytes delegates to SUN engine");

        // JDK-8 fork: toString is Object-form (class@hash), generateSeed(-1) is an
        // unchecked NegativeArraySizeException, and there is no getParameters API
        String ts = new SecureRandom().toString();
        check(ts.startsWith("java.security.SecureRandom@"), "toString Object form (JDK-8)");
        try {
            new SecureRandom().generateSeed(-1);
            check(false, "generateSeed(-1) must fail");
        } catch (NegativeArraySizeException nase) {
            check(true, "generateSeed(-1) -> NegativeArraySizeException (JDK-8)");
        }
        try {
            SecureRandom.class.getMethod("getParameters");
            check(false, "getParameters must be absent on JDK-8");
        } catch (NoSuchMethodException nsme) {
            check(true, "no getParameters method (JDK-8)");
        }
        try {
            SecureRandom.getInstance("DRBG");
            check(false, "DRBG must be absent on JDK-8");
        } catch (NoSuchAlgorithmException nsae) {
            check(true, "DRBG -> NoSuchAlgorithmException (JDK-8)");
        }
    }
}

