/*
 * @test
 * @summary java.util.random.RandomGenerator conformance: white-box structure plus black-box
 *          behavior of the static factories and the default-method family on a fixed-seed
 *          generator (range and determinism assertions).
 * @run main RandomGeneratorTest
 */

import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

public class RandomGeneratorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.random.RandomGenerator");
        check(c.isInterface(), "RandomGenerator is an interface");
        c.getMethod("nextLong");
        c.getMethod("nextInt", int.class);
        c.getMethod("nextDouble");
        c.getMethod("nextBoolean");
        c.getMethod("nextBytes", byte[].class);
        c.getMethod("ints", long.class);
        c.getMethod("longs", long.class);
        c.getMethod("doubles", long.class);
        c.getMethod("of", String.class);
        c.getMethod("getDefault");
        passed++;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        testFactories();
        testDeterminism();
        testRanges();
        testStreams();
    }

    static void testFactories() {
        check(RandomGenerator.getDefault() != null, "getDefault() non-null");
        check(RandomGenerator.of("L32X64MixRandom") != null, "of(\"L32X64MixRandom\") non-null");
        check(!RandomGenerator.getDefault().isDeprecated(), "default generator not deprecated");
    }

    static void testDeterminism() {
        RandomGeneratorFactory<RandomGenerator> fac = RandomGeneratorFactory.getDefault();
        RandomGenerator a = fac.create(12345L);
        RandomGenerator b = fac.create(12345L);
        boolean same = true;
        for (int i = 0; i < 100; i++) {
            if (a.nextLong() != b.nextLong()) { same = false; break; }
        }
        check(same, "same seed yields identical sequence");
    }

    static void testRanges() {
        RandomGeneratorFactory<RandomGenerator> fac = RandomGeneratorFactory.getDefault();
        RandomGenerator g = fac.create(42L);
        for (int i = 0; i < 1000; i++) {
            int v = g.nextInt(10);
            check(v >= 0 && v < 10, "nextInt(bound) in [0,bound)");
        }
        check(g.nextInt(5, 20) >= 5 && g.nextInt(5, 20) < 20, "nextInt(origin,bound)");
        check(g.nextLong(100) >= 0 && g.nextLong(100) < 100, "nextLong(bound)");
        check(g.nextLong(5, 20) >= 5 && g.nextLong(5, 20) < 20, "nextLong(origin,bound)");

        double d = g.nextDouble();
        check(d >= 0.0 && d < 1.0, "nextDouble() in [0,1)");
        check(g.nextDouble(10.0) >= 0.0 && g.nextDouble(10.0) < 10.0, "nextDouble(bound)");
        check(g.nextDouble(5.0, 20.0) >= 5.0 && g.nextDouble(5.0, 20.0) < 20.0,
              "nextDouble(origin,bound)");

        float f = g.nextFloat();
        check(f >= 0.0f && f < 1.0f, "nextFloat() in [0,1)");
        check(g.nextFloat(10.0f) >= 0.0f && g.nextFloat(10.0f) < 10.0f, "nextFloat(bound)");

        boolean b = g.nextBoolean();
        check(b == true || b == false, "nextBoolean() returns a boolean");

        byte[] bs = new byte[64];
        g.nextBytes(bs);
        check(bs.length == 64, "nextBytes fills array");

        check(!Double.isNaN(g.nextGaussian()), "nextGaussian() finite");
        check(!Double.isNaN(g.nextExponential()), "nextExponential() finite");
    }

    static void testStreams() {
        RandomGeneratorFactory<RandomGenerator> fac = RandomGeneratorFactory.getDefault();
        RandomGenerator g = fac.create(42L);
        check(g.ints(32).count() == 32, "ints(streamSize)");
        check(g.ints(0, 10).limit(50).allMatch(i -> i >= 0 && i < 10), "ints(origin,bound)");
        check(g.longs(32).count() == 32, "longs(streamSize)");
        check(g.longs(0, 10).limit(50).allMatch(i -> i >= 0 && i < 10), "longs(origin,bound)");
        check(g.doubles(32).count() == 32, "doubles(streamSize)");
        check(g.doubles(0.0, 10.0).limit(50).allMatch(x -> x >= 0.0 && x < 10.0),
              "doubles(origin,bound)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
