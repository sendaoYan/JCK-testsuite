/*
 * @test
 * @summary java.util.concurrent.ForkJoinPool conformance: white-box structure + black-box
 *          behavior (fork/join divide-and-conquer, submit, commonPool, shutdown).
 * @run main ForkJoinPoolTest
 */

import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ForkJoinPoolTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Class<?> cls(String n) {
        try { return Class.forName(n); } catch (ClassNotFoundException e) { throw new AssertionError(e); }
    }

    static void hasMethod(Class<?> c, String n, Class<?>... p) {
        try { c.getMethod(n, p); } catch (NoSuchMethodException e) { throw new AssertionError("missing " + n + " on " + c.getName(), e); }
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = cls("java.util.concurrent.ForkJoinPool");
        check(!c.isInterface(), "ForkJoinPool is a class");
        check(c.getSuperclass() == cls("java.util.concurrent.AbstractExecutorService"),
              "extends AbstractExecutorService");
        check(ScheduledExecutorService.class.isAssignableFrom(c), "implements ScheduledExecutorService (JDK-27)");
        hasMethod(c, "invoke", ForkJoinTask.class);
        hasMethod(c, "submit", Callable.class);
        hasMethod(c, "execute", Runnable.class);
        hasMethod(c, "commonPool");
        hasMethod(c, "getParallelism");
        hasMethod(c, "shutdown");
        hasMethod(c, "awaitTermination", long.class, TimeUnit.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static class Sum extends RecursiveTask<Long> {
        final long[] a;
        final int lo, hi;
        Sum(long[] a, int lo, int hi) { this.a = a; this.lo = lo; this.hi = hi; }
        protected Long compute() {
            if (hi - lo <= 2) {
                long s = 0;
                for (int i = lo; i < hi; i++) s += a[i];
                return s;
            }
            int mid = (lo + hi) >>> 1;
            Sum left = new Sum(a, lo, mid);
            Sum right = new Sum(a, mid, hi);
            left.fork();
            Long r = right.compute();
            Long l = left.join();
            return l + r;
        }
    }

    static long seqSum(long[] a) {
        long s = 0;
        for (long v : a) s += v;
        return s;
    }

    static void testBlackBox() throws Exception {
        long[] data = new long[1000];
        long expected = 0;
        for (int i = 0; i < data.length; i++) {
            data[i] = i + 1;
            expected += data[i];
        }

        ForkJoinPool pool = new ForkJoinPool(4);
        try {
            check(pool.getParallelism() == 4, "getParallelism reflects constructor");
            // fork/join divide-and-conquer sum
            check(pool.invoke(new Sum(data, 0, data.length)) == expected,
                  "fork/join divide-and-conquer sum equals sequential sum");

            // submit(Callable) returns a ForkJoinTask
            ForkJoinTask<Integer> ft = pool.submit((Callable<Integer>) () -> 40 + 2);
            check(ft.get() == 42, "submit(Callable).get");

            // execute(Runnable) runs the task
            final boolean[] ran = new boolean[1];
            pool.execute(() -> ran[0] = true);
            pool.awaitQuiescence(10, TimeUnit.SECONDS);
            check(ran[0], "execute(Runnable) runs the task");
        } finally {
            pool.shutdown();
        }
        check(pool.awaitTermination(10, TimeUnit.SECONDS), "awaitTermination after shutdown");

        // commonPool exists and has positive parallelism
        ForkJoinPool common = ForkJoinPool.commonPool();
        check(common != null && common.getParallelism() >= 1, "commonPool has positive parallelism");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
