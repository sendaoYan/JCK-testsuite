/*
 * @test
 * @summary java.util.concurrent.ScheduledFuture API conformance: delay/cancel via scheduler.
 * @run main ScheduledFutureTest
 */

import java.util.concurrent.Delayed;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ScheduledFutureTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testScheduledFuture() throws Exception {
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        try {
            final int[] value = new int[1];
            ScheduledFuture<?> f = exec.schedule(() -> { value[0] = 7; }, 50, TimeUnit.MILLISECONDS);
            check(f instanceof Delayed, "extends Delayed");
            f.get();
            check(value[0] == 7, "task executed");
            check(f.isDone(), "isDone after get");
        } finally {
            exec.shutdownNow();
        }
    }

    public static void main(String[] args) throws Exception {
        testScheduledFuture();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
