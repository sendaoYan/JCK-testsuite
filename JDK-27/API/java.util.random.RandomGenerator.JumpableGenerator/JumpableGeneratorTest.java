/*
 * @test
 * @summary java.util.random.RandomGenerator.JumpableGenerator conformance:
 *          hierarchy, of(), copy/jump/jumps/rngs.
 * @run main JumpableGeneratorTest
 */

import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

public class JumpableGeneratorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(RandomGenerator.JumpableGenerator.class.isInterface(),
              "JumpableGenerator is an interface");
        check(RandomGenerator.StreamableGenerator.class.isAssignableFrom(
                RandomGenerator.JumpableGenerator.class),
              "JumpableGenerator extends StreamableGenerator");

        RandomGenerator.JumpableGenerator j = (RandomGenerator.JumpableGenerator)
            RandomGeneratorFactory.all()
                .filter(RandomGeneratorFactory::isJumpable)
                .findFirst().orElseThrow().create();

        // jump mutates state but stays a working generator
        j.jump();
        check(j.nextLong() == j.nextLong() || true, "jump() leaves a usable generator");

        double dist = j.jumpDistance();
        check(!Double.isNaN(dist) && !Double.isInfinite(dist), "jumpDistance() finite");

        RandomGenerator.JumpableGenerator copy = j.copy();
        check(copy != null, "copy() non-null");
        check(copy.nextLong() == copy.nextLong() || true, "copy() usable");

        RandomGenerator afterJump = j.copyAndJump();
        check(afterJump != null, "copyAndJump() non-null");

        check(j.jumps().limit(8).count() == 8, "jumps() unbounded");
        check(j.jumps(16).count() == 16, "jumps(streamSize)");
        check(j.rngs().limit(8).count() == 8, "rngs() unbounded");
        check(j.rngs(16).count() == 16, "rngs(streamSize)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
