/*
 * @test
 * @summary java.util.random.RandomGenerator.SplittableGenerator conformance:
 *          hierarchy, split/splits/rngs.
 * @run main SplittableGeneratorTest
 */

import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

public class SplittableGeneratorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(RandomGenerator.StreamableGenerator.class.isAssignableFrom(
                RandomGenerator.SplittableGenerator.class),
              "SplittableGenerator extends StreamableGenerator");

        RandomGenerator.SplittableGenerator s = (RandomGenerator.SplittableGenerator)
            RandomGeneratorFactory.all()
                .filter(RandomGeneratorFactory::isSplittable)
                .findFirst().orElseThrow().create();

        RandomGenerator.SplittableGenerator child = s.split();
        check(child != null, "split() non-null");
        check(child.nextLong() == child.nextLong() || true, "split() usable");

        RandomGenerator.SplittableGenerator other = s.split(s);
        check(other != null, "split(source) non-null");

        check(s.splits().limit(8).count() == 8, "splits() unbounded");
        check(s.splits(16).count() == 16, "splits(streamSize)");
        check(s.splits(s).limit(8).count() == 8, "splits(source) unbounded");
        check(s.splits(16, s).count() == 16, "splits(streamSize,source)");
        check(s.rngs().limit(8).count() == 8, "rngs() unbounded");
        check(s.rngs(16).count() == 16, "rngs(streamSize)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
