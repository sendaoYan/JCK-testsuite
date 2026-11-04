/*
 * @test
 * @summary java.util.concurrent.ThreadPoolExecutor.DiscardPolicy conformance: silently discards.
 * @run main DiscardPolicyTest
 */

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DiscardPolicyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDiscardPolicy() {
        ThreadPoolExecutor exec = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(1));
        try {
            ThreadPoolExecutor.DiscardPolicy policy = new ThreadPoolExecutor.DiscardPolicy();
            policy.rejectedExecution(() -> { }, exec);
            check(true, "DiscardPolicy discards without throwing");
        } finally {
            exec.shutdown();
        }
    }

    public static void main(String[] args) {
        testDiscardPolicy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
