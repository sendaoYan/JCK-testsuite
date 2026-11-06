/*
 * @test
 * @summary java.util.concurrent.FutureTask conformance: white-box structure + black-box
 *          behavior (run/get/cancel/set/setException, repeated run, cancellation,
 *          timed get, protected done hook).
 * @run main FutureTaskTest
 */

import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class FutureTaskTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Class<?> cls(String n) {
        try { return Class.forName(n); } catch (ClassNotFoundException e) { throw new AssertionError(e); }
    }

    static void hasCtor(Class<?> c, Class<?>... p) {
        try { c.getConstructor(p); } catch (NoSuchMethodException e) { throw new AssertionError("missing ctor on " + c.getName(), e); }
        passed++;
    }

    static void hasDeclared(Class<?> c, String n, Class<?>... p) {
        try { c.getDeclaredMethod(n, p); } catch (NoSuchMethodException e) { throw new AssertionError("missing " + n + " on " + c.getName(), e); }
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = cls("java.util.concurrent.FutureTask");
        check(!c.isInterface(), "FutureTask is a class");
        check(RunnableFuture.class.isAssignableFrom(c), "implements RunnableFuture");
        hasCtor(c, java.util.concurrent.Callable.class);
        hasCtor(c, Runnable.class, Object.class);
        hasDeclared(c, "run");
        hasDeclared(c, "get");
        hasDeclared(c, "cancel", boolean.class);
        hasDeclared(c, "set", Object.class);
        hasDeclared(c, "setException", Throwable.class);
        hasDeclared(c, "done");
    }

    // ---- Part 2: black-box (behavioral) ----
    static class SetTask extends FutureTask<Integer> {
        SetTask() { super(() -> 0); }
        void doSet(int v) { set(v); }
        void doFail(Throwable t) { setException(t); }
    }

    static void testBlackBox() throws Exception {
        // Callable form; run once -> result
        AtomicInteger invocations = new AtomicInteger();
        FutureTask<Integer> ft = new FutureTask<>(() -> invocations.incrementAndGet());
        check(!ft.isDone() && !ft.isCancelled(), "not done/cancelled before run");
        ft.run();
        ft.run(); // repeated run must not re-invoke the Callable
        check(invocations.get() == 1, "Callable invoked exactly once across repeated run()");
        check(ft.isDone() && !ft.isCancelled(), "done, not cancelled after run");
        check(ft.get() == 1, "get result");
        check(!ft.cancel(true), "cancel after completion returns false");

        // Runnable + fixed result form
        FutureTask<String> r = new FutureTask<>(() -> { }, "fixed");
        r.run();
        check("fixed".equals(r.get()), "Runnable with fixed result");

        // cancel before run: run() is a no-op
        FutureTask<Integer> ct = new FutureTask<>(() -> 99);
        check(ct.cancel(true), "cancel returns true");
        ct.run();
        check(ct.isCancelled() && ct.isDone(), "cancelled and done");
        try {
            ct.get();
            check(false, "get on cancelled should throw");
        } catch (CancellationException expected) {
            check(true, "get on cancelled throws CancellationException");
        }

        // get(timeout) on a pending task times out
        FutureTask<Integer> pending = new FutureTask<>(() -> 5);
        try {
            pending.get(1, TimeUnit.MILLISECONDS);
            check(false, "get(timeout) should time out");
        } catch (TimeoutException expected) {
            check(true, "get(timeout) throws TimeoutException while pending");
        }

        // protected set() -> done/get
        SetTask st = new SetTask();
        st.doSet(10);
        check(st.isDone() && st.get() == 10, "set() completes the task with a result");

        // protected setException() -> done/get wraps cause
        SetTask ft2 = new SetTask();
        ft2.doFail(new IllegalStateException("bad"));
        check(ft2.isDone(), "setException() completes the task");
        try {
            ft2.get();
            check(false, "get should throw");
        } catch (ExecutionException expected) {
            check(expected.getCause() instanceof IllegalStateException, "get wraps the cause");
        }

        // protected done() hook runs on completion
        AtomicBoolean doneHook = new AtomicBoolean();
        FutureTask<Integer> hooked = new FutureTask<Integer>(() -> 1) {
            protected void done() { doneHook.set(true); }
        };
        hooked.run();
        check(doneHook.get(), "done() hook invoked on completion");
    }

    // get() on a pending task is interruptible: interrupt unblocks it with
    // InterruptedException and clears the interrupt flag is observed by the caller.
    static void testInterruptibleGet() throws Exception {
        FutureTask<Integer> pending = new FutureTask<>(() -> 5);
        CountDownLatch entered = new CountDownLatch(1);
        AtomicBoolean interrupted = new AtomicBoolean(false);
        Thread t = new Thread(() -> {
            entered.countDown();
            try {
                pending.get();
            } catch (InterruptedException expected) {
                interrupted.set(true);
            } catch (ExecutionException e) {
                throw new AssertionError(e);
            } finally {
                Thread.currentThread().interrupt(); // restore flag for cleanup
            }
        });
        t.start();
        entered.await();
        t.interrupt();
        t.join(5000);
        check(!t.isAlive(), "get() thread finished after interrupt");
        check(interrupted.get(), "get() throws InterruptedException when interrupted");
    }

    // cancel(false) on a RUNNING task: returns true, does NOT interrupt the
    // running computation, and the computed result is discarded (get -> CE).
    static void testCancelFalseWhileRunning() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        final boolean[] interruptedInside = {false};
        FutureTask<Integer> ft = new FutureTask<>(() -> {
            started.countDown();
            try { release.await(); } catch (InterruptedException e) { interruptedInside[0] = true; }
            return 7;
        });
        Thread t = new Thread(ft::run);
        t.setDaemon(true);
        t.start();
        check(started.await(5, TimeUnit.SECONDS), "task running (cancel(false) scenario)");
        check(ft.cancel(false), "cancel(false) on running task returns true");
        release.countDown();
        t.join(5000);
        check(!t.isAlive(), "running thread finished");
        check(!interruptedInside[0], "cancel(false) does not interrupt the computation");
        check(ft.isCancelled() && ft.isDone(), "cancelled and done after cancel(false)");
        try {
            ft.get();
            check(false, "get after cancel(false) should throw");
        } catch (CancellationException expected) {
            check(true, "computed result discarded: get -> CancellationException");
        }
    }

    // cancel(true) on a RUNNING task interrupts the thread running the callable.
    // The callable parks on a latch that is never released, so the interrupt is
    // the only way it unblocks — no test-side release races the interrupt.
    static void testCancelTrueWhileRunning() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        final int[] interruptCount = {0};
        FutureTask<Integer> ft = new FutureTask<>(() -> {
            started.countDown();
            try { new CountDownLatch(1).await(); } catch (InterruptedException e) { interruptCount[0]++; }
            return 8;
        });
        Thread t = new Thread(ft::run);
        t.setDaemon(true);
        t.start();
        check(started.await(5, TimeUnit.SECONDS), "task running (cancel(true) scenario)");
        check(ft.cancel(true), "cancel(true) on running task returns true");
        t.join(5000);
        check(!t.isAlive(), "interrupted runner finished");
        check(interruptCount[0] >= 1, "cancel(true) delivered InterruptedException to computation");
        check(ft.isCancelled(), "cancelled after cancel(true)");
    }

    // protected runAndReset(): true on a fresh task (callable runs, no result set,
    // isDone stays false); false once set()/cancel()/callable-throw terminated it.
    static class RRTask extends FutureTask<Integer> {
        final int[] calls;
        RRTask(java.util.concurrent.Callable<Integer> c, int[] calls) { super(c); this.calls = calls; }
        boolean rnr() { return runAndReset(); }
        void inject(int v) { set(v); }
    }

    static void testRunAndReset() throws Exception {
        int[] calls = {0};
        RRTask t = new RRTask(() -> { calls[0]++; return 1; }, calls);
        check(t.rnr(), "runAndReset on fresh task returns true");
        check(calls[0] == 1, "runAndReset invoked the callable");
        check(!t.isDone() && !t.isCancelled(), "runAndReset leaves task runnable (no result)");
        check(t.rnr(), "runAndReset can run the task again");
        check(calls[0] == 2, "callable invoked twice across two runAndReset");
        RRTask done = new RRTask(() -> { calls[0]++; return 1; }, calls);
        done.inject(9);
        check(!done.rnr(), "runAndReset false after set()");
        check(calls[0] == 2, "callable not re-invoked after set()");
        RRTask cancelled = new RRTask(() -> { calls[0]++; return 1; }, calls);
        cancelled.cancel(false);
        check(!cancelled.rnr(), "runAndReset false after cancel()");
        check(calls[0] == 2, "callable not invoked for cancelled runAndReset");
        RRTask throwing = new RRTask(() -> { calls[0]++; throw new RuntimeException("boom"); }, calls);
        check(!throwing.rnr(), "runAndReset false when callable throws");
        check(throwing.isDone(), "throwing runAndReset completes the task exceptionally");
        try {
            throwing.get();
            check(false, "get after throwing runAndReset should throw");
        } catch (ExecutionException expected) {
            check(true, "runAndReset wraps callable failure in ExecutionException");
        }
    }

    // done() fires exactly once per terminal state: on cancel, and on set()+run().
    static void testDoneHookCounts() throws Exception {
        final int[] onCancel = {0};
        FutureTask<Integer> c = new FutureTask<Integer>(() -> 1) {
            protected void done() { onCancel[0]++; }
        };
        c.cancel(true);
        check(onCancel[0] == 1, "done() invoked exactly once on cancel");
        c.run(); // no-op, must not re-fire done()
        check(onCancel[0] == 1, "done() not re-invoked by no-op run after cancel");

        class DTask extends FutureTask<Integer> {
            int count;
            DTask() { super(() -> 1); }
            void inject(int v) { set(v); }
            protected void done() { count++; }
        }
        DTask d = new DTask();
        d.inject(5);
        d.run();
        d.run();
        check(d.count == 1, "done() invoked exactly once for set-then-run");
        check(d.get() == 5, "injected result preserved");
    }

    // set() before run(): subsequent run() is a no-op and the callable never runs.
    static void testSetBeforeRun() throws Exception {
        final int[] calls = {0};
        class PreSet extends FutureTask<Integer> {
            PreSet() { super(() -> { calls[0]++; return 1; }); }
            void inject(int v) { set(v); }
        }
        PreSet p = new PreSet();
        p.inject(10);
        p.run();
        p.run();
        check(calls[0] == 0, "callable never invoked when result was set before run");
        check(p.isDone(), "task done after set()");
        check(p.get() == 10, "get returns pre-set value");
    }

    // Multiple threads blocked in get() are all released by one completion.
    static void testMultipleWaiters() throws Exception {
        class WaiterTask extends FutureTask<Integer> {
            WaiterTask() { super(() -> 1); }
            void inject(int v) { set(v); }
        }
        final WaiterTask ft = new WaiterTask();
        final CountDownLatch entered = new CountDownLatch(3);
        final CountDownLatch finished = new CountDownLatch(3);
        final int[] results = {0, 0, 0};
        for (int i = 0; i < 3; i++) {
            final int idx = i;
            Thread t = new Thread(() -> {
                try {
                    entered.countDown();
                    results[idx] = ft.get();
                } catch (Exception e) {
                    throw new AssertionError(e);
                } finally {
                    finished.countDown();
                }
            });
            t.setDaemon(true);
            t.start();
        }
        check(entered.await(5, TimeUnit.SECONDS), "all waiters parked in get()");
        ft.inject(42);
        check(finished.await(5, TimeUnit.SECONDS), "all waiters released by completion");
        check(results[0] == 42 && results[1] == 42 && results[2] == 42, "all waiters got the value");
    }

    // get(timeout) in terminal states returns immediately (no TimeoutException).
    static void testTimedGetTerminalStates() throws Exception {
        FutureTask<Integer> done = new FutureTask<>(() -> 3);
        done.run();
        check(done.get(5, TimeUnit.SECONDS) == 3, "timed get on completed returns value immediately");
        FutureTask<Integer> cancelled = new FutureTask<>(() -> 3);
        cancelled.cancel(false);
        try {
            cancelled.get(5, TimeUnit.SECONDS);
            check(false, "timed get on cancelled should throw");
        } catch (CancellationException expected) {
            check(true, "timed get on cancelled -> CancellationException");
        }
        FutureTask<Integer> failed = new FutureTask<>(() -> { throw new IllegalArgumentException("x"); });
        failed.run();
        try {
            failed.get(5, TimeUnit.SECONDS);
            check(false, "timed get on failed should throw");
        } catch (ExecutionException expected) {
            check(expected.getCause() instanceof IllegalArgumentException, "timed get on failed -> ExecutionException");
        }
    }

    // ExecutionException carries the original exception object by identity.
    static void testExceptionIdentity() throws Exception {
        RuntimeException boom = new RuntimeException("identity");
        FutureTask<Integer> ft = new FutureTask<>(() -> { throw boom; });
        ft.run();
        try {
            ft.get();
            check(false, "get should throw");
        } catch (ExecutionException expected) {
            check(expected.getCause() == boom, "ExecutionException cause is the original object (==)");
        }
    }

    // FutureTask is NOT Serializable (contract: no Serializable interface).
    static void testNotSerializable() throws Exception {
        check(!java.io.Serializable.class.isAssignableFrom(FutureTask.class),
                "FutureTask does not implement Serializable");
        FutureTask<Integer> done = new FutureTask<>(() -> 42);
        done.run();
        try {
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
            oos.writeObject(done);
            oos.close();
            check(false, "writing a FutureTask should throw NotSerializableException");
        } catch (java.io.NotSerializableException expected) {
            check(true, "writeObject(FutureTask) -> NotSerializableException");
        }
    }

    // A FutureTask cancelled while queued in an executor is a no-op when the
    // pool eventually dequeues it: the callable never runs. A FIFO tail task
    // behind it proves the worker already dequeued (and skipped) it.
    static void testQueuedCancelledIsNoOp() throws Exception {
        final int[] calls = {0};
        CountDownLatch gateStarted = new CountDownLatch(1);
        CountDownLatch gateOpened = new CountDownLatch(1);
        java.util.concurrent.ThreadPoolExecutor pool = new java.util.concurrent.ThreadPoolExecutor(
                1, 1, 0L, TimeUnit.MILLISECONDS,
                new java.util.concurrent.ArrayBlockingQueue<Runnable>(4));
        try {
            FutureTask<Integer> gate = new FutureTask<>(() -> {
                gateStarted.countDown();
                gateOpened.await(5, TimeUnit.SECONDS);
                return 0;
            });
            FutureTask<Integer> queued = new FutureTask<>(() -> { calls[0]++; return 1; });
            FutureTask<Integer> tail = new FutureTask<>(() -> 2);
            pool.execute(gate);
            check(gateStarted.await(5, TimeUnit.SECONDS), "worker is busy on the gate task");
            pool.execute(queued); // FIFO: parks in the queue behind the busy worker
            pool.execute(tail);   // FIFO: parks behind `queued`
            check(queued.cancel(false), "queued task cancelled");
            gateOpened.countDown();
            check(tail.get(5, TimeUnit.SECONDS) == 2, "tail done: worker dequeued past the cancelled task");
            check(gate.get(5, TimeUnit.SECONDS) == 0, "gate task completed");
            check(queued.isDone(), "cancelled queued task reached terminal state");
            try {
                queued.get();
                check(false, "get on queue-cancelled task should throw");
            } catch (CancellationException expected) {
                check(true, "queue-cancelled get -> CancellationException");
            }
            check(calls[0] == 0, "callable of cancelled queued task never invoked");
        } finally {
            pool.shutdownNow();
        }
    }

    // JDK-8 records the version gap: no state() and no toString() override
    // (both added to FutureTask in JDK 19+).
    static void testStateMethodAbsentJdk8() throws Exception {
        try {
            FutureTask.class.getMethod("state");
            check(false, "JDK-8 FutureTask must not expose state()");
        } catch (NoSuchMethodException expected) {
            check(true, "JDK-8 FutureTask has no state() (added in JDK 19+)");
        }
        check(FutureTask.class.getMethod("toString").getDeclaringClass() == Object.class,
                "JDK-8 FutureTask does not override toString()");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testInterruptibleGet();
        testCancelFalseWhileRunning();
        testCancelTrueWhileRunning();
        testRunAndReset();
        testDoneHookCounts();
        testSetBeforeRun();
        testMultipleWaiters();
        testTimedGetTerminalStates();
        testExceptionIdentity();
        testNotSerializable();
        testQueuedCancelledIsNoOp();
        testStateMethodAbsentJdk8();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
