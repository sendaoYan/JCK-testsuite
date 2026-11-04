/*
 * @test
 * @summary java.util.concurrent.ForkJoinPool API conformance: invoke of a RecursiveTask
 *          sum.
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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

    // =============== existing smoke test (kept verbatim) ===============

    static class Sum extends RecursiveTask<Integer> {
        private final int n;
        Sum(int n) { this.n = n; }
        protected Integer compute() {
            if (n <= 1) return n;
            Sum s = new Sum(n - 1);
            s.fork();
            return n + s.join();
        }
    }

    static void testForkJoinPool() {
        ForkJoinPool pool = new ForkJoinPool();
        try {
            check(pool.invoke(new Sum(10)) == 55, "fork/join sum 1..10");
        } finally {
            pool.shutdown();
        }
    }

    // =============== contract additions ===============

    // ---- Part A: white-box (structural) ----
    static void testWhiteBoxStructure() throws Exception {
        Class<?> c = cls("java.util.concurrent.ForkJoinPool");
        check(!c.isInterface(), "ForkJoinPool is a class");
        check(c.getSuperclass() == cls("java.util.concurrent.AbstractExecutorService"),
              "extends AbstractExecutorService");
        check(!java.util.concurrent.ScheduledExecutorService.class.isAssignableFrom(c),
              "JDK-8 ForkJoinPool does NOT implement ScheduledExecutorService");
        check(cls("java.util.concurrent.Executor").isAssignableFrom(c), "implements Executor");
        check(c.getField("defaultForkJoinWorkerThreadFactory").get(null) != null,
              "static defaultForkJoinWorkerThreadFactory is non-null");
        hasMethod(c, "commonPool");
        hasMethod(c, "invoke", ForkJoinTask.class);
        hasMethod(c, "execute", ForkJoinTask.class);
        hasMethod(c, "execute", Runnable.class);
        hasMethod(c, "submit", ForkJoinTask.class);
        hasMethod(c, "submit", Callable.class);
        hasMethod(c, "submit", Runnable.class, Object.class);
        hasMethod(c, "submit", Runnable.class);
        hasMethod(c, "invokeAll", java.util.Collection.class);
        hasMethod(c, "getFactory");
        hasMethod(c, "getUncaughtExceptionHandler");
        hasMethod(c, "getParallelism");
        hasMethod(c, "getCommonPoolParallelism");
        hasMethod(c, "getPoolSize");
        hasMethod(c, "getAsyncMode");
        hasMethod(c, "getRunningThreadCount");
        hasMethod(c, "getActiveThreadCount");
        hasMethod(c, "isQuiescent");
        hasMethod(c, "getStealCount");
        hasMethod(c, "getQueuedTaskCount");
        hasMethod(c, "getQueuedSubmissionCount");
        hasMethod(c, "hasQueuedSubmissions");
        hasMethod(c, "shutdown");
        hasMethod(c, "shutdownNow");
        hasMethod(c, "isTerminated");
        hasMethod(c, "isTerminating");
        hasMethod(c, "isShutdown");
        hasMethod(c, "awaitTermination", long.class, TimeUnit.class);
        hasMethod(c, "awaitQuiescence", long.class, TimeUnit.class);
        hasMethod(c, "managedBlock", cls("java.util.concurrent.ForkJoinPool$ManagedBlocker"));
        hasMethod(c, "toString");
    }

    // ---- Part B: constructor matrix ----
    static void testConstructorMatrix() {
        try { new ForkJoinPool(0); check(false, "FJP(0) must throw IAE"); }
        catch (IllegalArgumentException e) { check(true, "FJP(0) -> IllegalArgumentException"); }
        try { new ForkJoinPool(-3); check(false, "FJP(-3) must throw IAE"); }
        catch (IllegalArgumentException e) { check(true, "FJP(-3) -> IllegalArgumentException"); }
        ForkJoinPool p1 = new ForkJoinPool(1);
        try { check(p1.getParallelism() == 1, "FJP(1).getParallelism == 1"); }
        finally { p1.shutdownNow(); }
        ForkJoinPool p2 = new ForkJoinPool(2);
        try { check(p2.getParallelism() == 2, "FJP(2).getParallelism == 2"); }
        finally { p2.shutdownNow(); }
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
            check(async.getParallelism() == 2, "4-arg ctor parallelism");
            check(async.getFactory() == ForkJoinPool.defaultForkJoinWorkerThreadFactory, "4-arg ctor factory");
            check(async.getUncaughtExceptionHandler() == ueh, "4-arg ctor handler returned");
        } finally { async.shutdownNow(); }
        ForkJoinPool sync = new ForkJoinPool(1, ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, false);
        try { check(!sync.getAsyncMode(), "4-arg ctor asyncMode false reflected"); }
        finally { sync.shutdownNow(); }
    }

    // ---- Part C: fresh pool observable state ----
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
            check(!pool.isShutdown(), "fresh pool not shutdown");
            check(!pool.isTerminated(), "fresh pool not terminated");
            check(!pool.isTerminating(), "fresh pool not terminating");
            String s = pool.toString();
            check(s != null && s.contains("ForkJoinPool"), "toString contains class name");
            check(s.contains("Running"), "fresh pool toString contains Running");
        } finally { pool.shutdownNow(); }
    }

    // ---- Part D: common pool ----
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

    // ---- Part E: classic divide-and-conquer sum 1..100 = 5050 ----
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
            invokeAll(left, right);   // classic: both children complete before return
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

    // ---- Part F: submit/execute variants ----
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

            final AtomicBoolean ranX = new AtomicBoolean(false);
            pool.execute(new Runnable() { public void run() { ranX.set(true); } });
            check(pool.awaitQuiescence(5, TimeUnit.SECONDS), "pool quiesces after execute(Runnable)");
            check(ranX.get(), "execute(Runnable) ran the runnable");

            check(pool.invoke(new RecursiveTask<Integer>() {
                protected Integer compute() { return 7; }
            }) == 7, "invoke returns the computed value");

            // null arguments
            try { pool.invoke(null); check(false, "invoke(null) must throw NPE"); }
            catch (NullPointerException e2) { check(true, "invoke(null) -> NullPointerException"); }
            try { pool.execute((ForkJoinTask<?>) null); check(false, "execute(null FJT) must throw NPE"); }
            catch (NullPointerException e2) { check(true, "execute(null FJT) -> NullPointerException"); }
            try { pool.submit((ForkJoinTask<?>) null); check(false, "submit(null FJT) must throw NPE"); }
            catch (NullPointerException e2) { check(true, "submit(null FJT) -> NullPointerException"); }
        } finally { pool.shutdownNow(); }
    }

    // ---- Part G: exception relay from worker ----
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

    // ---- Part H: bounded active-thread invariants ----
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
            check(pool.getActiveThreadCount() == 0, "active count back to 0 when idle");
        } finally {
            t.release.countDown();
            pool.shutdownNow();
        }
    }

    // ---- Part I: custom worker factory ----
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

    // ---- Part J: shutdown lifecycle ----
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

    // ---- Part K: shutdownNow lifecycle (empty-list contract) ----
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

    public static void main(String[] args) throws Exception {
        testForkJoinPool();
        testWhiteBoxStructure();
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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
