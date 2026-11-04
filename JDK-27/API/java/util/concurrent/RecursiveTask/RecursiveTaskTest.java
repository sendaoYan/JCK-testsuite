/*
 * @test
 * @summary java.util.concurrent.RecursiveTask conformance: white-box structure + black-box
 *          behavior (compute via ForkJoinPool, fork/join divide-and-conquer).
 * @run main RecursiveTaskTest
 */

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class RecursiveTaskTest {
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

    // =============== contract additions ===============

    // ---- Part A: full white-box surface ----
    static void testWhiteBoxStructure() throws Exception {
        Class<?> c = cls("java.util.concurrent.RecursiveTask");
        check(c.getConstructor() != null, "public no-arg constructor exists");
        java.lang.reflect.Method m = c.getDeclaredMethod("compute");
        check(java.lang.reflect.Modifier.isAbstract(m.getModifiers()), "compute() is abstract");
        check(!java.lang.reflect.Modifier.isAbstract(c.getMethod("getRawResult").getModifiers()),
              "getRawResult() is concrete in RecursiveTask");
    }

    // ---- Part B: raw-result lifecycle ----
    static void testRawResultLifecycle() throws Exception {
        RecursiveTask<Integer> t = new RecursiveTask<Integer>() {
            protected Integer compute() { return 42; }
        };
        check(t.getRawResult() == null, "getRawResult null before execution");
        check(!t.isDone(), "not done before execution");
        check(t.invoke() == 42, "invoke() on the caller thread returns the value");
        check(t.getRawResult() == 42, "getRawResult carries the value after compute");
        check(t.join() == 42, "join returns the computed value");
        check(t.get() == 42, "get returns the computed value");
    }

    // ---- Part C: divide-and-conquer sum 1..100 == 5050 with invokeAll ----
    static class Sum100 extends RecursiveTask<Long> {
        static final int TH = 8;
        final int[] a; final int lo, hi;
        Sum100(int[] a, int lo, int hi) { this.a = a; this.lo = lo; this.hi = hi; }
        protected Long compute() {
            if (hi - lo <= TH) {
                long s = 0;
                for (int i = lo; i < hi; i++) s += a[i];
                return s;
            }
            int mid = (lo + hi) >>> 1;
            Sum100 l = new Sum100(a, lo, mid);
            Sum100 r = new Sum100(a, mid, hi);
            invokeAll(l, r);
            if (!l.isDone() || !r.isDone())
                throw new AssertionError("invokeAll children must be complete");
            return l.join() + r.join();
        }
    }

    static void testDivideAndConquerSum() throws Exception {
        int[] data = new int[100];
        for (int i = 0; i < 100; i++) data[i] = i + 1;
        ForkJoinPool pool = new ForkJoinPool(3);
        try {
            check(pool.invoke(new Sum100(data, 0, data.length)) == 5050L,
                  "invokeAll-based divide-and-conquer sum 1..100 == 5050");
            // and via fork/join pairing from the main thread
            Sum100 top = new Sum100(data, 0, data.length);
            top.fork();
            check(top.join() == 5050L, "external fork + join also yields 5050");
        } finally { pool.shutdownNow(); }
    }

    // ---- Part D: recursive maximum with fork/join ----
    static class Max extends RecursiveTask<Long> {
        static final int TH = 8;
        final long[] a; final int lo, hi;
        Max(long[] a, int lo, int hi) { this.a = a; this.lo = lo; this.hi = hi; }
        protected Long compute() {
            if (hi - lo <= TH) {
                long m = Long.MIN_VALUE;
                for (int i = lo; i < hi; i++) m = Math.max(m, a[i]);
                return m;
            }
            int mid = (lo + hi) >>> 1;
            Max l = new Max(a, lo, mid);
            Max r = new Max(a, mid, hi);
            l.fork();
            long rv = r.compute();
            return Math.max(l.join(), rv);
        }
    }

    static void testRecursiveMax() throws Exception {
        long[] vals = new long[100];
        for (int i = 0; i < 100; i++) vals[i] = (i * 53L) % 199L;
        vals[41] = 500L;
        ForkJoinPool pool = new ForkJoinPool(3);
        try {
            check(pool.invoke(new Max(vals, 0, vals.length)) == 500L,
                  "recursive fork/join max == fixed maximum");
        } finally { pool.shutdownNow(); }
    }

    // ---- Part E: fib(10) == 55 classic ----
    static class Fib extends RecursiveTask<Integer> {
        final int n;
        Fib(int n) { this.n = n; }
        protected Integer compute() {
            if (n <= 1) return n;
            Fib a = new Fib(n - 1);
            a.fork();
            return a.join() + new Fib(n - 2).invoke();
        }
    }

    static void testFibonacci() throws Exception {
        ForkJoinPool pool = new ForkJoinPool(2);
        try {
            check(pool.invoke(new Fib(10)) == 55, "fork/join fib(10) == 55");
        } finally { pool.shutdownNow(); }
    }

    // ---- Part F: exception semantics ----
    static void testExceptionSemantics() throws Exception {
        final IllegalStateException boom = new IllegalStateException("rt");
        RecursiveTask<Integer> bad = new RecursiveTask<Integer>() {
            protected Integer compute() { throw boom; }
        };
        try {
            bad.invoke();
            check(false, "same-thread invoke must rethrow");
        } catch (RuntimeException e) {
            check(e == boom, "same-thread invoke rethrows the original object");
        }
        check(bad.isCompletedAbnormally(), "completed abnormally");
        check(bad.getException() == boom, "getException == original (same thread)");

        ForkJoinPool pool = new ForkJoinPool(2);
        try {
            RecursiveTask<Integer> bad2 = new RecursiveTask<Integer>() {
                protected Integer compute() { throw boom; }
            };
            try {
                pool.invoke(bad2);
                check(false, "cross-thread invoke must throw");
            } catch (IllegalStateException e) {
                check(e != boom, "cross-thread rethrow is rebuilt");
                check(e.getCause() == boom, "rebuilt wraps the original as cause");
            }
            // child failure propagates through parent join
            RecursiveTask<Integer> parent = new RecursiveTask<Integer>() {
                protected Integer compute() {
                    RecursiveTask<Integer> child = new RecursiveTask<Integer>() {
                        protected Integer compute() { throw boom; }
                    };
                    invokeAll(child);
                    return child.join();
                }
            };
            try {
                pool.invoke(parent);
                check(false, "parent of failing child must throw");
            } catch (IllegalStateException e) {
                check(true, "child exception propagates through parent join (" + e.getClass().getSimpleName() + ")");
            }
        } finally { pool.shutdownNow(); }
    }

    // ---- Part G: timed get on a blocked task ----
    static class Blocked extends RecursiveTask<Integer> {
        final CountDownLatch started = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        protected Integer compute() {
            started.countDown();
            try {
                if (!release.await(5, TimeUnit.SECONDS)) throw new AssertionError("release timeout");
            } catch (InterruptedException e) { throw new RuntimeException(e); }
            return 777;
        }
    }

    static void testTimedGet() throws Exception {
        ForkJoinPool pool = new ForkJoinPool(1);
        Blocked t = new Blocked();
        try {
            ForkJoinTask<Integer> f = pool.submit(t);
            check(t.started.await(5, TimeUnit.SECONDS), "blocked task started");
            try {
                f.get(150, TimeUnit.MILLISECONDS);
                check(false, "timed get must time out");
            } catch (TimeoutException e) { check(true, "get(150ms) -> TimeoutException"); }
            t.release.countDown();
            check(f.get() == 777, "value returned after release");
        } finally {
            t.release.countDown();
            pool.shutdownNow();
        }
    }

    // ---- Part H: reinitialize reuse ----
    static void testReinitialize() throws Exception {
        final AtomicInteger runs = new AtomicInteger();
        RecursiveTask<Integer> t = new RecursiveTask<Integer>() {
            protected Integer compute() { return runs.incrementAndGet() * 10; }
        };
        check(t.invoke() == 10, "first execution");
        t.reinitialize();
        check(t.getRawResult() == 10,
              "reinitialize leaves the previous raw result untouched (documented)");
        check(t.invoke() == 20, "second execution after reinitialize");
        check(runs.get() == 2, "compute ran twice");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testWhiteBoxStructure();
        testRawResultLifecycle();
        testDivideAndConquerSum();
        testRecursiveMax();
        testFibonacci();
        testExceptionSemantics();
        testTimedGet();
        testReinitialize();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
