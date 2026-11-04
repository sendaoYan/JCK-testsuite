/*
 * @test
 * @summary java.util.concurrent.RunnableFuture conformance: white-box structure + black-box
 *          behavior (run + Future get/isDone/isCancelled via FutureTask).
 * @run main RunnableFutureTest
 */

import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

public class RunnableFutureTest {
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
        Class<?> c = cls("java.util.concurrent.RunnableFuture");
        check(c.isInterface(), "RunnableFuture is an interface");
        Class<?>[] ifs = c.getInterfaces();
        boolean runnable = false, future = false;
        for (Class<?> i : ifs) {
            if (i == Runnable.class) runnable = true;
            if (i == Future.class) future = true;
        }
        check(runnable, "extends Runnable");
        check(future, "extends Future");
        hasMethod(c, "run");
        // FutureTask implements it
        Class<?> ft = cls("java.util.concurrent.FutureTask");
        check(RunnableFuture.class.isAssignableFrom(ft), "FutureTask implements RunnableFuture");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        RunnableFuture<Integer> f = new FutureTask<>(() -> 42);
        check(f instanceof Runnable, "instance is a Runnable");
        check(!f.isDone() && !f.isCancelled(), "not done/cancelled before run");
        f.run();
        check(f.isDone() && !f.isCancelled(), "done, not cancelled after run");
        check(f.get() == 42, "get returns the result");

        // the Runnable(Runnable, V) form stores the supplied result
        RunnableFuture<String> r = new FutureTask<>(() -> { }, "fixed");
        r.run();
        check("fixed".equals(r.get()), "Runnable with fixed result");
    }

    // ---- Part 3: run() is idempotent — callable executes exactly once ----
    static void testRunIdempotent() throws Exception {
        final java.util.concurrent.atomic.AtomicInteger calls =
            new java.util.concurrent.atomic.AtomicInteger();
        RunnableFuture<Integer> f = new FutureTask<>(() -> calls.incrementAndGet());
        check(!f.isDone(), "not done before run");
        f.run();
        check(f.isDone(), "isDone after first run");
        f.run();
        check(f.isDone(), "still done after second run");
        check(calls.get() == 1, "callable executed exactly once (run is idempotent)");
        check(f.get() == 1, "get returns the single computed result");
        check(!f.cancel(true), "cancel after completion returns false");
        check(f.get() == 1, "value still available after failed cancel");
    }

    // ---- Part 4: cancel before run suppresses execution; threaded run works ----
    static void testCancelBeforeRun() throws Exception {
        final java.util.concurrent.atomic.AtomicInteger calls =
            new java.util.concurrent.atomic.AtomicInteger();
        RunnableFuture<Integer> f = new FutureTask<>(() -> calls.incrementAndGet());
        check(f.cancel(true), "cancel(true) before run succeeds");
        f.run();
        check(calls.get() == 0, "run() after cancel does not execute the callable");
        check(f.isCancelled() && f.isDone(), "cancelled+done state");
        RunnableFuture<Integer> f2 = new FutureTask<>(() -> 5);
        Thread t = new Thread(f2, "rf-runner");
        t.setDaemon(true);
        t.start();
        t.join(5000);
        check(f2.isDone(), "run via a Thread completes the future");
        check(f2.get() == 5, "result available after threaded run");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testRunIdempotent();
        testCancelBeforeRun();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
