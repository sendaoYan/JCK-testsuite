/*
 * @test
 * @summary java.util.concurrent.RunnableScheduledFuture API conformance: isPeriodic.
 * @run main RunnableScheduledFutureTest
 */

import java.util.concurrent.Executors;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class RunnableScheduledFutureTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testRunnableScheduledFuture() {
        ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
        try {
            RunnableScheduledFuture<?> oneShot = (RunnableScheduledFuture<?>)
                exec.schedule(() -> { }, 1, java.util.concurrent.TimeUnit.HOURS);
            RunnableScheduledFuture<?> periodic = (RunnableScheduledFuture<?>)
                exec.scheduleAtFixedRate(() -> { }, 1, 1, java.util.concurrent.TimeUnit.HOURS);
            check(!oneShot.isPeriodic(), "one-shot is not periodic");
            check(periodic.isPeriodic(), "fixed-rate is periodic");
        } finally {
            exec.shutdownNow();
        }
    }

    public static void main(String[] args) {
        testRunnableScheduledFuture();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
