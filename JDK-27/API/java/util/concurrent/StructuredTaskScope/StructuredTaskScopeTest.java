/*
 * @test
 * @summary java.util.concurrent.StructuredTaskScope conformance: white-box structure + black-box behavior.
 * @enablePreview
 * @run main StructuredTaskScopeTest
 */

import java.util.*;
import java.util.concurrent.*;

public class StructuredTaskScopeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Class<?> cls(String n) {
        try { return Class.forName(n); } catch (ClassNotFoundException e) { throw new AssertionError(e); }
    }

    static void hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); } catch (NoSuchMethodException e) { throw new AssertionError("missing " + name + " on " + c.getName(), e); }
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = cls("java.util.concurrent.StructuredTaskScope");
        check(c.isInterface(), "StructuredTaskScope is an interface");
        check(AutoCloseable.class.isAssignableFrom(c), "extends AutoCloseable");
        hasMethod(c, "fork", Callable.class);
        hasMethod(c, "fork", Runnable.class);
        hasMethod(c, "join");
        hasMethod(c, "isCancelled");
        hasMethod(c, "close");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        try (StructuredTaskScope<String, List<String>, ExecutionException> scope =
                 StructuredTaskScope.open(StructuredTaskScope.Joiner.allSuccessfulOrThrow())) {
            StructuredTaskScope.Subtask<String> a = scope.fork(() -> "A");
            StructuredTaskScope.Subtask<String> b = scope.fork(() -> "B");
            check(!scope.isCancelled(), "!isCancelled while running");
            List<String> results = scope.join();
            check(results.size() == 2, "join() returns two results");
            check(results.contains("A") && results.contains("B"), "join() has both results");
            check(a.state() == StructuredTaskScope.Subtask.State.SUCCESS, "subtask A SUCCESS");
            check(b.state() == StructuredTaskScope.Subtask.State.SUCCESS, "subtask B SUCCESS");
            check("A".equals(a.get()), "subtask.get() returns value");
        }
    }

    // ---- Part 3: contract deep-dive ----
    static void testLifecycle() throws Exception {
        // default open(): join returns null (Void), subtasks run on virtual threads
        try (StructuredTaskScope<String, Void, ExecutionException> scope = StructuredTaskScope.open()) {
            final boolean[] virtual = new boolean[1];
            scope.fork(() -> { virtual[0] = Thread.currentThread().isVirtual(); return "v"; });
            Void r = scope.join();
            check(r == null, "default scope join returns null");
            check(virtual[0], "subtasks run on virtual threads by default");
            check(!scope.isCancelled(), "not cancelled after clean join");
        }

        // open + close with no fork and no join is legal
        StructuredTaskScope<Object, Void, ExecutionException> bare = StructuredTaskScope.open();
        bare.close();
        check(true, "close without fork or join is legal");

        // close is idempotent after proper usage
        StructuredTaskScope<String, Void, ExecutionException> s3 = StructuredTaskScope.open();
        s3.fork(() -> "x");
        s3.join();
        s3.close();
        s3.close();
        check(true, "second close after join returns normally");
    }

    static void testIllegalStates() throws Exception {
        // join() twice -> IllegalStateException
        try (StructuredTaskScope<String, Void, ExecutionException> scope = StructuredTaskScope.open()) {
            scope.join();
            try {
                scope.join();
                check(false, "second join should throw");
            } catch (IllegalStateException expected) {
                check(true, "second join throws IllegalStateException");
            }
        }
        // close() before join after forking -> IllegalStateException
        StructuredTaskScope<String, Void, ExecutionException> s2 = StructuredTaskScope.open();
        s2.fork(() -> "x");
        try {
            s2.close();
            check(false, "close before join should throw");
        } catch (IllegalStateException expected) {
            check("Owner did not join after forking".equals(expected.getMessage()),
                    "close-before-join message: " + expected.getMessage());
        }
        // after close, fork and join are rejected
        try {
            s2.fork(() -> "y");
            check(false, "fork after close should throw");
        } catch (IllegalStateException expected) {
            check(true, "fork after close throws IllegalStateException");
        }
        try {
            s2.join();
            check(false, "join after close should throw");
        } catch (IllegalStateException expected) {
            check(true, "join after close throws IllegalStateException");
        }
    }

    static void testJoinerMatrix() throws Exception {
        // allSuccessfulOrThrow: results in fork order
        try (StructuredTaskScope<String, List<String>, ExecutionException> scope =
                 StructuredTaskScope.open(StructuredTaskScope.Joiner.allSuccessfulOrThrow())) {
            scope.fork(() -> "A");
            scope.fork(() -> "B");
            scope.fork(() -> "C");
            List<String> r = scope.join();
            check(r.size() == 3 && "A".equals(r.get(0)) && "B".equals(r.get(1)) && "C".equals(r.get(2)),
                    "allSuccessfulOrThrow returns results in fork order: " + r);
        }
        // allSuccessfulOrThrow with one failure -> ExecutionException(cause identity)
        try (StructuredTaskScope<String, List<String>, ExecutionException> scope =
                 StructuredTaskScope.open(StructuredTaskScope.Joiner.allSuccessfulOrThrow())) {
            IllegalStateException boom = new IllegalStateException("asot-fail");
            scope.fork(() -> "ok");
            StructuredTaskScope.Subtask<String> bad = scope.fork(() -> { throw boom; });
            try {
                scope.join();
                check(false, "join with failed subtask should throw");
            } catch (ExecutionException ee) {
                check(ee.getCause() == boom, "allSuccessfulOrThrow join cause identity");
            }
            check(scope.isCancelled(), "scope cancelled after a failure");
            check(bad.state() == StructuredTaskScope.Subtask.State.FAILED, "failed subtask FAILED");
            check(bad.exception() == boom, "subtask.exception() identity");
        }
        // anySuccessfulOrThrow: one success + one failure -> the success value
        try (StructuredTaskScope<String, String, ExecutionException> scope =
                 StructuredTaskScope.open(StructuredTaskScope.Joiner.anySuccessfulOrThrow())) {
            scope.fork(() -> { throw new IllegalStateException("any-f1"); });
            scope.fork(() -> "win");
            check("win".equals(scope.join()), "anySuccessfulOrThrow returns the success");
        }
        // anySuccessfulOrThrow: all fail -> ExecutionException with the first failure as cause
        try (StructuredTaskScope<String, String, ExecutionException> scope =
                 StructuredTaskScope.open(StructuredTaskScope.Joiner.anySuccessfulOrThrow())) {
            IllegalStateException first = new IllegalStateException("any-e1");
            scope.fork(() -> { throw first; });
            scope.fork(() -> { throw new IllegalStateException("any-e2"); });
            try {
                scope.join();
                check(false, "all-failed anySuccessfulOrThrow should throw");
            } catch (ExecutionException ee) {
                check(ee.getCause() == first, "anySuccessfulOrThrow all-fail cause is a subtask exception");
            }
        }
        // awaitAllSuccessfulOrThrow: success -> null; failure -> ExecutionException
        try (StructuredTaskScope<String, Void, ExecutionException> scope =
                 StructuredTaskScope.open(StructuredTaskScope.Joiner.awaitAllSuccessfulOrThrow())) {
            scope.fork(() -> "a");
            scope.fork(() -> "b");
            check(scope.join() == null && !scope.isCancelled(),
                    "awaitAllSuccessfulOrThrow joins to null when all succeed");
        }
        try (StructuredTaskScope<String, Void, ExecutionException> scope =
                 StructuredTaskScope.open(StructuredTaskScope.Joiner.awaitAllSuccessfulOrThrow())) {
            IllegalStateException aw = new IllegalStateException("await-fail");
            scope.fork(() -> { throw aw; });
            try {
                scope.join();
                check(false, "awaitAllSuccessfulOrThrow failure should throw");
            } catch (ExecutionException ee) {
                check(ee.getCause() == aw, "awaitAllSuccessfulOrThrow cause identity");
            }
        }
    }

    static void testAllUntilCancellation() throws Exception {
        // predicate returning true on the first subtask cancels the scope
        try (StructuredTaskScope<String, List<StructuredTaskScope.Subtask<String>>, RuntimeException> scope =
                 StructuredTaskScope.open(StructuredTaskScope.Joiner.<String>allUntil(
                         sub -> sub.state() == StructuredTaskScope.Subtask.State.SUCCESS
                                 && "stop".equals(sub.get())))) {
            StructuredTaskScope.Subtask<String> stopper = scope.fork(() -> "stop");
            StructuredTaskScope.Subtask<String> slow = scope.fork(() -> {
                Thread.sleep(30_000);
                return "slow";
            });
            List<StructuredTaskScope.Subtask<String>> all = scope.join();
            check(scope.isCancelled(), "scope cancelled by predicate");
            check(all.size() == 2 && all.get(0) == stopper && all.get(1) == slow,
                    "allUntil join returns all subtasks in fork order");
            check(stopper.state() == StructuredTaskScope.Subtask.State.SUCCESS
                    && "stop".equals(stopper.get()), "triggering subtask completed");
            check(slow.state() == StructuredTaskScope.Subtask.State.UNAVAILABLE,
                    "cancelled subtask state UNAVAILABLE");
        }
    }

    static void testTimeoutCancellation() throws Exception {
        try (StructuredTaskScope<String, Void, ExecutionException> scope =
                 StructuredTaskScope.open(cfg -> cfg.withTimeout(java.time.Duration.ofMillis(300)))) {
            StructuredTaskScope.Subtask<String> slow = scope.fork(() -> {
                Thread.sleep(30_000);
                return "slow";
            });
            try {
                scope.join();
                check(false, "timeout join should throw");
            } catch (ExecutionException ee) {
                check(ee.getCause() instanceof StructuredTaskScope.CancelledByTimeoutException,
                        "timeout join throws ExecutionException(CancelledByTimeoutException): " + ee.getCause());
            }
            check(scope.isCancelled(), "scope cancelled after timeout");
            check(slow.state() == StructuredTaskScope.Subtask.State.UNAVAILABLE,
                    "interrupted subtask reports UNAVAILABLE after timeout cancellation");
        }
    }

    static void testThreadConfiguration() throws Exception {
        // custom thread factory -> platform threads with the factory's naming
        final java.util.List<String> threadNames =
                java.util.Collections.synchronizedList(new java.util.ArrayList<>());
        final boolean[] platform = new boolean[1];
        try (StructuredTaskScope<String, Void, ExecutionException> scope =
                 StructuredTaskScope.open(cfg -> cfg.withThreadFactory(r -> {
                     Thread t = new Thread(r, "sts-factory-thread");
                     t.setDaemon(true);
                     return t;
                 }))) {
            scope.fork(() -> {
                platform[0] = !Thread.currentThread().isVirtual();
                threadNames.add(Thread.currentThread().getName());
                return "x";
            });
            scope.join();
        }
        check(platform[0], "custom thread factory yields platform threads");
        check(threadNames.size() == 1 && "sts-factory-thread".equals(threadNames.get(0)),
                "subtask ran on the factory-produced thread: " + threadNames);

        // well-formed nesting: inner scope closed before outer - never throws
        StructuredTaskScope<String, Void, ExecutionException> outer = StructuredTaskScope.open();
        StructuredTaskScope<String, Void, ExecutionException> inner = StructuredTaskScope.open();
        inner.close();
        outer.close();
        check(true, "well-formed nested scopes close cleanly");
    }

    static void testForkVariants() throws Exception {
        // fork(Runnable): runs, get() is null, state SUCCESS
        try (StructuredTaskScope<Object, Void, ExecutionException> scope = StructuredTaskScope.open()) {
            final boolean[] ran = new boolean[1];
            StructuredTaskScope.Subtask<Object> st = scope.fork(() -> { ran[0] = true; });
            scope.join();
            check(ran[0], "Runnable subtask executed");
            check(st.get() == null, "Runnable subtask get() is null");
            check(st.state() == StructuredTaskScope.Subtask.State.SUCCESS, "Runnable subtask SUCCESS");
        }
        // fork(null) -> NPE
        try (StructuredTaskScope<String, Void, ExecutionException> scope = StructuredTaskScope.open()) {
            try {
                scope.fork((java.util.concurrent.Callable<String>) null);
                check(false, "fork(null) should throw NPE");
            } catch (NullPointerException expected) {
                check(true, "fork(null Callable) throws NullPointerException");
            }
            scope.join();
        }
        // Subtask as Supplier
        try (StructuredTaskScope<String, List<String>, ExecutionException> scope =
                 StructuredTaskScope.open(StructuredTaskScope.Joiner.allSuccessfulOrThrow())) {
            java.util.function.Supplier<String> sup = scope.fork(() -> "sup");
            scope.join();
            check("sup".equals(sup.get()), "Subtask is usable as a Supplier");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testLifecycle();
        testIllegalStates();
        testJoinerMatrix();
        testAllUntilCancellation();
        testTimeoutCancellation();
        testThreadConfiguration();
        testForkVariants();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
