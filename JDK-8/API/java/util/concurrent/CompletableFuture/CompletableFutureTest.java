/*
 * @test
 * @summary java.util.concurrent.CompletableFuture API conformance: completedFuture/get/join,
 *          thenApply, completeExceptionally/isCompletedExceptionally, and getNow.
 * @run main CompletableFutureTest
 */

import java.util.concurrent.CompletableFuture;

public class CompletableFutureTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testCompletableFuture() throws Exception {
        CompletableFuture<Integer> cf = CompletableFuture.completedFuture(42);
        check(cf.isDone() && !cf.isCancelled(), "isDone/isCancelled");
        check(cf.get() == 42, "get");
        check(cf.join() == 42, "join");
        CompletableFuture<Integer> doubled = cf.thenApply(x -> x * 2);
        check(doubled.join() == 84, "thenApply");
        CompletableFuture<Integer> failed = new CompletableFuture<>();
        failed.completeExceptionally(new IllegalStateException("boom"));
        check(failed.isCompletedExceptionally(), "isCompletedExceptionally");
        CompletableFuture<Integer> pending = new CompletableFuture<>();
        check(pending.getNow(-1) == -1, "getNow default on incomplete");
    }

    public static void main(String[] args) throws Exception {
        testCompletableFuture();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
