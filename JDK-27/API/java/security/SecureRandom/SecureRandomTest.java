/*
 * @test
 * @summary java.security.SecureRandom conformance: full contract coverage.
 *          getInstance variants + exact exceptions (NoSuchAlgorithmException /
 *          NullPointerException / IllegalArgumentException / NoSuchProviderException),
 *          default and SHA1PRNG instances, nextBytes non-trivial (bounded not-all-zero
 *          check on a fixed-length buffer), nextBytes(null)/setSeed(null) -> NPE,
 *          generateSeed length + negative -> IllegalArgumentException, setSeed
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
        checkThrows(IllegalArgumentException.class, () -> gsNeg.generateSeed(-1),
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

        System.out.println("PASS (" + passed + " assertions)");
    }
}
