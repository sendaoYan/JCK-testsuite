/*
 * @test
 * @summary java.util.concurrent.Future API conformance: get/isDone/isCancelled via FutureTask.
 * @run main FutureTest
 */

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

public class FutureTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFuture() throws Exception {
        FutureTask<Integer> ft = new FutureTask<>(() -> 5);
        check(!ft.isDone(), "not done initially");
        check(!ft.isCancelled(), "not cancelled initially");
        ft.run();
        check(ft.get() == 5, "get returns the result");
        check(ft.isDone(), "done after run");
        check(!ft.isCancelled(), "still not cancelled");
    }

    // ---- white-box: interface method face (5 abstract methods, no defaults in JDK-8) ----
    static void testWhiteBoxStructure() throws Exception {
        Class<Future> c = Future.class;
        check(c.isInterface(), "Future is an interface");
        check(c.getTypeParameters().length == 1, "one type parameter <V>");
        check(c.getDeclaredMethods().length == 5, "exactly 5 declared methods (locked method face)");
        check(c.getMethod("cancel", boolean.class).getReturnType() == boolean.class, "cancel(boolean)");
        check(c.getMethod("isCancelled").getReturnType() == boolean.class, "isCancelled()");
        check(c.getMethod("isDone").getReturnType() == boolean.class, "isDone()");
        check(c.getMethod("get").getExceptionTypes().length == 2, "get() declares checked exceptions");
        check(c.getMethod("get", long.class, TimeUnit.class) != null, "get(long, TimeUnit)");
        check(Future.class.isAssignableFrom(FutureTask.class), "FutureTask implements Future");
        check(Future.class.isAssignableFrom(CompletableFuture.class),
              "CompletableFuture implements Future");
        check(java.util.concurrent.ScheduledFuture.class.isAssignableFrom(java.util.concurrent.RunnableScheduledFuture.class),
              "ScheduledFuture sub-hierarchy is assignable to Future");
    }

    // ---- behaviour driven by CompletableFuture.completedFuture ----
    static void testCompletedFuture() throws Exception {
        Future<String> done = CompletableFuture.completedFuture("v");
        check(done.isDone(), "completedFuture isDone");
        check(!done.isCancelled(), "completedFuture not cancelled");
        check("v".equals(done.get()), "get returns the value immediately");
        check("v".equals(done.get(1, TimeUnit.SECONDS)), "get(timeout) returns the value");
        check("v".equals(done.get(0, TimeUnit.MILLISECONDS)),
              "zero timeout on a completed future returns the value");
        check(!done.cancel(false), "cancel(false) on completed future returns false");
        check(!done.cancel(true), "cancel(true) on completed future returns false");
        check(done.isDone() && !done.isCancelled(), "state unchanged after failed cancel");
        check("v".equals(done.get()), "value still available after failed cancel");
    }

    // ---- cancel on a pending CompletableFuture ----
    static void testCancelPending() throws Exception {
        CompletableFuture<Integer> pending = new CompletableFuture<Integer>();
        check(pending.cancel(false), "cancel(false) on pending future returns true");
        check(pending.isCancelled(), "isCancelled after cancel");
        check(pending.isDone(), "isDone after cancel");
        boolean got = false;
        try { pending.get(); }
        catch (CancellationException expected) { got = true; }
        check(got, "get() on cancelled throws CancellationException");
        boolean got2 = false;
        try { pending.get(1, TimeUnit.MILLISECONDS); }
        catch (CancellationException expected) { got2 = true; }
        check(got2, "get(timeout) on cancelled throws CancellationException (not TimeoutException)");
        // empirically-locked quirk (both JDK-8 and JDK-27): CompletableFuture.cancel keeps
        // returning true on an already-cancelled future (it returns isCancelled()),
        // unlike FutureTask.cancel which returns false the second time.
        check(pending.cancel(true), "CompletableFuture re-cancel returns true again (实测)");
    }

    // ---- get(timeout) on a never-completing future -> TimeoutException ----
    static void testTimeoutOnPending() throws Exception {
        CompletableFuture<Integer> pending = new CompletableFuture<Integer>();
        boolean got = false;
        try { pending.get(20, TimeUnit.MILLISECONDS); }
        catch (TimeoutException expected) {
            check(expected.getMessage() == null, "timeout exception carries null message");
            got = true;
        }
        check(got, "get(timeout) on pending throws TimeoutException");
        check(!pending.isDone(), "timing out does not complete the future");
    }

    // ---- FutureTask: cancel-before-run suppresses execution; run is idempotent ----
    static void testFutureTaskLifecycle() throws Exception {
        final AtomicInteger runs = new AtomicInteger();
        FutureTask<Integer> ft = new FutureTask<Integer>(new Callable<Integer>() {
            public Integer call() { runs.incrementAndGet(); return 9; }
        });
        check(ft.cancel(false), "cancel before run succeeds");
        ft.run(); // must be a no-op now
        check(runs.get() == 0, "run() after cancel does not execute the callable");
        check(ft.isCancelled() && ft.isDone(), "cancelled state after suppressed run");
        boolean got = false;
        try { ft.get(); }
        catch (CancellationException expected) { got = true; }
        check(got, "get() after cancelled run throws CancellationException");

        final AtomicInteger runs2 = new AtomicInteger();
        FutureTask<Integer> ft2 = new FutureTask<Integer>(new Callable<Integer>() {
            public Integer call() { runs2.incrementAndGet(); return 11; }
        });
        ft2.run();
        ft2.run(); // second run is a no-op
        check(runs2.get() == 1, "second run() does not re-execute the callable");
        check(ft2.get() == 11 && ft2.isDone() && !ft2.isCancelled(),
              "state stable after re-run");
        check(!ft2.cancel(true), "cancel after completion returns false");
        check(ft2.get() == 11, "value still available after failed cancel");
    }

    // ---- FutureTask(Runnable, V) fixed-result form ----
    static void testRunnableFixedResult() throws Exception {
        FutureTask<String> f = new FutureTask<String>(new Runnable() { public void run() { } }, "fixed");
        f.run();
        check("fixed".equals(f.get()), "Runnable form yields the fixed result");
        FutureTask<Object> fNull = new FutureTask<Object>(new Runnable() { public void run() { } }, null);
        fNull.run();
        check(fNull.get() == null, "Runnable form with null result yields null");
    }

    public static void main(String[] args) throws Exception {
        testFuture();
        testWhiteBoxStructure();
        testCompletedFuture();
        testCancelPending();
        testTimeoutOnPending();
        testFutureTaskLifecycle();
        testRunnableFixedResult();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
