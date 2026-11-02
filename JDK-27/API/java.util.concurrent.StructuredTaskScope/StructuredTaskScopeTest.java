/*
 * @test
 * @summary java.util.concurrent.StructuredTaskScope conformance: interface,
 *          open(Joiner), fork/join, and subtask results.
 * @run main StructuredTaskScopeTest
 * @enablePreview
 */

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;

public class StructuredTaskScopeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(StructuredTaskScope.class.isInterface(), "StructuredTaskScope is an interface");
        check(AutoCloseable.class.isAssignableFrom(StructuredTaskScope.class),
              "StructuredTaskScope extends AutoCloseable");

        try (StructuredTaskScope<String, List<String>, ExecutionException> scope =
                 StructuredTaskScope.open(StructuredTaskScope.Joiner.allSuccessfulOrThrow())) {
            StructuredTaskScope.Subtask<String> a = scope.fork(() -> "A");
            StructuredTaskScope.Subtask<String> b = scope.fork(() -> "B");
            List<String> results = scope.join();
            check(results.size() == 2, "join() returns two results");
            check(results.contains("A") && results.contains("B"), "join() has both results");
            check(a.state() == StructuredTaskScope.Subtask.State.SUCCESS, "subtask A SUCCESS");
            check(b.state() == StructuredTaskScope.Subtask.State.SUCCESS, "subtask B SUCCESS");
            check("A".equals(a.get()), "subtask.get() returns value");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
