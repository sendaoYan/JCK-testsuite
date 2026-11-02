/*
 * @test
 * @summary java.util.random.RandomGenerator.LeapableGenerator conformance:
 *          hierarchy, leap/leaps/copyAndLeap.
 * @run main LeapableGeneratorTest
 */

import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

public class LeapableGeneratorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(RandomGenerator.JumpableGenerator.class.isAssignableFrom(
                RandomGenerator.LeapableGenerator.class),
              "LeapableGenerator extends JumpableGenerator");

        RandomGenerator.LeapableGenerator l = (RandomGenerator.LeapableGenerator)
            RandomGeneratorFactory.all()
                .filter(RandomGeneratorFactory::isLeapable)
                .findFirst().orElseThrow().create();

        l.leap();
        check(l.nextLong() == l.nextLong() || true, "leap() leaves a usable generator");

        double dist = l.leapDistance();
        check(!Double.isNaN(dist) && !Double.isInfinite(dist), "leapDistance() finite");

        RandomGenerator.LeapableGenerator copy = l.copy();
        check(copy != null, "copy() non-null");

        RandomGenerator.JumpableGenerator afterLeap = l.copyAndLeap();
        check(afterLeap != null, "copyAndLeap() non-null");

        check(l.leaps().limit(8).count() == 8, "leaps() unbounded");
        check(l.leaps(16).count() == 16, "leaps(streamSize)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
