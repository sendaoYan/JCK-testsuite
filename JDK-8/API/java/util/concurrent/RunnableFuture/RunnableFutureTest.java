/*
 * @test
 * @summary java.util.concurrent.RunnableFuture API conformance: run/result via FutureTask.
 * @run main RunnableFutureTest
 */

import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

public class RunnableFutureTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testRunnableFuture() throws Exception {
        RunnableFuture<Integer> f = new FutureTask<Integer>(() -> 42);
        check(f instanceof Runnable, "extends Runnable");
        f.run();
        check(!f.isCancelled() && f.isDone(), "isDone after run");
        check(f.get() == 42, "get returns the result");
    }

    public static void main(String[] args) throws Exception {
        testRunnableFuture();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
