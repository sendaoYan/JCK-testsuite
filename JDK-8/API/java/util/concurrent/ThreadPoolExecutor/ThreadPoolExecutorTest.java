/*
 * @test
 * @summary java.util.concurrent.ThreadPoolExecutor API conformance: submit a Callable and
 *          terminate.
 * @run main ThreadPoolExecutorTest
 */

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolExecutorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testThreadPoolExecutor() throws Exception {
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                                                        new LinkedBlockingQueue<Runnable>());
        Future<Integer> f = tpe.submit(new Callable<Integer>() {
            public Integer call() { return 7; }
        });
        check(f.get() == 7, "submit result");
        tpe.shutdown();
        check(tpe.awaitTermination(10, TimeUnit.SECONDS), "terminates");
    }

    public static void main(String[] args) throws Exception {
        testThreadPoolExecutor();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
