/*
 * @test
 * @summary java.util.concurrent.RecursiveTask API conformance: compute a value via ForkJoinPool.
 * @run main RecursiveTaskTest
 */

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class RecursiveTaskTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testRecursiveTask() {
        RecursiveTask<Integer> task = new RecursiveTask<Integer>() {
            protected Integer compute() {
                return 42;
            }
        };
        ForkJoinPool pool = new ForkJoinPool();
        try {
            check(pool.invoke(task) == 42, "invoke returns computed value");
        } finally {
            pool.shutdown();
        }
    }

    public static void main(String[] args) {
        testRecursiveTask();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
