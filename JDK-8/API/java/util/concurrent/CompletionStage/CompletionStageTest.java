/*
 * @test
 * @summary java.util.concurrent.CompletionStage API conformance: thenApply/thenAccept.
 * @run main CompletionStageTest
 */

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class CompletionStageTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testCompletionStage() throws Exception {
        CompletionStage<Integer> stage = CompletableFuture.completedFuture(21);
        CompletionStage<String> mapped = stage.thenApply(x -> "v" + (x * 2));
        CompletableFuture<String> f = mapped.toCompletableFuture();
        check("v42".equals(f.get()), "thenApply transforms the value");
    }

    public static void main(String[] args) throws Exception {
        testCompletionStage();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
