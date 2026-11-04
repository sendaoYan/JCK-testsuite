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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testInterruptibleGet();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
