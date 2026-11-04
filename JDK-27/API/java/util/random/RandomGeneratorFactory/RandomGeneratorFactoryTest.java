/*
 * @test
 * @summary java.util.random.RandomGeneratorFactory conformance: white-box structure plus
 *          black-box behavior of discovery (all/getDefault/of), metadata accessors, create()
 *          variants, and fixed-seed determinism.
 * @run main RandomGeneratorFactoryTest
 */

import java.math.BigInteger;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

public class RandomGeneratorFactoryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.random.RandomGeneratorFactory");
        check(!c.isInterface(), "RandomGeneratorFactory is a class");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "RandomGeneratorFactory is final");
        c.getMethod("all");
        c.getMethod("getDefault");
        c.getMethod("of", String.class);
        c.getMethod("name");
        c.getMethod("create");
        c.getMethod("create", long.class);
        c.getMethod("create", byte[].class);
        passed++;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        testDiscovery();
        testMetadata();
        testCreateVariants();
        testDeterminism();
    }

    static void testDiscovery() {
        check(RandomGeneratorFactory.all().count() > 0, "all() non-empty");
        check(RandomGeneratorFactory.getDefault() != null, "getDefault() non-null");
        RandomGeneratorFactory<RandomGenerator> l32 = RandomGeneratorFactory.of("L32X64MixRandom");
        check(l32 != null, "of(\"L32X64MixRandom\") non-null");
        check("L32X64MixRandom".equals(l32.name()), "name() == L32X64MixRandom");
    }

    static void testMetadata() {
        RandomGeneratorFactory<RandomGenerator> l32 = RandomGeneratorFactory.of("L32X64MixRandom");
        check(l32.group() != null && !l32.group().isEmpty(), "group() non-empty");
        check(l32.stateBits() > 0, "stateBits() > 0");
        check(l32.equidistribution() >= 0, "equidistribution() >= 0");
        BigInteger period = l32.period();
        check(period != null && period.signum() > 0, "period() positive");
        // L32X64MixRandom is documented as a statistical, non-stochastic, streamable generator.
        check(l32.isStatistical(), "L32X64MixRandom isStatistical");
        check(!l32.isStochastic(), "L32X64MixRandom not stochastic");
        check(!l32.isHardware(), "L32X64MixRandom not hardware");
        check(!l32.isDeprecated(), "L32X64MixRandom not deprecated");
        check(l32.isStreamable(), "L32X64MixRandom isStreamable");
    }

    static void testCreateVariants() {
        RandomGeneratorFactory<RandomGenerator> l32 = RandomGeneratorFactory.of("L32X64MixRandom");
        check(l32.create() != null, "create() non-null");
        check(l32.create(123456789L) != null, "create(long) non-null");
        check(l32.create(new byte[32]) != null, "create(byte[]) non-null");
        check(l32.create(1L).nextLong() != l32.create(2L).nextLong(),
              "different seeds differ (first nextLong)");

        RandomGeneratorFactory<RandomGenerator> byName = RandomGeneratorFactory.of(l32.name());
        check(byName != null && l32.name().equals(byName.name()), "of(name()) round-trip");
    }

    static void testDeterminism() {
        RandomGeneratorFactory<RandomGenerator> l32 = RandomGeneratorFactory.of("L32X64MixRandom");
        RandomGenerator a = l32.create(12345L);
        RandomGenerator b = l32.create(12345L);
        boolean same = true;
        for (int i = 0; i < 100; i++) {
            if (a.nextLong() != b.nextLong()) { same = false; break; }
        }
        check(same, "same seed yields identical sequence");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
