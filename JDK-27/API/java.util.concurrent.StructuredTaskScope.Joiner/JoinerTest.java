/*
 * @test
 * @summary java.util.concurrent.StructuredTaskScope.Joiner conformance: static
 *          factories and anySuccessfulOrThrow behaviour.
 * @run main JoinerTest
 * @enablePreview
 */

import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;

public class JoinerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(StructuredTaskScope.Joiner.class.isInterface(), "Joiner is an interface");

        check(StructuredTaskScope.Joiner.allSuccessfulOrThrow() != null,
              "allSuccessfulOrThrow() non-null");
        check(StructuredTaskScope.Joiner.anySuccessfulOrThrow() != null,
              "anySuccessfulOrThrow() non-null");
        check(StructuredTaskScope.Joiner.awaitAllSuccessfulOrThrow() != null,
              "awaitAllSuccessfulOrThrow() non-null");
        check(StructuredTaskScope.Joiner.allUntil(sub -> true) != null,
              "allUntil(predicate) non-null");

        try (StructuredTaskScope<String, String, ExecutionException> scope =
                 StructuredTaskScope.open(StructuredTaskScope.Joiner.anySuccessfulOrThrow())) {
            scope.fork(() -> "winner");
            check("winner".equals(scope.join()), "anySuccessfulOrThrow returns success");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
