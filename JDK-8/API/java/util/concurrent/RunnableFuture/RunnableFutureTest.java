/*
 * @test
 * @summary java.util.concurrent.RunnableFuture API conformance: run/result via FutureTask.
 * @run main RunnableFutureTest
 */

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class RunnableFutureTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testRunnableFuture() throws Exception {
        RunnableFuture<Integer> f = new FutureTask<Integer>(() -> 42);
        check(f instanceof Runnable, "extends Runnable");
        f.run();
        check(!f.isCancelled() && f.isDone(), "isDone after run");
        check(f.get() == 42, "get returns the result");
    }

    // ---- white-box: interface shape ----
    static void testWhiteBoxStructure() throws Exception {
        Class<RunnableFuture> c = RunnableFuture.class;
        check(c.isInterface(), "RunnableFuture is an interface");
        check(c.getTypeParameters().length == 1, "one type parameter <V>");
        boolean runnable = false, future = false;
        for (Class<?> i : c.getInterfaces()) {
            if (i == Runnable.class) runnable = true;
            if (i == Future.class) future = true;
        }
        check(runnable, "direct superinterface Runnable");
        check(future, "direct superinterface Future");
        check(c.getDeclaredMethods().length == 1, "exactly one declared method run()");
        check(Runnable.class.isAssignableFrom(c) && Future.class.isAssignableFrom(c),
              "RunnableFuture is both Runnable and Future");
        check(RunnableFuture.class.isAssignableFrom(FutureTask.class),
              "FutureTask implements RunnableFuture");
        check(RunnableFuture.class.isAssignableFrom(
                  java.util.concurrent.RunnableScheduledFuture.class),
              "RunnableScheduledFuture extends RunnableFuture");
    }

    // ---- run is idempotent: callable executes exactly once across two run() calls ----
    static void testRunIdempotent() throws Exception {
        final AtomicInteger calls = new AtomicInteger();
        RunnableFuture<Integer> f = new FutureTask<Integer>(new Callable<Integer>() {
            public Integer call() { return calls.incrementAndGet(); }
        });
        check(!f.isDone(), "not done before run");
        f.run();
        check(f.isDone(), "isDone after first run");
        f.run();
        check(f.isDone(), "still done after second run");
        check(calls.get() == 1, "callable executed exactly once (run is idempotent)");
        check(f.get() == 1, "get returns the single computed result");
    }

    // ---- cancelled before run: run() is a no-op ----
    static void testCancelBeforeRun() throws Exception {
        final AtomicInteger calls = new AtomicInteger();
        RunnableFuture<Integer> f = new FutureTask<Integer>(new Callable<Integer>() {
            public Integer call() { return calls.incrementAndGet(); }
        });
        check(f.cancel(true), "cancel(true) before run succeeds");
        f.run();
        check(calls.get() == 0, "run() after cancel does not execute the callable");
        check(f.isCancelled() && f.isDone(), "cancelled+done state");
    }

    // ---- Runnable+result form through the interface type ----
    static void testRunnableWithResult() throws Exception {
        RunnableFuture<String> f = new FutureTask<String>(new Runnable() { public void run() { } },
            "fixed");
        f.run();
        check("fixed".equals(f.get()), "Runnable form yields the fixed result");
        // executing the same RunnableFuture from a plain Thread (it IS a Runnable)
        RunnableFuture<Integer> f2 = new FutureTask<Integer>(() -> 5);
        Thread t = new Thread(f2, "rf-runner");
        t.setDaemon(true);
        t.start();
        t.join(5000);
        check(f2.isDone(), "run via a Thread completes the future");
        check(f2.get() == 5, "result available after threaded run");
    }

    public static void main(String[] args) throws Exception {
        testRunnableFuture();
        testWhiteBoxStructure();
        testRunIdempotent();
        testCancelBeforeRun();
        testRunnableWithResult();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
