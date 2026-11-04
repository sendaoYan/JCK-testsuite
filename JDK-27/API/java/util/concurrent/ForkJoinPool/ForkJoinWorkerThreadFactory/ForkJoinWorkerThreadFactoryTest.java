/*
 * @test
 * @summary java.util.concurrent.ForkJoinPool$ForkJoinWorkerThreadFactory conformance: white-box structure + black-box behavior.
 * @run main ForkJoinWorkerThreadFactoryTest
 */

import java.util.concurrent.*;

public class ForkJoinWorkerThreadFactoryTest {
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
        Class<?> c = cls("java.util.concurrent.ForkJoinPool$ForkJoinWorkerThreadFactory");
        check(c.isInterface(), "ForkJoinWorkerThreadFactory is an interface");
        hasMethod(c, "newThread", ForkJoinPool.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ForkJoinPool pool = new ForkJoinPool(1);
        try {
            ForkJoinPool.ForkJoinWorkerThreadFactory factory = pool.getFactory();
            ForkJoinWorkerThread t = factory.newThread(pool);
            check(t instanceof Thread, "newThread returns a Thread");
            check(t.getPool() == pool, "getPool returns the pool");
            check(t.getUncaughtExceptionHandler() != null, "has an uncaught exception handler");
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
