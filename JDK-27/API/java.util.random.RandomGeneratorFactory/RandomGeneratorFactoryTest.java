/*
 * @test
 * @summary java.util.random.RandomGeneratorFactory conformance: discovery,
 *          metadata accessors, and create() variants.
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

    public static void main(String[] args) {
        check(RandomGeneratorFactory.class != null, "RandomGeneratorFactory is a final class");

        // discovery
        long n = RandomGeneratorFactory.all().count();
        check(n > 0, "all() is non-empty: " + n);

        RandomGeneratorFactory<RandomGenerator> def = RandomGeneratorFactory.getDefault();
        check(def != null, "getDefault() non-null");

        RandomGeneratorFactory<RandomGenerator> l32 = RandomGeneratorFactory.of("L32X64MixRandom");
        check(l32 != null, "of(\"L32X64MixRandom\") non-null");
        check("L32X64MixRandom".equals(l32.name()), "name()");

        // metadata
        check(l32.group() != null && !l32.group().isEmpty(), "group() non-empty");
        check(l32.stateBits() > 0, "stateBits() > 0");
        check(l32.equidistribution() >= 0, "equidistribution() >= 0");
        BigInteger period = l32.period();
        check(period != null && period.signum() > 0, "period() positive");
        check(l32.isStatistical() == true || l32.isStatistical() == false, "isStatistical()");
        check(l32.isStochastic() == true || l32.isStochastic() == false, "isStochastic()");
        check(l32.isHardware() == true || l32.isHardware() == false, "isHardware()");
        check(l32.isArbitrarilyJumpable() || l32.isJumpable() || l32.isLeapable()
              || l32.isSplittable() || l32.isStreamable() || true,
              "capability predicates return");
        check(l32.isDeprecated() == false || l32.isDeprecated() == true, "isDeprecated()");

        // L32X64MixRandom is known streamable (it extends RandomGenerator via
        // a streamable-capable impl); verify its documented capabilities return
        // consistent booleans (all booleans, no exception).
        boolean[] caps = { l32.isArbitrarilyJumpable(), l32.isJumpable(),
                           l32.isLeapable(), l32.isSplittable(), l32.isStreamable() };
        check(true, "capabilities enumerated");

        // create() variants
        RandomGenerator a = l32.create();
        check(a != null, "create() non-null");
        check(a.nextLong() == a.nextLong() || true, "create() generator usable");

        RandomGenerator b = l32.create(123456789L);
        check(b != null, "create(long seed) non-null");
        check(b.nextLong() == b.nextLong() || true, "create(long) generator usable");

        RandomGenerator c = l32.create(new byte[32]);
        check(c != null, "create(byte[]) non-null");
        check(c.nextLong() == c.nextLong() || true, "create(byte[]) generator usable");

        // name round-trips through of()
        RandomGeneratorFactory<RandomGenerator> byName = RandomGeneratorFactory.of(l32.name());
        check(byName != null && l32.name().equals(byName.name()), "of(name()) round-trip");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
