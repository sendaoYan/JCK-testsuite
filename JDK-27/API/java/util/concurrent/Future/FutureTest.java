/*
 * @test
 * @summary java.util.concurrent.Future conformance: white-box structure + black-box behavior
 *          (get/get(timeout)/cancel/state/resultNow/exceptionNow).
 * @run main FutureTest
 */

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class FutureTest {
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
        Class<?> c = cls("java.util.concurrent.Future");
        check(c.isInterface(), "Future is an interface");
        hasMethod(c, "cancel", boolean.class);
        hasMethod(c, "isCancelled");
        hasMethod(c, "isDone");
        hasMethod(c, "get");
        hasMethod(c, "get", long.class, TimeUnit.class);
        hasMethod(c, "resultNow");
        hasMethod(c, "exceptionNow");
        hasMethod(c, "state");
        // nested State enum has the four documented values
        Class<?> st = cls("java.util.concurrent.Future$State");
        check(st.isEnum(), "Future.State is an enum");
        Object[] consts = st.getEnumConstants();
        check(consts.length == 4, "Future.State has 4 constants");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        FutureTask<Integer> ft = new FutureTask<>(() -> 5);
        check(!ft.isDone(), "not done initially");
        check(!ft.isCancelled(), "not cancelled initially");
        check(ft.state() == Future.State.RUNNING, "state RUNNING while pending");
        ft.run();
        check(ft.get() == 5, "get returns the result");
        check(ft.get(1, TimeUnit.SECONDS) == 5, "get(timeout) returns the result");
        check(ft.isDone() && !ft.isCancelled(), "done, not cancelled");
        check(ft.state() == Future.State.SUCCESS, "state SUCCESS after normal completion");
        check(ft.resultNow() == 5, "resultNow returns the result");

        // cancel
        FutureTask<Integer> ct = new FutureTask<>(() -> 9);
        check(ct.cancel(true), "cancel returns true");
        check(ct.isCancelled() && ct.isDone(), "cancelled and done");
        check(ct.state() == Future.State.CANCELLED, "state CANCELLED after cancel");
        try {
            ct.get();
            check(false, "get on cancelled should throw");
        } catch (CancellationException expected) {
            check(true, "get on cancelled throws CancellationException");
        }

        // resultNow on pending throws IllegalStateException
        FutureTask<Integer> pending = new FutureTask<>(() -> 1);
        try {
            pending.resultNow();
            check(false, "resultNow on pending should throw");
        } catch (IllegalStateException expected) {
            check(true, "resultNow on pending throws IllegalStateException");
        }

        // exceptionNow / get on an exceptionally-completed future
        FutureTask<Integer> failed = new FutureTask<>(() -> { throw new IllegalStateException("bad"); });
        failed.run();
        check(failed.state() == Future.State.FAILED, "state FAILED after exceptional completion");
        check(failed.exceptionNow() instanceof IllegalStateException, "exceptionNow returns the cause");
        try {
            failed.get();
            check(false, "get on failed should throw");
        } catch (ExecutionException expected) {
            check(expected.getCause() instanceof IllegalStateException, "get wraps the cause in ExecutionException");
        }

        // get(timeout) on a pending future times out
        FutureTask<Integer> slow = new FutureTask<>(() -> { Thread.sleep(500); return 0; });
        try {
            slow.get(1, TimeUnit.MILLISECONDS);
            check(false, "get(timeout) should time out");
        } catch (TimeoutException expected) {
            check(true, "get(timeout) throws TimeoutException on a pending future");
        } finally {
            // never ran, nothing to clean up
        }
    }

    // ---- Part 3: default-method state matrix driven by CompletableFuture ----
    static void testStateMatrix() throws Exception {
        // RUNNING -> SUCCESS
        CompletableFuture<String> running = new CompletableFuture<>();
        check(running.state() == Future.State.RUNNING, "incomplete CompletableFuture is RUNNING");
        boolean illRan = false;
        try { running.resultNow(); }
        catch (IllegalStateException expected) { illRan = true; }
        check(illRan, "resultNow on RUNNING throws IllegalStateException");
        running.complete("v");
        check(running.state() == Future.State.SUCCESS, "completed is SUCCESS");
        check("v".equals(running.resultNow()), "resultNow returns the value");
        boolean illExc = false;
        try { running.exceptionNow(); }
        catch (IllegalStateException expected) { illExc = true; }
        check(illExc, "exceptionNow on SUCCESS throws IllegalStateException");
        // cancel-failed corner: completed future cannot be cancelled
        check(!running.cancel(false), "cancel(false) on completed future returns false");
        check(running.state() == Future.State.SUCCESS, "state unchanged after failed cancel");

        // RUNNING -> CANCELLED
        CompletableFuture<Integer> cancelled = new CompletableFuture<>();
        check(cancelled.cancel(false), "cancel on pending succeeds");
        check(cancelled.state() == Future.State.CANCELLED, "cancelled is CANCELLED");
        boolean illRes = false;
        // JDK-19+ contract (default impl, and CF's override alike): resultNow on a
        // cancelled future throws IllegalStateException, NOT CancellationException
        try { cancelled.resultNow(); }
        catch (IllegalStateException expected) { illRes = true; }
        check(illRes, "resultNow on CANCELLED throws IllegalStateException");
        boolean illExc2 = false;
        try { cancelled.exceptionNow(); }
        catch (IllegalStateException expected) { illExc2 = true; }
        check(illExc2, "exceptionNow on CANCELLED throws IllegalStateException");
        // empirically-locked (JDK-8 & JDK-27 alike): CompletableFuture re-cancel keeps
        // returning true (it returns isCancelled()), unlike FutureTask.
        check(cancelled.cancel(true), "CompletableFuture re-cancel returns true again (实测)");

        // FutureTask default-method messages (实测, JDK-27 default impl wording)
        FutureTask<Integer> ftCancelled = new FutureTask<>(() -> 1);
        ftCancelled.cancel(false);
        try { ftCancelled.resultNow(); }
        catch (IllegalStateException expected) {
            check("Task was cancelled".equals(expected.getMessage()),
                  "FutureTask cancelled resultNow message locked");
        }
        FutureTask<Integer> ftOk = new FutureTask<>(() -> 1);
        ftOk.run();
        try { ftOk.exceptionNow(); }
        catch (IllegalStateException expected) {
            check("Task completed with a result".equals(expected.getMessage()),
                  "FutureTask success exceptionNow message locked");
        }

        // RUNNING -> FAILED
        CompletableFuture<Integer> failed = new CompletableFuture<>();
        failed.completeExceptionally(new IllegalStateException("boom"));
        check(failed.state() == Future.State.FAILED, "exceptionally-completed is FAILED");
        check(failed.exceptionNow() instanceof IllegalStateException,
              "exceptionNow returns the cause");
        check("boom".equals(failed.exceptionNow().getMessage()), "cause message preserved");
        boolean illRes2 = false;
        try { failed.resultNow(); }
        catch (IllegalStateException expected) { illRes2 = true; }
        check(illRes2, "resultNow on FAILED throws IllegalStateException");
        check(CompletableFuture.failedFuture(new Exception()).state() == Future.State.FAILED,
              "failedFuture is FAILED");
        check(CompletableFuture.completedFuture(1).state() == Future.State.SUCCESS,
              "completedFuture is SUCCESS");
    }

    // ---- Part 4: get(timeout) unit handling on completed vs pending ----
    static void testTimeoutSemantics() throws Exception {
        Future<String> done = CompletableFuture.completedFuture("v");
        check("v".equals(done.get(0, TimeUnit.MILLISECONDS)),
              "zero timeout on a completed future returns the value");
        check("v".equals(done.get(1, TimeUnit.NANOSECONDS)),
              "1ns timeout on a completed future still returns the value");
        CompletableFuture<Integer> pending = new CompletableFuture<>();
        boolean got = false;
        try { pending.get(20, TimeUnit.MILLISECONDS); }
        catch (TimeoutException expected) { got = true; }
        check(got, "get(timeout) on a never-completing future throws TimeoutException");
        check(!pending.isDone(), "timing out does not complete the future");
        check(pending.cancel(true), "a timed-out pending future can still be cancelled");
        check(pending.isCancelled(), "cancelled after timeout");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testStateMatrix();
        testTimeoutSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
