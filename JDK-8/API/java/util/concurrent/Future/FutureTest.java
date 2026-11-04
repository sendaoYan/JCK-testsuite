/*
 * @test
 * @summary java.util.concurrent.Future API conformance: get/isDone/isCancelled via FutureTask.
 * @run main FutureTest
 */

import java.util.concurrent.FutureTask;

public class FutureTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFuture() throws Exception {
        FutureTask<Integer> ft = new FutureTask<>(() -> 5);
        check(!ft.isDone(), "not done initially");
        check(!ft.isCancelled(), "not cancelled initially");
        ft.run();
        check(ft.get() == 5, "get returns the result");
        check(ft.isDone(), "done after run");
        check(!ft.isCancelled(), "still not cancelled");
    }

    public static void main(String[] args) throws Exception {
        testFuture();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
