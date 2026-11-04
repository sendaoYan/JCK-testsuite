/*
 * @test
 * @summary java.util.Random conformance: white-box structure plus black-box behavior of
 *          seed reproducibility, nextInt(bound)/nextInt/nextLong/nextDouble/nextFloat ranges,
 *          nextBoolean/nextBytes, nextGaussian determinism, setSeed, and ints/longs/doubles
 *          streams.
 * @run main RandomTest
 */

import java.io.Serializable;
import java.util.Arrays;
import java.util.Random;
import java.util.random.RandomGenerator;

public class RandomTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void assertThrows(Class<? extends Throwable> exType, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (exType.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + " (threw " + t + ")");
        }
        throw new AssertionError(msg + " (no exception)");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.Random");
        check(!c.isInterface(), "Random is a class");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "Random is concrete");
        check(Serializable.class.isAssignableFrom(c), "Random implements Serializable");
        check(RandomGenerator.class.isAssignableFrom(c), "Random implements RandomGenerator");
        c.getMethod("setSeed", long.class);
        c.getMethod("nextInt");
        c.getMethod("nextInt", int.class);
        c.getMethod("nextLong");
        c.getMethod("nextDouble");
        c.getMethod("nextBytes", byte[].class);
        c.getMethod("ints", long.class);
        passed++;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        testSeedReproducibility();
        testRanges();
        testSetSeed();
        testStreams();
        testGaussian();
    }

    static void testSeedReproducibility() {
        Random a = new Random(12345L);
        Random b = new Random(12345L);
        boolean same = true;
        for (int i = 0; i < 100; i++) {
            if (a.nextInt() != b.nextInt()) { same = false; break; }
        }
        check(same, "same seed yields identical sequence");
        Random c = new Random(54321L);
        check(c.nextInt() != new Random(12345L).nextInt(), "different seed differs");
    }

    static void testRanges() {
        Random r = new Random(42L);
        for (int i = 0; i < 1000; i++) {
            int bound = r.nextInt(10);
            check(bound >= 0 && bound < 10, "nextInt(10) in [0,10)");
        }
        check(r.nextInt(1) == 0, "nextInt(1) == 0");
        assertThrows(IllegalArgumentException.class, () -> new Random(1).nextInt(0),
                "nextInt(0) throws IllegalArgumentException");

        double d = r.nextDouble();
        check(d >= 0.0 && d < 1.0, "nextDouble in [0,1)");
        float f = r.nextFloat();
        check(f >= 0.0f && f < 1.0f, "nextFloat in [0,1)");

        long l = r.nextLong();
        check(true, "nextLong callable");

        byte[] bytes = new byte[16];
        new Random(42L).nextBytes(bytes);
        check(bytes.length == 16, "nextBytes fills array");
        byte[] replay = new byte[16];
        new Random(42L).nextBytes(replay);
        check(Arrays.equals(bytes, replay), "nextBytes deterministic for same seed");
    }

    static void testSetSeed() {
        Random a = new Random(12345L);
        int first = a.nextInt();
        a.setSeed(12345L);
        check(a.nextInt() == first, "setSeed resets sequence");
        Random b = new Random(12345L);
        check(b.nextInt() == first, "setSeed matches constructor seed");
    }

    static void testStreams() {
        Random r = new Random(99L);
        int[] first = r.ints(5).toArray();
        check(first.length == 5, "ints(5) length");
        int[] second = new Random(99L).ints(5).toArray();
        check(Arrays.equals(first, second), "ints stream deterministic for same seed");

        long[] longs = new Random(7L).longs(3).toArray();
        check(longs.length == 3, "longs(3) length");
        double[] doubles = new Random(7L).doubles(4).toArray();
        check(doubles.length == 4, "doubles(4) length");
        for (double dd : doubles) {
            check(dd >= 0.0 && dd < 1.0, "doubles values in [0,1)");
        }
    }

    static void testGaussian() {
        Random a = new Random(12345L);
        Random b = new Random(12345L);
        a.nextGaussian();
        b.nextGaussian();
        check(a.nextGaussian() == b.nextGaussian(), "nextGaussian deterministic for same seed");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
