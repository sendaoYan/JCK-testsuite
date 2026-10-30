/*
 * @test
 * @summary java.util.concurrent.Executor API conformance: execute runs a task.
 * @run main ExecutorTest
 */

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

public class ExecutorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testExecutor() {
        AtomicBoolean ran = new AtomicBoolean();
        Executor e = new Executor() {
            public void execute(Runnable r) {
                r.run();
            }
        };
        e.execute(() -> ran.set(true));
        check(ran.get(), "execute runs the task");
    }

    public static void main(String[] args) {
        testExecutor();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
