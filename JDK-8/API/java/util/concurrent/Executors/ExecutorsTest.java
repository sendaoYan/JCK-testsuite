/*
 * @test
 * @summary java.util.concurrent.Executors API conformance: factory-created pools execute
 *          a Callable and terminate.
 * @run main ExecutorsTest
 */

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ExecutorsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testExecutors() throws Exception {
        ExecutorService single = Executors.newSingleThreadExecutor();
        ExecutorService fixed = Executors.newFixedThreadPool(2);
        ExecutorService cached = Executors.newCachedThreadPool();
        ScheduledExecutorService sched = Executors.newScheduledThreadPool(1);
        try {
            Future<Integer> f = single.submit(new Callable<Integer>() {
                public Integer call() { return 42; }
            });
            check(f.get() == 42, "submit result");
        } finally {
            single.shutdown();
            fixed.shutdown();
            cached.shutdown();
            sched.shutdown();
        }
        check(single.awaitTermination(10, TimeUnit.SECONDS), "single terminated");
    }

    public static void main(String[] args) throws Exception {
        testExecutors();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
