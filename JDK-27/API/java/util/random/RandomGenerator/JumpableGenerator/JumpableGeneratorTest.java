/*
 * @test
 * @summary java.util.random.RandomGenerator$JumpableGenerator conformance: white-box structure + black-box behavior.
 * @run main JumpableGeneratorTest
 */

import java.util.random.*;

public class JumpableGeneratorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Class<?> cls(String n) {
        try { return Class.forName(n); } catch (ClassNotFoundException e) { throw new AssertionError(e); }
    }

    static void hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); } catch (NoSuchMethodException e) { throw new AssertionError("missing " + name + " on " + c.getName(), e); }
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = cls("java.util.random.RandomGenerator$JumpableGenerator");
        check(c.isInterface(), "RandomGenerator.JumpableGenerator is an interface");
        check(RandomGenerator.StreamableGenerator.class.isAssignableFrom(c), "extends RandomGenerator.StreamableGenerator");
        hasMethod(c, "copy");
        hasMethod(c, "jump");
        hasMethod(c, "jumpDistance");
        hasMethod(c, "jumps");
        hasMethod(c, "jumps", long.class);
        hasMethod(c, "rngs");
        hasMethod(c, "rngs", long.class);
        hasMethod(c, "copyAndJump");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        RandomGenerator.JumpableGenerator g = (RandomGenerator.JumpableGenerator) RandomGeneratorFactory.all()
            .filter(RandomGeneratorFactory::isJumpable).findFirst().orElseThrow().create();
        double d = g.jumpDistance();
        check(d > 0 && Double.isFinite(d), "jumpDistance positive finite");
        RandomGenerator.JumpableGenerator copy = g.copy();
        check(copy.nextLong() == g.nextLong(), "copy shares state (same next value)");
        // copyAndJump() returns the pre-jump copy and jumps the original forward.
        RandomGenerator.JumpableGenerator snapshot = g.copy();
        RandomGenerator.JumpableGenerator returned = (RandomGenerator.JumpableGenerator) g.copyAndJump();
        check(returned.nextLong() == snapshot.nextLong(), "copyAndJump returns pre-jump copy");
        check(g.jumps().limit(6).count() == 6, "jumps() unbounded");
        check(g.jumps(12).count() == 12, "jumps(size)");
        check(g.rngs().limit(6).count() == 6, "rngs() unbounded");
        check(g.rngs(12).count() == 12, "rngs(size)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
