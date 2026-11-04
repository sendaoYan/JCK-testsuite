/*
 * @test
 * @summary java.util.random.RandomGenerator$StreamableGenerator conformance: white-box structure + black-box behavior.
 * @run main StreamableGeneratorTest
 */

import java.util.random.*;

public class StreamableGeneratorTest {
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
        Class<?> c = cls("java.util.random.RandomGenerator$StreamableGenerator");
        check(c.isInterface(), "RandomGenerator.StreamableGenerator is an interface");
        check(RandomGenerator.class.isAssignableFrom(c), "extends RandomGenerator");
        hasMethod(c, "rngs");
        hasMethod(c, "rngs", long.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        RandomGeneratorFactory<RandomGenerator> fac = RandomGeneratorFactory.all()
            .filter(RandomGeneratorFactory::isStreamable).findFirst().orElseThrow();
        RandomGenerator.StreamableGenerator s = (RandomGenerator.StreamableGenerator) fac.create();
        check(s.rngs().limit(5).count() == 5, "rngs() unbounded");
        check(s.rngs(9).count() == 9, "rngs(size)");
        RandomGenerator.StreamableGenerator s2 = RandomGenerator.StreamableGenerator.of(fac.name());
        check(s2 != null, "of(name) non-null");
        long v = s2.nextLong();
        check(v >= Long.MIN_VALUE && v <= Long.MAX_VALUE, "of(name) generator usable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
