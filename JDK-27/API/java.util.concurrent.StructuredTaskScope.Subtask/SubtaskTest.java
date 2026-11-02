/*
 * @test
 * @summary java.util.concurrent.StructuredTaskScope.Subtask conformance:
 *          extends Supplier, get()/state()/exception().
 * @run main SubtaskTest
 * @enablePreview
 */

import java.util.List;
import java.util.concurrent.StructuredTaskScope;
import java.util.function.Supplier;

public class SubtaskTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(StructuredTaskScope.Subtask.class.isInterface(), "Subtask is an interface");
        check(Supplier.class.isAssignableFrom(StructuredTaskScope.Subtask.class),
              "Subtask extends Supplier");

        try (StructuredTaskScope<String, List<String>,
                 java.util.concurrent.ExecutionException> scope =
                 StructuredTaskScope.open(StructuredTaskScope.Joiner.allSuccessfulOrThrow())) {
            StructuredTaskScope.Subtask<String> s = scope.fork(() -> "value");
            scope.join();
            check(StructuredTaskScope.Subtask.State.SUCCESS == s.state(),
                  "state() SUCCESS after completion");
            check("value".equals(s.get()), "get() returns the value");
        }

        // a failed subtask reports the exception
        try (StructuredTaskScope<String, List<StructuredTaskScope.Subtask<String>>, RuntimeException>
                 scope = StructuredTaskScope.open(
                         StructuredTaskScope.Joiner.allUntil(sub -> true))) {
            StructuredTaskScope.Subtask<String> bad =
                    scope.fork(() -> { throw new IllegalStateException("boom"); });
            scope.join();
            check(bad.state() == StructuredTaskScope.Subtask.State.FAILED,
                  "failed subtask state() FAILED");
            check(bad.exception() instanceof IllegalStateException,
                  "exception() returns the cause on failure");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
