/*
 * @test
 * @summary java.util.random.RandomGenerator$LeapableGenerator conformance: white-box structure + black-box behavior.
 * @run main LeapableGeneratorTest
 */

import java.util.random.*;

public class LeapableGeneratorTest {
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
        Class<?> c = cls("java.util.random.RandomGenerator$LeapableGenerator");
        check(c.isInterface(), "RandomGenerator.LeapableGenerator is an interface");
        check(RandomGenerator.JumpableGenerator.class.isAssignableFrom(c), "extends RandomGenerator.JumpableGenerator");
        hasMethod(c, "copy");
        hasMethod(c, "leap");
        hasMethod(c, "leapDistance");
        hasMethod(c, "leaps");
        hasMethod(c, "leaps", long.class);
        hasMethod(c, "copyAndLeap");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        RandomGenerator.LeapableGenerator g = (RandomGenerator.LeapableGenerator) RandomGeneratorFactory.all()
            .filter(RandomGeneratorFactory::isLeapable).findFirst().orElseThrow().create();
        double d = g.leapDistance();
        check(d > 0 && Double.isFinite(d), "leapDistance positive finite");
        RandomGenerator.LeapableGenerator copy = g.copy();
        check(copy.nextLong() == g.nextLong(), "copy shares state");
        // copyAndLeap() returns the pre-leap copy and leaps the original forward.
        RandomGenerator.LeapableGenerator snapshot = g.copy();
        RandomGenerator.JumpableGenerator returned = g.copyAndLeap();
        check(returned.nextLong() == snapshot.nextLong(), "copyAndLeap returns pre-leap copy");
        check(g.leaps().limit(6).count() == 6, "leaps() unbounded");
        check(g.leaps(12).count() == 12, "leaps(size)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
