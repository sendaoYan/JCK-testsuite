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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
