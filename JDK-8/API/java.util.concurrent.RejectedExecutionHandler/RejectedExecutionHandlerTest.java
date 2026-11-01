/*
 * @test
 * @summary java.util.concurrent.RejectedExecutionHandler API conformance: rejectedExecution via AbortPolicy.
 * @run main RejectedExecutionHandlerTest
 */

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RejectedExecutionHandlerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testRejectedExecutionHandler() {
        ThreadPoolExecutor exec = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(1));
        try {
            RejectedExecutionHandler handler = new ThreadPoolExecutor.AbortPolicy();
            boolean threw = false;
            try {
                handler.rejectedExecution(() -> { }, exec);
            } catch (RejectedExecutionException expected) {
                threw = true;
            }
            check(threw, "AbortPolicy throws RejectedExecutionException");
        } finally {
            exec.shutdown();
        }
    }

    public static void main(String[] args) {
        testRejectedExecutionHandler();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
