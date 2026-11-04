/*
 * @test
 * @summary java.util.concurrent.ForkJoinPool conformance: white-box structure + black-box
 *          behavior (fork/join divide-and-conquer, submit, commonPool, shutdown).
 * @run main ForkJoinPoolTest
 */

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

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
        check(c.getField("defaultForkJoinWorkerThreadFactory").get(null) != null,
              "static defaultForkJoinWorkerThreadFactory is non-null");
        hasMethod(c, "invoke", ForkJoinTask.class);
        hasMethod(c, "submit", Callable.class);
        hasMethod(c, "execute", Runnable.class);
        hasMethod(c, "commonPool");
        hasMethod(c, "getParallelism");
        hasMethod(c, "shutdown");
        hasMethod(c, "awaitTermination", long.class, TimeUnit.class);
        // JDK-27-specific surface
        hasMethod(c, "setParallelism", int.class);
        hasMethod(c, "externalSubmit", ForkJoinTask.class);
        hasMethod(c, "lazySubmit", ForkJoinTask.class);
        hasMethod(c, "invokeAllUninterruptibly", java.util.Collection.class);
        hasMethod(c, "invokeAny", java.util.Collection.class);
        hasMethod(c, "invokeAll", java.util.Collection.class, long.class, TimeUnit.class);
        hasMethod(c, "schedule", Runnable.class, long.class, TimeUnit.class);
        hasMethod(c, "schedule", Callable.class, long.class, TimeUnit.class);
        hasMethod(c, "scheduleAtFixedRate", Runnable.class, long.class, long.class, TimeUnit.class);
        hasMethod(c, "scheduleWithFixedDelay", Runnable.class, long.class, long.class, TimeUnit.class);
        hasMethod(c, "submitWithTimeout", Callable.class, long.class, TimeUnit.class,
                  java.util.function.Consumer.class);
        hasMethod(c, "cancelDelayedTasksOnShutdown");
        hasMethod(c, "getDelayedTaskCount");
        hasMethod(c, "close");
        hasMethod(c, "getQueuedSubmissionCount");
        hasMethod(c, "hasQueuedSubmissions");
        hasMethod(c, "getStealCount");
        hasMethod(c, "getAsyncMode");
        hasMethod(c, "getRunningThreadCount");
        hasMethod(c, "getActiveThreadCount");
        hasMethod(c, "isQuiescent");
        hasMethod(c, "awaitQuiescence", long.class, TimeUnit.class);
        hasMethod(c, "managedBlock", cls("java.util.concurrent.ForkJoinPool$ManagedBlocker"));
        // 10-arg constructor (since 9)
        check(c.getConstructor(int.class,
                cls("java.util.concurrent.ForkJoinPool$ForkJoinWorkerThreadFactory"),
                Thread.UncaughtExceptionHandler.class, boolean.class,
                int.class, int.class, int.class,
                java.util.function.Predicate.class, long.class, TimeUnit.class) != null,
              "10-arg constructor present (core/max parallelism, saturate, keepAlive)");
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

    // =============== contract additions ===============

    // ---- constructor matrix ----
    static void testConstructorMatrix() {
        try { new ForkJoinPool(0); check(false, "FJP(0) must throw IAE"); }
        catch (IllegalArgumentException e) { check(true, "FJP(0) -> IllegalArgumentException"); }
        try { new ForkJoinPool(-3); check(false, "FJP(-3) must throw IAE"); }
        catch (IllegalArgumentException e) { check(true, "FJP(-3) -> IllegalArgumentException"); }
        final Thread.UncaughtExceptionHandler ueh = new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread t, Throwable e) { }
        };
        ForkJoinPool def = new ForkJoinPool();
        try {
            check(!def.getAsyncMode(), "default ctor asyncMode false");
            check(def.getFactory() == ForkJoinPool.defaultForkJoinWorkerThreadFactory,
                  "default ctor uses defaultForkJoinWorkerThreadFactory");
            check(def.getUncaughtExceptionHandler() == null, "default ctor handler null");
        } finally { def.shutdownNow(); }
        ForkJoinPool async = new ForkJoinPool(2, ForkJoinPool.defaultForkJoinWorkerThreadFactory, ueh, true);
        try {
            check(async.getAsyncMode(), "4-arg ctor asyncMode true reflected");
            check(async.getUncaughtExceptionHandler() == ueh, "4-arg ctor handler returned");
        } finally { async.shutdownNow(); }
        ForkJoinPool ten = new ForkJoinPool(2, ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, false,
                1, 4, 256, null, 60, TimeUnit.SECONDS);
        try {
            check(ten.getParallelism() == 2, "10-arg ctor parallelism");
            check(!ten.getAsyncMode(), "10-arg ctor asyncMode false");
            check(ten.invoke(new RecursiveTask<Integer>() {
                protected Integer compute() { return 9; }
            }) == 9, "10-arg ctor pool executes tasks");
        } finally { ten.shutdownNow(); }
        try {
            new ForkJoinPool(0, ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, false,
                    1, 4, 256, null, 60, TimeUnit.SECONDS);
            check(false, "10-arg ctor parallelism 0 must throw IAE");
        } catch (IllegalArgumentException e) { check(true, "10-arg ctor parallelism 0 -> IAE"); }
        try {
            new ForkJoinPool(2, null, null, false);
            check(false, "null factory must throw NPE");
        } catch (NullPointerException e) { check(true, "null factory -> NullPointerException"); }
    }

    // ---- fresh pool observable state ----
    static void testFreshPoolState() {
        ForkJoinPool pool = new ForkJoinPool(2);
        try {
            check(pool.getPoolSize() == 0, "fresh pool has no worker threads yet");
            check(pool.isQuiescent(), "fresh pool is quiescent");
            check(pool.getRunningThreadCount() == 0, "fresh pool running == 0");
            check(pool.getActiveThreadCount() == 0, "fresh pool active == 0");
            check(pool.getQueuedTaskCount() == 0, "fresh pool queued tasks == 0");
            check(pool.getQueuedSubmissionCount() == 0, "fresh pool queued submissions == 0");
            check(!pool.hasQueuedSubmissions(), "fresh pool hasQueuedSubmissions false");
            check(pool.getStealCount() >= 0, "stealCount non-negative");
            check(pool.getDelayedTaskCount() >= 0, "delayed task count non-negative");
            check(!pool.isShutdown(), "fresh pool not shutdown");
            check(!pool.isTerminated(), "fresh pool not terminated");
            String s = pool.toString();
            check(s != null && s.contains("ForkJoinPool"), "toString contains class name");
            check(s.contains("Running"), "fresh pool toString contains Running");
        } finally { pool.shutdownNow(); }
    }

    // ---- common pool ----
    static void testCommonPool() throws Exception {
        ForkJoinPool a = ForkJoinPool.commonPool();
        ForkJoinPool b = ForkJoinPool.commonPool();
        check(a == b, "commonPool() returns the same instance");
        check(a.getParallelism() >= 1, "commonPool parallelism >= 1");
        check(ForkJoinPool.getCommonPoolParallelism() == a.getParallelism(),
              "getCommonPoolParallelism() == commonPool().getParallelism()");
        check(!a.isShutdown(), "common pool is never shut down");
        final AtomicBoolean ran = new AtomicBoolean(false);
        ForkJoinTask<?> t = new RecursiveAction() {
            protected void compute() { ran.set(true); }
        };
        a.execute(t);
        check(a.awaitQuiescence(5, TimeUnit.SECONDS), "commonPool quiesces after small task");
        check(t.isDone() && ran.get(), "commonPool executed the small task");
    }

    // ---- classic divide-and-conquer sum 1..100 = 5050 ----
    static class Sum100 extends RecursiveTask<Long> {
        static final int THRESHOLD = 8;
        final int[] a; final int lo, hi;
        Sum100(int[] a, int lo, int hi) { this.a = a; this.lo = lo; this.hi = hi; }
        protected Long compute() {
            if (hi - lo <= THRESHOLD) {
                long s = 0;
                for (int i = lo; i < hi; i++) s += a[i];
                return s;
            }
            int mid = (lo + hi) >>> 1;
            Sum100 left = new Sum100(a, lo, mid);
            Sum100 right = new Sum100(a, mid, hi);
            invokeAll(left, right);
            check(left.isDone() && right.isDone(), "invokeAll children done before return");
            return left.join() + right.join();
        }
    }

    static void testDivideAndConquerSum() {
        int[] data = new int[100];
        for (int i = 0; i < data.length; i++) data[i] = i + 1;
        ForkJoinPool pool = new ForkJoinPool(3);
        try {
            check(pool.invoke(new Sum100(data, 0, data.length)) == 5050L,
                  "fork/join divide-and-conquer sum 1..100 == 5050");
        } finally { pool.shutdownNow(); }
    }

    // ---- submit/execute variants ----
    static void testSubmitVariants() throws Exception {
        ForkJoinPool pool = new ForkJoinPool(2);
        try {
            RecursiveTask<Integer> fjt = new RecursiveTask<Integer>() {
                protected Integer compute() { return 40; }
            };
            ForkJoinTask<Integer> ret = pool.submit(fjt);
            check(ret == fjt, "submit(ForkJoinTask) returns the same task instance");
            check(ret.get() == 40, "submit(ForkJoinTask).get");

            ForkJoinTask<Integer> call = pool.submit(new Callable<Integer>() {
                public Integer call() { return 2; }
            });
            check(call.get() == 2, "submit(Callable).get");

            final AtomicBoolean ranR = new AtomicBoolean(false);
            ForkJoinTask<?> r = pool.submit(new Runnable() {
                public void run() { ranR.set(true); }
            });
            check(r.get() == null, "submit(Runnable).get returns null");
            check(ranR.get(), "submit(Runnable) ran the runnable");

            ForkJoinTask<String> rt = pool.submit(new Runnable() {
                public void run() { }
            }, "result");
            check("result".equals(rt.get()), "submit(Runnable,T).get returns T");

            final AtomicBoolean ranE = new AtomicBoolean(false);
            ForkJoinTask<?> e = new RecursiveAction() {
                protected void compute() { ranE.set(true); }
            };
            pool.execute(e);
            check(pool.awaitQuiescence(5, TimeUnit.SECONDS), "pool quiesces after execute(FJT)");
            check(e.isDone() && ranE.get(), "execute(ForkJoinTask) ran the task");

            check(pool.invoke(new RecursiveTask<Integer>() {
                protected Integer compute() { return 7; }
            }) == 7, "invoke returns the computed value");

            try { pool.invoke(null); check(false, "invoke(null) must throw NPE"); }
            catch (NullPointerException e2) { check(true, "invoke(null) -> NullPointerException"); }
            try { pool.execute((ForkJoinTask<?>) null); check(false, "execute(null FJT) must throw NPE"); }
            catch (NullPointerException e2) { check(true, "execute(null FJT) -> NullPointerException"); }
            try { pool.submit((ForkJoinTask<?>) null); check(false, "submit(null FJT) must throw NPE"); }
            catch (NullPointerException e2) { check(true, "submit(null FJT) -> NullPointerException"); }
        } finally { pool.shutdownNow(); }
    }

    // ---- exception relay from worker ----
    static void testExceptionFromTask() throws Exception {
        final IllegalStateException boom = new IllegalStateException("boom");
        ForkJoinPool pool = new ForkJoinPool(2);
        try {
            RecursiveTask<Integer> bad = new RecursiveTask<Integer>() {
                protected Integer compute() { throw boom; }
            };
            try {
                pool.invoke(bad);
                check(false, "failing task via invoke must throw");
            } catch (IllegalStateException e) {
                check(e != boom, "cross-thread rethrow rebuilds the exception object");
                check(e.getCause() == boom, "rebuilt rethrow carries the original as cause");
            }
            check(bad.isCompletedAbnormally(), "task completed abnormally");
            check(bad.getException() instanceof IllegalStateException, "getException class preserved");

            ForkJoinTask<Integer> badC = pool.submit(new Callable<Integer>() {
                public Integer call() { throw boom; }
            });
            try {
                badC.get();
                check(false, "failing callable get() must throw");
            } catch (java.util.concurrent.ExecutionException e) {
                check(e.getCause() instanceof IllegalStateException,
                      "submit(Callable) failure wrapped as ExecutionException with ISE cause");
            }
        } finally { pool.shutdownNow(); }
    }

    // ---- bounded active-thread invariants ----
    static class BlockTask extends RecursiveTask<Integer> {
        final CountDownLatch started = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        protected Integer compute() {
            started.countDown();
            try {
                if (!release.await(5, TimeUnit.SECONDS)) throw new AssertionError("release timeout");
            } catch (InterruptedException e) { throw new RuntimeException(e); }
            return 123;
        }
    }

    static void testActiveCountBounded() throws Exception {
        ForkJoinPool pool = new ForkJoinPool(2);
        BlockTask t = new BlockTask();
        try {
            ForkJoinTask<Integer> f = pool.submit(t);
            check(t.started.await(5, TimeUnit.SECONDS), "blocked task started");
            int ac = pool.getActiveThreadCount();
            check(ac >= 0 && ac <= 2, "getActiveThreadCount within [0, parallelism]");
            check(pool.getPoolSize() >= 1, "at least one worker while task is running");
            t.release.countDown();
            check(f.get() == 123, "blocked task returns its value");
            check(pool.awaitQuiescence(5, TimeUnit.SECONDS), "pool quiesces");
        } finally {
            t.release.countDown();
            pool.shutdownNow();
        }
    }

    // ---- custom worker factory ----
    static void testCustomFactory() throws Exception {
        final AtomicInteger created = new AtomicInteger();
        final java.util.concurrent.CopyOnWriteArrayList<Thread> made =
            new java.util.concurrent.CopyOnWriteArrayList<Thread>();
        Thread.UncaughtExceptionHandler ueh = new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread t, Throwable e) { }
        };
        ForkJoinPool.ForkJoinWorkerThreadFactory fac = new ForkJoinPool.ForkJoinWorkerThreadFactory() {
            public ForkJoinWorkerThread newThread(ForkJoinPool p) {
                ForkJoinWorkerThread wt = new ForkJoinWorkerThread(p) { };
                wt.setName("JckWorker-" + created.incrementAndGet());
                made.add(wt);
                return wt;
            }
        };
        ForkJoinPool pool = new ForkJoinPool(2, fac, ueh, false);
        try {
            check(pool.getFactory() == fac, "getFactory returns the custom factory");
            final ForkJoinPool seenPool = pool;
            String info = pool.invoke(new RecursiveTask<String>() {
                protected String compute() {
                    Thread t = Thread.currentThread();
                    ForkJoinWorkerThread w = (ForkJoinWorkerThread) t;
                    return t.getName() + "|" + t.isDaemon() + "|" + (w.getPool() == seenPool) + "|" + (w.getPoolIndex() >= 0);
                }
            });
            check(info.startsWith("JckWorker-"), "worker name carries factory prefix: " + info);
            check(info.contains("|true|"), "pool worker runs as daemon and getPool()==pool");
            check(info.endsWith("|true"), "getPoolIndex() >= 0 for a running worker");
            check(created.get() >= 1, "factory was invoked at least once");
            check(!made.isEmpty() && made.get(0).getName().startsWith("JckWorker-"),
                  "factory-recorded thread has the custom name");
            check(!made.isEmpty() && made.get(0).isDaemon(), "factory-created worker is daemon");
            check(!made.isEmpty() && made.get(0).getUncaughtExceptionHandler() == ueh,
                  "pool handler installed on factory thread");
        } finally { pool.shutdownNow(); }
    }

    // ---- shutdown lifecycle ----
    static void testShutdownLifecycle() throws Exception {
        ForkJoinPool pool = new ForkJoinPool(1);
        pool.shutdown();
        check(pool.isShutdown(), "isShutdown after shutdown()");
        try {
            pool.execute(new RecursiveAction() { protected void compute() { } });
            check(false, "execute after shutdown must throw REE");
        } catch (RejectedExecutionException e) { check(true, "execute after shutdown -> RejectedExecutionException"); }
        try {
            pool.submit(new RecursiveAction() { protected void compute() { } });
            check(false, "submit after shutdown must throw REE");
        } catch (RejectedExecutionException e) { check(true, "submit after shutdown -> RejectedExecutionException"); }
        check(pool.awaitTermination(5, TimeUnit.SECONDS), "idle pool terminates after shutdown()");
        check(pool.isTerminated(), "isTerminated after awaitTermination");
        check(pool.isShutdown(), "isShutdown stays true");
        String s = pool.toString();
        check(s.contains("Terminated") || s.contains("Shut"), "toString reflects termination state");
    }

    // ---- shutdownNow lifecycle (empty-list contract) ----
    static void testShutdownNowLifecycle() throws Exception {
        ForkJoinPool pool = new ForkJoinPool(1);
        BlockTask t1 = new BlockTask();
        BlockTask t2 = new BlockTask();
        BlockTask t3 = new BlockTask();
        pool.execute(t1);
        pool.execute(t2);
        pool.execute(t3);
        check(t1.started.await(5, TimeUnit.SECONDS), "first task started on the single worker");
        java.util.List<Runnable> rem = pool.shutdownNow();
        check(rem.isEmpty(), "ForkJoinPool.shutdownNow() always returns an empty list (documented)");
        check(pool.isShutdown(), "isShutdown after shutdownNow");
        check(pool.awaitTermination(5, TimeUnit.SECONDS), "pool terminates after shutdownNow");
        check(pool.isTerminated(), "isTerminated after shutdownNow");
        check(!t2.started.await(0, TimeUnit.SECONDS) && !t3.started.await(0, TimeUnit.SECONDS),
              "queued tasks never commenced after shutdownNow");
        t1.release.countDown();
        t2.release.countDown();
        t3.release.countDown();
    }

    // ---- JDK-27-specific APIs ----
    static void testJdk27Specifics() throws Exception {
        // setParallelism / externalSubmit / schedule / invokeAny / invokeAllUninterruptibly / submitWithTimeout
        ForkJoinPool sp = new ForkJoinPool(4);
        try {
            check(sp.setParallelism(2) == 4, "setParallelism returns the previous value (4)");
            check(sp.getParallelism() == 2, "setParallelism applied");
            try { sp.setParallelism(0); check(false, "setParallelism(0) must throw IAE"); }
            catch (IllegalArgumentException e) { check(true, "setParallelism(0) -> IllegalArgumentException"); }
            try { sp.setParallelism(-1); check(false, "setParallelism(-1) must throw IAE"); }
            catch (IllegalArgumentException e) { check(true, "setParallelism(-1) -> IllegalArgumentException"); }

            RecursiveTask<Integer> ex1 = new RecursiveTask<Integer>() { protected Integer compute() { return 7; } };
            check(sp.externalSubmit(ex1).get(5, TimeUnit.SECONDS) == 7, "externalSubmit executes the task");

            java.util.concurrent.ScheduledFuture<String> sf =
                sp.schedule(new Callable<String>() { public String call() { return "sched"; } }, 10, TimeUnit.MILLISECONDS);
            check("sched".equals(sf.get(5, TimeUnit.SECONDS)), "schedule(Callable) returns its value");

            java.util.List<Callable<Integer>> cs = new java.util.ArrayList<Callable<Integer>>();
            cs.add(new Callable<Integer>() { public Integer call() { return 42; } });
            cs.add(new Callable<Integer>() { public Integer call() { return 42; } });
            check(sp.invokeAny(cs) == 42, "invokeAny returns a completed result");
            java.util.List<java.util.concurrent.Future<Integer>> all = sp.invokeAllUninterruptibly(cs);
            check(all.size() == 2 && all.get(0).get() == 42 && all.get(1).get() == 42,
                  "invokeAllUninterruptibly completes all callables");

            // submitWithTimeout: fast callable succeeds, blocked callable is cancelled
            check(sp.submitWithTimeout(new Callable<Integer>() {
                public Integer call() { return 5; }
            }, 5, TimeUnit.SECONDS, null).get() == 5, "submitWithTimeout within deadline returns value");
            final CountDownLatch never = new CountDownLatch(1);
            ForkJoinTask<Integer> tooLate = sp.submitWithTimeout(new Callable<Integer>() {
                public Integer call() {
                    try { never.await(5, TimeUnit.SECONDS); } catch (InterruptedException e) { }
                    return 1;
                }
            }, 100, TimeUnit.MILLISECONDS, null);
            try {
                tooLate.get(5, TimeUnit.SECONDS);
                check(false, "timed-out submitWithTimeout task must not return a value");
            } catch (java.util.concurrent.CancellationException e) {
                check(true, "submitWithTimeout null action cancels the task on timeout");
            } finally { never.countDown(); }
        } finally { sp.shutdownNow(); }

        // lazySubmit: prompt processing is NOT guaranteed, so drive it via awaitQuiescence
        // (never join a lazy submission from outside - it is not externally helpable)
        ForkJoinPool lp = new ForkJoinPool(2);
        try {
            RecursiveTask<Integer> lz = new RecursiveTask<Integer>() { protected Integer compute() { return 42; } };
            lp.lazySubmit(lz);
            check(lp.awaitQuiescence(5, TimeUnit.SECONDS), "pool quiesces after lazySubmit");
            check(lz.isDone() && lz.getRawResult() == 42, "lazySubmit task executed with its value");
        } finally { lp.shutdownNow(); }

        // close() implements AutoCloseable: shutdown + await termination
        try (ForkJoinPool twr = new ForkJoinPool(1)) {
            check(twr.invoke(new RecursiveTask<Integer>() { protected Integer compute() { return 10; } }) == 10,
                  "try-with-resources pool usable before close");
        }
        ForkJoinPool closed = new ForkJoinPool(1);
        closed.close();
        check(closed.isShutdown() && closed.isTerminated(), "close() shuts down and awaits termination");

        // getDelayedTaskCount on a pool with a pending far-future schedule
        ForkJoinPool dp = new ForkJoinPool(1);
        try {
            dp.schedule(new Runnable() { public void run() { } }, 1, TimeUnit.HOURS);
            // delayed tasks may be counted lazily, so only non-negativity is guaranteed
            check(dp.getDelayedTaskCount() >= 0, "getDelayedTaskCount non-negative with a pending schedule");
            dp.cancelDelayedTasksOnShutdown();
            check(true, "cancelDelayedTasksOnShutdown callable");
        } finally { dp.shutdownNow(); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstructorMatrix();
        testFreshPoolState();
        testCommonPool();
        testDivideAndConquerSum();
        testSubmitVariants();
        testExceptionFromTask();
        testActiveCountBounded();
        testCustomFactory();
        testShutdownLifecycle();
        testShutdownNowLifecycle();
        testJdk27Specifics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
