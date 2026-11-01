/*
 * @test
 * @summary java.util.concurrent.ForkJoinWorkerThread API conformance: getPool/thread identity.
 * @run main ForkJoinWorkerThreadTest
 */

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;

public class ForkJoinWorkerThreadTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testForkJoinWorkerThread() {
        ForkJoinPool pool = new ForkJoinPool(1);
        try {
            ForkJoinWorkerThread wt = pool.getFactory().newThread(pool);
            check(wt instanceof Thread, "extends Thread");
            check(wt.getPool() == pool, "getPool returns the pool");
        } finally {
            pool.shutdown();
        }
    }

    public static void main(String[] args) {
        testForkJoinWorkerThread();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
