/*
 * @test
 * @summary java.util.concurrent.StructuredTaskScope$Subtask conformance: white-box structure + black-box behavior.
 * @enablePreview
 * @run main SubtaskTest
 */

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

public class SubtaskTest {
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
        Class<?> c = cls("java.util.concurrent.StructuredTaskScope$Subtask");
        check(c.isInterface(), "Subtask is an interface");
        check(Supplier.class.isAssignableFrom(c), "Subtask extends Supplier");
        hasMethod(c, "state");
        hasMethod(c, "get");
        hasMethod(c, "exception");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        try (StructuredTaskScope<String, List<String>, ExecutionException> scope =
                 StructuredTaskScope.open(StructuredTaskScope.Joiner.allSuccessfulOrThrow())) {
            StructuredTaskScope.Subtask<String> s = scope.fork(() -> "value");
            scope.join();
            check(s.state() == StructuredTaskScope.Subtask.State.SUCCESS, "state() SUCCESS");
            check("value".equals(s.get()), "get() returns the value");
        }
        try (StructuredTaskScope<String, List<StructuredTaskScope.Subtask<String>>, RuntimeException> scope =
                 StructuredTaskScope.open(StructuredTaskScope.Joiner.allUntil(sub -> true))) {
            StructuredTaskScope.Subtask<String> bad = scope.fork(() -> { throw new IllegalStateException("boom"); });
            scope.join();
            check(bad.state() == StructuredTaskScope.Subtask.State.FAILED, "failed subtask state FAILED");
            check(bad.exception() instanceof IllegalStateException, "exception() returns cause");
        }
    }

    // ---- Part 3: contract deep-dive ----
    static void testAccessBeforeJoinRejected() throws Exception {
        try (StructuredTaskScope<String, List<StructuredTaskScope.Subtask<String>>, RuntimeException> scope =
                 StructuredTaskScope.open(StructuredTaskScope.Joiner.allUntil(sub -> true))) {
            StructuredTaskScope.Subtask<String> st = scope.fork(() -> "v");
            try {
                st.get();
                check(false, "get() before join should throw");
            } catch (IllegalStateException expected) {
                check(expected.getMessage() == null, "get() before join ISE has null message");
            }
            try {
                st.exception();
                check(false, "exception() before join should throw");
            } catch (IllegalStateException expected) {
                check(expected.getMessage() == null, "exception() before join ISE has null message");
            }
            // state() is safe to call before join; its value is UNAVAILABLE while the
            // subtask is still running and is timing-dependent for fast subtasks, so we
            // only assert that the call itself never throws
            Object s0 = st.state();
            check(s0 != null, "state() is callable before join and yields an enum value");
            scope.join();
        }
    }

    static void testSuccessSubtask() throws Exception {
        try (StructuredTaskScope<String, List<String>, ExecutionException> scope =
                 StructuredTaskScope.open(StructuredTaskScope.Joiner.allSuccessfulOrThrow())) {
            String sentinel = new String("unique");
            StructuredTaskScope.Subtask<String> st = scope.fork(() -> sentinel);
            scope.join();
            check(st.state() == StructuredTaskScope.Subtask.State.SUCCESS, "SUCCESS state");
            check(st.get() == sentinel, "get() returns the identical object produced by the Callable");
            try {
                st.exception();
                check(false, "exception() on successful subtask should throw");
            } catch (IllegalStateException expected) {
                check(true, "exception() on SUCCESS throws IllegalStateException");
            }
        }
    }

    static void testFailedSubtask() throws Exception {
        try (StructuredTaskScope<String, List<StructuredTaskScope.Subtask<String>>, RuntimeException> scope =
                 StructuredTaskScope.open(StructuredTaskScope.Joiner.allUntil(sub -> true))) {
            IllegalStateException boom = new IllegalStateException("fs");
            StructuredTaskScope.Subtask<String> bad = scope.fork(() -> { throw boom; });
            scope.join();
            check(bad.state() == StructuredTaskScope.Subtask.State.FAILED, "FAILED state");
            check(bad.exception() == boom, "exception() returns the identical thrown object");
            check("fs".equals(bad.exception().getMessage()), "exception message preserved");
            try {
                bad.get();
                check(false, "get() on FAILED subtask should throw");
            } catch (IllegalStateException expected) {
                check(true, "get() on FAILED subtask throws IllegalStateException");
            }
        }
    }

    static void testRunnableSubtask() throws Exception {
        try (StructuredTaskScope<Object, Void, ExecutionException> scope = StructuredTaskScope.open()) {
            final boolean[] ran = new boolean[1];
            StructuredTaskScope.Subtask<Object> st = scope.fork(() -> { ran[0] = true; });
            scope.join();
            check(ran[0], "Runnable subtask ran");
            check(st.state() == StructuredTaskScope.Subtask.State.SUCCESS, "Runnable subtask SUCCESS");
            check(st.get() == null, "Runnable subtask get() returns null");
        }
    }

    static void testCancelledSubtask() throws Exception {
        // a subtask cancelled by scope cancellation reports UNAVAILABLE, not FAILED
        try (StructuredTaskScope<String, List<StructuredTaskScope.Subtask<String>>, RuntimeException> scope =
                 StructuredTaskScope.open(StructuredTaskScope.Joiner.<String>allUntil(
                         sub -> "stop".equals(sub.get())))) {
            scope.fork(() -> "stop");
            StructuredTaskScope.Subtask<String> slow = scope.fork(() -> {
                Thread.sleep(30_000);
                return "slow";
            });
            scope.join();
            check(slow.state() == StructuredTaskScope.Subtask.State.UNAVAILABLE,
                    "cancelled-before-completion subtask reports UNAVAILABLE");
        }
    }

    static void testSupplierTyping() throws Exception {
        try (StructuredTaskScope<String, List<String>, ExecutionException> scope =
                 StructuredTaskScope.open(StructuredTaskScope.Joiner.allSuccessfulOrThrow())) {
            Supplier<String> asSupplier = scope.fork(() -> "sup");
            scope.join();
            check("sup".equals(asSupplier.get()), "Subtask used through its Supplier supertype");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testAccessBeforeJoinRejected();
        testSuccessSubtask();
        testFailedSubtask();
        testRunnableSubtask();
        testCancelledSubtask();
        testSupplierTyping();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
