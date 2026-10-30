/*
 * @test
 * @summary java.util.concurrent.ScheduledThreadPoolExecutor API conformance: submit a
 *          Callable and terminate.
 * @run main ScheduledThreadPoolExecutorTest
 */

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ScheduledThreadPoolExecutorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testScheduledThreadPoolExecutor() throws Exception {
        ScheduledThreadPoolExecutor stpe = new ScheduledThreadPoolExecutor(1);
        try {
            Future<Integer> f = stpe.submit(new Callable<Integer>() {
                public Integer call() { return 5; }
            });
            check(f.get() == 5, "submit result");
        } finally {
            stpe.shutdown();
        }
        check(stpe.awaitTermination(10, TimeUnit.SECONDS), "terminates");
    }

    public static void main(String[] args) throws Exception {
        testScheduledThreadPoolExecutor();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
