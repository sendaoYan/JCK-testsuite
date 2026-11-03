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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
