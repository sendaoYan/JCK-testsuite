/*
 * @test
 * @summary java.util.concurrent.ExecutorService API conformance: submit/get a Callable and
 *          shutdown/isShutdown.
 * @run main ExecutorServiceTest
 */

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ExecutorServiceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testExecutorService() throws Exception {
        ExecutorService exec = Executors.newSingleThreadExecutor();
        Future<Integer> f = exec.submit(() -> 42);
        check(f.get() == 42, "submit/get");
        exec.shutdown();
        check(exec.isShutdown(), "isShutdown");
    }

    public static void main(String[] args) throws Exception {
        testExecutorService();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
