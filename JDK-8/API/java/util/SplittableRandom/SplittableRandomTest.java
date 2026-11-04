/*
 * @test
 * @summary java.util.SplittableRandom API conformance: seeded determinism, nextLong
 *          variation, nextInt(bound) range, and split.
 * @run main SplittableRandomTest
 */

import java.util.SplittableRandom;

public class SplittableRandomTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDeterminism() {
        SplittableRandom r = new SplittableRandom(42);
        long a = r.nextLong();
        long b = r.nextLong();
        check(a != b, "nextLong produces variation");
        SplittableRandom r2 = new SplittableRandom(42);
        check(r2.nextLong() == a && r2.nextLong() == b, "same seed same sequence");
    }

    static void testBoundAndSplit() {
        SplittableRandom r = new SplittableRandom(7);
        int v = r.nextInt(10);
        check(v >= 0 && v < 10, "nextInt(bound) within [0, bound)");
        check(r.split() != null, "split returns a child");
    }

    public static void main(String[] args) {
        testDeterminism();
        testBoundAndSplit();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
