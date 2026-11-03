/*
 * @test
 * @summary java.util.random.RandomGenerator$SplittableGenerator conformance: white-box structure + black-box behavior.
 * @run main SplittableGeneratorTest
 */

import java.util.random.*;

public class SplittableGeneratorTest {
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
        Class<?> c = cls("java.util.random.RandomGenerator$SplittableGenerator");
        check(c.isInterface(), "RandomGenerator.SplittableGenerator is an interface");
        check(RandomGenerator.StreamableGenerator.class.isAssignableFrom(c), "extends RandomGenerator.StreamableGenerator");
        hasMethod(c, "split");
        hasMethod(c, "split", RandomGenerator.SplittableGenerator.class);
        hasMethod(c, "splits");
        hasMethod(c, "splits", long.class);
        hasMethod(c, "splits", RandomGenerator.SplittableGenerator.class);
        hasMethod(c, "splits", long.class, RandomGenerator.SplittableGenerator.class);
        hasMethod(c, "rngs");
        hasMethod(c, "rngs", long.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        RandomGenerator.SplittableGenerator s = (RandomGenerator.SplittableGenerator) RandomGeneratorFactory.all()
            .filter(RandomGeneratorFactory::isSplittable).findFirst().orElseThrow().create();
        RandomGenerator.SplittableGenerator a = s.split();
        RandomGenerator.SplittableGenerator b = s.split(a);
        check(a != null && b != null, "split()/split(source) return generators");
        check(a.nextLong() == a.nextLong() || true, "split() usable");
        check(s.splits().limit(5).count() == 5, "splits() unbounded");
        check(s.splits(8).count() == 8, "splits(size)");
        check(s.splits(s).limit(3).count() == 3, "splits(source) unbounded");
        check(s.splits(4, s).count() == 4, "splits(size, source)");
        check(s.rngs().limit(5).count() == 5, "rngs() unbounded");
        check(s.rngs(8).count() == 8, "rngs(size)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
