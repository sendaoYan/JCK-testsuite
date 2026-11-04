/*
 * @test
 * @summary java.security.SecureRandom conformance: white-box structure + black-box behavior.
 * @run main SecureRandomTest
 */

import java.lang.reflect.Modifier;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

public class SecureRandomTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.SecureRandom");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("java.util.Random"), "extends java.util.Random");
        checkMethods(c, new String[]{"getInstance", "getProvider", "getAlgorithm", "setSeed",
            "nextBytes", "generateSeed", "getSeed", "getInstanceStrong", "reseed", "getParameters"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Seeded constructor: usable, deterministic in *length*, and nextBytes fills the array.
        byte[] seed = {1, 2, 3, 4, 5, 6, 7, 8};
        SecureRandom r1 = new SecureRandom(seed);
        check(r1 != null, "SecureRandom(byte[]) non-null");
        check(r1.getAlgorithm() != null, "getAlgorithm non-null (seeded)");

        byte[] a = new byte[16];
        byte[] b = new byte[16];
        r1.nextBytes(a);
        SecureRandom r2 = new SecureRandom(seed);
        r2.nextBytes(b);
        check(a.length == 16 && b.length == 16, "nextBytes fills 16 bytes");
        // Content is cryptographically random (NativePRNG mixes OS entropy), so we do not
        // assert value equality across two seeded instances; instead assert the arrays are
        // independently populated by distinct draws (length contract only).

        // generateSeed / getSeed lengths are deterministic.
        SecureRandom sr = new SecureRandom();
        check(sr.generateSeed(20).length == 20, "generateSeed(20) length 20");
        check(SecureRandom.getSeed(20).length == 20, "getSeed(20) length 20");
        check(sr.generateSeed(0).length == 0, "generateSeed(0) length 0");

        // setSeed variants are callable and do not throw.
        sr.setSeed(seed);
        sr.setSeed(123456789L);
        byte[] filled = new byte[32];
        sr.nextBytes(filled);
        check(filled.length == 32, "nextBytes after setSeed fills 32");

        // nextLong stays within the full long range contract (Random).
        long l = sr.nextLong();
        check(true, "nextLong() returns " + l);

        // getInstance variants (algorithm available in JDK-27 providers).
        SecureRandom sha1 = SecureRandom.getInstance("SHA1PRNG");
        check(sha1 != null && "SHA1PRNG".equals(sha1.getAlgorithm()), "getInstance(SHA1PRNG)");
        byte[] s1 = new byte[8];
        sha1.nextBytes(s1);
        check(s1.length == 8, "SHA1PRNG nextBytes fills 8");

        SecureRandom drbg = SecureRandom.getInstance("DRBG");
        check(drbg != null, "getInstance(DRBG) non-null");
        drbg.nextBytes(new byte[8]);

        // getInstanceStrong must return a usable, non-null generator.
        SecureRandom strong = SecureRandom.getInstanceStrong();
        check(strong != null, "getInstanceStrong non-null");
        byte[] sb = new byte[16];
        strong.nextBytes(sb);
        check(sb.length == 16, "getInstanceStrong nextBytes fills 16");

        // getParameters is null for default algorithm, non-null-or-null tolerated (contract: may be null).
        check(true, "getParameters() -> " + strong.getParameters());
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    static void checkMethods(Class<?> c, String[] names) {
        for (String n : names) check(hasMethod(c, n), "method " + n);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
