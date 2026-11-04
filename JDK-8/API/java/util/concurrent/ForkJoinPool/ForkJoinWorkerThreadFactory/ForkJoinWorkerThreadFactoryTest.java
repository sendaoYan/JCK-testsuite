/*
 * @test
 * @summary java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory conformance.
 * @run main ForkJoinWorkerThreadFactoryTest
 */

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;

public class ForkJoinWorkerThreadFactoryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFactory() {
        ForkJoinPool pool = new ForkJoinPool();
        ForkJoinPool.ForkJoinWorkerThreadFactory factory = pool.getFactory();
        ForkJoinWorkerThread t = factory.newThread(pool);
        check(t instanceof Thread, "newThread returns a Thread");
        check(t.getPool() == pool, "getPool returns the pool");
        check(t.getUncaughtExceptionHandler() != null, "has an uncaught exception handler");
    }

    public static void main(String[] args) {
        testFactory();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
