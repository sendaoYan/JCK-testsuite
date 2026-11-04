/*
 * @test
 * @summary java.util.concurrent.ForkJoinPool API conformance: invoke of a RecursiveTask
 *          sum.
 * @run main ForkJoinPoolTest
 */

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class ForkJoinPoolTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Sum extends RecursiveTask<Integer> {
        private final int n;
        Sum(int n) { this.n = n; }
        protected Integer compute() {
            if (n <= 1) return n;
            Sum s = new Sum(n - 1);
            s.fork();
            return n + s.join();
        }
    }

    static void testForkJoinPool() {
        ForkJoinPool pool = new ForkJoinPool();
        try {
            check(pool.invoke(new Sum(10)) == 55, "fork/join sum 1..10");
        } finally {
            pool.shutdown();
        }
    }

    public static void main(String[] args) {
        testForkJoinPool();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
