/*
 * @test
 * @summary java.lang.Runnable API conformance: run() executes synchronously on the calling
 *          thread and on a new thread when passed to Thread.start().
 * @run main RunnableTest
 */

public class RunnableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        final boolean[] ran = {false};
        Runnable r = () -> ran[0] = true;
        r.run();
        check(ran[0], "run() executes synchronously on calling thread");

        final boolean[] threadRan = {false};
        Thread t = new Thread(() -> threadRan[0] = true);
        t.start();
        t.join();
        check(threadRan[0], "run() executes on started thread");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
