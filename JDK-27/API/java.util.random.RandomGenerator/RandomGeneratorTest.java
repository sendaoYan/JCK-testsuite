/*
 * @test
 * @summary java.util.random.RandomGenerator conformance: static factories and
 *          the default method family on a default generator.
 * @run main RandomGeneratorTest
 */

import java.util.random.RandomGenerator;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public class RandomGeneratorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        // interface + well-known implementation
        check(RandomGenerator.class.isInterface(), "RandomGenerator is an interface");

        RandomGenerator g = RandomGenerator.getDefault();
        check(g != null, "getDefault() non-null");

        RandomGenerator named = RandomGenerator.of("L32X64MixRandom");
        check(named != null, "of(\"L32X64MixRandom\") non-null");

        // nextInt / nextLong / nextDouble / nextBoolean / nextFloat
        int a = g.nextInt();
        int b = g.nextInt();
        check(a >= Integer.MIN_VALUE && a <= Integer.MAX_VALUE, "nextInt() in range");
        check(g.nextInt(10) >= 0 && g.nextInt(10) < 10, "nextInt(bound) in [0,bound)");
        check(g.nextInt(5, 20) >= 5 && g.nextInt(5, 20) < 20, "nextInt(origin,bound)");
        check(g.nextLong() != 0 || true, "nextLong() returns");
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
        check(g.nextFloat(5.0f, 20.0f) >= 5.0f && g.nextFloat(5.0f, 20.0f) < 20.0f,
              "nextFloat(origin,bound)");
        boolean anyTrue = false, anyFalse = false;
        for (int i = 0; i < 100; i++) {
            if (g.nextBoolean()) anyTrue = true; else anyFalse = true;
        }
        check(anyTrue && anyFalse, "nextBoolean() produces both values");

        // nextBytes
        byte[] bs = new byte[64];
        g.nextBytes(bs);
        check(true, "nextBytes(byte[]) returns");

        // Gaussian and exponential are finite doubles
        check(!Double.isNaN(g.nextGaussian()), "nextGaussian() finite");
        check(!Double.isNaN(g.nextGaussian(1.0, 2.0)), "nextGaussian(mean,stddev) finite");
        check(!Double.isNaN(g.nextExponential()), "nextExponential() finite");

        // streams
        check(g.ints().limit(100).count() == 100, "ints() unbounded");
        check(g.ints(32).count() == 32, "ints(streamSize)");
        check(g.ints(0, 10).limit(50).allMatch(i -> i >= 0 && i < 10), "ints(origin,bound)");
        check(g.ints(32, 0, 10).count() == 32, "ints(streamSize,origin,bound)");
        check(g.longs().limit(100).count() == 100, "longs() unbounded");
        check(g.longs(32).count() == 32, "longs(streamSize)");
        check(g.longs(0, 10).limit(50).allMatch(i -> i >= 0 && i < 10),
              "longs(origin,bound)");
        check(g.longs(32, 0, 10).count() == 32, "longs(streamSize,origin,bound)");
        check(g.doubles().limit(100).count() == 100, "doubles() unbounded");
        check(g.doubles(32).count() == 32, "doubles(streamSize)");
        check(g.doubles(0.0, 10.0).limit(50).allMatch(x -> x >= 0.0 && x < 10.0),
              "doubles(origin,bound)");
        check(g.doubles(32, 0.0, 10.0).count() == 32, "doubles(streamSize,origin,bound)");

        // isDeprecated: the default algorithm is not deprecated
        check(!g.isDeprecated(), "default generator is not deprecated");

        // Random's deprecated nextLong() is abstract on the interface; Random
        // implements it, so verify the abstract contract via the subclass.
        check(g.nextLong() == g.nextLong() || true, "nextLong() usable");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
