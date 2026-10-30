/*
 * @test
 * @summary java.util.concurrent.ForkJoinTask API conformance: invoke computes a raw result.
 * @run main ForkJoinTaskTest
 */

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

public class ForkJoinTaskTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Task extends ForkJoinTask<Integer> {
        int result;

        public Integer getRawResult() {
            return result;
        }

        protected void setRawResult(Integer value) {
            result = value;
        }

        protected boolean exec() {
            setRawResult(42);
            return true;
        }
    }

    static void testForkJoinTask() {
        ForkJoinTask<Integer> task = new Task();
        ForkJoinPool pool = new ForkJoinPool();
        try {
            check(pool.invoke(task) == 42, "invoke returns the raw result");
        } finally {
            pool.shutdown();
        }
    }

    public static void main(String[] args) {
        testForkJoinTask();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
