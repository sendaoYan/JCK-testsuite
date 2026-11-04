/*
 * @test
 * @summary java.util.concurrent.ForkJoinTask conformance: white-box structure + black-box
 *          behavior (invoke/fork/join/adapt/complete/completeExceptionally/getException),
 *          plus JDK-19+ state()/resultNow()/exceptionNow()/timed quietlyJoin/adaptInterruptible.
 * @run main ForkJoinTaskTest
 */

import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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
        try { c.getMethod(n, p); } catch (NoSuchMethodException e) { throw new AssertionError("missing " + n + " on " + c.getName(), e); }
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = cls("java.util.concurrent.ForkJoinTask");
        check(c.isInterface() == false && java.lang.reflect.Modifier.isAbstract(c.getModifiers()),
              "ForkJoinTask is an abstract class");
        check(Future.class.isAssignableFrom(c), "implements Future");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
        hasMethod(c, "fork");
        hasMethod(c, "join");
        hasMethod(c, "invoke");
        hasMethod(c, "cancel", boolean.class);
        hasMethod(c, "get");
        hasMethod(c, "complete", Object.class);
        hasMethod(c, "completeExceptionally", Throwable.class);
        hasMethod(c, "getException");
        hasMethod(c, "isCompletedAbnormally");
        hasMethod(c, "adapt", Callable.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static class Task extends ForkJoinTask<Integer> {
        int result;
        public Integer getRawResult() { return result; }
        protected void setRawResult(Integer value) { result = value; }
        protected boolean exec() { setRawResult(42); return true; }
    }

    static class FibTask extends ForkJoinTask<Integer> {
        final int n;
        int result;
        FibTask(int n) { this.n = n; }
        public Integer getRawResult() { return result; }
        protected void setRawResult(Integer v) { result = v; }
        protected boolean exec() {
            if (n <= 1) { result = n; return true; }
            FibTask a = new FibTask(n - 1);
            FibTask b = new FibTask(n - 2);
            a.fork();
            b.fork();
            result = a.join() + b.join();
            return true;
        }
    }

    static void testBlackBox() throws Exception {
        ForkJoinPool pool = new ForkJoinPool();
        try {
            // invoke computes the raw result
            Task t = new Task();
            check(pool.invoke(t) == 42, "invoke returns the raw result");
            check(t.isDone() && !t.isCancelled(), "task done after invoke");
            check(t.get() == 42, "get after invoke");

            // fork/join recursion
            FibTask fib = new FibTask(10);
            check(pool.invoke(fib) == 55, "fork/join fibonacci(10) == 55");

            // adapt from Runnable/result and Callable
            ForkJoinTask<String> ar = ForkJoinTask.adapt(() -> { }, "done");
            check("done".equals(pool.invoke(ar)), "adapt(Runnable, result)");
            ForkJoinTask<Integer> ac = ForkJoinTask.adapt((Callable<Integer>) () -> 100);
            check(pool.invoke(ac) == 100, "adapt(Callable)");

            // complete(V) and completeExceptionally(Throwable)
            Task c = new Task();
            c.complete(7);
            check(c.isDone() && c.get() == 7, "complete(V)");
            Task e = new Task();
            e.completeExceptionally(new IllegalStateException("x"));
            check(e.isCompletedAbnormally(), "isCompletedAbnormally after completeExceptionally");
            check(e.getException() instanceof IllegalStateException, "getException returns the cause");
        } finally {
            pool.shutdown();
        }
        check(pool.awaitTermination(10, TimeUnit.SECONDS), "pool terminates");
    }

    // =============== contract additions (shared with JDK-8 variant) ===============

    // ---- Part A: full white-box surface ----
    static void testWhiteBoxStructure() throws Exception {
        Class<?> c = cls("java.util.concurrent.ForkJoinTask");
        hasMethod(c, "invokeAll", ForkJoinTask.class, ForkJoinTask.class);
        hasMethod(c, "invokeAll", ForkJoinTask[].class);
        hasMethod(c, "invokeAll", java.util.Collection.class);
        hasMethod(c, "isDone");
        hasMethod(c, "isCancelled");
        hasMethod(c, "isCompletedNormally");
        hasMethod(c, "quietlyComplete");
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
        // JDK-19+ additions
        hasMethod(c, "state");
        hasMethod(c, "resultNow");
        hasMethod(c, "exceptionNow");
        hasMethod(c, "quietlyJoin", long.class, TimeUnit.class);
        hasMethod(c, "quietlyJoinUninterruptibly", long.class, TimeUnit.class);
        // JDK-20+ additions
        hasMethod(c, "adaptInterruptible", Callable.class);
        hasMethod(c, "adaptInterruptible", Runnable.class, Object.class);
        hasMethod(c, "adaptInterruptible", Runnable.class);
    }

    // ---- Part B: invoke on the caller thread ----
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

        try {
            ForkJoinTask.invokeAll(actionOf(new AtomicInteger()),
                new RecursiveAction() { protected void compute() { throw new IllegalStateException("ia"); } });
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

    // ---- Part M: tag methods ----
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

    // =============== JDK-19/20+ specific contracts ===============

    // ---- Part O: state()/resultNow()/exceptionNow() state machine ----
    static void testStateMatrix() throws Exception {
        // fresh task: RUNNING
        RecursiveTask<Integer> fresh = new RecursiveTask<Integer>() {
            protected Integer compute() { return 1; }
        };
        check(fresh.state() == Future.State.RUNNING, "fresh task state == RUNNING");
        try { fresh.resultNow(); check(false, "resultNow on RUNNING must throw ISE"); }
        catch (IllegalStateException e) { check(true, "resultNow on RUNNING -> IllegalStateException"); }
        try { fresh.exceptionNow(); check(false, "exceptionNow on RUNNING must throw ISE"); }
        catch (IllegalStateException e) { check(true, "exceptionNow on RUNNING -> IllegalStateException"); }

        // success: SUCCESS + resultNow returns value
        RecursiveTask<Integer> ok = new RecursiveTask<Integer>() {
            protected Integer compute() { return 42; }
        };
        ok.invoke();
        check(ok.state() == Future.State.SUCCESS, "completed task state == SUCCESS");
        check(ok.resultNow() == 42, "resultNow returns the computed value");
        try { ok.exceptionNow(); check(false, "exceptionNow on SUCCESS must throw ISE"); }
        catch (IllegalStateException e) { check(true, "exceptionNow on SUCCESS -> IllegalStateException"); }

        // failure: FAILED + exceptionNow returns the exception
        final RuntimeException boom = new IllegalStateException("st");
        RecursiveTask<Integer> bad = new RecursiveTask<Integer>() {
            protected Integer compute() { throw boom; }
        };
        try { bad.invoke(); } catch (RuntimeException ignore) { }
        check(bad.state() == Future.State.FAILED, "failed task state == FAILED");
        check(bad.exceptionNow() == boom, "exceptionNow returns the ORIGINAL exception (same thread)");
        try { bad.resultNow(); check(false, "resultNow on FAILED must throw ISE"); }
        catch (IllegalStateException e) { check(true, "resultNow on FAILED -> IllegalStateException"); }

        // cancelled: CANCELLED + resultNow/exceptionNow throw ISE
        // (FJT records no exception object for cancellation, so exceptionNow is ISE,
        //  not the CancellationException suggested by the Future interface default)
        RecursiveTask<Integer> can = new RecursiveTask<Integer>() {
            protected Integer compute() { return 1; }
        };
        check(can.cancel(true), "cancel fresh task");
        check(can.state() == Future.State.CANCELLED, "cancelled task state == CANCELLED");
        try { can.resultNow(); check(false, "resultNow on CANCELLED must throw ISE"); }
        catch (IllegalStateException e) { check(true, "resultNow on CANCELLED -> IllegalStateException"); }
        try { can.exceptionNow(); check(false, "exceptionNow on CANCELLED must throw ISE"); }
        catch (IllegalStateException e) { check(true, "exceptionNow on CANCELLED -> IllegalStateException (FJT: no exception recorded)"); }
    }

    // ---- Part P: timed quietlyJoin variants ----
    static void testTimedQuietlyJoin() throws Exception {
        RecursiveTask<Integer> done = new RecursiveTask<Integer>() {
            protected Integer compute() { return 3; }
        };
        done.invoke();
        check(done.quietlyJoin(100, TimeUnit.MILLISECONDS), "quietlyJoin(timeout) true for a completed task");
        check(done.quietlyJoinUninterruptibly(100, TimeUnit.MILLISECONDS),
              "quietlyJoinUninterruptibly(timeout) true for a completed task");

        // never-forked task cannot complete: both variants time out and return false
        RecursiveTask<Integer> never = new RecursiveTask<Integer>() {
            protected Integer compute() { return 4; }
        };
        check(!never.quietlyJoin(50, TimeUnit.MILLISECONDS), "quietlyJoin(timeout) false for an unstarted task");
        check(!never.quietlyJoinUninterruptibly(50, TimeUnit.MILLISECONDS),
              "quietlyJoinUninterruptibly(timeout) false for an unstarted task");
        check(never.state() == Future.State.RUNNING, "unstarted task still RUNNING after timed quietlyJoin");
    }

    // ---- Part Q: adaptInterruptible forms ----
    static void testAdaptInterruptible() throws Exception {
        final AtomicBoolean ran = new AtomicBoolean(false);
        ForkJoinTask<?> ar = ForkJoinTask.adaptInterruptible(new Runnable() {
            public void run() { ran.set(true); }
        });
        check(ar.invoke() == null, "adaptInterruptible(Runnable).invoke returns null");
        check(ran.get(), "adaptInterruptible(Runnable) ran the runnable");

        ForkJoinTask<String> arr = ForkJoinTask.adaptInterruptible(new Runnable() {
            public void run() { }
        }, "done");
        check("done".equals(arr.invoke()), "adaptInterruptible(Runnable,T) returns T after execution");

        ForkJoinTask<Integer> ac = ForkJoinTask.adaptInterruptible(new Callable<Integer>() {
            public Integer call() { return 200; }
        });
        check(ac.invoke() == 200, "adaptInterruptible(Callable) returns the callable value");

        final RuntimeException boom = new IllegalStateException("ai");
        ForkJoinTask<Integer> bad = ForkJoinTask.adaptInterruptible(new Callable<Integer>() {
            public Integer call() { throw boom; }
        });
        try {
            bad.invoke();
            check(false, "adaptInterruptible(Callable) failure must rethrow");
        } catch (RuntimeException e) {
            check(e == boom, "adaptInterruptible(Callable) same-thread rethrows the original exception");
        }
        check(bad.isCompletedAbnormally(), "adaptInterruptible failing task completed abnormally");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
        testStateMatrix();
        testTimedQuietlyJoin();
        testAdaptInterruptible();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
