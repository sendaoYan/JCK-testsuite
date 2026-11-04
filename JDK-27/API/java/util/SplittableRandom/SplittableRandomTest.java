/*
 * @test
 * @summary java.util.SplittableRandom conformance: white-box structure plus black-box behavior
 *          of seeded determinism, nextLong/nextInt/nextInt(bound)/nextInt(origin,bound)/nextDouble
 *          ranges, and deterministic split.
 * @run main SplittableRandomTest
 */

import java.util.SplittableRandom;
import java.util.random.RandomGenerator;

public class SplittableRandomTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.SplittableRandom");
        check(!c.isInterface(), "SplittableRandom is a class");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "SplittableRandom is final");
        check(RandomGenerator.class.isAssignableFrom(c), "SplittableRandom implements RandomGenerator");
        check(RandomGenerator.SplittableGenerator.class.isAssignableFrom(c),
              "SplittableRandom implements SplittableGenerator");
        c.getMethod("split");
        c.getMethod("nextLong");
        c.getMethod("nextInt", int.class);
        c.getMethod("nextDouble");
        passed++;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        testDeterminism();
        testRanges();
        testSplit();
    }

    static void testDeterminism() {
        SplittableRandom r = new SplittableRandom(42);
        long a = r.nextLong();
        long b = r.nextLong();
        check(a != b, "nextLong produces variation");
        SplittableRandom r2 = new SplittableRandom(42);
        check(r2.nextLong() == a && r2.nextLong() == b, "same seed same sequence");

        SplittableRandom x = new SplittableRandom(1);
        int i1 = x.nextInt();
        check(new SplittableRandom(1).nextInt() == i1, "nextInt deterministic");
    }

    static void testRanges() {
        SplittableRandom r = new SplittableRandom(7);
        for (int i = 0; i < 1000; i++) {
            int v = r.nextInt(10);
            check(v >= 0 && v < 10, "nextInt(bound) within [0, bound)");
        }
        for (int i = 0; i < 1000; i++) {
            int v = r.nextInt(5, 10);
            check(v >= 5 && v < 10, "nextInt(origin, bound) within [5, 10)");
        }
        double d = r.nextDouble();
        check(d >= 0.0 && d < 1.0, "nextDouble in [0,1)");
        check(r.nextLong(100) >= 0 && r.nextLong(100) < 100, "nextLong(bound) in [0,100)");
    }

    static void testSplit() {
        SplittableRandom a = new SplittableRandom(123L);
        SplittableRandom b = new SplittableRandom(123L);
        SplittableRandom childA = a.split();
        SplittableRandom childB = b.split();
        check(childA.nextLong() == childB.nextLong(), "split child deterministic for same seed");
        check(a.nextLong() == b.nextLong(), "parent sequence unaffected by split");
        check(childA.split() != null, "nested split returns a child");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
