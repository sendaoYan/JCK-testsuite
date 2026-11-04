/*
 * @test
 * @summary java.util.concurrent.RecursiveTask conformance: white-box structure + black-box
 *          behavior (compute via ForkJoinPool, fork/join divide-and-conquer).
 * @run main RecursiveTaskTest
 */

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;

public class RecursiveTaskTest {
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
        Class<?> c = cls("java.util.concurrent.RecursiveTask");
        check(!c.isInterface() && java.lang.reflect.Modifier.isAbstract(c.getModifiers()),
              "RecursiveTask is an abstract class");
        check(c.getSuperclass() == ForkJoinTask.class, "extends ForkJoinTask");
        try {
            c.getDeclaredMethod("compute");
            passed++;
        } catch (NoSuchMethodException e) {
            throw new AssertionError("missing compute()", e);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static class Sum extends RecursiveTask<Long> {
        final long[] a;
        final int lo, hi;
        Sum(long[] a, int lo, int hi) { this.a = a; this.lo = lo; this.hi = hi; }
        protected Long compute() {
            if (hi - lo <= 1) return a[lo];
            int mid = (lo + hi) >>> 1;
            Sum left = new Sum(a, lo, mid);
            Sum right = new Sum(a, mid, hi);
            left.fork();
            Long r = right.compute();
            Long l = left.join();
            return l + r;
        }
    }

    static void testBlackBox() throws Exception {
        // trivial compute returns a value
        RecursiveTask<Integer> trivial = new RecursiveTask<Integer>() {
            protected Integer compute() { return 42; }
        };
        ForkJoinPool pool = new ForkJoinPool();
        try {
            check(pool.invoke(trivial) == 42, "invoke returns computed value");

            // real fork/join divide-and-conquer sum
            long[] data = new long[1000];
            long expected = 0;
            for (int i = 0; i < data.length; i++) {
                data[i] = i + 1;
                expected += data[i];
            }
            check(pool.invoke(new Sum(data, 0, data.length)) == expected,
                  "fork/join divide-and-conquer sum matches sequential sum");
        } finally {
            pool.shutdown();
        }
        check(pool.awaitTermination(10, TimeUnit.SECONDS), "pool terminates");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
