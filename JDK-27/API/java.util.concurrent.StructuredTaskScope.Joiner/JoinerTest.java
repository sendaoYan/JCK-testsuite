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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
