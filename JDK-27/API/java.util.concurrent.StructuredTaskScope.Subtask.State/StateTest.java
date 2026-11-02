/*
 * @test
 * @summary java.util.concurrent.StructuredTaskScope.Subtask.State conformance:
 *          enum constants and SUCCESS/FAILED reporting.
 * @run main StateTest
 * @enablePreview
 */

import java.util.List;
import java.util.concurrent.StructuredTaskScope;

public class StateTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        StructuredTaskScope.Subtask.State[] all =
                StructuredTaskScope.Subtask.State.values();
        check(all.length == 3, "three states");
        check(StructuredTaskScope.Subtask.State.valueOf("UNAVAILABLE") ==
                StructuredTaskScope.Subtask.State.UNAVAILABLE, "valueOf UNAVAILABLE");
        check(StructuredTaskScope.Subtask.State.valueOf("SUCCESS") ==
                StructuredTaskScope.Subtask.State.SUCCESS, "valueOf SUCCESS");
        check(StructuredTaskScope.Subtask.State.valueOf("FAILED") ==
                StructuredTaskScope.Subtask.State.FAILED, "valueOf FAILED");

        try (StructuredTaskScope<String, List<StructuredTaskScope.Subtask<String>>, RuntimeException>
                 scope = StructuredTaskScope.open(
                         StructuredTaskScope.Joiner.allUntil(sub -> true))) {
            StructuredTaskScope.Subtask<String> bad =
                    scope.fork(() -> { throw new RuntimeException("boom"); });
            scope.join();
            check(bad.state() == StructuredTaskScope.Subtask.State.FAILED,
                  "failed subtask reports FAILED");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
