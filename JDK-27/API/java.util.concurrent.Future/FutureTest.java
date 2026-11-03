/*
 * @test
 * @summary java.util.concurrent.Future conformance: white-box structure + black-box behavior
 *          (get/get(timeout)/cancel/state/resultNow/exceptionNow).
 * @run main FutureTest
 */

import java.util.concurrent.CancellationException;
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
