/*
 * @test
 * @summary java.util.concurrent.ExecutorCompletionService API conformance: take returns a completed task.
 * @run main ExecutorCompletionServiceTest
 */

import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorCompletionServiceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testExecutorCompletionService() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(1);
        try {
            CompletionService<Integer> cs = new ExecutorCompletionService<>(pool);
            cs.submit(() -> 9);
            check(cs.take().get() == 9, "take returns the completed task result");
        } finally {
            pool.shutdown();
        }
    }

    public static void main(String[] args) throws Exception {
        testExecutorCompletionService();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
