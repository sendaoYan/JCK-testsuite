/*
 * @test
 * @summary java.util.concurrent.FutureTask API conformance: isDone/isCancelled before and
 *          after run, and get returning the computed result.
 * @run main FutureTaskTest
 */

import java.util.concurrent.FutureTask;

public class FutureTaskTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFutureTask() throws Exception {
        FutureTask<Integer> ft = new FutureTask<>(() -> 42);
        check(!ft.isDone() && !ft.isCancelled(), "not done/cancelled before run");
        ft.run();
        check(ft.isDone() && !ft.isCancelled(), "done after run");
        check(ft.get() == 42, "get result");
    }

    public static void main(String[] args) throws Exception {
        testFutureTask();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
