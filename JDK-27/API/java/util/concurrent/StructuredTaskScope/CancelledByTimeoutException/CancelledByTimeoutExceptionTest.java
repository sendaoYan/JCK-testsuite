/*
 * @test
 * @summary java.util.concurrent.StructuredTaskScope$CancelledByTimeoutException conformance: white-box structure + black-box behavior.
 * @enablePreview
 * @run main CancelledByTimeoutExceptionTest
 */

import java.util.concurrent.*;

public class CancelledByTimeoutExceptionTest {
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = cls("java.util.concurrent.StructuredTaskScope$CancelledByTimeoutException");
        check(c.getSuperclass() == RuntimeException.class, "extends RuntimeException");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final class");
        hasCtor(c);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        StructuredTaskScope.CancelledByTimeoutException e = new StructuredTaskScope.CancelledByTimeoutException();
        check(e instanceof RuntimeException, "instanceof RuntimeException");
        check(e.getMessage() == null, "no-arg ctor null message");
    }

    // ---- Part 3: real timeout scenario (thrown from an actual scope cancellation) ----
    static void testThrownByTimeout() throws Exception {
        try (StructuredTaskScope<String, Void, ExecutionException> scope =
                 StructuredTaskScope.open(cfg ->
                         cfg.withTimeout(java.time.Duration.ofMillis(200)))) {
            StructuredTaskScope.Subtask<String> slow = scope.fork(() -> {
                Thread.sleep(30_000);
                return "slow";
            });
            try {
                scope.join();
                check(false, "join with expired timeout should throw");
            } catch (ExecutionException ee) {
                Throwable cause = ee.getCause();
                check(cause.getClass() == StructuredTaskScope.CancelledByTimeoutException.class,
                        "join throws ExecutionException caused by exactly CancelledByTimeoutException: " + cause);
                check(cause instanceof RuntimeException,
                        "CancelledByTimeoutException is a RuntimeException when thrown");
                check(cause.getMessage() == null, "thrown instance has a null message");
                check(cause.getStackTrace().length > 0,
                        "thrown instance carries a stack trace from the timeout site");
            }
            check(scope.isCancelled(), "scope is cancelled after the timeout fired");
            check(slow.state() == StructuredTaskScope.Subtask.State.UNAVAILABLE,
                    "the hung subtask never completed: UNAVAILABLE");
        }
    }

    static void testNotThrownWithoutTimeout() throws Exception {
        // without a configured timeout a slow scope simply waits for its subtasks
        try (StructuredTaskScope<String, Void, ExecutionException> scope = StructuredTaskScope.open()) {
            scope.fork(() -> "quick");
            check(scope.join() == null, "no timeout configured: join returns normally");
            check(!scope.isCancelled(), "scope never cancelled");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testThrownByTimeout();
        testNotThrownWithoutTimeout();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
