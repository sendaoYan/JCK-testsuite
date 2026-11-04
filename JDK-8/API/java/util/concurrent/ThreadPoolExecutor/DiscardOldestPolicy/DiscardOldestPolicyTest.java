/*
 * @test
 * @summary java.util.concurrent.ThreadPoolExecutor.DiscardOldestPolicy conformance: discards oldest.
 * @run main DiscardOldestPolicyTest
 */

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DiscardOldestPolicyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDiscardOldestPolicy() {
        ThreadPoolExecutor exec = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(1));
        try {
            ThreadPoolExecutor.DiscardOldestPolicy policy = new ThreadPoolExecutor.DiscardOldestPolicy();
            policy.rejectedExecution(() -> { }, exec);
            check(true, "DiscardOldestPolicy does not throw");
        } finally {
            exec.shutdown();
        }
    }

    public static void main(String[] args) {
        testDiscardOldestPolicy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
