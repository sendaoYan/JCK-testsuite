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

        // ---- 边界值/等价类/等价性（本棒补测） ----
        SplittableRandom r = new SplittableRandom(42L);
        check(r.nextInt(1) == 0, "nextInt(bound=1) == 0");
        check(r.nextLong(1) == 0L, "nextLong(bound=1) == 0");
        check(r.nextInt(5, 6) == 5, "nextInt(origin, origin+1) == origin");
        check(r.nextLong(7, 8) == 7L, "nextLong(origin, origin+1) == origin");
        double db = r.nextDouble(1.0);
        check(db >= 0.0 && db < 1.0, "nextDouble(bound=1) in [0,1)");
        byte[] empty = new byte[0];
        r.nextBytes(empty);
        check(empty.length == 0, "nextBytes(empty) no-op");

        // 越界 origin/bound 抛异常
        assertThrows(IllegalArgumentException.class, () -> new SplittableRandom(1).nextInt(0),
                "nextInt(bound=0)");
        assertThrows(IllegalArgumentException.class, () -> new SplittableRandom(1).nextInt(5, 5),
                "nextInt(origin==bound)");
        assertThrows(IllegalArgumentException.class, () -> new SplittableRandom(1).nextLong(5, 5),
                "nextLong(origin==bound)");
    }

    static void assertThrows(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": expected " + type.getSimpleName()
                    + " but got " + t.getClass().getSimpleName(), t);
        }
        throw new AssertionError(msg + ": expected " + type.getSimpleName() + " but nothing thrown");
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
