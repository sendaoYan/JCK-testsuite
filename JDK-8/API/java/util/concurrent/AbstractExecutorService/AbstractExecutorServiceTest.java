/*
 * @test
 * @summary java.util.concurrent.AbstractExecutorService API conformance: submit returns a Future.
 * @run main AbstractExecutorServiceTest
 */

import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class AbstractExecutorServiceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class DirectExecutor extends AbstractExecutorService {
        public void shutdown() {}

        public List<Runnable> shutdownNow() {
            return Collections.emptyList();
        }

        public boolean isShutdown() {
            return false;
        }

        public boolean isTerminated() {
            return false;
        }

        public boolean awaitTermination(long timeout, TimeUnit unit) {
            return false;
        }

        public void execute(Runnable command) {
            command.run();
        }
    }

    static void testAbstractExecutorService() throws Exception {
        AbstractExecutorService ses = new DirectExecutor();
        Future<Integer> f = ses.submit(new Callable<Integer>() {
            public Integer call() {
                return 3;
            }
        });
        check(f.get() == 3, "submit returns the task result");
    }

    public static void main(String[] args) throws Exception {
        testAbstractExecutorService();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
