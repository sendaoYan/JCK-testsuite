/*
 * @test
 * @summary java.util.concurrent.CompletableFuture.AsynchronousCompletionTask marker interface conformance.
 * @run main AsynchronousCompletionTaskTest
 */

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class AsynchronousCompletionTaskTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class MarkerExec implements Executor, CompletableFuture.AsynchronousCompletionTask {
        public void execute(Runnable r) { r.run(); }
    }

    static void testMarker() {
        check(CompletableFuture.AsynchronousCompletionTask.class.isInterface(), "is an interface");
        CompletableFuture<Integer> cf = CompletableFuture.supplyAsync(() -> 1, new MarkerExec());
        check(cf.join() == 1, "supplyAsync with a marked executor");
    }

    public static void main(String[] args) {
        testMarker();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
