/*
 * @test
 * @summary java.util.concurrent.RecursiveAction conformance: white-box structure + black-box
 *          behavior (compute invoked by ForkJoinPool, fork/join divide-and-conquer action).
 * @run main RecursiveActionTest
 */

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class RecursiveActionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Class<?> cls(String n) {
        try { return Class.forName(n); } catch (ClassNotFoundException e) { throw new AssertionError(e); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = cls("java.util.concurrent.RecursiveAction");
        check(!c.isInterface() && java.lang.reflect.Modifier.isAbstract(c.getModifiers()),
              "RecursiveAction is an abstract class");
        check(c.getSuperclass() == ForkJoinTask.class, "extends ForkJoinTask");
        try {
            c.getDeclaredMethod("compute");
            passed++;
        } catch (NoSuchMethodException e) {
            throw new AssertionError("missing compute()", e);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static class SumAction extends RecursiveAction {
        final int lo, hi;
        final AtomicInteger sum;
        SumAction(int lo, int hi, AtomicInteger sum) { this.lo = lo; this.hi = hi; this.sum = sum; }
        protected void compute() {
            if (hi - lo <= 1) {
                sum.addAndGet(lo);
                return;
            }
            int mid = (lo + hi) >>> 1;
            SumAction left = new SumAction(lo, mid, sum);
            SumAction right = new SumAction(mid, hi, sum);
            left.fork();
            right.compute();
            left.join();
        }
    }

    static void testBlackBox() throws Exception {
        // trivial action: compute() runs and mutates shared state
        AtomicBoolean ran = new AtomicBoolean(false);
        RecursiveAction task = new RecursiveAction() {
            protected void compute() { ran.set(true); }
        };
        ForkJoinPool pool = new ForkJoinPool();
        try {
            pool.invoke(task);
            check(ran.get(), "compute ran");

            // fork/join divide-and-conquer action accumulates a result
            AtomicInteger sum = new AtomicInteger();
            int n = 100;
            pool.invoke(new SumAction(0, n, sum));
            check(sum.get() == (n - 1) * n / 2, "fork/join action sums 0..99 = " + ((n - 1) * n / 2));
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
