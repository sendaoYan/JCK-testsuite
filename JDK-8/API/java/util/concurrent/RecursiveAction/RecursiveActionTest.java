/*
 * @test
 * @summary java.util.concurrent.RecursiveAction API conformance: compute is invoked by a
 *          ForkJoinPool.
 * @run main RecursiveActionTest
 */

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicBoolean;

public class RecursiveActionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testRecursiveAction() {
        final AtomicBoolean ran = new AtomicBoolean(false);
        RecursiveAction task = new RecursiveAction() {
            protected void compute() { ran.set(true); }
        };
        ForkJoinPool pool = new ForkJoinPool();
        try {
            pool.invoke(task);
        } finally {
            pool.shutdown();
        }
        check(ran.get(), "compute ran");
    }

    public static void main(String[] args) {
        testRecursiveAction();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
