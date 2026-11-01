/*
 * @test
 * @summary java.util.concurrent.CompletionService API conformance: submit/take.
 * @run main CompletionServiceTest
 */

import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;

public class CompletionServiceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testCompletionService() throws Exception {
        CompletionService<String> cs =
            new ExecutorCompletionService<String>(Executors.newSingleThreadExecutor());
        cs.submit(() -> "result");
        check("result".equals(cs.take().get()), "take returns the completed task");
    }

    public static void main(String[] args) throws Exception {
        testCompletionService();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
