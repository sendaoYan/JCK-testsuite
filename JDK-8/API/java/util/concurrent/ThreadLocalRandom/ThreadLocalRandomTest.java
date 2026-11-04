/*
 * @test
 * @summary java.util.concurrent.ThreadLocalRandom API conformance: nextInt(bound),
 *          nextLong(bound), and nextDouble range.
 * @run main ThreadLocalRandomTest
 */

import java.util.concurrent.ThreadLocalRandom;

public class ThreadLocalRandomTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testThreadLocalRandom() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        int v = r.nextInt(10);
        check(v >= 0 && v < 10, "nextInt(bound) range");
        long w = r.nextLong(5);
        check(w >= 0 && w < 5, "nextLong(bound) range");
        double d = r.nextDouble();
        check(d >= 0.0 && d < 1.0, "nextDouble range");
    }

    public static void main(String[] args) {
        testThreadLocalRandom();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
