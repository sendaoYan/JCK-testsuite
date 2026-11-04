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
import java.util.concurrent.atomic.AtomicLong;

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

    // =============== contract additions ===============

    // ---- Part A: full white-box surface ----
    static void testWhiteBoxStructure() throws Exception {
        Class<?> c = cls("java.util.concurrent.RecursiveAction");
        check(c.getConstructor() != null, "public no-arg constructor exists");
        java.lang.reflect.Method m = c.getDeclaredMethod("compute");
        check(java.lang.reflect.Modifier.isAbstract(m.getModifiers()), "compute() is abstract");
        check(m.getReturnType() == void.class, "compute() returns void");
        check(c.getMethod("getRawResult").getReturnType() == Void.class,
              "getRawResult() declared to return Void");
    }

    // ---- Part B: raw result is always null ----
    static void testRawResultAlwaysNull() throws Exception {
        RecursiveAction a = new RecursiveAction() {
            protected void compute() { }
        };
        check(a.getRawResult() == null, "getRawResult null before execution");
        a.invoke();
        check(a.isDone(), "done after invoke");
        check(a.getRawResult() == null, "getRawResult null after execution");
        check(a.join() == null, "join() returns null for RecursiveAction");
        check(a.get() == null, "get() returns null for RecursiveAction");
        check(a.invoke() == null, "invoke() returns null for RecursiveAction");
    }

    // ---- Part C: invokeAll(t1, t2) both complete before returning ----
    static void testInvokeAllPair() throws Exception {
        final AtomicInteger runs = new AtomicInteger();
        RecursiveAction a1 = new RecursiveAction() {
            protected void compute() { runs.incrementAndGet(); }
        };
        RecursiveAction a2 = new RecursiveAction() {
            protected void compute() { runs.incrementAndGet(); }
        };
        ForkJoinTask.invokeAll(a1, a2);
        check(runs.get() == 2, "invokeAll(t1,t2) executed both actions");
        check(a1.isDone() && a2.isDone(), "both actions complete before invokeAll returns");

        ForkJoinTask.invokeAll(new RecursiveAction[] { });
        ForkJoinTask.invokeAll();
        check(true, "empty invokeAll completes without error");
        java.util.ArrayList<RecursiveAction> empty = new java.util.ArrayList<RecursiveAction>();
        check(ForkJoinTask.invokeAll(empty).isEmpty(), "invokeAll(empty collection) -> empty result");
    }

    // ---- Part D: nested divide-and-conquer transformations ----
    static class Doubler extends RecursiveAction {
        static final int TH = 8;
        final int[] a; final int lo, hi;
        Doubler(int[] a, int lo, int hi) { this.a = a; this.lo = lo; this.hi = hi; }
        protected void compute() {
            if (hi - lo <= TH) {
                for (int i = lo; i < hi; i++) a[i] = a[i] * 2;
                return;
            }
            int mid = (lo + hi) >>> 1;
            Doubler l = new Doubler(a, lo, mid);
            Doubler r = new Doubler(a, mid, hi);
            invokeAll(l, r);
            if (!l.isDone() || !r.isDone())
                throw new AssertionError("children must be done after invokeAll");
        }
    }

    static class MaxFinder extends RecursiveAction {
        static final int TH = 8;
        final long[] a; final int lo, hi;
        final AtomicLong max;
        MaxFinder(long[] a, int lo, int hi, AtomicLong max) { this.a = a; this.lo = lo; this.hi = hi; this.max = max; }
        protected void compute() {
            if (hi - lo <= TH) {
                for (int i = lo; i < hi; i++) {
                    long v, seen;
                    while ((v = a[i]) > (seen = max.get()))
                        if (max.compareAndSet(seen, v)) break;
                }
                return;
            }
            int mid = (lo + hi) >>> 1;
            invokeAll(new MaxFinder(a, lo, mid, max), new MaxFinder(a, mid, hi, max));
        }
    }

    static void testDivideAndConquer() throws Exception {
        int[] data = new int[100];
        for (int i = 0; i < 100; i++) data[i] = i + 1;
        ForkJoinPool pool = new ForkJoinPool(3);
        try {
            pool.invoke(new Doubler(data, 0, data.length));
            boolean ok = true;
            for (int i = 0; i < 100; i++) if (data[i] != (i + 1) * 2) ok = false;
            check(ok, "recursive action doubled every element (1..100 -> 2..200)");

            long[] vals = new long[100];
            for (int i = 0; i < 100; i++) vals[i] = (i * 37L) % 101L;
            vals[73] = 1000L; // unique maximum
            AtomicLong max = new AtomicLong(Long.MIN_VALUE);
            pool.invoke(new MaxFinder(vals, 0, vals.length, max));
            check(max.get() == 1000L, "recursive action found the fixed maximum");
        } finally { pool.shutdownNow(); }
    }

    // ---- Part E: exception propagation ----
    static void testExceptionPropagation() throws Exception {
        final IllegalStateException boom = new IllegalStateException("ra");
        RecursiveAction bad = new RecursiveAction() {
            protected void compute() { throw boom; }
        };
        try {
            bad.invoke(); // same thread
            check(false, "compute exception must propagate");
        } catch (RuntimeException e) {
            check(e == boom, "same-thread invoke rethrows the original object");
        }
        check(bad.isCompletedAbnormally(), "completed abnormally");
        check(bad.getException() == boom, "getException == original (same thread)");

        ForkJoinPool pool = new ForkJoinPool(2);
        try {
            RecursiveAction bad2 = new RecursiveAction() {
                protected void compute() { throw boom; }
            };
            try {
                pool.invoke(bad2);
                check(false, "pool invoke of failing action must throw");
            } catch (IllegalStateException e) {
                check(e != boom, "cross-thread rethrow rebuilds the exception");
                check(e.getCause() == boom, "rebuilt copy carries original as cause");
            }
        } finally { pool.shutdownNow(); }
    }

    // ---- Part F: fork/join from main thread and reuse ----
    static void testForkJoinAndReuse() throws Exception {
        final AtomicInteger runs = new AtomicInteger();
        RecursiveAction a = new RecursiveAction() {
            protected void compute() { runs.incrementAndGet(); }
        };
        a.fork();           // external fork uses commonPool
        a.join();
        check(runs.get() == 1, "external fork+join ran the action exactly once");
        check(a.isDone(), "done after join");

        a.reinitialize();
        check(!a.isDone(), "not done after reinitialize");
        a.invoke();
        check(runs.get() == 2, "action reusable after reinitialize");

        RecursiveAction quiet = new RecursiveAction() {
            protected void compute() { }
        };
        quiet.quietlyComplete();
        check(quiet.isDone(), "quietlyComplete marks the action done");
        quiet.join();
        check(true, "join on quietly-completed action returns normally");
    }

    // ---- Part G: invokeAll collection form ----
    static void testInvokeAllCollection() throws Exception {
        final AtomicInteger runs = new AtomicInteger();
        java.util.ArrayList<RecursiveAction> ts = new java.util.ArrayList<RecursiveAction>();
        for (int i = 0; i < 5; i++) {
            ts.add(new RecursiveAction() {
                protected void compute() { runs.incrementAndGet(); }
            });
        }
        java.util.Collection<RecursiveAction> done = ForkJoinTask.invokeAll(ts);
        check(runs.get() == 5, "invokeAll(collection) ran all five actions");
        check(done.size() == 5, "result collection has all five tasks");
        for (RecursiveAction r : done) check(r.isDone(), "collection task done");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testWhiteBoxStructure();
        testRawResultAlwaysNull();
        testInvokeAllPair();
        testDivideAndConquer();
        testExceptionPropagation();
        testForkJoinAndReuse();
        testInvokeAllCollection();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
