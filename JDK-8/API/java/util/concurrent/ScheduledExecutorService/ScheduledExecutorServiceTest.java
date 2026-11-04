/*
 * @test
 * @summary java.util.concurrent.ScheduledExecutorService API conformance: schedule a Callable.
 * @run main ScheduledExecutorServiceTest
 */

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduledExecutorServiceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testScheduledExecutorService() throws Exception {
        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
        try {
            Future<Integer> f = ses.schedule(new Callable<Integer>() {
                public Integer call() { return 99; }
            }, 0, TimeUnit.MILLISECONDS);
            check(f.get() == 99, "scheduled result");
        } finally {
            ses.shutdown();
        }
    }

    public static void main(String[] args) throws Exception {
        testScheduledExecutorService();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
