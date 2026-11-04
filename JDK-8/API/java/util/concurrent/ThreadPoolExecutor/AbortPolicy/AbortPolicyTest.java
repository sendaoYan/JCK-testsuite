/*
 * @test
 * @summary java.util.concurrent.ThreadPoolExecutor.AbortPolicy conformance: throws on rejection.
 * @run main AbortPolicyTest
 */

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class AbortPolicyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAbortPolicy() {
        ThreadPoolExecutor exec = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(1));
        try {
            ThreadPoolExecutor.AbortPolicy policy = new ThreadPoolExecutor.AbortPolicy();
            boolean threw = false;
            try {
                policy.rejectedExecution(() -> { }, exec);
            } catch (RejectedExecutionException expected) {
                threw = true;
            }
            check(threw, "AbortPolicy throws RejectedExecutionException");
        } finally {
            exec.shutdown();
        }
    }

    public static void main(String[] args) {
        testAbortPolicy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
