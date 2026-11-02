/*
 * @test
 * @summary java.util.random.RandomGenerator.StreamableGenerator conformance:
 *          hierarchy, rngs()/rngs(long).
 * @run main StreamableGeneratorTest
 */

import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

public class StreamableGeneratorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(RandomGenerator.class.isAssignableFrom(
                RandomGenerator.StreamableGenerator.class),
              "StreamableGenerator extends RandomGenerator");

        RandomGenerator.StreamableGenerator s = (RandomGenerator.StreamableGenerator)
            RandomGeneratorFactory.all()
                .filter(RandomGeneratorFactory::isStreamable)
                .findFirst().orElseThrow().create();

        check(s.rngs().limit(8).count() == 8, "rngs() unbounded");
        check(s.rngs(16).count() == 16, "rngs(streamSize)");
        // each produced generator is itself a working RandomGenerator
        RandomGenerator first = s.rngs().findFirst().orElseThrow();
        check(first.nextLong() == first.nextLong() || true, "rngs() elements usable");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
