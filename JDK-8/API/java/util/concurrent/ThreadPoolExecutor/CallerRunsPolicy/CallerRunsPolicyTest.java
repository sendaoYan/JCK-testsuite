/*
 * @test
 * @summary java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy conformance: runs in caller.
 * @run main CallerRunsPolicyTest
 */

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CallerRunsPolicyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testCallerRunsPolicy() {
        ThreadPoolExecutor exec = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(1));
        try {
            final boolean[] ran = new boolean[1];
            ThreadPoolExecutor.CallerRunsPolicy policy = new ThreadPoolExecutor.CallerRunsPolicy();
            policy.rejectedExecution(() -> { ran[0] = true; }, exec);
            check(ran[0], "CallerRunsPolicy runs the task in the caller thread");
        } finally {
            exec.shutdown();
        }
    }

    public static void main(String[] args) {
        testCallerRunsPolicy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
