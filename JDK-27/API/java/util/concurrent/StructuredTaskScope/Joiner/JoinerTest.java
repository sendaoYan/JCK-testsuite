/*
 * @test
 * @summary java.util.concurrent.StructuredTaskScope$Joiner conformance: white-box structure + black-box behavior.
 * @enablePreview
 * @run main JoinerTest
 */

import java.util.*;
import java.util.concurrent.*;

public class JoinerTest {
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
        Class<?> c = cls("java.util.concurrent.StructuredTaskScope$Joiner");
        check(c.isInterface(), "Joiner is an interface");
        hasMethod(c, "onFork", StructuredTaskScope.Subtask.class);
        hasMethod(c, "onComplete", StructuredTaskScope.Subtask.class);
        hasMethod(c, "result");
        hasMethod(c, "timeout");
        hasMethod(c, "allSuccessfulOrThrow");
        hasMethod(c, "allSuccessfulOrThrow", java.util.function.Function.class);
        hasMethod(c, "anySuccessfulOrThrow");
        hasMethod(c, "anySuccessfulOrThrow", java.util.function.Function.class);
        hasMethod(c, "awaitAllSuccessfulOrThrow");
        hasMethod(c, "awaitAllSuccessfulOrThrow", java.util.function.Function.class);
        hasMethod(c, "allUntil", java.util.function.Predicate.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(StructuredTaskScope.Joiner.allSuccessfulOrThrow() != null, "allSuccessfulOrThrow()");
        check(StructuredTaskScope.Joiner.anySuccessfulOrThrow() != null, "anySuccessfulOrThrow()");
        check(StructuredTaskScope.Joiner.awaitAllSuccessfulOrThrow() != null, "awaitAllSuccessfulOrThrow()");
        check(StructuredTaskScope.Joiner.allUntil(sub -> true) != null, "allUntil(predicate)");

        try (StructuredTaskScope<String, String, ExecutionException> scope =
                 StructuredTaskScope.open(StructuredTaskScope.Joiner.anySuccessfulOrThrow())) {
            scope.fork(() -> "winner");
            check("winner".equals(scope.join()), "anySuccessfulOrThrow returns success");
        }
    }

    // ---- Part 3: contract deep-dive ----
    static final class RecordingJoiner implements StructuredTaskScope.Joiner<String, String, RuntimeException> {
        final java.util.concurrent.atomic.AtomicInteger forks = new java.util.concurrent.atomic.AtomicInteger();
        final java.util.concurrent.atomic.AtomicInteger completes = new java.util.concurrent.atomic.AtomicInteger();
        volatile StructuredTaskScope.Subtask<String> lastForked;
        volatile StructuredTaskScope.Subtask<String> lastCompleted;
        public boolean onFork(StructuredTaskScope.Subtask<String> sub) {
            forks.incrementAndGet();
            lastForked = sub;
            return false; // false: do not cancel
        }
        public boolean onComplete(StructuredTaskScope.Subtask<String> sub) {
            completes.incrementAndGet();
            lastCompleted = sub;
            return false;
        }
        public String result() { return "R" + forks.get() + "/" + completes.get(); }
        public String timeout() { return "TO"; }
    }

    static void testCustomJoinerCallbacks() throws Exception {
        RecordingJoiner rec = new RecordingJoiner();
        try (StructuredTaskScope<String, String, RuntimeException> scope = StructuredTaskScope.open(rec)) {
            StructuredTaskScope.Subtask<String> st = scope.fork(() -> "v");
            // onFork is invoked synchronously by fork(), with the new Subtask
            check(rec.forks.get() == 1, "onFork invoked once per fork (synchronously)");
            check(rec.lastForked == st, "onFork receives the forked Subtask");
            check(rec.completes.get() <= 1, "at most one completion observed before join");
            String r = scope.join();
            check("R1/1".equals(r), "join() returns the Joiner's result(): " + r);
            check(rec.completes.get() == 1, "onComplete invoked once per completed subtask");
            check(rec.lastCompleted != null && "v".equals(rec.lastCompleted.get()),
                    "onComplete receives the completed Subtask");
            check(!scope.isCancelled(), "returning false from callbacks never cancels");
        }
    }

    static class MyEx extends RuntimeException {
        MyEx(Throwable cause) { super(cause); }
    }

    static void testCustomExceptionFunction() throws Exception {
        // allSuccessfulOrThrow(fn): the function maps the failure to the thrown exception
        try (StructuredTaskScope<String, List<String>, MyEx> scope =
                 StructuredTaskScope.open(StructuredTaskScope.Joiner.allSuccessfulOrThrow(MyEx::new))) {
            IllegalStateException orig = new IllegalStateException("fn-orig");
            scope.fork(() -> { throw orig; });
            try {
                scope.join();
                check(false, "custom-function joiner should throw");
            } catch (MyEx me) {
                check(me.getCause() == orig, "custom exception function receives the original failure");
            }
        }
    }

    static void testFactoryJoiners() throws Exception {
        // allSuccessfulOrThrow returns fork-ordered results
        try (StructuredTaskScope<Integer, List<Integer>, ExecutionException> scope =
                 StructuredTaskScope.open(StructuredTaskScope.Joiner.allSuccessfulOrThrow())) {
            scope.fork(() -> 1);
            scope.fork(() -> 2);
            List<Integer> r = scope.join();
            check(r.size() == 2 && r.get(0) == 1 && r.get(1) == 2, "results in fork order: " + r);
        }
        // anySuccessfulOrThrow: both succeed -> one of the values (deterministic single fork)
        try (StructuredTaskScope<String, String, ExecutionException> scope =
                 StructuredTaskScope.open(StructuredTaskScope.Joiner.anySuccessfulOrThrow())) {
            scope.fork(() -> "only");
            check("only".equals(scope.join()), "anySuccessfulOrThrow single fork value");
        }
        // awaitAllSuccessfulOrThrow failure -> ExecutionException with identical cause
        try (StructuredTaskScope<String, Void, ExecutionException> scope =
                 StructuredTaskScope.open(StructuredTaskScope.Joiner.awaitAllSuccessfulOrThrow())) {
            IllegalStateException aw = new IllegalStateException("await-ex");
            scope.fork(() -> { throw aw; });
            try {
                scope.join();
                check(false, "awaitAllSuccessfulOrThrow should throw on failure");
            } catch (ExecutionException ee) {
                check(ee.getCause() == aw, "awaitAllSuccessfulOrThrow cause identity");
            }
        }
        // allUntil: predicate false for all -> join returns everything, no cancellation
        try (StructuredTaskScope<String, List<StructuredTaskScope.Subtask<String>>, RuntimeException> scope =
                 StructuredTaskScope.open(StructuredTaskScope.Joiner.<String>allUntil(sub -> false))) {
            StructuredTaskScope.Subtask<String> a = scope.fork(() -> "a");
            StructuredTaskScope.Subtask<String> bad = scope.fork(() -> { throw new IllegalStateException("au"); });
            List<StructuredTaskScope.Subtask<String>> all = scope.join();
            check(!scope.isCancelled(), "never-true predicate does not cancel");
            check(all.size() == 2 && all.get(0) == a && all.get(1) == bad, "allUntil lists all subtasks in fork order");
            check(bad.state() == StructuredTaskScope.Subtask.State.FAILED, "allUntil exposes FAILED subtask");
        }
    }

    static void testJoinerTimeoutHook() throws Exception {
        // timeout() returning a value: join returns that value on timeout instead of throwing
        RecordingJoiner rec = new RecordingJoiner();
        try (StructuredTaskScope<String, String, RuntimeException> scope =
                 StructuredTaskScope.open(rec, cfg -> cfg.withTimeout(java.time.Duration.ofMillis(200)))) {
            scope.fork(() -> { Thread.sleep(30_000); return "slow"; });
            check("TO".equals(scope.join()), "custom timeout() value returned by join");
            check(scope.isCancelled(), "timeout cancels the scope");
        }
        // timeout() throwing: join propagates that exception directly
        class ThrowingTimeout implements StructuredTaskScope.Joiner<String, String, MyEx> {
            public boolean onFork(StructuredTaskScope.Subtask<String> s) { return false; }
            public boolean onComplete(StructuredTaskScope.Subtask<String> s) { return false; }
            public String result() { return "normal"; }
            public String timeout() throws MyEx { throw new MyEx(null); }
        }
        try (StructuredTaskScope<String, String, MyEx> scope =
                 StructuredTaskScope.open(new ThrowingTimeout(),
                         cfg -> cfg.withTimeout(java.time.Duration.ofMillis(200)))) {
            scope.fork(() -> { Thread.sleep(30_000); return "slow"; });
            try {
                scope.join();
                check(false, "throwing timeout() should propagate");
            } catch (MyEx expected) {
                check(true, "timeout() exception propagates directly from join");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testCustomJoinerCallbacks();
        testCustomExceptionFunction();
        testFactoryJoiners();
        testJoinerTimeoutHook();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
