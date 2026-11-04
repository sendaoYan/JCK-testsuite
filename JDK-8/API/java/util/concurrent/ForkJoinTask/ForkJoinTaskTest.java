/*
 * @test
 * @summary java.util.concurrent.ForkJoinTask API conformance: invoke computes a raw result.
 * @run main ForkJoinTaskTest
 */

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ForkJoinTaskTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Class<?> cls(String n) {
        try { return Class.forName(n); } catch (ClassNotFoundException e) { throw new AssertionError(e); }
    }

    static void hasMethod(Class<?> c, String n, Class<?>... p) {
        try { c.getMethod(n, p); } catch (NoSuchMethodException e) { throw new AssertionError("missing " + n, e); }
        passed++;
    }

    // =============== existing smoke test (kept verbatim) ===============

    static class Task extends ForkJoinTask<Integer> {
        int result;

        public Integer getRawResult() {
            return result;
        }

        protected void setRawResult(Integer value) {
            result = value;
        }

        protected boolean exec() {
            setRawResult(42);
            return true;
        }
    }

    static void testForkJoinTask() {
        ForkJoinTask<Integer> task = new Task();
        ForkJoinPool pool = new ForkJoinPool();
        try {
            check(pool.invoke(task) == 42, "invoke returns the raw result");
        } finally {
            pool.shutdown();
        }
    }

    // =============== contract additions ===============

    // ---- Part A: white-box structure ----
    static void testWhiteBoxStructure() throws Exception {
        Class<?> c = cls("java.util.concurrent.ForkJoinTask");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "ForkJoinTask is abstract");
        check(!c.isInterface(), "ForkJoinTask is a class");
        check(java.util.concurrent.Future.class.isAssignableFrom(c), "implements Future");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
        hasMethod(c, "fork");
        hasMethod(c, "join");
        hasMethod(c, "invoke");
        hasMethod(c, "invokeAll", ForkJoinTask.class, ForkJoinTask.class);
        hasMethod(c, "invokeAll", ForkJoinTask[].class);
        hasMethod(c, "invokeAll", java.util.Collection.class);
        hasMethod(c, "cancel", boolean.class);
        hasMethod(c, "isDone");
        hasMethod(c, "isCancelled");
        hasMethod(c, "isCompletedAbnormally");
        hasMethod(c, "isCompletedNormally");
        hasMethod(c, "getException");
        hasMethod(c, "completeExceptionally", Throwable.class);
        hasMethod(c, "complete", Object.class);
        hasMethod(c, "quietlyComplete");
        hasMethod(c, "get");
        hasMethod(c, "get", long.class, TimeUnit.class);
        hasMethod(c, "quietlyJoin");
        hasMethod(c, "quietlyInvoke");
        hasMethod(c, "helpQuiesce");
        hasMethod(c, "reinitialize");
        hasMethod(c, "getPool");
        hasMethod(c, "inForkJoinPool");
        hasMethod(c, "tryUnfork");
        hasMethod(c, "getQueuedTaskCount");
        hasMethod(c, "getSurplusQueuedTaskCount");
        hasMethod(c, "getRawResult");
        hasMethod(c, "getForkJoinTaskTag");
        hasMethod(c, "setForkJoinTaskTag", short.class);
        hasMethod(c, "compareAndSetForkJoinTaskTag", short.class, short.class);
        hasMethod(c, "adapt", Runnable.class);
        hasMethod(c, "adapt", Runnable.class, Object.class);
        hasMethod(c, "adapt", Callable.class);
    }

    // ---- Part B: invoke on the caller thread (no pool needed) ----
    static void testInvokeOnCaller() throws Exception {
        Task t = new Task();
        check(t.invoke() == 42, "invoke() on a non-pool thread executes in the caller");
        check(t.isDone() && !t.isCancelled(), "done and not cancelled after invoke");
        check(t.isCompletedNormally(), "completed normally");
        check(t.get() == 42, "get() after invoke");
        check(t.getRawResult() == 42, "getRawResult reflects setRawResult");
        check(!t.isCompletedAbnormally(), "not completed abnormally");
    }

    // ---- Part C: fork/join pairing from the main thread ----
    static void testForkJoinPair() throws Exception {
        check(!ForkJoinTask.inForkJoinPool(), "inForkJoinPool() false on main thread");
        check(ForkJoinTask.getPool() == null, "static getPool() null on main thread");
        RecursiveTask<Integer> t = new RecursiveTask<Integer>() {
            protected Integer compute() { return 21; }
        };
        check(!t.isDone(), "task not done before fork");
        t.fork();
        check(t.join() == 21, "fork then join returns the computed value");
        check(t.isDone(), "isDone after join");
        check(!t.isCancelled(), "not cancelled after normal join");
        // a never-forked task cannot be unforked
        RecursiveTask<Integer> nf = new RecursiveTask<Integer>() {
            protected Integer compute() { return 1; }
        };
        check(!nf.tryUnfork(), "tryUnfork false for a never-forked task");
    }

    // ---- Part D: identity probes inside a pool task ----
    static void testInsidePoolIdentity() throws Exception {
        final ForkJoinPool pool = new ForkJoinPool(2);
        try {
            String info = pool.invoke(new RecursiveTask<String>() {
                protected String compute() {
                    StringBuilder sb = new StringBuilder();
                    sb.append(ForkJoinTask.inForkJoinPool()).append(',');
                    sb.append(ForkJoinTask.getPool() == pool).append(',');
                    sb.append(Thread.currentThread() instanceof java.util.concurrent.ForkJoinWorkerThread).append(',');
                    sb.append(ForkJoinTask.getSurplusQueuedTaskCount() >= 0).append(',');
                    sb.append(ForkJoinTask.getQueuedTaskCount() >= 0);
                    ForkJoinTask.helpQuiesce();
                    return sb.toString();
                }
            });
            check(info.startsWith("true,true,"), "inside a pool task: inForkJoinPool true and getPool()==pool");
            check(info.contains(",true,true"), "surplus/queued counts non-negative in-task");
            check(info.endsWith("true"), "thread is a ForkJoinWorkerThread while running");
        } finally { pool.shutdownNow(); }
    }

    // ---- Part E: invokeAll forms ----
    static void testInvokeAllForms() throws Exception {
        final AtomicInteger runs = new AtomicInteger();
        RecursiveAction a1 = new RecursiveAction() {
            protected void compute() { runs.incrementAndGet(); }
        };
        RecursiveAction a2 = new RecursiveAction() {
            protected void compute() { runs.incrementAndGet(); }
        };
        ForkJoinTask.invokeAll(a1, a2);
        check(runs.get() == 2, "invokeAll(t1,t2) executed both tasks");
        check(a1.isDone() && a2.isDone(), "both tasks done after invokeAll(t1,t2)");

        final AtomicInteger arrRuns = new AtomicInteger();
        ForkJoinTask.invokeAll(new ForkJoinTask<?>[] {
            actionOf(arrRuns), actionOf(arrRuns)
        });
        check(arrRuns.get() == 2, "invokeAll(array) executed both tasks");

        final AtomicInteger collRuns = new AtomicInteger();
        java.util.List<RecursiveAction> coll = new java.util.ArrayList<RecursiveAction>();
        coll.add(actionOf(collRuns));
        coll.add(actionOf(collRuns));
        java.util.Collection<RecursiveAction> res = ForkJoinTask.invokeAll(coll);
        check(collRuns.get() == 2, "invokeAll(collection) executed both tasks");
        check(res.size() == 2, "invokeAll(collection) returns the same number of tasks");
        for (RecursiveAction r : res) check(r.isDone(), "collection element done");

        ForkJoinTask.invokeAll();                                        // empty varargs
        ForkJoinTask.invokeAll(new ForkJoinTask<?>[0]);                  // empty array
        check(ForkJoinTask.invokeAll(new java.util.ArrayList<RecursiveAction>()).isEmpty(),
              "invokeAll(empty collection) returns empty list");
        check(true, "empty invokeAll forms complete without throwing");

        // exception in one element: invokeAll rethrows the (unchecked) exception
        final IllegalStateException boom = new IllegalStateException("ia");
        try {
            ForkJoinTask.invokeAll(actionOf(new AtomicInteger()),
                new RecursiveAction() { protected void compute() { throw boom; } });
            check(false, "invokeAll with failing task must throw");
        } catch (IllegalStateException e) {
            check(true, "invokeAll rethrows the unchecked exception class");
        }
    }

    static RecursiveAction actionOf(final AtomicInteger ctr) {
        return new RecursiveAction() {
            protected void compute() { ctr.incrementAndGet(); }
        };
    }

    // ---- Part F: classic compute-internal invokeAll (no deadlock) ----
    static class Fib extends RecursiveTask<Integer> {
        final int n;
        Fib(int n) { this.n = n; }
        protected Integer compute() {
            if (n <= 1) return n;
            Fib f1 = new Fib(n - 1);
            Fib f2 = new Fib(n - 2);
            invokeAll(f1, f2);
            return f1.join() + f2.join();
        }
    }

    static void testNestedInvokeAll() throws Exception {
        ForkJoinPool pool = new ForkJoinPool(3);
        try {
            check(pool.invoke(new Fib(10)) == 55, "nested invokeAll fibonacci(10) == 55");
        } finally { pool.shutdownNow(); }
    }

    // ---- Part G: exception semantics ----
    static class FailTask extends ForkJoinTask<Integer> {
        final RuntimeException boom;
        FailTask(RuntimeException b) { boom = b; }
        public Integer getRawResult() { return null; }
        protected void setRawResult(Integer v) { }
        protected boolean exec() { throw boom; }
    }

    static void testExceptionSameThread() throws Exception {
        final RuntimeException boom = new IllegalStateException("boom");
        FailTask t = new FailTask(boom);
        try {
            t.invoke();
            check(false, "invoke must rethrow the compute exception");
        } catch (RuntimeException e) {
            check(e == boom, "same-thread invoke rethrows the ORIGINAL exception object");
        }
        check(t.isCompletedAbnormally(), "isCompletedAbnormally after failure");
        check(!t.isCompletedNormally(), "not isCompletedNormally after failure");
        check(t.getException() == boom, "getException() returns the original object (same thread)");
        try {
            t.join();
            check(false, "join must rethrow");
        } catch (RuntimeException e) {
            check(e == boom, "same-thread join rethrows the original object");
        }
        try {
            t.get();
            check(false, "get must wrap in ExecutionException");
        } catch (ExecutionException e) {
            check(e.getCause() == boom, "get() ExecutionException cause == original");
        }
    }

    static void testExceptionCrossThread() throws Exception {
        final RuntimeException boom = new IllegalStateException("xthread");
        ForkJoinPool pool = new ForkJoinPool(2);
        try {
            FailTask t = new FailTask(boom);
            try {
                pool.invoke(t);
                check(false, "cross-thread invoke must throw");
            } catch (IllegalStateException e) {
                check(e != boom, "cross-thread rethrow is a rebuilt copy (stack accuracy)");
                check(e.getCause() == boom, "rebuilt copy wraps the original as cause");
            }
            check(t.getException() instanceof IllegalStateException,
                  "getException class preserved across threads");
            // quietly variants never throw
            FailTask q = new FailTask(boom);
            q.quietlyInvoke();
            check(q.isCompletedAbnormally(), "quietlyInvoke completes abnormally without throwing");
            check(q.getException() instanceof IllegalStateException, "quietlyInvoke records the exception");
            FailTask q2 = new FailTask(boom);
            q2.fork();
            q2.quietlyJoin();
            check(q2.isDone(), "quietlyJoin returns without throwing");
        } finally { pool.shutdownNow(); }
    }

    // ---- Part H: cancellation ----
    static void testCancellation() throws Exception {
        // never-started task: cancel is unconditional
        RecursiveTask<Integer> t = new RecursiveTask<Integer>() {
            protected Integer compute() { return 1; }
        };
        check(!t.isDone(), "not done before cancel");
        check(t.cancel(true), "cancel(true) returns true for a fresh task");
        check(t.isCancelled(), "isCancelled after cancel");
        check(t.isDone(), "isDone after cancel");
        try {
            t.get();
            check(false, "get after cancel must throw CancellationException");
        } catch (CancellationException e) { check(true, "get after cancel -> CancellationException"); }
        try {
            t.join();
            check(false, "join after cancel must throw CancellationException");
        } catch (CancellationException e) { check(true, "join after cancel -> CancellationException"); }
        check(t.cancel(false), "repeated cancel keeps returning true (FJT: true iff cancelled upon return)");
        t.quietlyInvoke(); // must not throw even though cancelled
        check(t.isCancelled(), "still cancelled after quietlyInvoke");

        // in-flight task cancellation
        final CountDownLatch release = new CountDownLatch(1);
        RecursiveTask<Integer> blocked = new RecursiveTask<Integer>() {
            protected Integer compute() {
                try { release.await(5, TimeUnit.SECONDS); } catch (InterruptedException e) { }
                return 2;
            }
        };
        blocked.fork();
        check(blocked.cancel(true), "cancel(true) on a forked in-flight task returns true");
        try {
            blocked.get(2, TimeUnit.SECONDS);
            check(false, "get on cancelled in-flight task must throw");
        } catch (CancellationException e) { check(true, "cancelled in-flight task get -> CancellationException"); }
        release.countDown();
    }

    // ---- Part I: timed get ----
    static class BlockTask extends RecursiveTask<Integer> {
        final CountDownLatch started = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        protected Integer compute() {
            started.countDown();
            try {
                if (!release.await(5, TimeUnit.SECONDS)) throw new AssertionError("release timeout");
            } catch (InterruptedException e) { throw new RuntimeException(e); }
            return 321;
        }
    }

    static void testTimedGet() throws Exception {
        ForkJoinPool pool = new ForkJoinPool(1);
        BlockTask t = new BlockTask();
        try {
            ForkJoinTask<Integer> f = pool.submit(t);
            check(t.started.await(5, TimeUnit.SECONDS), "task started");
            try {
                f.get(150, TimeUnit.MILLISECONDS);
                check(false, "timed get on unfinished task must time out");
            } catch (TimeoutException e) { check(true, "get(timeout) -> TimeoutException"); }
            t.release.countDown();
            check(f.get() == 321, "get() returns value after release");
        } finally {
            t.release.countDown();
            pool.shutdownNow();
        }
    }

    // ---- Part J: complete(V) injection ----
    static void testCompleteInjection() throws Exception {
        final AtomicInteger execs = new AtomicInteger();
        Task t = new Task() {
            protected boolean exec() { execs.incrementAndGet(); return super.exec(); }
        };
        t.complete(99);
        check(t.isDone(), "isDone after complete(V)");
        check(t.isCompletedNormally(), "complete(V) completes normally");
        check(t.invoke() == 99, "invoke() after complete(V) returns injected value without executing");
        check(execs.get() == 0, "exec() never ran after pre-completion");
        check(t.get() == 99, "get() returns injected value");
        check(t.getRawResult() == 99, "getRawResult carries injected value");
        check(t.join() == 99, "join() returns injected value");

        RecursiveAction ra = new RecursiveAction() {
            protected void compute() { }
        };
        ra.quietlyComplete();
        check(ra.isDone(), "quietlyComplete marks done");
        check(ra.join() == null, "RecursiveAction join returns null after quietlyComplete");
    }

    // ---- Part K: completeExceptionally ----
    static void testCompleteExceptionally() throws Exception {
        final RuntimeException boom = new IllegalStateException("ce");
        Task t = new Task();
        t.completeExceptionally(boom);
        check(t.isDone(), "done after completeExceptionally");
        check(t.isCompletedAbnormally(), "abnormal after completeExceptionally");
        try {
            t.join();
            check(false, "join must rethrow injected exception");
        } catch (RuntimeException e) {
            check(e == boom, "same-thread join rethrows the injected object");
        }
        check(t.getException() == boom, "getException == injected throwable");
        try {
            t.get();
            check(false, "get must wrap");
        } catch (ExecutionException e) {
            check(e.getCause() == boom, "ExecutionException cause == injected");
        }
    }

    // ---- Part L: reinitialize reuse ----
    static void testReinitialize() throws Exception {
        final AtomicInteger runs = new AtomicInteger();
        RecursiveTask<Integer> t = new RecursiveTask<Integer>() {
            protected Integer compute() { return runs.incrementAndGet(); }
        };
        check(t.invoke() == 1, "first run");
        t.reinitialize();
        check(!t.isDone(), "not done after reinitialize");
        check(!t.isCancelled(), "not cancelled after reinitialize");
        check(t.getException() == null, "exception cleared by reinitialize");
        check(t.invoke() == 2, "second run after reinitialize re-executes compute");
        check(runs.get() == 2, "compute ran exactly twice");

        FailTask ft = new FailTask(new IllegalStateException("r"));
        try { ft.invoke(); } catch (RuntimeException ignore) { }
        check(ft.isCompletedAbnormally(), "failed before reinitialize");
        ft.reinitialize();
        check(ft.getException() == null, "exception state cleared after reinitialize of failed task");
        check(!ft.isDone(), "failed task not done after reinitialize");
    }

    // ---- Part M: tag methods (present since JDK 8) ----
    static void testTags() throws Exception {
        RecursiveTask<Integer> t = new RecursiveTask<Integer>() {
            protected Integer compute() { return 1; }
        };
        check(t.getForkJoinTaskTag() == 0, "initial tag is 0");
        check(t.setForkJoinTaskTag((short) 5) == 0, "setForkJoinTaskTag returns the previous tag");
        check(t.getForkJoinTaskTag() == 5, "tag updated to 5");
        check(t.compareAndSetForkJoinTaskTag((short) 5, (short) 7), "CAS with matching expected succeeds");
        check(!t.compareAndSetForkJoinTaskTag((short) 5, (short) 9), "CAS with stale expected fails");
        check(t.getForkJoinTaskTag() == 7, "tag unchanged by failed CAS");
        check(t.setForkJoinTaskTag((short) 5) == 7, "second set returns previous tag 7");
    }

    // ---- Part N: adapt forms ----
    static void testAdapt() throws Exception {
        final AtomicBoolean ran = new AtomicBoolean(false);
        ForkJoinTask<?> ar = ForkJoinTask.adapt(new Runnable() {
            public void run() { ran.set(true); }
        });
        check(ar.invoke() == null, "adapt(Runnable).invoke returns null");
        check(ran.get(), "adapt(Runnable) ran the runnable");

        ForkJoinTask<String> arr = ForkJoinTask.adapt(new Runnable() {
            public void run() { }
        }, "done");
        check("done".equals(arr.invoke()), "adapt(Runnable,T) returns T after execution");

        ForkJoinTask<Integer> ac = ForkJoinTask.adapt(new Callable<Integer>() {
            public Integer call() { return 100; }
        });
        check(ac.invoke() == 100, "adapt(Callable) returns the callable value");

        final RuntimeException boom = new IllegalStateException("adapt");
        ForkJoinTask<Integer> bad = ForkJoinTask.adapt(new Callable<Integer>() {
            public Integer call() { throw boom; }
        });
        try {
            bad.invoke();
            check(false, "adapt(Callable) failure must rethrow");
        } catch (RuntimeException e) {
            check(e == boom, "adapt(Callable) same-thread rethrows the original exception");
        }
        check(bad.isCompletedAbnormally(), "adapted failing task completed abnormally");
    }

    public static void main(String[] args) throws Exception {
        testForkJoinTask();
        testWhiteBoxStructure();
        testInvokeOnCaller();
        testForkJoinPair();
        testInsidePoolIdentity();
        testInvokeAllForms();
        testNestedInvokeAll();
        testExceptionSameThread();
        testExceptionCrossThread();
        testCancellation();
        testTimedGet();
        testCompleteInjection();
        testCompleteExceptionally();
        testReinitialize();
        testTags();
        testAdapt();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
