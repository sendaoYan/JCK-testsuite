/*
 * @test
 * @summary java.util.concurrent.ForkJoinWorkerThread conformance: white-box structure + black-box behavior.
 * @run main ForkJoinWorkerThreadTest
 */

import java.util.concurrent.*;

public class ForkJoinWorkerThreadTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Class<?> cls(String n) {
        try { return Class.forName(n); } catch (ClassNotFoundException e) { throw new AssertionError(e); }
    }

    static void hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); } catch (NoSuchMethodException e) { throw new AssertionError("missing " + name + " on " + c.getName(), e); }
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = cls("java.util.concurrent.ForkJoinWorkerThread");
        check(!c.isInterface(), "class");
        check(Thread.class.isAssignableFrom(c), "extends Thread");
        hasMethod(c, "getPool");
        hasMethod(c, "getPoolIndex");
        hasMethod(c, "getQueuedTaskCount");
        hasMethod(c, "run");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ForkJoinPool pool = new ForkJoinPool(1);
        try {
            ForkJoinWorkerThread wt = pool.getFactory().newThread(pool);
            check(wt instanceof Thread, "extends Thread");
            check(wt.getPool() == pool, "getPool returns the pool");
            check(wt.getPoolIndex() >= -1, "getPoolIndex callable");
            check(wt.getQueuedTaskCount() == 0, "fresh worker has no queued tasks");
        } finally {
            pool.shutdown();
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
